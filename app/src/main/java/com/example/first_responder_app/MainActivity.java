package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.first_responder_app.DirectionAPI.ETA;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentHomeBinding;
import com.example.first_responder_app.fragments.HomeFragmentDirections;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.interfaces.DrawerLocker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.ActionCodeMultiFactorInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DrawerLocker, ActiveUser {
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    DrawerLayout drawer;
    Drawable icon;
    UsersDataModel activeUser;
    LocationManager mLocationManager;
    Address incidentAddr;
    IncidentDataModel respIncident;

    final int ACCESS_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //setup navigation drawer
        drawer = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //setup navigation for drawer
        NavHostFragment navHostFragment =
                (NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        NavController navController = null;
        if(navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        if(navController != null) {
            AppBarConfiguration appBarConfiguration =
                    new AppBarConfiguration.Builder(navController.getGraph())
                            .setOpenableLayout(drawer)
                            .build();


            NavigationView navView = findViewById(R.id.navView);
            NavigationUI.setupWithNavController(navView, navController);
        }

        //save the navigation icon to use later
        icon = toolbar.getNavigationIcon();

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setDrawerLocked(boolean lock) {
        if(lock){
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toolbar.setNavigationIcon(null);
        }else{
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toolbar.setNavigationIcon(icon);
        }
    }


    /**
     * Set the logged in user
     * @param user the user who just logged in
     */
    @Override
    public void setActive(UsersDataModel user) {
        this.activeUser = user;
        TextView username = findViewById(R.id.nav_username);
        username.setText(user.getUsername());


        //Ensure that the active user data is updated if database is updated
        DocumentReference docRef = FirestoreDatabase.getInstance().getDb().collection("users").document(user.getDocumentId());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    activeUser = snapshot.toObject(UsersDataModel.class);


                    if(activeUser != null && activeUser.isIs_responding()){
                        setActiveUserRespondingAddr();
                    }else{
                        stopETA();
                    }


                } else {
                    System.out.print("Current data: null");
                }
            }
        });


    }

    /**
     *
     * @return the logged in user
     */
    @Override
    public UsersDataModel getActive() {
        return activeUser;
    }


    public void setActiveUserRespondingAddr(){
        FirestoreDatabase.getInstance().getDb().collection("incident").whereArrayContains("responding", activeUser.getDocumentId()).addSnapshotListener((value, error) -> {
            if(error != null) {
                Log.w(TAG, "Listening failed for firestore incident collection");
            }
            else {
                ArrayList<IncidentDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot incidentDoc : value) {
                    IncidentDataModel incidentDataModel = incidentDoc.toObject(IncidentDataModel.class);
                    temp.add(incidentDataModel);
                }
                if(temp.size() > 0) {
                    respIncident = temp.get(0);
                    String addr = respIncident.getLocation();
                    incidentAddr = addrToCoords(addr);
                    updateETA();
                }

            }
        });
    }

    /**
     * Convert a string address into an Address object
     *
     * @param addr The string address you want to convert
     * @return The Address object
     */
    public Address addrToCoords(String addr){
        //Get coordinates from address
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
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

    /**
     * Update the active user's ETA
     */
    public void updateETA(){
        //Ask for permissions if needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
            return;
        }

        if(mLocationManager != null)
            mLocationManager.removeUpdates(mLocationListener);

        //setup location listener
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000,
                500, mLocationListener);
    }

    /**
     * Stop updating ETA
     */
    public void stopETA(){
        if(mLocationManager != null)
            mLocationManager.removeUpdates(mLocationListener);
    }


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng destination = new LatLng(0,0);
            if(incidentAddr != null){
                destination = new LatLng(incidentAddr.getLatitude(), incidentAddr.getLongitude());
            }

            ETA eta = new ETA();
            eta.setListener(s -> {
                if(respIncident != null)
                    FirestoreDatabase.getInstance().updateETA(respIncident.getDocumentId(), activeUser.getDocumentId(), respIncident.getEta(), s);
            });
            eta.execute("https://maps.googleapis.com/maps/api/distancematrix/json?destinations=" + destination.latitude + "%2C" + destination.longitude + "&origins="  + loc.latitude + "%2C" + loc.longitude);

        }
    };

}


