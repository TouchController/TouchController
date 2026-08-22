package top.fifthlight.fabazel.mavenpublisher;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ArtifactSigner implements AutoCloseable {
    private final Path signOutputPath;
    private final @Nullable String keyId;
    private final @Nullable String passphrase;

    public ArtifactSigner(Path signOutputPath, @Nullable String keyId, @Nullable String passphrase) {
        this.signOutputPath = signOutputPath;
        this.keyId = keyId;
        this.passphrase = passphrase;
    }

    public ArtifactSigner(@Nullable String keyId, @Nullable String passphrase) throws IOException {
        this(Files.createTempDirectory("maven-publisher-gpg"), keyId, passphrase);
    }

    public Path signFile(Path input) throws IOException, InterruptedException {
        var outputFile = Files.createTempFile(signOutputPath, "sign", ".asc");
        var command = new ArrayList<String>();
        command.add("gpg");
        command.add("--batch");
        command.add("--yes");
        if (keyId != null && !keyId.isEmpty()) {
            command.add("--local-user");
            command.add(keyId);
        }
        if (passphrase != null) {
            command.add("--pinentry-mode");
            command.add("loopback");
            command.add("--passphrase-fd");
            command.add("0");
        }
        command.add("--armor");
        command.add("--detach-sign");
        command.add("--output");
        command.add(outputFile.toAbsolutePath().toString());
        command.add(input.toAbsolutePath().toString());

        Process process;
        try {
            process = new ProcessBuilder(command).redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new IOException("Failed to start gpg; please make sure GPG is installed: " + e.getMessage(), e);
        }
        try (var stdin = process.getOutputStream()) {
            if (passphrase != null) {
                var buffer = StandardCharsets.UTF_8.encode(passphrase);
                stdin.write(buffer.array(), buffer.arrayOffset(), buffer.limit());
                Arrays.fill(buffer.array(), buffer.arrayOffset(), buffer.limit(), (byte) 0);
                stdin.flush();
            }
        }
        var processOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        var exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("gpg failed with exit code " + exitCode + ":\n" + processOutput.strip());
        }
        return outputFile;
    }

    @Override
    public void close() throws IOException {
        try (var paths = Files.walk(signOutputPath)) {
            for (var it = paths.sorted(Comparator.reverseOrder()).iterator(); it.hasNext(); ) {
                Files.delete(it.next());
            }
        }
    }
}
