package np.com.grishma.fingerprintauthenticatedlogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import np.com.grishma.fingerprintauthenticatedlogin.helper.FingerprintHandler;
import np.com.grishma.fingerprintauthenticatedlogin.server.User;
import np.com.grishma.fingerprintauthenticatedlogin.server.UserImpl;

/**
 * An activity that allows user to login to the app using Fingerprint Authentication
 * An implementation of {@link np.com.grishma.fingerprintauthenticatedlogin.helper.FingerprintHandler.Callback}
 * to handle response from {@link FingerprintHandler} which is an implementation of {@link android.hardware.fingerprint.FingerprintManager.AuthenticationCallback}
 */
public class FingerprintLoginActivity extends AppCompatActivity implements FingerprintHandler.Callback {

    @BindView(R.id.text_greetings)
    TextView textGreetings;

    private KeyStore keyStore;
    private Signature signature;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintHandler handler;
    private SharedPreferences sharedPreferences;
    private User user = new UserImpl();

    public FingerprintLoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_login);
        ButterKnife.bind(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // get an instance of keystore
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }

        // get an instance of signature to be used later
        try {
            signature = Signature.getInstance("SHA256withECDSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to get an instance of Signature", e);
        }

        handler = new FingerprintHandler(this);
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);

        // set greetings text showing which user had last enabled the fingerprint authentication for login
        textGreetings.setText("Login for " + sharedPreferences.getString("username", null));

        // initialize signature
        if (initSignature()) {
            // if successful create crypto object using the signature created
            cryptoObject = new FingerprintManager.CryptoObject(signature);
            // start listener for fingerprint authentication
            handler.startAuth(fingerprintManager, cryptoObject);
        } else {
            Toast.makeText(FingerprintLoginActivity.this, "Your previous key was invalidated", Toast.LENGTH_SHORT).show();

            // handles instances where previous key was invalidated, reset saved data in shared preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("fingerprintEnabled", false);
            editor.putString("username", null);
            editor.apply();

            // redirect back to normal login page
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    /**
     * Initialize the {@link Signature} instance with the created key in the
     *
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    private boolean initSignature() {
        try {
            keyStore.load(null);
            PrivateKey key = (PrivateKey) keyStore.getKey(MainActivity.KEY_NAME, null);
            signature.initSign(key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            Log.e("InitSignature", e.getMessage());
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init signature", e);
        }
    }

    @Override
    public void onAuthenticated() {
        Signature signature = cryptoObject.getSignature();
        String username = sharedPreferences.getString("username", null);

        if (username != null) {
            try {
                // encrypt signature with bytes from username
                // for now we use username to sign the signature
                signature.update(username.getBytes());
                byte[] sigBytes = signature.sign();

                // verify in server if the created signature is from authorized key
                if (user.verify(username, sigBytes)) {

                    // if successful let the user login
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();

                } else {
                    // if unsuccessful show error message
                    Toast.makeText(FingerprintLoginActivity.this, "User verification failed", Toast.LENGTH_SHORT).show();
                }
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onError() {
//        handler.stopListening();
        Toast.makeText(FingerprintLoginActivity.this, "Try logging in with password", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.text_sign_in_another_user)
    public void setOnClick(View view) {
        // redirect to normal login page if another user decides to login
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("fingerprintEnabled", false);
        editor.putString("username", null);
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.stopListening();
    }
}
