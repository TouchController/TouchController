package top.fifthlight.armorstand;

import net.fabricmc.classtweaker.api.ClassTweakerReader;
import net.fabricmc.classtweaker.api.visitor.AccessWidenerVisitor;
import net.fabricmc.classtweaker.api.visitor.ClassTweakerVisitor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;import top.fifthlight.bazel.worker.api.Worker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class AccessWidenerConvertor extends Worker {
    private record AccessTransformerWriter(Writer writer) implements ClassTweakerVisitor, AutoCloseable {
        @NotNull
        @Override
        public AccessWidenerVisitor visitAccessWidener(String owner) {
            var name = owner.replace('/', '.');
            return new AccessWidenerVisitor() {
                @Override
                public void visitClass(AccessWidenerVisitor.AccessType access, boolean transitive) {
                    try {
                        switch (access) {
                            case ACCESSIBLE -> writer.write("public");
                            case EXTENDABLE -> writer.write("public-f");
                            case MUTABLE -> throw new IllegalArgumentException("Bad access for class: " + access);
                        }
                        writer.write(' ');
                        writer.write(name);
                        writer.write('\n');
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void visitMethod(String owner, String descriptor, AccessWidenerVisitor.AccessType access, boolean transitive) {
                    try {
                        switch (access) {
                            case ACCESSIBLE -> writer.write("public");
                            case EXTENDABLE -> writer.write("public-f");
                            case MUTABLE -> throw new IllegalArgumentException("Bad access for method: " + access);
                        }
                        writer.write(' ');
                        writer.write(owner.replace('/', '.'));
                        writer.write(' ');
                        writer.write(name);
                        writer.write(descriptor);
                        writer.write('\n');
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void visitField(String owner, String descriptor, AccessWidenerVisitor.AccessType access, boolean transitive) {
                    try {
                        switch (access) {
                            case ACCESSIBLE -> writer.write("public");
                            case EXTENDABLE -> throw new IllegalArgumentException("Bad access for field: " + access);
                            case MUTABLE -> writer.write("public-f");
                        }
                        writer.write(' ');
                        writer.write(owner.replace('/', '.'));
                        writer.write(' ');
                        writer.write(name);
                        writer.write('\n');
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }

    public static void main(String[] args) throws Exception {
        new AccessWidenerConvertor().run(args);
    }

    @Override
    protected int handleRequest(@NonNull PrintWriter out, @NonNull Path sandboxDir, String... args) {
        try {
            if (args.length < 2) {
                out.println("Usage: AccessWidenerConvertor <input> <output>");
                return 1;
            }

            var inputFile = sandboxDir.resolve(Path.of(args[0]));
            var outputFile = sandboxDir.resolve(Path.of(args[1]));

            try (var writer = Files.newBufferedWriter(outputFile); var accessTransformerWriter = new AccessTransformerWriter(writer)) {
                var accessWidenerReader = ClassTweakerReader.create(accessTransformerWriter);
                try (var reader = Files.newBufferedReader(inputFile)) {
                    accessWidenerReader.read(reader, null);
                }
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace(out);
            return 1;
        }
    }
}
