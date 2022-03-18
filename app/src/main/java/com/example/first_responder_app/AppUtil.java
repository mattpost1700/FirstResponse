package com.example.first_responder_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import android.app.Activity;

import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.google.firebase.Timestamp;

import java.util.Date;

public class AppUtil {
    public static final int RESPONDING_TIME_MAX = 30;


    public static boolean timeIsWithin(Timestamp timestamp, Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int time = prefs.getInt("respond", 30);
        return timeIsWithin(timestamp, time);
    }

    public static boolean timeIsWithin(Timestamp timestamp, int numberOfMinutes) {
        if(timestamp == null) return false;
        int numOfSeconds = numberOfMinutes * 60;
        long now = Timestamp.now().getSeconds();

        return timestamp.getSeconds() > (now - numOfSeconds);
    }

    public static Timestamp earliestTime(Context context){
        long now = Timestamp.now().getSeconds();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int time = prefs.getInt("respond", 30);
        int numOfSeconds = time * 60;

        long diff = now - numOfSeconds;
        Date date = new Date(diff * 1000);

        return new Timestamp(date);
    }

    public static UsersDataModel getActiveUser(Activity mainActivity) {
        ActiveUser activeUser = (ActiveUser) mainActivity;
        if (activeUser != null) {
            return activeUser.getActive();
        }
        return null;
    }

    public static Long numOfMinutesToMilliSeconds(int minutes) {
        return ((long) minutes) * 60000;
    }
}
