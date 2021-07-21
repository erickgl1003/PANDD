package com.example.pandd;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pandd.models.Post;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import im.delight.android.location.SimpleLocation;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapFragment";
    private GoogleMap mMap;
    Place location;
    private double userLat;
    private double userLong;
    private int km = 10;
    protected List<Post> allPosts = new ArrayList<Post>();
    List<Marker> markers = new ArrayList<Marker>();
    private SimpleLocation userLocation;


    public MapFragment() {
        // Required empty public constructor
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

    private boolean withinRange(Post post, int km) {
        //Calculates the distance between user and store.
        LatLng storell = new LatLng(post.getStore().getDouble("lat"), post.getStore().getDouble("long"));
        double dist = SimpleLocation.calculateDistance(storell.latitude, storell.longitude, userLat, userLong);
        dist /= 1000;
        return (dist <= km);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Once the google Map's map loads, put in inside our own map, and query the posts
        mMap = googleMap;

        //Initialize SimpleLocation to set the user current latitude and longitude
        Context context = getActivity();
        userLocation = new SimpleLocation(context);
        userLat = userLocation.getLatitude();
        userLong = userLocation.getLongitude();

        //Set the map to focus on the user location
        LatLng latLng = new LatLng(userLat, userLong);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        queryPosts();
    }
    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        query.include(Post.KEY_USER);
        query.include(Post.KEY_STORE);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                allPosts.addAll(posts);
                addStores();
            }
        });
    }

    private void addStores() {
        //Once the posts are queried, get the stores and put them inside our map

        List<LatLng> added = new ArrayList<>();
        for(int i = 0; i < allPosts.size();i++){
            Post post = allPosts.get(i);
            LatLng latLng = new LatLng(post.getStore().getDouble("lat"), post.getStore().getDouble("long"));
            if(withinRange(post,km) && !added.contains(latLng) ){
                String name = post.getStore().getString("name");
                markers.add(addMarker(latLng, name));
                added.add(latLng);
            }
            else if(withinRange(post,km) && added.contains(latLng)){
                int index = added.indexOf(latLng);
                Marker mark = markers.get(index);
                int posts =  Integer.parseInt(mark.getSnippet());
                mark.setSnippet(String.valueOf(posts+1));
            }
        }
    }

    public Marker addMarker(LatLng latLng, String name){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(name);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions.snippet("1");


        return mMap.addMarker(markerOptions);
    }


    @Override
    public boolean onMarkerClick(@NonNull @NotNull Marker marker) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
        query.whereEqualTo("long",marker.getPosition().longitude);
        query.whereEqualTo("lat",marker.getPosition().latitude);
        try {
            List<ParseObject> objects = query.find();
            ParseObject store = objects.get(0);
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra("field","store");
            intent.putExtra("value",store.getObjectId());
            getActivity().startActivity(intent);

        } catch (ParseException e) {
            Toasty.error(getActivity(),e.getMessage(),Toasty.LENGTH_SHORT).show();
            Log.i(TAG, String.valueOf(marker.getPosition().longitude) + "  " + String.valueOf(marker.getPosition().latitude));
            return false;
        }

        return true;
    }
}