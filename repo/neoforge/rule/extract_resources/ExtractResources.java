import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.jar.*;

public class ExtractResources {
    public static void main(String[] args) throws IOException {
        var inputPath = Path.of(args[0]);
        var outputPath = Path.of(args[1]);
        try (var jarFile = new JarFile(inputPath.toFile());
             var jos = new JarOutputStream(new FileOutputStream(outputPath.toFile()))) {
            var entries = new ArrayList<>(Collections.list(jarFile.entries()));
            entries.sort(Comparator.comparing(JarEntry::getName));
            for (var entry : entries) {
                if (entry.isDirectory())
                    continue;
                if (entry.getName().endsWith(".java"))
                    continue;
                var newEntry = new JarEntry(entry.getName());
                newEntry.setTime(0L);
                newEntry.setCreationTime(FileTime.fromMillis(0L));
                newEntry.setLastModifiedTime(FileTime.fromMillis(0L));
                jos.putNextEntry(newEntry);
                try (var is = jarFile.getInputStream(entry)) {
                    is.transferTo(jos);
                }
                jos.closeEntry();
            }
        }
    }
}
