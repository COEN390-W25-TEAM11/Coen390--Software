package com.example.lightingcontrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.lightingcontrol.helpers.SharedPreferencesHelper;

import api.LightService;
import api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.Arrays;

public class CreateLightFragment extends DialogFragment {

    // Services and helpers
    private SharedPreferencesHelper sharedPreferencesHelper;
    private LightService lightService;

    // UI elements
    private Spinner lightDropdown, sensorDropdown;
    private Button cancelButton, saveButton;

    // Callback
    private Runnable refreshCallback;

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    // Data
    private LightService.GetResponse data;

    public void setData(LightService.GetResponse data) {
        this.data = data;
        this.selectedLightId = data.lights[0].id;
        this.selectedSensorId = data.sensors[0].id;
    }

    private String selectedLightId;
    private String selectedSensorId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_light, container, false);

        // Initialize UI elements
        lightDropdown = view.findViewById(R.id.light_spinner);
        sensorDropdown = view.findViewById(R.id.sensor_spinner);
        cancelButton = view.findViewById(R.id.cancelButton);
        saveButton = view.findViewById(R.id.saveButton);

        // Initialize services
        sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());
        Retrofit retrofit = RetrofitClient.getRetrofit(sharedPreferencesHelper.getToken());
        lightService = retrofit.create(LightService.class);

        // Bind UI elements
        lightDropdown.setAdapter(new ArrayAdapter<LightService.GetResponse.LightResponse>(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, Arrays.asList(data.lights)));
        sensorDropdown.setAdapter(new ArrayAdapter<LightService.GetResponse.SensorResponse>(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, Arrays.asList(data.sensors)));

        lightDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                var item = (LightService.GetResponse.LightResponse) parent.getSelectedItem();
                selectedLightId = item.id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sensorDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                var item = (LightService.GetResponse.SensorResponse) parent.getSelectedItem();
                selectedSensorId = item.id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());
        saveButton.setOnClickListener(v -> createComboLight());

        return view;
    }

    private void createComboLight() {
        var model = new LightService.AssignLightSensorModel(selectedLightId, selectedSensorId);
        Call<Void> call = lightService.assignLightSensor(model);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "New combo created successfully", Toast.LENGTH_SHORT).show();
                    refreshCallback.run();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Error when creating combo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error when creating combo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}