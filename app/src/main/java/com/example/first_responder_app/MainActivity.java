package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentHomeBinding;
import com.example.first_responder_app.fragments.HomeFragmentDirections;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.interfaces.DrawerLocker;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.ActionCodeMultiFactorInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DrawerLocker, ActiveUser {
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    DrawerLayout drawer;
    Drawable icon;
    UsersDataModel activeUser;

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


    /**
     * When responding button is clicked update active users responding status
     *
     * @param b the button that was clicked
     */
    public void respondingButtonClicked(View b){
        String text = ((Button) b).getText().toString();
        String id = (String)((View)b.getParent()).getTag();

        if(activeUser == null){
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_LONG).show();
            return;
        }
        if(text.equals("Unavailable")){
            FirestoreDatabase.getInstance().responding(activeUser.getDocumentId(), id, text, false);
        }else if(activeUser.isIs_responding()){
            Toast.makeText(this, "Already Responding", Toast.LENGTH_LONG).show();
        }else {
            FirestoreDatabase.getInstance().responding(activeUser.getDocumentId(), id, text, true);
        }
    }
}


