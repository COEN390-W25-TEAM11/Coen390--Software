package com.example.lightingcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MotionLogsActivity extends AppCompatActivity {
    private Handler handler = new Handler(); // Handler for repeating task
    private int counter = 1; // Counter to track items
    private ArrayList<String> sensorOutput;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private TextView textView;
    private final OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_motion_logs);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Fetch ESP32 data
        fetchESP32Data();

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Motion Logs");

        listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView);
        sensorOutput = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, sensorOutput);
        listView.setAdapter(adapter);
        adapter.clear();
        // Start updating ListView every 1 second
        handler.postDelayed(updateListRunnable, 1000);
    }

    // Runnable to update ListView every second
    private Runnable updateListRunnable = new Runnable() {
        @Override
        public void run() {
            fetchESP32Data();
            handler.postDelayed(this, 1000); // Schedule next update in 1 second
        }
    };
    private void fetchESP32Data() {
        String esp32Url = "http://10.0.0.102/";  // Replace with your ESP32 IP

        Request request = new Request.Builder().url(esp32Url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> textView.setText("Failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                runOnUiThread(() -> {
                    sensorOutput.add(responseData);  // Add motion status
                    adapter.notifyDataSetChanged();  // Refresh ListView
                    textView.setText("Esp32 is connected!");  // Update TextView with motion status
                });
            }
        });
    }

    // show the toolbar
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.topAppBarDashboard, menu);
//        return true;
//    }

    // functionalities for toolbar button (return)
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.backtomain) {
//            Intent lightIntent = new Intent(this, LightingControlActivity.class);
//            startActivity(lightIntent);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}