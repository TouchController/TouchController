package top.fifthlight.fabazel.mappingmerger;

import net.fabricmc.mappingio.format.tiny.Tiny2FileWriter;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.fifthlight.bazel.worker.api.Worker;
import top.fifthlight.fabazel.mappingmerger.context.InputEntry;
import top.fifthlight.fabazel.mappingmerger.context.MappingFormat;
import top.fifthlight.fabazel.mappingmerger.context.MergeContext;
import top.fifthlight.fabazel.mappingmerger.operation.ChangeSourceNamespaceOperation;
import top.fifthlight.fabazel.mappingmerger.operation.CompleteNamespaceOperation;
import top.fifthlight.fabazel.mappingmerger.operation.ImportMappingOperation;
import top.fifthlight.fabazel.mappingmerger.operation.Operation;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MappingMerger extends Worker {
    private static class CliContext {
        private final MergeContext.Builder contextBuilder = new MergeContext.Builder();
        private Path outputPath = null;
        private final List<Operation> operations = new ArrayList<>();
        private boolean insideMapping = false;
        private Path mappingPath = null;
        private String mappingName = null;
        private MappingFormat mappingFormat = null;
        private final Map<String, String> namespaceMappings = new HashMap<>();

        public void setOutputPath(Path outputPath) {
            if (this.outputPath != null) {
                throw new IllegalArgumentException("Output path is already specified: " + this.outputPath);
            }
            this.outputPath = outputPath;
        }

        private void clearMapping() {
            mappingPath = null;
            mappingName = null;
            mappingFormat = null;
            namespaceMappings.clear();
        }

        public void finishMapping() {
            if (!insideMapping) {
                return;
            }
            insideMapping = false;
            if (mappingPath == null) {
                throw new IllegalArgumentException("No path specified for mapping");
            }
            if (mappingFormat == null) {
                throw new IllegalArgumentException("No format specified for mapping");
            }
            if (mappingName == null) {
                throw new IllegalArgumentException("No name specified for mapping");
            }
            contextBuilder.addInputEntry(mappingName, new InputEntry(
                    mappingPath,
                    mappingFormat,
                    new HashMap<>(namespaceMappings)
            ));
            clearMapping();
        }

        public void enterMapping() {
            if (insideMapping) {
                finishMapping();
            }
            insideMapping = true;
            clearMapping();
        }

        public boolean isInsideMapping() {
            return insideMapping;
        }

        public void setMappingPath(Path path) {
            if (mappingPath != null) {
                throw new IllegalArgumentException("Mapping path is already specified: " + mappingPath);
            }
            mappingPath = path;
        }

        public void setMappingName(String name) {
            if (mappingName != null) {
                throw new IllegalArgumentException("Mapping name is already specified: " + mappingName);
            }
            mappingName = name;
        }

        public void setMappingFormat(MappingFormat format) {
            if (mappingFormat != null) {
                throw new IllegalArgumentException("Mapping format is already specified: " + mappingFormat);
            }
            mappingFormat = format;
        }

        public void addNamespaceMapping(String from, String to) {
            namespaceMappings.put(from, to);
        }

        public void addOperation(Operation operation) {
            finishMapping();
            operations.add(operation);
        }

        public Result build() {
            finishMapping();
            return new Result(contextBuilder.build(), outputPath, operations);
        }

        public record Result(MergeContext context, Path outputPath, List<Operation> operations) {
        }
    }

    @Override
    protected int handleRequest(@NonNull PrintWriter out, @Nullable Path sandboxDir, String... args) {
        try {
            var context = new CliContext();
            var enterOperation = false;
            for (var argIndex = 0; argIndex < args.length; argIndex++) {
                var arg = args[argIndex];

                if (enterOperation) {
                    if (arg.startsWith(">")) {
                        context.addOperation(new ImportMappingOperation(arg.substring(1)));
                    } else {
                        var leftIndex = arg.indexOf('(');
                        var rightIndex = arg.indexOf(')');
                        if (leftIndex == -1 || rightIndex == -1) {
                            throw new IllegalArgumentException("Bad operation: " + arg);
                        }
                        var operationName = arg.substring(0, leftIndex);
                        var operationArg = arg.substring(leftIndex + 1, rightIndex).trim();
                        switch (operationName) {
                            case "changeSrc" -> {
                                var entries = operationArg.split(",");
                                context.addOperation(switch (entries.length) {
                                    case 1 -> new ChangeSourceNamespaceOperation(entries[0].trim(), false);
                                    case 2 -> new ChangeSourceNamespaceOperation(
                                            entries[0].trim(),
                                            Boolean.parseBoolean(entries[1].trim())
                                    );
                                    default -> throw new IllegalStateException("Bad argument count: " + entries.length);
                                });
                            }
                            case "completeNamespace" -> {
                                var entries = operationArg.split(",");
                                var namespaceMappings = new HashMap<String, String>();
                                for (var entry : entries) {
                                    var separatorIndex = entry.indexOf("->");
                                    if (separatorIndex == -1) {
                                        throw new IllegalArgumentException("Bad operation argument: " + operationArg);
                                    }
                                    var from = entry.substring(0, separatorIndex).trim();
                                    var to = entry.substring(separatorIndex + 2).trim();
                                    namespaceMappings.put(from, to);
                                }
                                context.addOperation(new CompleteNamespaceOperation(namespaceMappings));
                            }
                            default -> throw new IllegalArgumentException("Bad operation: " + operationName);
                        }
                    }
                } else if (arg.startsWith("--")) {
                    var option = arg.substring(2);
                    switch (option) {
                        case "mapping":
                            context.enterMapping();
                            break;
                        case "output":
                            context.finishMapping();
                            if (argIndex >= args.length - 1) {
                                throw new IllegalArgumentException("No value for argument: " + arg);
                            }
                            context.setOutputPath(sandboxDir != null ? sandboxDir.resolve(Path.of(args[argIndex + 1])) : Path.of(args[argIndex + 1]));
                            argIndex++;
                            break;
                        case "":
                            context.finishMapping();
                            enterOperation = true;
                            break;
                        default:
                            throw new IllegalArgumentException("Bad option: " + option);
                    }
                } else if (context.isInsideMapping()) {
                    var equalsIndex = arg.indexOf('=');
                    if (equalsIndex == -1) {
                        throw new IllegalArgumentException("Bad argument: " + arg);
                    }
                    var key = arg.substring(0, equalsIndex);
                    var value = arg.substring(equalsIndex + 1);
                    switch (key) {
                        case "path":
                            context.setMappingPath(sandboxDir != null ? sandboxDir.resolve(Path.of(value)) : Path.of(value));
                            break;
                        case "name":
                            context.setMappingName(value);
                            break;
                        case "format":
                            var format = Arrays.<MappingFormat>stream(MappingFormat.values())
                                    .filter(type -> type.getName().equals(value))
                                    .findAny()
                                    .<IllegalArgumentException>orElseThrow(() -> {
                                        var availableMappingTypes = Arrays.<MappingFormat>stream(MappingFormat.values())
                                                .<String>map(MappingFormat::getName)
                                                .collect(Collectors.joining("\n"));
                                        return new IllegalArgumentException(
                                                "Bad mapping type: " + value + "\n" + "Available mappings type:" + "\n" + availableMappingTypes
                                        );
                                    });
                            context.setMappingFormat(format);
                            break;
                        case "namespace-mapping":
                            var separatorIndex = value.indexOf(':');
                            if (separatorIndex == -1) {
                                throw new IllegalArgumentException("Bad argument: " + arg);
                            }
                            var from = value.substring(0, separatorIndex);
                            var to = value.substring(separatorIndex + 1);
                            context.addNamespaceMapping(from, to);
                            break;
                        default:
                            throw new IllegalArgumentException("Bad argument: " + arg);
                    }
                } else {
                    throw new IllegalArgumentException("Bad argument: " + arg);
                }
            }

            var contextResult = context.build();
            var tree = new MemoryMappingTree();
            for (var operation : contextResult.operations()) {
                tree = operation.run(tree, contextResult.context());
            }
            try (var writer = Files.newBufferedWriter(contextResult.outputPath()); var visitor = new Tiny2FileWriter(writer, false)) {
                tree.accept(visitor);
            }

            return 0;
        } catch (Exception ex) {
            ex.printStackTrace(out);
            return 1;
        }
    }

    public static void main(String[] args) throws Exception {
        new MappingMerger().run(args);
    }
}
