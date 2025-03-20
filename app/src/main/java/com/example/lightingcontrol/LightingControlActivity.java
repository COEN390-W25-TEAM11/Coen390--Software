package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.auth0.android.jwt.JWT;

import java.util.Objects;

import api.AuthService;
import api.LightService;
import api.RetrofitClient;
import retrofit2.Retrofit;

public class LightingControlActivity extends AppCompatActivity implements CreateLightFragment.RefreshAfterSave {

    protected SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_control);

        //SwitchCompat powerSwitch = findViewById(R.id.powerSwitch);
        //SwitchCompat sensorSwitch = findViewById(R.id.sensorSwitch);
        //SeekBar brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        Button motionLogsBtn = findViewById(R.id.motionLogsButton);
        //Button settingsBtn = findViewById(R.id.settingsButton);
        Button newLightBtn = findViewById(R.id.newLightButton);
        TextView helloUser = findViewById(R.id.helloUser);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lighting Control");

        // setup hello user text
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        String token = sharedPreferencesHelper.getToken();
        if (token != null) {
            JWT jwt = new JWT(token);
            String username = jwt.getClaim("sub").asString();

            if (username != null) {
                helloUser.setText("Hello " + username + "!");
            } else {
                helloUser.setText("Hello User!"); // Fallback in case username is null
            }
        } else {
            helloUser.setText("Hello Guest!");
        }

        // functionality for settings button
        /*settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LightingControlActivity.this, SettingsActivity.class));
            }
        });
        */

        // when new light button is clicked, open create light fragment
        newLightBtn.setOnClickListener(v -> {
            // open the fragment
            CreateLightFragment createLightFragment = new CreateLightFragment();
            createLightFragment.setRefreshAfterSave(LightingControlActivity.this);
            createLightFragment.show(getSupportFragmentManager(), "createLightFragment");
        });

        // functionality for motion logs button
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

    @Override
    public void refreshList() {
        loadListView();
    }

    // reload listview with any added light
    private void loadListView() {

    }
}
