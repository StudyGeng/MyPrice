package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdatePassword extends AppCompatActivity {
    private EditText currentInput, newInput, confirmInput;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_update_password);

        loadingDialog = new LoadingDialog(this);
        currentInput = findViewById(R.id.et_current_password_password);
        newInput = findViewById(R.id.et_new_password_password);
        confirmInput = findViewById(R.id.et_confirm_password_password);
        Button backButton = findViewById(R.id.btn_back_password);
        Button saveButton = findViewById(R.id.btn_save_password);
        backButton.setOnClickListener(view -> goToAccount());
        saveButton.setOnClickListener(view -> changePassword());
    }

    private void changePassword() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            showMessage("Please sign in again before changing your password.", true, false);
            return;
        }
        String current = currentInput.getText().toString();
        String next = newInput.getText().toString();
        String confirm = confirmInput.getText().toString();
        if (current.isEmpty()) {
            currentInput.setError("Required");
            return;
        }
        if (next.length() < 8) {
            newInput.setError("Use at least 8 characters");
            return;
        }
        if (next.equals(current)) {
            newInput.setError("Choose a different password");
            return;
        }
        if (!next.equals(confirm)) {
            confirmInput.setError("Passwords do not match");
            return;
        }

        loadingDialog.Start("Updating password...");
        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), current))
                .addOnCompleteListener(this, verified -> {
                    if (!verified.isSuccessful()) {
                        loadingDialog.Dismiss();
                        currentInput.setError("Current password is incorrect");
                        return;
                    }
                    user.updatePassword(next).addOnCompleteListener(this, updated -> {
                        loadingDialog.Dismiss();
                        if (updated.isSuccessful()) {
                            currentInput.setText("");
                            newInput.setText("");
                            confirmInput.setText("");
                            showMessage("Password updated successfully.", false, true);
                        } else {
                            showMessage("Could not update your password. Please try again.", false, false);
                        }
                    });
                });
    }

    private void showMessage(String message, boolean goToLogin, boolean success) {
        MessageDialog dialog = new MessageDialog(this);
        dialog.Show(success ? "positive" : "error", message);
        Button close = dialog.dialog.findViewById(R.id.btn_close_msg);
        close.setOnClickListener(view -> {
            dialog.Dismiss();
            if (goToLogin) {
                startActivity(new Intent(this, Login.class));
                finish();
            } else if (success) goToAccount();
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
