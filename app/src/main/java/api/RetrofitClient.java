package api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory; // to convert JSON response to java objects

public class RetrofitClient {
    private static final String PC_IP = "https://f4ad-138-229-30-132.ngrok-free.app"; // connect to ngrok address
    private static Retrofit retrofit; // retrofit instance

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(PC_IP)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
