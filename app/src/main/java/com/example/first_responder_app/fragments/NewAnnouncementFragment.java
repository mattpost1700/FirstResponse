package com.example.first_responder_app.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.NotificationService;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.GroupDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentAnnouncementNewBinding;
import com.example.first_responder_app.viewModels.NewAnnouncementViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NewAnnouncementFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    NotificationService _notificationService = new NotificationService();
    FirestoreDatabase firestoreDatabase = new FirestoreDatabase();
    private NewAnnouncementViewModel mViewModel;

    private UsersDataModel activeUser;
    private GroupDataModel currentlySelectedGroup;

    public static NewAnnouncementFragment newInstance() {
        return new NewAnnouncementFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentAnnouncementNewBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_announcement_new, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        activeUser = AppUtil.getActiveUser(getActivity());
        if(activeUser == null) {
            getActivity().getFragmentManager().popBackStack();
            Toast.makeText(getContext(), "User is not logged in!", Toast.LENGTH_SHORT).show();
        }

        List<GroupDataModel> groups = new ArrayList<>();
        groups.add(null);
        List<String> groupsNames = new ArrayList<>();
        groupsNames.add("ALL");

        FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.GROUPS_COLLECTION_DIR)
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        groups.add(doc.toObject(GroupDataModel.class));
                    }

                    Spinner spinner = binding.annoucGroupSpinner;
                    spinner.setSelection(0);

                    ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, groupsNames);
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            GroupDataModel selectedGroup = groups.get(position);
                            currentlySelectedGroup = selectedGroup;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                });

        binding.announcementCreateConfirm.setOnClickListener(v -> {
            mViewModel.setAnnounTitle(binding.newAnnounTitle.getText().toString());
            mViewModel.setAnnounDes(binding.newAnnounDescription.getText().toString());
            NavDirections action = NewAnnouncementFragmentDirections.actionNewAnnouncementFragmentToAnnouncementFragment();

            Log.d("TAG", "onCreateView: " + currentlySelectedGroup);

            if (TextUtils.isEmpty(mViewModel.getAnnounTitle().toString()) || TextUtils.isEmpty(mViewModel.getAnnounDes().toString())){
                binding.newAnnounLog.setText(R.string.new_announ_log_msg);
                binding.newAnnounLog.setVisibility(View.VISIBLE);
            }
            else {
                try {
                    firestoreDatabase.addAnnouncement(mViewModel.getAnnounTitle(), mViewModel.getAnnounDes(), currentlySelectedGroup, activeUser);
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