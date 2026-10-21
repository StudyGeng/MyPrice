package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class FindAccount extends AppCompatActivity {
    private EditText emailInput;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_find_account);

        loadingDialog = new LoadingDialog(this);
        emailInput = findViewById(R.id.et_email_findacc);
        Button findButton = findViewById(R.id.btn_find_findacc);
        Button backButton = findViewById(R.id.btn_back_findacc);
        findButton.setOnClickListener(view -> sendResetLink());
        backButton.setOnClickListener(view -> goToLogin());
    }

    private void sendResetLink() {
        String email = emailInput.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            return;
        }
        loadingDialog.Start("Sending password reset link...");
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(this, task -> {
            loadingDialog.Dismiss();
            MessageDialog dialog = new MessageDialog(this);
            dialog.Show(task.isSuccessful() ? "positive" : "error",
                    task.isSuccessful()
                            ? "If this email has an account, open the Firebase password reset link in your inbox."
                            : "Could not send a reset link. Check your connection and try again.");
            Button close = dialog.dialog.findViewById(R.id.btn_close_msg);
            close.setOnClickListener(view -> {
                dialog.Dismiss();
                if (task.isSuccessful()) goToLogin();
            });
        });
    }

    private void goToLogin() {
        startActivity(new Intent(this, Login.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        goToLogin();
    }
}
