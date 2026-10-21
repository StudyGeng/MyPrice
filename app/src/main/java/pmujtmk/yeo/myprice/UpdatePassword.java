package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class UpdatePassword extends AppCompatActivity {

    EditText et_current_password, et_new_password, et_confirm_password;
    Button btn_back, btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_update_password);
        LoadingDialog loadingDialog = new LoadingDialog(UpdatePassword.this);
        MessageDialog messageDialog = new MessageDialog(UpdatePassword.this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");

        btn_back = findViewById(R.id.btn_back_password);
        btn_save = findViewById(R.id.btn_save_password);
        et_current_password = findViewById(R.id.et_current_password_password);
        et_new_password = findViewById(R.id.et_new_password_password);
        et_confirm_password = findViewById(R.id.et_confirm_password_password);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UpdatePassword.this, Account.class));
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onClick(View view) {
                boolean CurrentPassword = (et_current_password.getText().toString().equals(Database_py.callAttr("GetLoggedIn_Password").toString()));
                String Validation = Tools_py.callAttr("PasswordValidation", et_new_password.getText().toString()).toString();
                boolean ConfirmPassword = (et_new_password.getText().toString().equals(et_confirm_password.getText().toString()));
                boolean noRepaetOldPassword = (!et_current_password.getText().toString().equals(et_new_password.getText().toString()));

                if (CurrentPassword && (Validation.equals("Valid")) && ConfirmPassword && noRepaetOldPassword){
                    String UniqueID = Database_py.callAttr("GetLoggedIn_UniqueID").toString();
                    String ID = Database_py.callAttr("GetLoggedIn_ID").toString();
                    String StoreName = Database_py.callAttr("GetLoggedIn_StoreName").toString();
                    String Location = Database_py.callAttr("GetLoggedIn_Location").toString();
                    String Email = Database_py.callAttr("GetLoggedIn_Email").toString();
                    String Password = Database_py.callAttr("GetLoggedIn_Password").toString();
                    String NewPassword = et_new_password.getText().toString();

                    loadingDialog.Start("Updating...");
                    new Thread() {
                        @Override
                        public void run() {
                            int LoginResult;
                            try {
                                LoginResult = Database_py.callAttr("Login", Email, Password).toInt();
                                if (LoginResult == 0) {
                                    Database_py.callAttr("UpdateAccountInformation", UniqueID, ID, StoreName, Location, Email, NewPassword);
                                    Tools_py.callAttr("SendAccountChangedInformation", "Password", "None", "None", "None", Email);
                                }
                            }
                            catch (Exception e){
                                LoginResult = 3;
                            }
                            try {
                                int finalLoginResult = LoginResult;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingDialog.Dismiss();
                                        switch (finalLoginResult) {
                                            case 0:
                                                // Login Success
                                                Database_py.callAttr("UpdateAccountInformation2", UniqueID, ID, StoreName, Location, Email, NewPassword);

                                                messageDialog.Show("positive", "Password updated successfully!");
                                                Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                        startActivity(new Intent(UpdatePassword.this, Account.class));
                                                    }
                                                });
                                                break;

                                            case 1:
                                                // Login Error: Email changed/Account deleted
                                                // Go to Login
                                                loadingDialog.Dismiss();
                                                messageDialog.Show("error", "Your account might has been deleted or your email has been changed."); // Display error message
                                                Button btn_close2 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close2.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                        startActivity(new Intent(UpdatePassword.this, Login.class));
                                                    }
                                                });
                                                break;

                                            case 2:
                                                // Login Error: Password changed
                                                // Go to Login
                                                loadingDialog.Dismiss();
                                                messageDialog.Show("error", "Your password has been changed."); // Display error message
                                                Button btn_close3 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close3.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                        startActivity(new Intent(UpdatePassword.this, Login.class));
                                                    }
                                                });
                                                break;

                                            default:
                                                // Login error: No Internet Connection
                                                loadingDialog.Dismiss();
                                                messageDialog.Show("error", "No Internet Connection"); // Display error message
                                                Button btn_close4 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close4.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                    }
                                                });
                                        }
                                    }
                                });
                            } catch (final Exception ex) {}
                        }
                    }.start();
                }
                else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    else{
                        v.vibrate(500);
                    }
                    if (!CurrentPassword){
                        et_current_password.setError("Wrong Password");
                    }
                    if (!noRepaetOldPassword){
                        et_new_password.setError("New password cannot be the same with the old password");
                    }
                    if (!Validation.equals("Valid")){
                        if (String.valueOf(Validation.charAt(0)).equals("1")){
                            et_new_password.setError("Password must be at least 8 characters");
                        }
                        else{
                            et_new_password.setError("Password must be consist of uppercase, lowercase, numeric and allowed symbols: !@#_$%*");
                        }
                    }
                    if (!ConfirmPassword){
                        et_confirm_password.setError("Password not match");
                    }
                }
            }
        });
    }
}