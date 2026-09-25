package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DelayStageTest {

    @Test
    void acceptsStageZero() {
        DelayStage stage = new DelayStage("JHB01", 0);
        assertTrue(isValid(stage.getStage()));
    }

    @Test
    void acceptsStageEight() {
        DelayStage stage = new DelayStage("JHB01", 8);
        assertTrue(isValid(stage.getStage()));
    }

    @Test
    void rejectsStageBelowZero() {
        DelayStage stage = new DelayStage("JHB01", -1);
        assertFalse(isValid(stage.getStage()));
    }

    @Test
    void rejectsStageAboveEight() {
        DelayStage stage = new DelayStage("JHB01", 9);
        assertFalse(isValid(stage.getStage()));
    }

    private boolean isValid(int stage) {
        return stage >= 0 && stage <= 8;
    }
}
