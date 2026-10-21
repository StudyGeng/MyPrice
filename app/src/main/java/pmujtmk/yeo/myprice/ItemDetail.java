package pmujtmk.yeo.myprice;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ItemDetail extends AppCompatActivity {

    TextView TV_StoreName, TV_Budget, TV_Balance, TV_Name, TV_Price, TV_promotion1, TV_promotion2, TV_BalanceTxt;
    Button Btn_Scan, Btn_AddToCart, Btn_Cart, Btn_Back, Btn_Menu;
    ImageView IV_image;
    boolean prevOverLimit = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable dark mode

        PromotionDialog promotionDialog = new PromotionDialog(ItemDetail.this);

        // Declare vibrator function
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Declare message dialog
        MessageDialog messageDialog = new MessageDialog(ItemDetail.this);

        // Make sure python is ready
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        // Declare the python
        Python py = Python.getInstance();
        final PyObject Cart_py = py.getModule("Cart"); // Link to Cart.py
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py
        final PyObject Tools_py = py.getModule("Tools"); // Link to Tools.py

        // Assign ID
        IV_image = findViewById(R.id.iv_image_id);
        TV_StoreName = findViewById(R.id.tv_storename_id);
        TV_Budget = findViewById(R.id.tv_budget_id);
        TV_Balance = findViewById(R.id.tv_balance_id);
        TV_Name = findViewById(R.id.tv_name_id);
        TV_Price = findViewById(R.id.tv_price_id);
        TV_promotion1 = findViewById(R.id.tv_promotion1_id);
        TV_promotion2 = findViewById(R.id.tv_promotion2_id);
        TV_BalanceTxt = findViewById(R.id.tv_balancetxt_id);
        Btn_Scan = findViewById(R.id.btn_scanagain_id);
        Btn_AddToCart = findViewById(R.id.btn_addtocart_id);
        Btn_Cart = findViewById(R.id.btn_cart_id);
        Btn_Back = findViewById(R.id.btn_back_id);
        Btn_Menu = findViewById(R.id.btn_menu_id);

        String barcode = null;
        String name;
        String price = null;
        String image;
        String all_discount;
        String all_limit;
        String member_discount;
        String member_limit;
        String start_day;
        String end_day;

        Intent intent = this.getIntent();
        String result = intent.getStringExtra("result");
        if (result != null){
            resultManagement(result);
        }
        else {
            // Get values from previous page
            image = intent.getStringExtra("image");
            barcode = intent.getStringExtra("barcode");
            name = intent.getStringExtra("name");
            price = intent.getStringExtra("price");
            all_discount = intent.getStringExtra("all_dis");
            all_limit = intent.getStringExtra("all_limit");
            member_discount = intent.getStringExtra("mem_dis");
            member_limit = intent.getStringExtra("mem_limit");
            start_day = intent.getStringExtra("start");
            end_day = intent.getStringExtra("end");

            TV_Name.setText(name);
            TV_Price.setText(price);

            // Alert user when they spend more than 90% of their budget
            if (Cart_py.callAttr("SpendLimitAlert").toBoolean()){
                // Over limit
                TV_Balance.setTextColor(getColor(R.color.red));
                TV_BalanceTxt.setTextColor(getColor(R.color.red));
            }
            else{
                // Within limit
                TV_Balance.setTextColor(getColor(R.color.black));
                TV_BalanceTxt.setTextColor(getColor(R.color.black));
            }

            // Display promotion
            if (!start_day.equals("Tap to set") && Tools_py.callAttr("PromotionValidation", start_day, end_day).toBoolean()){
                if (!all_discount.equals(price) && !member_discount.equals(price)){ // Both available
                    if (!all_limit.equals("0")){
                        TV_promotion1.setText("For All: " + all_discount + " (Limit: " + all_limit + ")");
                    }
                    else {
                        TV_promotion1.setText("For All: " + all_discount);
                    }
                    if (!member_limit.equals("0")){
                        TV_promotion2.setText("Member Price: " + member_discount + "(Limit: " + member_limit + ")");
                    }
                    else {
                        TV_promotion2.setText("Member Price: " + member_discount);
                    }
                }
                else if (!all_discount.equals(price)){ // All Discount
                    if (!all_limit.equals("0")){
                        TV_promotion1.setText("For All: " + all_discount + " (Limit: " + all_limit + ")");
                    }
                    else {
                        TV_promotion1.setText("For All: " + all_discount);
                    }
                    TV_promotion2.setText("");
                }
                else { //Member Discount
                    if (!member_limit.equals("0")){
                        TV_promotion1.setText("Member Price: " + member_discount + " (Limit: " + member_limit + ")");
                    }
                    else {
                        TV_promotion1.setText("Member Price: " + member_discount);
                    }
                    TV_promotion2.setText("");
                }
            }
            else {
                TV_promotion1.setText("Sorry, currently this item don't");
                TV_promotion2.setText("have any promotions.");
            }

            // Display image
            if (!image.equals("Not Available")){
                // Image available
                byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                IV_image.setImageBitmap(bitmap);
            }
            else{
                // Image not available - Set to default image
                IV_image.setImageResource(R.drawable.imgnotfound);
            }
        }
        PyObject balance = Cart_py.callAttr("CheckBalance", true); // Get Balance in RM form
        PyObject budget = Cart_py.callAttr("CheckBudget", true); // Get Budget in RM form
        PyObject StoreName = Store_py.callAttr("GetStoreName"); // Get store name

        TV_StoreName.setText(StoreName.toString());
        TV_Budget.setText(budget.toString());
        TV_Balance.setText(balance.toString());

        // Button Scan
        Btn_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanBarcode();
            }
        });

        // Button Add To Cart
        String finalPrice = price;
        String finalBarcode = barcode;
        Btn_AddToCart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onClick(View view) {
                Double balance = Double.parseDouble(Cart_py.callAttr("CheckBalance", false).toString()); // Get balance in double form
                Double itemPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", finalPrice).toString()); // Convert price into RM form
                // Check either balance enough or not
                if (balance >= itemPrice){ // To ensure the budget is enough (compared with maximum value)
                    // balance still enough

                    Cart_py.callAttr("AddToCart", finalBarcode); // Add item into cart

                    PyObject balanceStr = Cart_py.callAttr("CheckBalance", true); // Get balance in RM form
                    TV_Balance.setText(balanceStr.toString());

                    Toast.makeText(ItemDetail.this, "Added into cart", Toast.LENGTH_SHORT).show();
                    Cart_py.callAttr("ResetAndManageCart");

                    // Check either spend more than 90% or not
                    if (Cart_py.callAttr("SpendLimitAlert").toBoolean()){
                        // Over limit
                        if (!prevOverLimit){
                            // First time
                            TV_Balance.setTextColor(getColor(R.color.red));
                            TV_BalanceTxt.setTextColor(getColor(R.color.red));
                            // Create vibration
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            }
                            else{
                                v.vibrate(500);
                            }
                            messageDialog.Show("negative", "Oh no! You already spend more than 90% of your budget.");
                            Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                            btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    messageDialog.Dismiss();
                                }
                            });
                            prevOverLimit = true;
                        }
                    }
                    else{
                        // within limit
                        TV_Balance.setTextColor(getColor(R.color.black));
                        TV_BalanceTxt.setTextColor(getColor(R.color.black));
                        prevOverLimit = false;
                    }
                }
                else{
                    // budget not enough
                    messageDialog.Show("negative", "Sorry, you don't have enough budget.");
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

        // Button Cart
        Btn_Cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenCart();
            }
        });

        // Button Back (Arrow)
        Btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to cart
                startActivity(new Intent(ItemDetail.this, Cart.class));
            }
        });

        // Button Menu (Triple Dot)
        Btn_Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(ItemDetail.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.customer, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.Promotion:
                                promotionDialog.Show();
                                return true;
                            //case R.id.history:
                                // Add Action
                               // return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
    }

    // Go to Cart
    private void OpenCart() {
        startActivity(new Intent(ItemDetail.this, Cart.class));
    }

    // Open Barcode Scanner
    private void ScanBarcode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(BarcodeScanner_ID.class);
        barLaucher.launch(options);
    }

    // Get result from barcode scanner
    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null){
            resultManagement(result.getContents());
        }
    });

    private void resultManagement(String result){
        MessageDialog messageDialog = new MessageDialog(ItemDetail.this); // Declare Message Dialog
        Python py = Python.getInstance(); // Declare python function
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py

        PyObject obj = Store_py.callAttr("CheckBarcode", result); // Check Barcode
        String Barcode = obj.toString();

        if(Barcode.equals("Not Found")) {
            // Invalid Barcode
            messageDialog.Show("error", "Invalid Barcode!");
            Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ItemDetail.this, Cart.class));
                    messageDialog.Dismiss();
                }
            });
        }
        else{
            // Valid Barcode
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
            Intent i = new Intent(ItemDetail.this, ItemDetail.class);
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

    // Go back function
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        startActivity(new Intent(ItemDetail.this, Cart.class)); // Go to cart
    }
}