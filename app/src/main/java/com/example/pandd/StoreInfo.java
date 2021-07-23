package com.example.pandd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class StoreInfo extends AppCompatActivity {

    private TextView tvName;
    private TextView tvAddress;
    private TextView tvMapId;
    private TextView tvCoordinates;
    private Button btnSearch;

    private String mapId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_info);

        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvMapId = findViewById(R.id.tvMapId);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        btnSearch = findViewById(R.id.btnSearch);

        Intent intent = getIntent();
        mapId = intent.getStringExtra("mapId");
        tvName.setText(intent.getStringExtra("name"));
        tvAddress.setText(intent.getStringExtra("address"));
        tvMapId.setText(mapId);
        tvCoordinates.setText(intent.getStringExtra("coordinates"));



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
}