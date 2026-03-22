package top.fifthlight.fabazel.remapper;

import net.fabricmc.classtweaker.api.ClassTweaker;
import net.fabricmc.classtweaker.api.ClassTweakerReader;
import net.fabricmc.classtweaker.api.ClassTweakerWriter;
import net.fabricmc.classtweaker.visitors.ClassTweakerRemapperVisitor;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class AccessWidenerRemapper implements OutputConsumerPath.ResourceRemapper {
    private final Remapper remapper;
    private final String fromNamespace;
    private final String toNamespace;

    public AccessWidenerRemapper(Remapper remapper, String fromNamespace, String toNamespace) {
        this.remapper = remapper;
        this.fromNamespace = fromNamespace;
        this.toNamespace = toNamespace;
    }

    @Override
    public boolean canTransform(TinyRemapper remapper, Path path) {
        var fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".accesswidener") || fileName.endsWith(".classtweaker");
    }

    @Override
    public void transform(Path destinationDirectory, Path relativePath, InputStream input, TinyRemapper tinyRemapper) throws IOException {
        var outputFile = destinationDirectory.resolve(relativePath.toString());

        var content = input.readAllBytes();

        var version = ClassTweakerReader.readVersion(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content))));
        var writer = ClassTweakerWriter.create(version);
        var accessWidenerRemapper = new ClassTweakerRemapperVisitor(
                writer,
                this.remapper,
                fromNamespace,
                toNamespace
        );

        var reader = ClassTweakerReader.create(accessWidenerRemapper);
        try {
            reader.read(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content))), null);
            Files.write(outputFile, writer.getOutput());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
