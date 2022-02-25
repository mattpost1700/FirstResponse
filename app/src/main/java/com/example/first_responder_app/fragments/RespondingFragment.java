package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentRespondingBinding;
import com.example.first_responder_app.databinding.FragmentUserBinding;
import com.example.first_responder_app.recyclerViews.IncidentRecyclerViewAdapter;
import com.example.first_responder_app.recyclerViews.RespondersRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.RespondingViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RespondingFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<UsersDataModel> listOfRespondingDataModel;
    RespondersRecyclerViewAdapter respondingRecyclerViewAdapter;

    private RespondingViewModel mViewModel;

    public static RespondingFragment newInstance() {
        return new RespondingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentRespondingBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_responding, container, false);

        listOfRespondingDataModel = new ArrayList<>();

        final SwipeRefreshLayout pullToRefresh = binding.respondingSwipeRefreshLayout;
        pullToRefresh.setOnRefreshListener(() -> {
            refreshData();
            pullToRefresh.setRefreshing(false);
        });

        // onclick
        RespondersRecyclerViewAdapter.ResponderClickListener responderClickListener = (view, position) -> {
            Bundle result = new Bundle();
            result.putSerializable("user", listOfRespondingDataModel.get(position));
            getParentFragmentManager().setFragmentResult("requestKey", result);

            NavDirections action = RespondingFragmentDirections.actionRespondingFragmentToUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        };

        // Recycler view
        RecyclerView respondingRecyclerView = binding.respondingRecyclerView;
        respondingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        respondingRecyclerViewAdapter = new RespondersRecyclerViewAdapter(getContext(), listOfRespondingDataModel);
        respondingRecyclerViewAdapter.setResponderClickListener(responderClickListener);
        respondingRecyclerView.setAdapter(respondingRecyclerViewAdapter);

        addResponderEventListener();

        return binding.getRoot();
    }

    private void addResponderEventListener() {
        db.collection("users").whereEqualTo("is_responding", true).addSnapshotListener((value, error) -> {
            if(error != null) {
                Log.w(TAG, "Listening failed for firestore users collection");
            }
            else {
                ArrayList<UsersDataModel> temp = new ArrayList<>();
                for(QueryDocumentSnapshot userDoc : value) {
                    temp.add(userDoc.toObject(UsersDataModel.class));
                }

                listOfRespondingDataModel.clear();
                listOfRespondingDataModel.addAll(temp);
                respondingRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private void refreshData() {
        db.collection("users").whereEqualTo("is_responding", true).get().addOnCompleteListener(userTask -> {
            if(userTask.isSuccessful()) {
                ArrayList<UsersDataModel> temp = new ArrayList<>();
                for(QueryDocumentSnapshot userDoc : userTask.getResult()) {
                    temp.add(userDoc.toObject(UsersDataModel.class));
                }

                listOfRespondingDataModel.clear();
                listOfRespondingDataModel.addAll(temp);
                respondingRecyclerViewAdapter.notifyDataSetChanged();
                Log.d("TAG", "populateResponders: ");
            }
            else {
                Log.w(TAG, "refreshData: Could not refresh", userTask.getException());
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RespondingViewModel.class);
        // TODO: Use the ViewModel
    }

}