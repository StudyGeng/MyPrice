package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class Account extends AppCompatActivity{
    TextView tv_storename, tv_location;
    Button btn_storeinformation, btn_email, btn_password, btn_back, btn_GenerateQRCode, btn_DeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_account);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        LoadingDialog loadingDialog = new LoadingDialog(Account.this);
        MessageDialog messageDialog = new MessageDialog(Account.this);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!uid.equals(Database_py.callAttr("GetLoggedIn_ID").toString())) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

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
                loadingDialog.Start("Generating Store QR Code...");
                new Thread(() -> {
                    Bitmap qr = null;
                    try {
                        if (!uid.equals(FirebaseBridge.getUid()) ||
                                !Database_py.callAttr("CheckMerchantApproval", uid).toBoolean()) {
                            throw new IllegalStateException("Store is not approved");
                        }
                        BitMatrix bits = new MultiFormatWriter().encode(uid, BarcodeFormat.QR_CODE, 720, 720);
                        qr = Bitmap.createBitmap(720, 720, Bitmap.Config.RGB_565);
                        for (int x = 0; x < 720; x++) {
                            for (int y = 0; y < 720; y++) {
                                qr.setPixel(x, y, bits.get(x, y) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    } catch (Exception ignored) {
                        qr = null;
                    }
                    Bitmap finalQr = qr;
                    runOnUiThread(() -> {
                        loadingDialog.Dismiss();
                        if (finalQr == null) {
                            messageDialog.Show("error", "Could not generate QR code. Check your connection and store approval.");
                            Button close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                            close.setOnClickListener(v -> messageDialog.Dismiss());
                            return;
                        }
                        ImageView image = new ImageView(Account.this);
                        image.setImageBitmap(finalQr);
                        image.setAdjustViewBounds(true);
                        int padding = (int) (16 * getResources().getDisplayMetrics().density);
                        image.setPadding(padding, padding, padding, padding);
                        new AlertDialog.Builder(Account.this)
                                .setTitle("Store QR Code")
                                .setMessage("Shoppers can scan this code. You can take a screenshot to print it.")
                                .setView(image)
                                .setPositiveButton("Close", null)
                                .show();
                    });
                }).start();
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

                tv_message.setText("See how to delete this store account in Firebase Console?");
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        new AlertDialog.Builder(Account.this)
                                .setTitle("Delete store account")
                                .setMessage("In Realtime Database, remove this store's entries under items, stores, merchantProfiles, approvalRequests, and merchantApprovals. Then delete the user in Authentication > Users. Store ID: " + uid)
                                .setPositiveButton("OK", null)
                                .show();
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
