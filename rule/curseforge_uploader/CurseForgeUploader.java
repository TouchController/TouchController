package top.fifthlight.fabazel.curseforge;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import top.fifthlight.fabazel.tokenhelper.TokenBackends;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "CurseForgeUploader",
        mixinStandardHelpOptions = true,
        description = "Upload Minecraft mod to CurseForge."
)
public class CurseForgeUploader implements Callable<Integer> {
    public static final String userAgent = "fifth_light/TouchController";

    @CommandLine.Option(names = {"--use-token-from-environment"}, description = "Read token from environment variable CURSEFORGE_TOKEN")
    private boolean useTokenFromEnvironment;

    @CommandLine.Option(names = {"--project-id"}, description = "Project ID", required = true)
    private int projectId;

    @CommandLine.Option(names = {"--changelog"}, description = "Changelog file", required = true)
    private Path changelogFile;

    @CommandLine.Option(names = {"--changelog-type"}, description = "Changelog format", defaultValue = "text")
    private String changelogType;

    @CommandLine.Option(names = {"--display-name"}, description = "Display name")
    private String displayName;

    @CommandLine.Option(names = {"--game-version"}, description = "Game version, in 'type:version' format", required = true)
    private List<String> gameVersions = new ArrayList<>();

    @CommandLine.Option(names = {"--release-type"}, description = "Release type", required = true)
    private String releaseType;

    @CommandLine.Option(names = {"--marked-for-manual-release"}, description = "Marked as manual release", defaultValue = "false")
    private boolean manualRelease;

    @CommandLine.Option(names = {"--dependency"}, description = "Dependencies, in 'slug:id:type' format")
    private List<String> dependencies = new ArrayList<>();

    @CommandLine.Option(names = {"--file"}, description = "File to be uploaded", required = true)
    private Path uploadFile;

    @CommandLine.Option(names = {"--token-secret-id"}, description = "API secret ID")
    private String tokenSecretId;

    private static final Set<String> USABLE_CHANGELOG_TYPES = Set.of("text", "html", "markdown");
    private static final Set<String> USABLE_RELEASE_TYPES = Set.of("alpha", "beta", "release");

    @Override
    public Integer call() throws Exception {
        var objectMapper = new ObjectMapper();

        if (!USABLE_CHANGELOG_TYPES.contains(changelogType)) {
            throw new IllegalArgumentException("Invalid changelog type: " + changelogType);
        }
        if (!USABLE_RELEASE_TYPES.contains(releaseType)) {
            throw new IllegalArgumentException("Invalid release type: " + releaseType);
        }
        if (!useTokenFromEnvironment && tokenSecretId == null) {
            throw new IllegalArgumentException("tokenSecretId cannot be null");
        }

        var changelog = Files.readString(changelogFile);

        // Acquire token to access version list
        String token;
        if (useTokenFromEnvironment) {
            token = System.getenv("CURSEFORGE_TOKEN");
            if (token == null) {
                throw new IllegalArgumentException("Token is not set in environment variable CURSEFORGE_TOKEN");
            }
        } else {
            var tokenBackend = TokenBackends.getDefault();
            token = tokenBackend.getToken(tokenSecretId);
            if (token == null) {
                throw new IllegalArgumentException("Token " + tokenSecretId + " not found");
            }
        }

        try (var client = HttpClient.newHttpClient()) {
            var versionsResolver = new CurseForgeVersionsResolver(client, objectMapper, token);
            var gameVersionIds = gameVersions.stream()
                    .map(gameVersion -> {
                        var colonIndex = gameVersion.indexOf(':');
                        if (colonIndex == -1) {
                            throw new IllegalArgumentException("Invalid game version: " + gameVersion);
                        }
                        var type = gameVersion.substring(0, colonIndex);
                        var version = gameVersion.substring(colonIndex + 1);
                        return versionsResolver.resolve(type, version).orElseThrow(() -> new IllegalArgumentException("Game version " + gameVersion + " not found"));
                    });

            var uploadMetadata = new UploadMetadata(
                    changelog,
                    changelogType,
                    displayName,
                    null,
                    gameVersionIds.toList(),
                    releaseType,
                    manualRelease,
                    new UploadMetadata.Relations(
                            dependencies.stream()
                                    .map(dependency -> {
                                        var parts = dependency.split(":");
                                        if (parts.length != 3) {
                                            throw new IllegalArgumentException("Invalid dependency: " + dependency);
                                        }
                                        var slug = parts[0];
                                        var id = Integer.parseInt(parts[1]);
                                        var type = parts[2];
                                        return new UploadMetadata.ProjectRelation(slug, id, type);
                                    })
                                    .toList())
            );

            var jsonPayload = objectMapper.writeValueAsString(uploadMetadata);

            var bodyPublisher = MultipartBodyPublisher.newBuilder()
                    .formPart("metadata", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .filePart("file", uploadFile, MediaType.APPLICATION_OCTET_STREAM)
                    .build();

            var uploadRequest = HttpRequest.newBuilder(URI.create("https://curseforge.com/api/projects/" + projectId + "/upload-file"))
                    .header("X-Api-Token", token)
                    .header("User-Agent", CurseForgeUploader.userAgent)
                    .header("Accept", "application/json")
                    .POST(bodyPublisher)
                    .build();

            var uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
            if (uploadResponse.statusCode() != 200) {
                throw new RuntimeException("Upload failed: " + uploadResponse.statusCode() + " " + uploadResponse.body());
            }

            var response = objectMapper.readValue(uploadResponse.body(), UploadResponse.class);
            System.err.println("Upload successful: " + response.id());
        }

        return 0;
    }

    public static void main(String... args) {
        var root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        var exitCode = new CommandLine(new CurseForgeUploader()).execute(args);
        System.exit(exitCode);
    }
}
