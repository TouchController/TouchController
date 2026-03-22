package wincred;

import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// https://stackoverflow.com/questions/65144266/use-the-windows-credential-manager-in-java-to-get-credentials-for-authentication
public class WinCred implements CredAdvapi32 {

    public static <T> T coalesce(final T maybeNullValue, final T nonNullValue) {
        return maybeNullValue == null ? nonNullValue : maybeNullValue;
    }

    public record Credential(String target, String username, String password) {
            public Credential(String target, String username, String password) {
                this.target = coalesce(target, "");
                this.username = coalesce(username, "");
                this.password = coalesce(password, "");
            }
        }

    public Credential getCredential(String target) {
        var pcredMem = new PCREDENTIAL();

        try {
            if (CredRead(target, 1, 0, pcredMem)) {
                var credMem = new CREDENTIAL(pcredMem.credential);
                var passwordBytes = credMem.CredentialBlob.getByteArray(0, credMem.CredentialBlobSize);

                var password = new String(passwordBytes, StandardCharsets.UTF_16LE);
                return new Credential(credMem.TargetName, credMem.UserName, password);
            } else {
                var err = Native.getLastError();
                throw new LastErrorException(err);
            }
        } finally {
            CredFree(pcredMem.credential);
        }
    }

    public void setCredential(String target, String userName, String password) throws UnsupportedEncodingException {
        var credMem = new CREDENTIAL();

        credMem.Flags = 0;
        credMem.TargetName = target;
        credMem.Type = CRED_TYPE_GENERIC;
        credMem.UserName = userName;
        credMem.AttributeCount = 0;
        credMem.Persist = CRED_PERSIST_ENTERPRISE;
        var bpassword = password.getBytes(StandardCharsets.UTF_16LE);
        credMem.CredentialBlobSize = bpassword.length;
        credMem.CredentialBlob = getPointer(bpassword);
        if (!CredWrite(credMem, 0)) {
            var err = Native.getLastError();
            throw new LastErrorException(err);
        }
    }

    public void deleteCredential(String target) throws UnsupportedEncodingException {
        if (!CredDelete(target, CRED_TYPE_GENERIC, 0)) {
            var err = Native.getLastError();
            throw new LastErrorException(err);
        }
    }

    private static Pointer getPointer(byte[] array) {
        Pointer p = new Memory(array.length);
        p.write(0, array, 0, array.length);

        return p;
    }

    @Override
    public boolean CredRead(String targetName, int type, int flags, PCREDENTIAL pcredential) throws LastErrorException {
        synchronized (INSTANCE) {
            return INSTANCE.CredRead(targetName, type, flags, pcredential);
        }
    }

    @Override
    public boolean CredWrite(CREDENTIAL credential, int flags) throws LastErrorException {
        synchronized (INSTANCE) {
            return INSTANCE.CredWrite(credential, flags);
        }
    }

    @Override
    public boolean CredDelete(String targetName, int type, int flags) throws LastErrorException {
        synchronized (INSTANCE) {
            return INSTANCE.CredDelete(targetName, type, flags);
        }
    }

    @Override
    public void CredFree(Pointer credential) throws LastErrorException {
        synchronized (INSTANCE) {
            INSTANCE.CredFree(credential);
        }
    }
}
