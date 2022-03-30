package com.example.first_responder_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.first_responder_app.R;
import com.example.first_responder_app.viewModels.AdminEditUserViewModel;

public class AdminEditUserFragment extends Fragment {

    private AdminEditUserViewModel mViewModel;

    public static AdminEditUserFragment newInstance() {
        return new AdminEditUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_edit_user_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AdminEditUserViewModel.class);
        // TODO: Use the ViewModel
    }

}