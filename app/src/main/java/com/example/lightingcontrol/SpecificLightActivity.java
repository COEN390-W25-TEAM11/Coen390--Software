package com.example.lightingcontrol;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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

public class SpecificLightActivity extends AppCompatActivity {


    // Services and helper
    private SharedPreferencesHelper sharedPreferencesHelper;
    private LightService lightService;

    // Data
    private LightService.GetResponse.LightResponse currentLight;

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

        // Get intent data
        currentLight = (LightService.GetResponse.LightResponse) getIntent().getSerializableExtra("light");

        // Initialize services
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        Retrofit retrofit = RetrofitClient.getRetrofit(sharedPreferencesHelper.getToken());
        lightService = retrofit.create(LightService.class);

        // Set click listeners on buttons
        Button renameButton = findViewById(R.id.renameButton);
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText input = new EditText(SpecificLightActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(currentLight.name);

                new AlertDialog
                        .Builder(SpecificLightActivity.this).setTitle("Light name")
                        .setView(input)
                        .setPositiveButton("Save", (dialog, which) -> {
                            currentLight.name = input.getText().toString();
                            updateInterface();
                            save();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                        })
                        .show();
            }
        });

        Button overrideButton = findViewById(R.id.overrideButton);
        overrideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLight.overide = !currentLight.overide;
                updateInterface();
                save();
            }
        });

        ToggleButton toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setChecked(currentLight.state == 1);
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentLight.state = isChecked ? 1 : 0;
            updateInterface();
            save();
        });

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
                    .Builder(SpecificLightActivity.this).setTitle("Delete light")
                    .setMessage("Are you sure you want to delete this light?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Call<Void> call = lightService.deleteLight(currentLight.id);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (!response.isSuccessful()) {
                                    Toast.makeText(SpecificLightActivity.this, "Could not delete light", Toast.LENGTH_SHORT).show();
                                } else {
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(SpecificLightActivity.this, "Could not delete light", Toast.LENGTH_SHORT).show();
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

    private void save() {
        Call<Void> call = lightService.updateLight(currentLight.id, new LightService.UpdateLightModel(currentLight.name, currentLight.overide, currentLight.state));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SpecificLightActivity.this, "Could not update light", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SpecificLightActivity.this, "Could not update light", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInterface() {
        TextView lightName = findViewById(R.id.lightName);
        TextView lightInformation = findViewById(R.id.lightInformation);
        TextView lightStatus = findViewById(R.id.lightStatus);
        Button overrideButton = findViewById(R.id.overrideButton);
        ToggleButton toggleButton = findViewById(R.id.toggleButton);

        lightName.setText(currentLight.name);

        lightInformation.setText(String.format("Light ID: %d \nCurrent Mode: %s", currentLight.pin, currentLight.overide ? "Manual Mode" : "Sensor Mode"));

        String status = "";
        if (currentLight.overide) {
            lightStatus.setVisibility(VISIBLE);
            lightStatus.setText(String.format("Light is %s", currentLight.state == 1 ? "On" : "Off"));
            lightStatus.setTextColor(currentLight.state == 1 ? Color.GREEN : Color.RED);
        } else {
            lightStatus.setVisibility(INVISIBLE);
        }

        if (currentLight.overide) {
            overrideButton.setText("Switch to Sensor Mode");
            toggleButton.setVisibility(VISIBLE);
        } else {
            overrideButton.setText("Switch to Manual Mode");
            toggleButton.setVisibility(INVISIBLE);
        }
    }
}
