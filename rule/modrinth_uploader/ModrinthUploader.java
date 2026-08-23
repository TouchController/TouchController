package top.fifthlight.fabazel.modrinthuploader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.MoreBodyPublishers;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import top.fifthlight.fabazel.tokenhelper.TokenBackends;

import java.io.IOException;
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
        name = "ModrinthUploader",
        mixinStandardHelpOptions = true,
        description = "Upload Minecraft mod to Modrinth."
)
public class ModrinthUploader implements Callable<Integer> {
    public static final String userAgent = "fifth_light/TouchController";

    @CommandLine.Option(names = {"--use-token-from-environment"}, description = "Read token from environment variable MODRINTH_TOKEN")
    private boolean useTokenFromEnvironment;

    @CommandLine.Option(names = {"--token-secret-id"}, description = "API secret ID")
    private String tokenSecretId;

    @CommandLine.Option(names = {"--project-id"}, description = "Project ID", required = true)
    private String projectId;

    @CommandLine.Option(names = {"--version-name"}, description = "Version name", required = true)
    private String versionName;

    @CommandLine.Option(names = {"--version-id"}, description = "Version ID", required = true)
    private String versionId;

    @CommandLine.Option(names = {"--version-type"}, description = "Version type", required = true)
    private String versionType;

    @CommandLine.Option(names = {"--changelog"}, description = "Changelog file")
    private Path changelogFile;

    @CommandLine.Option(names = {"--game-version"}, description = "Game version", required = true)
    private List<String> gameVersions = new ArrayList<>();

    @CommandLine.Option(names = {"--loader"}, description = "Mod loader", required = true)
    private List<String> loaders = new ArrayList<>();

    @CommandLine.Option(names = {"--dependency"}, description = "Dependencies, in 'type:project-id[:version-id]' format")
    private List<String> dependencies = new ArrayList<>();

    @CommandLine.Option(names = {"--file-name"}, description = "Name of the uploaded file", required = true)
    private String fileName;

    @CommandLine.Parameters(index = "0", description = "File to be uploaded")
    private Path uploadFile;

    private static final Set<String> USABLE_VERSION_TYPES = Set.of("alpha", "beta", "release");

    @Override
    public Integer call() throws Exception {
        if (!USABLE_VERSION_TYPES.contains(versionType)) {
            throw new IllegalArgumentException("Invalid version type: " + versionType);
        }
        if (!useTokenFromEnvironment && tokenSecretId == null) {
            throw new IllegalArgumentException("tokenSecretId cannot be null");
        }
        if (gameVersions.isEmpty()) {
            throw new IllegalArgumentException("gameVersions cannot be empty");
        }
        if (loaders.isEmpty()) {
            throw new IllegalArgumentException("loaders cannot be empty");
        }

        String changelog = null;
        if (changelogFile != null) {
            changelog = Files.readString(changelogFile);
        }

        var parsedDependencies = dependencies.stream()
                .map(ModrinthUploadData.Dependency::parse)
                .toList();

        var uploadData = new ModrinthUploadData(versionName, versionId, changelog, parsedDependencies, gameVersions, versionType, loaders, projectId, List.of("primary_file"), "primary_file", true);

        String token;
        if (useTokenFromEnvironment) {
            token = System.getenv("MODRINTH_TOKEN");
            if (token == null) {
                throw new IllegalArgumentException("Token is not set in environment variable MODRINTH_TOKEN");
            }
        } else {
            var tokenBackend = TokenBackends.getDefault();
            token = tokenBackend.getToken(tokenSecretId);
            if (token == null) {
                throw new IllegalArgumentException("Token " + tokenSecretId + " not found");
            }
        }

        try (var httpClient = HttpClient.newHttpClient()) {
            var mapper = new ObjectMapper();
            var body = MultipartBodyPublisher.newBuilder()
                    .textPart("data", mapper.writeValueAsString(uploadData))
                    .formPart("primary_file", fileName, MoreBodyPublishers.ofMediaType(HttpRequest.BodyPublishers.ofFile(uploadFile), MediaType.APPLICATION_OCTET_STREAM))
                    .build();
            var request = HttpRequest.newBuilder(URI.create("https://api.modrinth.com/v2/version"))
                    .header("Authorization", token)
                    .header("User-Agent", userAgent)
                    .header("Content-Type", body.mediaType().toString())
                    .POST(body)
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Upload failed: " + response.statusCode() + " " + response.body());
            }
        }

        return 0;
    }

    public static void main(String... args) {
        var root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        var exitCode = new CommandLine(new ModrinthUploader()).execute(args);
        System.exit(exitCode);
    }
}
