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
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class UpdateEmail extends AppCompatActivity {

    EditText et_currentpassword, et_newEmail;
    Button btn_back, btn_save;
    TextView tv_prevEmail;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_update_email);
        LoadingDialog loadingDialog = new LoadingDialog(UpdateEmail.this);
        MessageDialog messageDialog = new MessageDialog(UpdateEmail.this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");

        btn_back = findViewById(R.id.btn_back_email);
        btn_save = findViewById(R.id.btn_save_email);
        et_currentpassword = findViewById(R.id.et_password_email);
        et_newEmail = findViewById(R.id.et_newemail_email);
        tv_prevEmail = findViewById(R.id.tv_prevemail_email);

        tv_prevEmail.setText("Prev. Email: " + Database_py.callAttr("GetLoggedIn_Email").toString());

        btn_save.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onClick(View view) {
                String Validation = Tools_py.callAttr("EmailValidation", et_newEmail.getText().toString()).toString();
                String enteredPassword = et_currentpassword.getText().toString();
                if ((Validation.equals("Valid")) && (enteredPassword.equals(Database_py.callAttr("GetLoggedIn_Password").toString()))){
                    String UniqueID = Database_py.callAttr("GetLoggedIn_UniqueID").toString();
                    String ID = Database_py.callAttr("GetLoggedIn_ID").toString();
                    String StoreName = Database_py.callAttr("GetLoggedIn_StoreName").toString();
                    String Location = Database_py.callAttr("GetLoggedIn_Location").toString();
                    String Email = Database_py.callAttr("GetLoggedIn_Email").toString();
                    String Password = Database_py.callAttr("GetLoggedIn_Password").toString();

                    loadingDialog.Start("Validating...");
                    new Thread() {
                        @Override
                        public void run() {
                            //Check either the email is already registered or not
                            Boolean Registered = null;
                            int LoginResult;
                            try {
                                LoginResult = Database_py.callAttr("Login", Email, Password).toInt();
                                if (LoginResult == 0) {
                                    Registered = Database_py.callAttr("CheckRegisteredEmail", et_newEmail.getText().toString()).toBoolean();
                                }
                            }
                            catch (Exception e){
                                LoginResult = 3;
                            }
                            try {
                                Boolean finalRegistered = Registered;
                                int finalLoginResult = LoginResult;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingDialog.Dismiss();
                                        switch (finalLoginResult) {
                                            case 0:
                                                // Login Success
                                                if (finalRegistered) {
                                                    messageDialog.Show("error", et_newEmail.getText().toString()
                                                            + " already registered as MyPrice's account. Please use another email address.");
                                                    Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                    btn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            messageDialog.Dismiss();
                                                        }
                                                    });
                                                }
                                                else {
                                                    // Go to OTP verification
                                                    Intent i = new Intent(UpdateEmail.this, OTPverification.class);
                                                    i.putExtra("email", et_newEmail.getText().toString());
                                                    i.putExtra("type", "changeEmail");
                                                    i.putExtra("uniqueid", UniqueID);
                                                    i.putExtra("id", ID);
                                                    i.putExtra("storename", StoreName);
                                                    i.putExtra("location", Location);
                                                    i.putExtra("prevemail", Email);
                                                    i.putExtra("password", Password);
                                                    startActivity(i);
                                                }
                                                break;

                                            case 1:
                                                // Login Error: Email changed/Account deleted
                                                // Go to Login
                                                loadingDialog.Dismiss();
                                                messageDialog.Show("error", "Your account might has been deleted or your email has been changed."); // Display error message
                                                Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                        startActivity(new Intent(UpdateEmail.this, Login.class));
                                                    }
                                                });
                                                break;

                                            case 2:
                                                // Login Error: Password changed
                                                // Go to Login
                                                loadingDialog.Dismiss();
                                                messageDialog.Show("error", "Your password has been changed."); // Display error message
                                                Button btn_close2 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close2.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                        startActivity(new Intent(UpdateEmail.this, Login.class));
                                                    }
                                                });
                                                break;

                                            default:
                                                // Login error: No Internet Connection
                                                loadingDialog.Dismiss();
                                                messageDialog.Show("error", "No Internet Connection"); // Display error message
                                                Button btn_close3 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close3.setOnClickListener(new View.OnClickListener() {
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
                        et_newEmail.setError("Invalid Email");
                    }
                    if (!enteredPassword.equals(Database_py.callAttr("GetLoggedIn_Password").toString())){
                        et_currentpassword.setError("Wrong Password");
                    }
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UpdateEmail.this, Account.class));
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        startActivity(new Intent(UpdateEmail.this, Account.class));
    }
}