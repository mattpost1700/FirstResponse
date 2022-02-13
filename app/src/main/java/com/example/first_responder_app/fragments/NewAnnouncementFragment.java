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

import com.example.first_responder_app.dataModels.AnnouncementsDataModel;
import com.example.first_responder_app.databinding.FragmentAnnouncementNewBinding;
import com.example.first_responder_app.viewModels.NewAnnouncementViewModel;
import com.example.first_responder_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewAnnouncementFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
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
            mViewModel.setAnnounTitle(binding.newAnnounTitle.toString());
            mViewModel.setAnnounDes(binding.eventDescriptionText.toString());
            if (mViewModel.getAnnounDes().equals(null) || mViewModel.getAnnounTitle().equals(null)){
                binding.newAnnounLog.setText(R.string.new_announ_log_msg);
                binding.newAnnounLog.setVisibility(View.VISIBLE);
            }
            else {
                AnnouncementsDataModel newAnnoun = new AnnouncementsDataModel(mViewModel.getAnnounTitle(), mViewModel.getAnnounDes());
                db.collection("announcements")
                        .add(newAnnoun)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("new announcement page", "new announcement has been successfully created in the DB");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("new announcement page", "fail to create new announcement");
                            }
                        });
                NavDirections action = NewAnnouncementFragmentDirections.actionNewAnnouncementFragmentToAnnouncementFragment();
                Navigation.findNavController(binding.getRoot()).navigate(action);
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