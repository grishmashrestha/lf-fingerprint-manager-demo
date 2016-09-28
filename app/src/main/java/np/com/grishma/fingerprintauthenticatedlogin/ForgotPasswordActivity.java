package np.com.grishma.fingerprintauthenticatedlogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import np.com.grishma.fingerprintauthenticatedlogin.server.User;
import np.com.grishma.fingerprintauthenticatedlogin.server.UserImpl;

/**
 * An activity to that provides user interface to change password for a user
 * It will also take note if the user has allowed fingerprint auth for the next login
 */
public class ForgotPasswordActivity extends AppCompatActivity {
    @BindView(R.id.edit_username)
    TextView username;

    @BindView(R.id.edit_new_password)
    TextView newPassword;

    @BindView(R.id.checkbox_fingerprint_authentication)
    CheckBox checkBoxFingerprintAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_reset_password)
    public void setOnClick(View view) {
        String usernameTemp = username.getText().toString();
        String passwordTemp = newPassword.getText().toString();

        User user = new UserImpl();
        // reset password in server
        // currently update the params in sharedPreferences itself
        if (user.resetPasswordViaServer(usernameTemp, passwordTemp)) {

            // if user has checked fingerprint auth as login method for next login
            // save the user details in the app(sharedPreference)
            if (checkBoxFingerprintAuth.isChecked()) {
                // save on shared preferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED, true);
                editor.putString(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED_USERNAME, usernameTemp);
                editor.putString(FingerprintAuthenticatedLogin.FINGERPRINT_ENABLED_PASSWORD, passwordTemp);
                editor.apply();
            }
            Toast.makeText(ForgotPasswordActivity.this, R.string.message_password_reset_success, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Toast.makeText(ForgotPasswordActivity.this, R.string.message_password_reset_failure, Toast.LENGTH_SHORT).show();
        }
    }
}
