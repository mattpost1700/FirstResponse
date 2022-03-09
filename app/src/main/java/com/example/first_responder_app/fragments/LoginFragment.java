package com.example.first_responder_app.fragments;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean checkExist;
    private long last_text_edit = 0;
    private UsersDataModel user;
    private final Handler handler = new Handler();
    private FragmentLoginBinding binding;

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

        Context context = getActivity();

        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        //check whether user finished typing and query the data
        binding.loginUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                last_text_edit = System.currentTimeMillis();
                handler.removeCallbacks(input_finish_checker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mViewModel.setUsername(binding.loginUsername.getText().toString());
                if (binding.loginPassword.getText().length() > 0){
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, 500);
                }
            }
        });
        binding.loginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                last_text_edit = System.currentTimeMillis();
                handler.removeCallbacks(input_finish_checker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mViewModel.setPassword(binding.loginPassword.getText().toString());
                if (binding.loginPassword.getText().length() > 0){
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, 500);
                }
            }
        });

        binding.loginSubmit.setOnClickListener(v -> {
            if (mViewModel.getUsername() == null || mViewModel.getPassword() == null){
                binding.loginLog.setText(R.string.loginFailMsg);
                binding.loginLog.setVisibility(View.VISIBLE);
                Log.d("testing", "login failed: wrong username/password");
            }
            else {
                if (checkUsernameExists(mViewModel.getUsername())) {
                    if (checkPwMatch(mViewModel.getUsername(), mViewModel.getPassword())) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("savedUsername", mViewModel.getUsername());
                        editor.putString("savedPassword", mViewModel.getPassword());
                        editor.apply();
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
            }
        });

        binding.bypassBtn.setOnClickListener(v -> {
            NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String usernameQuickLogin = sharedPref.getString("savedUsername", null);
        String passwordQuickLogin = sharedPref.getString("savedPassword", null);
        Log.d("testing", "usernameQuick: " + usernameQuickLogin);
        Log.d("testing", "pwQuick: " + passwordQuickLogin);
        if (usernameQuickLogin != null && passwordQuickLogin != null){

            db.collection("users").whereEqualTo("username", usernameQuickLogin).whereEqualTo("password", passwordQuickLogin).get().addOnCompleteListener(usersTask -> {
                Log.d(TAG, "READ DATABASE - LOGIN FRAGMENT");
                if (usersTask.isSuccessful()) {
                    for (QueryDocumentSnapshot userDoc : usersTask.getResult()) {
                        user = userDoc.toObject(UsersDataModel.class);
                        checkExist = true;
                        if(user.getPassword().equals(passwordQuickLogin)) {
                            ActiveUser activeUser = ((ActiveUser)getActivity());
                            activeUser.setActive(user);

                            NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
                            Navigation.findNavController(binding.getRoot()).navigate(action);
                        }
                    }
                } else {
                    Log.d(TAG, "db get failed in Login page " + usersTask.getException());
                    checkExist = false;
                }
            });

//            populateUserList(usernameQuickLogin);
//            Log.d("lastLogin", Calendar.getInstance().getTime().toString());
//            NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
//            Navigation.findNavController(binding.getRoot()).navigate(action);
        }
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

    private final Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + 250)) {
                populateUserList(mViewModel.getUsername());
            }
        }
    };

    private void populateUserList(String usernameInput) {
        db.collection("users").whereEqualTo("username", usernameInput).get().addOnCompleteListener(usersTask -> {
            Log.d(TAG, "READ DATABASE - LOGIN FRAGMENT");
            if (usersTask.isSuccessful()) {
                for (QueryDocumentSnapshot userDoc : usersTask.getResult()) {
                    user = userDoc.toObject(UsersDataModel.class);
                    checkExist = true;
                }
            } else {
                Log.d(TAG, "db get failed in Login page " + usersTask.getException());
                checkExist = false;
            }
        });
    }

    //check if username is valid & fetch user info
    private boolean checkUsernameExists(String username) {
        Log.d(TAG, "fetch user info + validates input: ");
        return checkExist;
    }

    //check if password matches the record in db
    private boolean checkPwMatch(String username, String pw){
        return pw.equals(user.getPassword());
    }

}