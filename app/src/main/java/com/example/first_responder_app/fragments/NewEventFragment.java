package com.example.first_responder_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.NotificationService;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentEventNewBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.NewEventViewModel;
import com.google.firebase.Timestamp;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewEventFragment extends Fragment {

    private NewEventViewModel mViewModel;
    FirestoreDatabase firestoreDatabase;
    NotificationService _notificationService = new NotificationService();

    public static NewEventFragment newInstance() {
        return new NewEventFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentEventNewBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_new, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        //switch to Home fragment upon clicking it
        //also if you have any other code relates to onCreateView just add it from here

        mViewModel = new ViewModelProvider(this).get(NewEventViewModel.class);
        firestoreDatabase = new FirestoreDatabase();

        //Open Timepicker when the timepicker button is pressed
        binding.eventTimePicker.setOnClickListener(v -> {
            Log.d("TAG", "onCreateView: CLICKED");
            TimePickerFragment fragment = new TimePickerFragment();
            fragment.setListener(binding.newEventTime::setText);
            fragment.show(getActivity().getSupportFragmentManager(), "Timepicker");
        });

        //Open Datepicker when the datepicker button is pressed
        binding.eventDatePicker.setOnClickListener(v -> {
            Log.d("TAG", "onCreateView: CLICKED");
            DatePickerFragment fragment = new DatePickerFragment();
            fragment.setListener(binding.newEventDate::setText);
            fragment.show(getActivity().getSupportFragmentManager(), "Datepicker");
        });


        binding.eventCreateConfirm.setOnClickListener(v -> {
            //TODO: validate input if needed

            NavDirections action = NewEventFragmentDirections.actionNewEventFragmentToEventGroupFragment();

            String title = binding.newEventTitle.getText().toString();
            String description = binding.newEventDescription.getText().toString();
            String location = binding.newEventLocation.getText().toString();
            String eventDate = binding.newEventDate.getText().toString();
            String eventTime = binding.newEventTime.getText().toString();
            String duration = binding.newEventDurationText.getText().toString();

            //sends the event that includes the following:
            //title, location, description, duration
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setData(CalendarContract.Events.CONTENT_URI);
            intent.putExtra(CalendarContract.Events.TITLE, title);
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
            intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
            intent.putExtra(CalendarContract.Events.DURATION, duration);

            Log.d("TAG", "onCreateView: " + intent.resolveActivity(requireContext().getPackageManager()));
            if (intent.resolveActivity(requireContext().getPackageManager()) != null){
                startActivity(intent);
            } else {
                Log.d("EVENT INTENT: ", "Event setup Failed");
            }

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || location.equals("") || eventDate.equals("MM/DD/YYYY") || eventTime.equals("HH:MM") || duration.equals("")){
                binding.newEventLog.setText(R.string.event_title_description_is_empty);
                binding.newEventLog.setVisibility(View.VISIBLE);
            }
            else {
                try {

                    Date d = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.getDefault()).parse(eventDate + " " + eventTime);

                    ActiveUser a = (ActiveUser)getActivity();
                    firestoreDatabase.setActiveUser(a.getActive());

                    //firestoreDatabase.addEvent(location, title, description, d, Integer.parseInt(duration));
//                    try {
//                        _notificationService.notifyPostReq(getContext(), "events", "New Event", title);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    Navigation.findNavController(binding.getRoot()).navigate(action);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error Creating Event", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NewEventViewModel.class);

    }

}