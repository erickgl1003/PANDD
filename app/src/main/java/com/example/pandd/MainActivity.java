package com.example.pandd;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseInstallation;
import com.parse.ParseUser;



public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_LOCATION_CODE = 11;
    FragmentContainerView flContainer;
    BottomNavigationView bottomNavigationView;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

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

    private void tutorialBottomNavigationView() {
        //Create the sequence in the tutorial of showcase views for BottomNavigationView
        ShowcaseView.Builder svHome = showcaseBuilder(new ViewTarget(R.id.home, this));
        svHome.setContentTitle("Home")
                .setContentText("Click the Home button to see all the posts")
                .singleShot(42)
                .setShowcaseEventListener(new SimpleShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        ShowcaseView.Builder svMap = showcaseBuilder(new ViewTarget(R.id.map, MainActivity.this));
                        svMap.setContentTitle("Map")
                                .setContentText("Click the Map button to see all the nearby stores with posts in them")
                                .setShowcaseEventListener(new SimpleShowcaseEventListener() {
                                    @Override
                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                        ShowcaseView.Builder svPost = showcaseBuilder(new ViewTarget(R.id.post, MainActivity.this));
                                        svPost.setContentTitle("Post")
                                                .setContentText("Click the Post button to create a new post")
                                                .setShowcaseEventListener(new SimpleShowcaseEventListener() {
                                                    @Override
                                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                                        tutorialToolbar();
                                                    }
                                                })
                                                .build();
                                    }
                                })
                                .build();
                    }
                })
                .build();
    }

    private void tutorialToolbar() {
        //Create the sequence in the tutorial of showcase views for Toolbar (and Post's store)
        ShowcaseView.Builder svRecycler = showcaseBuilder(new ViewTarget(R.id.rvPosts, this));
        svRecycler.setContentTitle("Stores")
                .setContentText("Click the store name in any post to display info about it. Double click it to do a search of all posts in that store.")
                .setShowcaseEventListener(new SimpleShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        ShowcaseView.Builder svSearch = showcaseBuilder(new ToolbarActionItemTarget(toolbar,R.id.search));
                        svSearch.setContentTitle("Search")
                                .setContentText("Click the search button to search posts by product name")
                                .setShowcaseEventListener(new SimpleShowcaseEventListener() {
                                    @Override
                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                        ShowcaseView.Builder svLogout = showcaseBuilder(new ToolbarActionItemTarget(toolbar,R.id.logout));
                                        svLogout.setContentTitle("Logout")
                                                .setContentText("Click the logout button to logout of your account and close the app")
                                                .build();
                                    }
                                })
                                .build();
                    }
                })
                .build();
    }

    ShowcaseView.Builder showcaseBuilder(Target viewTarget){
        return new ShowcaseView.Builder(this)
                .setTarget(viewTarget)
                .setStyle(R.style.CustomShowcaseTheme)
                .blockAllTouches();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        //Inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Set showcaseView for tutorial if it's the first time using the app
        tutorialBottomNavigationView();
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

    private void leftSwipe() { bottomNavigationView.setSelectedItemId(R.id.home); }
    private void rightSwipe() { bottomNavigationView.setSelectedItemId(R.id.map); }
}