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
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.ReportDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentReportBinding;
import com.example.first_responder_app.viewModels.ReportViewModel;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportFragment extends Fragment {

    private IncidentDataModel incident;
    FragmentReportBinding binding;

    private UsersDataModel activeUser;

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_report, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        View bindingView = binding.getRoot();

        activeUser = AppUtil.getActiveUser(getActivity());
        if(activeUser == null) {
            getActivity().getFragmentManager().popBackStack();
            Toast.makeText(getContext(), "User is not logged in!", Toast.LENGTH_SHORT).show();
        }

        ReportViewModel mViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);
        incident = mViewModel.getIncidentDataModel();

        setText();

        binding.floatingActionButton.setOnClickListener(view -> {
            // Send to db
            //String fire_department_id, String incident_id, String user_created_id, String address, String units, String box_number, String incident_type, String narrative) {

            ReportDataModel report = null;
            try {
                report = new ReportDataModel(activeUser.getFire_department_id(),
                        incident.getDocumentId(),
                        activeUser.getDocumentId(),
                        binding.addressTextView.getText().toString(),
                        binding.unitsTextView.getText().toString(),
                        binding.boxNumberTextView.getText().toString(),
                        binding.incidentTypeTextView.getText().toString(),
                        binding.reportOfficerText.getText().toString());
            } catch (NullPointerException nullPointerException) {
                report = null;
                Toast.makeText(getActivity(), "You must fill out the whole form", Toast.LENGTH_SHORT).show();
            }

            if(report != null) {
                FirestoreDatabase.getInstance().getDb().collection(FirestoreDatabase.REPORTS_COLLECTION_DIR).add(report)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "onCreateView: added " + documentReference);
                            Toast.makeText(getActivity(), "Report saved!", Toast.LENGTH_SHORT).show();

                            NavDirections action = ReportFragmentDirections.actionReportFragmentToIncidentFragment();
                            Navigation.findNavController(binding.getRoot()).navigate(action);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "onCreateView: Failed to upload document!", e));
            }
        });

        return bindingView;
    }


    private void setText() {
        binding.addressTextView.setText(incident.getLocation());
        if(incident.getUnits() != null)
            binding.unitsTextView.setText(incident.getUnits().toString().substring(1, incident.getUnits().toString().length()-1));
        binding.incidentTypeTextView.setText(incident.getIncident_type());
        Date created = new Date();
        String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(created);
        binding.reportIncidentDate.setText(date);
    }
}