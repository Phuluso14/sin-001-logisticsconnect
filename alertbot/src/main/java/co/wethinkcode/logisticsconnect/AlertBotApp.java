package co.wethinkcode.logisticsconnect;

import co.wethinkcode.logisticsconnect.mq.MqConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

public class AlertBotApp {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int ALERT_THRESHOLD = 5;

    public static void main(String[] args) {
        startMessageConsumer();

        Javalin app = Javalin.create().start(7054);

        app.get("/health", ctx -> ctx.result("OK"));

        System.out.println("AlertBot running on port 7054");
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

                            String hubId = data.get("hubId").asText();
                            int stage = data.get("stage").asInt();

                            if (stage >= ALERT_THRESHOLD) {
                                System.out.println(
                                        "ALERT: Hub " + hubId
                                                + " has a serious delay. Stage: "
                                                + stage
                                );
                            }
                        }
                    } catch (Exception e) {
                        System.err.println(
                                "Could not process alert message: "
                                        + e.getMessage()
                        );
                    }
                });

            } catch (Exception e) {
                System.err.println(
                        "Could not connect AlertBot to ActiveMQ: "
                                + e.getMessage()
                );
            }
        });

        consumerThread.setDaemon(true);
        consumerThread.start();
    }
}


// MQ TODO (stretch goal): subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.logisticsconnect.mq.MqConfig)
