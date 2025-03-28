package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import api.AuthService;
import api.RetrofitClient;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // define shared preferences
    protected SharedPreferencesHelper sharedPreferenceHelper;

    // define elements
    private EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize Toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar1);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lighting Control");

        // initialize UI elements
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.login_button);

        // initialize shared preferences
        sharedPreferenceHelper = new SharedPreferencesHelper(MainActivity.this);

        // initialize retrofit and authservice
        Retrofit retrofit = RetrofitClient.getUnauthenticatedRetrofit();
        AuthService authService = retrofit.create(AuthService.class);

        // button functionality
        loginBtn.setOnClickListener(v -> {

            String userUsername = username.getText().toString();
            String userPassword = password.getText().toString();

            // call login API
            AuthService.UserLogin userLogin = new AuthService.UserLogin(userUsername, userPassword);
            Call<AuthService.LoginResponse> call = authService.login(userLogin);

            call.enqueue(new Callback<AuthService.LoginResponse>() {
                @Override
                public void onResponse(Call<AuthService.LoginResponse> call, Response<AuthService.LoginResponse> response) {
                    if (response.isSuccessful() && !isFinishing() && !isDestroyed()) {
                        AuthService.LoginResponse loginResponse = response.body();
                        String token = loginResponse.getToken();
                        sharedPreferenceHelper.saveToken(token); // save JWT token
                        Intent intent = new Intent(MainActivity.this, LightingControlActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Login failed: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthService.LoginResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
