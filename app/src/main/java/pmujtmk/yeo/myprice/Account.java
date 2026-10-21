package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class Account extends AppCompatActivity{
    TextView tv_storename, tv_location;
    Button btn_storeinformation, btn_email, btn_password, btn_back, btn_GenerateQRCode, btn_DeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_account);

        LoadingDialog loadingDialog = new LoadingDialog(Account.this);
        MessageDialog messageDialog = new MessageDialog(Account.this);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");

        tv_storename = findViewById(R.id.tv_storename_myaccount);
        tv_location = findViewById(R.id.tv_location_myaccount);
        btn_email = findViewById(R.id.btn_email_myaccount);
        btn_password = findViewById(R.id.btn_password_myaccount);
        btn_back = findViewById(R.id.btn_back_findacc);
        btn_storeinformation = findViewById(R.id.btn_storeinformation_myaccount);
        btn_GenerateQRCode = findViewById(R.id.btn_qrcode_myaccount);
        btn_DeleteAccount = findViewById(R.id.btn_deleteacc_myaccount);

        tv_storename.setText(Database_py.callAttr("GetLoggedIn_StoreName").toString());
        tv_location.setText(Database_py.callAttr("GetLoggedIn_Location").toString());

        btn_storeinformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Account.this, UpdateStoreInformation.class));
            }
        });

        btn_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Account.this, UpdateEmail.class));
            }
        });

        btn_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Account.this, UpdatePassword.class));
            }
        });

        btn_GenerateQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UniqueID = Database_py.callAttr("GetLoggedIn_ID").toString();
                String StoreName = Database_py.callAttr("GetLoggedIn_StoreName").toString();
                String Location = Database_py.callAttr("GetLoggedIn_Location").toString();
                String Email = Database_py.callAttr("GetLoggedIn_Email").toString();
                String Password = Database_py.callAttr("GetLoggedIn_Password").toString();

                loadingDialog.Start("Generating Unique QR Code");
                new Thread() {
                    @Override
                    public void run() {
                        int LoginResult;
                        try {
                            LoginResult = Database_py.callAttr("Login", Email, Password).toInt();
                            if (LoginResult == 0) {
                                Tools_py.callAttr("Send_QRcode_To_User", UniqueID, StoreName, Location, Email, "", "generate");
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
                                            messageDialog.Show("positive", "Unique QR code generated!\nPlease check your email.");
                                            Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                            btn_close.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    messageDialog.Dismiss();
                                                }
                                            });
                                            break;

                                        case 1:
                                            // Login Error: Email changed/Account deleted
                                            // Go to Login
                                            messageDialog.Show("error", "Your account might has been deleted or your email has been changed."); // Display error message
                                            Button btn_close5 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                            btn_close5.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    messageDialog.Dismiss();
                                                    startActivity(new Intent(Account.this, Login.class));
                                                }
                                            });
                                            break;

                                        case 2:
                                            // Login Error: Password changed
                                            // Go to Login
                                            messageDialog.Show("error", "Your password has been changed."); // Display error message
                                            Button btn_close2 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                            btn_close2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    messageDialog.Dismiss();
                                                    startActivity(new Intent(Account.this, Login.class));
                                                }
                                            });
                                            break;

                                        default:
                                            // Login error: No Internet Connection
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
        });

        btn_DeleteAccount.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(Account.this);
                dialog.setContentView(R.layout.question_dialog);
                dialog.setCancelable(false);

                Button btn_yes, btn_no;
                TextView tv_message;
                tv_message = dialog.findViewById(R.id.tv_message_qd);
                btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                btn_no = dialog.findViewById(R.id.btn_no_qd);

                tv_message.setText("Do you sure to delete this account?");
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        String email = Database_py.callAttr("GetLoggedIn_Email").toString();
                        String password = Database_py.callAttr("GetLoggedIn_Password").toString();
                        loadingDialog.Start("Loading...");
                        new Thread(){
                            @Override
                            public void run() {
                                String[][] Data = null;
                                int LoginResult;
                                String StoreName = null;
                                String Location = null;
                                try {
                                    LoginResult = Database_py.callAttr("Login", email, password).toInt();
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
                                                    String UniqueID = Database_py.callAttr("GetLoggedIn_UniqueID").toString();
                                                    String ID = Database_py.callAttr("GetLoggedIn_ID").toString();
                                                    String Email = Database_py.callAttr("GetLoggedIn_Email").toString();

                                                    Intent i = new Intent(Account.this, OTPverification.class);
                                                    i.putExtra("email", Email);
                                                    i.putExtra("type", "deleteAccount");
                                                    i.putExtra("uniqueid", UniqueID);
                                                    i.putExtra("id", ID);
                                                    startActivity(i);
                                                    break;

                                                case 1:
                                                    // Login Error: Email changed/Account deleted
                                                    // Go to Login
                                                    messageDialog.Show("error", "Your account might has been deleted or your email has been changed."); // Display error message
                                                    Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                    btn_close.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            messageDialog.Dismiss();
                                                            startActivity(new Intent(Account.this, Login.class));
                                                        }
                                                    });
                                                    break;

                                                case 2:
                                                    // Login Error: Password changed
                                                    // Go to Login
                                                    messageDialog.Show("error", "Your password has been changed."); // Display error message
                                                    Button btn_close2 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                    btn_close2.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            messageDialog.Dismiss();
                                                            startActivity(new Intent(Account.this, Login.class));
                                                        }
                                                    });
                                                    break;

                                                default:
                                                    // Login error: No Internet Connection
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
                });

                btn_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account.this, Store_Home.class);
                intent.putExtra("refresh", "false");
                startActivity(intent);
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        Intent intent = new Intent(Account.this, Store_Home.class);
        intent.putExtra("refresh", "false");
        startActivity(intent);
    }
}