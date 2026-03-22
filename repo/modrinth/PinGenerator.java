import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PinGenerator {
    private static final String PIN_SOURCE = "$PIN_SOURCE";
    private static final String PIN_TARGET = "$PIN_TARGET";

    public static void main(String[] args) throws Exception {
        var sourcePath = Path.of(PIN_SOURCE);
        var targetPath = Path.of(PIN_TARGET);
        Files.createDirectories(targetPath.getParent());
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.err.println("Pin file copied successfully: " + targetPath);
    }
}
