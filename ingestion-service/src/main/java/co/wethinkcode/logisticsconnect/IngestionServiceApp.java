package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.io.InputStream;
import java.util.List;

public class IngestionServiceApp {

    public static void main(String[] args) {
        try {
            HubCleaner cleaner = new HubCleaner();

            InputStream input = IngestionServiceApp.class
                    .getClassLoader()
                    .getResourceAsStream("hubs-global.csv");

            if (input == null) {
                throw new IllegalStateException("hubs-global.csv was not found");
            }

            List<Hub> hubs = cleaner.loadAndClean(input);
            ObjectMapper mapper = new ObjectMapper();

            Javalin app = Javalin.create().start(7050);

            app.get("/health", ctx -> ctx.result("OK"));

            app.get("/hubs", ctx -> {
                ctx.contentType("application/json");
                ctx.result(mapper.writeValueAsString(hubs));
            });

            app.get("/hubs/{hubId}", ctx -> {
                String requestedId = ctx.pathParam("hubId").toUpperCase();

                Hub found = null;

                for (Hub hub : hubs) {
                    if (hub.getHubId().equalsIgnoreCase(requestedId)) {
                        found = hub;
                        break;
                    }
                }

                if (found == null) {
                    ctx.status(404).result("Hub not found");
                    return;
                }

                ctx.contentType("application/json");
                ctx.result(mapper.writeValueAsString(found));
            });

            System.out.println("Ingestion Service running on port 7050");
            System.out.println("Loaded " + hubs.size() + " cleaned hubs.");

        } catch (Exception e) {
            System.err.println("Could not start ingestion service: " + e.getMessage());
        }
    }
}
