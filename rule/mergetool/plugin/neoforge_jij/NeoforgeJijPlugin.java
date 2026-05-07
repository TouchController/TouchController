package top.fifthlight.mergetools.merger.plugin.neoforgejij;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;
import top.fifthlight.mergetools.merger.plugin.jarinjar.JarInJarPlugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NeoforgeJijPlugin implements Plugin {
    private static final long DOS_EPOCH = 315532800000L;
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("([^:]+):([^:]+):([^:]+):([^:]*)");
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

    private final LinkedHashMap<String, NeoforgeEntry> neoforgeEntries = new LinkedHashMap<>();

    private record NeoforgeEntry(String group, String artifact, String version, String fmlType) {}

    @Override
    public int priority() {
        return 402;
    }

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        if ("--jij-neoforge".equals(arg)) {
            var id = environment.readNextArg();
            var description = environment.readNextArg();
            var matcher = DESCRIPTION_PATTERN.matcher(description);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Bad neoforge description: " + description);
            }
            neoforgeEntries.put(id, new NeoforgeEntry(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)));
            return true;
        }
        return false;
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {
        var context = environment.getAttribute(JarInJarPlugin.JIJ_CONTEXT);
        if (context == null) return;

        var metadataJars = new ArrayList<JijMetadata.Jar>();
        for (var entry : neoforgeEntries.entrySet()) {
            var id = entry.getKey();
            var neoforgeEntry = entry.getValue();
            var jiJEntry = context.entries().get(id);
            if (jiJEntry == null) continue;

            var existingEntry = mergeEntries.get(jiJEntry.entryPath());
            if (existingEntry != null) {
                try {
                    var wrappedBytes = buildWrappedJar(neoforgeEntry, jiJEntry.sourcePath());
                    mergeEntries.put(jiJEntry.entryPath(), new BytesMergeEntry(wrappedBytes));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            metadataJars.add(new JijMetadata.Jar(
                    new JijMetadata.Jar.Identifier(neoforgeEntry.group(), neoforgeEntry.artifact()),
                    new JijMetadata.Jar.Version("[" + neoforgeEntry.version() + ",)", neoforgeEntry.version()),
                    jiJEntry.entryPath(),
                    false));
        }

        if (!metadataJars.isEmpty()) {
            try {
                var metadataPath = context.basePath() + "metadata.json";
                var baos = new ByteArrayOutputStream();
                MAPPER.writeValue(baos, new JijMetadata(metadataJars));
                mergeEntries.put(metadataPath, new BytesMergeEntry(baos.toByteArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private byte[] buildWrappedJar(NeoforgeEntry neoforgeEntry, java.nio.file.Path sourcePath) throws IOException {
        var resultStream = new ByteArrayOutputStream();
        try (var entryInputStream = new JarInputStream(Files.newInputStream(sourcePath));
             var entryOutputStream = new ZipOutputStream(resultStream)) {
            var manifest = entryInputStream.getManifest();
            if (manifest == null) {
                manifest = new Manifest();
            }
            if (!neoforgeEntry.fmlType().isBlank()) {
                manifest.getMainAttributes().putValue("FMLModType", neoforgeEntry.fmlType());
            }

            var manifestEntry = new JarEntry("META-INF/MANIFEST.MF");
            setJarEntryTime(manifestEntry);
            entryOutputStream.putNextEntry(manifestEntry);
            manifest.write(entryOutputStream);
            entryOutputStream.closeEntry();

            JarEntry entry;
            while ((entry = entryInputStream.getNextJarEntry()) != null) {
                setJarEntryTime(entry);
                entryOutputStream.putNextEntry(entry);
                entryInputStream.transferTo(entryOutputStream);
                entryOutputStream.closeEntry();
            }
        }
        return resultStream.toByteArray();
    }

    private static void setJarEntryTime(ZipEntry entry) {
        entry.setCreationTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastAccessTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastModifiedTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setTimeLocal(LocalDateTime.ofEpochSecond(DOS_EPOCH / 1000, 0, ZoneOffset.UTC));
    }

    private record JijMetadata(@JsonProperty("jars") List<Jar> jars) {
        public record Jar(@JsonProperty("identifier") Identifier identifier,
                          @JsonProperty("version") Version version,
                          @JsonProperty("path") String path,
                          @JsonProperty("isObfuscated") Boolean isObfuscated) {
            public record Identifier(@JsonProperty("group") String group,
                                     @JsonProperty("artifact") String artifact) {}
            public record Version(@JsonProperty("range") String range,
                                  @JsonProperty("artifact") String artifactVersion) {}
        }
    }

    private record BytesMergeEntry(byte[] content) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            output.write(content);
        }
    }
}
