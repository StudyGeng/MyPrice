package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

/**
 * Legacy route kept for old navigation. Verification and password recovery now
 * use Firebase email links; this screen must never run the old client-side SMTP
 * code or write directly to account data.
 */
public class OTPverification extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);
        MessageDialog dialog = new MessageDialog(this);
        dialog.Show("error", "This old verification-code screen is no longer used. Please use the Firebase email link instead.");
        Button close = dialog.dialog.findViewById(R.id.btn_close_msg);
        close.setOnClickListener(view -> {
            dialog.Dismiss();
            goBackToAccountFlow();
        });
        Button verify = findViewById(R.id.btn_verify_otp);
        Button resend = findViewById(R.id.btn_resend_otp);
        verify.setOnClickListener(view -> goBackToAccountFlow());
        resend.setOnClickListener(view -> goBackToAccountFlow());
    }

    private void goBackToAccountFlow() {
        String type = getIntent().getStringExtra("type");
        Class<?> destination;
        if ("resetPassword".equals(type)) destination = FindAccount.class;
        else if ("changeEmail".equals(type)) destination = UpdateEmail.class;
        else if ("deleteAccount".equals(type)) destination = Account.class;
        else destination = Login.class;
        startActivity(new Intent(this, destination));
        finish();
    }

    @Override
    public void onBackPressed() {
        goBackToAccountFlow();
    }
}
