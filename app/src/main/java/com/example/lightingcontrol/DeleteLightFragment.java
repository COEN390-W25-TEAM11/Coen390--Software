package com.example.lightingcontrol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DeleteLightFragment extends DialogFragment {

    private static final String ARG_LIGHT_NAME = "light_name";

    public interface DeleteLightListener {
        void onDeleteConfirmed();
    }

    private DeleteLightListener listener;

    public static DeleteLightFragment newInstance(String lightName) {
        DeleteLightFragment fragment = new DeleteLightFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LIGHT_NAME, lightName);
        fragment.setArguments(args);
        return fragment;
    }

    public void setDeleteLightListener(DeleteLightListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String lightName = getArguments() != null ?
                getArguments().getString(ARG_LIGHT_NAME, "") : "";

        return new AlertDialog.Builder(requireContext())
                .setTitle("Delete Light")
                .setMessage(String.format(
                        "Are you sure you want to permanently delete '%s'?\n\n" +
                                "This action cannot be undone and all associated data will be lost.",
                        lightName))
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (listener != null) {
                        listener.onDeleteConfirmed();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}