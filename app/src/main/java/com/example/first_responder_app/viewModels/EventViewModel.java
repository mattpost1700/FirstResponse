package com.example.first_responder_app.viewModels;

import androidx.lifecycle.ViewModel;

import com.example.first_responder_app.dataModels.EventsDataModel;

public class EventViewModel extends BaseViewModel {

    private EventsDataModel eventDetail;
    private String id;
    private String eventTitle;
    private String eventDescription;
    private String eventLocation;

    public EventViewModel(){ super(); }

    public EventsDataModel getEventDetail() {
        return eventDetail;
    }

    public void setEventDetail(EventsDataModel eventDetail) {
        this.eventDetail = eventDetail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

}