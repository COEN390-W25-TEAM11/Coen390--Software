package api;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LightService {

    @GET("/Api")
    Call<GetResponse> get();

    class GetResponse {
        public LightResponse[] lights;
        public SensorResponse[] sensors;
        public CombinationResponse[] combinations;


        public static class LightResponse {
            public String id;
            public String name;
        }

        public static class SensorResponse {
            public String id;
            public String name;
            public MotionResponse[] motion;
        }

        public static class MotionResponse {

        }

        public static class CombinationResponse {
            public String id;
            public String lightId;
            public String sensorId;
        }
    }

    @PATCH("/Api/light/{lightId")
    Call<Void> updateLight(@Path("lightId") String lightId, @Body UpdateLightModel model);

    class UpdateLightModel {
        public String name;
        public boolean overide;
        public int state;

        public UpdateLightModel(String name, boolean overide, int state) {
            this.name = name;
            this.overide = overide;
            this.state = state;
        }
    }

    @DELETE("/Api/light/{lightId")
    Call<Void> deleteLight(@Path("lightId") String lightId);

    @PATCH("/Api/sensor/{sensorId")
    Call<Void> updateSensor(@Path("sensorId") String sensorId, @Body UpdateSensorModel model);

    class UpdateSensorModel {
        public String name;
        public int sensitivity;
        public int timeout;

        public UpdateSensorModel(String name, int sensitivity, int timeout) {
            this.name = name;
            this.sensitivity = sensitivity;
            this.timeout = timeout;
        }
    }

    @DELETE("/Api/sensor/{sensorId")
    Call<Void> deleteSensor(@Path("sensorId") String sensorId);

    @POST("Api/assigned")
    Call<Void> assignLightSensor(@Body AssignLightSensorModel model);

    class AssignLightSensorModel {
        public String lightId;
        public String sensorId;

        public AssignLightSensorModel(String lightId, String sensorId) {
            this.lightId = lightId;
            this.sensorId = sensorId;
        }
    }

    @DELETE("Api/assigned/{id}")
    Call<Void> deleteAssign(@Path("id") String id);


    @Deprecated
    @GET("Light")
    Call<List<LightResponse>> getLights();

    @Deprecated
    @GET("Light/{lightId}") // Use the correct route with lightId
    Call<LightResponse> getLightById(@Path("lightId") UUID lightId);

    @Deprecated
    @POST("Light")
    Call<Void> postLight(@Body Light light);

    @Deprecated
    @PATCH("Light/{lightId}")
    Call<Void> patchLight(@Path("lightId") String lightId, @Body LightUpdateDto lightUpdateDto);

    @Deprecated
    @GET("Light/{lightId}/MotionHistory")
    Call<List<MotionHistory>> getMotionByLight(@Path("lightId") UUID lightId);

    @Deprecated
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


    @Deprecated
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


    @Deprecated
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


    @Deprecated
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
