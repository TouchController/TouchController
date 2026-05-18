package top.fifthlight.fabazel.devlaunchwrapper;

import com.google.devtools.build.runfiles.AutoBazelRepository;
import com.google.devtools.build.runfiles.Runfiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

// Bazel don't allow us to transfer arguments in BUILD file, so let's hack
@SuppressWarnings({"RedundantExplicitVariableType", "SimplifyStreamApiCallChains"})
@AutoBazelRepository
public class DevLaunchWrapper {
    private static final String version = System.getProperty("dev.launch.version", null);
    private static final String neoFormVersion = System.getProperty("dev.launch.neoFormVersion", null);
    private static final String type = System.getProperty("dev.launch.type", "client");
    private static final String assetsPath = System.getProperty("dev.launch.assetsPath", null);
    private static final String assetsVersion = System.getProperty("dev.launch.assetsVersion", null);
    private static final String accessToken = System.getProperty("dev.launch.accessToken", "");
    private static final String mainClass = System.getProperty("dev.launch.mainClass", null);
    private static final String glfwLibName = System.getenv("GLFW_LIBNAME");
    private static final String copyFiles = System.getProperty("dev.launch.copyFiles", null);
    private static final String expandRunfileProperties = System.getProperty("dev.launch.expandRunfileProperties", null);
    private static final String nativeManifestPath = System.getProperty("dev.launch.nativeManifest", null);
    private static final String legacyAssets = System.getProperty("dev.launch.legacyAssets", "");
    private static final String legacyHome = System.getProperty("dev.launch.legacyHome", "");

    private static void writeString(Path path, String content) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(content);
        }
    }

    private static String readString(Path path) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            char[] buf = new char[4096];
            int len;
            while ((len = reader.read(buf)) != -1) {
                content.append(buf, 0, len);
            }
        }
        return content.toString();
    }

    private static class CopyDirectoryVisitor extends SimpleFileVisitor<Path> {
        private final Path fromPath;
        private final Path toPath;
        private final CopyOption[] copyOptions;

        public CopyDirectoryVisitor(Path fromPath, Path toPath, CopyOption... options) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOptions = options;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            Files.createDirectories(targetPath);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOptions);
            return FileVisitResult.CONTINUE;
        }
    }

    public static void main(String[] args) throws ReflectiveOperationException, IOException {
        Runfiles runfiles = Runfiles.preload().withSourceRepository(AutoBazelRepository_DevLaunchWrapper.NAME);

        if (expandRunfileProperties != null) {
            for (String property : expandRunfileProperties.split(",")) {
                String original = System.getProperty(property);
                System.setProperty(property, runfiles.rlocation(original));
            }
        }

        if (nativeManifestPath != null) {
            Path nativesDir = Paths.get("natives");
            Files.createDirectories(nativesDir);
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(runfiles.rlocation(nativeManifestPath)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    String[] entries = line.split(":");
                    if (entries.length < 2) {
                        throw new IllegalArgumentException("Invalid native manifest entry: " + line);
                    }
                    Path path;
                    if (entries[0].startsWith("external/")) {
                        path = Paths.get(runfiles.rlocation(entries[0].substring(9)));
                    } else {
                        path = Paths.get(runfiles.rlocation(entries[0]));
                    }
                    List<String> excludes = Arrays.stream(entries).skip(1).collect(Collectors.toList());
                    try (JarInputStream jis = new JarInputStream(Files.newInputStream(path))) {
                        JarEntry entry;
                        outer:
                        while ((entry = jis.getNextJarEntry()) != null) {
                            for (String exclude : excludes) {
                                if (entry.getName().startsWith(exclude)) {
                                    continue outer;
                                }
                            }
                            Path targetPath = nativesDir.resolve(entry.getName());
                            Files.createDirectories(targetPath.getParent());
                            Files.copy(jis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
            System.setProperty("org.lwjgl.librarypath", nativesDir.toAbsolutePath().toString());
        }

        Path workDir = Paths.get(".");
        Path workDirAbsolute = workDir.toAbsolutePath();
        if (copyFiles != null) {
            String[] copyFileList = copyFiles.split(",");
            for (String entry : copyFileList) {
                int colonIndex = entry.indexOf(':');
                if (colonIndex == -1) {
                    throw new IllegalArgumentException("Invalid copy file entry: " + entry);
                }
                String fromStr = entry.substring(0, colonIndex);
                Path from = Paths.get(runfiles.rlocation(fromStr)).toRealPath();
                Path to = workDir.resolve(entry.substring(colonIndex + 1));
                Files.createDirectories(to.getParent());
                if (Files.isDirectory(from)) {
                    Files.walkFileTree(from, new CopyDirectoryVisitor(from, to, StandardCopyOption.REPLACE_EXISTING));
                } else {
                    Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));

        if (glfwLibName != null) {
            System.setProperty("org.lwjgl.glfw.libname", glfwLibName);
        }

        if ("true".equals(legacyHome)) {
            System.setProperty("user.home", workDirAbsolute.toString());
            Path minecraftDir = Paths.get(".minecraft");
            System.out.println(minecraftDir.toAbsolutePath());
            Files.deleteIfExists(minecraftDir);
            Files.createSymbolicLink(minecraftDir, workDir);
        }
        argsList.add("--gameDir");
        argsList.add(workDirAbsolute.toString());

        argsList.add("--accessToken");
        argsList.add(accessToken);
        if (version != null) {
            argsList.add("--version");
            argsList.add(version);
        }
        if (neoFormVersion != null) {
            if (version != null) {
                argsList.add("--fml.mcVersion");
                argsList.add(version);
            }
            argsList.add("--fml.neoFormVersion");
            argsList.add(neoFormVersion);
        }

        String assetsPath = DevLaunchWrapper.assetsPath;
        if (assetsVersion != null) {
            Path realAssetsVersion = Paths.get(runfiles.rlocation(Paths.get(assetsVersion).normalize().toString()));
            Path realAssetsPath = realAssetsVersion.resolve(Paths.get("..", "..")).toAbsolutePath();
            assetsPath = realAssetsPath.toString();
        }
        if (assetsPath != null) {
            String realAssetsPath = runfiles.rlocation(Paths.get(assetsPath).normalize().toString());
            Path realAssets = Paths.get(realAssetsPath);
            if ("true".equals(legacyAssets)) {
                Path resourcesDir = Paths.get("resources");
                Files.deleteIfExists(resourcesDir);
                Files.createSymbolicLink(resourcesDir, realAssets.resolve("legacy"));
            } else {
                Path path = realAssets.toRealPath();
                argsList.add("--assetsDir");
                argsList.add(path.toString());
                if (version != null) {
                    Path versionPath = path.resolve(Paths.get("versions", version));
                    argsList.add("--assetIndex");
                    argsList.add(readString(versionPath));
                }
            }
        }

        switch (type) {
            case "client":
                Path allowSymlinksPath = Paths.get("allowed_symlinks.txt");
                writeString(allowSymlinksPath, "[regex].*\n");
                break;
            case "server":
                Path serverPropertiesPath = Paths.get("server.properties");
                if (!Files.exists(serverPropertiesPath)) {
                    writeString(serverPropertiesPath, "online-mode=false\n");
                }
                argsList.add("--nogui");
                Path eulaPath = Paths.get("eula.txt");
                writeString(eulaPath, "eula=true\n");
                break;
        }

        System.err.println("Launching game with arguments: " + String.join(" ", argsList));
        String[] array = new String[argsList.size()];
        array = argsList.toArray(array);

        if (mainClass == null) {
            throw new IllegalArgumentException("No main class specified. Specify your real main class with dev.launch.mainClass JVM property.");
        }
        Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(mainClass);
        Method mainMethod = clazz.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) array);
    }
}
