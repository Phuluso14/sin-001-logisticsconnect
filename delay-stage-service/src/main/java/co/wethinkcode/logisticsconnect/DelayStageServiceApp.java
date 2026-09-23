package co.wethinkcode.logisticsconnect;

import co.wethinkcode.logisticsconnect.mq.MqConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DelayStageServiceApp {

    private static final Map<String, Integer> stages = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7052);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/delay-stage/{hubId}", ctx -> {
            String hubId = ctx.pathParam("hubId").toUpperCase();
            int stage = stages.getOrDefault(hubId, 0);

            ctx.json(new DelayStage(hubId, stage));
        });

        app.post("/delay-stage/{hubId}", ctx -> {
            String hubId = ctx.pathParam("hubId").toUpperCase();
            DelayStage request = mapper.readValue(ctx.body(), DelayStage.class);

            if (request.getStage() < 0 || request.getStage() > 8) {
                ctx.status(400).result("Stage must be between 0 and 8");
                return;
            }

            stages.put(hubId, request.getStage());

            try {
                publishStageChange(hubId, request.getStage());
            } catch (Exception e) {
                ctx.status(503).result(
                        "Delay stage saved, but ActiveMQ is unavailable"
                );
                return;
            }

            ctx.status(200);
            ctx.json(new DelayStage(hubId, request.getStage()));
        });

        System.out.println("Delay Stage Service running on port 7052");
    }

    private static void publishStageChange(String hubId, int stage) throws Exception {
        ActiveMQConnectionFactory factory =
                new ActiveMQConnectionFactory(MqConfig.BROKER_URL);

        try (Connection connection = factory.createConnection()) {
            connection.start();

            Session session = connection.createSession(
                    false,
                    Session.AUTO_ACKNOWLEDGE
            );

            Topic topic = session.createTopic(MqConfig.TOPIC);
            MessageProducer producer = session.createProducer(topic);

            StageChangeEvent event = new StageChangeEvent(
                    hubId, stage, Instant.now().toString()
            );
            String json = mapper.writeValueAsString(event);

            TextMessage message = session.createTextMessage(json);
            producer.send(message);

            producer.close();
            session.close();
        }
    }
}
