package np.com.grishma.fingerprintauthenticatedlogin.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context appContext;
    Callback callback;
    boolean mSelfCancelled;


    public FingerprintHandler(Context context) {
        appContext = context;
        callback = (Callback) context;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mSelfCancelled = false;
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    public void stopListening() {
        if (cancellationSignal != null) {
            mSelfCancelled = true;
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            Toast.makeText(appContext, errString, Toast.LENGTH_SHORT).show();
            if (errMsgId == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                callback.onError();
            }
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(appContext, helpString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(appContext, "Fingerprint not recognized. Try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Toast.makeText(appContext, "Authentication succeeded", Toast.LENGTH_SHORT).show();
        callback.onAuthenticated();
    }

    /**
     * an interface to communicate the results of fingerprint authentication to its parent view
     */
    public interface Callback {

        void onAuthenticated();

        void onError();
    }
}