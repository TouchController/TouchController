package top.fifthlight.multijar.neov3;

import cpw.mods.jarhandling.JarContents;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.DevEnvUtils;
import net.neoforged.jarjar.nio.layzip.LayeredZipFileSystemProvider;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class NeoV3Locator implements IDependencyLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NeoV3Locator.class);
    private final ModFileDiscoveryAttributes attributes;

    @SuppressWarnings("ReturnValueIgnored")
    public NeoV3Locator() throws NoSuchMethodException, NoSuchFieldException {
        IDependencyLocator.class.getMethod("scanMods", List.class, IDiscoveryPipeline.class);
        IModFile.class.getMethod("findResource", String[].class);
        IModFile.class.getMethod("getSecureJar");
        IModFile.class.getMethod("getModInfos");
        IModInfo.class.getMethod("getModId");
        IModInfo.class.getMethod("getVersion");
        JarContents.class.getMethod("of", Collection.class);
        JarContents.class.getMethod("findFile", String.class);
        IDiscoveryPipeline.class.getMethod("readModFile", JarContents.class, ModFileDiscoveryAttributes.class);
        IDiscoveryPipeline.class.getMethod("addModFile", IModFile.class);
        ModFileDiscoveryAttributes.class.getField("DEFAULT");
        LayeredZipFileSystemProvider.class.getField("SCHEME");

        attributes = ModFileDiscoveryAttributes.DEFAULT.withDependencyLocator(this);
    }

    private void processJar(
            JarContents contents,
            Function<String, Optional<MavenVersionItem>> modVersionProvider,
            IDiscoveryPipeline pipeline
    ) throws IOException {
        var manifestUri = contents.findFile(MultiJarManifest.NEOFORGE_MANIFEST_PATH);
        if (manifestUri.isEmpty()) {
            return;
        }
        var manifestPath = Path.of(manifestUri.get());

        ComparableMultiJarManifest<MavenVersionItem, MavenVersionRange> manifest;
        try (var reader = Files.newBufferedReader(manifestPath)) {
            manifest = ComparableMultiJarManifest.fromJson(reader, MavenVersionFactory.INSTANCE);
        } catch (FileNotFoundException | NoSuchFileException e) {
            return;
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
                    LOGGER.warn("Failed to find item {} for mod {}", item, contents.getPrimaryPath());
                    continue item;
                }
                jijPaths.add(Path.of(jij.get()));
            }
            LOGGER.info("Loading item {} for mod {}", jijPaths, contents.getPrimaryPath());
            var jijModFile = pipeline.readModFile(JarContents.of(jijPaths), attributes);
            pipeline.addModFile(jijModFile);
        }
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

        var gameDirectory = FMLPaths.GAMEDIR.get();
        LOGGER.info("MultiJar loader on Minecraft {} in directory {}", minecraftVersion, gameDirectory);

        if (!FMLEnvironment.production) {
            for (var path : DevEnvUtils.findFileSystemRootsOfFileOnClasspath(
                    MultiJarManifest.NEOFORGE_MANIFEST_PATH)) {
                if (!Files.isRegularFile(path)) continue;
                try (var contents = JarContents.of(path)) {
                    processJar(contents, modVersionProvider, pipeline);
                } catch (IOException e) {
                    LOGGER.warn("Failed to read mod {} in classpath", path);
                }
            }
        }

        var modsDir = gameDirectory.resolve(FMLPaths.MODSDIR.relative()).toAbsolutePath().normalize();
        if (!Files.exists(modsDir)) {
            // Skip if the mods dir doesn't exist yet.
            return;
        }

        try (var walk = Files.walk(modsDir, 1)) {
            walk.forEach(path -> {
                if (!Files.isRegularFile(path)) return;
                if (!path.toString().endsWith(".jar")) return;
                try {
                    if (Files.size(path) == 0) return;
                } catch (IOException ignored) {}

                try (var contents = JarContents.of(path)) {
                    processJar(contents, modVersionProvider, pipeline);
                } catch (IOException e) {
                    LOGGER.warn("Failed to read mod {}", path);
                }

            });
        } catch (IOException ex) {
            LOGGER.warn("Failed to scan mods directory {}", modsDir);
        }
    }

    @Override
    public String toString() {
        return "MultiJar NeoV3";
    }
}
