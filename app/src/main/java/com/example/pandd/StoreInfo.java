package com.example.pandd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class StoreInfo extends AppCompatActivity {

    private TextView tvName;
    private TextView tvAddress;
    private TextView tvMapId;
    private TextView tvCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_info);

        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvMapId = findViewById(R.id.tvMapId);
        tvCoordinates = findViewById(R.id.tvCoordinates);

        Intent intent = getIntent();
        tvName.setText(intent.getStringExtra("name"));
        tvAddress.setText(intent.getStringExtra("address"));
        tvMapId.setText(intent.getStringExtra("mapId"));
        tvCoordinates.setText(intent.getStringExtra("coordinates"));

    }
}