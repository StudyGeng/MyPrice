package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class Register extends AppCompatActivity {

    Button btn_register, btn_login;
    EditText et_StoreName, et_Location, et_Email, et_Password, et_ConfrimPassword, et_key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        LoadingDialog loadingDialog = new LoadingDialog(Register.this);
        MessageDialog messageDialog = new MessageDialog(Register.this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));

        Python py = Python.getInstance();
        final PyObject Tools_py = py.getModule("Tools");
        final PyObject Database_py = py.getModule("Database");

        btn_register = findViewById(R.id.btn_register_register);
        btn_login = findViewById(R.id.btn_login_register);
        et_StoreName = findViewById(R.id.et_store_register);
        et_Location = findViewById(R.id.et_location_register);
        et_Email = findViewById(R.id.et_email_register);
        et_Password = findViewById(R.id.et_password_register);
        et_ConfrimPassword = findViewById(R.id.et_confrimpaswword_register);
        et_key = findViewById(R.id.et_key_register);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onClick(View view) {
                String validation = Tools_py.callAttr("registrationValidation", et_StoreName.getText().toString(),
                        et_Location.getText().toString(), et_Email.getText().toString(), et_Password.getText().toString(),
                        et_ConfrimPassword.getText().toString(), et_key.getText().toString()).toString();
                if (validation.equals("Valid")){
                    loadingDialog.Start("Validating informations...");
                    new Thread() {
                        @Override
                        public void run() {
                            //Check either the email is already registered or not
                            String keyResult = null;
                            String keyID = null;
                            Boolean Registered = null;
                            try {
                                keyResult = Database_py.callAttr("CheckKey", et_key.getText().toString()).toJava(String[].class)[0];
                                keyID = Database_py.callAttr("CheckKey", et_key.getText().toString()).toJava(String[].class)[1];
                                Registered = Database_py.callAttr("CheckRegisteredEmail", et_Email.getText().toString()).toBoolean();
                            }
                            catch (Exception e){}
                            try {
                                String finalKeyResult = keyResult;
                                Boolean finalRegistered = Registered;
                                String finalKeyID = keyID;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingDialog.Dismiss();
                                        if (finalKeyResult == null){
                                            MessageDialog messageDialog = new MessageDialog(Register.this);
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
                                        else if (finalKeyResult.equals("invalid")){
                                            messageDialog.Show("error", "Please enter a valid registration key. Contact the project administrator for registration details.");
                                            Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                            btn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    messageDialog.Dismiss();
                                                }
                                            });
                                        }
                                        else if (finalRegistered == null){
                                            MessageDialog messageDialog = new MessageDialog(Register.this);
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
                                        else if (finalRegistered){
                                            messageDialog.Show("error", et_Email.getText().toString()
                                                    + " already registered as MyPrice's account. Please proceed to the login.");
                                            Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                            btn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    messageDialog.Dismiss();
                                                }
                                            });
                                        }
                                        else{
                                            // Go to OTP verification

                                            Intent i = new Intent(Register.this, OTPverification.class);
                                            i.putExtra("email", et_Email.getText().toString());
                                            i.putExtra("type", "register");
                                            i.putExtra("storename", et_StoreName.getText().toString());
                                            i.putExtra("location", et_Location.getText().toString());
                                            i.putExtra("password", et_Password.getText().toString());
                                            i.putExtra("keyID", finalKeyID);
                                            startActivity(i);
                                        }
                                    }
                                });
                            } catch (final Exception ex) {

                            }
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

                    if (String.valueOf(validation.charAt(1)).equals("1")){
                        et_StoreName.setError("Invalid Store Name");
                    }
                    if (String.valueOf(validation.charAt(2)).equals("1")){
                        et_Location.setError("Invalid Location");
                    }
                    if (String.valueOf(validation.charAt(3)).equals("1")){
                        et_Email.setError("Invalid Email");
                    }
                    if (String.valueOf(validation.charAt(4)).equals("1")){
                        et_Password.setError("Password must be at least 8 characters");
                    }
                    if (String.valueOf(validation.charAt(5)).equals("1")){
                        et_Password.setError("Password must be consist of uppercase, lowercase, numeric and allowed symbols: !@#_$%*");
                    }
                    if (String.valueOf(validation.charAt(6)).equals("1")){
                        et_ConfrimPassword.setError("Password not match");
                    }
                    if (String.valueOf(validation.charAt(7)).equals("1")){
                        et_key.setError("Registration key must be 19 characters");
                    }
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

    }
    public void onBackPressed() {
        moveTaskToBack(true);
        startActivity(new Intent(Register.this, Login.class));
    }
}