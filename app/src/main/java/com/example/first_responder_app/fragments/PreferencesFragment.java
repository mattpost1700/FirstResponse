package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.first_responder_app.viewModels.PreferencesViewModel;
import com.example.first_responder_app.databinding.FragmentHomeBinding;
import com.example.first_responder_app.databinding.PreferencesFragmentBinding;

import java.util.Objects;

public class PreferencesFragment extends PreferenceFragmentCompat {

    private ListPreference mListPreference;
    private PreferencesViewModel mViewModel;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);


    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        PreferencesFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.preferences_fragment, container, false);



        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.d(TAG, "onSharedPreferenceChanged: ");
                switch(key){
                    case "theme":
                        updateTheme(binding.getRoot().getContext());
                        break;
                }
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(binding.getRoot().getContext());
        prefs.registerOnSharedPreferenceChangeListener(listener);

        return super.onCreateView(inflater, container, savedInstanceState);

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
        }
    }
}