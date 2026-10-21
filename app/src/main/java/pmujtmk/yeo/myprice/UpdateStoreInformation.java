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

public class UpdateStoreInformation extends AppCompatActivity{
    EditText et_storename, et_location;
    Button btn_back, btn_save;
    TextView tv_prevStoreName, tv_prevLocation;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_update_store_information);
        LoadingDialog loadingDialog = new LoadingDialog(UpdateStoreInformation.this);
        MessageDialog messageDialog = new MessageDialog(UpdateStoreInformation.this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");

        et_storename = findViewById(R.id.et_storename_storeinfo);
        et_location = findViewById(R.id.et_location_storeinfo);
        btn_back = findViewById(R.id.btn_back_storeinfo);
        btn_save = findViewById(R.id.btn_save_storeinfo);
        tv_prevStoreName = findViewById(R.id.tv_prevstorenamedisplay_storeinfo);
        tv_prevLocation = findViewById(R.id.tv_prevlocationdisplay_storeinfo);

        tv_prevStoreName.setText(": " + Database_py.callAttr("GetLoggedIn_StoreName").toString());
        tv_prevLocation.setText(": " + Database_py.callAttr("GetLoggedIn_Location").toString());

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UpdateStoreInformation.this, Account.class));
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onClick(View view) {
                String Validation = Tools_py.callAttr("StoreNameAndLocationValidation", et_storename.getText().toString(), et_location.getText().toString()).toString();
                if (Validation.equals("Valid")){
                    String UniqueID = Database_py.callAttr("GetLoggedIn_UniqueID").toString();
                    String ID = Database_py.callAttr("GetLoggedIn_ID").toString();
                    String StoreName = et_storename.getText().toString();
                    String Location = et_location.getText().toString();
                    String Email = Database_py.callAttr("GetLoggedIn_Email").toString();
                    String Password = Database_py.callAttr("GetLoggedIn_Password").toString();

                    loadingDialog.Start("Updating...");
                    new Thread() {
                        @Override
                        public void run() {
                            int LoginResult;
                            try {
                                LoginResult = Database_py.callAttr("Login", Email, Password).toInt();
                                if (LoginResult == 0) {
                                    Database_py.callAttr("UpdateAccountInformation", UniqueID, ID, StoreName, Location, Email, Password);
                                    Tools_py.callAttr("SendAccountChangedInformation", "StoreInformation", StoreName, Location, "None", Email);
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
                                                Database_py.callAttr("UpdateAccountInformation2", UniqueID, ID, StoreName, Location, Email, Password);

                                                messageDialog.Show("positive", "Store information updated successfully!");
                                                Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                        startActivity(new Intent(UpdateStoreInformation.this, Account.class));
                                                    }
                                                });
                                                break;

                                            case 1:
                                                // Login Error: Email changed/Account deleted
                                                // Go to Login
                                                loadingDialog.Dismiss();
                                                messageDialog.Show("error", "Your account might has been deleted or your email has been changed."); // Display error message
                                                Button btn_close5 = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close5.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                        startActivity(new Intent(UpdateStoreInformation.this, Login.class));
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
                                                        startActivity(new Intent(UpdateStoreInformation.this, Login.class));
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
                    if (String.valueOf(Validation.charAt(0)).equals("1")){
                        et_storename.setError("Invalid Store Name");
                    }
                    if (String.valueOf(Validation.charAt(1)).equals("1")){
                        et_location.setError("Invalid Location");
                    }
                }
            }
        });
    }

}