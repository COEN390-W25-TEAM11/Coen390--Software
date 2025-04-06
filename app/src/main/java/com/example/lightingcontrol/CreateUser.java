package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import api.AuthService;
import api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CreateUser extends AppCompatActivity {

    private EditText regUsername, regPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        // Set up the toolbar with a back arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Enable the "Up" button (back arrow)
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Register");
        }

        // Initialize views
        regUsername = findViewById(R.id.reg_username);
        regPassword = findViewById(R.id.reg_password);
        Button saveButton = findViewById(R.id.save_button);

        // Save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = regUsername.getText().toString().trim();
                String password = regPassword.getText().toString().trim();

                // Validate inputs
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(CreateUser.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Use Retrofit to register the user
                Retrofit retrofit = RetrofitClient.getUnauthenticatedRetrofit();
                AuthService authService = retrofit.create(AuthService.class);
                AuthService.UserLogin newUser = new AuthService.UserLogin(username, password);
                Call<Void> call = authService.register(newUser);

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CreateUser.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            // Navigate to login activity
                            Intent intent = new Intent(CreateUser.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CreateUser.this, "Registration failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(CreateUser.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Handle toolbar's Up (back arrow) button press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to login activity
            Intent intent = new Intent(CreateUser.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
