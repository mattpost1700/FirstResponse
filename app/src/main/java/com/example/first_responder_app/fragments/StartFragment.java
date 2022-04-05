package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.example.first_responder_app.databinding.StartFragmentBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.StartViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class StartFragment extends Fragment {

    private StartViewModel mViewModel;

    private StartFragmentBinding binding;
    NavHostFragment navHostFragment;
    NavController navController;

    public static StartFragment newInstance() {
        return new StartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.start_fragment, container, false);
        navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String usernameQuickLogin = sharedPref.getString("savedUsername", null);
        String passwordQuickLogin = sharedPref.getString("savedPassword", null);
        Log.d("testing", "usernameQuick: " + usernameQuickLogin);
        Log.d("testing", "pwQuick: " + passwordQuickLogin);
        if (usernameQuickLogin != null && passwordQuickLogin != null){

            FirestoreDatabase.getInstance().getDb().collection("users")
                    .whereEqualTo("username", usernameQuickLogin)
                    .whereEqualTo("password", passwordQuickLogin)
                    .get().addOnCompleteListener(usersTask -> {
                Log.d(TAG, "READ DATABASE - LOGIN FRAGMENT");
                if (usersTask.isSuccessful()) {
                    for (QueryDocumentSnapshot userDoc : usersTask.getResult()) {
                        UsersDataModel user = userDoc.toObject(UsersDataModel.class);
                        //checkExist = true;
                        if(user.getPassword().equals(passwordQuickLogin)) {
                            Log.d(TAG, "onStart: LOGIN");
                            ActiveUser activeUser = ((ActiveUser)getActivity());
                            activeUser.setActive(user);

                            if(FirestoreDatabase.getInstance().setActiveUser(user)) {
                                successfullyHidAdminOptions(user);

                                NavDirections action = StartFragmentDirections.actionStartFragmentToHomeFragment();
                                Navigation.findNavController(binding.getRoot()).navigate(action);
                            } else {
                                // User cannot make good queries
                                Toast.makeText(getContext(), "User does not have a department", Toast.LENGTH_SHORT).show();
                                goToLogin();
                            }
                        } else {
                            goToLogin();
                        }
                    }
                } else {
                    Log.d(TAG, "db get failed in Login page " + usersTask.getException());
                    //checkExist = false;
                    goToLogin();
                }
            });
        } else {
            goToLogin();
        }

        return binding.getRoot();
    }

    private void goToLogin() {
        NavDirections action = StartFragmentDirections.actionStartFragmentToLoginFragment();
        navController.navigate(action);
    }

    private void successfullyHidAdminOptions(UsersDataModel user) {
        try {
            if(user == null || !user.isIs_admin()) {
                NavigationView navigationView = ((NavigationView) getActivity().findViewById(R.id.navView));

                // hide admin options
                navigationView.getMenu().findItem(R.id.searchUserFragment).setVisible(false);
                navigationView.getMenu().findItem(R.id.adminEditGroupFragment).setVisible(false);
                navigationView.getMenu().findItem(R.id.editRankFragment).setVisible(false);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "hideAdminOptions: error", e);
            // TODO: Do something so bad stuff doesn't happen!
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(StartViewModel.class);
        // TODO: Use the ViewModel
    }

}