package be.planty.agents.prototyping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
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
        final var url = "ws://localhost:8080/websocket/pairing";
        final var socketClient = new StandardWebSocketClient();

        //WebSocketStompClient stompClient = new WebSocketStompClient(socketClient);
        final var sockJsClient = new SockJsClient(asList(
                new WebSocketTransport(socketClient)
        ));
        final var stompClient = new WebSocketStompClient(sockJsClient);

        stompClient.setMessageConverter(
                new CompositeMessageConverter(asList(
                    new MappingJackson2MessageConverter(),
                    new StringMessageConverter()
        )));

        stompClient.setTaskScheduler(new DefaultManagedTaskScheduler());
        final StompSessionHandler handler = new PairingSessionHandler();
        logger.info("Connecting to: " + url + " ...");
        stompClient.connect(url, handler);
    }
}

class PairingSessionHandler extends MyStompSessionHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PairingSessionHandler.class);

    private StompSession session;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        logger.info("Connected!");
        session.subscribe("/topic/pairing.res", this);
        final var payload = new PairingRequest("Agent X", "1234", "ASDF");
        logger.info("Sending: " + toPrettyJson(payload));
        session.send("/topic/pairing.req", payload);
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
        final var url = "ws://localhost:8080/websocket/agent";
        final var socketClient = new StandardWebSocketClient();

        //WebSocketStompClient stompClient = new WebSocketStompClient(socketClient);
        final var sockJsClient = new SockJsClient(asList(
                new WebSocketTransport(socketClient)
        ));
        final var stompClient = new WebSocketStompClient(sockJsClient);

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        final var handler = new AgentSessionHandler();
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

class AgentSessionHandler extends MyStompSessionHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PairingSessionHandler.class);
    private StompSession session;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        logger.info("Subscribing to /topic/agent.req...");
        session.subscribe("/topic/agent.req", this);
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        logger.info("Received headers: " + headers);
        logger.info("Received payload: " + payload);
        if (headers.getDestination().equals("/topic/agent.req")) {
            session.send("/topic/agent.res", "Thank you for choosing me!");
        }
    }

}

class MyStompSessionHandlerAdapter extends StompSessionHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MyStompSessionHandlerAdapter.class);

    private static final ObjectWriter objWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error(exception.toString(), exception);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        logger.error(exception.toString(), exception);
    }

    protected String toPrettyJson(Object payload) {
        try {
            return objWriter.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return String.valueOf(payload);
        }
    }
}
