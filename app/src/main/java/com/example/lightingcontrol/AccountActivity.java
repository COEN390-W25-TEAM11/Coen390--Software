package com.example.lightingcontrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AccountActivity extends AppCompatActivity {

    // UI Components
    private TextView passwordText;
    private ImageButton togglePasswordButton;
    private ListView accountsListView;
    private MaterialButton changePasswordButton;

    // Data
    private List<HashMap<String, Object>> accountList;
    private AccountsAdapter adapter;
    private boolean isPasswordVisible = false;
    private String currentPassword = "toor"; // Default password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initializeToolbar();
        initializePasswordViews();
        initializeChangePasswordButton();
        initializeAccountsList();
    }

    private void initializeToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBarAccount);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIconTint(getResources().getColor(android.R.color.white));
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private void initializePasswordViews() {
        passwordText = findViewById(R.id.passwordText);
        togglePasswordButton = findViewById(R.id.togglePasswordButton);
        togglePasswordButton.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void initializeChangePasswordButton() {
        changePasswordButton = findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(v -> {
            // Show the black dialog when button is pressed
            ChangePasswordFragment.newInstance()
                    .show(getSupportFragmentManager(), "ChangePasswordDialog");
        });
    }

    private void initializeAccountsList() {
        accountsListView = findViewById(R.id.accountsListView);
        accountList = new ArrayList<>();

        // Add 5 child accounts
        for (int i = 1; i <= 5; i++) {
            HashMap<String, Object> account = new HashMap<>();
            account.put("name", "ChildAccount" + i);
            account.put("active", false);
            accountList.add(account);
        }

        adapter = new AccountsAdapter();
        accountsListView.setAdapter(adapter);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Create asterisks matching password length
            StringBuilder masked = new StringBuilder();
            for (int i = 0; i < currentPassword.length(); i++) {
                masked.append("*");
            }
            passwordText.setText(masked.toString());
            togglePasswordButton.setImageResource(R.drawable.ic_visibility_off);
        } else {
            passwordText.setText(currentPassword);
            togglePasswordButton.setImageResource(R.drawable.ic_visibility);
        }
        isPasswordVisible = !isPasswordVisible;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class AccountsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return accountList.size();
        }

        @Override
        public Object getItem(int position) {
            return accountList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(AccountActivity.this)
                        .inflate(R.layout.account_list_item, parent, false);

                holder = new ViewHolder();
                holder.accountName = convertView.findViewById(R.id.accountName);
                holder.accountSwitch = convertView.findViewById(R.id.accountSwitch);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final HashMap<String, Object> account = accountList.get(position);
            holder.accountName.setText((String) account.get("name"));
            holder.accountSwitch.setChecked((Boolean) account.get("active"));

            // Remove previous listener to avoid recycling issues
            holder.accountSwitch.setOnCheckedChangeListener(null);

            // Set new listener
            holder.accountSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                account.put("active", isChecked);
                String status = isChecked ? "activated" : "deactivated";
                Toast.makeText(AccountActivity.this,
                        account.get("name") + " " + status,
                        Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }

        class ViewHolder {
            TextView accountName;
            SwitchMaterial accountSwitch;
        }
    }
}