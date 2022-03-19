package com.example.first_responder_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.CreateUserFragmentBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.interfaces.DrawerLocker;
import com.example.first_responder_app.viewModels.CreateUserViewModel;

public class CreateUserFragment extends Fragment {

    private CreateUserViewModel mViewModel;
    private CreateUserFragmentBinding binding;

    public static CreateUserFragment newInstance() {
        return new CreateUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DrawerLocker drawerLocker = ((DrawerLocker) getActivity());
        if (drawerLocker != null) {
            drawerLocker.setDrawerLocked(true);
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.create_user_fragment, container, false);

        mViewModel = new ViewModelProvider(requireActivity()).get(CreateUserViewModel.class);
        binding.createUserFireDepartmentIdEditText.setText(mViewModel.getFireDepartmentId());

        binding.createUserCreateButton.setOnClickListener(v -> {
            if (validInput()) {
                FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.FIRE_DEPARTMENT_COLLECTION_DIR).document(binding.createUserFireDepartmentIdEditText.getText().toString()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.toObject(UsersDataModel.class) != null) {
                                UsersDataModel newUser = new UsersDataModel();
                                fillUser(newUser);

                                FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.USERS_COLLECTION_DIR).add(newUser).addOnSuccessListener(documentReference -> {
                                    // Success, login
                                    if (FirestoreDatabase.getInstance().setActiveUser(newUser)) {
                                        newUser.setDocumentId(documentReference.getId());
                                        ActiveUser activeUser = ((ActiveUser)getActivity());
                                        activeUser.setActive(newUser);

                                        NavDirections action = CreateUserFragmentDirections.actionCreateUserFragmentToHomeFragment();
                                        Navigation.findNavController(binding.getRoot()).navigate(action);
                                    } else {
                                        Toast.makeText(getContext(), "User does not have a department", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload user. Network error", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(getContext(), "Fire department does not exist", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Cannot check for fire department. Network error", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "You must fill all of the text fields", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private void fillUser(UsersDataModel user) {
        user.setFirst_name(binding.createUserFirstNameEditText.getText().toString());
        user.setLast_name(binding.createUserLastNameEditText.getText().toString());
        user.setUsername(binding.createUserUsernameEditText.getText().toString());
        user.setPassword(binding.createUserPasswordEditText.getText().toString());
        user.setEmail(binding.createUserEmailEditText.getText().toString());
        user.setPhone_number(binding.createUserPhoneNumberEditText.getText().toString());
        user.setAddress(binding.createUserAddressEditText.getText().toString());
        user.setFire_department_id(binding.createUserFireDepartmentIdEditText.getText().toString());
    }

    private boolean validInput() {
        return !"".equals(binding.createUserFirstNameEditText.getText().toString()) &&
                !"".equals(binding.createUserLastNameEditText.getText().toString()) &&
                !"".equals(binding.createUserUsernameEditText.getText().toString()) &&
                !"".equals(binding.createUserPasswordEditText.getText().toString()) &&
                !"".equals(binding.createUserEmailEditText.getText().toString()) &&
                !"".equals(binding.createUserPhoneNumberEditText.getText().toString()) &&
                !"".equals(binding.createUserAddressEditText.getText().toString()) &&
                !"".equals(binding.createUserFireDepartmentIdEditText.getText().toString());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CreateUserViewModel.class);
    }

}