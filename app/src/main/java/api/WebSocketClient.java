package api;

import android.util.Log;

import androidx.annotation.NonNull;


import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient {

    private WebSocket webSocket;
    private OkHttpClient client;

    private final String URL = "wss://58e5-66-130-27-237.ngrok-free.app/Notification/ws"; // wss://[ngrok link]/Notification/ws
    private final String token;

    private Consumer<String> messageListener;

    public WebSocketClient(String token) {
        this.token = token;

        connectWebSocket();
    }

    private void connectWebSocket() {
        client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                messageListener.accept(text);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                Log.e("WebSocket", "Connection failed", t);
            }
        });
    }

    public void stop() {
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
    }

    public void setOnMessageListener(Consumer<String> listener) {
        this.messageListener = listener;
    }
}