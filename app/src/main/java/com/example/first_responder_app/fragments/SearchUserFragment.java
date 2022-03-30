package com.example.first_responder_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.SearchUserFragmentBinding;
import com.example.first_responder_app.viewModels.SearchUserViewModel;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends Fragment implements SearchView.OnQueryTextListener {

    private SearchUserViewModel mViewModel;

    //https://abhiandroid.com/ui/searchview

    ListView list;
    //ListViewAdapter adapter;
    SearchView editsearch;
    private UsersDataModel activeUser;
    private List<UsersDataModel> listOfAllFireDepartmentUsers;

    public static SearchUserFragment newInstance() {
        return new SearchUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SearchUserFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.search_user_fragment, container, false);

        activeUser = AppUtil.getActiveUser(getActivity());

        FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.USERS_COLLECTION_DIR)
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                .orderBy("first_name", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<UsersDataModel> temp = new ArrayList<>();

                    for (QueryDocumentSnapshot userDoc : queryDocumentSnapshots) {
                        UsersDataModel userDataModel = userDoc.toObject(UsersDataModel.class);
                        temp.add(userDataModel);
                    }

                    listOfAllFireDepartmentUsers = temp;
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SearchUserViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}