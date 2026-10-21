package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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

public class FindAccount extends AppCompatActivity {

    Button btn_find, btn_back;
    EditText et_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_find_account);
        LoadingDialog loadingDialog = new LoadingDialog(FindAccount.this);
        MessageDialog messageDialog = new MessageDialog(FindAccount.this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");

        btn_find = findViewById(R.id.btn_find_findacc);
        btn_back = findViewById(R.id.btn_back_findacc);
        et_email = findViewById(R.id.et_email_findacc);

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Validation = Tools_py.callAttr("EmailValidation", et_email.getText().toString()).toString();
                if (Validation.equals("Valid")){
                    loadingDialog.Start("Searching for your account...");
                    new Thread() {
                        @Override
                        public void run() {
                            //Check either the email is already registered or not
                            Boolean Registered = null;
                            try {
                                Registered = Database_py.callAttr("CheckRegisteredEmail", et_email.getText().toString()).toBoolean();
                            }
                            catch (Exception e){}
                            try {
                                Boolean finalRegistered = Registered;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingDialog.Dismiss();
                                        if (finalRegistered == null){
                                            MessageDialog messageDialog = new MessageDialog(FindAccount.this);
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
                                            String UniqueID = Database_py.callAttr("GetLoginInformation", et_email.getText().toString()).toJava(String[].class)[0];
                                            String ID = Database_py.callAttr("GetLoginInformation", et_email.getText().toString()).toJava(String[].class)[1];
                                            String StoreName = Database_py.callAttr("GetLoginInformation", et_email.getText().toString()).toJava(String[].class)[2];
                                            String Location = Database_py.callAttr("GetLoginInformation", et_email.getText().toString()).toJava(String[].class)[3];

                                            // Go to OTP verification
                                            Intent i = new Intent(FindAccount.this, OTPverification.class);
                                            i.putExtra("email", et_email.getText().toString());
                                            i.putExtra("type", "resetPassword");
                                            i.putExtra("uniqueid", UniqueID);
                                            i.putExtra("id", ID);
                                            i.putExtra("storename", StoreName);
                                            i.putExtra("location", Location);
                                            startActivity(i);
                                        }
                                        else{
                                            messageDialog.Show("error", "Account not found.");
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
                        et_email.setError("Invalid Email");
                    }
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FindAccount.this, Login.class));
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        startActivity(new Intent(FindAccount.this, Login.class));
    }
}