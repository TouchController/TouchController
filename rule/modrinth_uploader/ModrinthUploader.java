package top.fifthlight.fabazel.modrinthuploader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.MoreBodyPublishers;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import org.slf4j.LoggerFactory;
import top.fifthlight.fabazel.tokenhelper.TokenBackend;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModrinthUploader {
    public static void main(String[] args) throws Exception {
        var root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        var useTokenFromEnvironment = false;
        String tokenSecretId = null;
        String projectId = null;
        String versionName = null;
        String versionId = null;
        String versionType = null;
        String changelogPath = null;
        var gameVersions = new ArrayList<String>();
        var loaders = new ArrayList<String>();
        var dependencies = new ArrayList<ModrinthUploadData.Dependency>();
        String fileName = null;
        String filePath = null;

        String tempDependencyProjectId = null;
        String tempDependencyVersionId = null;
        ModrinthUploadData.Dependency.Type tempDependencyType = null;
        var inDependencyBlock = false;

        for (var i = 0; i < args.length; i++) {
            var arg = args[i];
            switch (arg) {
                case "--use-token-from-environment" -> useTokenFromEnvironment = true;
                case "--token-secret-id" -> tokenSecretId = args[++i];
                case "--project-id" -> projectId = args[++i];
                case "--version-name" -> versionName = args[++i];
                case "--version-id" -> versionId = args[++i];
                case "--version-type" -> versionType = args[++i];
                case "--changelog" -> changelogPath = args[++i];
                case "--game-version" -> gameVersions.add(args[++i]);
                case "--loader" -> loaders.add(args[++i]);
                case "--dependency" -> {
                    if (inDependencyBlock) {
                        if (tempDependencyProjectId != null && tempDependencyType != null) {
                            dependencies.add(new ModrinthUploadData.Dependency(tempDependencyProjectId, tempDependencyVersionId, tempDependencyType));
                        }
                    }
                    tempDependencyProjectId = null;
                    tempDependencyVersionId = null;
                    tempDependencyType = null;
                    inDependencyBlock = true;
                }
                case "--dependency-project-id" -> tempDependencyProjectId = args[++i];
                case "--dependency-version-id" -> tempDependencyVersionId = args[++i];
                case "--dependency-type" -> tempDependencyType = ModrinthUploadData.Dependency.Type.fromName(args[++i]);
                case "--file-name" -> fileName = args[++i];
                default -> {
                    if (filePath != null) {
                        throw new IllegalArgumentException("Duplicate file path: " + arg);
                    }
                    filePath = arg;
                }
            }
        }

        if (inDependencyBlock && tempDependencyProjectId != null && tempDependencyType != null) {
            dependencies.add(new ModrinthUploadData.Dependency(tempDependencyProjectId, tempDependencyVersionId, tempDependencyType));
        }

        if (!useTokenFromEnvironment) {
            Objects.requireNonNull(tokenSecretId, "tokenSecretId cannot be null");
        }
        Objects.requireNonNull(projectId, "projectId cannot be null");
        Objects.requireNonNull(versionName, "versionName cannot be null");
        Objects.requireNonNull(versionId, "versionId cannot be null");
        Objects.requireNonNull(versionType, "versionType cannot be null");
        Objects.requireNonNull(fileName, "fileName cannot be null");
        Objects.requireNonNull(filePath, "filePath cannot be null");
        if (gameVersions.isEmpty()) {
            throw new IllegalArgumentException("gameVersions cannot be empty");
        }
        if (loaders.isEmpty()) {
            throw new IllegalArgumentException("loaders cannot be empty");
        }
        String changelog = null;
        if (changelogPath != null) {
            changelog = Files.readString(Path.of(changelogPath));
        }

        var uploadData = new ModrinthUploadData(versionName, versionId, changelog, dependencies, gameVersions, versionType, loaders, projectId, List.of("primary_file"), "primary_file", true);

        String token;
        if (useTokenFromEnvironment) {
            token = System.getenv("MODRINTH_TOKEN");
            if (token == null) {
                throw new IllegalArgumentException("Token is not set in environment variable MODRINTH_TOKEN");
            }
        } else {
            var tokenBackend = TokenBackend.getDefault();
            token = tokenBackend.getToken(tokenSecretId);
            if (token == null) {
                throw new IllegalArgumentException("Token " + tokenSecretId + " not found");
            }
        }

        try (var httpClient = HttpClient.newHttpClient()) {
            var mapper = new ObjectMapper();
            var body = MultipartBodyPublisher.newBuilder()
                    .textPart("data", mapper.writeValueAsString(uploadData))
                    .formPart("primary_file", fileName, MoreBodyPublishers.ofMediaType(HttpRequest.BodyPublishers.ofFile(Path.of(filePath)), MediaType.APPLICATION_OCTET_STREAM))
                    .build();
            var request = HttpRequest.newBuilder(URI.create("https://api.modrinth.com/v2/version"))
                    .header("Authorization", token)
                    .header("User-Agent", "fifth_light/ArmorStand")
                    .header("Content-Type", body.mediaType().toString())
                    .POST(body)
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Upload failed: " + response.statusCode() + " " + response.body());
            }
        }
    }
}
