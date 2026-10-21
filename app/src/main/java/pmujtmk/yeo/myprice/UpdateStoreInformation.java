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
import com.google.firebase.auth.FirebaseAuth;

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
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }
        LoadingDialog loadingDialog = new LoadingDialog(UpdateStoreInformation.this);
        MessageDialog messageDialog = new MessageDialog(UpdateStoreInformation.this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!uid.equals(Database_py.callAttr("GetLoggedIn_ID").toString())) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

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
                    String StoreName = et_storename.getText().toString();
                    String Location = et_location.getText().toString();
                    String Email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    loadingDialog.Start("Updating...");
                    new Thread() {
                        @Override
                        public void run() {
                            boolean updated = false;
                            try {
                                if (!uid.equals(FirebaseBridge.getUid())) {
                                    throw new IllegalStateException("Store session changed");
                                }
                                Database_py.callAttr("UpdateAccountInformation", uid, uid, StoreName, Location, Email, "");
                                updated = true;
                            } catch (Exception e) {
                                updated = false;
                            }
                            boolean finalUpdated = updated;
                            runOnUiThread(() -> {
                                loadingDialog.Dismiss();
                                if (finalUpdated) {
                                    Database_py.callAttr("SaveLoginInformation", uid, uid, StoreName, Location, Email, "");
                                    messageDialog.Show("positive", "Store information updated successfully!");
                                    Button close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                    close.setOnClickListener(v1 -> {
                                        messageDialog.Dismiss();
                                        startActivity(new Intent(UpdateStoreInformation.this, Account.class));
                                    });
                                } else {
                                    messageDialog.Show("error", "Could not update store information. Check your connection and store approval.");
                                    Button close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                    close.setOnClickListener(v1 -> messageDialog.Dismiss());
                                }
                            });
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
