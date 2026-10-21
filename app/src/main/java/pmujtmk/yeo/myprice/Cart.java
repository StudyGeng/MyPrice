package pmujtmk.yeo.myprice;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

import pmujtmk.yeo.myprice.databinding.ActivityCartBinding;

public class Cart extends AppCompatActivity {
    ActivityCartBinding binding;
    TextView TV_emptycart, TV_Budget, TV_Total, TV_Balance, TV_Saved, TV_BalanceTxt;
    Button Btn_EditBudget, Btn_Home, Btn_Scan, Btn_Checkout, Btn_Menu;

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable dark mode
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Declare message dialog
        MessageDialog messageDialog = new MessageDialog(Cart.this);

        PromotionDialog promotionDialog = new PromotionDialog(Cart.this);

        // Make sure python is ready
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        // Declare python function
        Python py = Python.getInstance();
        final PyObject Cart_py = py.getModule("Cart"); // Link to Cart.py
        final PyObject Tools_py = py.getModule("Tools"); // Link to Tools.py

        // Assign id
        TV_emptycart = findViewById(R.id.txt_emptycart_cart);
        TV_Budget = findViewById(R.id.tv_budget_cart);
        TV_Total = findViewById(R.id.tv_total_cart);
        TV_Balance = findViewById(R.id.tv_balance_cart);
        TV_Saved = findViewById(R.id.tv_saved_cart);
        TV_BalanceTxt = findViewById(R.id.tv_balancetxt_cart);
        Btn_EditBudget = findViewById(R.id.btn_edit_cart);
        Btn_Home = findViewById(R.id.btn_home_cart);
        Btn_Scan = findViewById(R.id.btn_scan_cart);
        Btn_Checkout = findViewById(R.id.btn_checkout_cart);
        Btn_Menu = findViewById(R.id.btn_menu_cart);

        Cart_py.callAttr("ResetAndManageCart");

        // Get Cart detail from local database (sqlite3)
        String[] barcode = Cart_py.callAttr("GetBarcode").toJava(String[].class);
        String[] name = Cart_py.callAttr("GetItemName").toJava(String[].class);
        String[] price = Cart_py.callAttr("GetPrice").toJava(String[].class);
        String[] all_dis_price = Cart_py.callAttr("GetAllDiscountPrice").toJava(String[].class);
        String[] all_dis_limit = Cart_py.callAttr("GetAllDiscountLimit").toJava(String[].class);
        String[] member_dis_price = Cart_py.callAttr("GetMemberDiscountPrice").toJava(String[].class);
        String[] member_dis_limit = Cart_py.callAttr("GetMemberDiscountLimit").toJava(String[].class);
        String[] start_day = Cart_py.callAttr("GetStartPromotion").toJava(String[].class);
        String[] end_day = Cart_py.callAttr("GetEndPromotion").toJava(String[].class);
        String[] image = Cart_py.callAttr("GetImage").toJava(String[].class);
        String[] quantity = Cart_py.callAttr("GetQuantity").toJava(String[].class);
        String[] cartID = Cart_py.callAttr("GetCartID").toJava(String[].class);

        String budget = Cart_py.callAttr("CheckBudget", true).toString(); // get budget in RM form
        String total = Cart_py.callAttr("CheckSpend", true).toString(); // get total spend in RM form
        String balance = Cart_py.callAttr("CheckBalance", true).toString(); // get balance in RM form
        String saved = Cart_py.callAttr("CheckSavesAmount").toString(); // get saved amount

        TV_Budget.setText(": " + budget);
        TV_Total.setText(": " + total);
        TV_Balance.setText(": " + balance);

        // Display saved amount
        if (saved.equals("RM0.00")){
            TV_Saved.setText("");
        }
        else{
            TV_Saved.setText("(Saved " + saved + ")");
        }

        // Check either cart is empty or not
        if(barcode.length == 0){
            TV_emptycart.setText("There is nothing inside your cart...");
        }
        else{
            TV_emptycart.setText("");
        }
        ArrayList<Item> userArrayList = new ArrayList<>();

        for(int i = 0; i< barcode.length; i++){
            String Displayed_Price, OfferStr;
            double OriPrice, OfferPrice;
            int  OfferD;
            String Membership = Cart_py.callAttr("CheckMembership").toString();
            if (!start_day[i].equals("Tap to set") && Tools_py.callAttr("PromotionValidation", start_day[i], end_day[i]).toBoolean()) {
                // Both offers available
                if (!all_dis_price[i].equals(price[i]) && !member_dis_price[i].equals(price[i])) {
                    // Non-Member
                    if (Membership.equals("0")) {
                        OriPrice = Tools_py.callAttr("GetDoubleForm", price[i]).toDouble();
                        OfferPrice = Tools_py.callAttr("GetDoubleForm", all_dis_price[i]).toDouble();
                        OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                        OfferStr = " (" + OfferD + "% off)";
                        Displayed_Price = all_dis_price[i] + OfferStr;
                    }
                    // Member
                    else {
                        if (Tools_py.callAttr("GetDoubleForm", all_dis_price[i]).toDouble() >= Tools_py.callAttr("GetDoubleForm", member_dis_price[i]).toDouble()) {
                            OriPrice = Tools_py.callAttr("GetDoubleForm", price[i]).toDouble();
                            OfferPrice = Tools_py.callAttr("GetDoubleForm", member_dis_price[i]).toDouble();
                            OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                            OfferStr = " (" + OfferD + "% off)";
                            Displayed_Price = member_dis_price[i] + OfferStr;
                        } else {
                            OriPrice = Tools_py.callAttr("GetDoubleForm", price[i]).toDouble();
                            OfferPrice = Tools_py.callAttr("GetDoubleForm", all_dis_price[i]).toDouble();
                            OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                            OfferStr = " (" + OfferD + "% off)";
                            Displayed_Price = all_dis_price[i] + OfferStr;
                        }
                    }
                }
                // Only Member Price
                else if (!member_dis_price[i].equals(price[i]) && Membership.equals("1")) {
                    OriPrice = Tools_py.callAttr("GetDoubleForm", price[i]).toDouble();
                    OfferPrice = Tools_py.callAttr("GetDoubleForm", member_dis_price[i]).toDouble();
                    OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                    OfferStr = " (" + OfferD + "% off)";
                    Displayed_Price = member_dis_price[i] + OfferStr;
                }
                // Only Discounted Price
                else if (!all_dis_price[i].equals(price[i])) {
                    OriPrice = Tools_py.callAttr("GetDoubleForm", price[i]).toDouble();
                    OfferPrice = Tools_py.callAttr("GetDoubleForm", all_dis_price[i]).toDouble();
                    OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                    OfferStr = " (" + OfferD + "% off)";
                    Displayed_Price = all_dis_price[i] + OfferStr;
                }
                // Both offer unavailable
                else {
                    Displayed_Price = price[i];
                }
            }
            else {
                Displayed_Price = price[i];
            }

            Item item = new Item("", image[i], barcode[i], name[i], quantity[i], price[i], all_dis_price[i], member_dis_price[i], Displayed_Price, "0");
            userArrayList.add(item);

        }

        ListAdapter_Cart listAdapterCart = new ListAdapter_Cart(Cart.this,userArrayList);

        binding.listview.setAdapter(listAdapterCart);
        binding.listview.setClickable(true);
        binding.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Pass values to Item Detail 2
                Intent i = new Intent(Cart.this,ItemDetail2.class);
                i.putExtra("name", name[position]);
                i.putExtra("price", price[position]);
                i.putExtra("all_dis_price", all_dis_price[position]);
                i.putExtra("all_limit", all_dis_limit[position]);
                i.putExtra("mem_dis_price", member_dis_price[position]);
                i.putExtra("mem_limit", member_dis_limit[position]);
                i.putExtra("start", start_day[position]);
                i.putExtra("end", end_day[position]);
                i.putExtra("image", image[position]);
                i.putExtra("quantity", quantity[position]);
                i.putExtra("cartID", cartID[position]);

                startActivity(i);
            }
        });

        // Check either spend more than 90% or not
        if (Cart_py.callAttr("SpendLimitAlert").toBoolean()){
            TV_Balance.setTextColor(getColor(R.color.red));
            TV_BalanceTxt.setTextColor(getColor(R.color.red));
        }
        else{
            TV_Balance.setTextColor(getColor(R.color.black));
            TV_BalanceTxt.setTextColor(getColor(R.color.black));
        }

        // Button Home
        Btn_Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (barcode.length != 0) {
                    // Cart is not empty. Ask confirmation first.
                    Dialog dialog = new Dialog(Cart.this);
                    dialog.setContentView(R.layout.question_dialog);
                    dialog.setCancelable(false);

                    Button btn_yes, btn_no;
                    TextView tv_message;
                    tv_message = dialog.findViewById(R.id.tv_message_qd);
                    btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                    btn_no = dialog.findViewById(R.id.btn_no_qd);

                    tv_message.setText("Every things in the cart will removed after back to home page. Do you sure to continue?");
                    btn_yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            startActivity(new Intent(Cart.this, Home.class));
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
                else{
                    //Cart is empty
                    startActivity(new Intent(Cart.this, Home.class));
                }
            }
        });

        // Button Scan
        Btn_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanBarcode();
            }
        });

        // Button Edit Budget (Pen)
        Btn_EditBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Cart.this, SetBudget.class));
            }
        });

        // Button Check Out
        Btn_Checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (barcode.length != 0) {
                    // Cart is not empty. Ask confirmation to checkout
                    Dialog dialog = new Dialog(Cart.this);
                    dialog.setContentView(R.layout.question_dialog);
                    dialog.setCancelable(false);

                    Button btn_yes, btn_no;
                    TextView tv_message;
                    tv_message = dialog.findViewById(R.id.tv_message_qd);
                    btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                    btn_no = dialog.findViewById(R.id.btn_no_qd);

                    tv_message.setText("Do you sure to proceed to checkout?");
                    btn_yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            startActivity(new Intent(Cart.this, Checkout.class));
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
                else{
                    // Cart is empty. Cannot checkout
                    messageDialog.Show("error", "Your cart is still empty!");
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

        // Button Menu (Triple Dot)
        Btn_Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(Cart.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.customer, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.Promotion:
                                //Add Action
                                promotionDialog.Show();
                                return true;
                            //case R.id.history:
                                // Add Action
                                //return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        Intent intent = this.getIntent();

        String result = intent.getStringExtra("result");
        if (result != null){
            resultManagement(result);
        }
    }

    private void ScanBarcode(){
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(BarcodeScanner_Cart.class);
        barLaucher.launch(options);
    }

    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null){
            resultManagement(result.getContents());
        }
    });

    private void resultManagement(String result){
        Python py = Python.getInstance();
        final PyObject Store_py = py.getModule("Store");
        MessageDialog messageDialog = new MessageDialog(Cart.this);
        PyObject obj = Store_py.callAttr("CheckBarcode", result);
        String Barcode = obj.toString();

        if(Barcode.equals("Not Found")) {
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
            Intent i = new Intent(Cart.this, ItemDetail.class);
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}