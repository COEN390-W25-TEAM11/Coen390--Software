package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;
import java.util.UUID;

import api.LightService;
import api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SpecificLightActivity extends AppCompatActivity {

    String currentLightId, currentLightName;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private LightService lightService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_light);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");

        TextView lightName = findViewById(R.id.lightName);
        TextView lightInformation = findViewById(R.id.lightInformation);

        // get the data that was passed
        Intent intent = getIntent();
        currentLightId = intent.getStringExtra("lightId");
        currentLightName = intent.getStringExtra("lightName");

        lightName.setText("Selected light: " + currentLightName);

        // get JWT token
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        String token = sharedPreferencesHelper.getToken();

        // Initialize Retrofit
        Retrofit retrofit = RetrofitClient.getRetrofit(token);
        lightService = retrofit.create(LightService.class);

        // display light information
        displayLightInformation();
    }

    private void displayLightInformation() {
        UUID lightId = UUID.fromString(currentLightId); // Convert string id to UUID
        Log.d("UUID: ", String.valueOf(lightId));

        Call<LightService.LightResponse> call = lightService.getLightById(lightId);
        call.enqueue(new Callback<LightService.LightResponse>() {
            @Override
            public void onResponse(Call<LightService.LightResponse> call, Response<LightService.LightResponse> response) {
                if (response.isSuccessful()) {
                    LightService.LightResponse lightResponse = response.body();
                    if (lightResponse != null) {
                        // show info
                        TextView lightInformation = findViewById(R.id.lightInformation);
                        lightInformation.setText("Current state: " + String.valueOf(lightResponse.getState())); // Convert int to String

                    } else {
                        Toast.makeText(SpecificLightActivity.this, "Light data is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SpecificLightActivity.this, "Error fetching light: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LightService.LightResponse> call, Throwable t) {
                Toast.makeText(SpecificLightActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
