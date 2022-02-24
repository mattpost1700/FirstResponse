package com.example.first_responder_app.viewModels;

import androidx.lifecycle.ViewModel;

import com.example.first_responder_app.dataModels.IncidentDataModel;

public class IncidentViewModel extends ViewModel {
    private IncidentDataModel incidentDataModel;

    public IncidentViewModel(){ super(); }

    public IncidentDataModel getIncidentDataModel() {
        return incidentDataModel;
    }

    public void setIncidentDataModel(IncidentDataModel data){
        this.incidentDataModel = data;
    }
}