package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmail extends AppCompatActivity {
    private EditText passwordInput, newEmailInput;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_update_email);

        loadingDialog = new LoadingDialog(this);
        passwordInput = findViewById(R.id.et_password_email);
        newEmailInput = findViewById(R.id.et_newemail_email);
        TextView previousEmail = findViewById(R.id.tv_prevemail_email);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        previousEmail.setText("Prev. Email: " + (user == null ? "" : user.getEmail()));

        Button backButton = findViewById(R.id.btn_back_email);
        Button saveButton = findViewById(R.id.btn_save_email);
        backButton.setOnClickListener(view -> goToAccount());
        saveButton.setOnClickListener(view -> changeEmail());
    }

    private void changeEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            showMessage("Please sign in again before changing your email.", true);
            return;
        }
        String oldEmail = user.getEmail();
        String newEmail = newEmailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            newEmailInput.setError("Enter a valid email address");
            return;
        }
        if (newEmail.equalsIgnoreCase(oldEmail)) {
            newEmailInput.setError("Enter a different email address");
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Required");
            return;
        }

        loadingDialog.Start("Checking account...");
        user.reauthenticate(EmailAuthProvider.getCredential(oldEmail, password))
                .addOnCompleteListener(this, checked -> {
                    if (!checked.isSuccessful()) {
                        loadingDialog.Dismiss();
                        passwordInput.setError("Current password is incorrect");
                        return;
                    }
                    user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(this, sent -> {
                        loadingDialog.Dismiss();
                        if (!sent.isSuccessful()) {
                            showMessage("Could not send the verification link. This email may already be in use.", false);
                            return;
                        }
                        FirebaseAuth.getInstance().signOut();
                        showMessage("Open the verification link sent to your new email. Then sign in with the new email.", true);
                    });
                });
    }

    private void showMessage(String message, boolean goToLogin) {
        MessageDialog dialog = new MessageDialog(this);
        dialog.Show(goToLogin ? "positive" : "error", message);
        Button close = dialog.dialog.findViewById(R.id.btn_close_msg);
        close.setOnClickListener(view -> {
            dialog.Dismiss();
            if (goToLogin) {
                startActivity(new Intent(this, Login.class));
                finish();
            }
        });
    }

    private void goToAccount() {
        startActivity(new Intent(this, Account.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        goToAccount();
    }
}
