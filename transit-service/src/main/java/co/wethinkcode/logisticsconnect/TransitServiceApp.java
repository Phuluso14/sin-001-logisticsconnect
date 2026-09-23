package co.wethinkcode.logisticsconnect;

import co.wethinkcode.logisticsconnect.mq.MqConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransitServiceApp {

    private static final String HUB_SERVICE_URL = "http://localhost:7051";
    private static final Map<String, Integer> delayStages = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        startMessageConsumer();

        Javalin app = Javalin.create().start(7053);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/eta/{hubId}", ctx -> {
            String hubId = ctx.pathParam("hubId").toUpperCase();

            String hubJson = getHub(hubId);

            if (hubJson == null) {
                ctx.status(404).result("Hub not found");
                return;
            }

            JsonNode hub = mapper.readTree(hubJson);
            int stage = delayStages.getOrDefault(hubId, 0);

            int baseMinutes = 60;
            int delayMinutes = stage * 30;
            int totalMinutes = baseMinutes + delayMinutes;

            EtaResponse response = new EtaResponse(
                    hub.get("hubId").asText(),
                    hub.get("sortingCenter").asText(),
                    stage,
                    totalMinutes
            );

            ctx.json(response);
        });

        System.out.println("Transit Service running on port 7053");
        System.out.println("Waiting for messages on " + MqConfig.TOPIC);
    }

    private static String getHub(String hubId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HUB_SERVICE_URL + "/hubs/" + hubId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                return null;
            }

            return response.body();

        } catch (Exception e) {
            System.err.println("Could not contact hub service: " + e.getMessage());
            return null;
        }
    }

    private static void startMessageConsumer() {
        Thread consumerThread = new Thread(() -> {
            try {
                ActiveMQConnectionFactory factory =
                        new ActiveMQConnectionFactory(MqConfig.BROKER_URL);

                Connection connection = factory.createConnection();
                connection.start();

                Session session = connection.createSession(
                        false,
                        Session.AUTO_ACKNOWLEDGE
                );

                Topic topic = session.createTopic(MqConfig.TOPIC);
                MessageConsumer consumer = session.createConsumer(topic);

                consumer.setMessageListener(message -> {
                    try {
                        if (message instanceof TextMessage textMessage) {
                            JsonNode data = mapper.readTree(textMessage.getText());

                            String hubId = data.get("hubId").asText().toUpperCase();
                            int stage = data.get("stage").asInt();

                            delayStages.put(hubId, stage);

                            System.out.println(
                                    "Transit received stage update: "
                                            + hubId + " -> stage " + stage
                            );
                        }
                    } catch (Exception e) {
                        System.err.println(
                                "Could not process MQ message: " + e.getMessage()
                        );
                    }
                });

            } catch (Exception e) {
                System.err.println(
                        "Could not connect to ActiveMQ. "
                                + "Start common/docker-compose.yml first."
                );
                System.err.println(e.getMessage());
            }
        });

        consumerThread.setDaemon(true);
        consumerThread.start();
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.logisticsconnect.mq.MqConfig)
