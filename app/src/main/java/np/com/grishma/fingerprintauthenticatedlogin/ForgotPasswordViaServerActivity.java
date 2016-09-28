package np.com.grishma.fingerprintauthenticatedlogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import np.com.grishma.fingerprintauthenticatedlogin.server.User;
import np.com.grishma.fingerprintauthenticatedlogin.server.UserImpl;

public class ForgotPasswordViaServerActivity extends AppCompatActivity {

    @BindView(R.id.edit_username)
    TextView username;

    @BindView(R.id.edit_new_password)
    TextView newPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_via_server);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_reset_password)
    public void setOnClick(View view) {
        String usernameTemp = username.getText().toString();
        String passwordTemp = newPassword.getText().toString();

        User user = new UserImpl();
        if (user.resetPasswordViaServer(usernameTemp, passwordTemp)) {
            Toast.makeText(ForgotPasswordViaServerActivity.this, "Password successfully reset", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Toast.makeText(ForgotPasswordViaServerActivity.this, "Password could not be reset", Toast.LENGTH_SHORT).show();
        }
    }
}
