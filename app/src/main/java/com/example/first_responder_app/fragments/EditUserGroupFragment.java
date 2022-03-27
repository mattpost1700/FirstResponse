package com.example.first_responder_app.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.first_responder_app.viewModels.EditUserGroupViewModel;
import com.example.first_responder_app.R;

public class EditUserGroupFragment extends Fragment {

    private EditUserGroupViewModel mViewModel;

    public static EditUserGroupFragment newInstance() {
        return new EditUserGroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_user_group, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditUserGroupViewModel.class);
        // TODO: Use the ViewModel
    }

}