package com.example.first_responder_app.fragments;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.databinding.FragmentIncidentBinding;
import com.example.first_responder_app.databinding.FragmentReportBinding;
import com.example.first_responder_app.viewModels.ReportViewModel;

public class ReportFragment extends Fragment {

    private IncidentDataModel incident;
    FragmentReportBinding binding;

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_report, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        View bindingView = binding.getRoot();

        ReportViewModel mViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);
        incident = mViewModel.getIncidentDataModel();

        setText();

        return inflater.inflate(R.layout.fragment_report, container, false);
    }



    private void setText(){
        //TODO: Figure out why this doesn't work
        Log.d("TAG", "setText: " + incident.getIncident_type());
        binding.addressTextView.setText(incident.getLocation());
        Log.d("TAG", "setText: " + binding.addressTextView.getText().toString());
        binding.unitsTextView.setText(incident.getUnits() + "");
        binding.incidentTypeTextView.setText(incident.getIncident_type());
        binding.reportIncidentDate.setText("test");
    }

}