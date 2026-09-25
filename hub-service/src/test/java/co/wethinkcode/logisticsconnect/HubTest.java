package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HubTest {

    @Test
    void serializesHubAsJson() throws Exception {
        Hub hub = new Hub();
        hub.setHubId("JHB01");
        hub.setProvince("Gauteng");
        hub.setSortingCenter("Johannesburg Central");
        hub.setActive(true);

        String json = new ObjectMapper().writeValueAsString(hub);

        assertTrue(json.contains("\"hubId\":\"JHB01\""));
        assertTrue(json.contains("\"province\":\"Gauteng\""));
        assertTrue(json.contains("\"sortingCenter\":\"Johannesburg Central\""));
        assertTrue(json.contains("\"active\":true"));
    }
}
