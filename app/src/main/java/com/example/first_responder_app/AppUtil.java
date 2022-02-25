package com.example.first_responder_app;

import com.google.firebase.Timestamp;

import java.util.Date;

public class AppUtil {
    public static final int RESPONDING_TIME_MAX = 30;

    public static boolean timeIsWithin(Timestamp timestamp) { return timeIsWithin(timestamp, RESPONDING_TIME_MAX); }

    public static boolean timeIsWithin(Timestamp timestamp, int numberOfMinutes) {
        if(timestamp == null) return false;
        int numOfSeconds = numberOfMinutes * 60;
        int now = Timestamp.now().toDate().getSeconds();
        return timestamp.getSeconds() > (now - numOfSeconds);
    }
}
