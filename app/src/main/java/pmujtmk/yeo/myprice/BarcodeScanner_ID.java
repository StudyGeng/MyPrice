package pmujtmk.yeo.myprice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;

public class BarcodeScanner_ID extends Activity implements DecoratedBarcodeView.TorchListener {

    CaptureManager capture;
    DecoratedBarcodeView barcodeScannerView;
    ImageButton btn_FlashLight, btn_Close;
    Button btn_EnterManually;
    ViewfinderView viewfinderView;
    boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_barcode_scanner_id);

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner_id);
        barcodeScannerView.setTorchListener(this);
        viewfinderView = findViewById(R.id.zxing_viewfinder_view);
        btn_FlashLight = findViewById(R.id.btn_flashlight_id);
        btn_EnterManually = findViewById(R.id.btn_entermanually_id);
        btn_Close = findViewById(R.id.btn_close_id);

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.setShowMissingCameraPermissionDialog(false);
        capture.decode();

        int color = Color.argb(100, 0, 0,0);
        viewfinderView.setMaskColor(color);
        viewfinderView.setLaserVisibility(true);

        if (!hasFlash()) {
            btn_FlashLight.setVisibility(View.GONE);
        }

        btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_FlashLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen) {
                    barcodeScannerView.setTorchOff();
                    isOpen = false;
                }
                else {
                    barcodeScannerView.setTorchOn();
                    isOpen = true;
                }
            }
        });

        btn_EnterManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BarcodeScanner_ID.this, ManualEnterBarcode_ID.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onTorchOn() {
        btn_FlashLight.setImageResource(R.drawable.ic_baseline_flashlight_on_24);
    }

    @Override
    public void onTorchOff() {
        btn_FlashLight.setImageResource(R.drawable.ic_baseline_flashlight_off_24);
    }

}