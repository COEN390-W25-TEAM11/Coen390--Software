package api;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LightService {

    // Get all the lights in database with the last 50 motion history
    @GET("Light")
    Call<List<LightResponse>> getLights();

    // Get light object by Id
    @GET("Light/{lightId}") // Use the correct route with lightId
    Call<LightResponse> getLightById(@Path("lightId") UUID lightId);

    // Create a light to be stored in the database
    @POST("Light")
    Call<Void> postLight(@Body Light light);

    // Update the settings of a light
    @PATCH("Light/{lightId}")
    Call<Void> patchLight(@Path("lightId") String lightId, @Body LightUpdateDto lightUpdateDto);

    // Light object
    class Light {
        private String id, name;
        private boolean Overide;
        private int state;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isOveride() {
            return Overide;
        }

        public void setOveride(boolean overide) {
            this.Overide = overide;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }

    // Update light object
    class LightUpdateDto {
        public String name;
        public int state;
        public boolean overide;

        public LightUpdateDto(String name, int state, boolean overide) {
            this.name = name;
            this.state = state;
            this.overide = overide;
        }

    }

    // Motion history object
    class MotionHistory {
        private String id;
        private String dateTime;
        private boolean motion;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }

        public boolean isMotion() {
            return motion;
        }

        public void setMotion(boolean motion) {
            this.motion = motion;
        }
    }

    // Response to a light API
    class LightResponse {
        private String id;
        private String name;
        private boolean Overide;
        private int state;
        private List<MotionHistory> motionHistory;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isOveride() {
            return Overide;
        }

        public void setOveride(boolean overide) {
            this.Overide = overide;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public List<MotionHistory> getMotionHistory() {
            return motionHistory;
        }

        public void setMotionHistory(List<MotionHistory> motionHistory) {
            this.motionHistory = motionHistory;
        }
    }
}
