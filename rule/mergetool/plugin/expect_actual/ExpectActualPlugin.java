package top.fifthlight.mergetools.merger.plugin.expectactual;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;
import top.fifthlight.mergetools.processor.ActualData;
import top.fifthlight.mergetools.processor.AspectData;
import top.fifthlight.mergetools.processor.ExpectData;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExpectActualPlugin implements Plugin, ExpectActualPluginContext {
    @Override
    public int priority() {
        return 1000;
    }

    private static final String expectPrefix = "META-INF/expects/";
    private static final String actualPrefix = "META-INF/actuals/";
    private static final String aspectManifestPath = "META-INF/aspect.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final HashMap<String, ExpectData> expectDataMap = new HashMap<>();
    private final HashMap<String, ActualData> actualDataMap = new HashMap<>();
    private final HashSet<String> factoryClasses = new HashSet<>();

    private boolean aspectMode = false;
    private String aspectClassName;
    private String aspectImplPackageSuffix = null;
    private final List<AspectData> aspectDependencies = new ArrayList<>();

    @Override
    public Map<String, ActualData> getActualDataMap() {
        return actualDataMap;
    }

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        try {
            if ("--aspect-mode".equals(arg)) {
                aspectMode = Boolean.parseBoolean(environment.readNextArg());
                return true;
            }
            if ("--aspect-class".equals(arg)) {
                aspectClassName = ExpectActualUtils.fqnToInternalName(environment.readNextArg());
                return true;
            }
            if ("--aspect-impl-package-suffix".equals(arg)) {
                aspectImplPackageSuffix = environment.readNextArg();
                return true;
            }
            if ("--aspect".equals(arg)) {
                var path = environment.resolvePath(Path.of(environment.readNextArg()));
                try (var jarFile = new JarFile(path.toFile())) {
                    var entry = jarFile.getJarEntry(aspectManifestPath);
                    if (entry == null) {
                        throw new IllegalStateException("Aspect JAR missing " + aspectManifestPath + ": " + path);
                    }
                    try (var inputStream = new BufferedInputStream(jarFile.getInputStream(entry));
                         var reader = new InputStreamReader(inputStream)) {
                        var aspectData = mapper.readValue(reader, AspectData.class);
                        aspectDependencies.add(aspectData);
                    }
                }
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public boolean processJarEntry(JarFile file, JarEntry entry, PreprocessEnvironment environment) throws IOException {
        var name = entry.getName();
        if (name.startsWith(expectPrefix) && name.endsWith(".json")) {
            try (var inputStream = new BufferedInputStream(file.getInputStream(entry));
                 var reader = new InputStreamReader(inputStream)) {
                var expectData = mapper.readValue(reader, ExpectData.class);
                var interfaceFullQualifiedName = name.substring(expectPrefix.length(), name.length() - ".json".length());
                var interfaceClassPath = ExpectActualUtils.descriptorNameToInternalName(expectData.interfaceName());
                var interfaceFactoryPath = interfaceClassPath + "Factory.class";
                expectDataMap.put(interfaceFullQualifiedName, expectData);
                factoryClasses.add(interfaceFactoryPath);
                // If the factory class already exists, it will be overwritten
                environment.putMergeEntry(interfaceFactoryPath, new ExpectManifest(this, interfaceFullQualifiedName, expectData));
            }
            return true;
        }
        if (name.startsWith(actualPrefix) && name.endsWith(".json")) {
            try (var inputStream = new BufferedInputStream(file.getInputStream(entry));
                 var reader = new InputStreamReader(inputStream)) {
                var actualData = mapper.readValue(reader, ActualData.class);
                var interfaceFullQualifiedName = name.substring(actualPrefix.length(), name.length() - ".json".length());
                if (actualDataMap.containsKey(interfaceFullQualifiedName)) {
                    throw new IllegalStateException("Duplicate actual expectData: " + interfaceFullQualifiedName);
                }
                actualDataMap.put(interfaceFullQualifiedName, actualData);
            }
            return true;
        }
        // Filter generated factory classes out in case expect manifest entries are processed first
        return factoryClasses.contains(name);
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {
        if (aspectMode) {
            preSortingAspectJar(mergeEntries);
        } else {
            preSortingConsumer(mergeEntries);
        }
    }

    private String computeImplInternalName(String aspectProviderInternalName) {
        if (aspectImplPackageSuffix == null || aspectImplPackageSuffix.isEmpty()) {
            return aspectProviderInternalName + "Impl";
        }
        var lastSlash = aspectProviderInternalName.lastIndexOf('/');
        var basePath = aspectProviderInternalName.substring(0, lastSlash);
        var shortName = aspectProviderInternalName.substring(lastSlash + 1);
        var suffixPath = aspectImplPackageSuffix.replace('.', '/');
        return basePath + "/" + suffixPath + "/" + shortName + "Impl";
    }

    private String computeImplFqn(String aspectProviderInternalName) {
        return ExpectActualUtils.internalNameToFqn(computeImplInternalName(aspectProviderInternalName));
    }

    private void preSortingAspectJar(Map<String, MergeEntry> mergeEntries) {
        // Step 1: Process dependent Aspect JARs
        for (var aspectData : aspectDependencies) {
            for (var expectEntry : aspectData.expects()) {
                var fqn = ExpectActualUtils.internalNameToFqn(ExpectActualUtils.descriptorNameToInternalName(expectEntry.interfaceName()));
                if (!actualDataMap.containsKey(fqn)) {
                    throw new IllegalStateException("Aspect expect not satisfied: " + fqn);
                }
            }
            var aspectProviderInternalName = ExpectActualUtils.descriptorNameToInternalName(aspectData.aspectProviderInterface());
            var aspectProviderFqn = ExpectActualUtils.internalNameToFqn(aspectProviderInternalName);
            var implInternalName = computeImplInternalName(aspectProviderInternalName);
            mergeEntries.put(implInternalName + ".class", new AspectProviderImplEntry(this, aspectData.aspectProviderInterface(), aspectData, implInternalName));

            var servicesPath = "META-INF/services/" + aspectProviderFqn;
            if (mergeEntries.containsKey(servicesPath)) {
                throw new IllegalStateException("ServiceLoader file for " + aspectProviderFqn + " already exists");
            }
            var implFqn = computeImplFqn(aspectProviderInternalName);
            mergeEntries.put(servicesPath, new ServiceLoaderRegistrationEntry(implFqn));
        }

        // Step 2: Separate satisfied and unresolved expects
        var unresolvedExpects = new ArrayList<ExpectData>();
        for (var expectEntry : expectDataMap.entrySet()) {
            var key = expectEntry.getKey();
            if (!actualDataMap.containsKey(key)) {
                unresolvedExpects.add(expectEntry.getValue());
            } else {
                // Step 4: Process satisfied expects (same as consumer mode)
                var actualData = actualDataMap.get(key);
                var actualSpiFactoryPath = ExpectActualUtils.descriptorNameToInternalName(actualData.spiFactoryName()) + ".class";
                mergeEntries.remove(actualSpiFactoryPath);
                var spiManifestPath = "META-INF/services/" + key + "$Factory";
                mergeEntries.remove(spiManifestPath);
            }
        }

        // Step 3: Verify non-empty
        if (unresolvedExpects.isEmpty()) {
            throw new IllegalStateException("Aspect JAR must have unresolved expects");
        }

        var aspectProviderInterface = ExpectActualUtils.internalNameToDescriptor(aspectClassName);
        var aspectProviderFactory = ExpectActualUtils.internalNameToDescriptor(aspectClassName + "Factory");

        // Step 5: Generate AspectProvider interface
        mergeEntries.put(aspectClassName + ".class",
                new AspectProviderInterfaceEntry(aspectClassName, unresolvedExpects));

        // Step 6: Generate AspectProviderFactory class
        mergeEntries.put(aspectClassName + "Factory.class",
                new AspectProviderFactoryEntry(aspectClassName));

        // Step 7: Replace unresolved ExpectManifests with delegate versions
        for (var expectData : unresolvedExpects) {
            var interfaceClassPath = ExpectActualUtils.descriptorNameToInternalName(expectData.interfaceName());
            var factoryPath = interfaceClassPath + "Factory.class";
            var existing = (ExpectManifest) mergeEntries.get(factoryPath);
            if (existing != null) {
                mergeEntries.put(factoryPath, existing.withAspectProvider(aspectProviderInterface, aspectProviderFactory));
            }

            var spiManifestPath = "META-INF/services/" + interfaceClassPath + "$Factory";
            mergeEntries.remove(spiManifestPath);
        }

        // Step 8: Generate aspect.json
        var aspectExpectEntries = unresolvedExpects.stream()
                .map(e -> new AspectData.ExpectEntry(e.interfaceName(), e.constructors()))
                .toArray(AspectData.ExpectEntry[]::new);
        var aspectData = new AspectData(aspectProviderInterface, aspectProviderFactory, aspectExpectEntries);
        mergeEntries.put(aspectManifestPath, new AspectManifestEntry(aspectData));

    }

    private void preSortingConsumer(Map<String, MergeEntry> mergeEntries) {
        // Step 1: Verify aspect coverage
        for (var aspectData : aspectDependencies) {
            for (var expectEntry : aspectData.expects()) {
                var fqn = ExpectActualUtils.internalNameToFqn(ExpectActualUtils.descriptorNameToInternalName(expectEntry.interfaceName()));
                if (!actualDataMap.containsKey(fqn)) {
                    throw new IllegalStateException("Aspect expect not satisfied: " + fqn);
                }
            }
        }

        // Step 2: Generate AspectProviderImpl
        for (var aspectData : aspectDependencies) {
            var aspectProviderFqn = ExpectActualUtils.descriptorNameToInternalName(aspectData.aspectProviderInterface());
            var implInternalName = computeImplInternalName(aspectProviderFqn);
            mergeEntries.put(implInternalName + ".class", new AspectProviderImplEntry(this, aspectData.aspectProviderInterface(), aspectData, implInternalName));
        }

        // Step 3: Generate ServiceLoader registration
        for (var aspectData : aspectDependencies) {
            var aspectProviderInternalName = ExpectActualUtils.descriptorNameToInternalName(aspectData.aspectProviderInterface());
            var aspectProviderFqn = ExpectActualUtils.internalNameToFqn(aspectProviderInternalName);
            var servicesPath = "META-INF/services/" + aspectProviderFqn;
            if (mergeEntries.containsKey(servicesPath)) {
                throw new IllegalStateException("ServiceLoader file for " + aspectProviderFqn + " already exists");
            }
            var implFqn = computeImplFqn(aspectProviderInternalName);
            mergeEntries.put(servicesPath, new ServiceLoaderRegistrationEntry(implFqn));
        }

        // Step 4: Process internal expect/actual (same as original behavior)
        for (var expectEntry : expectDataMap.entrySet()) {
            var key = expectEntry.getKey();
            var actualData = actualDataMap.get(key);
            if (actualData == null) {
                throw new IllegalStateException("Missing actual class for: " + key);
            }

            var actualSpiFactoryPath = ExpectActualUtils.descriptorNameToInternalName(actualData.spiFactoryName()) + ".class";
            if (!mergeEntries.containsKey(actualSpiFactoryPath)) {
                throw new IllegalStateException("Missing actual spi factory: " + actualSpiFactoryPath);
            }
            mergeEntries.remove(actualSpiFactoryPath);

            var spiManifestPath = "META-INF/services/" + key + "$Factory";
            if (!mergeEntries.containsKey(spiManifestPath)) {
                throw new IllegalStateException("Missing spi manifest: " + spiManifestPath);
            }
            mergeEntries.remove(spiManifestPath);
        }

        // Step 5: Clean up aspect expect intermediate artifacts
        for (var aspectData : aspectDependencies) {
            for (var expectEntry : aspectData.expects()) {
                var fqn = ExpectActualUtils.internalNameToFqn(ExpectActualUtils.descriptorNameToInternalName(expectEntry.interfaceName()));
                var actualData = actualDataMap.get(fqn);
                if (actualData == null) {
                    throw new IllegalStateException("Missing actual class for: " + fqn);
                }
                var actualSpiFactoryPath = ExpectActualUtils.descriptorNameToInternalName(actualData.spiFactoryName()) + ".class";
                mergeEntries.remove(actualSpiFactoryPath);
                var spiManifestPath = "META-INF/services/" + fqn + "$Factory";
                mergeEntries.remove(spiManifestPath);
            }
        }
    }
}
