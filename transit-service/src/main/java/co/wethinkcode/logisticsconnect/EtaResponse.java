package co.wethinkcode.logisticsconnect;

public class EtaResponse {
    private String hubId;
    private String sortingCenter;
    private int delayStage;
    private int estimatedArrivalMinutes;

    public EtaResponse() {
    }

    public EtaResponse(String hubId, String sortingCenter, int delayStage, int estimatedArrivalMinutes) {
        this.hubId = hubId;
        this.sortingCenter = sortingCenter;
        this.delayStage = delayStage;
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }

    public String getHubId() {
        return hubId;
    }

    public String getSortingCenter() {
        return sortingCenter;
    }

    public int getDelayStage() {
        return delayStage;
    }

    public int getEstimatedArrivalMinutes() {
        return estimatedArrivalMinutes;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public void setSortingCenter(String sortingCenter) {
        this.sortingCenter = sortingCenter;
    }

    public void setDelayStage(int delayStage) {
        this.delayStage = delayStage;
    }

    public void setEstimatedArrivalMinutes(int estimatedArrivalMinutes) {
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }
}
