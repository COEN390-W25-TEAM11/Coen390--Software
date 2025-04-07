package com.example.lightingcontrol.account;

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

import com.example.lightingcontrol.R;
import com.example.lightingcontrol.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Arrays;
import java.util.List;

import api.AuthService;
import api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AccountActivity extends AppCompatActivity {

    private SharedPreferencesHelper sharedPreferencesHelper;
    private Retrofit retrofit;
    private ImageButton togglePasswordButton;
    private ListView accountsListView;
    private MaterialButton changePasswordButton;

    // Data
    private List<AuthService.UserItem> accountList;
    private AccountsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        retrofit = RetrofitClient.getRetrofit(sharedPreferencesHelper.getToken());

        initializeToolbar();
        initializeUsernameText();
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

    private void initializeUsernameText() {
        TextView usernameText = findViewById(R.id.usernameText);
        usernameText.setText(sharedPreferencesHelper.getUsername());
    }

    private void initializeChangePasswordButton() {
        changePasswordButton = findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(v -> {
            // Show the black dialog when button is pressed
            var changePasswordFragment = ChangePasswordFragment.newInstance();

            changePasswordFragment.setPasswordChangeListener(new ChangePasswordFragment.PasswordChangeListener() {
                @Override
                public void onPasswordChanged(String newPassword) {
                    AuthService authService = retrofit.create(AuthService.class);
                    AuthService.ChangePasswordRequest request = new AuthService.ChangePasswordRequest(newPassword);
                    Call<Void> call = authService.changePassword(request);

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AccountActivity.this, "Change password successful!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AccountActivity.this, "Could not change password!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(AccountActivity.this, "Could not change password!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            changePasswordFragment.show(getSupportFragmentManager(), "ChangePasswordDialog");
        });
    }

    private void initializeAccountsList() {
        accountsListView = findViewById(R.id.accountsListView);

        AuthService authService = retrofit.create(AuthService.class);
        Call<AuthService.UserItem[]> call = authService.listUsers();

        call.enqueue(new Callback<AuthService.UserItem[]>() {
            @Override
            public void onResponse(Call<AuthService.UserItem[]> call, Response<AuthService.UserItem[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accountList = Arrays.asList(response.body());

                    adapter = new AccountsAdapter();
                    accountsListView.setAdapter(adapter);
                } else {
                    Toast.makeText(AccountActivity.this, "Could not load user list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthService.UserItem[]> call, Throwable t) {
                Toast.makeText(AccountActivity.this, "Could not load user list", Toast.LENGTH_SHORT).show();
            }
        });
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
        public AuthService.UserItem getItem(int position) {
            return accountList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
//            return getItem(position).userId;
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

            final AuthService.UserItem account = accountList.get(position);

            holder.accountName.setText((String) account.username);
            holder.accountSwitch.setChecked((Boolean) account.isEnabled);

            // Remove previous listener to avoid recycling issues
            holder.accountSwitch.setOnCheckedChangeListener(null);

            // Set new listener
            holder.accountSwitch.setEnabled(!account.username.equals(sharedPreferencesHelper.getUsername()));
            holder.accountSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                account.isEnabled = isChecked;

                AuthService authService = retrofit.create(AuthService.class);
                Call<Void> call = authService.modifyUser(new AuthService.ModifyUser(account.userId, account.isEnabled, account.isAdmin));

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(AccountActivity.this, "Could edit user permissions", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(AccountActivity.this, "Could edit user permissions", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            return convertView;
        }

        class ViewHolder {
            TextView accountName;
            SwitchMaterial accountSwitch;
        }
    }
}