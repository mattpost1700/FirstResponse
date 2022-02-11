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

import com.example.first_responder_app.interfaces.DrawerLocker;
import com.example.first_responder_app.viewModels.LoginViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;

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
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        //switch to Home fragment upon clicking it
        //also if you have any other code relates to onCreateView just add it from here
        binding.loginSubmit.setOnClickListener(v -> {
            mViewModel.setUsername(binding.loginUsername.getText().toString());
            mViewModel.setPassword(binding.loginPassword.getText().toString());
            NavDirections action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
            Log.e( "testing", "username: " + mViewModel.getUsername() + " pw: " + mViewModel.getPassword());
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
        // TODO: Use the ViewModel
    }
}