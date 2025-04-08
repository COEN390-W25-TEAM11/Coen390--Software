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
import com.example.lightingcontrol.account.AccountActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

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
    private ListView listView;

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
        listView = findViewById(R.id.listView);
//        listView.setOnItemClickListener(this);

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
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"Android", "IPhone", "WindowsMobile", "Blackberry",
                "WebOS", "Ubuntu", "Windows7", "Max OS X"});

        listView.setAdapter(adapter);
    }

    private void onAddLightClick() {
        CreateLightFragment createLightFragment = new CreateLightFragment();
        createLightFragment.setRefreshAfterSave(this::loadData);
        createLightFragment.show(getSupportFragmentManager(), "createLightFragment");
    }

    private void onMyAccountClick() {
        // Navigate to AccountActivity
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
    }
}