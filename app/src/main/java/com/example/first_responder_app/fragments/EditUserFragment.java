package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.MainActivity;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentEditUserBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.EditUserViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.viewModels.UserViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class EditUserFragment extends Fragment {

    private EditUserViewModel mViewModel;
    private FirestoreDatabase firestoreDatabase;
    FirebaseFirestore db;
    private HashMap<String, String> ranksAndIds;
    private ActiveUser activeUser;
    private UsersDataModel user;
    private ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_IMAGE_SELECT = 1002;
    public static final int MULTIPLE_PERMISSIONS = 100;
    public static final String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String currentPhotoPath;
    private Context applicationContext;
    private Uri selectedImage;
    private String dbPicPath;
    FragmentEditUserBinding binding;
    public static EditUserFragment newInstance() {
        return new EditUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_user, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        //sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        firestoreDatabase = new FirestoreDatabase();
        db = firestoreDatabase.getDb();
        applicationContext = MainActivity.getContextOfApplication();
        getPermission();

        activeUser = (ActiveUser) getActivity();
        if (activeUser != null) {
            user = activeUser.getActive();
        }

        imageView = binding.imageViewForProfilePic;
        ranksAndIds = new HashMap<>();


        populateEditTexts(binding.userFirstName, binding.userLastName, binding.userPhone, binding.userAddress, binding.userEmailAddr);


        //taking picture option
        binding.floatingActionButton5.setOnClickListener(v -> {
            try {
                if (ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermission();
                } else {
                    dispatchTakePictureIntent();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //uploading a picture
        binding.uploadProfilePicBtn.setOnClickListener(v -> {
            try {
                if (ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermission();
                } else {
                    openGallery();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.saveButton.setOnClickListener(v -> {
            String firstName = binding.userFirstName.getText().toString();
            String lastName = binding.userLastName.getText().toString();
            String phone = binding.userPhone.getText().toString();
            String address = binding.userAddress.getText().toString();
            String email = binding.userEmailAddr.getText().toString();
            String id = "";

            if (activeUser != null) {
                UsersDataModel user = activeUser.getActive();
                if (user != null) {
                    id = user.getDocumentId();
                }
            }

            if (id != null) {
                String errorMsg = "";
                if (!firestoreDatabase.validateName(firstName)) {
                    errorMsg = "First name has invalid format";
                } else if (!firestoreDatabase.validateName(lastName)) {
                    errorMsg = "Last name has invalid format";
                } else if (!firestoreDatabase.validatePhone(phone)) {
                    errorMsg = "Phone number has invalid format";
                } else if(email.equals("")){
                    errorMsg = "Email can't be empty";
                }


                if (errorMsg.equals("")) {
                    //TODO: await
                    firestoreDatabase.editUser(firstName, lastName, user.getRank_id(), phone, address, email, id, getActivity());
                    uploadImage();

                    user.setFirst_name(firstName);
                    user.setLast_name(lastName);
                    user.setPhone_number(phone);
                    user.setAddress(address);
                    user.setEmail(email);
                    user.setRemote_path_to_profile_picture(dbPicPath);
                    UserViewModel userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
                    userViewModel.setUserDataModel(user);

                } else {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.d("User", "No active user found");
            }

        });
        
        
        ImageView profilePictureImageView = binding.imageViewForProfilePic;
        //load user profile picture
        try {
            final File localFile = File.createTempFile("Images", "bmp");
            StorageReference ref = FirestoreDatabase.profilePictureRef.child(user.getRemote_path_to_profile_picture());
            ref.getFile(localFile)
                    .addOnSuccessListener(bytes -> {
                        try {
                            profilePictureImageView.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                        } catch (Exception e) {
                            Log.d(TAG, "onCreateView: No profile picture found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "getUserProfile: Could not load profile picture!", e);
                    });
        } catch (IOException e) {
            Log.e(TAG, "onCreateView: Failed creating temp file", e);
        }catch(IllegalArgumentException e){
            Log.d(TAG, "No profile picture");
        }

        return binding.getRoot();
    }



    public void populateEditTexts(EditText firstName, EditText lastName, EditText phone, EditText address, EditText email) {
        if (user != null) {
            firstName.setText(user.getFirst_name());
            lastName.setText(user.getLast_name());
            phone.setText(user.getPhone_number());
            address.setText(user.getAddress());
            email.setText(user.getEmail());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditUserViewModel.class);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECT) {
            displaySelectedImage(data);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            displayImage();
        }
    }

    //picture part
    public void getPermission(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions,
                    MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_IMAGE_SELECT:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                }
                break;
        }
        if(requestCode == MULTIPLE_PERMISSIONS){
            Toast.makeText(getContext(), "Permissions Granted", Toast.LENGTH_SHORT).show();
        }
    }

    //upload picture part
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, REQUEST_IMAGE_SELECT);
    }

    private void displaySelectedImage(Intent data) {
        try {
            selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = applicationContext.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            currentPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap temp = BitmapFactory.decodeFile(currentPhotoPath);
            imageView.setImageBitmap(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImage() {
        if (selectedImage != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            dbPicPath = UUID.randomUUID().toString();
            // Defining the child of storageReference
            StorageReference ref
                    = firestoreDatabase.getStorage().getReference()
                    .child(
                            "profile_pictures/"
                                    + dbPicPath);

            // adding listeners on upload
            // or failure of image

            ref.putFile(selectedImage)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(getContext(),
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();

                                    NavDirections action = EditUserFragmentDirections.actionEditUserFragmentToUserFragment();
                                    Navigation.findNavController(binding.getRoot()).navigate(action);
                                    db.collection("users").document(user.getDocumentId()).update("remote_path_to_profile_picture", (dbPicPath));
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(getContext(),
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                }
                            });
        }else{
            NavDirections action = EditUserFragmentDirections.actionEditUserFragmentToUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        }

    }

    //taking picture part
    private void displayImage() {
        if (currentPhotoPath != null) {
            Bitmap temp = BitmapFactory.decodeFile(currentPhotoPath);
            imageView.setImageBitmap(temp);
        } else {
            Toast.makeText(getContext(), "Image Path is null", Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                selectedImage = photoURI;
                Log.d(TAG, "dispatchTakePictureIntent: ");
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

}