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
import androidx.lifecycle.LiveData;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.databinding.FragmentEditUserBinding;
import com.example.first_responder_app.viewModels.EditUserViewModel;
import com.example.first_responder_app.R;
//import com.example.first_responder_app.viewModels.SharedViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class EditUserFragment extends Fragment {

    private EditUserViewModel mViewModel;
    //private SharedViewModel sharedViewModel;
    private FirestoreDatabase firestoreDatabase;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    private HashMap<String, String> ranksAndIds;

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

        Spinner rankSpinner = binding.userRank;
        ranksAndIds = new HashMap<>();
        populateRanks(rankSpinner);

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
            //LiveData<String> id = sharedViewModel.getUserID();
            //String b = id.getValue();
            //TODO: pass document id
            String id = "kkpk8zvwk1cZh5N20a9z";
            String rankName = binding.userRank.getSelectedItem().toString();
            String rankID = ranksAndIds.get(rankName);

            firestoreDatabase.editUser(firstName, lastName, rankID, phone, address, id);

            NavDirections action = EditUserFragmentDirections.actionEditUserFragmentToUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);

        });

        return binding.getRoot();
    }

    public void populateRanks(Spinner rankSpinner) {
        //ArrayList<String> rankNames = firestoreDatabase.getRanks();
        //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, rankNames);
        //arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //rankSpinner.setAdapter(arrayAdapter);

        ArrayList<String> rankNames = new ArrayList<>();
        db.collection("ranks")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("DB", "task is successfull!!");
                            Log.d("DB", String.valueOf(task.getResult()));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("DB", "rankname is : " + document.get("rank_name"));
                                rankNames.add((String) document.get("rank_name"));
                                ranksAndIds.putIfAbsent((String) document.get("rank_name"), document.getId());
                                Log.d("DB", "ranknames is now" + rankNames);
                            }
                            Log.d("DB", "ranknames before populating is " + rankNames);
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, rankNames);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            rankSpinner.setAdapter(arrayAdapter);
                        } else {
                            Log.d("DB", "Error getting ranks: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditUserViewModel.class);
        // TODO: Use the ViewModel
    }

}