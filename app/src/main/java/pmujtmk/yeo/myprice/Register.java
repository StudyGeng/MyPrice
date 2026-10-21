package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private static final String DATABASE_URL = "https://suprise1happyday-default-rtdb.asia-southeast1.firebasedatabase.app";
    private EditText storeNameInput, locationInput, emailInput, passwordInput, confirmInput, referenceInput;
    private LoadingDialog loadingDialog;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(this);
        storeNameInput = findViewById(R.id.et_store_register);
        locationInput = findViewById(R.id.et_location_register);
        emailInput = findViewById(R.id.et_email_register);
        passwordInput = findViewById(R.id.et_password_register);
        confirmInput = findViewById(R.id.et_confrimpaswword_register);
        referenceInput = findViewById(R.id.et_key_register);
        referenceInput.setHint("Optional approval reference");

        Button registerButton = findViewById(R.id.btn_register_register);
        Button loginButton = findViewById(R.id.btn_login_register);
        registerButton.setOnClickListener(view -> register());
        loginButton.setOnClickListener(view -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }

    private void register() {
        String storeName = storeNameInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirm = confirmInput.getText().toString();
        String reference = referenceInput.getText().toString().trim();

        if (storeName.isEmpty() || storeName.length() > 100) {
            storeNameInput.setError("Use 1 to 100 characters");
            return;
        }
        if (location.isEmpty() || location.length() > 200) {
            locationInput.setError("Use 1 to 200 characters");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            return;
        }
        if (password.length() < 8) {
            passwordInput.setError("Use at least 8 characters");
            return;
        }
        if (!password.equals(confirm)) {
            confirmInput.setError("Passwords do not match");
            return;
        }
        if (reference.length() > 100) {
            referenceInput.setError("Use up to 100 characters");
            return;
        }

        loadingDialog.Start("Creating account...");
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, created -> {
            if (!created.isSuccessful() || auth.getCurrentUser() == null) {
                loadingDialog.Dismiss();
                showMessage("Account creation failed. Check the email address or try another one.", false);
                return;
            }
            FirebaseUser user = auth.getCurrentUser();
            Map<String, Object> request = new HashMap<>();
            request.put("storeName", storeName);
            request.put("location", location);
            request.put("email", user.getEmail());
            if (!reference.isEmpty()) request.put("reference", reference);
            FirebaseDatabase.getInstance(DATABASE_URL).getReference("approvalRequests")
                    .child(user.getUid()).setValue(request).addOnCompleteListener(this, saved -> {
                        if (!saved.isSuccessful()) {
                            // A registration without a reviewable request is unusable.
                            user.delete().addOnCompleteListener(this, deleted -> {
                                auth.signOut();
                                loadingDialog.Dismiss();
                                showMessage(deleted.isSuccessful()
                                        ? "Could not submit your approval request. Please try registering again."
                                        : "Account created, but the approval request failed. Contact the MyPrice owner.", false);
                            });
                            return;
                        }
                        user.sendEmailVerification().addOnCompleteListener(this, sent -> {
                            auth.signOut();
                            loadingDialog.Dismiss();
                            showMessage(sent.isSuccessful()
                                    ? "Account created. Verify your email, then wait for store approval before signing in."
                                    : "Account created, but the verification email could not be sent. Sign in later to resend it.", true);
                        });
                    });
        });
    }

    private void showMessage(String message, boolean returnToLogin) {
        MessageDialog dialog = new MessageDialog(this);
        dialog.Show(returnToLogin ? "positive" : "error", message);
        Button close = dialog.dialog.findViewById(R.id.btn_close_msg);
        close.setOnClickListener(view -> {
            dialog.Dismiss();
            if (returnToLogin) {
                startActivity(new Intent(this, Login.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Login.class));
        finish();
    }
}
