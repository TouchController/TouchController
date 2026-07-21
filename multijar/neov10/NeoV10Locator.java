package top.fifthlight.multijar.neov10;

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IDependencyLocator;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.fifthlight.multijar.common.ComparableMultiJarManifest;
import top.fifthlight.multijar.common.JarItem;
import top.fifthlight.multijar.common.MultiJarManifest;
import top.fifthlight.multijar.common.maven385.MavenVersionFactory;
import top.fifthlight.multijar.common.maven385.MavenVersionItem;
import top.fifthlight.multijar.common.maven385.MavenVersionRange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

public class NeoV10Locator implements IDependencyLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NeoV10Locator.class);
    private final ModFileDiscoveryAttributes attributes;

    @SuppressWarnings("ReturnValueIgnored")
    public NeoV10Locator() throws NoSuchMethodException, NoSuchFieldException {
        IDependencyLocator.class.getMethod("scanMods", List.class, IDiscoveryPipeline.class);
        IModFile.class.getMethod("getModInfos");
        IModInfo.class.getMethod("getModId");
        IModInfo.class.getMethod("getVersion");
        JarContents.class.getMethod("ofPaths", Collection.class);
        JarContents.class.getMethod("findFile", String.class);
        IDiscoveryPipeline.class.getMethod("readModFile", JarContents.class, ModFileDiscoveryAttributes.class);
        IDiscoveryPipeline.class.getMethod("addModFile", IModFile.class);
        ModFileDiscoveryAttributes.class.getField("DEFAULT");

        attributes = ModFileDiscoveryAttributes.DEFAULT.withDependencyLocator(this);
    }

    @Override
    public void scanMods(List<IModFile> loadedMods, IDiscoveryPipeline pipeline) {
        var modVersions = new HashMap<String, MavenVersionItem>();
        for (var modFile : loadedMods) {
            for (var modInfo : modFile.getModInfos()) {
                modVersions.put(modInfo.getModId(), new MavenVersionItem(modInfo.getVersion()));
            }
        }
        Function<String, Optional<MavenVersionItem>> modVersionProvider = modid -> Optional.ofNullable(modVersions.get(modid));

        var minecraftVersion = modVersions.get("minecraft");
        if (minecraftVersion == null) {
            LOGGER.error("Could not find minecraft mod!");
            return;
        }

        LOGGER.info("MultiJar loader on Minecraft {}", minecraftVersion);

        try {
            var jarPath = Path.of(NeoV10Locator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            try (var content = JarContents.ofPath(jarPath)) {
                processJar(content, modVersionProvider, pipeline);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to open mod JAR", ex);
        }
    }

    private void processJar(
            JarContents contents,
            Function<String, Optional<MavenVersionItem>> modVersionProvider,
            IDiscoveryPipeline pipeline
    ) throws IOException {
        var manifestStream = contents.openFile(MultiJarManifest.NEOFORGE_MANIFEST_PATH);
        if (manifestStream == null) {
            return;
        }

        ComparableMultiJarManifest<MavenVersionItem, MavenVersionRange> manifest;
        try (var reader = new BufferedReader(new InputStreamReader(manifestStream))) {
            manifest = ComparableMultiJarManifest.fromJson(reader, MavenVersionFactory.INSTANCE);
        } catch (Exception e) {
            LOGGER.warn("Failed to parse loader manifest for mod {}", contents.getPrimaryPath(), e);
            return;
        }

        LOGGER.info("Loading mod {}", contents.getPrimaryPath());

        item:
        for (JarItem item : manifest.items(modVersionProvider)) {
            var jijPaths = new ArrayList<Path>();
            for (var path : item.jarPaths()) {
                var jij = contents.findFile(path);
                if (jij.isEmpty()) {
                    LOGGER.warn("Failed to find jar {} for mod {}", path, contents.getPrimaryPath());
                    continue item;
                }

                var jijCacheDir = FMLPaths.JIJ_CACHEDIR.get();
                Path tempFile;
                try {
                    tempFile = Files.createTempFile(jijCacheDir, "_jij", ".tmp");
                } catch (IOException e) {
                    LOGGER.error("Failed to create temp file in {}: {}", jijCacheDir, e);
                    continue item;
                }

                var filename = path.substring(path.lastIndexOf('/') + 1);
                Path finalPath;
                try {
                    var checksum = extractEmbeddedJarFile(contents, path, tempFile);
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
                jijPaths.add(finalPath);
            }
            LOGGER.info("Loading jar {} for mod {}", jijPaths, contents.getPrimaryPath());

            JarContents jijContents;
            try {
                jijContents = JarContents.ofPaths(jijPaths);
            } catch (IOException e) {
                LOGGER.error("Failed to read JiJ file {}", jijPaths, e);
                continue;
            }
            var jijModFile = pipeline.readModFile(jijContents, attributes);
            pipeline.addModFile(jijModFile);
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
    public String toString() {
        return "MultiJar NeoV10";
    }
}
