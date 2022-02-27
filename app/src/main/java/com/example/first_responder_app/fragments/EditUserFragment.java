package com.example.first_responder_app.fragments;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentEditUserBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.EditUserViewModel;
import com.example.first_responder_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class EditUserFragment extends Fragment {

    private EditUserViewModel mViewModel;
    private FirestoreDatabase firestoreDatabase;
    FirebaseFirestore db;
    private HashMap<String, String> ranksAndIds;
    private ActiveUser activeUser;
    private UsersDataModel user;

    public static EditUserFragment newInstance() {
        return new EditUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        FragmentEditUserBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_user, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        //sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        firestoreDatabase = new FirestoreDatabase();
        db = firestoreDatabase.getDb();

        activeUser = (ActiveUser)getActivity();
        if(activeUser != null){
            user = activeUser.getActive();
        }

        Spinner rankSpinner = binding.userRank;
        ranksAndIds = new HashMap<>();
        populateRanks(rankSpinner);

        populateEditTexts(binding.userFirstName, binding.userLastName, binding.userPhone, binding.userAddress);

        rankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                rankSpinner.setSelection(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // can leave this empty
            }
        });

        binding.saveButton.setOnClickListener(v -> {
            String firstName = binding.userFirstName.getText().toString();
            String lastName = binding.userLastName.getText().toString();
            String phone = binding.userPhone.getText().toString();
            String address = binding.userAddress.getText().toString();
            String id = "";
            if(activeUser != null){
                UsersDataModel user = activeUser.getActive();
                if (user != null) {
                    id = user.getDocumentId();
                }
            }
            String rankName = binding.userRank.getSelectedItem().toString();
            String rankID = ranksAndIds.get(rankName);

            if (id != null) {
                String errorMsg = "";
                if (!firestoreDatabase.validateName(firstName)) {
                    errorMsg = "First name has invalid format";
                } else if (!firestoreDatabase.validateName(lastName)) {
                    errorMsg = "Last name has invalid format";
                } else if (!firestoreDatabase.validatePhone(phone)) {
                    errorMsg = "Phone number has invalid format";
                }


                if (errorMsg.equals("")) {
                    //TODO: await
                    firestoreDatabase.editUser(firstName, lastName, rankID, phone, address, id, getActivity());
                    NavDirections action = EditUserFragmentDirections.actionEditUserFragmentToUserFragment();
                    Navigation.findNavController(binding.getRoot()).navigate(action);
                } else {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.d("User", "No active user found");
            }

        });

        return binding.getRoot();
    }

    public void populateRanks(Spinner rankSpinner) {

        ArrayList<String> rankNames = new ArrayList<>();

        db.collection("ranks")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("DB", "task is successful!!");

                            String initialRankName = null;
                            String initialRankId = null;
                            if (user != null) {
                                initialRankId = user.getRankId();
                            }

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //This array is used to populate the spinner
                                rankNames.add((String) document.get("rank_name"));

                                //This hashmap is used to get the id from the rank name selected in the spinner
                                ranksAndIds.putIfAbsent((String) document.get("rank_name"), document.getId());

                                if (initialRankId != null && initialRankId.equals(document.getId())) {
                                    initialRankName = (String) document.get("rank_name");
                                }
                            }
                            //Create spinner populated by ArrayList of rank names
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, rankNames);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            rankSpinner.setAdapter(arrayAdapter);

                            //Set initial position to be current rank of ActiveUser
                            if (initialRankName != null) {
                                int initialPosition = arrayAdapter.getPosition(initialRankName);
                                rankSpinner.setSelection(initialPosition);
                            }

                        } else {
                            Log.d("DB", "Error getting ranks: ", task.getException());
                        }
                    }
                });
    }

    public void populateEditTexts(EditText firstName, EditText lastName, EditText phone, EditText address) {
        if (user != null) {
            firstName.setText(user.getFirst_name());
            lastName.setText(user.getLast_name());
            phone.setText(Long.toString(user.getPhone_number()));
            address.setText(user.getAddress());

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditUserViewModel.class);
    }

}