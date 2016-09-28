package np.com.grishma.fingerprintauthenticatedlogin;

import android.app.Application;
import android.content.Context;

public class FingerprintAuthenticatedLogin extends Application {

    public static final String FINGERPRINT_ENABLED = "fingerprintEnabled";
    public static final String FINGERPRINT_ENABLED_USERNAME = "usernameForFingerprint";
    public static final String FINGERPRINT_ENABLED_PASSWORD = "passwordForFingerprint";

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    /**
     * Gives the {@link Context} of the app
     *
     * @return {@link Context} the app belongs to
     */
    public static Context getContext() {
        return context;
    }
}
