package top.fifthlight.fabazel.curseforge;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class CurseForgeVersionsResolver {
    private final HashMap<String, VersionTypeEntry> versionTypeEntries = new HashMap<>();
    private final List<VersionEntry> versionEntries = new ArrayList<>();

    public CurseForgeVersionsResolver(HttpClient client, ObjectMapper mapper, String token) throws IOException, InterruptedException {
        var versionTypesRequest = HttpRequest.newBuilder(URI.create("https://minecraft.curseforge.com/api/game/version-types"))
                .GET()
                .header("X-Api-Token", token)
                .header("User-Agent", CurseForgeUploader.userAgent)
                .build();
        var versionTypesResponse = client.send(versionTypesRequest, HttpResponse.BodyHandlers.ofInputStream());
        if (versionTypesResponse.statusCode() != 200) {
            throw new IOException("Failed to fetch version types: " + versionTypesResponse.statusCode());
        }
        var versionTypes = mapper.readValue(versionTypesResponse.body(), new TypeReference<List<VersionTypeEntry>>() {});

        var versionsRequest = HttpRequest.newBuilder(URI.create("https://minecraft.curseforge.com/api/game/versions"))
                .GET()
                .header("X-Api-Token", token)
                .header("User-Agent", CurseForgeUploader.userAgent)
                .build();
        var versionsResponse = client.send(versionsRequest, HttpResponse.BodyHandlers.ofInputStream());
        if (versionsResponse.statusCode() != 200) {
            throw new IOException("Failed to fetch versions: " + versionsResponse.statusCode());
        }
        var versions = mapper.readValue(versionsResponse.body(), new TypeReference<List<VersionEntry>>() {});
        initialize(versionTypes, versions);
    }

    public CurseForgeVersionsResolver(List<VersionTypeEntry> versionTypeEntries, List<VersionEntry> versionEntries) {
        initialize(versionTypeEntries, versionEntries);
    }

    private void initialize(List<VersionTypeEntry> versionTypeEntries, List<VersionEntry> versionEntries) {
        versionTypeEntries.forEach(entry -> this.versionTypeEntries.put(entry.slug(), entry));
        this.versionEntries.addAll(versionEntries);
    }

    public Optional<Integer> resolve(@NonNull String type, @NonNull String version) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(version);

        var typeEntry = this.versionTypeEntries.get(type);
        if (typeEntry == null) {
            return Optional.empty();
        }
        for (var entry : this.versionEntries) {
            if (entry.gameVersionTypeId() == typeEntry.id() && entry.slug().equals(version)) {
                return Optional.of(entry.id());
            }
        }
        return Optional.empty();
    }
}
