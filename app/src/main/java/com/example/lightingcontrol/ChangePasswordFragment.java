package com.example.lightingcontrol;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ChangePasswordFragment extends DialogFragment {

    public interface PasswordChangeListener {
        void onPasswordChanged(String newPassword);
    }

    private PasswordChangeListener listener;

    public void setPasswordChangeListener(PasswordChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        EditText newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button saveButton = view.findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            // Validate empty fields
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password length (4-8 characters)
            if (newPassword.length() < 4 || newPassword.length() > 8) {
                Toast.makeText(getContext(), "Password must be 4-8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password match
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            // All validations passed
            if (listener != null) {
                listener.onPasswordChanged(newPassword);
                Toast.makeText(getContext(), "Password saved successfully", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        return view;
    }

    public static ChangePasswordFragment newInstance() {
        return new ChangePasswordFragment();
    }
}