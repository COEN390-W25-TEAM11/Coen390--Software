package com.example.lightingcontrol;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lightingcontrol.helpers.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import api.LightService;
import api.MotionNotificationClient;
import api.RetrofitClient;
import api.WebSocketClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SpecificSensorActivity extends AppCompatActivity {


    // Services, helper, and clients
    private SharedPreferencesHelper sharedPreferencesHelper;
    private LightService lightService;
    private MotionNotificationClient motionNotificationClient;

    // Data
    private LightService.GetResponse.SensorResponse currentSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_sensor);

        // Initialize MaterialToolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBarLight);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIconTint(getResources().getColor(android.R.color.white));
        }

        // Get intent data
        currentSensor = (LightService.GetResponse.SensorResponse) getIntent().getSerializableExtra("sensor");

        // Initialize services
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        Retrofit retrofit = RetrofitClient.getRetrofit(sharedPreferencesHelper.getToken());
        lightService = retrofit.create(LightService.class);

        // Set click listeners on buttons
        Button renameButton = findViewById(R.id.renameButton);
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(SpecificSensorActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(currentSensor.name);

                new AlertDialog
                        .Builder(SpecificSensorActivity.this).setTitle("Sensor name")
                        .setView(input)
                        .setPositiveButton("Save", (dialog, which) -> {
                            currentSensor.name = input.getText().toString();
                            updateInterface();
                            save();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                        })
                        .show();
            }
        });

        final int MIN_TIMEOUT = 1;
        final int MAX_TIMEOUT = 120;
        Button timeoutButton = findViewById(R.id.timeoutButton);
        timeoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(SpecificSensorActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setText(String.valueOf(currentSensor.timeout / 1000));
                input.setFilters(new InputFilter[]{new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        String newVal = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
                        if (newVal.isEmpty()) return null;

                        int input = Integer.parseInt(newVal);
                        if (input >= MIN_TIMEOUT && input <= MAX_TIMEOUT)
                            return null;

                        return "";
                    }
                }});

                new AlertDialog
                        .Builder(SpecificSensorActivity.this).setTitle("Sensor timeout")
                        .setView(input)
                        .setMessage(String.format(Locale.getDefault(), "Set the timeout for this sensor in seconds from %d to %d", MIN_TIMEOUT, MAX_TIMEOUT))
                        .setPositiveButton("Save", (dialog, which) -> {
                            currentSensor.timeout = Integer.parseInt(input.getText().toString()) * 1000;
                            save();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                        })
                        .show();
            }
        });

        motionNotificationClient = new MotionNotificationClient(sharedPreferencesHelper.getToken(), currentSensor.id);
        motionNotificationClient.setNewMovementListener(this::addMovement);

        updateInterface();
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
                deleteIcon.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
                deleteItem.setIcon(deleteIcon);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_delete) {
            new AlertDialog
                    .Builder(SpecificSensorActivity.this).setTitle("Delete sensor")
                    .setMessage("Are you sure you want to delete this sensor?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Call<Void> call = lightService.deleteSensor(currentSensor.id);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (!response.isSuccessful()) {
                                    Toast.makeText(SpecificSensorActivity.this, "Could not delete sensor", Toast.LENGTH_SHORT).show();
                                } else {
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(SpecificSensorActivity.this, "Could not delete sensor", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                    })
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        motionNotificationClient.stop();
        super.onDestroy();
    }

    private void save() {
        Call<Void> call = lightService.updateSensor(currentSensor.id, new LightService.UpdateSensorModel(currentSensor.name, currentSensor.sensitivity, currentSensor.timeout));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SpecificSensorActivity.this, "Could not update sensor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SpecificSensorActivity.this, "Could not update sensor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInterface() {
        TextView sensorName = findViewById(R.id.sensorName);
        TextView sensorInformation = findViewById(R.id.sensorInformation);
        ListView movementListVew = findViewById(R.id.listView);

        sensorName.setText(currentSensor.name);

        sensorInformation.setText(String.format(Locale.getDefault(), "Connected Sensor: %d", currentSensor.pin));

        var motionList = Arrays.asList(currentSensor.motion);
        motionList = motionList.stream().filter(e -> e.motion).collect(Collectors.toList());

        movementListVew.setAdapter(new ArrayAdapter<>(this, R.layout.list_view_item, motionList));
    }

    private void addMovement(LightService.GetResponse.MotionResponse movement) {
        var motionLinkedList = new LinkedList<LightService.GetResponse.MotionResponse>(Arrays.asList(currentSensor.motion));
        motionLinkedList.addFirst(movement);

        currentSensor.motion = motionLinkedList.toArray(new LightService.GetResponse.MotionResponse[0]);

        new Handler(Looper.getMainLooper()).post(this::updateInterface);
    }
}
