package com.example.first_responder_app.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.first_responder_app.R;
import com.example.first_responder_app.viewModels.PreferencesViewModel;

public class PreferencesFragment extends PreferenceFragmentCompat {

    private ListPreference mListPreference;
    private PreferencesViewModel mViewModel;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }




}