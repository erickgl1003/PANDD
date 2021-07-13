package com.example.pandd;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.pandd.models.Post;
import com.example.pandd.models.Store;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class PostFragment extends Fragment implements OnMapReadyCallback {
    public static final String TAG = "PostFragment";
    private static final int UPLOAD_REQUEST = 50;

    private TextView tvBarcode;
    private EditText etDescription;
    private EditText etProduct;
    private Button btnCaptureImage;
    private Button btnSubmit;
    private Button btnScan;

    Bitmap bitmap = null;
    File imagT = null;

    private GoogleMap mMap;
    Place location;

    private boolean locationFilled = false;
    String barcode = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.activity_post, parent, false);

        //Initialize the Places class
        Places.initialize(getActivity().getApplicationContext(), "AIzaSyDJ3olO_dD1oGRywV3jPUumcFF12KgXJIM");
        PlacesClient placesClient = Places.createClient(getActivity());

        //Get and initialize the supportMapFragment class
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                addMarker(place);
                locationFilled = true;
                location = place;
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getActivity(),status.toString(),Toast.LENGTH_SHORT).show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvBarcode = view.findViewById(R.id.tvBarcode);
        etDescription = view.findViewById(R.id.etDescription);
        etProduct = view.findViewById(R.id.etProduct);
        btnCaptureImage = view.findViewById(R.id.btnCapture);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnScan = view.findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });

        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUploadPhoto();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                String product = etProduct.getText().toString();

                if(description.isEmpty()){
                    Toast.makeText(getActivity(),"Description can't be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(product.isEmpty()){
                    Toast.makeText(getActivity(),"Product can't be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!locationFilled){
                    Toast.makeText(getActivity(),"Store can't be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, product, barcode, location, currentUser, imagT);
            }
        });
    }


    //TODO Scan feature
    private void scan() {
    }

    public void onUploadPhoto(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, UPLOAD_REQUEST);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void addMarker(Place place){

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(place.getLatLng());
        markerOptions.title(place.getName()+"");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == UPLOAD_REQUEST && resultCode == Activity.RESULT_OK && data != null){
            Uri photoUri = data.getData();
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
            }catch (FileNotFoundException e){
                e.printStackTrace();
                Log.e(TAG, "File not found");
            } catch (IOException e){
                Log.d(TAG, e.getLocalizedMessage());
            }
            File testDir = getActivity().getApplicationContext().getFilesDir();
            imagT = new File(testDir, "photo.jpg");
            OutputStream os;
            try {
                os = new FileOutputStream(imagT);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }
        }
    }

    private void savePost(String description, String product, String code, Place place, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(currentUser);
        post.setProduct(product);
        post.setStore(getStore(place));

        //Optional fields
        if(!barcode.equals(""))
            post.setBarcode(code);
        if(photoFile != null)
            post.setImage(new ParseFile(photoFile));

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.e(TAG,"Error while saving post ",e);
                    Toast.makeText(getActivity(),"Error while saving post", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG,"Post saved successfully");
                etDescription.setText("");
                etProduct.setText("");
                tvBarcode.setVisibility(View.GONE);
                barcode = "";
                Toast.makeText(getActivity(),"Post successfully published!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ParseObject getStore(Place place) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
        query.whereEqualTo("mapId",place.getId());
        try {
            List<ParseObject> objects = query.find();
            if(objects.isEmpty()){
                Store store = new Store();
                store.setName(place.getName());
                store.setAddress(place.getAddress());
                store.setMapId(place.getId());
                try {
                    store.save();
                    objects.add(store);
                } catch (ParseException e) {
                    Toast.makeText(getActivity(),"Error saving: " + e.getMessage(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            return objects.get(0);
        } catch (ParseException e) {
            Toast.makeText(getActivity(),"Error querying: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return null;
    }

}
