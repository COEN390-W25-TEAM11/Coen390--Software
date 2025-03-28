package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.auth0.android.jwt.JWT;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import api.LightService;
import api.RetrofitClient;
import api.WebSocketClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LightingControlActivity extends AppCompatActivity implements CreateLightFragment.RefreshAfterSave, AdapterView.OnItemClickListener {

    protected SharedPreferencesHelper sharedPreferencesHelper;
    private LightService lightService;
    private List<LightService.LightResponse> lights;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_control);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lighting Control");

        // set up UI elements
        Button motionLogsBtn = findViewById(R.id.motionLogsButton);
        Button newLightBtn = findViewById(R.id.newLightButton);
        TextView helloUser = findViewById(R.id.helloUser);
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(this); // make clickable

        // get JWT token
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        String token = sharedPreferencesHelper.getToken();

        // initialize retrofit and lightservice
        Retrofit retrofit = RetrofitClient.getRetrofit(token);
        lightService = retrofit.create(LightService.class);

        // setup hello user header
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

        // fetch lights from API endpoint
        fetchLights();

        // when new light button is clicked, open create light fragment
        newLightBtn.setOnClickListener(v -> {
            CreateLightFragment createLightFragment = new CreateLightFragment();
            createLightFragment.setRefreshAfterSave(LightingControlActivity.this);
            createLightFragment.show(getSupportFragmentManager(), "createLightFragment");
        });

        // when motion logs button is clicked, open motion logs activity
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

    // make request to endpoint to get lights
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

    // update UI, reload listview with any added light(s)
    private void loadListView() {
        if (lights != null && listView != null) {
            List<String> lightNames = new ArrayList<>();
            for (LightService.LightResponse light : lights) {
                lightNames.add(light.getName() + " (ID: " + light.getId() + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lightNames);
            listView.setAdapter(adapter);
        } else {
            Toast.makeText(LightingControlActivity.this, "Failed to load lights", Toast.LENGTH_SHORT).show();
        }
    }

    // re fetch the lights
    @Override
    public void refreshList() {
        fetchLights();
    }

    // functionality for when an entry in the listView is clicked
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent specificLightIntent = new Intent(this, SpecificLightActivity.class);

        // save the light Id of the entry that needs to be opened
        String clickedLight = (String) parent.getItemAtPosition(position);
        String lightIdToSend = clickedLight.substring(clickedLight.indexOf(":") + 2, clickedLight.indexOf(")")); // get ID, in between colon and parentheses
        String lightNameToSend = clickedLight.substring(0, clickedLight.indexOf("("));

        Log.d("lightToSend: ", lightIdToSend);
        Log.d("lightNameToSend: ", lightNameToSend);

        // pass light to SpecificLightActivity
        if (lightIdToSend != null && lightNameToSend != null) {
            specificLightIntent.putExtra("lightId", lightIdToSend);
            specificLightIntent.putExtra("lightName", lightNameToSend);

            // go to SpecificLightActivity
            startActivity(specificLightIntent);
        }
    }
}
