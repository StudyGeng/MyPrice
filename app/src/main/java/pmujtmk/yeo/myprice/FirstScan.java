package pmujtmk.yeo.myprice;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
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

public class FirstScan extends AppCompatActivity {

    Button Btn_Scan;
    TextView TV_StoreName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable Dark Mode
        setContentView(R.layout.activity_first_scan);

        // Make sure Python is ready
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        // Declare the python function
        Python py = Python.getInstance();
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py

        PyObject obj = Store_py.callAttr("GetStoreName"); // Get Store Name

        // Assign id
        Btn_Scan = findViewById(R.id.btn_scan_fs);
        TV_StoreName = findViewById(R.id.lbl_storename_fs);

        TV_StoreName.setText(obj.toString());

        // Button Scan
        Btn_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanBarcode();
            }
        });

        Intent intent = this.getIntent();

        String result = intent.getStringExtra("result");
        if (result != null){
            resultManagement(result);
        }
    }

    // QR code scanner
    private void ScanBarcode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(BarcodeScanner_FS.class);
        barLaucher.launch(options);
    }

    // Get result from QR code Scanner
    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null){
            resultManagement(result.getContents());
        }
    });

    private void resultManagement(String result){
        Python py = Python.getInstance(); // Declare python function
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py
        MessageDialog messageDialog = new MessageDialog(FirstScan.this); // Declare message dialog
        PyObject obj = Store_py.callAttr("CheckBarcode", result);
        String Barcode = obj.toString();

        // Check Scanner Barcode
        if(Barcode.equals("Not Found")) {
            // Invalid Barcode
            messageDialog.Show("error", "Invalid Barcode!");
            Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    messageDialog.Dismiss();
                }
            });
        }

        else{
            // Valid Barcode
            // Go to Item Detail
            String ItemName = Store_py.callAttr("GetItemName", Barcode).toString();
            String ItemPrice = Store_py.callAttr("GetItemPrice", Barcode).toString();
            String ItemAllDiscountPrice = Store_py.callAttr("GetItemAllDiscountPrice", Barcode).toString();
            String ItemAllDiscountLimit = Store_py.callAttr("GetItemAllDiscountLimit", Barcode).toString();
            String ItemMemberDiscountPrice = Store_py.callAttr("GetItemMemberDiscountPrice", Barcode).toString();
            String ItemMemberDiscountLimit = Store_py.callAttr("GetItemMemberDiscountLimit", Barcode).toString();
            String ItemPromotionStart = Store_py.callAttr("GetItemPromotionDuration", Barcode).toJava(String[].class)[0];
            String ItemPromotionEnd = Store_py.callAttr("GetItemPromotionDuration", Barcode).toJava(String[].class)[1];
            String ItemImage = Store_py.callAttr("GetItemImage", Barcode).toString();

            // Pass values to Item Detail
            Intent i = new Intent(FirstScan.this, ItemDetail.class);
            i.putExtra("barcode", Barcode);
            i.putExtra("name", ItemName);
            i.putExtra("price", ItemPrice);
            i.putExtra("all_dis", ItemAllDiscountPrice);
            i.putExtra("all_limit", ItemAllDiscountLimit);
            i.putExtra("mem_dis", ItemMemberDiscountPrice);
            i.putExtra("mem_limit", ItemMemberDiscountLimit);
            i.putExtra("start", ItemPromotionStart);
            i.putExtra("end", ItemPromotionEnd);
            i.putExtra("image", ItemImage);

            startActivity(i);
        }
    }
}