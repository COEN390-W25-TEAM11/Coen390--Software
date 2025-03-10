package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Toolbar (ensure your layout has a Toolbar with id "toolbar")
        Toolbar myToolbar = findViewById(R.id.my_toolbar1);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Lighting Control");

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.login_button);

        loginBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString();
            String userPassword = password.getText().toString();

            if (userEmail.equals("admin") && userPassword.equals("1234")) {
                Intent intent = new Intent(MainActivity.this, LightingControlActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
