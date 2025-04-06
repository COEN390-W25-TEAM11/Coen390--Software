package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateUser extends AppCompatActivity {

    private EditText regUsername, regPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        // Initialize views
        regUsername = findViewById(R.id.reg_username);
        regPassword = findViewById(R.id.reg_password);
        Button saveButton = findViewById(R.id.save_button);

        // Save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input values
                String username = regUsername.getText().toString();
                String password = regPassword.getText().toString();

                // Validate inputs
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(CreateUser.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Here you would typically:
                // 1. Call your API to register the user
                // 2. Save the user data locally if needed
                // 3. Navigate back to login

                // For now, we'll just show a toast and go back to login
                Toast.makeText(CreateUser.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                // Navigate back to login activity
                Intent intent = new Intent(CreateUser.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            }
        });
    }
}