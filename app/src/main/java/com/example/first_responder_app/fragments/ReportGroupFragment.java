package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.ReportDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.ReportGroupFragmentBinding;
import com.example.first_responder_app.recyclerViews.IncidentGroupRecyclerViewAdapter;
import com.example.first_responder_app.recyclerViews.ReportGroupRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.EditReportViewModel;
import com.example.first_responder_app.viewModels.ReportGroupViewModel;
import com.example.first_responder_app.viewModels.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReportGroupFragment extends Fragment {

    private ReportGroupViewModel mViewModel;

    ReportGroupFragmentBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<ReportDataModel> reports;
    ReportGroupRecyclerViewAdapter reportGroupRecyclerViewAdapter;
    private UsersDataModel activeUser;

    public static ReportGroupFragment newInstance() {
        return new ReportGroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.report_group_fragment, container, false);

        activeUser = AppUtil.getActiveUser(getActivity());
        reports = new ArrayList<>();

        ReportGroupRecyclerViewAdapter.ReportClickListener reportClickListener = (view, position) -> {
            //TODO: setup click listener to edit report


            EditReportViewModel editReportViewModel = new ViewModelProvider(requireActivity()).get(EditReportViewModel.class);
            editReportViewModel.setReport(reports.get(position));

            NavDirections action = ReportGroupFragmentDirections.actionReportGroupFragmentToEditReport();
            Navigation.findNavController(binding.getRoot()).navigate(action);

        };

        ReportGroupRecyclerViewAdapter.ReportLongClickListener reportLongClickListener = (view, position) -> {
           AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                   .setTitle("Delete Report")
                   .setMessage("Are you sure you want to delete this report?")
                   .setPositiveButton("Yes", (dialogInterface, i) -> {
                        ReportDataModel report = reports.get(position);
                        db.collection("reports").document(report.getDocumentId()).delete();
                        reports.remove(position);
                        checkReportsEmpty();
                        reportGroupRecyclerViewAdapter.notifyDataSetChanged();
                   })
                   .setNegativeButton("No", (dialogInterface, i) -> {

                   });
           dialog.show();
        };


        RecyclerView reportGroupRecyclerView = binding.reportGroupRecycler;
        reportGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportGroupRecyclerViewAdapter = new ReportGroupRecyclerViewAdapter(getContext(), reports);
        reportGroupRecyclerViewAdapter.setReportClickListener(reportClickListener);
        reportGroupRecyclerViewAdapter.setReportLongClickListener(reportLongClickListener);
        reportGroupRecyclerView.setAdapter(reportGroupRecyclerViewAdapter);

        populateReports();

        return binding.getRoot();
    }


    private void populateReports(){
        db.collection("reports").whereEqualTo("user_created_id", activeUser.getDocumentId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<ReportDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot reportDoc : task.getResult()) {
                    ReportDataModel reportDataModel = reportDoc.toObject(ReportDataModel.class);
                    temp.add(reportDataModel);
                }

                reports.clear();
                reports.addAll(temp);
                checkReportsEmpty();
                reportGroupRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                Log.w(TAG, "onCreateView: get failed in HomeFragment with", task.getException());
            }
        });
    }

    private void checkReportsEmpty(){
        if(reports.size() == 0){
            binding.reportGroupRecycler.setVisibility(View.GONE);
            binding.reportgroupNoreports.setVisibility(View.VISIBLE);
        }else{
            binding.reportGroupRecycler.setVisibility(View.VISIBLE);
            binding.reportgroupNoreports.setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ReportGroupViewModel.class);
    }

}