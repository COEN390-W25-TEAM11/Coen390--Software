package com.example.lightingcontrol;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class FaqActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_faq);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize MaterialToolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBarFaq);
        setSupportActionBar(toolbar);

        // Configure toolbar with white back arrow and title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIconTint(getResources().getColor(android.R.color.white));
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleAnswerVisibility(View view) {
        // Get the TextView that represents the answer
        TextView answer = null;

        if (view.getId() == R.id.q1) {
            answer = findViewById(R.id.a1);
        } else if (view.getId() == R.id.q2) {
            answer = findViewById(R.id.a2);
        } else if (view.getId() == R.id.q3) {
            answer = findViewById(R.id.a3);
        } else if (view.getId() == R.id.q4) {
            answer = findViewById(R.id.a4);
        } else if (view.getId() == R.id.q5) {
            answer = findViewById(R.id.a5);
        } else if (view.getId() == R.id.q6) {
            answer = findViewById(R.id.a6);
        } else if (view.getId() == R.id.q7) {
            answer = findViewById(R.id.a7);
        } else if (view.getId() == R.id.q8) {
            answer = findViewById(R.id.a8);
        } else if (view.getId() == R.id.q9) {
            answer = findViewById(R.id.a9);
        }

        // Toggle the visibility of the answer
        if (answer != null) {
            if (answer.getVisibility() == View.GONE) {
                answer.setVisibility(View.VISIBLE);
            } else {
                answer.setVisibility(View.GONE);
            }
        }
    }
}