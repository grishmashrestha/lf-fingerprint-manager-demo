package np.com.grishma.fingerprintauthenticatedlogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

    @BindView(R.id.text_greetings_name)
    TextView textGreetings;

    private FingerprintHandler handler;
    private SharedPreferences sharedPreferences;
    private User user = new UserImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_login);
        ButterKnife.bind(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        handler = new FingerprintHandler(this);
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);

        // set greetings text showing which user had last enabled the fingerprint authentication for login
        textGreetings.setText(sharedPreferences.getString(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED_USERNAME, ""));

        // start listener for fingerprint authentication, authenticate null
        handler.startAuth(fingerprintManager, null);
    }

    @Override
    public void onAuthenticated() {
        // new code without implementation of asymmetric keys
        String username = sharedPreferences.getString(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED_USERNAME, null);
        String password = sharedPreferences.getString(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED_PASSWORD, null);

        if (username != null) {
            if (user.verify(username, password)) {

                // if successful, let the user login
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();

            } else {
                // if unsuccessful show error message
                Toast.makeText(FingerprintLoginActivity.this, "User verification failed from server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onError() {
        Toast.makeText(FingerprintLoginActivity.this, "Try logging in with password", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_sign_in_another_user)
    public void setOnClick(View view) {
        // redirect to normal login page if another user decides to login
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED, false);
        editor.putString(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED_USERNAME, null);
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
