package com.example.lightingcontrol.account;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lightingcontrol.R;
import com.example.lightingcontrol.helpers.SharedPreferencesHelper;
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

@SuppressLint("SetTextI18n")
public class AccountActivity extends AppCompatActivity {

    private SharedPreferencesHelper sharedPreferencesHelper;
    private Retrofit retrofit;
    private ImageButton togglePasswordButton;

    // Data
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
        MaterialButton changePasswordButton = findViewById(R.id.changePasswordButton);
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
        TextView connectedAccountSubtitle = findViewById(R.id.connectedAccountSubtitle);

        AuthService authService = retrofit.create(AuthService.class);
        Call<AuthService.UserItem[]> call = authService.listUsers();

        call.enqueue(new Callback<AuthService.UserItem[]>() {
            @Override
            public void onResponse(Call<AuthService.UserItem[]> call, Response<AuthService.UserItem[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var accountList = Arrays.asList(response.body());

                    adapter = new AccountsAdapter(AccountActivity.this, accountList);

                    ListView accountsListView = findViewById(R.id.accountsListView);
                    accountsListView.setAdapter(adapter);
                } else if (response.code() == 403) {
                    connectedAccountSubtitle.setText("You do not have administrative permissions to view connected accounts");
                } else {
                    connectedAccountSubtitle.setText("Could not load user list");
                }
            }

            @Override
            public void onFailure(Call<AuthService.UserItem[]> call, Throwable t) {
                connectedAccountSubtitle.setText("Could not load user list");
            }
        });
    }

    private void displayAccountList() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class AccountsAdapter extends ArrayAdapter<AuthService.UserItem> {
        private final Context context;
        private final List<AuthService.UserItem> accountList;

        public AccountsAdapter(Context context, List<AuthService.UserItem> accountList) {
            super(context, 0, accountList);
            this.context = context;
            this.accountList = accountList;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            final AuthService.UserItem account = accountList.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.account_list_item, parent, false);
            }

            TextView accountName = convertView.findViewById(R.id.accountName);
            SwitchMaterial userSwitch = convertView.findViewById(R.id.accountSwitch);

            // Remove previous listener to avoid recycling issues
            userSwitch.setOnCheckedChangeListener(null);

            accountName.setText((String) account.username);
            userSwitch.setChecked((Boolean) account.isEnabled);

            userSwitch.setEnabled(!account.username.equals(sharedPreferencesHelper.getUsername()));

            userSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                account.isEnabled = isChecked;

                AuthService authService = retrofit.create(AuthService.class);
                Call<Void> call = authService.modifyUser(new AuthService.ModifyUser(account.userId, account.isEnabled, account.isAdmin));

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(AccountActivity.this, "Could not edit user permissions", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(AccountActivity.this, "Could not edit user permissions", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            return convertView;
        }
    }
}