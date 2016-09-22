package np.com.grishma.fingerprintauthenticatedlogin.server;

import java.security.PublicKey;

/**
 * An interface that defines the methods required for the user backend.
 */
public interface User {
    /**
     * Verifies the authenticity of the provided username by confirming that it was signed with
     * the private key enrolled for the username.
     *
     * @param username          the username, its contents are signed
     *                          by the private key in the client side.
     * @param usernameSignature the signature of the username.
     * @return true if the signedSignature was verified, false otherwise. If this method returns
     * true, the server can consider the username is successful.
     */
    boolean verify(String username, byte[] usernameSignature);

    /**
     * Verifies the authenticity of the provided username by password.
     *
     * @param username the username of the user, its contents are signed by the
     *                 private key in the client side.
     * @param password the password for the user associated with the {@code username}.
     * @return true if the password is verified.
     */
    boolean verify(String username, String password);

    /**
     * Enrolls a public key associated with the userId
     *
     * @param username  the unique identifier of the user within the app including server side
     *                  implementation
     * @param password  the password for the user for the server side
     * @param publicKey the public key object to verify the signature from the user
     * @return true if the enrollment was successful, false otherwise
     */
    boolean enroll(String username, String password, PublicKey publicKey);
}

