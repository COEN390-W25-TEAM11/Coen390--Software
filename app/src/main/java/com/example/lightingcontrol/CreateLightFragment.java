package com.example.lightingcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import api.LightService;
import api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import java.util.UUID;

public class CreateLightFragment extends DialogFragment {

    public interface RefreshAfterSave {
        void refreshList();
    }

    private RefreshAfterSave refreshAfterSave;

    public void setRefreshAfterSave(RefreshAfterSave listener) {
        this.refreshAfterSave = listener;
    }

    // define UI elements
    protected EditText editName;
    protected Button cancelButton, saveButton;
    protected SharedPreferencesHelper sharedPreferencesHelper;
    LightService lightService;

    public CreateLightFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_light, container, false);

        // setup UI elements
        editName = view.findViewById(R.id.editName);
        cancelButton = view.findViewById(R.id.cancelButton); // Initialize cancelButton
        saveButton = view.findViewById(R.id.saveButton); // Initialize saveButton

        // Get the auth token from shared preferences
        sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        Retrofit retrofit = RetrofitClient.getRetrofit(sharedPreferencesHelper.getToken());
        lightService = retrofit.create(LightService.class);

        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "A name must be chosen", Toast.LENGTH_SHORT).show();
                return;
            }

            createNewLight(name);
        });

        return view;
    }

    private void createNewLight(String name) {
        LightService.Light newLight = new LightService.Light();
        UUID uuid = UUID.randomUUID(); // Generate a random UUID
        String uuidString = uuid.toString();

        newLight.setId(uuidString);
        newLight.setName(name);
        newLight.setOveride(false); // default
        newLight.setState(0); // default

        Call<Void> call = lightService.postLight(newLight);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "New light created successfully", Toast.LENGTH_SHORT).show();
                    if (refreshAfterSave != null) {
                        refreshAfterSave.refreshList();
                    }
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Failed to create new light: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}