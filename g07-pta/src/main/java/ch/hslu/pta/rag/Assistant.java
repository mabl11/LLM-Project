package ch.hslu.pta.rag;

import dev.langchain4j.service.Result;

/**
 * We need this Interface for langChain4J magic to happen
 * More info here: <a href="https://docs.langchain4j.dev/tutorials/ai-services">...</a>
 */
public interface Assistant {
    //String answer(String query);
    Result<String> answer(String userQuery);
}