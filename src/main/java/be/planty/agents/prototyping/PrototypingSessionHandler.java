package be.planty.agents.prototyping;

import be.planty.agents.assistant.AgentSessionHandler;
import be.planty.models.prototyping.ActionRequest;
import be.planty.models.prototyping.ActionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static be.planty.models.assistant.Constants.PAYLOAD_TYPE_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class PrototypingSessionHandler extends AgentSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PrototypingSessionHandler.class);

    private static final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private static final String BASE_URL = "http://dummy-api-host/api";

    private static final AtomicLong appSequence = new AtomicLong(0);

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        //super.handleFrame(headers, payload);
        if (payload instanceof ActionRequest
                && "NewWebAppIntent".equals(((ActionRequest) payload).action)) {
            try {
                final var prettyJson = objectWriter.writeValueAsString(payload);
                logger.info("Received action request: " + prettyJson);

                // TODO: remove the following once the implementation is ready
                if (true) {
                    //sendActionResponse(headers, HttpStatus.NOT_IMPLEMENTED.value());
                    final String appId;
                    synchronized (appSequence) {
                        appId = "app#" + appSequence.incrementAndGet();
                    }
                    final var actionResponse = new ActionResponse<>(HttpStatus.OK.value(), appId);
                    logger.info("actionResponse: " + actionResponse);
                    sendActionResponse(headers, actionResponse);
                    return;
                }

                final var actionRequest = (ActionRequest) payload;
                final var phoneNumber = actionRequest.parameters.getOrDefault("phoneNumber", "0123456789");
                final var appName = actionRequest.parameters.get("appName");

                final var moreDetailsResponse = new RestTemplate()
                        .getForEntity(BASE_URL + "/more-details/phone-no/" + phoneNumber, Map.class);

                logger.info("moreDetailsResponse: " + moreDetailsResponse);
                if (moreDetailsResponse.getStatusCode().isError()) {
                    sendActionResponse(headers, moreDetailsResponse.getStatusCodeValue());
                    return;
                }
                final var customerId = String.valueOf(moreDetailsResponse.getBody().get("customerId"));
                final HttpEntity<ObjectNode> creationRequest = newCreationRequest(appName, customerId);
                final var creationResponse = new RestTemplate()
                        .postForEntity(BASE_URL + "/app-generator/create", creationRequest, Map.class);
                logger.info("creationResponse: " + creationResponse);
                if (creationResponse.getStatusCode().isError()) {
                    sendActionResponse(headers, creationResponse.getStatusCodeValue());
                    return;
                }
                final var appId = String.valueOf(creationResponse.getBody().get("appId"));
                final var actionResponse = new ActionResponse<>(HttpStatus.OK.value(), appId);
                logger.info("actionResponse: " + actionResponse);
                sendActionResponse(headers, actionResponse);

            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        } else {
            sendActionResponse(headers, HttpStatus.NOT_IMPLEMENTED.value());
        }
    }

    private HttpEntity<ObjectNode> newCreationRequest(String appName, String customerId) {
        final var createHeaders = new HttpHeaders();
        createHeaders.setContentType(APPLICATION_JSON);
        final var nodeFactory = JsonNodeFactory.instance;
        final var creationPayload = nodeFactory.objectNode();
        creationPayload
                .put("customerId", customerId)
                .put("appName", appName);
        return new HttpEntity<>(creationPayload, createHeaders);
    }

    private void sendActionResponse(StompHeaders headers, Integer statusCode) {
        sendActionResponse(headers, new ActionResponse(statusCode));
    }

    private void sendActionResponse(StompHeaders headers, ActionResponse actionResponse) {
        logger.info("Request STOMP headers (original)...");
        headers.forEach((k, v) -> logger.info("\t" + k + ": " + v));
        final var newHeaders = createStompHeaders(headers);
        newHeaders.set(PAYLOAD_TYPE_KEY, actionResponse.getClass().getTypeName());
        logger.info("Sending: " + actionResponse + "...");
        try {
            logger.info(objectWriter.writeValueAsString(actionResponse));

        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        logger.info("Response STOMP headers (new)...");
        newHeaders.forEach((k, v) -> logger.info("\t" + k + ": " + v));
        session.send(newHeaders, actionResponse);
    }
}
