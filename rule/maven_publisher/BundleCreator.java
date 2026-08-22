package top.fifthlight.fabazel.mavenpublisher;

import org.eclipse.aether.artifact.Artifact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BundleCreator implements AutoCloseable {
    private final ZipOutputStream zos;
    private final List<Map.Entry<String, MessageDigest>> digests = Stream.of(
                    Map.entry("md5", "MD5"),
                    Map.entry("sha1", "SHA-1"),
                    Map.entry("sha256", "SHA-256"),
                    Map.entry("sha512", "SHA-512"))
            .map(Utils.unchecked(entry -> Map.entry(entry.getKey(), MessageDigest.getInstance(entry.getValue()))))
            .toList();

    private BundleCreator(ZipOutputStream zos) {
        this.zos = zos;
    }

    public BundleCreator(Path bundlePath) throws IOException {
        this(new ZipOutputStream(Files.newOutputStream(bundlePath)));
    }

    private void addZipEntry(String zipPath, Path path) throws IOException {
        zos.putNextEntry(new ZipEntry(zipPath));
        DigestInputStream[] digestInputStreams = new DigestInputStream[digests.size()];
        try (var stream = Files.newInputStream(path)) {
            var inputStream = stream;
            for (var i = 0; i < digests.size(); i++) {
                var digestInputStream = new DigestInputStream(inputStream, digests.get(i).getValue());
                digestInputStreams[i] = digestInputStream;
                inputStream = digestInputStream;
            }
            inputStream.transferTo(zos);
        }
        zos.closeEntry();
        for (var i = 0; i < digests.size(); i++) {
            var suffix = digests.get(i).getKey();
            var digest = digestInputStreams[i].getMessageDigest();
            zos.putNextEntry(new ZipEntry(zipPath + "." + suffix));
            zos.write(HexFormat.of().formatHex(digest.digest()).getBytes(StandardCharsets.US_ASCII));
            zos.closeEntry();
        }
    }

    public void addArtifact(Artifact artifact) throws IOException {
        var baseDirectory = artifact.getGroupId().replace('.', '/') + '/' + artifact.getArtifactId() + '/' + artifact.getVersion();
        var fileName = Utils.artifactFileName(artifact);
        addZipEntry(baseDirectory + "/" + fileName, artifact.getFile().toPath());
    }

    @Override
    public void close() throws IOException {
        zos.close();
    }
}
