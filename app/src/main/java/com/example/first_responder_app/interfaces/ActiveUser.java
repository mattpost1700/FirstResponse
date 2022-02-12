package com.example.first_responder_app.interfaces;

import com.example.first_responder_app.dataModels.UsersDataModel;

public interface ActiveUser {
    /**
     *
     * @param user the user who just logged in
     */
    void setActive(UsersDataModel user);

    /**
     *
     * @return the user who is logged in
     */
    UsersDataModel getActive();
}
