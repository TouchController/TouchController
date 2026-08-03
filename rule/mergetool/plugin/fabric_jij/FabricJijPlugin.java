package top.fifthlight.mergetools.merger.plugin.fabricjij;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;
import top.fifthlight.mergetools.merger.plugin.jarinjar.JarInJarPlugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class FabricJijPlugin implements Plugin {
    private static final long DOS_EPOCH = 315532800000L;
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    private static final String FABRIC_MOD_JSON = "fabric.mod.json";

    private final TreeMap<String, String> fabricEntries = new TreeMap<>();

    @Override
    public int priority() {
        return 401;
    }

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        if ("--jij-fabric".equals(arg)) {
            var id = environment.readNextArg();
            var version = environment.readNextArg();
            fabricEntries.put(id, version);
            return true;
        }
        return false;
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {
        var context = environment.getAttribute(JarInJarPlugin.JIJ_CONTEXT);
        if (context == null) return;

        for (var entry : fabricEntries.entrySet()) {
            var id = entry.getKey();
            var version = entry.getValue();
            var jiJEntry = context.entries().get(id);
            if (jiJEntry == null) continue;

            if (!version.equals("=")) {
                var existingEntry = mergeEntries.get(jiJEntry.entryPath());
                if (existingEntry != null) {
                    mergeEntries.put(jiJEntry.entryPath(), new WrappedJarMergeEntry(id, version, existingEntry));
                }
            }
        }

        var existing = mergeEntries.get(FABRIC_MOD_JSON);
        if (existing != null) {
            var jarPaths = new ArrayList<String>();
            for (var id : fabricEntries.keySet()) {
                var jiJEntry = context.entries().get(id);
                if (jiJEntry != null) {
                    jarPaths.add(jiJEntry.entryPath());
                }
            }
            mergeEntries.put(FABRIC_MOD_JSON, new FabricModJsonMergeEntry(existing, jarPaths));
        }
    }

    private static byte[] buildWrappedJar(String id, String version, MergeEntry originalEntry) throws IOException {
        var originalBytes = readMergeEntry(originalEntry);
        var resultStream = new ByteArrayOutputStream();
        try (var jos = new JarOutputStream(resultStream)) {
            var fabricModEntry = new JarEntry(FABRIC_MOD_JSON);
            setJarEntryTime(fabricModEntry);
            jos.putNextEntry(fabricModEntry);
            var modJson = MAPPER.createObjectNode();
            modJson.put("schemaVersion", 1);
            modJson.put("id", id);
            modJson.put("version", version);
            modJson.put("name", id);
            var custom = MAPPER.createObjectNode();
            custom.put("fabric-loom:generated", true);
            modJson.set("custom", custom);
            jos.write(MAPPER.writeValueAsBytes(modJson));
            jos.closeEntry();

            try (var jis = new JarInputStream(new ByteArrayInputStream(originalBytes))) {
                JarEntry entry;
                while ((entry = jis.getNextJarEntry()) != null) {
                    setJarEntryTime(entry);
                    jos.putNextEntry(entry);
                    jis.transferTo(jos);
                    jos.closeEntry();
                    jis.closeEntry();
                }
            }
        }
        return resultStream.toByteArray();
    }

    private static byte[] readMergeEntry(MergeEntry entry) throws IOException {
        var baos = new ByteArrayOutputStream();
        try {
            entry.write(baos);
        } catch (Exception e) {
            throw new IOException(e);
        }
        return baos.toByteArray();
    }

    private static void setJarEntryTime(ZipEntry entry) {
        entry.setCreationTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastAccessTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastModifiedTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setTimeLocal(LocalDateTime.ofEpochSecond(DOS_EPOCH / 1000, 0, ZoneOffset.UTC));
    }

    private record WrappedJarMergeEntry(String id, String version, MergeEntry originalEntry) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            output.write(buildWrappedJar(id, version, originalEntry));
        }
    }

    private record FabricModJsonMergeEntry(MergeEntry originalEntry, List<String> jarPaths) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            var originalBytes = readMergeEntry(originalEntry);
            var rootNode = (ObjectNode) MAPPER.readTree(originalBytes);
            var jarsArray = MAPPER.createArrayNode();
            for (var path : jarPaths) {
                var jarObj = MAPPER.createObjectNode();
                jarObj.put("file", path);
                jarsArray.add(jarObj);
            }
            rootNode.set("jars", jarsArray);
            MAPPER.writeValue(output, rootNode);
        }
    }
}
