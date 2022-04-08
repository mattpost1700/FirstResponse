package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentLoginBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.interfaces.DrawerLocker;
import com.example.first_responder_app.viewModels.LoginViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean checkExist;
    private long last_text_edit = 0;
    private UsersDataModel user;
    private final Handler handler = new Handler();
    private FragmentLoginBinding binding;
    private SharedPreferences sharedPref;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //lock drawer on login page
        DrawerLocker drawerLocker = ((DrawerLocker)getActivity());
        if(drawerLocker != null){
            drawerLocker.setDrawerLocked(true);
        }

        Context context = getActivity();

        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);



        binding.loginCreateAccountFab.setOnClickListener(v -> {
            NavDirections action = LoginFragmentDirections.actionLoginFragmentToCreateUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        binding.loginCreateDepartmentFab.setOnClickListener(v -> {
            NavDirections action = LoginFragmentDirections.actionLoginFragmentToCreateDepartmentFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        binding.loginSubmit.setOnClickListener(v -> {
            if (binding.loginUsername.getText().toString().equals("") || binding.loginPassword.getText().toString().equals("")){
                binding.loginLog.setText(R.string.loginFailMsg);
                binding.loginLog.setVisibility(View.VISIBLE);
                Log.d("testing", "login failed: wrong username/password");
            }
            else {

                db.collection("users").whereEqualTo("username", binding.loginUsername.getText().toString()).whereEqualTo("password", binding.loginPassword.getText().toString()).get().addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        if (t.getResult().isEmpty()) {
                            binding.loginLog.setText(R.string.loginFailMsg);
                            binding.loginLog.setVisibility(View.VISIBLE);
                        } else {
                            for (QueryDocumentSnapshot userDoc : t.getResult()) {
                                user = userDoc.toObject(UsersDataModel.class);
                            }

                            try {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("savedUsername", binding.loginUsername.getText().toString());
                                editor.putString("savedPassword", binding.loginPassword.getText().toString());
                                editor.apply();
                                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                                sharedPref = EncryptedSharedPreferences.create(
                                        "secret_shared_prefs",
                                        masterKeyAlias,
                                        context,
                                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                                );
                            } catch (Exception e){
                                Log.d("Login Encryption: ", "Encryption failed");
                            }
                            ActiveUser activeUser = ((ActiveUser)getActivity());
                            activeUser.setActive(user);

                            successfullyHidAdminOptions();
                            NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
                            Navigation.findNavController(binding.getRoot()).navigate(action);
                        }
                    } else {
                        Log.d(TAG, "Error logging in");
                    }
                });


            }
        });

        binding.bypassBtn.setOnClickListener(v -> {
            successfullyHidAdminOptions();

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
        if (usernameQuickLogin != null && passwordQuickLogin != null){

            db.collection("users")
                    .whereEqualTo("username", usernameQuickLogin)
                    .whereEqualTo("password", passwordQuickLogin)
                    .get().addOnCompleteListener(usersTask -> {
                Log.d(TAG, "READ DATABASE - LOGIN FRAGMENT");
                if (usersTask.isSuccessful()) {
                    for (QueryDocumentSnapshot userDoc : usersTask.getResult()) {
                        user = userDoc.toObject(UsersDataModel.class);
                        checkExist = true;
                        if(user.getPassword().equals(passwordQuickLogin)) {
                            Log.d(TAG, "onStart: LOGIN");
                            ActiveUser activeUser = ((ActiveUser)getActivity());
                            activeUser.setActive(user);

                            if(FirestoreDatabase.getInstance().setActiveUser(user)) {
                                successfullyHidAdminOptions();

                                NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
                                Navigation.findNavController(binding.getRoot()).navigate(action);
                            } else {
                                // User cannot make good queries
                                Toast.makeText(getContext(), "User does not have a department", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "db get failed in Login page " + usersTask.getException());
                    checkExist = false;
                }
            });
        }
    }

    private void successfullyHidAdminOptions() {
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
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }
}