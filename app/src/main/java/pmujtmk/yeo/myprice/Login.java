package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    private static final String DATABASE_URL = "https://suprise1happyday-default-rtdb.asia-southeast1.firebasedatabase.app";
    private EditText emailInput;
    private EditText passwordInput;
    private LoadingDialog loadingDialog;
    private PyObject localDatabase;
    private FirebaseAuth auth;
    private DatabaseReference root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login);

        loadingDialog = new LoadingDialog(this);
        auth = FirebaseAuth.getInstance();
        root = FirebaseDatabase.getInstance(DATABASE_URL).getReference();
        if (!Python.isStarted()) Python.start(new AndroidPlatform(this));
        localDatabase = Python.getInstance().getModule("Database");

        emailInput = findViewById(R.id.et_email_login);
        passwordInput = findViewById(R.id.et_password_login);
        Button loginButton = findViewById(R.id.btn_login_login);
        Button registerButton = findViewById(R.id.btn_register_login);
        Button resetButton = findViewById(R.id.btn_forgetpassword_login);

        loginButton.setOnClickListener(view -> signIn());
        registerButton.setOnClickListener(view -> startActivity(new Intent(this, Register.class)));
        resetButton.setOnClickListener(view -> startActivity(new Intent(this, FindAccount.class)));
    }

    private void signIn() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Required");
            return;
        }

        try {
            localDatabase.callAttr("ResetLoginTable");
        } catch (Exception e) {
            showMessage("Could not open local account data. Please restart the app.");
            return;
        }
        auth.signOut();
        loadingDialog.Start("Signing in...");
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful() || auth.getCurrentUser() == null) {
                loadingDialog.Dismiss();
                showMessage("Sign-in failed. Check your email and password, then try again.");
                return;
            }
            FirebaseUser user = auth.getCurrentUser();
            user.reload().addOnCompleteListener(this, reload -> {
                if (!reload.isSuccessful() || auth.getCurrentUser() == null) {
                    deny("Could not check email verification. Please try again.");
                    return;
                }
                FirebaseUser refreshedUser = auth.getCurrentUser();
                if (!refreshedUser.isEmailVerified()) {
                    refreshedUser.sendEmailVerification().addOnCompleteListener(this, sent ->
                            deny(sent.isSuccessful()
                                    ? "Please open the verification link sent to your email, then sign in again."
                                    : "Your email is not verified. Check your inbox or try again later to resend the link."));
                    return;
                }
                String uid = refreshedUser.getUid();
                refreshedUser.getIdToken(true).addOnCompleteListener(this, tokenTask -> {
                    if (!tokenTask.isSuccessful()) {
                        deny("Could not refresh your store sign-in. Please try again.");
                        return;
                    }
                    root.child("merchantApprovals").child(uid).get().addOnCompleteListener(this, approval -> {
                        if (!approval.isSuccessful()) {
                            deny("Could not check store approval. Please try again.");
                            return;
                        }
                        if (!Boolean.TRUE.equals(approval.getResult().getValue(Boolean.class))) {
                            deny("Your store is waiting for approval in Firebase Console.");
                            return;
                        }
                        loadStore(uid, refreshedUser.getEmail());
                    });
                });
            });
        });
    }

    private void loadStore(String uid, String email) {
        root.child("stores").child(uid).get().addOnCompleteListener(this, storeTask -> {
            if (!storeTask.isSuccessful()) {
                deny("Could not load your store. Please check the database rules and connection.");
                return;
            }
            DataSnapshot store = storeTask.getResult();
            if (store.exists()) {
                openStore(uid, email, store.child("name").getValue(String.class),
                        store.child("location").getValue(String.class));
                return;
            }
            // The owner sets only merchantApprovals/{uid}=true. Create the public listing
            // from the owner's private registration request on first approved login.
            root.child("approvalRequests").child(uid).get().addOnCompleteListener(this, requestTask -> {
                if (!requestTask.isSuccessful() || !requestTask.getResult().exists()) {
                    deny("Your store details are missing. Contact the MyPrice owner.");
                    return;
                }
                DataSnapshot request = requestTask.getResult();
                String name = request.child("storeName").getValue(String.class);
                String location = request.child("location").getValue(String.class);
                if (name == null || location == null || name.trim().isEmpty() || location.trim().isEmpty()) {
                    deny("Your store details are incomplete. Contact the MyPrice owner.");
                    return;
                }
                Map<String, Object> privateProfile = new HashMap<>();
                privateProfile.put("name", name);
                privateProfile.put("location", location);
                privateProfile.put("email", email);
                Map<String, Object> publicStore = new HashMap<>();
                publicStore.put("name", name);
                publicStore.put("location", location);
                root.child("merchantProfiles").child(uid).setValue(privateProfile).addOnCompleteListener(this, profileSaved -> {
                    if (!profileSaved.isSuccessful()) {
                        deny("Could not save your store profile. Check the database rules.");
                        return;
                    }
                    root.child("stores").child(uid).setValue(publicStore).addOnCompleteListener(this, storeSaved -> {
                        if (!storeSaved.isSuccessful()) {
                            deny("Could not publish your approved store. Check the database rules.");
                            return;
                        }
                        root.child("approvalRequests").child(uid).removeValue().addOnCompleteListener(this, removed ->
                                openStore(uid, email, name, location));
                    });
                });
            });
        });
    }

    private void openStore(String uid, String email, String name, String location) {
        if (name == null || location == null || email == null) {
            deny("Your store details are incomplete. Contact the MyPrice owner.");
            return;
        }
        try {
            // Keep the original local profile shape. Never store the Firebase password.
            localDatabase.callAttr("SaveLoginInformation", uid, uid, name, location, email, "");
        } catch (Exception e) {
            deny("Could not save local store details. Please try again.");
            return;
        }
        loadingDialog.Dismiss();
        Intent intent = new Intent(this, Store_Home.class);
        intent.putExtra("refresh", "true");
        startActivity(intent);
        finish();
    }

    private void deny(String message) {
        auth.signOut();
        loadingDialog.Dismiss();
        showMessage(message);
    }

    private void showMessage(String message) {
        MessageDialog dialog = new MessageDialog(this);
        dialog.Show("error", message);
        Button close = dialog.dialog.findViewById(R.id.btn_close_msg);
        close.setOnClickListener(view -> dialog.Dismiss());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Home.class));
        finish();
    }
}
