package com.example.first_responder_app.viewModels;

import androidx.lifecycle.ViewModel;

import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.google.firebase.firestore.auth.User;

public class UserViewModel extends ViewModel {
    private UsersDataModel userDataModel;

    public UserViewModel(){ super(); }

    public UsersDataModel getUserDataModel() {
        return userDataModel;
    }

    public void setUserDataModel(UsersDataModel data){
        this.userDataModel = data;
    }
}