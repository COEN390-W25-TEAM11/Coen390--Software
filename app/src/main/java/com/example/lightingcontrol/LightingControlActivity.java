package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

public class LightingControlActivity extends AppCompatActivity {

    private Switch powerSwitch, sensorSwitch;
    private SeekBar brightnessSeekBar, sensitivitySeekBar;
    private Button settingsBtn, presetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_control);

        powerSwitch = findViewById(R.id.powerSwitch);
        sensorSwitch = findViewById(R.id.sensorSwitch);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        sensitivitySeekBar = findViewById(R.id.sensitivitySeekBar);
        settingsBtn = findViewById(R.id.settingsButton);
        presetBtn = findViewById(R.id.presetButton);

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
    }
}
