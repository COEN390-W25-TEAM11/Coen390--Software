package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class LightingControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_control);

        SwitchCompat powerSwitch = findViewById(R.id.powerSwitch);
        SwitchCompat sensorSwitch = findViewById(R.id.sensorSwitch);
        SeekBar brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        Button motionLogsBtn = findViewById(R.id.motionLogsButton);
        Button settingsBtn = findViewById(R.id.settingsButton);
        Button presetBtn = findViewById(R.id.presetButton);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lighting Control");

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LightingControlActivity.this, SettingsActivity.class));
            }
        });

        presetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LightingControlActivity.this, PresetActivity.class));
            }
        });

        motionLogsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LightingControlActivity.this, MotionLogsActivity.class));
            }
        });


    }

    // show the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    // functionalities for toolbar button (return)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.backtomain) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
