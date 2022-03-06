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
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.Toolbar;

import com.example.first_responder_app.MainActivity;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.first_responder_app.interfaces.DrawerLocker;
import com.example.first_responder_app.viewModels.LoginViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentLoginBinding;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<UsersDataModel> listOfUser;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //lock drawer on login page
        DrawerLocker drawerLocker = ((DrawerLocker)getActivity());
        if(drawerLocker != null){
            drawerLocker.setDrawerLocked(true);
        }


        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        FragmentLoginBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        //get user info for comparison
        listOfUser = new ArrayList<>();
        //TODO: change it to query single entry instead of entire user list
        populateUserList();

        //switch to Home fragment upon clicking it
        //also if you have any other code relates to onCreateView just add it from here
        binding.loginSubmit.setOnClickListener(v -> {

            if (listOfUser.size() == 0) {
                binding.loginLog.setText(R.string.emtpyDbMsg);
                binding.loginLog.setVisibility(View.VISIBLE);
            }
            mViewModel.setUsername(binding.loginUsername.getText().toString());
            mViewModel.setPassword(binding.loginPassword.getText().toString());
            if (checkUsernameExists(mViewModel.getUsername())) {
                if (checkPwMatch(mViewModel.getUsername(), mViewModel.getPassword())) {
                    NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
                    Navigation.findNavController(binding.getRoot()).navigate(action);
                    Log.d("testing", "username: " + mViewModel.getUsername() + " pw: " + mViewModel.getPassword() + " Login success.");
                } else {
                    binding.loginLog.setText(R.string.loginFailMsg);
                    binding.loginLog.setVisibility(View.VISIBLE);
                    Log.d("testing", "login failed: wrong username/password");
                }
            } else {
                binding.loginLog.setText(R.string.loginFailMsg);
                binding.loginLog.setVisibility(View.VISIBLE);
                Log.d("testing", "login failed: wrong username/password");
            }
        });

        binding.bypassBtn.setOnClickListener(v -> {
            NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        //unlock drawer when leaving login page
        DrawerLocker drawerLocker = ((DrawerLocker)getActivity());
        if(drawerLocker != null){
            drawerLocker.setDrawerLocked(false);
        }
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }

    // TODO: Do not read in all users? Needs to be fixed!
    private void populateUserList() {

        db.collection("users").get().addOnCompleteListener(usersTask -> {
            Log.d(TAG, "READ DATABASE - LOGIN FRAGMENT");
            if (usersTask.isSuccessful()) {
                for (QueryDocumentSnapshot userDoc : usersTask.getResult()) {
                    UsersDataModel usersDataModel = userDoc.toObject(UsersDataModel.class);

                    listOfUser.add(usersDataModel);
                }
            } else {
                Log.d(TAG, "db get failed in Login page " + usersTask.getException());
            }
        });
    }

    //check if username is valid
    private boolean checkUsernameExists(String username) {
        for (int i = 0; i < listOfUser.size(); i++) {
            if (listOfUser.get(i).getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    //check if password matches the record in db
    private boolean checkPwMatch(String username, String pw){
        for (int i = 0; i < listOfUser.size(); i++){
            if (listOfUser.get(i).getUsername().equals(username)){
                if (listOfUser.get(i).getPassword().equals(pw)){
                    ActiveUser activeUser = ((ActiveUser)getActivity());
                    if(activeUser != null){
                        activeUser.setActive(listOfUser.get(i));
                    }
                    return true;
                }
            }
        }
        return false;
    }
}