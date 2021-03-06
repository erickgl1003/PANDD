package com.example.pandd;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class PostFragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = "PostFragment";
    private static final int UPLOAD_REQUEST = 50;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;;

    private TextView tvBarcode;
    private EditText etDescription;
    private EditText etProduct;
    private EditText etExpiring;
    private Button btnCaptureImage;
    private Button btnSubmit;
    private Button btnScan;
    private BottomNavigationView bottomNavigationView;

    Bitmap bitmap = null;
    File imagT = null;

    private GoogleMap mMap;
    Place location;

    private boolean locationFilled = false;
    String barcode = "";

    private int primaryColor;

    ProgressDialog progressdialog = null;

    public PostFragment(){
        //Required empty public constructor because it's a Fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_post, parent, false);

        //Initialize the Places class using the context from this fragment and the Maps API Key
        ApplicationInfo ai = null;
        try {
            ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(),PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
        //Application info can't be null since it is required to get the API Key, in order for the application to execute properly.
        assert ai != null;
        Places.initialize(getActivity().getApplicationContext(), ai.metaData.getString("com.google.android.geo.API_KEY"));
        PlacesClient placesClient = Places.createClient(getActivity());

        //Get and initialize the supportMapFragment class
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return and style of the element
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        autocompleteFragment.setHint("Search store...");

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
                //This just means the user closed the autocomplete fragment, not really an error! Still Log'd it just in case.
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
        etExpiring = view.findViewById(R.id.etExpiring);
        btnCaptureImage = view.findViewById(R.id.btnCapture);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnScan = view.findViewById(R.id.btnScan);
        bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);

        //Set progressdialog properties
        progressdialog = new ProgressDialog(getActivity(),R.style.AppCompatAlertDialogStyle);
        progressdialog.setMessage("Please wait...");
        progressdialog.setCancelable(false);

        //Get the primary color from the app for styling
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primaryColor = typedValue.data;

        etExpiring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUploadPhoto(UPLOAD_REQUEST);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                String product = etProduct.getText().toString();
                Date expiring = null;
                if(!etExpiring.getText().toString().isEmpty()) {
                    try {
                        expiring = new SimpleDateFormat("dd/MM/yyyy").parse(etExpiring.getText().toString());
                    } catch (java.text.ParseException e) {
                        //Should never happen because dates are obtained through a datePicker, but in case it does:
                        Toasty.error(getActivity(), "Error formatting date",Toast.LENGTH_SHORT).show();
                    }
                }

                if(verifyEmpty(description, "Description")) return;
                if(verifyEmpty(product, "Product")) return;

                if(!locationFilled){
                    Toasty.warning(getActivity(),"Store can't be empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                //Show progress dialog while post is made
                progressdialog.show();

                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, product, barcode, location, currentUser, imagT,expiring);
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // Month +1 because January is returned as 0
                final String selectedDate = twoDigits(day) + "/" + twoDigits(month+1) + "/" + year;
                etExpiring.setText(selectedDate);
            }
        });
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    //Add a 0 before the number if it is a single digit value
    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    private boolean verifyEmpty(String string, String field) {
        if(string.isEmpty()){
            Toasty.warning(getActivity(),field + " field can't be empty!",Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void launchCamera() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void scan(Bitmap takenImage) {
        //Receives the image in bitmap and uses BarcodeScanner.process to find the barcodes.
        InputImage image = InputImage.fromBitmap(takenImage, 0);
        BarcodeScanner scanner = BarcodeScanning.getClient();

        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        if(barcodes.size() == 0){
                            Toasty.error(getActivity(),"No barcode detected",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(barcodes.size() > 1){
                            Toasty.warning(getActivity(),"There's more than 1 barcode! Be sure to only scan one barcode per post",Toast.LENGTH_LONG).show();
                            return;
                        }
                        Barcode barcodeScanned = barcodes.get(0);
                        String rawValue = barcodeScanned.getRawValue();
                        int valueType = barcodeScanned.getValueType();

                        if(valueType != Barcode.TYPE_PRODUCT && valueType != Barcode.TYPE_TEXT ){
                            Toasty.warning(getActivity(),"That's not a product barcode! Be sure to scan only valid barcodes",Toast.LENGTH_LONG).show();
                            Log.i(TAG, String.valueOf(valueType));
                            return;
                        }

                        barcode = rawValue;

                        if(!barcode.equals("")){
                            String barcodeText = "Barcode: " + barcode;
                            Spannable spannable = customize(barcodeText,8,barcodeText.length());
                            tvBarcode.setText(spannable, TextView.BufferType.SPANNABLE);
                            tvBarcode.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void onUploadPhoto(int requestCode){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
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
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
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
                Toasty.success(getActivity(),"Image uploaded successfully!",Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }
        }

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //Get photo file from intent and convert to bitmap.
                String filePath = data.getStringExtra("photo");
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                bitmap = getResizedBitmap(bitmap);
                scan(bitmap);

            }else { // Result was a failure
                Toasty.warning(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int newWidth = width/2;
        int newHeight = height/2;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // Create a Matrix to manipulate it
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        // "Recreate" the new, resized, bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    private void savePost(String description, String product, String code, Place place, ParseUser currentUser, File photoFile, Date expiring) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(currentUser);
        post.setProduct(product.toLowerCase());
        ParseObject store = getStore(place);
        post.setStore(store);

        //Optional fields
        if(!barcode.equals(""))
            post.setBarcode(code);
        if(photoFile != null)
            post.setImage(new ParseFile(photoFile));
        if(expiring != null)
            post.setExpiring(expiring);

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.e(TAG,"Error while saving post ",e);
                    Toasty.error(getActivity(),"Error while saving post", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG,"Post saved successfully");
                etDescription.setText("");
                etProduct.setText("");
                tvBarcode.setVisibility(View.GONE);
                barcode = "";
                etExpiring.setText("");
                Toasty.success(getActivity(),"Post successfully published!", Toast.LENGTH_SHORT).show();
                progressdialog.dismiss();
                getSubscribedUsers(store);
            }
        });
    }

    private void getSubscribedUsers(ParseObject store) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Notify");
        query.whereEqualTo("storeid",store.getString("mapId"));
        query.whereNotEqualTo("userid", ParseUser.getCurrentUser());

        try {
            List<ParseObject> objects = query.find();
            if(objects.size()  > 0){
                for(ParseObject object : objects){
                    //If the current user is subscribed, don't notify him, but everyone else.
                    notifyUser(object.getParseUser("userid"), store.getString("name"));
                }
            }
            bottomNavigationView.setSelectedItemId(R.id.home);

        } catch (ParseException error) {
            Toasty.error(getActivity(),error.getMessage(),Toasty.LENGTH_SHORT).show();
        }

    }

    private void notifyUser(ParseUser userid, String storeName) {
        JSONObject data = new JSONObject();
        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
        //Find the installation instance where the user is logged in (if any)
        pushQuery.whereEqualTo("userId", userid);
        try {
            data.put("alert", "Store name: " + storeName);
            data.put("title", "New post in a subscribed store!");
        } catch (JSONException e) {
            Toasty.error(getActivity(),"Error notifying users",Toast.LENGTH_SHORT).show();
            Log.i(TAG,e.getMessage());
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();

    }

    //TODO: Use Google api function to retrieve real distance between locations
    private ParseObject getStore(Place place) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
        query.whereEqualTo("mapId",place.getId());
        try {
            List<ParseObject> objects = query.find();
            if(objects.isEmpty()){//If the store isn't registered in the database, create it
                Store store = new Store();
                store.setName(place.getName());
                store.setAddress(place.getAddress());
                store.setMapId(place.getId());
                store.setLat(place.getLatLng().latitude);
                store.setLong(place.getLatLng().longitude);
                try {
                    store.save();
                    objects.add(store);
                } catch (ParseException e) {
                    Toasty.error(getActivity(),"Error saving: " + e.getMessage(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            return objects.get(0);
        } catch (ParseException e) {
            Toasty.error(getActivity(),"Error querying: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public Spannable customize(String text, int start, int end){//Custom Spannable to give colors and style to text
        Spannable spannableText = new SpannableString(text);
        spannableText.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(primaryColor),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableText;
    }

}
