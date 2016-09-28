package np.com.grishma.fingerprintauthenticatedlogin;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

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
    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // initialize required parameters
        keyguardManager = getSystemService(KeyguardManager.class);
        fingerprintManager = getSystemService(FingerprintManager.class);
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Please grant permission for Fingerprint access", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!fingerprintManager.isHardwareDetected()) {
            Toast.makeText(MainActivity.this, "Fingerprint hardware not detected", Toast.LENGTH_SHORT).show();
            checkBoxFingerprintAuth.setVisibility(View.INVISIBLE);
        } else if (!keyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a fingerprint or lock screen.
            Toast.makeText(this,
                    "Secure lock screen hasn't set up.\n"
                            + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                    Toast.LENGTH_LONG).show();
            checkBoxFingerprintAuth.setVisibility(View.INVISIBLE);
            return;
        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
            checkBoxFingerprintAuth.setVisibility(View.INVISIBLE);
            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (sharedPreferences.getBoolean("fingerprintEnabled", false)) {
            startActivity(new Intent(this, FingerprintLoginActivity.class));
            finish();
        }
    }

    @OnClick({R.id.button_login, R.id.button_forgot_password, R.id.button_forgot_password_server})
    public void setOnClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.button_login:

                String usernameString = username.getText().toString();
                String passwordString = password.getText().toString();

                // verify user in the server
                if (user.verify(usernameString, passwordString)) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    if (checkBoxFingerprintAuth.isChecked() && fingerprintManager.hasEnrolledFingerprints()) {
                        // enroll process below:
                        enroll();
                    }
                    intent = new Intent(this, DashboardActivity.class);
                    intent.putExtra("username", usernameString);
                } else {
                    Toast.makeText(MainActivity.this, "Wrong credentials", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.button_forgot_password:
                intent = new Intent(this, ForgotPasswordActivity.class);
                Toast.makeText(MainActivity.this, "Reset password via app", Toast.LENGTH_SHORT).show();
                break;

            case R.id.button_forgot_password_server:
                Toast.makeText(MainActivity.this, "Reset password via server", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, ForgotPasswordViaServerActivity.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }

        if (intent != null && view.getId() == R.id.button_login)
            finish();
    }

    public void enroll() {
        // no implementation of public key in server
        String usernameTemp = username.getText().toString();
        String passwordTemp = password.getText().toString();
        // save on shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("fingerprintEnabled", true);
        editor.putString("usernameForFingerprint", usernameTemp);
        editor.putString("passwordForFingerprint", passwordTemp);
        editor.apply();
    }

}

