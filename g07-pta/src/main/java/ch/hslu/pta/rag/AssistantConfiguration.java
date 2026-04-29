package ch.hslu.pta.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.model.input.PromptTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Configuration
public class AssistantConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AssistantConfiguration.class);
    public static final String BASE_URL = "http://localhost:11434"; //"https://ollama.com";
    public static final String MODEL_NAME = "cogito-2.1:671b-cloud";
    public static final String DOCUMENT_PATH = "documents";
    public static final int MAX_RESULTS = 5;
    public static final double MIN_SCORE = 0.5;
    public static final boolean LOG_REQUESTS = true;
    public static final boolean LOG_RESPONSES = true;
    public static final int MAX_MESSAGES = 10;
    public static final int MAX_SEGMENT_SIZE = 1000;
    public static final int OVERLAP_SIZE = 300;
    public static final Duration EMBEDDING_TIMEOUT = Duration.ofSeconds(600);
    public static final Duration CHAT_TIMEOUT = Duration.ofSeconds(600);
    public static final double TEMPERATURE = 1.2;
    public static final String API_KEY = "509f2fa2c65549739734f08981aea65f.NlIs9HuzJz9ik8sBqT0xEl0x";
    private static final String SYSTEM_PROMPT_PATH = "prompt/clippy_prompt.md";
    private String userRole = "default";

    @Bean
    @Qualifier("defaultAssistant")
    public Assistant defaultAssistant() {
        return createAssistantForRole("default");
    }

    @Bean
    @Qualifier("hrAssistant")
    public Assistant hrAssistant() {
        return createAssistantForRole("HR");
    }

    @Bean
    @Qualifier("developerAssistant")
    public Assistant developerAssistant() {
        return createAssistantForRole("Developer");
    }

    @Bean
    @Qualifier("financeBroAssistant")
    public Assistant financeBroAssistant() {
        return createAssistantForRole("FinanceBro");
    }

    private Assistant createAssistantForRole (String userRole) {
        ChatModel chatModel = createOllamaChatModel();
        EmbeddingModel embeddingModel = getEmbeddingModel();

        List<Document> textDocuments = new ArrayList<>();
        if (!"default".equals(userRole)) {
            textDocuments.addAll(loadDocuments(JsonReader.getPermissionPath(userRole)));
        }
        textDocuments.addAll(loadDocuments(DOCUMENT_PATH));

        DocumentSplitter splitter = DocumentSplitters.recursive(MAX_SEGMENT_SIZE, OVERLAP_SIZE);
        List<TextSegment> segments = splitter.splitAll(textDocuments);

        LOG.info("Creating embeddings for {} segments (role={})", segments.size(), userRole);
        List<Embedding> embeddings = getEmbeddings(embeddingModel, segments);
        EmbeddingStore<TextSegment> embeddingStore = createEmbeddingStore(embeddings, segments);
        ContentRetriever contentRetriever = getContentRetriever(embeddingStore, embeddingModel);
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(MAX_MESSAGES);

        return createAssistant(chatModel, contentRetriever, chatMemory);
    }

    public void setUserRole(String userRole){
        LOG.info("user role");
        this.userRole = userRole;
    }

    private static List<Document> loadDocuments(String documentPath) {
        Path directoryPath = getDirectoryPath(documentPath);
        LOG.info("Documents loading from: {}", directoryPath);
        return loadDocumentsFromPath(directoryPath);
    }

    public static Path getDirectoryPath(String documentPath) {
        try {
            URL resource = AssistantConfiguration.class.getClassLoader().getResource(documentPath);
            return Paths.get(Objects.requireNonNull(resource).toURI());
        } catch (URISyntaxException e) {
            LOG.error("Directory does not exists {}", documentPath, e);
            throw new RuntimeException(e);
        }
    }

    private static List<Document> loadDocumentsFromPath(Path directoryPath) {
        if (Objects.isNull(directoryPath) || !Files.exists(directoryPath)) {
            return new ArrayList<>();
        }

        List<Document> documents = new ArrayList<>();

        PathMatcher txtExtensionMatcher = FileSystems.getDefault().getPathMatcher("glob:**.txt");
        PathMatcher pdfExtensionMatcher = FileSystems.getDefault().getPathMatcher("glob:**.pdf");

        documents.addAll(FileSystemDocumentLoader.loadDocumentsRecursively(directoryPath, txtExtensionMatcher, new TextDocumentParser()));
        documents.addAll(FileSystemDocumentLoader.loadDocumentsRecursively(directoryPath, pdfExtensionMatcher, new ApachePdfBoxDocumentParser()));

        LOG.info("Loaded {} documents", documents.size());
        for (Document document : documents) {
            LOG.info("Processing documents {}", document.metadata().getString(Document.FILE_NAME));
        }

        return documents;
    }

    private static String loadSystemPrompt(){
        try{
            Path p = getDirectoryPath(AssistantConfiguration.SYSTEM_PROMPT_PATH);
            return Files.readString(p,  StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Error while loading Path: {}", AssistantConfiguration.SYSTEM_PROMPT_PATH, e);
            throw new RuntimeException("File not found at "+ AssistantConfiguration.SYSTEM_PROMPT_PATH, e);
        }
    }

    static Assistant createAssistant(ChatModel chatModel, ContentRetriever contentRetriever, ChatMemory chatMemory) {
        String systemPrompt = loadSystemPrompt();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .contentInjector(DefaultContentInjector.builder()
                        .promptTemplate(PromptTemplate.from(systemPrompt))
                        .build()
                )
                .build();

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(chatMemory)
                .build();
    }

    static ContentRetriever getContentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(MAX_RESULTS)
                .minScore(MIN_SCORE)
                .build();
    }

    static EmbeddingStore<TextSegment> createEmbeddingStore(List<Embedding> embeddings, List<TextSegment> segments) {
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }

    static List<Embedding> getEmbeddings(EmbeddingModel embeddingModel, List<TextSegment> segments) {
        return embeddingModel.embedAll(segments).content();
    }

    static OllamaChatModel createOllamaChatModel() {
        return OllamaChatModel.builder()
                .customHeaders(Map.of("Authorization", "Bearer" + API_KEY))
                .baseUrl(BASE_URL)
                .modelName(MODEL_NAME)
                .timeout(CHAT_TIMEOUT)
                .logRequests(LOG_REQUESTS)
                .logResponses(LOG_RESPONSES)
                .temperature(TEMPERATURE)
                .build();
    }

    static EmbeddingModel getEmbeddingModel() {
        final String JINA_KEY = "jina_b6305b05d86c4b7180a3d26ba8f38a6dGMuXmvpzzYjAHCqb4xoucIilqqxQ";
        //final String OPENAI_KEY = "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";
        return OpenAiEmbeddingModel
                .builder()
                .apiKey(JINA_KEY)
                .baseUrl("https://api.jina.ai/v1")
                .modelName("jina-embeddings-v3")
                .timeout(EMBEDDING_TIMEOUT)
                .build();
    }
}