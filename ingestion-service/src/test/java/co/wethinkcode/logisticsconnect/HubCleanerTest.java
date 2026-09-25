package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubCleanerTest {

    @Test
    void cleansHubData() throws Exception {
        String csv = "hub_id,province,sorting_center,active\n"
                + " jhb01 , gauteng , johannesburg central , yes\n";

        HubCleaner cleaner = new HubCleaner();
        List<Hub> hubs = cleaner.loadAndClean(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(1, hubs.size());
        assertEquals("JHB01", hubs.get(0).getHubId());
        assertEquals("Gauteng", hubs.get(0).getProvince());
        assertEquals("Johannesburg Central", hubs.get(0).getSortingCenter());
        assertTrue(hubs.get(0).isActive());
    }

    @Test
    void removesDuplicateProvinceAndCenterRecords() throws Exception {
        String csv = "hub_id,province,sorting_center,active\n"
                + "JHB01,Gauteng,Johannesburg Central,no\n"
                + "JHB99,Gauteng,Johannesburg Central,yes\n";

        HubCleaner cleaner = new HubCleaner();
        List<Hub> hubs = cleaner.loadAndClean(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(1, hubs.size());
        assertTrue(hubs.get(0).isActive());
    }
}
