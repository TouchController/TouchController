package top.fifthlight.fabazel.devlaunchwrapper.plugin;

import top.fifthlight.fabazel.devlaunchwrapper.DevLaunchContext;
import top.fifthlight.fabazel.devlaunchwrapper.DevLaunchPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AssetsPlugin implements DevLaunchPlugin {
    @Override
    public int priority() {
        return 600;
    }

    @Override
    public void load(DevLaunchContext context) throws IOException {
        String assetsVersion = DevLaunchPlugin.property("assetsVersion");

        if (assetsVersion == null) {
            return;
        }

        Path assetsVersionPath = Paths.get(context.resolveRunfile(Paths.get(assetsVersion).normalize().toString()));
        Path assetsPath = assetsVersionPath.getParent().getParent().toRealPath();

        if ("true".equals(DevLaunchPlugin.property("legacyAssets"))) {
            Path resourcesDir = Paths.get("resources");
            Files.deleteIfExists(resourcesDir);
            Files.createSymbolicLink(resourcesDir, assetsPath.resolve("legacy"));
        } else {
            context.addArgs("--assetsDir", assetsPath.toString());
            String version = DevLaunchPlugin.property("version");
            if (version != null) {
                Path versionPath = assetsPath.resolve(Paths.get("versions", version));
                context.addArgs("--assetIndex", DevLaunchPlugin.readString(versionPath));
            }
        }
    }
}
