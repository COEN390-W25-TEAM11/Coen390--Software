package api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthService {

    // Endpoint for logging in
    @POST("Auth/login")
    Call<LoginResponse> login(@Body UserLogin user);

    // Endpoint for registering
    @POST("Auth/register")
    Call<Void> register(@Body UserLogin user);

    @POST("Auth/change-password")
    Call<Void> changePassword(@Body ChangePasswordRequest request);

    @GET("Auth/list-users")
    Call<UserItem[]> listUsers();

    @POST("Auth/modify-user")
    Call<Void> modifyUser(@Body ModifyUser request);

    // Response class for login/registration
    class LoginResponse {
        private String token; // JWT token

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    // Request class for login/registration
    class UserLogin {
        private String Username;
        private String Password;

        public UserLogin(String username, String password) {
            this.Username = username;
            this.Password = password;
        }

        public String getUsername() {
            return Username;
        }

        public String getPassword() {
            return Password;
        }
    }

    class ChangePasswordRequest {
        private String newPassword;

        public ChangePasswordRequest(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    class UserItem {
        public String userId;
        public String username;
        public boolean isEnabled;
        public boolean isAdmin;
    }

    class ModifyUser {
        public String userId;
        public boolean isEnabled;
        public boolean isAdmin;

        public ModifyUser(String userId, boolean isEnabled, boolean isAdmin) {
            this.userId = userId;
            this.isEnabled = isEnabled;
            this.isAdmin = isAdmin;
        }
    }
}
