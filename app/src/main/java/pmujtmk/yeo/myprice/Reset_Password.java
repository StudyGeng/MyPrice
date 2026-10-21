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

public class Reset_Password extends AppCompatActivity {
    EditText et_newPassword, et_confirmPassword;
    Button btn_back, btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_reset_password);
        LoadingDialog loadingDialog = new LoadingDialog(Reset_Password.this);
        MessageDialog messageDialog = new MessageDialog(Reset_Password.this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");

        Intent intent = this.getIntent();
        String UniqueID = intent.getStringExtra("uniqueid");
        String ID = intent.getStringExtra("id");
        String StoreName = intent.getStringExtra("storename");
        String Location = intent.getStringExtra("location");
        String Email = intent.getStringExtra("email");

        et_newPassword = findViewById(R.id.et_password_resetpassword);
        et_confirmPassword = findViewById(R.id.et_confirmpassword_resetpassword);
        btn_save = findViewById(R.id.btn_save_resetpassword);
        btn_back = findViewById(R.id.btn_back_resetpassword);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onClick(View view) {
                String Validation = Tools_py.callAttr("PasswordValidation", et_newPassword.getText().toString()).toString();
                boolean ConfirmPassword = (et_newPassword.getText().toString().equals(et_confirmPassword.getText().toString()));

                if ((Validation.equals("Valid")) && ConfirmPassword){
                    loadingDialog.Start("Resetting Password...");
                    new Thread() {
                        @Override
                        public void run() {
                            Boolean connectionAvaibility;
                            try {
                                Database_py.callAttr("UpdateAccountInformation", UniqueID, ID, StoreName, Location, Email, et_newPassword.getText().toString());
                                Tools_py.callAttr("SendAccountChangedInformation", "Password", "None", "None", "None", Email);
                                connectionAvaibility = true;
                            }
                            catch (Exception e){
                                connectionAvaibility = false;
                            }
                            try {
                                Boolean finalConnectionAvaibility = connectionAvaibility;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingDialog.Dismiss();
                                        if (finalConnectionAvaibility) {
                                            messageDialog.Show("positive", "Your password has been reset.");
                                            Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                            btn_close.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    messageDialog.Dismiss();
                                                    startActivity(new Intent(Reset_Password.this, Login.class));
                                                }
                                            });
                                        }
                                        else{
                                            MessageDialog messageDialog = new MessageDialog(Reset_Password.this);
                                            loadingDialog.Dismiss();
                                            messageDialog.Show("error", "No Internet Connection");
                                            Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                            btn_close.setOnClickListener(new View.OnClickListener() {
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
                    if (!Validation.equals("Valid")){
                        if (String.valueOf(Validation.charAt(0)).equals("1")){
                            et_newPassword.setError("Password must be at least 8 characters");
                        }
                        else{
                            et_newPassword.setError("Password must be consist of uppercase, lowercase, numeric and allowed symbols: !@#_$%*");
                        }
                    }
                    if (!ConfirmPassword){
                        et_confirmPassword.setError("Password not match");
                    }
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Reset_Password.this, FindAccount.class));
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        startActivity(new Intent(Reset_Password.this, FindAccount.class));
    }
}