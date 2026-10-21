package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class Login extends AppCompatActivity {

    Button Btn_Register, Btn_Reset, Btn_Login;
    EditText ET_Email, ET_Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login);
        LoadingDialog loadingDialog = new LoadingDialog(Login.this);
        MessageDialog messageDialog = new MessageDialog(Login.this);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");

        Btn_Register = findViewById(R.id.btn_register_login);
        Btn_Reset=findViewById(R.id.btn_forgetpassword_login);
        Btn_Login = findViewById(R.id.btn_login_login);
        ET_Email = findViewById(R.id.et_email_login);
        ET_Password = findViewById(R.id.et_password_login);

        Btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Database_py.callAttr("ResetLoginTable");
                if ((ET_Email.getText().toString().length() != 0) && (ET_Password.getText().toString().length() != 0)) {
                    loadingDialog.Start("Loading...");
                    new Thread() {
                        @Override
                        public void run() {
                            String UniqueID = null;
                            String ID = null;
                            String StoreName = null;
                            String Location = null;
                            int LoginResult;
                            try {
                                LoginResult = Database_py.callAttr("Login", ET_Email.getText().toString(), ET_Password.getText().toString()).toInt();
                                if (LoginResult == 0) {
                                    UniqueID = Database_py.callAttr("GetLoginInformation", ET_Email.getText().toString()).toJava(String[].class)[0];
                                    ID = Database_py.callAttr("GetLoginInformation", ET_Email.getText().toString()).toJava(String[].class)[1];
                                    StoreName = Database_py.callAttr("GetLoginInformation", ET_Email.getText().toString()).toJava(String[].class)[2];
                                    Location = Database_py.callAttr("GetLoginInformation", ET_Email.getText().toString()).toJava(String[].class)[3];
                                }
                            }
                            catch (Exception e){
                                LoginResult = 3;
                                System.out.println(e);
                            }
                            try {
                                String UniqueID_ = UniqueID;
                                String ID_ = ID;
                                String StoreName_ = StoreName;
                                String Location_ = Location;
                                int finalLoginResult = LoginResult;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingDialog.Dismiss();
                                        switch (finalLoginResult) {
                                            case 1:
                                                messageDialog.Show("error", "This email have not been registered.");
                                                Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                    }
                                                });
                                                break;
                                            case 2:
                                                messageDialog.Show("error", "Wrong Password!");
                                                Button btn1 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn1.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                    }
                                                });
                                                break;
                                            case 0:
                                                Database_py.callAttr("SaveLoginInformation", UniqueID_, ID_, StoreName_, Location_, ET_Email.getText().toString(), ET_Password.getText().toString());
                                                Intent i = new Intent(Login.this, Store_Home.class);
                                                i.putExtra("refresh", "true");
                                                startActivity(i);
                                                break;
                                            default:
                                                MessageDialog messageDialog = new MessageDialog(Login.this);
                                                loadingDialog.Dismiss();
                                                messageDialog.Show("error", "No Internet Connection");
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
                            } catch (final Exception ex) {
                            }
                        }
                    }.start();
                }
                else{
                    if (ET_Email.getText().toString().length() == 0){
                        ET_Email.setError("Required");
                    }
                    if (ET_Password.getText().toString().length() == 0){
                        ET_Password.setError("Required");
                    }
                }
            }
        });

        Btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        Btn_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, FindAccount.class));
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        startActivity(new Intent(Login.this, Home.class));
    }
}