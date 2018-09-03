package be.planty.agents.prototyping;

import be.planty.agents.assistant.AgentSessionHandler;
import be.planty.models.prototyping.ActionRequest;
import be.planty.models.prototyping.ActionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.stomp.StompHeaders;

import static be.planty.models.assistant.Constants.PAYLOAD_TYPE_KEY;

public class PrototypingSessionHandler extends AgentSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PrototypingSessionHandler.class);

    private static final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        //super.handleFrame(headers, payload);
        if (payload instanceof ActionRequest) {
            try {
                final String prettyJson = objectWriter.writeValueAsString(payload);
                logger.info("Received action request: " + prettyJson);

            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
            final var response = new ActionResponse(HttpStatus.OK.value());
            final StompHeaders newHeaders = createStompHeaders(headers);
            newHeaders.set(PAYLOAD_TYPE_KEY, response.getClass().getTypeName());
            logger.info("Sending: " + response);
            session.send(newHeaders, response);
        }
    }
}
