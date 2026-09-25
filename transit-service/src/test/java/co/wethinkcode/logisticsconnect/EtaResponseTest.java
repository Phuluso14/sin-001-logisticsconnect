package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EtaResponseTest {

    @Test
    void calculatesEtaForNoDelay() {
        assertEquals(60, calculateEta(0));
    }

    @Test
    void calculatesEtaForStageFive() {
        assertEquals(210, calculateEta(5));
    }

    @Test
    void storesEtaResponseData() {
        EtaResponse response = new EtaResponse(
                "JHB01", "Johannesburg Central", 5, 210
        );

        assertEquals("JHB01", response.getHubId());
        assertEquals("Johannesburg Central", response.getSortingCenter());
        assertEquals(5, response.getDelayStage());
        assertEquals(210, response.getEstimatedArrivalMinutes());
    }

    private int calculateEta(int stage) {
        return 60 + stage * 30;
    }
}
