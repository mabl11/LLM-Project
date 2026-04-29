package ch.hslu.pta.rag;

public class CliApplication {

    // User Role -> resources/permission/userConfig.json to see all user Roles.
    private static final String USER_ROLE = "FinanceBro";

    public static void main(String[] args) {
        // Use Spring's RAG configuration — but without running Spring Boot fully
        AssistantConfiguration config = new AssistantConfiguration();
        config.setUserRole(USER_ROLE);

        Assistant assistant = config.defaultAssistant();

        CliController.startCLI(assistant);
    }
}