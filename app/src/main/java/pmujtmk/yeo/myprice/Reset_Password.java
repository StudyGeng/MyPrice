package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

/** The former in-app reset route is replaced by Firebase's emailed reset link. */
public class Reset_Password extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        MessageDialog dialog = new MessageDialog(this);
        dialog.Show("error", "Password reset now happens through a secure Firebase email link. Enter your email on the previous screen.");
        Button close = dialog.dialog.findViewById(R.id.btn_close_msg);
        close.setOnClickListener(view -> {
            dialog.Dismiss();
            goToFindAccount();
        });
        findViewById(R.id.btn_save_resetpassword).setOnClickListener(view -> goToFindAccount());
        findViewById(R.id.btn_back_resetpassword).setOnClickListener(view -> goToFindAccount());
    }

    private void goToFindAccount() {
        startActivity(new Intent(this, FindAccount.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        goToFindAccount();
    }
}
