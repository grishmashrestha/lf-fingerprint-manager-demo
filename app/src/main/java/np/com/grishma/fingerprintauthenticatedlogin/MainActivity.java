package np.com.grishma.fingerprintauthenticatedlogin;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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

    private SharedPreferences sharedPreferences;
    private User user = new UserImpl();
    private FingerprintManager fingerprintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // initialize required parameters
        KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
        fingerprintManager = getSystemService(FingerprintManager.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        if (sharedPreferences.getBoolean(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED, false)) {
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
                        // enroll process below
                        enroll(usernameString, passwordString);
                    }
                    intent = new Intent(this, DashboardActivity.class);
                    intent.putExtra("username", usernameString);
                } else {
                    Toast.makeText(MainActivity.this, R.string.message_wrong_credentials, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.button_forgot_password:
                intent = new Intent(this, ForgotPasswordActivity.class);
                Toast.makeText(MainActivity.this, R.string.message_reset_password_via_app, Toast.LENGTH_SHORT).show();
                break;

            case R.id.button_forgot_password_server:
                Toast.makeText(MainActivity.this, R.string.message_reset_password_via_server, Toast.LENGTH_SHORT).show();
                intent = new Intent(this, ForgotPasswordViaServerActivity.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }

        if (intent != null && view.getId() == R.id.button_login)
            finish();
    }

    /**
     * Register the username and password for fingerprint authentication in user's current device
     * For demo purpose, it is saved in shared preference under
     * the keys "usernameForFingerprint" for username
     * and "passwordForFingerprint" for password
     * Also, another flag "fingerprintEnabled" is set to true, so that next time a user
     * logs out and tries to login, the app remembers the last user to allow fingerprint auth for login
     * and shows {@link FingerprintLoginActivity}
     * instead of normal username/password login page {@link MainActivity}
     *
     * @param usernameTemp username of the user that wants fingerprint auth
     * @param passwordTemp password of the user that wants fingerprint auth
     */
    public void enroll(String usernameTemp, String passwordTemp) {
        // save on shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED, true);
        editor.putString(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED_USERNAME, usernameTemp);
        editor.putString(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED_PASSWORD, passwordTemp);
        editor.apply();
    }

}

