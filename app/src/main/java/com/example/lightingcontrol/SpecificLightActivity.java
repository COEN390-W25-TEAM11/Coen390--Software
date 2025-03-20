package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import api.LightService;
import api.RetrofitClient;
import api.WebSocketClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SpecificLightActivity extends AppCompatActivity {

    String currentLightId, currentLightName;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private LightService lightService;
    private LightService.LightResponse lightResponse;
    private LightService.Light light;
    private boolean sensorMode = true;
    private boolean manualMode = false;
    private boolean lightInitialized = false;
    private UUID lightId;
    private WebSocketClient webSocketClient;
    private TextView noMotionMessage;

    private TextView lightInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_light);

        // get the data that was passed from LightingControlActivity
        Intent intent = getIntent();
        currentLightId = intent.getStringExtra("lightId");
        currentLightName = intent.getStringExtra("lightName");

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Light");

        // setup UI elements
        TextView lightName = findViewById(R.id.lightName);
        lightName.setText("Selected light: " + currentLightName);
        TextView lightInformation = findViewById(R.id.lightInformation);
        Button overrideButton = findViewById(R.id.overrideButton);
        overrideButton.setText("Click for Manual Mode");
        ToggleButton toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setVisibility(ToggleButton.INVISIBLE); // set invisible by default
        ListView listView = findViewById(R.id.listView);
        noMotionMessage = findViewById(R.id.noMotionMessage);
        noMotionMessage.setVisibility(noMotionMessage.INVISIBLE); // set invisible by default

        // get JWT token
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        String token = sharedPreferencesHelper.getToken();

        // initialize retrofit and lightservice
        Retrofit retrofit = RetrofitClient.getRetrofit(token);
        lightService = retrofit.create(LightService.class);

        // connect web socket
        webSocketClient = new WebSocketClient(new WebSocketClient.MyWebSocketListener());
        webSocketClient.connectWebSocket();

        // display light information
        lightId = UUID.fromString(currentLightId); // convert string id to UUID
        Log.d("UUID: ", String.valueOf(lightId));

        // get the light object using endpoint
        Call<LightService.LightResponse> call = lightService.getLightById(lightId);
        call.enqueue(new Callback<LightService.LightResponse>() {
            @Override
            public void onResponse(Call<LightService.LightResponse> call, Response<LightService.LightResponse> response) {
                if (response.isSuccessful()) {
                    lightResponse = response.body();
                    if (lightResponse != null) {

                        // set light object that is open
                        light = new LightService.Light();
                        light.setId(lightResponse.getId());
                        light.setName(lightResponse.getName());
                        light.setOveride(lightResponse.isOveride());
                        light.setState(lightResponse.getState());
                        lightInitialized = true;

                        Log.d("light id: ", lightResponse.getId());
                        Log.d("light name: ", lightResponse.getName());

                        refreshInfo();

                    } else {
                        Toast.makeText(SpecificLightActivity.this, "Light data is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SpecificLightActivity.this, "Error getting light" + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LightService.LightResponse> call, Throwable t) {
                Toast.makeText(SpecificLightActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // fetch motion activity
        fetchMotionHistory();

        // when override button is clicked, switch modes
        overrideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lightInitialized && manualMode) { // if in manual mode before the click

                    manualMode = false;
                    sensorMode = true; // switch to sensor mode
                    overrideButton.setText("Click for Manual Mode");
                    toggleButton.setVisibility(ToggleButton.INVISIBLE);

                    // update the light
                    light.setOveride(false);
                    updateLight(light);

                } else if (lightInitialized && sensorMode) { // if in sensor mode before the click

                    sensorMode = false;
                    manualMode = true; // switch to manual mode
                    overrideButton.setText("Click for Sensor Mode");
                    toggleButton.setVisibility(ToggleButton.VISIBLE); // allow on/off toggling

                    // update the light
                    light.setOveride(true);
                    updateLight(light);

                } else {
                    Toast.makeText(SpecificLightActivity.this, "Error switching modes", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // functionality for toggle
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lightInitialized) {

                    if (toggleButton.isChecked()) {
                        light.setState(1);
                        updateLight(light);
                        lightInformation.setText("Current state: OFF");

                        Log.d("light name: ", light.getName());
                    } else {
                        light.setState(0);
                        updateLight(light);
                        lightInformation.setText("Current state: ON");
                    }

                } else {
                    Toast.makeText(SpecificLightActivity.this, "Error toggling", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // update the light using patch endpoint
    private void updateLight(LightService.Light light) {

        LightService.LightUpdateDto lightUpdateDto = new LightService.LightUpdateDto(light.getName(), light.getState(), light.isOveride());

        Call<Void> call = lightService.patchLight(lightId.toString(), lightUpdateDto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("updateLight", "success updating light");
                } else {
                    Toast.makeText(SpecificLightActivity.this, "Failed to update light", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SpecificLightActivity.this, "Network error updating light" , Toast.LENGTH_SHORT).show();
            }
        });
    }

    // refresh the UI
    private void refreshInfo() {

        // refresh the current state
        TextView lightInformation = findViewById(R.id.lightInformation);
        if (lightResponse.getState() == 0) {
            lightInformation.setText("Current state: OFF");
        } else {
            lightInformation.setText("Current state: ON");
        }

        // refresh the motion logs ...
    }

    private void fetchMotionHistory() {
        ListView listView = findViewById(R.id.listView);
        Call<List<LightService.MotionHistory>> call = lightService.getMotionByLight(lightId); // Use the new endpoint
        call.enqueue(new Callback<List<LightService.MotionHistory>>() {
            @Override
            public void onResponse(Call<List<LightService.MotionHistory>> call, Response<List<LightService.MotionHistory>> response) {
                if (response.isSuccessful()) {
                    List<LightService.MotionHistory> motionHistory = response.body();

                    if (motionHistory != null && !motionHistory.isEmpty()) {
                        // motion history exists, display it in the ListView
                        ArrayAdapter<LightService.MotionHistory> adapter = new ArrayAdapter<>(
                                SpecificLightActivity.this,
                                android.R.layout.simple_list_item_1,
                                motionHistory
                        );
                        listView.setAdapter(adapter);
                        listView.setVisibility(View.VISIBLE); // Show the ListView
                        noMotionMessage.setVisibility(View.INVISIBLE); // Hide the message
                    } else {
                        // no motion history, display the message
                        listView.setVisibility(View.INVISIBLE); // hide the listview.
                        noMotionMessage.setVisibility(View.VISIBLE);
                        noMotionMessage.setText("No motion history to display");
                        //Toast.makeText(SpecificLightActivity.this, "No motion history to display", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // error fetching motion history
                    listView.setVisibility(View.INVISIBLE); // hide the listview.
                    noMotionMessage.setVisibility(View.VISIBLE);
                    noMotionMessage.setText("Error displaying motion history");
                    //Toast.makeText(SpecificLightActivity.this, "Failed to fetch motion history: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LightService.MotionHistory>> call, Throwable t) {
                Toast.makeText(SpecificLightActivity.this, "Network error fetching motion history", Toast.LENGTH_SHORT).show();
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
            Intent lightIntent = new Intent(this, LightingControlActivity.class);
            startActivity(lightIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
