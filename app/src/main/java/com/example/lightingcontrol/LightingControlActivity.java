package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lightingcontrol.account.AccountActivity;
import com.example.lightingcontrol.auth.MainActivity;
import com.example.lightingcontrol.helpers.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;
import java.util.LinkedList;

import api.LightService;
import api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LightingControlActivity extends AppCompatActivity {
    // Services and helpers
    protected SharedPreferencesHelper sharedPreferencesHelper;
    private LightService lightService;

    // UI elements
    private ListView lightAndSensorListView;
    private ListView comboListView;

    // Data
    private LightService.GetResponse data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_control);

        // Initialize MaterialToolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBarDashboard);
        setSupportActionBar(toolbar);

        // Configure toolbar with white back arrow and title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIconTint(getResources().getColor(android.R.color.white));
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        }

        // Initialize views
        TextView helloUser = findViewById(R.id.helloUser);
        TextView addLightText = findViewById(R.id.addLightText);
        TextView myAccountText = findViewById(R.id.myAccountText);
        lightAndSensorListView = findViewById(R.id.listView1);
        comboListView = findViewById(R.id.listView2);

        // Set click listeners for menu items
        addLightText.setOnClickListener(v -> onAddLightClick());
        myAccountText.setOnClickListener(v -> onMyAccountClick());

        // Get JWT token
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        // Initialize retrofit and lightservice
        Retrofit retrofit = RetrofitClient.getRetrofit(sharedPreferencesHelper.getToken());
        lightService = retrofit.create(LightService.class);

        String username = sharedPreferencesHelper.getUsername();
        helloUser.setText(username != null ? "Hello " + username + "!" : "Hello User!");

        initListViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to LoginActivity instead of finishing
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initListViews() {
        lightAndSensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                var item = (KeyValueItem) parent.getItemAtPosition(position);

                if (item.type.equals("Light")) {
                    Intent intent = new Intent(LightingControlActivity.this, SpecificLightActivity.class);
                    intent.putExtra("light", Arrays.stream(data.lights).filter(e -> e.id.equals(item.id)).findFirst().get());
                    LightingControlActivity.this.startActivity(intent);
                } else if (item.type.equals("Sensor")) {
                    Intent intent = new Intent(LightingControlActivity.this, SpecificSensorActivity.class);
                    intent.putExtra("sensor", Arrays.stream(data.sensors).filter(e -> e.id.equals(item.id)).findFirst().get());
                    LightingControlActivity.this.startActivity(intent);
                }
            }
        });

        comboListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                var item = (ComboItem) parent.getItemAtPosition(position);

                new AlertDialog.Builder(LightingControlActivity.this)
                        .setTitle("Confirm")
                        .setMessage("Are you sure you want to delete this combination?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Call<Void> call = lightService.deleteAssign(item.id);

                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (!response.isSuccessful()) {
                                        Toast.makeText(LightingControlActivity.this, "The combination could not be deleted", Toast.LENGTH_SHORT).show();
                                    }
                                    loadData();
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(LightingControlActivity.this, "The combination could not be deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                        })
                        .show();

            }
        });
    }

    private void loadData() {
        Call<LightService.GetResponse> call = lightService.get();
        call.enqueue(new Callback<LightService.GetResponse>() {

            @Override
            public void onResponse(Call<LightService.GetResponse> call, Response<LightService.GetResponse> response) {
                if (response.isSuccessful()) {
                    data = response.body();
                    updateListView();
                } else {
                    Toast.makeText(LightingControlActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LightService.GetResponse> call, Throwable t) {
                Toast.makeText(LightingControlActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateListView() {
        LinkedList<KeyValueItem> lightAndSensorList = new LinkedList<>();

        for (var e : this.data.lights) {
            lightAndSensorList.add(new KeyValueItem("Light", e.id, e.name));
        }

        for (var e : this.data.sensors) {
            lightAndSensorList.add(new KeyValueItem("Sensor", e.id, e.name));
        }

        lightAndSensorListView.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, lightAndSensorList));

        LinkedList<ComboItem> comboList = new LinkedList<>();

        for (var e : this.data.combinations) {
            comboList.add(new ComboItem(
                    e.id,
                    lightAndSensorList.stream().filter(item -> item.id.equals(e.lightId)).findFirst().get().name,
                    lightAndSensorList.stream().filter(item -> item.id.equals(e.sensorId)).findFirst().get().name
            ));
        }

        comboListView.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, comboList));
    }

    private void onAddLightClick() {
        CreateLightFragment createLightFragment = new CreateLightFragment();
        createLightFragment.setRefreshCallback(this::loadData);
        createLightFragment.setData(this.data);
        createLightFragment.show(getSupportFragmentManager(), "createLightFragment");
    }

    private void onMyAccountClick() {
        // Navigate to AccountActivity
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
    }

    private static class KeyValueItem {
        public String type;
        public String id;
        public String name;

        public KeyValueItem(String type, String id, String name) {
            this.type = type;
            this.id = id;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return type + ": " + name;
        }
    }

    private static class ComboItem {
        public String id;
        public String lightName;
        public String sensorName;

        public ComboItem(String id, String lightName, String sensorName) {
            this.id = id;
            this.lightName = lightName;
            this.sensorName = sensorName;
        }

        @NonNull
        @Override
        public String toString() {
            return lightName + " and " + sensorName;
        }
    }
}