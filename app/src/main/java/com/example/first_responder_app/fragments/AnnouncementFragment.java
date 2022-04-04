package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.AnnouncementsDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentAnnouncementBinding;
import com.example.first_responder_app.recyclerViews.AnnouncementRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.AnnouncementViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AnnouncementViewModel mViewModel;
    private AnnouncementRecyclerViewAdapter announcementAdapter;
    private List<AnnouncementsDataModel> listOfAnnouncements;
    FragmentAnnouncementBinding binding;

    private UsersDataModel activeUser;

    public static AnnouncementFragment newInstance() {
        return new AnnouncementFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_announcement, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        activeUser = AppUtil.getActiveUser(getActivity());
        if (activeUser == null) {
            getActivity().getFragmentManager().popBackStack();
            Toast.makeText(getContext(), "User is not logged in!", Toast.LENGTH_SHORT).show();
        }

        listOfAnnouncements = new ArrayList<>();
        populateAnnouncmentList();

        RecyclerView announcementRecyclerView = binding.rvAnnoun;
        announcementRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        announcementAdapter = new AnnouncementRecyclerViewAdapter(getContext(), listOfAnnouncements);
        announcementRecyclerView.setAdapter(announcementAdapter);

        final SwipeRefreshLayout pullToRefresh = binding.announcementSwipeRefreshLayout;
        pullToRefresh.setOnRefreshListener(() -> {
            populateAnnouncmentList();
            pullToRefresh.setRefreshing(false);
        });

        binding.newAnnouncementButton.setOnClickListener(v -> {
            NavDirections action = AnnouncementFragmentDirections.actionAnnouncementFragmentToNewAnnouncementFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        return binding.getRoot();
    }

    private void populateAnnouncmentList() {
        if(activeUser.getGroup_ids() != null && activeUser.getGroup_ids().size() > 0) {
            Task getAnnoucementsForGroups = db.collection("announcements")
                    .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                    .whereIn("intended_group_id", activeUser.getGroup_ids())
                    .orderBy(FirestoreDatabase.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get();

            Task getAnnouncementsForAll = db.collection("announcements")
                    .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                    .whereEqualTo("intended_group_id", null)
                    .orderBy(FirestoreDatabase.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get();

            Tasks.whenAllSuccess(getAnnoucementsForGroups, getAnnouncementsForAll).addOnSuccessListener(objects -> {
                ArrayList<AnnouncementsDataModel> temp = new ArrayList<>();
                for (Object fakeQuerySnapshot : objects) {
                    QuerySnapshot querySnapshot = ((QuerySnapshot) fakeQuerySnapshot);
                    for (QueryDocumentSnapshot announcementDoc : querySnapshot) {
                        AnnouncementsDataModel announcementDataModel = announcementDoc.toObject(AnnouncementsDataModel.class);
                        temp.add(announcementDataModel);
                    }
                }

                listOfAnnouncements.clear();
                listOfAnnouncements.addAll(temp);

                checkAnnouncementEmpty();
                announcementAdapter.notifyDataSetChanged();
            })
                    .addOnFailureListener(e -> Log.e(TAG, "populateAnnouncmentList: db get failed in announcement page", e));
        }else{
            checkAnnouncementEmpty();
        }
    }


    /**
     * Check if the announcement list is empty
     * If so show the "no announcements" text
     */
    private void checkAnnouncementEmpty() {
        if (listOfAnnouncements.size() == 0) {
            binding.rvAnnoun.setVisibility(View.GONE);
            binding.announcementNoneText.setVisibility(View.VISIBLE);
        } else {
            binding.rvAnnoun.setVisibility(View.VISIBLE);
            binding.announcementNoneText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AnnouncementViewModel.class);
    }

}