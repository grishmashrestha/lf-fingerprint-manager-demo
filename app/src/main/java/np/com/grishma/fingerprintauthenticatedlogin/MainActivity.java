package np.com.grishma.fingerprintauthenticatedlogin;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import np.com.grishma.fingerprintauthenticatedlogin.server.User;
import np.com.grishma.fingerprintauthenticatedlogin.server.UserImpl;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {

    public static final String KEY_NAME = "lf_fingerprint_auth";

    @BindView(R.id.edit_username)
    EditText username;

    @BindView(R.id.edit_password)
    EditText password;

    @BindView(R.id.button_login)
    Button buttonLogin;

    @BindView(R.id.checkbox_fingerprint_authentication)
    CheckBox checkBoxFingerprintAuth;

    private KeyStore keyStore;
    private KeyPairGenerator keyPairGenerator;
    private SharedPreferences sharedPreferences;
    private User user = new UserImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // initialize required parameters
        KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // get an instance of keystore
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }

        // get an instance of keypair generator to create asymmetric key pairs, i.e. public key and private key
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyPairGenerator", e);
        }

        if (sharedPreferences.getBoolean("fingerprintEnabled", false)) {
            startActivity(new Intent(this, FingerprintLoginActivity.class));
            finish();
        }
    }

    @OnClick(R.id.button_login)
    public void setOnClick(View view) {

        String usernameString = username.getText().toString();
        String passwordString = password.getText().toString();
        // verify user in the server
        if (user.verify(usernameString, passwordString)) {
            if (checkBoxFingerprintAuth.isChecked()) {
                // create a keypair
                createKeyPair();
                // enroll process below:
                // send public key to server
                enroll();
            }
        }

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("username", usernameString);
        startActivity(intent);
        finish();
    }

    private void enroll() {
        try {
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            keyStore.load(null);
            PublicKey publicKey = keyStore.getCertificate(KEY_NAME).getPublicKey();

            if (user.enroll("username", "password", publicKey)) {
                // save on shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("fingerprintEnabled", true);
                editor.putString("username", username.getText().toString());
                editor.apply();
            }
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates an asymmetric key pair in the Android Keystore. Every use of the private key must
     * be authorized by the user authenticating with fingerprint. Public key use is unrestricted.
     */
    public void createKeyPair() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(KEY_NAME,
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                            // Require the user to authenticate with a fingerprint to authorize
                            // every use of the private key
                            .setUserAuthenticationRequired(true)
                            .build());
            keyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }
}

