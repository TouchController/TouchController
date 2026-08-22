package top.fifthlight.fabazel.mavenpublisher;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.sisu.Parameters;
import org.eclipse.sisu.launch.Main;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

@Named
public class MavenPublisher {
    @Inject
    private AetherPublisher aetherPublisher;

    @Inject
    @Parameters
    private String[] args;

    public int run() {
        var cli = new Cli(aetherPublisher);
        return new CommandLine(cli).execute(args);
    }

    public static void main(String[] args) {
        var root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        var publisher = Main.boot(MavenPublisher.class, args);
        System.exit(publisher.run());
    }

    @Command(name = "maven-publisher", mixinStandardHelpOptions = true, description = "Publishes artifacts to a Maven repository")
    private static class Cli implements Callable<Integer> {
        private final AetherPublisher aetherPublisher;

        @Option(names = {"--groupId"}, description = "Maven coordinate groupId", required = true)
        private String groupId;

        @Option(names = {"--artifactId"}, description = "Maven coordinate artifactId", required = true)
        private String artifactId;

        @Option(names = {"--version"}, description = "Maven coordinate version", required = true)
        private String version;

        @Option(names = {"--artifact"}, description = "Artifact in format 'classifier:extension=file_path' (classifier can be empty for main artifact)", required = true)
        private List<String> artifacts = new ArrayList<>();

        @Option(names = {"--pom"}, description = "POM file path")
        private File pomFile;

        @Option(names = {"--repo-url"}, description = "URL of Maven repository to be published")
        private String mavenRepoUrl;

        @Option(names = {"--bundle"}, description = "Path of ZIP bundle")
        private Path bundleFile;

        @Option(names = {"--sign"}, description = "Sign artifacts with GPG, generating .asc signature files")
        private boolean sign;

        @Option(names = {"--gpg-key"}, description = "GPG key id or fingerprint to sign with")
        private String gpgKey;

        private Cli(AetherPublisher aetherPublisher) {
            this.aetherPublisher = aetherPublisher;
        }

        @Override
        public Integer call() throws Exception {
            var artifactItems = new ArrayList<Artifact>();

            if (pomFile != null) {
                var pomArtifact = new DefaultArtifact(groupId, artifactId, "pom", version).setFile(pomFile);
                artifactItems.add(pomArtifact);
            }

            var repoUrl = Optional.ofNullable(mavenRepoUrl)
                    .or(() -> Optional.ofNullable(System.getenv("MAVEN_REPO_URL")).filter(env -> !env.isEmpty()));
            var repoUsername = Optional.ofNullable(System.getenv("MAVEN_USER")).filter(env -> !env.isEmpty());
            var repoPassword = Optional.ofNullable(System.getenv("MAVEN_PASSWORD")).filter(env -> !env.isEmpty());

            var baseDir = Optional.ofNullable(System.getenv("BASEDIR"))
                    .filter(env -> !env.isEmpty())
                    .map(Path::of);
            var bundlePath = Optional.ofNullable(bundleFile)
                    .map(baseDir.map(dir -> (Function<Path, Path>) dir::resolve).orElse(Function.identity()));

            if (repoUrl.isEmpty() && bundlePath.isEmpty()) {
                System.err.println("No Maven repository URL or bundle path");
                return 1;
            }

            var gpgKeyId = Optional.ofNullable(gpgKey).or(() -> Optional.ofNullable(System.getenv("MAVEN_GPG_KEY"))
                    .filter(env -> !env.isEmpty()));
            var passphrase = Optional.ofNullable(System.getenv("MAVEN_GPG_PASSPHRASE"))
                    .filter(env -> !env.isEmpty());

            ArtifactSigner signer = null;
            try {
                if (sign) {
                    signer = new ArtifactSigner(gpgKeyId.orElse(null), passphrase.orElse(null));
                }

                for (var artifactSpec : artifacts) {
                    var parts = artifactSpec.split("=", 2);
                    if (parts.length != 2) {
                        System.err.println("Error: Invalid artifact format: " + artifactSpec + ". Expected 'classifier:extension=file_path'");
                        return 1;
                    }

                    var classifierExt = parts[0];
                    var filePath = parts[1];

                    var classifierParts = classifierExt.split(":", 2);
                    var classifier = "";
                    var extension = "jar";

                    if (classifierParts.length == 2) {
                        classifier = classifierParts[0];
                        extension = classifierParts[1];
                    } else if (classifierParts.length == 1) {
                        if (!classifierParts[0].isEmpty()) {
                            extension = classifierParts[0];
                        }
                    }

                    var artifactFile = new File(filePath);
                    if (!artifactFile.exists()) {
                        System.err.println("Error: Artifact file does not exist: " + filePath);
                        return 1;
                    }

                    var artifact = new DefaultArtifact(groupId, artifactId, classifier, extension, version).setFile(artifactFile);
                    artifactItems.add(artifact);
                }

                List<Artifact> publishArtifacts = artifactItems;
                if (signer != null) {
                    publishArtifacts = new ArrayList<>();
                    for (var artifact : artifactItems) {
                        publishArtifacts.add(artifact);
                        var signArtifact = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), artifact.getExtension() + ".asc", version);
                        publishArtifacts.add(signArtifact.setFile(signer.signFile(artifact.getFile().toPath()).toFile()));
                    }
                }

                if (repoUrl.isPresent()) {
                    aetherPublisher.publishArtifacts(publishArtifacts, repoUrl.get(), repoUsername.orElse(null), repoPassword.orElse(null));
                    System.err.println("Successfully deployed " + artifacts.size() + " artifact(s) to: " + repoUrl.get());
                }
                if (bundlePath.isPresent()) {
                    try (var bundleCreator = new BundleCreator(bundlePath.get())) {
                        for (var artifact : publishArtifacts) {
                            bundleCreator.addArtifact(artifact);
                        }
                    }
                    System.err.println("Successfully created bundle with " + artifacts.size() + " artifact(s) to: " + bundlePath.get().toAbsolutePath());
                }
                for (var artifact : publishArtifacts) {
                    System.err.println("  - " + Utils.artifactFileName(artifact));
                }
                return 0;
            } catch (Exception e) {
                System.err.println("Error deploying artifacts: " + e.getMessage());
                e.printStackTrace(System.err);
                return 1;
            } finally {
                if (signer != null) {
                    signer.close();
                }
            }
        }
    }
}
