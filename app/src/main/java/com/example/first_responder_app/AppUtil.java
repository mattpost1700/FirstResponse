package com.example.first_responder_app;

import com.google.firebase.Timestamp;

import java.util.Date;

public class AppUtil {
    public static final int RESPONDING_TIME_MAX = 30;

    public static boolean timeIsWithin(Timestamp timestamp) { return timeIsWithin(timestamp, RESPONDING_TIME_MAX); }

    public static boolean timeIsWithin(Timestamp timestamp, int numberOfMinutes) {
        if(timestamp == null) return false;
        int numOfSeconds = numberOfMinutes * 60;
        long now = Timestamp.now().getSeconds();

        return timestamp.getSeconds() > (now - numOfSeconds);
    }

    public static Timestamp earliestTime(){
        long now = Timestamp.now().getSeconds();
        int numOfSeconds = RESPONDING_TIME_MAX * 60;

        long diff = now - numOfSeconds;
        Date date = new Date(diff * 1000);

        return new Timestamp(date);
    }

    public static Long numOfMinutesToMilliSeconds(int minutes) {
        return ((long) minutes) * 60000;
    }
}
