package api;

import com.google.gson.Gson;

import java.util.function.Consumer;

public class MotionNotificationClient {

    private WebSocketClient webSocketClient;
    private Consumer<LightService.GetResponse.MotionResponse> newMovementListener;

    private final String sensorId;

    public void setNewMovementListener(Consumer<LightService.GetResponse.MotionResponse> newMovementListener) {
        this.newMovementListener = newMovementListener;
    }

    public MotionNotificationClient(String token, String sensorId) {
        webSocketClient = new WebSocketClient(token);
        webSocketClient.setOnMessageListener(this::messageHandler);
        this.sensorId = sensorId;
    }

    private void messageHandler(String message) {
        MessageModel messageModel = new Gson().fromJson(message, MessageModel.class);

        if (messageModel.sensorId.equalsIgnoreCase(sensorId) && messageModel.motion) {
            var motionResponse = new LightService.GetResponse.MotionResponse(messageModel.dateTime, true);
            newMovementListener.accept(motionResponse);
        }
    }

    public void stop() {
        webSocketClient.stop();
    }

    private static class MessageModel {
        public String sensorId;
        public String dateTime;
        public boolean motion;
    }
}
