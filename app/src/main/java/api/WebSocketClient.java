package api;

import android.util.Log;

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
    private final String URL = "ws://e335-138-229-30-132.ngrok-free.app";

    public WebSocketClient(WebSocketListener listener) {
        this.listener = listener;
    }

    public void connectWebSocket() {
        client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(URL)
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

    public static class MyWebSocketListener extends WebSocketListener {

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