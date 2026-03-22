package top.fifthlight.armorstand;

import net.fabricmc.classtweaker.api.ClassTweakerReader;
import net.fabricmc.classtweaker.api.ClassTweakerWriter;
import net.fabricmc.classtweaker.visitors.ClassTweakerRemapperVisitor;
import net.fabricmc.mappingio.extras.MappingTreeRemapper;
import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.jspecify.annotations.NonNull;import top.fifthlight.bazel.worker.api.Worker;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class AccessWidenerRemapper extends Worker {
    public static void main(String[] args) throws Exception {
        new AccessWidenerRemapper().run(args);
    }

    @Override
    protected int handleRequest(@NonNull PrintWriter out, @NonNull Path sandboxDir, String... args) {
        try {
            if (args.length < 5) {
                out.println("Usage: AccessWidenerRemapper <input> <output> <mapping> <fromNamespace> <toNamespace>");
                return 1;
            }

            var inputFile = sandboxDir.resolve(Path.of(args[0]));
            var outputFile = sandboxDir.resolve(Path.of(args[1]));
            var mappingFile = sandboxDir.resolve(Path.of(args[2]));
            var fromNamespace = args[3];
            var toNamespace = args[4];

            var mappingTree = new MemoryMappingTree();
            try (var reader = Files.newBufferedReader(mappingFile)) {
                Tiny2FileReader.read(reader, mappingTree);
            }
            var remapper = new MappingTreeRemapper(mappingTree, fromNamespace, toNamespace);

            try (var writer = Files.newOutputStream(outputFile)) {
                int version;
                try (var reader = Files.newBufferedReader(inputFile)) {
                    version = ClassTweakerReader.readVersion(reader);
                }
                var accessWidenerWriter = ClassTweakerWriter.create(version);
                var accessWidenerRemapper = new ClassTweakerRemapperVisitor(accessWidenerWriter, remapper, fromNamespace, toNamespace);
                var accessWidenerReader = ClassTweakerReader.create(accessWidenerRemapper);
                try (var reader = Files.newBufferedReader(inputFile)) {
                    accessWidenerReader.read(reader, null);
                }
                writer.write(accessWidenerWriter.getOutput());
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace(out);
            return 1;
        }
    }
}
