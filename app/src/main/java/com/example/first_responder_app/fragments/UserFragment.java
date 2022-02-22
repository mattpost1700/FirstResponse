package com.example.first_responder_app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentUserBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.UserViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class UserFragment extends Fragment {

    private UserViewModel mViewModel;
    private FirestoreDatabase firestoreDatabase;
    private FirebaseFirestore db;
    private ActiveUser activeUser;
    private UsersDataModel user;

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        FragmentUserBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        firestoreDatabase = new FirestoreDatabase();
        db = firestoreDatabase.getDb();

        activeUser = (ActiveUser)getActivity();
        if(activeUser != null){
            user = activeUser.getActive();
        }

        populateUser(binding.userName, binding.userPhone, binding.userAddress, binding.userRank);

        binding.editButton.setOnClickListener(v -> {
            NavDirections action = UserFragmentDirections.actionUserFragmentToEditUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);

        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        // TODO: Use the ViewModel
    }

    public void populateUser(TextView userName, TextView phone, TextView address, TextView rank) {
        if(activeUser != null && user != null){
            db.collection("ranks").document(user.getRank())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                rank.setText((String) document.get("rank_name"));
                            } else {
                                Log.d("DB", "No such document");
                            }
                        } else {
                            Log.d("DB", "get failed with ", task.getException());
                        }
                    }
                });
            String name = user.getFirst_name().trim() + " " + user.getLast_name().trim();
            userName.setText(name);
            String phoneNum = Long.toString(user.getPhone_number());
            phone.setText(phoneNum);
            address.setText(user.getAddress());
        }
    }

}