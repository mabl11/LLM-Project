package ch.hslu.pta.rag;

import dev.langchain4j.service.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CliController {
    public static final String DELIMITER = "==================================================";
    public static final String PROMPT = "User: ";
    public static final String EXIT = "exit";
    public static final String ANSWER = "Assistant: ";
    public static final String SOURCE = "Documents: ";

    private static final Logger LOG = LoggerFactory.getLogger(CliController.class);

    /**
     * Starts a simple CLI (command line interface) which allows users to interact with the given Assistant.
     * The CLI can be left by the command "EXIT"
     * @param assistant: an assistant, more info here: <a href="https://docs.langchain4j.dev/tutorials/ai-services">...</a>
     */
    public static void startCLI(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in)) {

            List<String> forbiddenPhrases = Arrays.asList(
                    "wenden Sie sich an die PTA Gruppe 7",
                    "bei seriösen Anfragen",
                    "wie kann ich Sie heute während Ihrer Einarbeitung unterstützen",
                    "Hallo, ich bin Clippy, Ihr professioneller Assistent"
            );

            while (true) {
                System.out.println(DELIMITER);
                System.out.println(PROMPT);
                String userQuery = scanner.nextLine();
                System.out.println(DELIMITER);

                if (EXIT.equalsIgnoreCase(userQuery)) {
                    break;
                }

                Result<String> result = assistant.answer(userQuery);

                String agentAnswer = result.content();

                System.out.println(DELIMITER);
                System.out.println(ANSWER + agentAnswer);

                boolean isValidAnswer = forbiddenPhrases.stream()
                        .noneMatch(agentAnswer::contains);

                if(isValidAnswer){

                    List<String> sources = result.sources().stream()
                            .filter(content -> {
                                Double score = content.metadata()
                                        .values().stream().filter(v -> v instanceof Number)
                                        .map(v -> ((Number) v).doubleValue())
                                                .findFirst().orElse(0.0);
                                return score > 0.73;
                            }) // only list document with score > 0.73
                            .map(content -> content.textSegment().metadata().getString("file_name"))
                            .distinct()
                            .toList();

                    System.out.println(SOURCE + sources);
                } else {
                    LOG.info("No sources, because of fallback answer");
                }
            }
        }
    }
}
