package api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory; // to convert JSON response to java objects

public class RetrofitClient {
    private static final String URL = "https://c683-138-229-30-132.ngrok-free.app"; // connect to ngrok address

    private static Retrofit unauthenticatedRetrofit; // retrofit instance for login, when we don't have JWT token yet
    private static Retrofit retrofit; // retrofit instance for all other API requests

    public static Retrofit getUnauthenticatedRetrofit() {
        if (unauthenticatedRetrofit == null) {
            unauthenticatedRetrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return unauthenticatedRetrofit;
    }

    public static Retrofit getRetrofit(String token) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(token))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        } else {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(token))
                    .build();
            retrofit = retrofit.newBuilder().client(client).build();
        }
        return retrofit;
    }
}
