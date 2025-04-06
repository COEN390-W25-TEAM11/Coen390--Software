package com.example.lightingcontrol;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import api.LightService;
import api.RetrofitClient;
import api.WebSocketClient;
import okhttp3.WebSocket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import androidx.fragment.app.DialogFragment;

public class SpecificLightActivity extends AppCompatActivity {

    private String currentLightId, currentLightName;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private LightService lightService;
    private LightService.LightResponse lightResponse;
    private LightService.Light light;
    private boolean lightInitialized = false;
    private UUID lightId;
    private WebSocketClient webSocketClient;
    private MyWebSocketListener webSocketListener;

    private List<String> formattedMotionHistory = new ArrayList<>();
    private ArrayAdapter<String> motionHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_light);

        // Initialize MaterialToolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBarLight);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIconTint(getResources().getColor(android.R.color.white));
        }

        // Initialize views
        TextView lightName = findViewById(R.id.lightName);
        TextView lightInformation = findViewById(R.id.lightInformation);
        TextView lightStatus = findViewById(R.id.lightStatus);
        Button overrideButton = findViewById(R.id.overrideButton);
        ToggleButton toggleButton = findViewById(R.id.toggleButton);
        ListView listView = findViewById(R.id.listView);

        // Get intent data
        currentLightId = getIntent().getStringExtra("lightId");
        currentLightName = getIntent().getStringExtra("lightName");
        lightName.setText(currentLightName);

        // Initialize motion history
        motionHistoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                formattedMotionHistory
        );
        listView.setAdapter(motionHistoryAdapter);

        // Initialize services
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        Retrofit retrofit = RetrofitClient.getRetrofit(sharedPreferencesHelper.getToken());
        lightService = retrofit.create(LightService.class);

        // Setup web socket
        webSocketListener = new MyWebSocketListener();
        webSocketClient = new WebSocketClient(webSocketListener, sharedPreferencesHelper.getToken());
        webSocketClient.connectWebSocket();

        // Fetch light data
        fetchLightData();
        fetchMotionHistory();

        // Set up button click listeners
        overrideButton.setOnClickListener(v -> toggleMode());
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (lightInitialized) {
                light.setState(isChecked ? 1 : 0);
                updateLight(light);
                updateLightStatus();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.light, menu);

        // Change trash icon color to white
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        if (deleteItem != null) {
            Drawable deleteIcon = deleteItem.getIcon();
            if (deleteIcon != null) {
                deleteIcon = deleteIcon.mutate();
                deleteIcon.setColorFilter(getResources().getColor(android.R.color.white),
                        PorterDuff.Mode.SRC_ATOP);
                deleteItem.setIcon(deleteIcon);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Add this new method:
    private void showDeleteDialog() {
        DeleteLightFragment deleteFragment = DeleteLightFragment.newInstance(currentLightName);
        deleteFragment.setDeleteLightListener(new DeleteLightFragment.DeleteLightListener() {
            @Override
            public void onDeleteConfirmed() {
                // For now, just show a toast
                Toast.makeText(SpecificLightActivity.this,
                        "Would delete " + currentLightName + " (implementation coming)",
                        Toast.LENGTH_SHORT).show();

                // Later you'll replace this with actual delete code:
                // deleteLight();
            }
        });
        deleteFragment.show(getSupportFragmentManager(), "deleteLightFragment");
    }
    private void fetchLightData() {
        lightId = UUID.fromString(currentLightId);
        Call<LightService.LightResponse> call = lightService.getLightById(lightId);
        call.enqueue(new Callback<LightService.LightResponse>() {
            @Override
            public void onResponse(Call<LightService.LightResponse> call, Response<LightService.LightResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lightResponse = response.body();
                    light = new LightService.Light();
                    light.setId(lightResponse.getId());
                    light.setName(lightResponse.getName());
                    light.setOveride(lightResponse.isOveride());
                    light.setState(lightResponse.getState());
                    lightInitialized = true;
                    updateUI();
                } else {
                    Toast.makeText(SpecificLightActivity.this, "Error getting light: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LightService.LightResponse> call, Throwable t) {
                Toast.makeText(SpecificLightActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        TextView lightInformation = findViewById(R.id.lightInformation);
        Button overrideButton = findViewById(R.id.overrideButton);
        ToggleButton toggleButton = findViewById(R.id.toggleButton);

        lightInformation.setText(String.format("Connected Sensor: \nCurrent Mode: %s",
                light.isOveride() ? "Manual Mode" : "Sensor Mode"));

        updateLightStatus();

        if (light.isOveride()) {
            overrideButton.setText("Switch to Sensor Mode");
            toggleButton.setVisibility(android.view.View.VISIBLE);
            toggleButton.setChecked(light.getState() == 1);
        } else {
            overrideButton.setText("Switch to Manual Mode");
            toggleButton.setVisibility(android.view.View.INVISIBLE);
        }
    }

    private void updateLightStatus() {
        TextView lightStatus = findViewById(R.id.lightStatus);
        lightStatus.setText(light.getState() == 1 ? "Light is ON" : "Light is OFF");
        lightStatus.setTextColor(light.getState() == 1 ?
                getResources().getColor(android.R.color.holo_green_dark) :
                getResources().getColor(android.R.color.holo_red_dark));
    }

    private void toggleMode() {
        if (lightInitialized) {
            light.setOveride(!light.isOveride());
            updateLight(light);
            updateUI();
        }
    }

    private void updateLight(LightService.Light light) {
        LightService.LightUpdateDto lightUpdateDto = new LightService.LightUpdateDto(
                light.getName(), light.getState(), light.isOveride());

        Call<Void> call = lightService.patchLight(lightId.toString(), lightUpdateDto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SpecificLightActivity.this, "Failed to update light", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SpecificLightActivity.this, "Network error updating light", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMotionHistory() {
        ListView listView = findViewById(R.id.listView);
        TextView motionHistoryHeader = findViewById(R.id.motionHistoryHeader);

        Call<List<LightService.MotionHistory>> call = lightService.getMotionByLight(lightId);
        call.enqueue(new Callback<List<LightService.MotionHistory>>() {
            @Override
            public void onResponse(Call<List<LightService.MotionHistory>> call, Response<List<LightService.MotionHistory>> response) {
                if (response.isSuccessful()) {
                    List<LightService.MotionHistory> motionHistory = response.body();

                    if (motionHistory != null && !motionHistory.isEmpty()) {
                        formattedMotionHistory.clear();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.US);
                        SimpleDateFormat displayFormat = new SimpleDateFormat("h:mm a\nEEEE, MMMM d, yyyy", Locale.US);

                        for (LightService.MotionHistory history : motionHistory) {
                            try {
                                Date date = dateFormat.parse(history.getDateTime());
                                formattedMotionHistory.add("Motion detected at " + displayFormat.format(date));
                            } catch (ParseException e) {
                                Log.e("DateParse", "Error parsing date", e);
                            }
                        }

                        runOnUiThread(() -> {
                            motionHistoryAdapter.notifyDataSetChanged();
                            motionHistoryHeader.setVisibility(android.view.View.VISIBLE);
                            listView.setVisibility(android.view.View.VISIBLE);
                        });
                    } else {
                        runOnUiThread(() -> {
                            motionHistoryHeader.setText("No motion history available");
                            motionHistoryHeader.setVisibility(android.view.View.VISIBLE);
                            listView.setVisibility(android.view.View.INVISIBLE);
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<LightService.MotionHistory>> call, Throwable t) {
                runOnUiThread(() -> {
                    motionHistoryHeader.setText("Error loading motion history");
                    motionHistoryHeader.setVisibility(android.view.View.VISIBLE);
                });
            }
        });
    }

    private class MyWebSocketListener extends WebSocketClient.MyWebSocketListener {
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if (text.contains("has movement") && text.contains(currentLightName)) {
                runOnUiThread(() -> fetchMotionHistory());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            //webSocketClient.disconnectWebSocket();
        }
    }
}
