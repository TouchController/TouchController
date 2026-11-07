package top.fifthlight.mergetools.merger.plugin.servicewrapper;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/// Merge interfaces into one wrapper class.
public class ServiceWrapperPlugin implements Plugin {
    private static final String SERVICES_PREFIX = "META-INF/services/";
    private static final String ARG_PREFIX = "--services=";
    private static final String CLASS_FILE_SUFFIX = ".class";

    private record WrapperItem(@NonNull String wrapperFqn, @NonNull List<String> implFqns) {
    }

    private final Map<String, WrapperItem> serviceWrappers = new LinkedHashMap<>();
    private final Set<String> usedWrapperFqns = new HashSet<>();

    @Override
    public int priority() {
        // After the services plugin (300), which merges the META-INF/services entries into mergeEntries.
        return 350;
    }

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        if (!arg.startsWith(ARG_PREFIX)) {
            return false;
        }
        var parts = arg.substring(ARG_PREFIX.length()).split(":", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid --services argument, expected --services=interface.fqn:wrapper.fqn:impl1.fqn,impl2.fqn: " + arg);
        }
        var interfaceFqn = parts[0];
        var wrapperFqn = parts[1];
        var impls = List.of(parts[2].split(",", -1));
        if (interfaceFqn.isEmpty() || wrapperFqn.isEmpty() || impls.stream().anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("Invalid --services argument, empty class name: " + arg);
        }
        for (var impl : impls) {
            if (impl.equals(wrapperFqn)) {
                throw new IllegalArgumentException("Wrapper must not be one of its implementations: " + impl);
            }
        }
        if (serviceWrappers.containsKey(interfaceFqn)) {
            throw new IllegalArgumentException("Duplicate wrapper for service: " + interfaceFqn);
        }
        if (!usedWrapperFqns.add(wrapperFqn)) {
            throw new IllegalArgumentException("Wrapper class is used by more than one service: " + wrapperFqn);
        }
        serviceWrappers.put(interfaceFqn, new WrapperItem(wrapperFqn, impls));
        return true;
    }

    private static byte @Nullable [] readClass(Map<String, MergeEntry> mergeEntries, String internalName) throws Exception {
        MergeEntry entry = mergeEntries.get(internalName + CLASS_FILE_SUFFIX);
        if (entry == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
        entry.write(stream);
        return stream.toByteArray();
    }

    private static LinkedHashSet<String> readServiceFile(MergeEntry entry, String fileName) {
        var stream = new ByteArrayOutputStream(256);
        try {
            entry.write(stream);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read service file: " + fileName, e);
        }
        var serviceFqns = new LinkedHashSet<String>();
        try (var reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(stream.toByteArray()), StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(serviceFqns::add);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read service file: " + fileName, e);
        }
        return serviceFqns;
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {
        if (serviceWrappers.isEmpty()) {
            return;
        }
        for (var entry : serviceWrappers.entrySet()) {
            var interfaceFqn = entry.getKey();
            var item = entry.getValue();
            var wrapperFqn = item.wrapperFqn;
            var implFqns = item.implFqns;
            var serviceFileName = SERVICES_PREFIX + interfaceFqn;

            var serviceEntry = mergeEntries.get(serviceFileName);
            if (serviceEntry == null) {
                throw new IllegalArgumentException(
                        "Service " + interfaceFqn + " has no META-INF/services registration in the merged inputs");
            }
            var serviceFqns = readServiceFile(serviceEntry, serviceFileName);
            for (var implFqn : implFqns) {
                if (!serviceFqns.contains(implFqn)) {
                    throw new IllegalArgumentException(
                            "Implementation " + implFqn + " of service " + interfaceFqn + " (wrapper " + wrapperFqn + ")"
                                    + " is not registered in any META-INF/services file of the merged inputs;"
                                    + " every implementation must be registered. Registered: " + serviceFqns);
                }
            }

            var wrapperInternalName = wrapperFqn.replace('.', '/');
            var interfaceInternalName = interfaceFqn.replace('.', '/');

            var implClasses = new ArrayList<byte[]>(implFqns.size());
            for (var implFqn : implFqns) {
                byte[] classBytes;
                try {
                    classBytes = readClass(mergeEntries, implFqn.replace('.', '/'));
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to read class " + implFqn, e);
                }
                if (classBytes == null) {
                    throw new IllegalStateException("Class " + implFqn + " not found");
                }
                implClasses.add(classBytes);
            }

            mergeEntries.put(wrapperInternalName + CLASS_FILE_SUFFIX,
                    new WrapperClassMergeEntry(wrapperInternalName, interfaceInternalName, implClasses));

            // Replace the registered implementations with the wrapper, keeping other registrations.
            var remaining = new LinkedHashSet<>(serviceFqns);
            implFqns.forEach(remaining::remove);
            remaining.add(wrapperFqn);
            mergeEntries.put(serviceFileName,
                    new WrapperServiceFileMergeEntry(String.join("\n", remaining) + "\n"));
        }
    }

    private record WrapperServiceFileMergeEntry(String content) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            output.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
