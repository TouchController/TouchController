package top.fifthlight.fabazel.tokenhelper;

import de.swiesend.secretservice.simple.SimpleCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.davidafsilva.apple.OSXKeychain;
import pt.davidafsilva.apple.OSXKeychainException;
import wincred.WinCred;

import java.io.IOException;
import java.util.Map;

public sealed abstract class TokenBackend {
    public abstract void saveToken(String tokenId, String tokenSecret) throws Exception;

    public abstract String getToken(String tokenId) throws Exception;

    public static TokenBackend getDefault() throws Exception {
        var systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")) {
            return new WindowsCredentialManager();
        } else if (systemName.contains("mac")) {
            return new MacOsKeychainService();
        } else {
            return new LinuxSecretService();
        }
    }

    public static final class LinuxSecretService extends TokenBackend {
        @Override
        public void saveToken(String tokenId, String tokenSecret) {
            try (var collection = new SimpleCollection()) {
                collection.createItem(tokenId, tokenSecret, Map.of("modrinth_token_id", tokenId));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public String getToken(String tokenId) throws IOException {
            try (var collection = new SimpleCollection()) {
                var items = collection.getItems(Map.of("modrinth_token_id", tokenId));
                if (items.isEmpty()) {
                    return null;
                }
                return new String(collection.getSecret(items.getFirst()));
            }
        }
    }

    public static final class MacOsKeychainService extends TokenBackend {
        private final OSXKeychain keychain = OSXKeychain.getInstance();

        public MacOsKeychainService() throws OSXKeychainException {
        }

        @Override
        public void saveToken(String tokenId, String tokenSecret) throws Exception {
            keychain.addGenericPassword("modrinth_token_id", tokenId, tokenSecret);
        }

        @Override
        public String getToken(String tokenId) throws Exception {
            return keychain.findGenericPassword("modrinth_token_id", tokenId).orElse(null);
        }
    }

    public static final class WindowsCredentialManager extends TokenBackend {
        private static final Logger logger = LoggerFactory.getLogger(WindowsCredentialManager.class);
        private static final WinCred winCred = new WinCred();

        @Override
        public void saveToken(String tokenId, String tokenSecret) throws IOException {
            winCred.setCredential(tokenId, "token", tokenSecret);
        }

        @Override
        public String getToken(String tokenId) {
            try {
                var cred = winCred.getCredential(tokenId);
                return cred.password();
            } catch (Exception ex) {
                logger.warn("Failed to get token from Windows Credential Manager: {}", ex.getMessage());
                return null;
            }
        }
    }
}
