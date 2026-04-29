package top.fifthlight.fabazel.accesswidenertransformer;

import net.fabricmc.classtweaker.api.ClassTweaker;
import net.fabricmc.classtweaker.api.ClassTweakerReader;
import net.fabricmc.classtweaker.classvisitor.AccessWidenerClassVisitor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import top.fifthlight.bazel.worker.api.Worker;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class AccessWidenerTransformer extends Worker {
    private static final long DOS_EPOCH = 315532800000L;

    private static void setJarEntryTime(JarEntry entry) {
        entry.setCreationTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastAccessTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastModifiedTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setTimeLocal(LocalDateTime.ofEpochSecond(DOS_EPOCH / 1000, 0, ZoneOffset.UTC));
    }

    public static void main(String[] args) throws Exception {
        new AccessWidenerTransformer().run(args);
    }

    @Override
    protected int handleRequest(@NonNull PrintWriter out, @Nullable Path sandboxDir, String... args) {
        try {
            if (args.length < 2) {
                out.println("Usage: AccessWidenerTransformer <input> <output> [accessWidenerFiles...]");
                return 1;
            }

            var inputFile = sandboxDir != null ? sandboxDir.resolve(Path.of(args[0])) : Path.of(args[0]);
            var outputFile = sandboxDir != null ? sandboxDir.resolve(Path.of(args[1])) : Path.of(args[1]);

            var accessWidener = ClassTweaker.newInstance();
            var accessWidenerReader = ClassTweakerReader.create(accessWidener);
            for (var i = 2; i < args.length; i++) {
                var srcFile = sandboxDir != null ? sandboxDir.resolve(Path.of(args[i])) : Path.of(args[i]);
                try (var reader = Files.newBufferedReader(srcFile)) {
                    accessWidenerReader.read(reader, null);
                }
            }

            try (var input = new JarInputStream(Files.newInputStream(inputFile)); var output = new JarOutputStream(Files.newOutputStream(outputFile))) {
                JarEntry entry;
                while ((entry = input.getNextJarEntry()) != null) {
                    var newEntry = new JarEntry(entry.getName());
                    setJarEntryTime(newEntry);
                    output.putNextEntry(newEntry);

                    if (entry.getName().endsWith(".class")) {
                        var classReader = new ClassReader(input);
                        var classWriter = new ClassWriter(0);
                        var classVisitor = new AccessWidenerClassVisitor(Opcodes.ASM9, classWriter, accessWidener);
                        classReader.accept(classVisitor, 0);
                        output.write(classWriter.toByteArray());
                    } else {
                        input.transferTo(output);
                    }

                    input.closeEntry();
                    output.closeEntry();
                }
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace(out);
            return 1;
        }
    }
}
