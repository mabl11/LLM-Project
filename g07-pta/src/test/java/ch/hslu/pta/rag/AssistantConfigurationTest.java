package ch.hslu.pta.rag;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled
/*
  The Test is Disabled, because it cannot be executed on the CI System, as the LLM is not available there
 */
class AssistantConfigurationTest {

    static Assistant assistant;

    @BeforeAll
    static void setUp() {
        AssistantConfiguration config = new AssistantConfiguration();
        assistant = config.defaultAssistant();
        assertNotNull(assistant);
    }
}