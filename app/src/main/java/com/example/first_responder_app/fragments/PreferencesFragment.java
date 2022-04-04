package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.PreferencesViewModel;
import com.example.first_responder_app.databinding.FragmentHomeBinding;
import com.example.first_responder_app.databinding.PreferencesFragmentBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class PreferencesFragment extends PreferenceFragmentCompat {

    private ListPreference mListPreference;
    private PreferencesViewModel mViewModel;

    SharedPreferences.OnSharedPreferenceChangeListener listener;
    PreferencesFragmentBinding binding;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);


    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.preferences_fragment, container, false);


        return super.onCreateView(inflater, container, savedInstanceState);

    }


    @Override
    public void onResume() {
        super.onResume();
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.d(TAG, "onSharedPreferenceChanged: ");
                switch(key){
                    case "theme":
                        updateTheme(binding.getRoot().getContext());
                        break;
                    case "fireNotificationPrefKey":
                        subOrUnsubTopic("fire", binding.getRoot().getContext());
                        break;
                    case "EMSNotificationPrefKey":
                        subOrUnsubTopic("EMS", binding.getRoot().getContext());

                }
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(binding.getRoot().getContext());
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(binding.getRoot().getContext());
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public void updateTheme(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String theme = prefs.getString("theme", "Light");
        switch(theme){
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            break;
        }
    }

    public void subOrUnsubTopic(String topic, Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        ActiveUser activeUser = (ActiveUser)c;
        UsersDataModel user = activeUser.getActive();

        String dept_id = "";
        if (user.getFire_department_id() != null) {
            dept_id = user.getFire_department_id();
        }

        boolean sub;
        if (topic.equalsIgnoreCase("fire")) {
            sub = prefs.getBoolean("fireNotificationPrefKey", true);
            topic = "fire_" + dept_id;
        } else {
            sub = prefs.getBoolean("EMSNotificationPrefKey", true);
            topic = "EMS_" + dept_id;
        }


        if (sub) {
            String finalTopic1 = topic;
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener(new OnCompleteListener<>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, finalTopic1 + " successfully subscribed to!");
                        }
                    });
        } else {
            String finalTopic = topic;
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                    .addOnCompleteListener(new OnCompleteListener<>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, finalTopic + " successfully unsubscribed from!");
                        }
                    });
        }
    }
}