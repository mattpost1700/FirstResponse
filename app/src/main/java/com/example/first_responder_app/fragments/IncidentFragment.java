package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
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
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.interfaces.RefreshETAs;
import com.example.first_responder_app.viewModels.IncidentViewModel;
import com.example.first_responder_app.viewModels.UserViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentIncidentBinding;
import com.example.first_responder_app.viewModels.ReportViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IncidentFragment extends DialogFragment implements OnMapReadyCallback {

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
    ListenerRegistration listenerRegistration;
    CharSequence[] responderArr;
    List<UsersDataModel> responderList;

    private IncidentViewModel mViewModel;
    private UserViewModel mUserViewModel;
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
        mMapView = (MapView) binding.googleMapView;
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        mViewModel = new ViewModelProvider(requireActivity()).get(IncidentViewModel.class);
        incident = mViewModel.getIncidentDataModel();
        responderList = new ArrayList<>();

        Log.d(TAG, "onCreate: " + getActivity().findViewById(R.id.incident_button_layout));

        initializeIncident(incident);


        //TODO: Check if user has permissions to file report (If not hide FAB)
        binding.incidentFileReportButton.setOnClickListener(v -> {
            ReportViewModel userViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);
            userViewModel.setIncidentDataModel(incidentDataModel);

            NavDirections action = IncidentFragmentDirections.actionIncidentFragmentToReportFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        //setup the dialog upon clicking the responder count icon, clicking on individual item will redirect to their profile page
        binding.incidentRespondingCount.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.dialog_title, null);

            builder.setCustomTitle(view)
                    .setItems(responderArr, (dialogInterface, i) -> {
                        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
                        mUserViewModel.setUserDataModel(responderList.get(i));
                        NavDirections action = IncidentFragmentDirections.actionIncidentFragmentToUserFragment();
                        Navigation.findNavController(binding.getRoot()).navigate(action);
                    });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });


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
    public void initializeIncident(IncidentDataModel incident) {

        setTextViews(incident);

        Map<String, String> status = incident.getStatus();

        //Get active user id
        ActiveUser activeUser = (ActiveUser) getActivity();
        if (activeUser != null) {
            UsersDataModel user = activeUser.getActive();
            if (user != null) active_id = user.getDocumentId();
        }


        //Highlight active button
        if (status != null && status.containsKey(active_id)) {
            setActiveButton(status.get(active_id));
        }


        setRespondingButtonClickListener();

        //Get the Address object of the incident
        incidentAddress = addrToCoords(incident.getLocation());


        setupLocationListener();

        AtomicInteger prevLength = new AtomicInteger();
        if (listenerRegistration == null) {
            //Ensure that the incident data is updated if database is updated
            docRef = FirestoreDatabase.getInstance().getDb().collection("incident").document(incident.getDocumentId());
            listenerRegistration = docRef.addSnapshotListener((snapshot, e) -> {
                Log.d(TAG, "READ DATABASE - INCIDENT FRAGMENT");

                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    incidentDataModel = snapshot.toObject(IncidentDataModel.class);

                    if (incidentDataModel != null) {
                        setTextViews(incidentDataModel);


                        Map<String, String> statuses = incidentDataModel.getStatus();
                        if (statuses != null) {
                            String status1 = statuses.get(active_id);
                            setActiveButton(status1);
                        }

                        //Get the Address object of the incident

                        incidentAddress = addrToCoords(incidentDataModel.getLocation());

                        int currentLength = incidentDataModel.getResponding().size();
                        if(prevLength.get() != currentLength){
                            prevLength.set(currentLength);
                            responderList.clear();
                            populateRespondersListFromDB();
                        }
                    }

                } else {
                    System.out.print("Current data: null");
                }
            });
        }
    }

    /**
     * Setup the responder data and display it
     *
     *
     */
    private void populateRespondersListFromDB() {
        responderList.clear();

        if (incidentDataModel.getResponding().size() != 0) {
            int upper = Math.floorDiv(incidentDataModel.getResponding().size(), 10);
            for (int i = 0; i < upper; i++) {
                populateResponders(i * 10, i * 10 + 10);
            }
            populateResponders((incidentDataModel.getResponding().size() - incidentDataModel.getResponding().size() % 10), incidentDataModel.getResponding().size());
        }
    }

    public void populateResponders(int startIdx, int endIdx) {
        FirestoreDatabase.getInstance().getDb()
                .collection("users")
                .whereIn(FieldPath.documentId(), incidentDataModel.getResponding().subList(startIdx, endIdx))
                .get().addOnCompleteListener(responderTask -> {
                    Log.d(TAG, "READ DATABASE - EVENT FRAGMENT");

                    if (responderTask.isSuccessful()) {
                        List<UsersDataModel> tempList = new ArrayList<>();
                        for (QueryDocumentSnapshot userDoc : responderTask.getResult()) {
                            UsersDataModel userData = userDoc.toObject(UsersDataModel.class);
                            tempList.add(userData);
                        }
                        responderList.addAll(tempList);
                        responderArr = new CharSequence[responderList.size()];
                        for (int i = 0; i < responderList.size(); i++){
                            responderArr[i] = responderList.get(i).getFull_name();
                        }
                    } else {
                        Log.w(TAG, "populateResponderList: Responder data failed to query", responderTask.getException());
                    }
                });
    }

    /**
     * Sets up the location listener used to calculate ETA
     */
    public void setupLocationListener() {
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
    public void setActiveButton(String s) {
        if (bindingView != null && bindingView.findViewById(R.id.incident_button_layout) != null) {
            LinearLayout linearLayout = bindingView.findViewById(R.id.incident_button_layout);
            int count = linearLayout.getChildCount();

            for (int i = 0; i < count; i++) {
                Chip b = (Chip) linearLayout.getChildAt(i);
                if (s != null && s.equals((String) b.getText()) && context != null) {
                    b.setChipBackgroundColor(ColorStateList.valueOf(MaterialColors.getColor(context, R.attr.colorSecondaryVariant, context.getResources().getColor(R.color.colorSecondaryDark))));
                    b.setTextColor(MaterialColors.getColor(context, R.attr.colorOnSecondary, context.getResources().getColor(R.color.white)));
                } else if (context != null) {
                    b.setChipBackgroundColor(ColorStateList.valueOf(MaterialColors.getColor(context, R.attr.colorPrimaryDark, context.getResources().getColor(R.color.colorPrimaryDark))));
                    b.setTextColor(MaterialColors.getColor(context, R.attr.colorOnPrimary, context.getResources().getColor(R.color.white)));
                }
            }
        }
    }

    /**
     * Update the text views of the fragment
     * Set value to null if you don't want to update
     *
     * @param incidentDataModel The incident to use to update the text views
     */
    @SuppressLint("SetTextI18n")
    public void setTextViews(IncidentDataModel incidentDataModel) {
        if (incidentDataModel == null) return;

        String addr = incidentDataModel.getLocation();
        Integer responding = incidentDataModel.getResponding().size();
        String time = incidentDataModel.getCreated_at().toDate().toString();
        String units = incidentDataModel.getUnits().toString();
        String type = incidentDataModel.getIncident_type();
        units = units.replace("[", "");
        units = units.replace("]", "");
        String[] unitsArr = units.split(", ");

        if (bindingView != null) {
            if (addr != null) {
                TextView addrText = ((TextView) bindingView.findViewById(R.id.incident_address_tv));
                if (addrText != null) addrText.setText(addr);
            }
            if (responding != null) {
                TextView respText = ((TextView) bindingView.findViewById(R.id.incident_responding_count));
                if (respText != null) respText.setText(responding.toString());
            }
            if (time != null) {
                TextView recText = ((TextView) bindingView.findViewById(R.id.incident_received_time_chip));
                if (recText != null) recText.setText(time);
            }
            if (unitsArr != null) {
                ChipGroup chipGroup = bindingView.findViewById(R.id.incident_chip_group);
                if (chipGroup.getChildCount() == 0) {
                    for (int i = 0; i < unitsArr.length; i++) {
                        TextView chip = new TextView(context);
                        chip.setText(unitsArr[i]);
                        chip.setPadding(25, 15, 25, 15);
                        chip.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background));
                        int id = View.generateViewId();
                        Log.d(TAG, "setTextViews: " + id);
                        chip.setId(id);
                        chipGroup.addView(chip);
                    }
                }
            }

            if(type != null){
                ((TextView) bindingView.findViewById(R.id.incident_type_chip)).setText(type);
            }
        }
    }

    /**
     * Update the ETA text view of the IncidentFragment
     *
     * @param text The text used to update the text view
     */
    public void setEtaText(String text) {
        TextView etaText = null;
        if (bindingView != null) {
            etaText = ((TextView) bindingView.findViewById(R.id.incident_eta_chip));

            if (etaText != null) {
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
    public Address addrToCoords(String addr) {

        //Get coordinates from address
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(addr, 1);
        } catch (IOException e) {
            Log.w("Invalid", "Invalid address");
            return null;
        }
        if (addresses.size() > 0) {
            return addresses.get(0);
        } else {
            return null;
        }
    }


    public void setRespondingButtonClickListener() {
        if (bindingView != null) {
            LinearLayout buttons = bindingView.findViewById(R.id.incident_button_layout);
            int size = buttons.getChildCount();
            for (int i = 0; i < size; i++) {
                buttons.getChildAt(i).setOnClickListener(this::respondingButtonClicked);
            }
        }
    }

    /**
     * When responding button is clicked update active users responding status
     *
     * @param b the button that was clicked
     */
    public void respondingButtonClicked(View b) {
        Log.d(TAG, "respondingButtonClicked: " + ((Button) b).getText());


        String text = ((Button) b).getText().toString();
        String id = incidentDataModel.getDocumentId();

        //Get active user id
        ActiveUser active = (ActiveUser) getActivity();
        UsersDataModel activeUser = null;
        if (active != null) {
            activeUser = active.getActive();
        }


        if (activeUser == null && context != null) {
            Toast.makeText(context, "You must be logged in", Toast.LENGTH_LONG).show();
        } else if (activeUser != null) {

            List<String> responses = activeUser.getResponses();
            if (!AppUtil.timeIsWithin(activeUser.getResponding_time(), requireContext())) {
                responses = new ArrayList<>();
            }

            //TODO: remove hardcoded string
            if (text.equals("Unavailable")) {
                FirestoreDatabase.getInstance().responding(activeUser.getDocumentId(), incidentDataModel, text, false, responses, null);
                setActiveButton(text);

            } else {
                FirestoreDatabase.getInstance().responding(activeUser.getDocumentId(), incidentDataModel, text, true, responses, () -> {
                    RefreshETAs refreshETAs = (RefreshETAs) getActivity();
                    refreshETAs.refresh();
                });
                setActiveButton(text);
            }
        }
    }


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng destination = new LatLng(0, 0);
            if (incidentAddress != null) {
                destination = new LatLng(incidentAddress.getLatitude(), incidentAddress.getLongitude());
            }


            eta = new ETA();
            eta.setListener(s -> setEtaText(s));
            eta.execute("https://maps.googleapis.com/maps/api/distancematrix/json?destinations=" + destination.latitude + "%2C" + destination.longitude + "&origins=" + loc.latitude + "%2C" + loc.longitude);

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

        if (listenerRegistration != null) listenerRegistration.remove();
        listenerRegistration = null;

        if (mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);

        if (eta != null) {
            eta.removeListener();
        }
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
        if(mMapView != null)
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


        if (incidentAddress != null) {
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("responderListDialog")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onPause() {
        if (mMapView != null) mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) mMapView.onLowMemory();
    }


}