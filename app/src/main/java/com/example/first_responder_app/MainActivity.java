package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.first_responder_app.DirectionAPI.ETA;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentHomeBinding;
import com.example.first_responder_app.fragments.HomeFragment;
import com.example.first_responder_app.fragments.HomeFragmentDirections;
import com.example.first_responder_app.fragments.IncidentFragment;
import com.example.first_responder_app.fragments.LoginFragment;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.interfaces.DrawerLocker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DrawerLocker, ActiveUser {
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    DrawerLayout drawer;
    Drawable icon;
    UsersDataModel activeUser;
    LocationManager mLocationManager;
    List<Address> incidentAddr;
    List<IncidentDataModel> respIncident;
    NavController navController;
    ListenerRegistration incidentListener;
    ListenerRegistration userListener;

    final int ACCESS_LOCATION_FRAGMENT = 101;
    final int ACCESS_LOCATION_MAIN = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupAppBar();




        if(savedInstanceState != null){
            Log.d(TAG, "onCreate: ");
            String user_id = savedInstanceState.getString("user_id");
            String username = savedInstanceState.getString("username");

            if(user_id != null && username != null) {
                UsersDataModel user = new UsersDataModel();
                user.setDocumentId(user_id);
                user.setUsername(username);

                setActive(user);
            }
        }
    }

    /**
     * Setup the appbar for the application
     */
    public void setupAppBar(){
        //setup toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //setup navigation drawer
        drawer = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //setup navigation for drawer
        NavHostFragment navHostFragment =
                (NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        navController = null;
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


            //Setup Nav Drawer user click event
            View headerView = navView.getHeaderView(0);
            headerView.findViewById(R.id.user_info).setOnClickListener(v -> {

                if(activeUser != null) {
                    navController.navigate(R.id.editUserFragment);
                    closeNavDrawer();
                }else{
                    Toast.makeText(this, "You must be logged in", Toast.LENGTH_LONG).show();
                }
            });
        }

        //save the navigation icon to use later
        icon = toolbar.getNavigationIcon();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_user:
                if(activeUser != null)
                    navController.navigate(R.id.editUserFragment);
                else
                    Toast.makeText(this, "You must be logged in", Toast.LENGTH_LONG).show();
                break;
            default:
                if(toggle.onOptionsItemSelected(item)){
                    return true;
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(activeUser != null) {
            outState.putString("user_id", activeUser.getDocumentId());
            outState.putString("username", activeUser.getUsername());
        }
    }


    @Override
    public void setDrawerLocked(boolean lock) {
        ActionBar actionBar = getSupportActionBar();
        if(lock){
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toolbar.setNavigationIcon(null);
            if(actionBar != null)
                actionBar.hide();
        }else{
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toolbar.setNavigationIcon(icon);
            if(actionBar != null)
                actionBar.show();
        }
    }

    public void closeNavDrawer(){
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.closeDrawers();


    }


    /**
     * Set the logged in user
     * @param user the user who just logged in
     */
    @Override
    public void setActive(UsersDataModel user) {
        this.activeUser = user;
        NavigationView navView = findViewById(R.id.navView);
        View header = navView.getHeaderView(0);
        Log.d(TAG, "setActive: " + header.findViewById(R.id.nav_username));
        TextView username = header.findViewById(R.id.nav_username);
        username.setText(user.getUsername());

        //Ensure that the active user data is updated if database is updated
        if(userListener == null) {
            DocumentReference docRef = FirestoreDatabase.getInstance().getDb().collection("users").document(user.getDocumentId());
            userListener = docRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    activeUser = snapshot.toObject(UsersDataModel.class);


                    if (activeUser != null && AppUtil.timeIsWithin(activeUser.getResponding_time())) {
                        setActiveUserRespondingAddr();
                    } else {
                        stopETA();
                    }


                } else {
                    System.out.print("Current data: null");
                }
            });

        }
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
        if(incidentListener == null) {
            incidentListener = FirestoreDatabase.getInstance().getDb().collection("incident").whereArrayContains("responding", activeUser.getDocumentId()).whereEqualTo("incident_complete", false).addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.w(TAG, "Listening failed for firestore incident collection");
                } else {
                    ArrayList<IncidentDataModel> temp = new ArrayList<>();
                    for (QueryDocumentSnapshot incidentDoc : value) {
                        IncidentDataModel incidentDataModel = incidentDoc.toObject(IncidentDataModel.class);
                        temp.add(incidentDataModel);
                    }

                    respIncident = new ArrayList<>();
                    incidentAddr = new ArrayList<>();

                    for (int i = 0; i < temp.size(); i++) {
                        respIncident.add(temp.get(i));
                        String addr = temp.get(i).getLocation();
                        incidentAddr.add(addrToCoords(addr));
                    }
                    Log.d(TAG, "setActiveUserRespondingAddr: " + respIncident.size());

                    if (mLocationManager == null) updateETA();

                }
            });
        }
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
        Log.d(TAG, "updateETA: ");
        //Ask for permissions if needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_MAIN);
            return;
        }

       if(mLocationManager == null) {
           //setup location listener
           mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
           mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000,
                   500, mLocationListener);
       }
    }

    /**
     * Checks if permissions were granted or not.
     * If so continue program where it left off.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
        switch (requestCode) {
            case ACCESS_LOCATION_MAIN:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        updateETA();
                    Log.d(TAG, "onRequestPermissionsResult: PERMISSION GRANTED Main");
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: PERMISSION DENIED Main");
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return;
            case ACCESS_LOCATION_FRAGMENT:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    FragmentManager fragmentManager = getSupportFragmentManager();

                    Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
                    if(fragment != null) {
                        IncidentFragment incidentFragment = (IncidentFragment) fragment.getChildFragmentManager().getPrimaryNavigationFragment();

                        if (incidentFragment != null) incidentFragment.setupLocationListener();
                    }
                    Log.d(TAG, "onRequestPermissionsResult: PERMISSION GRANTED");
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: PERMISSION DENIED");
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
        }
    }

        /**
         * Stop updating ETA
         */
        public void stopETA () {
            if (mLocationManager != null) {
                mLocationManager.removeUpdates(mLocationListener);
                mLocationManager = null;
            }
        }


        private final LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

                for(int i = 0; i < respIncident.size(); i++) {
                    LatLng destination = new LatLng(0, 0);

                    if (incidentAddr.get(i) != null) {
                        destination = new LatLng(incidentAddr.get(i).getLatitude(), incidentAddr.get(i).getLongitude());
                    }

                    int idx = i;
                    ETA eta = new ETA();
                    eta.setListener(s -> {
                        if (respIncident != null && respIncident.size() > 0)
                            FirestoreDatabase.getInstance().updateETA(respIncident.get(idx).getDocumentId(), activeUser.getDocumentId(), respIncident.get(idx).getEta(), s);
                    });
                    eta.execute("https://maps.googleapis.com/maps/api/distancematrix/json?destinations=" + destination.latitude + "%2C" + destination.longitude + "&origins=" + loc.latitude + "%2C" + loc.longitude);
                }
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

}


