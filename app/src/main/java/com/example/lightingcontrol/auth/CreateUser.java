package com.example.lightingcontrol.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lightingcontrol.R;

import api.AuthService;
import api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CreateUser extends AppCompatActivity {

    private EditText regUsername, regPassword;

    private ImageButton togglePasswordButton;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        // Initialize views
        regUsername = findViewById(R.id.reg_username);
        regPassword = findViewById(R.id.reg_password);
        Button saveButton = findViewById(R.id.save_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        // Save button click listener
        saveButton.setOnClickListener(v -> registerUser());

        // Cancel button click listener
        cancelButton.setOnClickListener(v -> returnToLogin());

        // Password visibility toggle
        togglePasswordButton = findViewById(R.id.togglePasswordButton);
        togglePasswordButton.setOnClickListener(v -> {
            if (isPasswordVisible) {
                regPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePasswordButton.setImageResource(R.drawable.ic_visibility_off);
            } else {
                regPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePasswordButton.setImageResource(R.drawable.ic_visibility);
            }
            isPasswordVisible = !isPasswordVisible;
            regPassword.setSelection(regPassword.getText().length());
        });
    }

    private void registerUser() {
        String username = regUsername.getText().toString().trim();
        String password = regPassword.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(CreateUser.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password length
        if (password.length() < 4 || password.length() > 8) {
            Toast.makeText(CreateUser.this, "Password must be 4-8 characters", Toast.LENGTH_SHORT).show();
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
                    returnToLogin();
                } else {
                    String errorMessage = "Registration failed";
                    if (response.code() == 400) {
                        errorMessage = "Username already exists";
                    }
                    Toast.makeText(CreateUser.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreateUser.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnToLogin() {
        startActivity(new Intent(CreateUser.this, MainActivity.class));
        finish();
    }
}