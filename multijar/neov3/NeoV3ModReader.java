package top.fifthlight.multijar.neov3;

import cpw.mods.jarhandling.JarContents;
import cpw.mods.jarhandling.SecureJar;
import net.neoforged.fml.loading.moddiscovery.readers.JarModsDotTomlModFileReader;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileReader;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import net.neoforged.neoforgespi.locating.ModFileInfoParser;
import org.jspecify.annotations.Nullable;
import top.fifthlight.mergetools.api.MergeServiceMethod;

public class NeoV3ModReader implements IModFileReader {
    @SuppressWarnings({"ReturnValueIgnored", "ResultOfMethodCallIgnored"})
    public NeoV3ModReader() throws NoSuchMethodException {
        ModFileDiscoveryAttributes.class.getMethod("dependencyLocator");
        SecureJar.class.getMethod("from", JarContents.class);
        JarModsDotTomlModFileReader.class.getMethod("manifestParser", IModFile.class);
        IModFile.class.getMethod("create", SecureJar.class, ModFileInfoParser.class, IModFile.Type.class, ModFileDiscoveryAttributes.class);
    }

    @Override
    @MergeServiceMethod
    public @Nullable IModFile read(JarContents jar, ModFileDiscoveryAttributes discoveryAttributes) {
        if (discoveryAttributes.dependencyLocator() instanceof NeoV3Locator) {
            return IModFile.create(SecureJar.from(jar), JarModsDotTomlModFileReader::manifestParser, IModFile.Type.LIBRARY, discoveryAttributes);
        }

        return null;
    }

    @Override
    @MergeServiceMethod
    public String toString() {
        return "MultiJar NeoV3";
    }

    @Override
    @MergeServiceMethod
    public int getPriority() {
        return LOWEST_SYSTEM_PRIORITY;
    }
}
