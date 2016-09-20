package np.com.grishma.fingerprintauthenticatedlogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edit_username)
    EditText username;

    @BindView(R.id.button_login)
    Button buttonLogin;

    @BindView(R.id.checkbox_fingerprint_authentication)
    CheckBox checkBoxFingerprintAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_login)
    public void setOnClick(View view) {
        if (checkBoxFingerprintAuth.isChecked()) {
            // verify user
            // enroll
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }


    }
}
