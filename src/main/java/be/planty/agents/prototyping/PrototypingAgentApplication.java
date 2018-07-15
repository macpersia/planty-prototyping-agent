package be.planty.agents.prototyping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSession.Receiptable;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import static java.util.Arrays.asList;

@SpringBootApplication
public class PrototypingAgentApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PrototypingAgentApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PrototypingAgentApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final String url = "ws://localhost:8080/websocket/pairing";
        WebSocketClient socketClient = new StandardWebSocketClient();

        //WebSocketStompClient stompClient = new WebSocketStompClient(socketClient);
        SockJsClient sockJsClient = new SockJsClient(asList(
                new WebSocketTransport(socketClient)
        ));
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        final StompSessionHandler handler = new PairingSessionHandler();
        logger.info("Connecting to: " + url + " ...");
        stompClient.connect(url, handler);
    }
}

class PairingSessionHandler extends StompSessionHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PairingSessionHandler.class);

    private static final ObjectWriter objWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private StompSession session;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        logger.info("Connected!");
        session.subscribe("/topic/pairing/responses", this);
        final Object payload = new PairingRequest("Agent X", "1234", "ASDF");
        logger.info("Sending: " + toPrettyJson(payload));
        session.send("/topic/pairing/requests", payload);
    }

    private String toPrettyJson(Object payload) {
        try {
            return objWriter.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return String.valueOf(payload);
        }
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
//			TextMessage msg = (TextMessage) payload;
//			logger.info("Received: " + msg.getPayload() + ", from: " + msg.getFrom());
        logger.info("Received headers: " + headers);
        logger.info("Received payload: " + payload);
        if (payload.toString().equals("accepted")) {
            subscribeToAgentRequests();
        }
    }

    static void subscribeToAgentRequests() {
        final String url = "ws://localhost:8080/websocket/agent";
        WebSocketClient socketClient = new StandardWebSocketClient();

        //WebSocketStompClient stompClient = new WebSocketStompClient(socketClient);
        SockJsClient sockJsClient = new SockJsClient(asList(
                new WebSocketTransport(socketClient)
        ));
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        final StompSessionHandler handler = new AgentSessionHandler();
        logger.info("Connecting to: " + url + " ...");
        stompClient.connect(url, handler);
    }

}

class PairingRequest {

    public final String name;
    public final String verificationCode;
    public final String publicKey;

    public PairingRequest(String name, String verificationCode, String publicKey) {
        this.name = name;
        this.verificationCode = verificationCode;
        this.publicKey = publicKey;
    }
}

class AgentSessionHandler extends StompSessionHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PairingSessionHandler.class);
    private StompSession session;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        logger.info("Subscribing to /topic/agent/requests...");
        session.subscribe("/topic/agent/requests", this);
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        logger.info("Received headers: " + headers);
        logger.info("Received payload: " + payload);
        if (headers.getDestination().equals("/topic/agent/requests")) {
            session.send("/topic/agent/responses", "Thank you for choosing me!");
        }
    }

}
