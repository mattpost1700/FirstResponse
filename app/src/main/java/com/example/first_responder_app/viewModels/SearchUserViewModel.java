package com.example.first_responder_app.viewModels;

import androidx.lifecycle.ViewModel;

import com.example.first_responder_app.dataModels.UsersDataModel;

public class SearchUserViewModel extends ViewModel {
    UsersDataModel selectedUser;

    public UsersDataModel getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(UsersDataModel selectedUser) {
        this.selectedUser = selectedUser;
    }
}