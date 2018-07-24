package be.planty.agents.prototyping;

import be.planty.agents.assistant.AssistantAgentApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PrototypingAgentApplication extends AssistantAgentApplication {

    @Override
    protected PrototypingAgent createAgent() {
        return new PrototypingAgent();
    }
}
