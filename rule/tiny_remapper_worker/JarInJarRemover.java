package top.fifthlight.fabazel.remapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class JarInJarRemover implements OutputConsumerPath.ResourceRemapper {
    public static final JarInJarRemover INSTANCE = new JarInJarRemover();

    private static boolean isJarInJar(Path path) {
        return path.getNameCount() == 3
                && path.getName(0).toString().equals("META-INF")
                && path.getName(1).toString().equals("jars")
                && path.getFileName().toString().toLowerCase().endsWith(".jar");
    }

    private static boolean isFabricModJson(Path path) {
        return path.getNameCount() == 1
                && path.getFileName().toString().equalsIgnoreCase("fabric.mod.json");
    }

    @Override
    public boolean canTransform(TinyRemapper remapper, Path path) {
        return isJarInJar(path) || isFabricModJson(path);
    }

    @Override
    public void transform(
            Path destinationDirectory,
            Path relativePath,
            InputStream input,
            TinyRemapper tinyRemapper
    ) {
        if (isJarInJar(relativePath)) {
            return;
        }
        if (isFabricModJson(relativePath)) {
            try {
                var outputFile = destinationDirectory.resolve(relativePath.toString());
                var mapper = new ObjectMapper();
                var jsonContent = mapper.readTree(input);
                var newContent = jsonContent;

                if (jsonContent.isObject()) {
                    var jsonObject = (ObjectNode) jsonContent;
                    var newObject = mapper.createObjectNode();

                    for (var entry : jsonObject.properties()) {
                        if (!"jars".equals(entry.getKey())) {
                            newObject.set(entry.getKey(), entry.getValue());
                        }
                    }
                    newContent = newObject;
                }

                try (var output = Files.newOutputStream(outputFile)) {
                    mapper.writerWithDefaultPrettyPrinter().writeValue(output, newContent);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
