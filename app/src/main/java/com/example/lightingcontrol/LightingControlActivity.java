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

import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.JWT;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import api.LightService;
import api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LightingControlActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    protected SharedPreferencesHelper sharedPreferencesHelper;
    private LightService lightService;
    private List<LightService.LightResponse> lights;
    private ListView listView;

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
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        // Set click listeners for menu items
        addLightText.setOnClickListener(v -> onAddLightClick());
        myAccountText.setOnClickListener(v -> onMyAccountClick());

        // Get JWT token
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        String token = sharedPreferencesHelper.getToken();

        // Initialize retrofit and lightservice
        Retrofit retrofit = RetrofitClient.getRetrofit(token);
        lightService = retrofit.create(LightService.class);

        // Setup hello user header
        if (token != null) {
            JWT jwt = new JWT(token);
            String username = jwt.getClaim("sub").asString();
            helloUser.setText(username != null ? "Hello " + username + "!" : "Hello User!");
        } else {
            helloUser.setText("Hello Guest!");
        }

        // Fetch lights from API endpoint
        fetchLights();
    }

    // Handle back button click
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

    // Handle "Add a new light" click
    private void onAddLightClick() {
        CreateLightFragment createLightFragment = new CreateLightFragment();
        createLightFragment.setRefreshAfterSave(() -> fetchLights());
        createLightFragment.show(getSupportFragmentManager(), "createLightFragment");
    }

    // Handle "My Account" click
    private void onMyAccountClick() {
        Toast.makeText(this, "My Account clicked", Toast.LENGTH_SHORT).show();
        // Implement account management here
    }

    // Fetch lights from API
    private void fetchLights() {
        Call<List<LightService.LightResponse>> call = lightService.getLights();
        call.enqueue(new Callback<List<LightService.LightResponse>>() {
            @Override
            public void onResponse(Call<List<LightService.LightResponse>> call, Response<List<LightService.LightResponse>> response) {
                if (response.isSuccessful()) {
                    lights = response.body();
                    loadListView();
                } else {
                    Toast.makeText(LightingControlActivity.this, "Failed to load lights", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LightService.LightResponse>> call, Throwable t) {
                Toast.makeText(LightingControlActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load lights into ListView
    private void loadListView() {
        if (lights != null && listView != null) {
            List<String> lightNames = new ArrayList<>();
            for (LightService.LightResponse light : lights) {
                lightNames.add("• " + light.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lightNames);
            listView.setAdapter(adapter);
        }
    }

    // Handle light item click
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (lights != null && position < lights.size()) {
            LightService.LightResponse light = lights.get(position);
            Intent intent = new Intent(this, SpecificLightActivity.class);
            intent.putExtra("lightId", light.getId());
            intent.putExtra("lightName", light.getName());
            startActivity(intent);
        }
    }
}