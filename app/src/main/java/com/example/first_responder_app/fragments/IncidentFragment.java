package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

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

import com.example.first_responder_app.DirectionAPI.ETA;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.interfaces.ActiveUser;
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

    LocationManager mLocationManager;
    Address incidentAddress;
    Activity activity;
    Context context;
    String active_id;
    IncidentDataModel incidentDataModel;
    DocumentReference docRef;

    private IncidentViewModel mViewModel;

    public static IncidentFragment newInstance() {
        return new IncidentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentIncidentBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_incident, container, false);
        Activity a = getActivity();
        if(a != null) {
            NavHostFragment navHostFragment =
                    (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

            if(navHostFragment != null){ NavController navController = navHostFragment.getNavController(); }
        }


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) binding.mapView;
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);



        return binding.getRoot();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activity = getActivity();
        context = getContext();


        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            String id = bundle.getString("id");
            String addr = bundle.getString("address");
            String type = bundle.getString("type");
            String time = bundle.getString("time");
            String statusString = bundle.getString("status");

            String units = bundle.getString("units");
            units = units.replace("[", "");
            units = units.replace("]", "");

            int responding = bundle.getInt("responding");

            setTextViews(addr, responding, time, units);

            Map<String, String> status = stringToHashMap(statusString);

            //Get active user id
            ActiveUser activeUser = (ActiveUser)activity;
            if(activeUser != null){
                UsersDataModel user = activeUser.getActive();
                if(user != null) active_id = user.getDocumentId();
            }


            //Highlight active button
            if(status != null && status.containsKey(active_id)){
                setActiveButton(status.get(active_id));
            }


            //Find the type of the incident
            FirestoreDatabase.getInstance().getDb().collection("incident_types").document(type).get().addOnCompleteListener(typeTask -> {
                if (typeTask.isSuccessful()) {
                   String t = (String)typeTask.getResult().get("type_name");
                   ((TextView)activity.findViewById(R.id.incident_type2)).setText(t);
                } else {
                    Log.d(TAG, "Error getting documents: ", typeTask.getException());
                }
            });

            setRespondingButtonClickListener();

            //Get the Address object of the incident
            incidentAddress = addrToCoords(addr);


           setupLocationListener();




            //Ensure that the incident data is updated if database is updated
            docRef = FirestoreDatabase.getInstance().getDb().collection("incident").document(id);
            docRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    incidentDataModel = snapshot.toObject(IncidentDataModel.class);
                    String addr1 = null;
                    if(incidentDataModel != null) {
                        addr1 = incidentDataModel.getLocation();
                        Integer responding1 = incidentDataModel.getResponding().size();
                        String time1 = incidentDataModel.getReceived_time().toDate().toString();
                        String units1 = incidentDataModel.getUnits().toString();
                        units1 = units1.replace("[", "");
                        units1 = units1.replace("]", "");
                        setTextViews(addr1, responding1, time1, units1);



                        Map<String, String> statuses = incidentDataModel.getStatus();
                        if(statuses != null){
                            String status1 = statuses.get(active_id);
                            setActiveButton(status1);
                        }
                    }
                    //Get the Address object of the incident
                    incidentAddress = addrToCoords(addr1);

                } else {
                    System.out.print("Current data: null");
                }
            });



        });
    }

    /**
     * Sets up the location listener used to calculate ETA
     */
    public void setupLocationListener(){
        //Ask for permissions if needed
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
            return;
        }

        //setup location listener
        mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000,
                500, mLocationListener);
    }



    /**
     * Converts a string into a Map
     *
     * @param s String to convert to a Map
     * @return the converted Map
     */
    public Map<String, String> stringToHashMap(String s){
        if(s==null) return null;

        s = s.replace("{", "");
        s = s.replace("}", "");
        String[] mappings = s.split(", ");

        Map<String, String> map = new HashMap<>();
        for (String mapping : mappings) {
            String[] keyVal = mapping.split("=");
            if (keyVal.length > 1) {
                map.put(keyVal[0], keyVal[1]);
            }
        }

        return map;
    }

    /**
     * Set the active button
     *
     * @param s Button text
     */
    public void setActiveButton(String s){
        if(activity != null && activity.findViewById(R.id.incident_button_layout) != null) {
            LinearLayout linearLayout = activity.findViewById(R.id.incident_button_layout);
            int count = linearLayout.getChildCount();

            for (int i = 0; i < count; i++) {
                Button b = (Button) linearLayout.getChildAt(i);
                if (s != null && s.equals((String)b.getText()) && context != null) {
                    Log.d(TAG, "setActiveButton: " + s);
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
     * @param addr The address of the incident
     * @param responding The number of people responding
     * @param time The time of the incident
     * @param units The units that are responding
     */
    @SuppressLint("SetTextI18n")
    public void setTextViews(String addr, Integer responding, String time, String units){
        if(activity != null) {
            if (addr != null) {
                TextView addrText = ((TextView) activity.findViewById(R.id.incident_address));
                if(addrText != null) addrText.setText(addr);
            }
            if (responding != null) {
                TextView respText = ((TextView) activity.findViewById(R.id.incident_responding2));
                if(respText != null) respText.setText(responding.toString());
            }
            if (time != null) {
                TextView recText = ((TextView) activity.findViewById(R.id.incident_received_time2));
                if(recText != null) recText.setText(time);
            }
            if (units != null) {
                TextView unitText = ((TextView) activity.findViewById(R.id.incident_units2));
                if(unitText != null) unitText.setText(units);
            }
        }
    }


    public void setEtaText(String text){
        TextView etaText = null;
        if(activity != null) {
            etaText = ((TextView)activity.findViewById(R.id.incident_eta2));

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
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
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
        if(activity != null){
            LinearLayout buttons = activity.findViewById(R.id.incident_button_layout);
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
        ActiveUser active = (ActiveUser)activity;
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
            } else if (activeUser.isIs_responding() && !alreadyResponding && context != null) {
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

            ETA eta = new ETA();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(IncidentViewModel.class);
        // TODO: Use the ViewModel
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        mLocationManager.removeUpdates(mLocationListener);

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


            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
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