package com.example.first_responder_app.viewModels;

import androidx.lifecycle.ViewModel;

public class CreateUserViewModel extends ViewModel {

    private String fireDepartmentId;

    public CreateUserViewModel() { super(); }

    public void setFireDepartmentId(String fireDepartmentId) {
        this.fireDepartmentId = fireDepartmentId;
    }

    public String getFireDepartmentId() {
        return fireDepartmentId;
    }
}