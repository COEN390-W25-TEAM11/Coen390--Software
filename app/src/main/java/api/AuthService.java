package api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    // Endpoint for logging in
    @POST("Auth/login")
    Call<LoginResponse> login(@Body UserLogin user);

    // Endpoint for registering
    @POST("Auth/register")
    Call<Void> register(@Body UserLogin user);


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
}
