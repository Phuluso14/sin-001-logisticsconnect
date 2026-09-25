package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StageChangeEventTest {

    @Test
    void serializesStageChangeEventForMessaging() throws Exception {
        StageChangeEvent event = new StageChangeEvent(
                "JHB01", 5, "2026-09-24T10:00:00Z"
        );

        String json = new ObjectMapper().writeValueAsString(event);

        assertEquals("JHB01", new ObjectMapper().readTree(json).get("hubId").asText());
        assertEquals(5, new ObjectMapper().readTree(json).get("stage").asInt());
        assertEquals("2026-09-24T10:00:00Z", new ObjectMapper().readTree(json).get("timestamp").asText());
    }
}
