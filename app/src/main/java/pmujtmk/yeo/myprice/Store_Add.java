package pmujtmk.yeo.myprice;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.chip.Chip;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class Store_Add extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    Button btn_Save, btn_Reset, btn_back;
    ImageButton btn_ScanBarcode, btn_minus_all, btn_plus_all, btn_minus_member, btn_plus_member;
    EditText et_Barcode, et_Name, et_Price;
    TextView tv_start, tv_end, tv_NewPrice_All, tv_Percentage_All, tv_Limit_All, tv_NewPrice_Member, tv_Percentage_Member, tv_Limit_Member;
    SeekBar seekBar_all, seekBar_member;
    ImageView iv_image;
    String Barcode, Name, Price, ImageValue;
    String all_discount, all_limit, member_discount, member_limit, start_day, end_day, StoreID;
    int percentage_All = 0;
    int percentage_Member = 0;
    boolean isStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_store_add);
        LoadingDialog loadingDialog = new LoadingDialog(Store_Add.this);
        MessageDialog messageDialog = new MessageDialog(Store_Add.this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (ContextCompat.checkSelfPermission(Store_Add.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Store_Add.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");
        StoreID = Database_py.callAttr("GetLoggedIn_ID").toString();

        ImageValue = "Not Available";

        btn_back = findViewById(R.id.btn_back_storeadd);
        btn_Save = findViewById(R.id.btn_save_storeadd);
        btn_Reset = findViewById(R.id.btn_reset_storeadd);
        btn_ScanBarcode = findViewById(R.id.btn_scan_storeadd);
        btn_minus_all = findViewById(R.id.btn_minus_all_storeadd);
        btn_plus_all = findViewById(R.id.btn_plus_all_storeadd);
        btn_minus_member = findViewById(R.id.btn_minus_member_storeadd);
        btn_plus_member = findViewById(R.id.btn_plus_member_storeadd);
        et_Barcode = findViewById(R.id.et_barcode_storeadd);
        et_Name = findViewById(R.id.et_itemname_storeadd);
        et_Price = findViewById(R.id.et_price_storeadd);
        tv_start = findViewById(R.id.tv_start_storeadd);
        tv_end = findViewById(R.id.tv_end_storeadd);
        tv_NewPrice_All = findViewById(R.id.tv_new_all_price_storeadd);
        tv_Percentage_All = findViewById(R.id.tv_all_percentage_storeadd);
        tv_Limit_All = findViewById(R.id.tv_all_limit_storeadd);
        tv_NewPrice_Member = findViewById(R.id.tv_new_member_price_storeadd);
        tv_Percentage_Member = findViewById(R.id.tv_member_percentage_storeadd);
        tv_Limit_Member = findViewById(R.id.tv_member_limit_storeadd);
        seekBar_all = findViewById(R.id.sb_allpercentage_storeadd);
        seekBar_member = findViewById(R.id.sb_member_percentage_storeadd);
        iv_image = findViewById(R.id.iv_image_storeadd);

        iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ImageValue.equals("Not Available")){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 100);
                }
                else {
                    Dialog dialog = new Dialog(Store_Add.this);
                    dialog.setContentView(R.layout.setimage_dialog);
                    dialog.setCancelable(false);
                    Button btn_newImage = dialog.findViewById(R.id.btn_addimage);
                    Button btn_removeImage = dialog.findViewById(R.id.btn_removeimage);
                    ImageButton btn_close = dialog.findViewById(R.id.btn_close_image);

                    btn_newImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, 100);
                        }
                    });

                    btn_removeImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            iv_image.setImageResource(R.drawable.imgnotfound);
                            ImageValue = "Not Available";
                        }
                    });

                    btn_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });

        btn_ScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
            }
        });

        et_Price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (et_Price.getText().toString().equals("")){
                    tv_NewPrice_All.setText("Invalid");
                    tv_NewPrice_Member.setText("Invalid");
                }
                else {
                    UpdateDiscount();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tv_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStart = true;
                tv_end.setText("Tap to set");
                showDatePickerDialog();
            }
        });

        tv_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStart = false;
                if (tv_start.getText().toString().equals("Tap to set")){
                    MessageDialog messageDialog = new MessageDialog(Store_Add.this);
                    messageDialog.Show("error", "Please set the start of the promotion period first!");
                    Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                    btn_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            messageDialog.Dismiss();
                        }
                    });
                }
                else {
                    showDatePickerDialog();
                }
            }
        });

        seekBar_all.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                percentage_All = i;
                UpdateDiscount();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btn_minus_all.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (percentage_All != 0){
                    percentage_All -= 1;
                    seekBar_all.setProgress(percentage_All);
                    UpdateDiscount();
                }
            }
        });

        btn_plus_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (percentage_All != 900) {
                    percentage_All += 1;
                    seekBar_all.setProgress(percentage_All);
                    UpdateDiscount();
                }
            }
        });

        tv_Limit_All.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(Store_Add.this);
                dialog.setContentView(R.layout.setlimit_dialog);
                dialog.setCancelable(false);
                ImageButton btn_minus = dialog.findViewById(R.id.btn_decrease_limit);
                ImageButton btn_plus = dialog.findViewById(R.id.btn_increase_limit);
                ImageButton btn_close = dialog.findViewById(R.id.btn_close_setlimit);
                Button btn_set = dialog.findViewById(R.id.btn_set);
                TextView tv_limit = dialog.findViewById(R.id.tv_limit);

                String currentLimit = tv_Limit_All.getText().toString();
                if (currentLimit.equals("Tap to set")) {
                    tv_limit.setText("No Limit");
                }
                else {
                    tv_limit.setText(tv_Limit_All.getText().toString());
                }
                btn_minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String currentLimit = tv_limit.getText().toString();
                        if (!currentLimit.equals("No Limit")){
                            int NextLimit = Integer.parseInt(currentLimit) - 1;
                            if (NextLimit == 0) {
                                tv_limit.setText("No Limit");
                            }
                            else {
                                tv_limit.setText(String.valueOf(NextLimit));
                            }
                        }
                    }
                });

                btn_plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String currentLimit = tv_limit.getText().toString();
                        if (!currentLimit.equals("No Limit")) {
                            int NextLimit = Integer.parseInt(currentLimit) + 1;
                            tv_limit.setText(String.valueOf(NextLimit));
                        }
                        else {
                            tv_limit.setText("1");
                        }
                    }
                });

                btn_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                btn_set.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tv_Limit_All.setText(tv_limit.getText().toString());
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        seekBar_member.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                percentage_Member = i;
                UpdateDiscount();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btn_minus_member.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (percentage_Member != 0){
                    percentage_Member -= 1;
                    seekBar_member.setProgress(percentage_Member);
                    UpdateDiscount();
                }
            }
        });

        btn_plus_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (percentage_Member != 900) {
                    percentage_Member += 1;
                    seekBar_member.setProgress(percentage_Member);
                    UpdateDiscount();
                }
            }
        });

        tv_Limit_Member.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(Store_Add.this);
                dialog.setContentView(R.layout.setlimit_dialog);
                dialog.setCancelable(false);
                ImageButton btn_minus = dialog.findViewById(R.id.btn_decrease_limit);
                ImageButton btn_plus = dialog.findViewById(R.id.btn_increase_limit);
                ImageButton btn_close = dialog.findViewById(R.id.btn_close_setlimit);
                Button btn_set = dialog.findViewById(R.id.btn_set);
                TextView tv_limit = dialog.findViewById(R.id.tv_limit);

                String currentLimit = tv_Limit_Member.getText().toString();
                if (currentLimit.equals("Tap to set")) {
                    tv_limit.setText("No Limit");
                }
                else {
                    tv_limit.setText(tv_Limit_Member.getText().toString());
                }
                btn_minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String currentLimit = tv_limit.getText().toString();
                        if (!currentLimit.equals("No Limit")){
                            int NextLimit = Integer.parseInt(currentLimit) - 1;
                            if (NextLimit == 0) {
                                tv_limit.setText("No Limit");
                            }
                            else {
                                tv_limit.setText(String.valueOf(NextLimit));
                            }
                        }
                    }
                });

                btn_plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String currentLimit = tv_limit.getText().toString();
                        if (!currentLimit.equals("No Limit")) {
                            int NextLimit = Integer.parseInt(currentLimit) + 1;
                            tv_limit.setText(String.valueOf(NextLimit));
                        }
                        else {
                            tv_limit.setText("1");
                        }
                    }
                });

                btn_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                btn_set.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tv_Limit_Member.setText(tv_limit.getText().toString());
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String validation = Tools_py.callAttr("ItemRegistrationValidation", et_Barcode.getText().toString(), et_Name.getText().toString(), et_Price.getText().toString()).toString();
                Boolean validationPromotion = (tv_start.getText().toString().equals("Tap to set")) && (tv_end.getText().toString().equals("Tap to set")) && (tv_Percentage_All.getText().toString().equals("0.0%"))
                        && (tv_Percentage_Member.getText().toString().equals("0.0%")) && (tv_Limit_All.getText().toString().equals("Tap to set")) && (tv_Limit_Member.getText().toString().equals("Tap to set"));
                Boolean validationPromotion2 = (!tv_start.getText().toString().equals("Tap to set")) && (!tv_end.getText().toString().equals("Tap to set")) && (!tv_Percentage_All.getText().toString().equals("0.0%"))
                        && (!tv_Limit_All.getText().toString().equals("Tap to set"));
                Boolean validationPromotion3 = (!tv_start.getText().toString().equals("Tap to set")) && (!tv_end.getText().toString().equals("Tap to set")) && (!tv_Percentage_Member.getText().toString().equals("0.0%"))
                        && (!tv_Limit_Member.getText().toString().equals("Tap to set"));

                if (validation.equals("Valid") && (validationPromotion || validationPromotion2 || validationPromotion3)) {
                    Barcode = et_Barcode.getText().toString();
                    Name =  et_Name.getText().toString();
                    Price = et_Price.getText().toString();
                    start_day = tv_start.getText().toString();
                    end_day = tv_end.getText().toString();

                    if (tv_NewPrice_All.getText().toString().equals("None")){
                        all_discount = et_Price.getText().toString();
                        all_limit = "0";
                    }
                    else{
                        all_discount = Tools_py.callAttr("GetDoubleForm", tv_NewPrice_All.getText().toString()).toString();
                        if (tv_Limit_All.getText().toString().equals("Tap to set")){
                            all_limit = "0";
                        }
                        else if (tv_Limit_All.getText().toString().equals("No Limit")){
                            all_limit = "0";
                        }
                        else{
                            all_limit = tv_Limit_All.getText().toString();
                        }
                    }

                    if (tv_NewPrice_Member.getText().toString().equals("None")){
                        member_discount = et_Price.getText().toString();
                        member_limit = "0";
                    }
                    else{
                        member_discount = Tools_py.callAttr("GetDoubleForm", tv_NewPrice_Member.getText().toString()).toString();
                        if (tv_Limit_Member.getText().toString().equals("Tap to set")){
                            member_limit = "0";
                        }
                        else if (tv_Limit_Member.getText().toString().equals("No Limit")){
                            member_limit = "0";
                        }
                        else{
                            member_limit = tv_Limit_Member.getText().toString();
                        }
                    }

                    String email = Database_py.callAttr("GetLoggedIn_Email").toString();
                    String password = Database_py.callAttr("GetLoggedIn_Password").toString();
                    loadingDialog.Start("Registering New Item...");
                    new Thread() {
                        public void run() {
                            Boolean registeredBarcode = null;
                            String UniqueID = null;
                            int LoginResult;
                            try {
                                LoginResult = Database_py.callAttr("Login", email, password).toInt();
                                if (LoginResult == 0) {
                                    registeredBarcode = Database_py.callAttr("CheckRegisteredBarcode", StoreID, Barcode).toBoolean();

                                    if (!registeredBarcode) {
                                        UniqueID = Database_py.callAttr("RegisterItem", StoreID, Barcode, Name, Price, all_discount, all_limit,
                                                member_discount, member_limit, start_day, end_day, ImageValue).toString();
                                    }
                                }
                            }
                            catch (Exception e){
                                LoginResult = 3;
                            }
                            try {
                                String UniqueID_ = UniqueID;
                                Boolean finalRegisteredBarcode = registeredBarcode;
                                int finalLoginResult = LoginResult;
                                runOnUiThread(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {
                                        loadingDialog.Dismiss();
                                        switch (finalLoginResult) {
                                            case 0:
                                                // Login Success
                                                if (!finalRegisteredBarcode) {
                                                    Toast.makeText(Store_Add.this, "New Item Registered!", Toast.LENGTH_LONG).show();
                                                    Database_py.callAttr("AddNewItem", UniqueID_, Barcode, Name, Price, all_discount, all_limit,
                                                            member_discount, member_limit, start_day, end_day, ImageValue);
                                                    Reset();
                                                } else {
                                                    Dialog dialog = new Dialog(Store_Add.this);
                                                    dialog.setContentView(R.layout.question_dialog);
                                                    dialog.setCancelable(false);

                                                    Button btn_yes, btn_no;
                                                    TextView tv_message;
                                                    tv_message = dialog.findViewById(R.id.tv_message_qd);
                                                    btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                                                    btn_no = dialog.findViewById(R.id.btn_no_qd);

                                                    tv_message.setText("This item is already registered. Do you want to update it's information?");
                                                    btn_yes.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            dialog.dismiss();
                                                            Update();
                                                        }
                                                    });

                                                    btn_no.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                    dialog.show();
                                                }break;

                                            case 1:
                                                // Login Error: Email changed/Account deleted
                                                // Go to Login
                                                loadingDialog.Dismiss();
                                                messageDialog.Show("error", "Your account might has been deleted or your email has been changed."); // Display error message
                                                Button btn_close = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                                                btn_close.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        messageDialog.Dismiss();
                                                        startActivity(new Intent(Store_Add.this, Login.class));
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
                                                        startActivity(new Intent(Store_Add.this, Login.class));
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
                else if (!validation.equals("Valid")){
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));

                    if (String.valueOf(validation.charAt(1)).equals("1")){
                        et_Barcode.setError("Required");
                    }
                    if (String.valueOf(validation.charAt(2)).equals("1")){
                        et_Name.setError("Required");
                    }
                    if (String.valueOf(validation.charAt(3)).equals("1")){
                        et_Price.setError("Required");
                    }
                }
                else {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    messageDialog.Show("error", "Please complete the promotion details.");
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

        btn_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Reset();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Store_Add.this, Store_Home.class);
                i.putExtra("refresh", "false");
                startActivity(i);
            }
        });
    }

    private void Update(){
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        LoadingDialog loadingDialog = new LoadingDialog(Store_Add.this);

        loadingDialog.Start("Updating Item...");
        new Thread(){
            @Override
            public void run() {
                String ID = Database_py.callAttr("GetUniqueID", StoreID, Barcode).toString();
                System.out.println("Unique ID:"+ ID);
                Database_py.callAttr("UpdateItemInformation", StoreID, ID, Barcode, Name, Price, all_discount, all_limit, member_discount, member_limit, start_day, end_day, ImageValue);
                try{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.Dismiss();
                            Toast.makeText(Store_Add.this, "Item Updated!", Toast.LENGTH_LONG).show();
                            Database_py.callAttr("UpdateItem", ID, Barcode, Name, Price, all_discount, all_limit, member_discount, member_limit, start_day, end_day, ImageValue);
                            Reset();
                        }
                    });
                }
                catch (final Exception ex){}
            }
        }.start();
    }

    @SuppressLint("SetTextI18n")
    private void Reset() {
        et_Barcode.setText("");
        et_Name.setText("");
        et_Price.setText("");
        iv_image.setImageResource(R.drawable.imgnotfound);
        ImageValue = "Not Available";
        seekBar_all.setProgress(0);
        seekBar_member.setProgress(0);
        tv_start.setText("Tap to set");
        tv_end.setText("Tap to set");
        tv_NewPrice_All.setText("Invalid");
        tv_Percentage_All.setText("0.0%");
        tv_Limit_All.setText("Tap to set");
        tv_NewPrice_Member.setText("Invalid");
        tv_Percentage_Member.setText("0.0%");
        tv_Limit_Member.setText("Tap to set");
    }

    @SuppressLint("SetTextI18n")
    private void UpdateDiscount(){
        Python py = Python.getInstance();
        final PyObject Tools_py = py.getModule("Tools");

        tv_Percentage_All.setText(((double)percentage_All/10) + "%");
        tv_Percentage_Member.setText(((double)percentage_Member/10) + "%");

        if (!et_Price.getText().toString().equals("")){
            double price = Double.parseDouble(et_Price.getText().toString());
            double new_price_all = price - (price * (((double)percentage_All/10) / 100));
            double new_price_member = price - (price * (((double)percentage_Member/10) / 100));
            if (Tools_py.callAttr("GetRMForm", new_price_all).toString().equals(Tools_py.callAttr("GetRMForm", price).toString())){
                tv_NewPrice_All.setText("None");
            }
            else{
                tv_NewPrice_All.setText(Tools_py.callAttr("GetRMForm", new_price_all).toString());
            }

            if (Tools_py.callAttr("GetRMForm", new_price_member).toString().equals(Tools_py.callAttr("GetRMForm", price).toString())){
                tv_NewPrice_Member.setText("None");
            }
            else{
                tv_NewPrice_Member.setText(Tools_py.callAttr("GetRMForm", new_price_member).toString());
            }
        }
    }

    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,Store_Add.this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        if (isStart) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }
        else {
            Python py = Python.getInstance();
            final PyObject Tools_py = py.getModule("Tools");
            int StartDay = Tools_py.callAttr("GetDateValue", tv_start.getText().toString()).toJava(Integer[].class)[0];
            int StartMonth = Tools_py.callAttr("GetDateValue", tv_start.getText().toString()).toJava(Integer[].class)[1];
            int StartYear = Tools_py.callAttr("GetDateValue", tv_start.getText().toString()).toJava(Integer[].class)[2];

            Calendar StartDate = Calendar.getInstance();
            StartDate.set(Calendar.DAY_OF_MONTH, StartDay);
            StartDate.set(Calendar.MONTH, StartMonth - 1);
            StartDate.set(Calendar.YEAR, StartYear);

            datePickerDialog.getDatePicker().setMinDate(StartDate.getTimeInMillis() + (1000 * 60 * 60 * 24));
        }
        datePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayofMonth) {
        month += 1;
        if (isStart) {
            tv_start.setText(dayofMonth + "/" + month + "/" + year);
        }
        else {
            tv_end.setText(dayofMonth + "/" + month + "/" + year);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 100) {
                Bitmap captureImage = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                captureImage.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] bArray = bos.toByteArray();
                ImageValue = Base64.encodeToString(bArray, Base64.DEFAULT);
                iv_image.setImageBitmap(captureImage);
            }
        }
        catch (RuntimeException e){
            System.out.println("Error");
        }
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(BarcodeScanner_Admin.class);
        barLaucher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null){
            et_Barcode.setText(result.getContents());
        }
    });

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        Intent i = new Intent(Store_Add.this, Store_Home.class);
        i.putExtra("refresh", "false");
        startActivity(i);
    }
}
