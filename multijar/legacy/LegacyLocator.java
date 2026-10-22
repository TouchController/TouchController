/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.multijar.legacy;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileDependencyLocator;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModProvider;
import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IDependencyLocator;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.forgespi.locating.IModProvider;
import net.minecraftforge.jarjar.nio.layzip.LayeredZipFileSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.fifthlight.multijar.common.ComparableMultiJarManifest;
import top.fifthlight.multijar.common.JarItem;
import top.fifthlight.multijar.common.MultiJarManifest;
import top.fifthlight.multijar.common.maven385.MavenVersionFactory;
import top.fifthlight.multijar.common.maven385.MavenVersionItem;
import top.fifthlight.multijar.common.maven385.MavenVersionRange;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class LegacyLocator extends AbstractJarFileDependencyLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyLocator.class);

    @SuppressWarnings("ReturnValueIgnored")
    public LegacyLocator() throws NoSuchMethodException, NoSuchFieldException {
        IDependencyLocator.class.getMethod("scanMods", Iterable.class);
        IModProvider.class.getMethod("name");
        IModProvider.class.getMethod("isValid", IModFile.class);
        IModProvider.class.getMethod("initArguments", Map.class);
        IModProvider.class.getMethod("scanFile", IModFile.class, Consumer.class);
        IModFile.class.getMethod("getModInfos");
        IModFile.class.getMethod("getFileName");
        IModInfo.class.getMethod("getModId");
        IModInfo.class.getMethod("getVersion");
        IModLocator.ModFileOrException.class.getMethod("file");
        IModLocator.ModFileOrException.class.getMethod("ex");
        AbstractModProvider.class.getDeclaredMethod("createMod", Path[].class);
        AbstractModProvider.class.getDeclaredMethod("getDefaultJarModType");
        AbstractJarFileModProvider.class.getMethod("scanFile", IModFile.class, Consumer.class);
        SecureJar.class.getMethod("getPath", String.class, String[].class);
        LayeredZipFileSystemProvider.class.getField("SCHEME");
    }

    @Override
    public String name() {
        return "MultiJar Legacy";
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
    }

    @Override
    public List<IModFile> scanMods(Iterable<IModFile> loadedMods) {
        var modVersions = new HashMap<String, MavenVersionItem>();
        for (var modFile : loadedMods) {
            if (modFile.getModFileInfo() == null) {
                continue;
            }
            for (IModInfo modInfo : modFile.getModInfos()) {
                modVersions.put(modInfo.getModId(), new MavenVersionItem(modInfo.getVersion()));
            }
        }
        Function<String, Optional<MavenVersionItem>> modVersionProvider =
                modId -> Optional.ofNullable(modVersions.get(modId));

        Path loaderJarPath;
        try {
            loaderJarPath = Path.of(LegacyLocator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            LOGGER.error("Failed to locate MultiJar loader JAR", e);
            return List.of();
        }

        SecureJar loaderJar;
        try {
            loaderJar = SecureJar.from(loaderJarPath);
        } catch (Exception e) {
            LOGGER.error("Failed to open MultiJar loader JAR {}", loaderJarPath, e);
            return List.of();
        }

        var manifestPath = loaderJar.getPath(MultiJarManifest.FORGE_MANIFEST_PATH);
        if (!Files.exists(manifestPath)) {
            return List.of();
        }

        ComparableMultiJarManifest<MavenVersionItem, MavenVersionRange> manifest;
        try (var reader = Files.newBufferedReader(manifestPath)) {
            manifest = ComparableMultiJarManifest.fromJson(reader, MavenVersionFactory.INSTANCE);
        } catch (Exception e) {
            LOGGER.warn("Failed to parse MultiJar manifest in {}", loaderJarPath, e);
            return List.of();
        }

        var result = new ArrayList<IModFile>();
        item:
        for (JarItem item : manifest.items(modVersionProvider)) {
            var roots = new ArrayList<Path>();
            for (var jarPath : item.jarPaths()) {
                var nestedJarPath = loaderJar.getPath(jarPath);
                if (!Files.exists(nestedJarPath)) {
                    LOGGER.warn("Failed to find Jar {} in {}", jarPath, loaderJarPath);
                    continue item;
                }
                try {
                    var uri = new URI(LayeredZipFileSystemProvider.SCHEME + ":" + nestedJarPath.toAbsolutePath().toUri().getRawSchemeSpecificPart()).normalize();
                    var fileSystem = FileSystems.newFileSystem(uri, Map.of("packagePath", nestedJarPath));
                    roots.add(fileSystem.getPath("/"));
                } catch (Exception e) {
                    LOGGER.warn("Failed to open Jar {} in {}", jarPath, loaderJarPath, e);
                    continue item;
                }
            }
            if (roots.isEmpty()) {
                continue;
            }
            var created = createMod(roots.toArray(Path[]::new));
            if (created.file() == null) {
                LOGGER.warn("Failed to load {} from {}: {}", roots, loaderJarPath, created.ex());
                continue;
            }
            LOGGER.info("Loading {} from {}", created.file().getFileName(), loaderJarPath);
            result.add(created.file());
        }
        return result;
    }

    @Override
    protected String getDefaultJarModType() {
        return IModFile.Type.GAMELIBRARY.name();
    }

    @Override
    public String toString() {
        return "MultiJar Legacy";
    }
}
