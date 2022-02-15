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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.first_responder_app.databinding.FragmentAnnouncementBinding;
import com.example.first_responder_app.viewModels.AnnouncementViewModel;
import com.example.first_responder_app.R;

public class AnnouncementFragment extends Fragment {

    private AnnouncementViewModel mViewModel;

    public static AnnouncementFragment newInstance() {
        return new AnnouncementFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        FragmentAnnouncementBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_announcement, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();

        binding.newAnnouncementButton.setOnClickListener(v -> {
            //TODO: validate input if needed

            NavDirections action = AnnouncementFragmentDirections.actionAnnouncementFragmentToNewAnnouncementFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);

        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AnnouncementViewModel.class);
        // TODO: Use the ViewModel
    }

}