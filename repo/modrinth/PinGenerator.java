import com.google.devtools.build.runfiles.AutoBazelRepository;
import com.google.devtools.build.runfiles.Runfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@AutoBazelRepository
public class PinGenerator {
    public static void main(String[] args) throws Exception {
        var runfiles = Runfiles.preload().withSourceRepository(AutoBazelRepository_PinGenerator.NAME);
        var sourcePath = Path.of(runfiles.rlocation(System.getProperty("pin.source")));
        var targetPath = Path.of(System.getProperty("pin.target"));
        if (targetPath.getParent() != null) {
            Files.createDirectories(targetPath.getParent());
        }
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.err.println("Pin file copied successfully: " + targetPath);
    }
}
