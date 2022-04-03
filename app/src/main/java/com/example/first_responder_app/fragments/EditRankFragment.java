package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.dataModels.RanksDataModel;
import com.example.first_responder_app.dataModels.ReportDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentEditRankBinding;
import com.example.first_responder_app.recyclerViews.EditRankRecyclerViewAdapter;
import com.example.first_responder_app.recyclerViews.EventGroupRecyclerViewAdapter;
import com.example.first_responder_app.recyclerViews.ReportGroupRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.EditRankViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.viewModels.EventViewModel;
import com.example.first_responder_app.viewModels.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EditRankFragment extends Fragment {

    private EditRankViewModel mViewModel;
    FragmentEditRankBinding binding;
    private UsersDataModel activeUser;
    private List<RanksDataModel> ranksList;
    private FirestoreDatabase firestoreDatabase = FirestoreDatabase.getInstance();
    private EditRankRecyclerViewAdapter editRankRecyclerViewAdapter;
    private FirebaseFirestore db = firestoreDatabase.getDb();

    public static EditRankFragment newInstance() {
        return new EditRankFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_rank, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        activeUser = AppUtil.getActiveUser(getActivity());
        ranksList = new ArrayList<>();
        populateRanks();

        //handles edit rank by onClick
        EditRankRecyclerViewAdapter.ItemClickListener editRankClickListener = ((view, position, data) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Edit Rank")
                    .setMessage("Rank Title");
            final EditText input = new EditText(getContext());
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            builder.setView(input);
            builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                RanksDataModel rank = ranksList.get(position);
                rank.setRank_name(input.getText().toString());
                db.collection("ranks").document(rank.getDocumentId()).set(rank);
                editRankRecyclerViewAdapter.notifyDataSetChanged();
            });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //handles hold to delete
        EditRankRecyclerViewAdapter.rankLongClickListener rankLongClickListener = (view, position) -> {
            @SuppressLint("NotifyDataSetChanged") AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Delete rank")
                    .setMessage("Are you sure you want to delete this rank?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        RanksDataModel rank = ranksList.get(position);
                        db.collection("ranks").document(rank.getDocumentId()).delete();
                        ranksList.remove(position);
                        checkRanksListEmpty();
                        editRankRecyclerViewAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> {

                    });
            dialog.show();
        };

        binding.addRankBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Create Rank")
                    .setMessage("Rank Title");
            final EditText input = new EditText(getContext());
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            builder.setView(input);
            builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                firestoreDatabase.addRank(input.getText().toString());
                populateRanks();
            });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        RecyclerView editRankRecyclerView = binding.editRankRecycler;
        editRankRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        editRankRecyclerViewAdapter = new EditRankRecyclerViewAdapter(getContext(), ranksList);
        editRankRecyclerViewAdapter.setClickListener(editRankClickListener);
        editRankRecyclerViewAdapter.setLongClickListener(rankLongClickListener);
        editRankRecyclerView.setAdapter(editRankRecyclerViewAdapter);

        /*  swipe to delete (now using hold to delete)
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                RanksDataModel deletedRank = ranksList.get(viewHolder.getAdapterPosition());
                int pos = viewHolder.getAdapterPosition();
                ranksList.remove(pos);
                editRankRecyclerViewAdapter.notifyItemRemoved(pos);
            }
        }).attachToRecyclerView(editRankRecyclerView);
        */

        final SwipeRefreshLayout pullToRefresh = binding.rankSwipeRefreshLayout;
        pullToRefresh.setOnRefreshListener(() -> {
            populateRanks();
            pullToRefresh.setRefreshing(false);
        });


        return binding.getRoot();
    }


    private void populateRanks(){
        db.collection("ranks")
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                .get().addOnCompleteListener(ranksTask -> {
            Log.d(TAG, "READ DATABASE - EDIT RANK FRAGMENT");

            if (ranksTask.isSuccessful()) {
                ArrayList<RanksDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot ranksDoc : ranksTask.getResult()){
                    RanksDataModel ranksDataModel = ranksDoc.toObject(RanksDataModel.class);
                    temp.add(ranksDataModel);
                }
                Log.d(TAG, "populateRankList: " + temp.size());
                ranksList.clear();
                ranksList.addAll(temp);
                checkRanksListEmpty();
                editRankRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "db get failed in edit ranks page " + ranksTask.getException());
            }
        });
    }

    private void checkRanksListEmpty() {
        Log.d(TAG, "checkRanksEmpty: " + ranksList.size());
        if (ranksList.size() == 0) {
            binding.editRankRecycler.setVisibility(View.GONE);
            binding.editRankNoneText.setVisibility(View.VISIBLE);
        } else {
            binding.editRankRecycler.setVisibility(View.VISIBLE);
            binding.editRankNoneText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditRankViewModel.class);
    }

}