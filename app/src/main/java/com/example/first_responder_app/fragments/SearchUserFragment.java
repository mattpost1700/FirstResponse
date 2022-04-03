package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
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
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.SearchUserAdapter;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.SearchUserFragmentBinding;
import com.example.first_responder_app.viewModels.SearchUserViewModel;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends Fragment implements SearchView.OnQueryTextListener {

    private SearchUserViewModel mViewModel;

    //https://abhiandroid.com/ui/searchview

    ListView listView;
    SearchView searchView;
    private UsersDataModel activeUser;
    private List<UsersDataModel> listOfAllFireDepartmentUsers;
    private SearchUserAdapter adapter;

    public static SearchUserFragment newInstance() {
        return new SearchUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SearchUserFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.search_user_fragment, container, false);

        activeUser = AppUtil.getActiveUser(getActivity());
        listOfAllFireDepartmentUsers = new ArrayList<>();
        adapter = new SearchUserAdapter(getContext(), listOfAllFireDepartmentUsers);
        searchView = binding.search;
        listView = binding.listview;

        fillList();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            UsersDataModel selectedUser = listOfAllFireDepartmentUsers.get(position);

            // Go to admin edit user
            mViewModel = new ViewModelProvider(requireActivity()).get(SearchUserViewModel.class);
            mViewModel.setSelectedUser(selectedUser);

            NavDirections action = SearchUserFragmentDirections.actionSearchUserFragmentToAdminEditUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        final SwipeRefreshLayout pullToRefresh = binding.searchUserSwipe;
        pullToRefresh.setOnRefreshListener(() -> {
            fillList();
            pullToRefresh.setRefreshing(false);
        });

        listView.setAdapter(adapter);
        searchView.setOnQueryTextListener(this);

        return binding.getRoot();
    }

    private void fillList() {
        FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.USERS_COLLECTION_DIR)
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            ArrayList<UsersDataModel> temp = new ArrayList<>();

            for (QueryDocumentSnapshot userDoc : queryDocumentSnapshots) {
                UsersDataModel userDataModel = userDoc.toObject(UsersDataModel.class);
                temp.add(userDataModel);
            }

            listOfAllFireDepartmentUsers.clear();
            listOfAllFireDepartmentUsers.addAll(temp);
            adapter.notifyDataSetChanged();

        }).addOnFailureListener(e -> Log.e(TAG, "onCreateView: failed to get users from department", e));
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
        String text = newText;
        adapter.filter(text);
        return false;
    }
}