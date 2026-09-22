package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class HubServiceApp {

    private static final String INGESTION_URL = "http://localhost:7050";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static List<Hub> hubs = new ArrayList<>();

    public static void main(String[] args) {
        loadHubsFromIngestion();

        Javalin app = Javalin.create().start(7051);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/hubs", ctx -> {
            ctx.json(hubs);
        });

        app.get("/hubs/{hubId}", ctx -> {
            String hubId = ctx.pathParam("hubId");

            for (Hub hub : hubs) {
                if (hub.getHubId().equalsIgnoreCase(hubId)) {
                    ctx.json(hub);
                    return;
                }
            }

            ctx.status(404).result("Hub not found");
        });

        System.out.println("Hub Service running on port 7051");
        System.out.println("Loaded " + hubs.size() + " hubs from ingestion service.");
    }

    private static void loadHubsFromIngestion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(INGESTION_URL + "/hubs"))
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Ingestion service returned " + response.statusCode()
                );
            }

            hubs = MAPPER.readValue(
                    response.body(),
                    new TypeReference<List<Hub>>() {}
            );

        } catch (Exception e) {
            System.err.println("Could not load hubs from ingestion service.");
            System.err.println("Make sure ingestion-service is running first.");
            System.err.println(e.getMessage());
        }
    }
}
