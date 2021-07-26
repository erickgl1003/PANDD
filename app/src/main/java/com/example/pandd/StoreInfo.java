package com.example.pandd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pandd.models.Notify;
import com.parse.DeleteCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class StoreInfo extends AppCompatActivity {

    private TextView tvName;
    private TextView tvAddress;
    private TextView tvMapId;
    private TextView tvCoordinates;
    private Button btnSearch;
    private Button btnNotify;

    private String mapId;
    private ParseUser userId;
    boolean alreadyInNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_info);

        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvMapId = findViewById(R.id.tvMapId);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        btnSearch = findViewById(R.id.btnSearch);
        btnNotify = findViewById(R.id.btnNotify);

        Intent intent = getIntent();
        mapId = intent.getStringExtra("mapId");
        userId = ParseUser.getCurrentUser();
        tvName.setText(intent.getStringExtra("name"));
        tvAddress.setText(intent.getStringExtra("address"));
        tvMapId.setText(mapId);
        tvCoordinates.setText(intent.getStringExtra("coordinates"));

        alreadyInNotify = queryUserNotified();
        if(alreadyInNotify){
            btnNotify.setText("Stop notifying me");
        }

        btnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(alreadyInNotify){
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Notify");
                    query.whereEqualTo("storeid",mapId);
                    query.whereEqualTo("userid", userId);
                    try {
                        List<ParseObject> objects = query.find();
                        ParseObject row = objects.get(0);
                        row.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e != null) {
                                    Toasty.error(StoreInfo.this,"Error while unsubscribing", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                btnNotify.setText("Notify me");
                                alreadyInNotify = false;
                                Toasty.success(StoreInfo.this,"Notifications deactivated!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (ParseException e) {
                        Toasty.error(StoreInfo.this,e.getMessage(),Toasty.LENGTH_SHORT).show();
                    }
                }
                else{
                    Notify notify = new Notify();
                    notify.setStoreId(mapId);
                    notify.setUserId(userId);

                    notify.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null) {
                                Toasty.error(StoreInfo.this,"Error while subscribing", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            btnNotify.setText("Stop notifying me");
                            alreadyInNotify = true;
                            Toasty.success(StoreInfo.this,"Notifications enabled!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        //When button is clicked, do a query for posts in that store
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Store");
                query.whereEqualTo("mapId",mapId);
                try {
                    List<ParseObject> objects = query.find();
                    ParseObject store = objects.get(0);
                    Intent i = new Intent(StoreInfo.this, SearchActivity.class);
                    i.putExtra("field","store");
                    i.putExtra("value",store.getObjectId());
                    startActivity(i);
                    finish();

                } catch (ParseException e) {
                    Toasty.error(StoreInfo.this,e.getMessage(),Toasty.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean queryUserNotified() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Notify");
        query.whereEqualTo("storeid",mapId);
        query.whereEqualTo("userid", userId);
        try {
            List<ParseObject> objects = query.find();
            if(objects.size() == 1){
                return true;
            }

        } catch (ParseException e) {
            Toasty.error(StoreInfo.this,e.getMessage(),Toasty.LENGTH_SHORT).show();
        }
        return false;
    }


}