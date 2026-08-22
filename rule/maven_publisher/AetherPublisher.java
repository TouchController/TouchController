package top.fifthlight.fabazel.mavenpublisher;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.jspecify.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.List;

@Singleton
@Named
public class AetherPublisher {
    @Inject
    private RepositorySystem repositorySystem;

    @Inject
    private LocalRepositoryManagerFactory localRepositoryManagerFactory;

    private RemoteRepository createRepository(String url, String username, String password) {
        var builder = new RemoteRepository.Builder("remote", "default", url);
        var auth = new AuthenticationBuilder();
        if (username != null) {
            auth.addUsername(username);
        }
        if (password != null) {
            auth.addPassword(password);
        }
        builder.setAuthentication(auth.build());
        return builder.build();
    }

    public void publishArtifacts(List<Artifact> artifacts, String repoUrl, @Nullable String username, @Nullable String password) throws NoLocalRepositoryManagerException, DeploymentException {
        var localRepoDirectory = Path.of(System.getProperty("user.home"), ".m2", "repository").toFile();
        var session = new DefaultRepositorySystemSession();
        session.setConfigProperty("aether.checksums.algorithms", "MD5,SHA-1,SHA-256,SHA-512");
        session.setLocalRepositoryManager(localRepositoryManagerFactory.newInstance(session, new LocalRepository(localRepoDirectory)));
        var repository = createRepository(repoUrl, username, password);

        var deployRequest = new DeployRequest();
        deployRequest.setRepository(repository);
        deployRequest.setArtifacts(artifacts);

        repositorySystem.deploy(session, deployRequest);
    }
}
