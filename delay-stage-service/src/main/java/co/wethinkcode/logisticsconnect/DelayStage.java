package co.wethinkcode.logisticsconnect;

public class DelayStage {
    private String hubId;
    private int stage;

    public DelayStage() {
    }

    public DelayStage(String hubId, int stage) {
        this.hubId = hubId;
        this.stage = stage;
    }

    public String getHubId() {
        return hubId;
    }

    public int getStage() {
        return stage;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
}
