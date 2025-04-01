package api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    // Endpoint for logging in
    @POST("Auth/login")
    Call<AuthService.LoginResponse> login(@Body UserLogin user);

    // Endpoint for registering (NOT IMPLEMENTED YET)
    @POST("Auth/register")
    Call<AuthService.LoginResponse> register(@Body UserLogin user);

    // UserLogin class
    class UserLogin {
        private String username;
        private String password;

        public UserLogin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // Response to a user login
    class LoginResponse {
        private String token; // JWT token

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
