package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
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

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.GroupDataModel;
import com.example.first_responder_app.dataModels.RanksDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.AdminEditUserFragmentBinding;
import com.example.first_responder_app.viewModels.AdminEditUserViewModel;
import com.example.first_responder_app.viewModels.SearchUserViewModel;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminEditUserFragment extends Fragment {

    private AdminEditUserViewModel mViewModel;
    private String currentlySelectedRankId;
    private List<String> currentlySelectedGroupIds;
    private ArrayList<String> userInGroupsNames;
    private ArrayList<String> userNotInGroupsNames;

    Spinner addGroupSpinner;
    Spinner removeGroupSpinner;

    UsersDataModel userToEdit;

    ArrayAdapter<String> addGroupAdapter;
    ArrayAdapter<String> removeGroupAdapter;
    List<GroupDataModel> allGroups = new ArrayList<>();
    List<String> allGroupsStrings = new ArrayList<>();

    public static AdminEditUserFragment newInstance() {
        return new AdminEditUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AdminEditUserFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.admin_edit_user_fragment, container, false);

        SearchUserViewModel searchUserViewModel = new ViewModelProvider(requireActivity()).get(SearchUserViewModel.class);
        userToEdit = searchUserViewModel.getSelectedUser();
        if (userToEdit == null) {
            // leave
        }

        currentlySelectedRankId = userToEdit.getRank_id();
        currentlySelectedGroupIds = userToEdit.getGroup_ids();

        ArrayList<RanksDataModel> listOfRanks = new ArrayList<>();
        ArrayList<String> listOfRankNames = new ArrayList<>();

        userInGroupsNames = new ArrayList<>();
        userNotInGroupsNames = new ArrayList<>();

        addGroupSpinner = binding.addGroupSpinner;
        removeGroupSpinner = binding.removeGroupSpinner;
        Spinner rankSpinner = binding.rankSpinner;
        rankSpinner.setSelection(listOfRankNames.indexOf(currentlySelectedRankId));

        rankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedString = listOfRankNames.get(position);

                for (RanksDataModel ranksDataModel : listOfRanks) {
                    String docId = ranksDataModel.getDocumentId();
                    if (docId.equals(selectedString)) {
                        currentlySelectedRankId = docId;
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.adminEditUserUserName.setText(userToEdit.getFull_name());

        ArrayAdapter<String> rankAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, listOfRankNames);
        rankSpinner.setAdapter(rankAdapter);

        FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.RANKS_COLLECTION_DIR)
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, userToEdit.getFire_department_id())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<RanksDataModel> temp = new ArrayList<>();
                    ArrayList<String> temp2 = new ArrayList<>();

                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        RanksDataModel ranksDataModel = queryDocumentSnapshot.toObject(RanksDataModel.class);

                        temp.add(ranksDataModel);
                        temp2.add(ranksDataModel.getRank_name());
                    }

                    listOfRanks.clear();
                    listOfRanks.addAll(temp);
                    listOfRankNames.clear();
                    listOfRankNames.addAll(temp2);

                    // I know this is bad...
                    for(RanksDataModel ranksDataModel : listOfRanks) {
                        if(ranksDataModel.getDocumentId().equals(userToEdit.getRank_id())) {
                            for(String mRankName : listOfRankNames) {
                                if(mRankName.equals(ranksDataModel.getRank_name())) {
                                    rankSpinner.setSelection(listOfRankNames.indexOf(mRankName));
                                    break;
                                }
                            }
                        }
                    }

                    rankAdapter.notifyDataSetChanged();
                });

        FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.GROUPS_COLLECTION_DIR)
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, userToEdit.getFire_department_id())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        List<String> userGroups = userToEdit.getGroup_ids();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            GroupDataModel currentGroup = doc.toObject(GroupDataModel.class);

                            allGroups.add(currentGroup);
                            allGroupsStrings.add(currentGroup.getName());
                            if (userGroups.contains(currentGroup.getDocumentId())) {
                                userInGroupsNames.add(currentGroup.getName());
                            } else {
                                userNotInGroupsNames.add(currentGroup.getName());
                            }
                        }

                        addGroupAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, userNotInGroupsNames);
                        addGroupSpinner.setAdapter(addGroupAdapter);

                        removeGroupAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, userInGroupsNames);
                        removeGroupSpinner.setAdapter(removeGroupAdapter);
                    } catch (Exception e) {
                        Log.e(TAG, "onCreateView: ", e);
                    }
                });


        binding.adminEditUserUpdateRankButton.setOnClickListener(v -> {
            String selectedRankString = rankSpinner.getSelectedItem().toString();

            for (RanksDataModel ranksDataModel : listOfRanks) {
                if(ranksDataModel.getRank_name().equals(selectedRankString)) {
                    FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.USERS_COLLECTION_DIR)
                            .document(userToEdit.getDocumentId()).update(FirestoreDatabase.FIELD_RANK_ID, ranksDataModel.getDocumentId())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "onCreateView: Failed to update user", Toast.LENGTH_SHORT).show());
                }
            }

        });

        binding.adminEditUserAddGroupButton.setOnClickListener(v -> {
            String selectedGroupToAdd = addGroupSpinner.getSelectedItem().toString();

            for(GroupDataModel groupDataModel : allGroups) {
                if(groupDataModel.getName().equals(selectedGroupToAdd)) {
                    FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.USERS_COLLECTION_DIR)
                            .document(userToEdit.getDocumentId()).update("group_ids", FieldValue.arrayUnion(groupDataModel.getDocumentId()))
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "onCreateView: Failed to update user", Toast.LENGTH_SHORT).show());
                }
            }

            userInGroupsNames.add(selectedGroupToAdd);
            userNotInGroupsNames.remove(selectedGroupToAdd);
            addGroupAdapter.notifyDataSetChanged();
            removeGroupAdapter.notifyDataSetChanged();
        });

        binding.adminEditUserRemoveGroupButton.setOnClickListener(v -> {
            String selectedGroupToRemove = removeGroupSpinner.getSelectedItem().toString();

            for (GroupDataModel groupDataModel : allGroups) {
                if(groupDataModel.getName().equals(selectedGroupToRemove)) {
                    FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.USERS_COLLECTION_DIR)
                            .document(userToEdit.getDocumentId()).update("group_ids", FieldValue.arrayRemove(groupDataModel.getDocumentId()))
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "onCreateView: Failed to update user", Toast.LENGTH_SHORT).show());
                }
            }

            userInGroupsNames.remove(selectedGroupToRemove);
            userNotInGroupsNames.add(selectedGroupToRemove);
            addGroupAdapter.notifyDataSetChanged();
            removeGroupAdapter.notifyDataSetChanged();
        });



        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AdminEditUserViewModel.class);
        // TODO: Use the ViewModel
    }
}