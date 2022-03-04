package com.example.first_responder_app.fragments;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.example.first_responder_app.R;

import java.sql.Time;
import java.util.Calendar;


public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    public interface TimeListener{
        public void returnTime(String time);
    }

    private TimeListener listener = null;

    public void setListener(TimeListener instance) {
        this.listener = instance;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user

        String minStr = String.valueOf(minute);
        if(minStr.length() == 1){
            minStr += "0";
        }

        String time = "";
        if(hourOfDay > 12){
            time = (hourOfDay - 12) + ":" + minStr + " PM";
        }else if(hourOfDay == 12){
            time = hourOfDay + ":" + minStr + " PM";
        }else if(hourOfDay == 0){
            time = 12 + ":" + minStr + " AM";
        } else{
            time = hourOfDay + ":" + minStr + " AM";
        }
        if(listener != null) listener.returnTime(time);
    }
}

