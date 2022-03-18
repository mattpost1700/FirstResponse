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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.ReportDataModel;
import com.example.first_responder_app.databinding.EditReportFragmentBinding;
import com.example.first_responder_app.viewModels.EditReportViewModel;
import com.example.first_responder_app.viewModels.IncidentViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditReport extends Fragment {

    private EditReportViewModel mViewModel;
    ReportDataModel report;
    EditReportFragmentBinding binding;
    public static EditReport newInstance() {
        return new EditReport();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.edit_report_fragment, container, false);

        mViewModel = new ViewModelProvider(requireActivity()).get(EditReportViewModel.class);
        report = mViewModel.getReport();

        populateText(report);


        binding.editreportFloatingActionButton.setOnClickListener(view -> {
            // Send to db
            //String fire_department_id, String incident_id, String user_created_id, String address, String units, String box_number, String incident_type, String narrative) {

            Map<String, Object> updates;
            try {

                updates = new HashMap<>();
                updates.put("address", binding.editreportAddressTextView.getText().toString());
                updates.put("box_number", binding.editreportBoxNumberTextView.getText().toString());
                updates.put("incident_type", binding.editreportIncidentTypeTextView.getText().toString());
                updates.put("units", binding.editreportUnitsTextView.getText().toString());
                updates.put("narrative", binding.editreportOfficerText.getText().toString());

            } catch (NullPointerException nullPointerException) {
                updates = null;
                Toast.makeText(getActivity(), "You must fill out the whole form", Toast.LENGTH_SHORT).show();
            }

            if(updates != null) {
                FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.REPORTS_COLLECTION_DIR).document(report.getDocumentId()).update(updates)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "onCreateView: added " + documentReference);
                            Toast.makeText(getActivity(), "Report saved!", Toast.LENGTH_SHORT).show();

                            NavDirections action = EditReportDirections.actionEditReportToReportGroupFragment();
                            Navigation.findNavController(binding.getRoot()).navigate(action);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "onCreateView: Failed to upload document!", e));
            }
        });



        return binding.getRoot();
    }

    private void populateText(ReportDataModel report){
        if(report != null) {
            binding.editreportAddressTextView.setText(report.getAddress());
            Date created = report.getCreated_at().toDate();
            String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(created);
            binding.editreportIncidentDate.setText(date);
            binding.editreportUnitsTextView.setText(report.getUnits());
            binding.editreportBoxNumberTextView.setText(report.getBox_number());
            binding.editreportIncidentTypeTextView.setText(report.getIncident_type());
            binding.editreportOfficerText.setText(report.getNarrative());
        }
    }




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditReportViewModel.class);
        // TODO: Use the ViewModel
    }

}