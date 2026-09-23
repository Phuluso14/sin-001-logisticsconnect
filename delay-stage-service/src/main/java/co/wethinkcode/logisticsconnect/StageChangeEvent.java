package co.wethinkcode.logisticsconnect;

public class StageChangeEvent {
    private String hubId;
    private int stage;
    private String timestamp;

    public StageChangeEvent(String hubId, int stage, String timestamp) {
        this.hubId = hubId;
        this.stage = stage;
        this.timestamp = timestamp;
    }

    public String getHubId() {
        return hubId;
    }

    public int getStage() {
        return stage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}