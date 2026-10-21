package pmujtmk.yeo.myprice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.net.CookieHandler;

public class OTPverification extends AppCompatActivity {
    private static final long START_TIME_IN_MILLIS = 60000;
    private CountDownTimer countDownTimer;
    private long TimeLeftInMillis = START_TIME_IN_MILLIS;

    String StoreName, Location, Password, Email, PrevEmail, UniqueID, ID, keyID;

    EditText et_otp1, et_otp2, et_otp3, et_otp4, et_otp5, et_otp6;
    TextView tv_requestcode, tv_countdown;
    Button btn_verify, btn_resend;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        LoadingDialog loadingDialog = new LoadingDialog(OTPverification.this);
        MessageDialog messageDialog = new MessageDialog(OTPverification.this);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Tools_py = py.getModule("Tools");

        et_otp1 = findViewById(R.id.et_otp1_otp);
        et_otp2 = findViewById(R.id.et_otp2_otp);
        et_otp3 = findViewById(R.id.et_otp3_otp);
        et_otp4 = findViewById(R.id.et_otp4_otp);
        et_otp5 = findViewById(R.id.et_otp5_otp);
        et_otp6 = findViewById(R.id.et_otp6_otp);
        tv_requestcode = findViewById(R.id.tv_requestcode_otp);
        tv_countdown = findViewById(R.id.tv_countdown_otp);
        btn_resend = findViewById(R.id.btn_resend_otp);
        btn_verify = findViewById(R.id.btn_verify_otp);

        Intent intent = this.getIntent();
        Email = intent.getStringExtra("email");
        String type = intent.getStringExtra("type");
        String VerificationCode = Tools_py.callAttr("GenerateVerificationCode").toString();
        String RequestCode = Tools_py.callAttr("GenerateRequestCode").toString();

        if (type.equals("register")){
            StoreName = intent.getStringExtra("storename");
            Location = intent.getStringExtra("location");
            Password = intent.getStringExtra("password");
            keyID = intent.getStringExtra("keyID");
        }
        else if (type.equals("changeEmail")){
            StoreName = intent.getStringExtra("storename");
            Location = intent.getStringExtra("location");
            Password = intent.getStringExtra("password");
            PrevEmail = intent.getStringExtra("prevemail");
            ID = intent.getStringExtra("id");
            UniqueID = intent.getStringExtra("uniqueid");
        }
        else if (type.equals("resetPassword")){
            StoreName = intent.getStringExtra("storename");
            Location = intent.getStringExtra("location");
            ID = intent.getStringExtra("id");
            UniqueID = intent.getStringExtra("uniqueid");
        }
        else{
            ID = intent.getStringExtra("id");
            UniqueID = intent.getStringExtra("uniqueid");
        }

        tv_requestcode.setText("Request Code: " + RequestCode);
        setupOTPinputs();

        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String EnteredCode = et_otp1.getText().toString() + et_otp2.getText().toString() + et_otp3.getText().toString()
                        + et_otp4.getText().toString() + et_otp5.getText().toString() + et_otp6.getText().toString();
                if (EnteredCode.equals(VerificationCode)){
                    if (type.equals("register")){
                        RegisterNewAccount();
                    }
                    else if (type.equals("changeEmail")){
                        updateEmail();
                    }
                    else if (type.equals("resetPassword")){
                        resetPassword();
                    }
                    else{
                        deleteAccount();
                    }
                }
                else{
                    messageDialog.Show("error", "You entered incorrect code.");
                    Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            messageDialog.Dismiss();
                        }
                    });
                }
            }
        });

        btn_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(OTPverification.this, OTPverification.class);
                i.putExtras(intent);
                startActivity(i);
                finish();
            }
        });

        loadingDialog.Start("Sending verification code to " + Email);
        new Thread() {
            @Override
            public void run() {
                Boolean ConnectionAvaibility;
                try {
                    Tools_py.callAttr("SendVerificationCode", Email, VerificationCode, RequestCode, type);
                    ConnectionAvaibility = true;
                }
                catch (Exception e){
                    ConnectionAvaibility = false;
                }
                try {
                    Boolean finalConnectionAvaibility = ConnectionAvaibility;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.Dismiss();
                            if (finalConnectionAvaibility){
                                StartCountDown();
                            }
                            else{
                                MessageDialog messageDialog = new MessageDialog(OTPverification.this);
                                loadingDialog.Dismiss();
                                messageDialog.Show("error", "No Internet Connection");
                                Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                btn_close.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        messageDialog.Dismiss();
                                        finish();
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

    private void deleteAccount() {
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");
        LoadingDialog loadingDialog = new LoadingDialog(OTPverification.this);
        MessageDialog messageDialog = new MessageDialog(OTPverification.this);

        loadingDialog.Start("Deleting account...");
        new Thread() {
            @Override
            public void run() {
                boolean accountDeleted = false;
                boolean emailSent = false;
                try {
                    Database_py.callAttr("DeleteAccount", UniqueID, ID);
                    accountDeleted = true;
                    try {
                        Tools_py.callAttr("SendDeletingAccountInformation", Email);
                        emailSent = true;
                    } catch (Exception e) {
                        // Deleting the account succeeded even if the confirmation email failed.
                    }
                }
                catch (Exception e){
                }
                try {
                    boolean finalAccountDeleted = accountDeleted;
                    boolean finalEmailSent = emailSent;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.Dismiss();
                            if (finalAccountDeleted) {
                                messageDialog.Show("positive", finalEmailSent
                                        ? "Your account has been deleted successfully."
                                        : "Your account was deleted, but the confirmation email could not be sent.");
                                Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                btn_close.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        messageDialog.Dismiss();
                                        startActivity(new Intent(OTPverification.this, Login.class));
                                    }
                                });
                            }
                            else {
                                MessageDialog messageDialog = new MessageDialog(OTPverification.this);
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

    private void resetPassword() {
        Intent i = new Intent(OTPverification.this, Reset_Password.class);
        i.putExtra("uniqueid", UniqueID);
        i.putExtra("id", ID);
        i.putExtra("storename", StoreName);
        i.putExtra("location", Location);
        i.putExtra("email", Email);
        startActivity(i);
    }

    private void RegisterNewAccount() {
        Python py = Python.getInstance();
        final PyObject Tools_py = py.getModule("Tools");
        final PyObject Database_py = py.getModule("Database");
        LoadingDialog loadingDialog = new LoadingDialog(OTPverification.this);

        loadingDialog.Start("Creating new account...");
        new Thread() {
            @Override
            public void run() {
                boolean accountCreated = false;
                boolean qrEmailSent = false;
                try {
                    String UniqueID;
                    while (true){
                        UniqueID = Tools_py.callAttr("GenerateUniqueID").toString();
                        if (!Boolean.parseBoolean(Database_py.callAttr("CheckUniqueID", UniqueID).toString())){
                            break;
                        }
                    }
                    Database_py.callAttr("RegisterNewAccount", UniqueID, StoreName, Location, Email, Password);
                    accountCreated = true;
                    try {
                        Database_py.callAttr("RemoveKey", keyID);
                    } catch (Exception e) {
                        // The account still exists if removing the registration key fails.
                    }
                    try {
                        Tools_py.callAttr("Send_QRcode_To_User", UniqueID, StoreName, Location, Email, Password, "register");
                        qrEmailSent = true;
                    } catch (Exception e) {
                        // The user can request the QR code again after logging in.
                    }
                }
                catch (Exception e) {
                    System.out.println(e);
                }
                try {
                    boolean finalAccountCreated = accountCreated;
                    boolean finalQrEmailSent = qrEmailSent;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.Dismiss();
                            if (finalAccountCreated) {
                                MessageDialog messageDialog = new MessageDialog(OTPverification.this);
                                messageDialog.Show("positive", finalQrEmailSent
                                        ? "New Account Registered!"
                                        : "Account created, but the QR email could not be sent. Log in and use Generate QR Code in My Account.");
                                Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                btn_close.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        messageDialog.Dismiss();
                                        startActivity(new Intent(OTPverification.this, Login.class));
                                    }
                                });
                            }
                            else {
                                MessageDialog messageDialog = new MessageDialog(OTPverification.this);
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

    private void updateEmail(){
        Python py = Python.getInstance();
        final PyObject Tools_py = py.getModule("Tools");
        final PyObject Database_py = py.getModule("Database");
        LoadingDialog loadingDialog = new LoadingDialog(OTPverification.this);
        MessageDialog messageDialog = new MessageDialog(OTPverification.this);

        loadingDialog.Start("Updating...");
        new Thread() {
            @Override
            public void run() {
                Boolean ConnectionAvailability;
                try {
                    Database_py.callAttr("UpdateAccountInformation", UniqueID, ID, StoreName, Location, Email, Password);
                    Tools_py.callAttr("SendAccountChangedInformation", "Email", "None", "None", Email, PrevEmail);
                    ConnectionAvailability = true;
                }
                catch (Exception e){
                    ConnectionAvailability = false;
                }
                try {
                    Boolean finalConnectionAvailability = ConnectionAvailability;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.Dismiss();
                            if (finalConnectionAvailability) {
                                Database_py.callAttr("UpdateAccountInformation2", UniqueID, ID, StoreName, Location, Email, Password);
                                messageDialog.Show("positive", "Email updated successfully!");
                                Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                btn_close.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        messageDialog.Dismiss();
                                        startActivity(new Intent(OTPverification.this, Account.class));
                                    }
                                });
                            }
                            else {
                                MessageDialog messageDialog = new MessageDialog(OTPverification.this);
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

    private void setupOTPinputs(){
        et_otp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()){
                    et_otp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_otp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()){
                    et_otp3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_otp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()){
                    et_otp4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_otp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()){
                    et_otp5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_otp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()){
                    et_otp6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void StartCountDown() {
        countDownTimer = new CountDownTimer(TimeLeftInMillis, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                TimeLeftInMillis = l;

                int minute = (int)(TimeLeftInMillis/1000)/60;
                int second = (int)(TimeLeftInMillis/1000)%60;
                @SuppressLint("DefaultLocale") String timeFormat = String.format("%02d:%02d", minute, second);
                tv_countdown.setText(timeFormat + " |");
            }

            @Override
            public void onFinish() {
                btn_resend.setTextColor(getColor(R.color.darkblue2));
                btn_resend.setEnabled(true);
            }
        }.start();
    }

    // make it focus on previous edit text when user click backspace on empty edit text
    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_DEL)){
            if (et_otp2.hasFocus()){
                et_otp1.requestFocus();
            }
            else if (et_otp3.hasFocus()){
                et_otp2.requestFocus();
            }
            else if (et_otp4.hasFocus()){
                et_otp3.requestFocus();
            }
            else if (et_otp5.hasFocus()){
                et_otp4.requestFocus();
            }
            else if (et_otp6.hasFocus()){
                if (et_otp6.getText().toString().equals("")){
                    et_otp5.requestFocus();
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void onBackPressed() {
        Intent intent = this.getIntent();
        String type = intent.getStringExtra("type");

        moveTaskToBack(true);
        if (type.equals("register")) {
            startActivity(new Intent(OTPverification.this, Register.class));
        }
        else if (type.equals("changeEmail")) {
            startActivity(new Intent(OTPverification.this, UpdateEmail.class));
        }
        else if (type.equals("resetPassword")) {
            startActivity(new Intent(OTPverification.this, FindAccount.class));
        }
        else {
            startActivity(new Intent(OTPverification.this, Account.class));
        }
    }
}
