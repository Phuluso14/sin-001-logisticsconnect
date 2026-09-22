package co.wethinkcode.logisticsconnect;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HubCleaner {

    private static final Map<String, String> PROVINCES = Map.of(
            "gauteng", "Gauteng",
            "western cape", "Western Cape",
            "kwazulu natal", "KwaZulu-Natal",
            "kwa-zulu natal", "KwaZulu-Natal",
            "free state", "Free State",
            "eastern cape", "Eastern Cape",
            "limpopo", "Limpopo",
            "north west", "North West",
            "mpumalanga", "Mpumalanga",
            "northern cape", "Northern Cape"
    );

    private static final Map<String, String> CENTERS = Map.of(
            "johannesburg central", "Johannesburg Central",
            "cape town port", "Cape Town Port",
            "pretoria north", "Pretoria North",
            "durban harbour", "Durban Harbour",
            "bloemfontein hub", "Bloemfontein Hub",
            "port elizabeth hub", "Port Elizabeth Hub",
            "polokwane hub", "Polokwane Hub",
            "rustenburg hub", "Rustenburg Hub",
            "nelspruit hub", "Nelspruit Hub",
            "kimberley hub", "Kimberley Hub"
    );

    private static final Map<String, String> CENTER_PROVINCES = Map.of(
            "Pretoria North", "Gauteng",
            "Johannesburg Central", "Gauteng",
            "Cape Town Port", "Western Cape",
            "Durban Harbour", "KwaZulu-Natal",
            "Bloemfontein Hub", "Free State",
            "Port Elizabeth Hub", "Eastern Cape",
            "Polokwane Hub", "Limpopo",
            "Rustenburg Hub", "North West",
            "Nelspruit Hub", "Mpumalanga",
            "Kimberley Hub", "Northern Cape"
    );

    public List<Hub> loadAndClean(InputStream inputStream) throws Exception {
        Map<String, Hub> uniqueHubs = new LinkedHashMap<>();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            reader.readNext(); // header

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 4) {
                    continue;
                }

                String hubId = cleanText(row[0]).toUpperCase();
                String province = cleanProvince(row[1]);
                String sortingCenter = cleanCenter(row[2]);
                boolean active = cleanBoolean(row[3]);

                if (hubId.isBlank() || sortingCenter.isBlank()) {
                    continue;
                }

                // If the province is missing, use the known province of the center.
                if (province.isBlank()) {
                    province = CENTER_PROVINCES.getOrDefault(sortingCenter, "Unknown");
                }

                Hub hub = new Hub(hubId, province, sortingCenter, active);

                // The real-world entity is identified by province + sorting center.
                String duplicateKey = province.toLowerCase() + "|" +
                        sortingCenter.toLowerCase();

                if (!uniqueHubs.containsKey(duplicateKey)) {
                    uniqueHubs.put(duplicateKey, hub);
                } else {
                    // If duplicate records disagree, an active record wins.
                    Hub existing = uniqueHubs.get(duplicateKey);
                    existing.setActive(existing.isActive() || hub.isActive());
                }
            }
        }

        return new ArrayList<>(uniqueHubs.values());
    }

    private String cleanText(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().replaceAll("\s+", " ");
    }

    private String cleanProvince(String value) {
        String cleaned = cleanText(value).toLowerCase();

        if (cleaned.isBlank() || isPlaceholder(cleaned)) {
            return "";
        }

        return PROVINCES.getOrDefault(cleaned, titleCase(cleaned));
    }

    private String cleanCenter(String value) {
        String cleaned = cleanText(value).toLowerCase();

        if (cleaned.isBlank() || isPlaceholder(cleaned)) {
            return "";
        }
        return CENTERS.getOrDefault(cleaned, titleCase(cleaned));
    }

    private boolean cleanBoolean(String value) {
        String cleaned = cleanText(value).toLowerCase();

        return cleaned.equals("y")
                || cleaned.equals("yes")
                || cleaned.equals("1")
                || cleaned.equals("true");
    }

    private boolean isPlaceholder(String value) {
        return value.isBlank()
                || value.equals("n/a")
                || value.equals("na")
                || value.equals("unknown")
                || value.equals("tbd")
                || value.equals("-")
                || value.equals("nan");
    }

    private String titleCase(String value) {
        StringBuilder result = new StringBuilder();

        for (String word : value.split(" ")) {
            if (word.isBlank()) {
                continue;
            }

            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }

        return result.toString().trim();
    }
}
