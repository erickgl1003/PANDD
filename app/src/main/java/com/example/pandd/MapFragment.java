package com.example.pandd;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.pandd.models.Store;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import im.delight.android.location.SimpleLocation;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapFragment";
    private GoogleMap mMap;
    private double userLat;
    private double userLong;
    private int km = 10;
    protected List<Store> allStores = new ArrayList<Store>();
    private SimpleLocation userLocation;

    public MapFragment() {
        //Required empty public constructor because it's a Fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_map, parent, false);

        //Initialize the Places class using the context from this fragment and the Maps API Key
        ApplicationInfo ai = null;
        try {
            ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
        //Application info can't be null since it is required to get the API Key, in order for the application to execute properly.
        assert ai != null;
        Places.initialize(getActivity().getApplicationContext(), ai.metaData.getString("com.google.android.geo.API_KEY"));
        PlacesClient placesClient = Places.createClient(getActivity());

        //Load the google Map's map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private boolean withinRange(LatLng storell, int km) {
        //Calculates the distance between user and store.
        double dist = SimpleLocation.calculateDistance(storell.latitude, storell.longitude, userLat, userLong);
        dist /= 1000;
        return (dist <= km);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Once the google Map's map loads, put in inside our own map
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        //Initialize SimpleLocation to set the user current latitude and longitude a
        Context context = getActivity();
        userLocation = new SimpleLocation(context);
        userLat = userLocation.getLatitude();
        userLong = userLocation.getLongitude();


        //Set the map to focus on the user location
        LatLng latLng = new LatLng(userLat, userLong);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        Marker userMarker = addMarker(latLng, -1);
        userMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        userMarker.setTitle("You");
        userMarker.showInfoWindow();
        queryStores();
    }
    protected void queryStores() {
        ParseQuery<Store> query = ParseQuery.getQuery(Store.class);
        query.addDescendingOrder("createdAt");

        query.findInBackground(new FindCallback<Store>() {
            @Override
            public void done(List<Store> stores, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                allStores.addAll(stores);
                addStores();
            }
        });
    }

    private void addStores() {
        //Once the posts are queried, get the stores and put them inside our map
        for(int i = 0; i < allStores.size();i++){
            Store store = allStores.get(i);
            LatLng latLng = new LatLng(store.getDouble("lat"), store.getDouble("long"));
            if(withinRange(latLng,km)){
                addMarker(latLng,i);
            }
        }
    }

    public Marker addMarker(LatLng latLng, int index){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if(index != -1)
        markerOptions.snippet(String.valueOf(index));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        return mMap.addMarker(markerOptions);
    }


    @Override
    public boolean onMarkerClick(@NonNull @NotNull Marker marker) {
        //If the marker is the user location, just show its info once clikec
        if(marker.getTitle() != null ){
            marker.showInfoWindow();
            return true;
        }

        //If the marker is a store, get it's index (which is saved in its snippet) and get the Store object form allStores to set the StoreInfo dialog
        int index = Integer.parseInt(marker.getSnippet());
        Store storeObj = allStores.get(index);
        Intent i = new Intent(getActivity(),StoreInfo.class);
        i.putExtra("name",storeObj.getName());
        i.putExtra("address",storeObj.getAddress());
        i.putExtra("mapId",storeObj.getMapId());
        String coordinates = getCoordinatesText(storeObj.getLat(),storeObj.getLong());
        i.putExtra("coordinates",coordinates);
        getActivity().startActivity(i);

        return true;
    }

    private String getCoordinatesText(Double lat, Double longitude) {
        return String.format("%.3f", lat) + ", " + String.format("%.3f", longitude);
    }
}