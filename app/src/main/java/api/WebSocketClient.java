package api;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClient {

    private WebSocket webSocket;
    private OkHttpClient client;
    private WebSocketListener listener;
    private final String URL = "wss://a218-76-71-164-56.ngrok-free.app/Notification/ws"; // wss://[ngrok link]/Notification/ws
    String token;

    public WebSocketClient(WebSocketListener listener, String token) {
        this.listener = listener;
        this.token = token;
    }

    public void connectWebSocket() {
        client = new OkHttpClient.Builder()
                //.pingInterval(30, TimeUnit.SECONDS) // Keep connection alive
                .build();

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", "Bearer " + token) // Send JWT token
                .build();

        webSocket = client.newWebSocket(request, listener);
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        } else {
            Log.e("WebSocketClient", "WebSocket is not connected.");
        }
    }

    public void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
        }
    }

    public static abstract class MyWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d("WebSocket", "Connection opened");
            // Handle connection open event
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d("WebSocket", "Received message: " + text);
            // Handle text message
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.d("WebSocket", "Received bytes message: " + bytes.hex());
            // Handle byte message
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.d("WebSocket", "Closing connection: " + code + " " + reason);
            // Handle connection closing
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.d("WebSocket", "Connection closed: " + code + " " + reason);
            // Handle connection closed
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.e("WebSocket", "Connection failure: " + t.getMessage());
            t.printStackTrace();
            // Handle connection failure
        }
    }
}