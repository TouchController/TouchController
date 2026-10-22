/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.multijar.legacy;

import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * ClasspathTransformerDiscoverer only flag JAR with IModLocator as early service JAR,
 * so create a placeholder ModLocator here.
 */
public class LegacyModLocator extends AbstractJarFileModLocator {
    public LegacyModLocator() throws NoSuchMethodException, NoSuchFieldException {
        new LegacyLocator();
    }

    @Override
    public Stream<Path> scanCandidates() {
        return Stream.of();
    }

    @Override
    public String name() {
        return "MultiJar Legacy";
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
    }

    @Override
    public String toString() {
        return "MultiJar Legacy";
    }
}
