package pmujtmk.yeo.myprice;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.TimeUnit;

/** Provides short-lived Firebase credentials to the existing Python database adapter. */
public final class FirebaseBridge {
    private FirebaseBridge() {
    }

    public static String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Store sign-in is required");
        }
        return user.getUid();
    }

    public static String getEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            throw new IllegalStateException("Store email is unavailable");
        }
        return user.getEmail();
    }

    public static String getIdToken() throws Exception {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Store sign-in is required");
        }
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            throw new IllegalStateException("Network access must run off the UI thread");
        }
        String token = Tasks.await(user.getIdToken(false), 30, TimeUnit.SECONDS).getToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Firebase sign-in expired");
        }
        return token;
    }
}
