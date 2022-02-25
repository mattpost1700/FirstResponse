package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.DirectionAPI.ETA;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentHomeBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.EventViewModel;
import com.example.first_responder_app.viewModels.IncidentViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentIncidentBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.firestore.DocumentReference;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class IncidentFragment extends Fragment implements OnMapReadyCallback {

    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    final int ACCESS_LOCATION = 101;
    final int ACCESS_LOCATION_MAP = 103;

    LocationManager mLocationManager;
    Address incidentAddress;
    Context context;
    String active_id;
    IncidentDataModel incidentDataModel;
    DocumentReference docRef;
    ETA eta;
    String id;
    IncidentDataModel incident;

    private IncidentViewModel mViewModel;
    private View bindingView;


    public static IncidentFragment newInstance() {
        return new IncidentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentIncidentBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_incident, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        bindingView = binding.getRoot();



        context = getContext();
        Log.d(TAG, "onCreateView: " + bindingView.findViewById(R.id.incident_button_layout));


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) binding.mapView;
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);


        mViewModel = new ViewModelProvider(requireActivity()).get(IncidentViewModel.class);
        incident = mViewModel.getIncidentDataModel();

        Log.d(TAG, "onCreate: " + getActivity().findViewById(R.id.incident_button_layout));

        initializeIncident(incident);


        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    /**
     * Setup the incident data and display it
     *
     * @param incident The incident to be displayed
     */
    public void initializeIncident(IncidentDataModel incident){

        setTextViews(incident);

        Map<String, String> status = incident.getStatus();

        //Get active user id
        ActiveUser activeUser = (ActiveUser)getActivity();
        if(activeUser != null){
            UsersDataModel user = activeUser.getActive();
            if(user != null) active_id = user.getDocumentId();
        }


        //Highlight active button
        if(status != null && status.containsKey(active_id)){
            setActiveButton(status.get(active_id));
        }


        //Find the type of the incident
        FirestoreDatabase.getInstance().getDb().collection("incident_types").document(incident.getIncident_type()).get().addOnCompleteListener(typeTask -> {
            if (typeTask.isSuccessful()) {
                String t = (String)typeTask.getResult().get("type_name");
                ((TextView)bindingView.findViewById(R.id.incident_type2)).setText(t);
            } else {
                Log.d(TAG, "Error getting documents: ", typeTask.getException());
            }
        });

        setRespondingButtonClickListener();

        //Get the Address object of the incident
        incidentAddress = addrToCoords(incident.getLocation());


        setupLocationListener();


        //Ensure that the incident data is updated if database is updated
        docRef = FirestoreDatabase.getInstance().getDb().collection("incident").document(incident.getDocumentId());
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                incidentDataModel = snapshot.toObject(IncidentDataModel.class);

                    if (incidentDataModel != null){
                        setTextViews(incidentDataModel);


                    Map<String, String> statuses = incidentDataModel.getStatus();
                    if (statuses != null) {
                        String status1 = statuses.get(active_id);
                        setActiveButton(status1);
                    }

                    //Get the Address object of the incident
                    incidentAddress = addrToCoords(incidentDataModel.getLocation());
                    }

            } else {
                System.out.print("Current data: null");
            }
        });
    }



    /**
     * Sets up the location listener used to calculate ETA
     */
    public void setupLocationListener(){
        Log.d(TAG, "setupLocationListener: ");
        //Ask for permissions if needed
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
            return;
        }


        //setup location listener
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000,
                500, mLocationListener);
    }



    /**
     * Set the active button
     *
     * @param s Button text
     */
    public void setActiveButton(String s){
        if(bindingView != null && bindingView.findViewById(R.id.incident_button_layout) != null) {
            LinearLayout linearLayout = bindingView.findViewById(R.id.incident_button_layout);
            int count = linearLayout.getChildCount();

            for (int i = 0; i < count; i++) {
                Button b = (Button) linearLayout.getChildAt(i);
                if (s != null && s.equals((String)b.getText()) && context != null) {
                    Log.d(TAG, "setActiveButton: " + s + "why is this being called");
                    b.setBackgroundColor(MaterialColors.getColor(context, R.attr.colorSecondary, context.getResources().getColor(R.color.teal_700)));
                }else if(context != null){
                    b.setBackgroundColor(MaterialColors.getColor(context, R.attr.colorPrimary, context.getResources().getColor(R.color.purple_200)));
                }
            }
        }
    }

    /**
     * Update the text views of the fragment
     * Set value to null if you don't want to update
     *
     * @param incidentDataModel The incident to use to update the text views
     *
     */
    @SuppressLint("SetTextI18n")
    public void setTextViews(IncidentDataModel incidentDataModel){
        if(incidentDataModel == null) return;

        String addr = incidentDataModel.getLocation();
        Integer responding = incidentDataModel.getResponding().size();
        String time = incidentDataModel.getReceived_time().toDate().toString();
        String units = incidentDataModel.getUnits().toString();
        units = units.replace("[", "");
        units = units.replace("]", "");

        if(bindingView != null) {
            if (addr != null) {
                TextView addrText = ((TextView) bindingView.findViewById(R.id.incident_address));
                if(addrText != null) addrText.setText(addr);
            }
            if (responding != null) {
                TextView respText = ((TextView) bindingView.findViewById(R.id.incident_responding2));
                if(respText != null) respText.setText(responding.toString());
            }
            if (time != null) {
                TextView recText = ((TextView) bindingView.findViewById(R.id.incident_received_time2));
                if(recText != null) recText.setText(time);
            }
            if (units != null) {
                TextView unitText = ((TextView) bindingView.findViewById(R.id.incident_units2));
                if(unitText != null) unitText.setText(units);
            }
        }
    }

    /**
     * Update the ETA text view of the IncidentFragment
     * @param text The text used to update the text view
     */
    public void setEtaText(String text){
        TextView etaText = null;
        if(bindingView != null) {
            etaText = ((TextView)bindingView.findViewById(R.id.incident_eta2));

            if(etaText != null){
                etaText.setText(text);
            }
        }
    }



    /**
     * Convert an address string to an Address object
     *
     * @param addr The address string to convert to an Address object
     * @return The Address object
     */
    public Address addrToCoords(String addr){

        //Get coordinates from address
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(addr, 1);
        } catch (IOException e) {
            Log.w("Invalid", "Invalid address");
            return null;
        }
        if(addresses.size() > 0){
            return addresses.get(0);
        }else{
            return null;
        }
    }


    public void setRespondingButtonClickListener(){
        if(bindingView != null){
            LinearLayout buttons = bindingView.findViewById(R.id.incident_button_layout);
            int size = buttons.getChildCount();
            for(int i = 0; i < size; i++){
                buttons.getChildAt(i).setOnClickListener(this::respondingButtonClicked);
            }
        }
    }

    /**
     * When responding button is clicked update active users responding status
     *
     * @param b the button that was clicked
     */
    public void respondingButtonClicked(View b){
        Log.d(TAG, "respondingButtonClicked: " + ((Button)b).getText());


        String text = ((Button) b).getText().toString();
        String id = incidentDataModel.getDocumentId();

        //Get active user id
        ActiveUser active = (ActiveUser)getActivity();
        UsersDataModel activeUser = null;
        if(active != null){
            activeUser = active.getActive();
        }


        if(activeUser == null && context != null){
            Toast.makeText(context, "You must be logged in", Toast.LENGTH_LONG).show();
        }else if(activeUser != null){

            Map<String, String> status = incidentDataModel.getStatus();
            boolean alreadyResponding = false;
            if(status != null) {
                //TODO: Currently have hardcoded string "Unavailable" - Will need to be replaced with all statuses that don't update the responding count
                alreadyResponding = status.containsKey(activeUser.getDocumentId()) && !Objects.equals(status.get(activeUser.getDocumentId()), "Unavailable");
            }

            if (text.equals("Unavailable")) {
                FirestoreDatabase.getInstance().responding(activeUser.getDocumentId(), id, text, false);
                setActiveButton(text);
            } else if (AppUtil.timeIsWithin(activeUser.getResponding_time()) && !alreadyResponding && context != null) {
                Toast.makeText(context, "Responding to Another Incident", Toast.LENGTH_LONG).show();
            } else {
                FirestoreDatabase.getInstance().responding(activeUser.getDocumentId(), id, text, true);
                setActiveButton(text);
            }
        }
    }


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng destination = new LatLng(0,0);
            if(incidentAddress != null){
                destination = new LatLng(incidentAddress.getLatitude(), incidentAddress.getLongitude());
            }


            eta = new ETA();
            eta.setListener(s -> setEtaText(s));
            eta.execute("https://maps.googleapis.com/maps/api/distancematrix/json?destinations=" + destination.latitude + "%2C" + destination.longitude + "&origins="  + loc.latitude + "%2C" + loc.longitude);

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(mLocationManager != null)mLocationManager.removeUpdates(mLocationListener);

        if(eta != null){ eta.removeListener(); }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("id", id);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {


        if(incidentAddress != null) {
            double longitude = incidentAddress.getLongitude();
            double latitude = incidentAddress.getLatitude();


            LatLng loc = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions().position(loc).title("Incident Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.0f));
            map.setTrafficEnabled(true);


            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_MAP);
                return;
            }
            map.setMyLocationEnabled(true);
        }
    }



    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


}