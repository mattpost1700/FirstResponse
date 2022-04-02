package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.GroupDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.AdminEditGroupFragmentBinding;
import com.example.first_responder_app.recyclerViews.AdminGroupsRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.AdminEditGroupViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminEditGroupFragment extends Fragment {

    private AdminEditGroupViewModel mViewModel;

    private List<GroupDataModel> listOfGroups;
    private AdminGroupsRecyclerViewAdapter adapter;
    private UsersDataModel activeUser;
    private ListenerRegistration groupListener;

    private FirebaseFirestore db;

    public static AdminEditGroupFragment newInstance() {
        return new AdminEditGroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AdminEditGroupFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.admin_edit_group_fragment, container, false);

        listOfGroups = new ArrayList<>();
        activeUser = AppUtil.getActiveUser(getActivity());
        db = FirestoreDatabase.getInstance().getDb();

        if(activeUser == null) {
            getActivity().getFragmentManager().popBackStack();
            Toast.makeText(getContext(), "User is not logged in!", Toast.LENGTH_SHORT).show();
        }

        binding.adminEditGroupCreateNewButton.setOnClickListener(v -> {
            String userInput = binding.adminEditGroupEditNameEditText.getText().toString();
            GroupDataModel newGroup = new GroupDataModel(userInput, activeUser.getFire_department_id());

            db.collection(FirestoreDatabase.GROUPS_COLLECTION_DIR).add(newGroup)
                    .addOnSuccessListener(documentReference -> {
                        listOfGroups.add(newGroup);
                        listOfGroups.sort(Comparator.comparing(GroupDataModel::getName));
                        adapter.notifyDataSetChanged();
                        binding.adminEditGroupEditNameEditText.setText("");
                    });
        });

        AdminGroupsRecyclerViewAdapter.GroupClickListener groupClickListener = (view, position) -> {
            GroupDataModel clickedGroup = adapter.getItem(position);
            String origName = clickedGroup.getName();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Edit group");

            final EditText input = new EditText(getContext());
            input.setText(origName);
            builder.setView(input);

            builder.setPositiveButton("Save", (dialog, which) -> {
                String userInput = input.getText().toString();
                if(origName != null && !origName.equals(userInput)) { // User input
                    db.collection(FirestoreDatabase.GROUPS_COLLECTION_DIR).document(clickedGroup.getDocumentId())
                            .update(FirestoreDatabase.FIELD_GROUP_NAME, userInput)
                            .addOnSuccessListener(unused -> {
                                listOfGroups.get(position).setName(userInput);
                                adapter.notifyItemChanged(position);
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update group", Toast.LENGTH_SHORT).show());
                }
            });
            builder.setNeutralButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.setNegativeButton("Delete", (dialog, which) -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this group?")
                        .setPositiveButton("yes", (dialog1, which1) -> {
                            // TODO: Remove group ids from users
                            db.collection(FirestoreDatabase.GROUPS_COLLECTION_DIR).document(clickedGroup.getDocumentId()).delete()
                                    .addOnSuccessListener(unused -> {
                                        listOfGroups.remove(position);
                                        adapter.notifyItemRemoved(position);
                                    });
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            });

            builder.show();
        };

        RecyclerView groupRecyclersView = binding.adminEditGroupGroupRecyclerView;
        groupRecyclersView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminGroupsRecyclerViewAdapter(getContext(), listOfGroups);
        adapter.setGroupClickListener(groupClickListener);
        groupRecyclersView.setAdapter(adapter);

        final SwipeRefreshLayout pullToRefresh = binding.adminEditGroupSwipeToRefresh;
        pullToRefresh.setOnRefreshListener(() -> {
            refreshGroups();
            pullToRefresh.setRefreshing(false);
        });

        refreshGroups();

        return binding.getRoot();
    }

    // TODO: Fix
    private void addGroupEventListener() {
        if(groupListener != null) return;
        groupListener = db.collection(FirestoreDatabase.GROUPS_COLLECTION_DIR)
                .whereArrayContains(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                .addSnapshotListener((value, error) -> {
                    if(error != null || value == null) {
                        Log.w(TAG, "Listening failed for firestore incident collection");
                    }
                    else {
                        ArrayList<GroupDataModel> temp = new ArrayList<>();

                        for(QueryDocumentSnapshot doc : value) {
                            temp.add(doc.toObject(GroupDataModel.class));
                        }

                        listOfGroups.clear();
                        listOfGroups.addAll(temp);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void refreshGroups() {
        db.collection(FirestoreDatabase.GROUPS_COLLECTION_DIR)
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                .orderBy(FirestoreDatabase.FIELD_GROUP_NAME, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<GroupDataModel> temp = new ArrayList<>();

                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        temp.add(doc.toObject(GroupDataModel.class));
                    }

                    listOfGroups.clear();
                    listOfGroups.addAll(temp);
                    adapter.notifyDataSetChanged();
                })
        .addOnFailureListener(e -> Log.e(TAG, "refreshGroups: Failed to get ranks", e));
    }

    @Override
    public void onDestroyView() {
        if(groupListener != null) groupListener.remove();
        groupListener = null;

        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AdminEditGroupViewModel.class);
        // TODO: Use the ViewModel
    }

}