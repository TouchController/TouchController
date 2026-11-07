package top.fifthlight.multijar.neov10;

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.moddiscovery.readers.JarModsDotTomlModFileReader;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileReader;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import net.neoforged.neoforgespi.locating.ModFileInfoParser;
import org.jspecify.annotations.Nullable;
import top.fifthlight.mergetools.api.MergeServiceMethod;

public class NeoV10ModReader implements IModFileReader {
    @SuppressWarnings({"ReturnValueIgnored", "ResultOfMethodCallIgnored"})
    public NeoV10ModReader() throws NoSuchMethodException {
        ModFileDiscoveryAttributes.class.getMethod("dependencyLocator");
        JarModsDotTomlModFileReader.class.getMethod("manifestParser", IModFile.class);
        IModFile.class.getMethod("create", JarContents.class, ModFileInfoParser.class, IModFile.Type.class, ModFileDiscoveryAttributes.class);
    }

    @Override
    @MergeServiceMethod
    public @Nullable IModFile read(JarContents jar, ModFileDiscoveryAttributes discoveryAttributes) {
        if (discoveryAttributes.dependencyLocator() instanceof NeoV10Locator) {
            return IModFile.create(jar, JarModsDotTomlModFileReader::manifestParser, IModFile.Type.LIBRARY, discoveryAttributes);
        }
        return null;
    }

    @Override
    @MergeServiceMethod
    public String toString() {
        return "MultiJar NeoV10";
    }

    @Override
    @MergeServiceMethod
    public int getPriority() {
        return LOWEST_SYSTEM_PRIORITY;
    }
}
