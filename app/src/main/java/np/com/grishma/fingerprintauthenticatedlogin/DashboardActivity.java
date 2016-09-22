package np.com.grishma.fingerprintauthenticatedlogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dashboard for user after successful login
 */
public class DashboardActivity extends AppCompatActivity {

    @BindView(R.id.text_greetings)
    TextView greetings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        String username = extras.getString("username");
        if (username != null) {
            greetings.setText("Welcome back " + username + " to your dashboard!");
        }

    }

    @OnClick(R.id.button_logout)
    public void setOnClick(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
