package com.example.first_responder_app.viewModels;


public class LoginViewModel extends BaseViewModel {
    private String mUsername;
    private String mPassword;

    //getter for username
    public String getUsername() {
        return mUsername;
    }

    //setter for username
    public void setUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    //getter for pw
    public String getPassword() {
        return mPassword;
    }

    //setter for pw
    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public LoginViewModel() {
        super();
    }
}