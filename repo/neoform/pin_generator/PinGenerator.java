import com.google.devtools.build.runfiles.AutoBazelRepository;
import com.google.devtools.build.runfiles.Runfiles;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@AutoBazelRepository
public class PinGenerator {
    private record HashEntry(String url, String hash) {
    }

    public static void main(String[] args) throws Exception {
        var runfiles = Runfiles.preload().withSourceRepository(AutoBazelRepository_PinGenerator.NAME);

        var manifestPath = Path.of(runfiles.rlocation(System.getProperty("pin.manifest")));
        var targetPath = Path.of(System.getProperty("pin.target"));

        Map<String, String> manifest;
        try (var reader = new InputStreamReader(Files.newInputStream(manifestPath), StandardCharsets.UTF_8)) {
            manifest = new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
        }

        List<HashEntry> entries = new ArrayList<>();
        for (var entry : manifest.entrySet()) {
            var url = entry.getKey();
            var flagName = entry.getValue();
            var rpath = System.getProperty(flagName);
            var file = Path.of(runfiles.rlocation(rpath));

            var digest = MessageDigest.getInstance("SHA-256");
            try (var input = Files.newInputStream(file)) {
                byte[] buffer = new byte[1 << 16];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            var hash = digest.digest();
            var hex = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                hex.append(String.format("%02x", value));
            }
            entries.add(new HashEntry(url, hex.toString()));
        }

        entries.sort(Comparator.comparing(HashEntry::url));

        if (targetPath.getParent() != null) {
            Files.createDirectories(targetPath.getParent());
        }
        try (var output = Files.newBufferedWriter(targetPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (var entry : entries) {
                output.write(entry.url);
                output.write(' ');
                output.write(entry.hash);
                output.newLine();
            }
        }

        System.err.println("Pin generated successfully: " + targetPath);
    }
}
