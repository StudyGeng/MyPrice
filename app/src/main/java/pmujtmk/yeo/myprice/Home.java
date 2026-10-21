package pmujtmk.yeo.myprice;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {

    Button btn_start, btn_login, btn_history;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable dark mode
        setContentView(R.layout.activity_home);

        // Declare the dialog
        MessageDialog messageDialog = new MessageDialog(Home.this);
        LoadingDialog loadingDialog = new LoadingDialog(Home.this);

        // Make sure Python is ready
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        // Declare the python function
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database"); // Link to Database.py
        final PyObject Cart_py = py.getModule("Cart"); // Link to Cart.py
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py

        // Check either the needed table in local database (sqlite3) already created or not
        // If not, the system will create it

        // Assign id
        btn_start = findViewById(R.id.btn_start_home);
        btn_login = findViewById(R.id.btn_login_home);
        btn_history = findViewById(R.id.btn_history_home);

        // Button Start
        btn_start.setOnClickListener(view -> {
            // Check either the cart is empty or not
            if (Cart_py.callAttr("GetBarcode").toJava(String[].class).length != 0){
                // Cart is not empty
                // Display question dialog - Do you want to continue the last shopping session...
                Dialog dialog = new Dialog(Home.this);
                dialog.setContentView(R.layout.question_dialog);
                dialog.setCancelable(false);

                Button btn_yes, btn_no;
                TextView tv_message;
                tv_message = dialog.findViewById(R.id.tv_message_qd);
                btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                btn_no = dialog.findViewById(R.id.btn_no_qd);

                String StoreName = Store_py.callAttr("GetStoreName").toString();
                tv_message.setText("Do you want to continue the last shopping session at " + StoreName + "?");

                // Yes: Go to cart
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        startActivity(new Intent(Home.this, Cart.class));
                    }
                });

                // No: Open QR code scanner (new session)
                btn_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        StartNewSession();
                    }
                });
                dialog.show();
            }
            else{
                // Cart is empty
                //Open QR code scanner (new session)
                StartNewSession();
            }
        });

        // Button Login
        btn_login.setOnClickListener(view -> {
            String cachedStoreId = Database_py.callAttr("GetLoggedIn_ID").toString();
            if (FirebaseAuth.getInstance().getCurrentUser() == null
                    || !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(cachedStoreId)) {
                startActivity(new Intent(Home.this, Login.class));
            } else {
                Intent intent1 = new Intent(Home.this, Store_Home.class);
                intent1.putExtra("refresh", "true");
                startActivity(intent1);
            }
        });

        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, ViewHistory.class));
            }
        });

        Intent intent = this.getIntent();

        String result = intent.getStringExtra("result");
        if (result != null){
            resultManagement(result);
        }
        else {
            Database_py.callAttr("CheckTable");
        }
    }

    // QR code scanner
    private void ScanQR() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(QRcodeScanner.class);
        barLaucher.launch(options);
    }

    // Get result from QR code Scanner
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null){
            Python py = Python.getInstance(); // Declare python function
            final PyObject Tools_py = py.getModule("Tools"); // Link to Tools.py
            String Decryptedresult;
            try {
                Decryptedresult = Tools_py.callAttr("DecryptData", result.getContents()).toString();
            } catch (Exception e) {
                Decryptedresult = "";
            }
            resultManagement(Decryptedresult);
        }
    });

    // Start new session
    private void StartNewSession(){
        Python py = Python.getInstance(); // Declare python function
        final PyObject Database_py = py.getModule("Database"); // Link to Database.py
        // Display loading dialog while download store detail from firebase
        LoadingDialog loadingDialog = new LoadingDialog(Home.this);
        loadingDialog.Start("Launching QR Code Scanner...");
        new Thread() {
            @Override
            public void run() {
                String[][] Data;
                try {
                    // Downloading store detail
                    Data = Database_py.callAttr("DownloadStoreDetail").toJava(String[][].class);
                }
                catch (Exception e){
                    // No internet connection
                    Data = null;
                }
                try {
                    String[][] finalData = Data;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (finalData != null) {
                                // Download completed
                                Database_py.callAttr("ResetTable"); // Replace the previous session only after a successful download
                                for (int i = 0; i < finalData.length; i++) {
                                    // Save data into local database (sqlite3)
                                    Database_py.callAttr("SaveStoreDetail", finalData[i][0], finalData[i][1], finalData[i][2]);
                                }
                                loadingDialog.Dismiss();
                                // Launching QR Code Scanner
                                ScanQR();
                            }
                            else{
                                // Download incompleted
                                // Display error no internet connection
                                MessageDialog messageDialog = new MessageDialog(Home.this);
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

    private void resultManagement(String result) {
        Python py = Python.getInstance(); // Declare python function
        final PyObject Database_py = py.getModule("Database"); // Link to Database.py
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py
        MessageDialog messageDialog = new MessageDialog(Home.this); // Declare message dialog
        PyObject obj = Store_py.callAttr("CheckQRCode", result); // Check either QR Code is valid or not
        String QRcontent = obj.toString();

        if(QRcontent.equals("Not Found")) {
            // QR code is invalid
            messageDialog.Show("error", "Invalid QR Code!");
            Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    messageDialog.Dismiss();
                }
            });
        }
        else{
            // QR code is valid
            // Display loading dialog while downloading Item List from Firebase
            LoadingDialog loadingDialog = new LoadingDialog(Home.this);
            loadingDialog.Start("Downloading Data...");
            new Thread() {
                @Override
                public void run() {
                    String[][] Data;
                    try {
                        Data = Database_py.callAttr("DownloadItemList", QRcontent).toJava(String[][].class); // Download Item List from Firebase
                    }
                    catch (Exception e){
                        Data = null; // No internet connection
                    }
                    try {
                        String[][] finalData = Data;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalData != null) {
                                    // Download completed
                                    // Save data into local database (sqlite3)
                                    for (int i = 0; i < finalData.length; i++) {
                                        Database_py.callAttr("SaveItemList", finalData[i][1], finalData[i][2], finalData[i][3], finalData[i][4], finalData[i][5], finalData[i][6], finalData[i][7], finalData[i][8], finalData[i][9], finalData[i][10]);
                                    }
                                    Store_py.callAttr("AddShoppingDetail", QRcontent); // Add shop detail into local database (sqlite3)
                                    loadingDialog.Dismiss();
                                    startActivity(new Intent(Home.this, SetBudget.class)); // Go to Set Budget
                                }
                                else{
                                    // Download incompleted
                                    // Display error no internet connection
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
                    } catch (final Exception ex) {

                    }
                }
            }.start();
        }
    }

    // Go back function
    @Override
    public void onBackPressed() {
        moveTaskToBack(true); // Quit the apps
    }
}
