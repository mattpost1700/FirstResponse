package com.example.first_responder_app.viewModels;

public class NewAnnouncementViewModel extends BaseViewModel {

    private String announTitle;
    private String announDes;


    public String getAnnounTitle() {
        return announTitle;
    }

    public void setAnnounTitle(String announTitle) {
        this.announTitle = announTitle;
    }

    public String getAnnounDes() {
        return announDes;
    }

    public void setAnnounDes(String announDes) {
        this.announDes = announDes;
    }

    public NewAnnouncementViewModel(){super();}
}