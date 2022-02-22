package com.example.first_responder_app;

import com.google.firebase.Timestamp;

import java.util.Date;

public class AppUtil {

    public static boolean timeIsWithin(Timestamp timestamp, int numberOfMinutes) {
        int numOfSeconds = numberOfMinutes * 60;
        int now = Timestamp.now().toDate().getSeconds();
        return timestamp.getSeconds() > (now - numOfSeconds);
    }
}
