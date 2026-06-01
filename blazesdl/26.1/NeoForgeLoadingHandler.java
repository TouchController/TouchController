package top.fifthlight.blazesdl;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.readers.JarModsDotTomlModFileReader;
import net.neoforged.neoforgespi.locating.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class NeoForgeLoadingHandler implements IDependencyLocator, IModFileReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(NeoForgeLoadingHandler.class);
    private static final Gson GSON = new Gson();
    private static final String MANIFEST_PATH = "fabric.mod.json";
    private final ModFileDiscoveryAttributes attributes = ModFileDiscoveryAttributes.DEFAULT.withDependencyLocator(this);

    // Copied from ClasspathResourceUtils
    private static List<Path> findFileSystemRootsOfFileOnClasspath(String relativePath) {
        var classLoader = Thread.currentThread().getContextClassLoader();
        if (IDependencyLocator.class.getModule().isNamed()) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        return findFileSystemRootsOfFileOnClasspath(classLoader, relativePath);
    }

    private static List<Path> findFileSystemRootsOfFileOnClasspath(ClassLoader classLoader, String relativePath) {
        Iterator<URL> resourceIt;
        try {
            resourceIt = classLoader.getResources(relativePath).asIterator();
        } catch (IOException var5) {
            throw new IllegalArgumentException("Failed to enumerate classpath locations of " + relativePath);
        }

        var result = new LinkedHashSet<Path>();

        while (resourceIt.hasNext()) {
            var resourceUrl = resourceIt.next();
            result.add(getRootFromResourceUrl(relativePath, resourceUrl));
        }

        return new ArrayList<>(result);
    }

    public static Path getRootFromResourceUrl(String relativePath, URL resourceUrl) {
        if ("jar".equals(resourceUrl.getProtocol())) {
            var fileUri = URI.create(resourceUrl.toString().split("!")[0].substring("jar:".length()));
            return Paths.get(fileUri);
        } else {
            Path resourcePath;
            try {
                resourcePath = Paths.get(resourceUrl.toURI());
            } catch (URISyntaxException var4) {
                throw new IllegalArgumentException("Failed to convert " + resourceUrl + " to URI");
            }

            var current = Paths.get(relativePath);

            while (current != null) {
                current = current.getParent();
                resourcePath = resourcePath.getParent();
                if (resourcePath == null) {
                    throw new IllegalArgumentException("Resource " + resourceUrl + " did not have same nesting depth as " + relativePath);
                }
            }

            return resourcePath;
        }
    }

    private static String extractEmbeddedJarFile(JarContents contents, String relativePath, Path destination) {
        try (var inStream = contents.openFile(relativePath); var outStream = Files.newOutputStream(destination)) {
            if (inStream == null) {
                throw new IOException("Mod file does not contain " + relativePath);
            }

            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            var digestOut = new DigestOutputStream(outStream, digest);
            inStream.transferTo(digestOut);

            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException e) {
            LOGGER.error("Failed to copy JiJ file {} to {}", relativePath, destination, e);
            throw new UncheckedIOException(e);
        }
    }

    private static void moveExtractedFileIntoPlace(Path source, Path destination) {
        try {
            Files.createDirectories(destination.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try {
            try {
                Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void scanMods(List<IModFile> loadedMods, IDiscoveryPipeline pipeline) {
        if (!FMLEnvironment.isProduction()) {
            for (var path : findFileSystemRootsOfFileOnClasspath(MANIFEST_PATH)) {
                if (!Files.isRegularFile(path)) continue;
                try (var contents = JarContents.ofPath(path)) {
                    processJar(contents, pipeline);
                } catch (IOException e) {
                    LOGGER.warn("Failed to read mod {} in classpath", path);
                }
            }
        }

        var gameDirectory = FMLPaths.GAMEDIR.get();
        var modsDir = gameDirectory.resolve(FMLPaths.MODSDIR.relative()).toAbsolutePath().normalize();
        if (!Files.exists(modsDir)) {
            return;
        }

        List<Path> jarFiles;
        try (var files = Files.list(modsDir)) {
            jarFiles = files
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();
        } catch (IOException e) {
            LOGGER.warn("Failed to list mods directory {}", modsDir, e);
            return;
        }

        for (var path : jarFiles) {
            if (!Files.isRegularFile(path)) continue;
            try {
                if (Files.size(path) == 0) continue;
            } catch (IOException ignored) {
            }

            try {
                processJar(JarContents.ofPath(path), pipeline);
            } catch (IOException e) {
                LOGGER.warn("Failed to read mod {}", path);
            }
        }
    }

    @Nullable
    private String readJijEntry(JsonReader reader) throws IOException {
        String file = null;
        reader.beginObject();
        outer:
        while (true) {
            var token = reader.peek();
            switch (token) {
                case NAME -> {
                    var name = reader.nextName();
                    if (name.equals("file")) {
                        file = reader.nextString();
                    } else {
                        reader.skipValue();
                    }
                }

                case END_OBJECT -> {
                    break outer;
                }
                default -> throw new IllegalStateException("Unexpected token: " + token);
            }
        }
        reader.endObject();
        return file;
    }

    private ArrayList<String> readJijEntries(JsonReader reader) throws IOException {
        var jars = new ArrayList<String>();
        reader.beginArray();
        jars:
        while (true) {
            var token = reader.peek();
            switch (token) {
                case BEGIN_OBJECT -> {
                    var entry = readJijEntry(reader);
                    if (entry != null) {
                        jars.add(entry);
                    }
                }
                case END_ARRAY -> {
                    break jars;
                }
                default -> throw new IllegalStateException("Unexpected token: " + token);
            }
        }
        reader.endArray();
        return jars;
    }

    private void processJar(JarContents contents, IDiscoveryPipeline pipeline) throws IOException {
        var manifestStream = contents.openFile(MANIFEST_PATH);
        if (manifestStream == null) {
            return;
        }

        String modId = null;
        ArrayList<String> jars = null;
        try (var reader = GSON.newJsonReader(new InputStreamReader(manifestStream))) {
            reader.beginObject();
            outer:
            while (true) {
                var token = reader.peek();
                switch (token) {
                    case NAME -> {
                        var name = reader.nextName();
                        switch (name) {
                            case "id" -> modId = reader.nextString();
                            case "jars" -> jars = readJijEntries(reader);
                            default -> reader.skipValue();
                        }
                    }

                    case END_OBJECT -> {
                        break outer;
                    }

                    default -> throw new IllegalStateException("Unexpected token: " + token);
                }
            }
            reader.endObject();
        } catch (Exception e) {
            LOGGER.debug("Failed to process manifest", e);
        }

        if (modId == null) {
            LOGGER.debug("Mod file {} does not contain id", contents.getPrimaryPath());
            return;
        }

        if (!"blazesdl".equals(modId)) {
            return;
        }

        pipeline.addModFile(pipeline.readModFile(contents, attributes));

        if (jars != null) {
            for (var jar : jars) {
                var jij = contents.findFile(jar);
                if (jij.isEmpty()) {
                    LOGGER.warn("Failed to find jar {} for mod {}", jar, contents.getPrimaryPath());
                    continue;
                }
                LOGGER.debug("Loading jar {} for mod {}", jar, contents.getPrimaryPath());

                var jijCacheDir = FMLPaths.JIJ_CACHEDIR.get();
                Path tempFile;
                try {
                    tempFile = Files.createTempFile(jijCacheDir, "_jij", ".tmp");
                } catch (IOException e) {
                    LOGGER.error("Failed to create temp file in {}: {}", jijCacheDir, e);
                    continue;
                }

                var filename = jar.substring(jar.lastIndexOf('/') + 1);
                Path finalPath;
                try {
                    var checksum = extractEmbeddedJarFile(contents, jar, tempFile);
                    finalPath = jijCacheDir.resolve(checksum + "/" + filename);
                    if (!Files.isRegularFile(finalPath)) {
                        moveExtractedFileIntoPlace(tempFile, finalPath);
                    }
                } finally {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException e) {
                        LOGGER.error("Failed to remove temp file {}: {}", tempFile, e);
                    }
                }

                JarContents jijContents;
                try {
                    jijContents = JarContents.ofPath(finalPath);
                } catch (IOException e) {
                    LOGGER.error("Failed to read JiJ file {} from mod {} to {}", jar, contents.getPrimaryPath(), finalPath, e);
                    continue;
                }
                var jijModFile = pipeline.readModFile(jijContents, attributes);
                pipeline.addModFile(jijModFile);
            }
        }
    }

    @Nullable
    @Override
    public IModFile read(JarContents jar, ModFileDiscoveryAttributes discoveryAttributes) {
        if (discoveryAttributes.dependencyLocator() instanceof NeoForgeLoadingHandler) {
            var modFile = JarModsDotTomlModFileReader.createModFile(jar, discoveryAttributes);
            if (modFile != null) {
                return modFile;
            }
            return IModFile.create(jar, JarModsDotTomlModFileReader::manifestParser, IModFile.Type.LIBRARY, discoveryAttributes);
        }
        return null;
    }
}
