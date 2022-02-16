package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
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
import android.widget.TextView;

import com.example.first_responder_app.DirectionAPI.ETA;
import com.example.first_responder_app.dataModels.IncidentTypesDataModel;
import com.example.first_responder_app.dataModels.RanksDataModel;
import com.example.first_responder_app.viewModels.IncidentViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentIncidentBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncidentFragment extends Fragment implements OnMapReadyCallback {

    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    final int ACCESS_LOCATION = 101;

    String address;
    LocationManager mLocationManager;
    List<Address> addresses;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    private IncidentViewModel mViewModel;

    public static IncidentFragment newInstance() {
        return new IncidentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FragmentIncidentBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_incident, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        //switch to Home fragment upon clicking it
        //also if you have any other code relates to onCreateView just add it from here


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
        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                String addr = bundle.getString("address");
                address = addr;
                String type = bundle.getString("type");
                String time = bundle.getString("time");

                db.collection("incident_types").document(type).get().addOnCompleteListener(typeTask -> {
                    if (typeTask.isSuccessful()) {
                        ArrayList<IncidentTypesDataModel> types = new ArrayList<>();
                       String t = (String)typeTask.getResult().get("type_name");
                       ((TextView)getActivity().findViewById(R.id.incident_type)).setText("Type of Call: " + t);
                    } else {
                        Log.d(TAG, "Error getting documents: ", typeTask.getException());
                    }
                });



                String units = bundle.getString("units");
                units = units.substring(1, units.length() - 1);
                int responding = bundle.getInt("responding");


                //Update text views
                ((TextView)getActivity().findViewById(R.id.incident_address)).setText(addr);
                ((TextView)getActivity().findViewById(R.id.incident_responding)).setText("Responding: " + responding);
                ((TextView)getActivity().findViewById(R.id.incident_received_time)).setText("Received Time: " + time);
                ((TextView)getActivity().findViewById(R.id.incident_units)).setText("Units: " + units);


                //Get coordinates from address
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocationName(address, 1);
                } catch (IOException e) {
                    Log.w("Invalid", "Invalid address");
                    return;
                }


                //calculate distance to incident
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
                    return;
                }

                //setup location listener
                mLocationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000,
                        500, mLocationListener);





            }
        });
    }




    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng destination = new LatLng(0,0);
            if(addresses.size() > 0){
                Address a = addresses.get(0);
                destination = new LatLng(a.getLatitude(), a.getLongitude());
            }

            ETA eta = new ETA();
            eta.setListener(s -> {
                Log.d(TAG, "onCreateView: " + s);
                ((TextView)getActivity().findViewById(R.id.incident_eta)).setText("ETA: " + s);
            });
            eta.execute("https://maps.googleapis.com/maps/api/distancematrix/json?destinations=" + destination.latitude + "%2C" + destination.longitude + "&origins="  + loc.latitude + "%2C" + loc.longitude);

        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(IncidentViewModel.class);
        // TODO: Use the ViewModel
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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
    public void onMapReady(GoogleMap map) {


        if(addresses.size() > 0) {
            Address address = addresses.get(0);
            double longitude = address.getLongitude();
            double latitude = address.getLatitude();


            LatLng loc = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions().position(loc).title("Incident Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.0f));
            map.setTrafficEnabled(true);


            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
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