package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertBotTest {

    private static final int ALERT_THRESHOLD = 5;

    @Test
    void alertsAtStageFive() {
        assertTrue(shouldAlert(5));
    }

    @Test
    void alertsAboveStageFive() {
        assertTrue(shouldAlert(8));
    }

    @Test
    void doesNotAlertBelowStageFive() {
        assertFalse(shouldAlert(4));
    }

    private boolean shouldAlert(int stage) {
        return stage >= ALERT_THRESHOLD;
    }
}
