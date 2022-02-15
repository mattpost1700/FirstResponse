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

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.NotificationService;
import com.example.first_responder_app.dataModels.AnnouncementsDataModel;
import com.example.first_responder_app.databinding.FragmentAnnouncementNewBinding;
import com.example.first_responder_app.viewModels.NewAnnouncementViewModel;
import com.example.first_responder_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.util.ArrayList;

public class NewAnnouncementFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    NotificationService _notificationService = new NotificationService();
    FirestoreDatabase firestoreDatabase = new FirestoreDatabase();
    private NewAnnouncementViewModel mViewModel;

    public static NewAnnouncementFragment newInstance() {
        return new NewAnnouncementFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentAnnouncementNewBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_announcement_new, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: TEST THE CODE IN THE FUTURE
        NavController navController = navHostFragment.getNavController();



        binding.announcementCreateConfirm.setOnClickListener(v -> {
            mViewModel.setAnnounTitle(binding.newAnnounTitle.getText().toString());
            mViewModel.setAnnounDes(binding.newAnnounDescription.getText().toString());
            NavDirections action = NewAnnouncementFragmentDirections.actionNewAnnouncementFragmentToAnnouncementFragment();

            if (mViewModel.getAnnounDes().equals(null) || mViewModel.getAnnounTitle().equals(null)){
                binding.newAnnounLog.setText(R.string.new_announ_log_msg);
                binding.newAnnounLog.setVisibility(View.VISIBLE);
            }
            else {
                try {
                    firestoreDatabase.addAnnouncement(mViewModel.getAnnounTitle(), mViewModel.getAnnounDes());
                    try {
                        _notificationService.notifyPostReq(getContext(), "announcements", mViewModel.getAnnounTitle(), mViewModel.getAnnounDes());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Navigation.findNavController(binding.getRoot()).navigate(action);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NewAnnouncementViewModel.class);
        // TODO: Use the ViewModel
    }

}