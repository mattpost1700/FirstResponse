package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.first_responder_app.AnnouncementRecyclerViewAdapter;
import com.example.first_responder_app.dataModels.AnnouncementsDataModel;
import com.example.first_responder_app.databinding.FragmentAnnouncementBinding;
import com.example.first_responder_app.viewModels.AnnouncementViewModel;
import com.example.first_responder_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AnnouncementViewModel mViewModel;
    private AnnouncementRecyclerViewAdapter announcementAdapter;
    private List<AnnouncementsDataModel> listOfAnnouncements;

    public static AnnouncementFragment newInstance() {
        return new AnnouncementFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        FragmentAnnouncementBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_announcement, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        listOfAnnouncements = new ArrayList<>();
        populateAnnounList();

        RecyclerView announcementRecyclerView = binding.rvAnnoun;
        announcementRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        announcementAdapter = new AnnouncementRecyclerViewAdapter(getContext(), listOfAnnouncements);
        announcementRecyclerView.setAdapter(announcementAdapter);

        binding.newAnnouncementButton.setOnClickListener(v -> {

            NavDirections action = AnnouncementFragmentDirections.actionAnnouncementFragmentToNewAnnouncementFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);

        });
        return binding.getRoot();
    }

    private void populateAnnounList(){
        db.collection("announcements").get().addOnCompleteListener(announTask -> {
            if (announTask.isSuccessful()) {
                ArrayList<AnnouncementsDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot announcementDoc : announTask.getResult()){
                    AnnouncementsDataModel announcementDataModel = announcementDoc.toObject(AnnouncementsDataModel.class);
                    temp.add(announcementDataModel);
                }
                listOfAnnouncements.clear();
                listOfAnnouncements.addAll(temp);
                announcementAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "db get failed in announcement page " + announTask.getException());
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AnnouncementViewModel.class);
    }

}