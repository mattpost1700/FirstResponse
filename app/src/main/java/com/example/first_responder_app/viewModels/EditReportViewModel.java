package com.example.first_responder_app.viewModels;

import androidx.lifecycle.ViewModel;

import com.example.first_responder_app.dataModels.ReportDataModel;

public class EditReportViewModel extends ViewModel {
    ReportDataModel report;

    public ReportDataModel getReport() {
        return report;
    }

    public void setReport(ReportDataModel report) {
        this.report = report;
    }
}