package ch.hslu.pta.rag;

import dev.langchain4j.service.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This module contains the main class for the implementation of the REST API. This API will be used as an Interface
 * to ensure the connectivity between User accessible UI and backend AI provided by LangChain4j
 */

@RestController
@RequestMapping("/api")
public class ApiController {

    private final Assistant defaultAssistant;
    private final Assistant hrAssistant;
    private final Assistant developerAssistant;
    private final Assistant financeBroAssistant;

    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    public ApiController(
            @Qualifier("defaultAssistant") Assistant defaultAssistant,
            @Qualifier("hrAssistant") Assistant hrAssistant,
            @Qualifier("developerAssistant") Assistant developerAssistant,
            @Qualifier("financeBroAssistant") Assistant financeBroAssistant)
    {
        this.defaultAssistant = defaultAssistant;
        this.hrAssistant = hrAssistant;
        this.developerAssistant = developerAssistant;
        this.financeBroAssistant = financeBroAssistant;
    }

    private List<String> forbiddenPhrases = Arrays.asList(
            "wenden Sie sich an die PTA Gruppe 7",
            "bei seriösen Anfragen",
            "wie kann ich Sie heute während Ihrer Einarbeitung unterstützen",
            "Hallo, ich bin Clippy, Ihr professioneller Assistent"
    );

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/request")
    public Map<String, Object> request(@RequestBody Map<String, String> input){

        LOG.info("[REQUEST]: {}", input);

        String request = input.get("request");
        String userRole = input.get("userRole");

        LOG.info("[REQUEST]: {}", request);
        LOG.info("[USER ROLE]: {}", userRole);

        Assistant assistant = switch (userRole.toLowerCase()) {
            case "hr" -> hrAssistant;
            case "developer" -> developerAssistant;
            case "finance" -> financeBroAssistant;
            default -> defaultAssistant;
        };

        Result<String> result = assistant.answer(request);
        String answer = result.content();

        Map<String, Object> response = new HashMap<>();
        response.put("answer", answer);
        response.put("userRole", userRole);

        LOG.info("[ANSWER]: {}", answer);

        boolean isValidAnswer = forbiddenPhrases.stream()
                .noneMatch(answer::contains);

        if(isValidAnswer) {
            List<Map<String, String>> sources = result.sources().stream()
                    .filter(content -> {
                        Double score = content.metadata()
                                .values().stream().filter(v -> v instanceof Number)
                                .map(v -> ((Number) v).doubleValue())
                                .findFirst().orElse(0.0);
                        return score > 0.718;
                    }) // only list document with score > 0.718
                    .map(content -> {
                        var metadata = content.textSegment().metadata();
                        String fileName = metadata.getString("file_name");
                        String dirPath = metadata.getString("absolute_directory_path");

                        Path fullPath = Paths.get(dirPath, fileName);

                        String downloadId = Base64.getUrlEncoder().encodeToString(fullPath.toString().getBytes(StandardCharsets.UTF_8));

                        Map<String, String> sourceMap = new HashMap<>();
                        sourceMap.put("name", fileName);
                        sourceMap.put("id", downloadId);
                        //sourceMap.put("path", dirPath);
                        return sourceMap;
                    })
                    .distinct()
                    .collect(Collectors.toList());

            List<Map<String, String>> uniqueSources = sources.stream()
                    .collect(Collectors.groupingBy(m -> m.get("id")))
                    .values().stream()
                    .map(list -> list.get(0))
                    .collect(Collectors.toList());

            response.put("sources", uniqueSources);
            LOG.info("[SOURCES]: {}", uniqueSources);
        } else {
            LOG.info("No sources, because of fallback answer");
        }
        return response;
    }

    @GetMapping("/files/{id}/{filename}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable("id") String id,
            @PathVariable("filename") String filename) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(id);
            String pathString = new String(decodedBytes, StandardCharsets.UTF_8);
            Path filePath = Paths.get(pathString);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {

                String contentType = Files.probeContentType(filePath);

                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                        .filename(resource.getFilename())
                        .build();

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
