package com.example.first_responder_app.fragments;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.databinding.FragmentEventNewBinding;
import com.example.first_responder_app.databinding.FragmentIncidentBinding;
import com.example.first_responder_app.viewModels.NewEventViewModel;
import com.example.first_responder_app.R;

import static android.content.ContentValues.TAG;
import com.example.first_responder_app.databinding.FragmentHomeBinding;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.RanksDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.viewModels.HomeViewModel;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewEventFragment extends Fragment {

    private NewEventViewModel mViewModel;

    public static NewEventFragment newInstance() {
        return new NewEventFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentEventNewBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_new, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        //switch to Home fragment upon clicking it
        //also if you have any other code relates to onCreateView just add it from here

        mViewModel = new ViewModelProvider(this).get(NewEventViewModel.class);
        FirestoreDatabase firestoreDatabase = new FirestoreDatabase();
        binding.eventCreateConfirm.setOnClickListener(v -> {
            //TODO: validate input if needed

            NavDirections action = NewEventFragmentDirections.actionNewEventFragmentToEventGroupFragment();

            String title = binding.newEventTitle.getText().toString();
            String description = binding.newEventDescription.getText().toString();
            try {
                firestoreDatabase.addEvent("US-East", title, description, new ArrayList<String>());
                try {
                    notifyNewEvent(title);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Navigation.findNavController(binding.getRoot()).navigate(action);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });


        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NewEventViewModel.class);



    }

    //TODO: make more generic
    public void notifyNewEvent(String title) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url ="https://fcm.googleapis.com/fcm/send";

        JSONObject notif = new JSONObject();
        notif.put("title", "New Event");
        notif.put("body", title);
        JSONObject json = new JSONObject();
        json.put("to", "/topics/events");
        json.put("restricted_package_name", "com.example.first_responder_app");
        json.put("notification", notif);

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST,
                url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VOLLEY", "Success");
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VOLLEY", "Failure: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "key=");
                return headers;
            }

        };
        queue.add(request_json);
    }

}