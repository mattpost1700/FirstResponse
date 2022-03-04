package com.example.first_responder_app.viewModels;

import androidx.lifecycle.ViewModel;

import com.example.first_responder_app.dataModels.IncidentDataModel;

public class ReportViewModel extends ViewModel {
    private IncidentDataModel incidentDataModel;

    public ReportViewModel(){ super(); }

    public IncidentDataModel getIncidentDataModel() {
        return incidentDataModel;
    }

    public void setIncidentDataModel(IncidentDataModel data){
        this.incidentDataModel = data;
    }
}