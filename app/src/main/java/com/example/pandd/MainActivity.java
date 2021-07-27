package com.example.pandd;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_LOCATION_CODE = 11;
    FragmentContainerView flContainer;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Set the current Parse installation with the current user to receive its notifications.
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("userId", ParseUser.getCurrentUser());
        installation.saveInBackground();

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(toolbar);

        // Fragment definition for bottom navigation view
        final Fragment homeFragment = new HomeFragment();
        final Fragment postFragment = new PostFragment();
        final Fragment mapFragment = new MapFragment();

        //Fragment manager setup
        flContainer= findViewById(R.id.flContainer);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(flContainer.getId(), homeFragment).commit();
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        //This swiping works for PostFragment (Since HomeFragment is filled with items with their respective listeners there's no "free space for swiping",
        //meaning I made its own swiper detector inside the recyclerView, and MapFragment is already filled with a map so not swiping space either)
        //So there's no point making a case handler for every fragment the user is in since it would only consume unnecessary resources.
        LinearLayout llScreen = findViewById(R.id.llScreen);
        llScreen.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSwipeLeft() {
                leftSwipe();
            }
            @Override
            public void onSwipeRight() {
                rightSwipe();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.home:
                        fragment = homeFragment;
                        break;
                    case R.id.map:
                        fragment=  mapFragment;
                        break;
                    case R.id.post:
                    default:
                        fragment = postFragment;
                        break;
                }
                fragmentManager.beginTransaction().replace(flContainer.getId(), fragment).commit();
                return true;
            }
        });
    }

    @Override
        public boolean onCreateOptionsMenu (Menu menu){
            //Inflate the menu
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        if (item.getItemId() == R.id.logout) {
            // navigate backwards to Login screen
            ParseUser.logOut();
            finish();
            return true;
        }
        if (item.getItemId() == R.id.search) {
            // navigate to search fragment
            onSearchRequested();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void leftSwipe() {
        bottomNavigationView.setSelectedItemId(R.id.home);
    }
    private void rightSwipe() {
        bottomNavigationView.setSelectedItemId(R.id.map);
    }

}