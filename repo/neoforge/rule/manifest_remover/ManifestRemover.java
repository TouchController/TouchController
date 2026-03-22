import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ManifestRemover {
    public static void main(String[] args) throws Exception {
        var inputJar = Path.of(args[0]);
        var outputJar = Path.of(args[1]);
        try (var inputZip = new ZipInputStream(Files.newInputStream(inputJar))) {
            try (var outputZip = new ZipOutputStream(Files.newOutputStream(outputJar))) {
                ZipEntry entry;
                while ((entry = inputZip.getNextEntry()) != null) {
                    if (!entry.getName().startsWith("META-INF/")) {
                        outputZip.putNextEntry(entry);
                        inputZip.transferTo(outputZip);
                    }
                }
            }
        }
    }
}
