package ch.hslu.pta.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rometools.utils.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;

import static com.healthmarketscience.jackcess.impl.expr.DefaultNumberFunctions.LOG;

public class JsonReader {

    private static final Logger LOG = LoggerFactory.getLogger(JsonReader.class);

    private JsonReader() {}

    private static String getPathFromRole(String role, Path jsonFile) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(jsonFile.toFile());
            JsonNode roleNode = root.get(role);
            if (roleNode == null) return null;
            return roleNode.get("documentPath").asText();
        } catch (Exception e) {
            LOG.error("Error while loading json file -> default path: documents");
            return "documents";
        }
    }

    public static String getPermissionPath(String role) {
        Path jsonFile = AssistantConfiguration.getDirectoryPath("permission/userConfig.json");
        return getPathFromRole(role, jsonFile);
    }

}