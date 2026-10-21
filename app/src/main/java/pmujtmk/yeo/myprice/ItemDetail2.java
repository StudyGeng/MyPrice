package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.Dialog;
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

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class ItemDetail2 extends AppCompatActivity {
    ImageView iv_Image;
    TextView tv_storeName, tv_name, tv_quantity, tv_price, tv_promotion1, tv_promotion2, tv_total, tv_balance, tv_balancetxt;
    Button btn_decrease, btn_increase, btn_cart, btn_removeItem, btn_back, btn_menu;
    int Current_Quantity;
    boolean prevOverSpend = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable dark mode
        setContentView(R.layout.activity_item_detail2);

        PromotionDialog promotionDialog = new PromotionDialog(ItemDetail2.this);

        // Declare vibrator function
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Make sure python is ready
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        // Declare python function
        Python py = Python.getInstance();
        final PyObject Cart_py = py.getModule("Cart"); // Link to Cart.py
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py
        final PyObject Tools_py = py.getModule("Tools"); // Link to Tools.py

        // Assign id
        tv_storeName = findViewById(R.id.tv_storename_id2);
        iv_Image = findViewById(R.id.iv_image_id2);
        tv_name = findViewById(R.id.tv_name_id2);
        tv_quantity = findViewById(R.id.tv_quantity_id2);
        btn_decrease = findViewById(R.id.btn_minus_id2);
        btn_increase = findViewById(R.id.btn_plus_id2);
        tv_price = findViewById(R.id.tv_price_id2);
        tv_promotion1 = findViewById(R.id.tv_promotion1_id2);
        tv_promotion2 = findViewById(R.id.tv_promotion2_id2);
        tv_total = findViewById(R.id.tv_total_id2);
        tv_balance = findViewById(R.id.tv_balance_id2);
        tv_balancetxt = findViewById(R.id.tv_balancetxt_id2);
        btn_cart = findViewById(R.id.btn_cart_id2);
        btn_removeItem = findViewById(R.id.btn_remove_id2);
        btn_back = findViewById(R.id.btn_back_id2);
        btn_menu = findViewById(R.id.btn_menu_id2);

        Intent intent = this.getIntent();
        // Get values from previous page
        String image = intent.getStringExtra("image");
        String name = intent.getStringExtra("name");
        String quantity = intent.getStringExtra("quantity");
        String price = intent.getStringExtra("price");
        String all_discount = intent.getStringExtra("all_dis_price");
        String all_limit = intent.getStringExtra("all_limit");
        String member_discount = intent.getStringExtra("mem_dis_price");
        String member_limit = intent.getStringExtra("mem_limit");
        String start_day = intent.getStringExtra("start");
        String end_day = intent.getStringExtra("end");
        String cartID = intent.getStringExtra("cartID");

        Current_Quantity = Integer.parseInt(quantity);

        String StoreName = Store_py.callAttr("GetStoreName").toString(); // Get Store name
        String Membership = Cart_py.callAttr("CheckMembership").toString(); // Check Membership
        double itemPrice;

        // Select the item price
        if (Membership.equals("1")){ //Member
            if (!member_discount.equals(price) && !all_discount.equals(price)){  // Both Promotions
                // Cheaper is priority
                if (Double.parseDouble(Tools_py.callAttr("GetDoubleForm", all_discount).toString()) >= Double.parseDouble(Tools_py.callAttr("GetDoubleForm", member_discount).toString())) { // Member cheaper
                    itemPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", member_discount).toString());
                }
                else { // All Discount cheaper
                    itemPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", all_discount).toString());
                }
            }
            else if (!member_discount.equals(price)){
                itemPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", member_discount).toString());
            }

            else if (!all_discount.equals(price)){ // All Discount Price
                itemPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", all_discount).toString());
            }
            else{ // No Promotion
                itemPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", price).toString());
            }
        }
        else{ //Non-Member
            if (!all_discount.equals(price)){ // Discount Price
                itemPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", all_discount).toString());
            }
            else{ // No Promotion
                itemPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", price).toString());
            }
        }

        Double TotalPrice = itemPrice * Current_Quantity;
        String TotalPriceSTR = Tools_py.callAttr("GetRMForm", TotalPrice).toString();
        String Balance = Cart_py.callAttr("CheckBalance", true).toString(); // Get balance in RM form

        tv_name.setText(name);
        tv_quantity.setText(quantity);
        tv_price.setText(price);
        tv_storeName.setText(StoreName);
        tv_total.setText(": " + TotalPriceSTR);
        tv_balance.setText(": " + Balance);

        // Check either already spend more 90% or not
        if (Cart_py.callAttr("SpendLimitAlert").toBoolean()){
            // Over limit
            tv_balance.setTextColor(getColor(R.color.red));
            tv_balancetxt.setTextColor(getColor(R.color.red));
        }
        else{
            // Within limit
            tv_balance.setTextColor(getColor(R.color.black));
            tv_balancetxt.setTextColor(getColor(R.color.black));
        }

        // Display promotion
        if (!start_day.equals("Tap to set") && Tools_py.callAttr("PromotionValidation", start_day, end_day).toBoolean()){
            if (!all_discount.equals(price) && !member_discount.equals(price)){ // Both available
                if (!all_limit.equals("0")){
                    tv_promotion1.setText("For All: " + all_discount + " (Limit: " + all_limit + ")");
                }
                else {
                    tv_promotion1.setText("For All: " + all_discount);
                }
                if (!member_limit.equals("0")){
                    tv_promotion2.setText("Member Price: " + member_discount + " (Limit: " + member_limit + ")");
                }
                else {
                    tv_promotion2.setText("Member Price: " + member_discount);
                }
            }
            else if (!all_discount.equals(price)){ // All Discount
                if (!all_limit.equals("0")){
                    tv_promotion1.setText("For All: " + all_discount + " (Limit: " + all_limit + ")");
                }
                else {
                    tv_promotion1.setText("For All: " + all_discount);
                }
                tv_promotion2.setText("");
            }
            else { //Member Discount
                if (!member_limit.equals("0")){
                    tv_promotion1.setText("Member Price: " + member_discount + " (Limit: " + member_limit + ")");
                }
                else {
                    tv_promotion1.setText("Member Price: " + member_discount);
                }
                tv_promotion2.setText("");
            }
        }
        else {
            tv_promotion1.setText("Sorry, currently this item don't");
            tv_promotion2.setText("have any promotions.");
        }

        // Display Image
        if (!image.equals("Not Available")){
            byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            iv_Image.setImageBitmap(bitmap);
        }
        else{
            iv_Image.setImageResource(R.drawable.imgnotfound); // Default image
        }

        // Button Increase Quantity
        btn_increase.setOnClickListener(new View.OnClickListener() {
            MessageDialog messageDialog = new MessageDialog(ItemDetail2.this);
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onClick(View view) {
                Double balance = Double.parseDouble(Cart_py.callAttr("CheckBalance", false).toString()); // Get Balance in double form
                Double OriItemPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", price).toString());
                // Check either balance is enough or not
                if (balance >= OriItemPrice) {
                    // Balance still enough
                    if (Cart_py.callAttr("CheckPromotionLimit", cartID).toBoolean()){
                        messageDialog.Show("negative", "Oh no. You already reached the limit of the promotion for this item. The rest quantity of this item will " +
                                "be calculated as normal price.");
                        Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                messageDialog.Dismiss();
                                tv_quantity.setText(String.valueOf(Current_Quantity));
                                Cart_py.callAttr("UpdateQuantity", cartID, 1); // Update item quantity

                                Double TotalPrice = itemPrice * Current_Quantity;
                                String TotalPriceSTR = Tools_py.callAttr("GetRMForm", TotalPrice).toString();
                                Cart_py.callAttr("ResetAndManageCart");
                                String Balance = Cart_py.callAttr("CheckBalance", true).toString();

                                tv_total.setText(": " + TotalPriceSTR);
                                tv_balance.setText(": " + Balance);

                                // Check either already spend more than 90% or not
                                if (Cart_py.callAttr("SpendLimitAlert").toBoolean()){
                                    // Over limit
                                    if (!prevOverSpend){
                                        // First time
                                        tv_balance.setTextColor(getColor(R.color.red));
                                        tv_balancetxt.setTextColor(getColor(R.color.red));
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
                                        prevOverSpend = true;
                                    }
                                }
                                else{
                                    // Within limit
                                    tv_balance.setTextColor(getColor(R.color.black));
                                    tv_balancetxt.setTextColor(getColor(R.color.black));
                                    prevOverSpend = false;
                                }
                            }
                        });
                    }
                    else{
                        Current_Quantity = Current_Quantity + 1;
                        tv_quantity.setText(String.valueOf(Current_Quantity));
                        Cart_py.callAttr("UpdateQuantity", cartID, 1); // Update item quantity

                        Double TotalPrice = itemPrice * Current_Quantity;
                        String TotalPriceSTR = Tools_py.callAttr("GetRMForm", TotalPrice).toString();
                        Cart_py.callAttr("ResetAndManageCart");
                        String Balance = Cart_py.callAttr("CheckBalance", true).toString();

                        tv_total.setText(": " + TotalPriceSTR);
                        tv_balance.setText(": " + Balance);

                        // Check either already spend more than 90% or not
                        if (Cart_py.callAttr("SpendLimitAlert").toBoolean()){
                            // Over limit
                            if (!prevOverSpend){
                                // First time
                                tv_balance.setTextColor(getColor(R.color.red));
                                tv_balancetxt.setTextColor(getColor(R.color.red));
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
                                prevOverSpend = true;
                            }
                        }
                        else{
                            // Within limit
                            tv_balance.setTextColor(getColor(R.color.black));
                            tv_balancetxt.setTextColor(getColor(R.color.black));
                            prevOverSpend = false;
                        }
                    }
                }
                else {
                    // Budget not enough
                    messageDialog.Show("negative", "Sorry, you don't have not enough budget");
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

        // Button Decrease Quantity
        btn_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check either it is last item or not
                if (Current_Quantity > 1){
                    // Not the last
                    if (!Cart_py.callAttr("CheckItemOverLimit", cartID).toBoolean()){
                        Current_Quantity = Current_Quantity - 1;
                    }
                    tv_quantity.setText(String.valueOf(Current_Quantity));
                    Cart_py.callAttr("UpdateQuantity", cartID, -1); // Update item quantity

                    Double TotalPrice = itemPrice * Current_Quantity;
                    String TotalPriceSTR = Tools_py.callAttr("GetRMForm", TotalPrice).toString();
                    Cart_py.callAttr("ResetAndManageCart");
                    String Balance = Cart_py.callAttr("CheckBalance", true).toString(); // Get balance in RM form

                    tv_total.setText(": " + TotalPriceSTR);
                    tv_balance.setText(": " + Balance);

                    // Check either spend over 90% or not
                    if (!Cart_py.callAttr("SpendLimitAlert").toBoolean()){
                        // Already within limit - Set text color back to black
                        tv_balance.setTextColor(getColor(R.color.black));
                        tv_balancetxt.setTextColor(getColor(R.color.black));
                    }
                }
                else{
                    // Last item - Ask either want to remove from cart or not
                    Dialog dialog = new Dialog(ItemDetail2.this);
                    dialog.setContentView(R.layout.question_dialog);
                    dialog.setCancelable(false);

                    Button btn_yes, btn_no;
                    TextView tv_message;
                    tv_message = dialog.findViewById(R.id.tv_message_qd);
                    btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                    btn_no = dialog.findViewById(R.id.btn_no_qd);

                    tv_message.setText("Do you want to remove " + name + " from the cart?");
                    btn_yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            Cart_py.callAttr("RemoveItemFromCart", cartID);
                            startActivity(new Intent(ItemDetail2.this, Cart.class));
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
            }
        });

        // Button Remove
        btn_removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(ItemDetail2.this);
                dialog.setContentView(R.layout.question_dialog);
                dialog.setCancelable(false);

                Button btn_yes, btn_no;
                TextView tv_message;
                tv_message = dialog.findViewById(R.id.tv_message_qd);
                btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                btn_no = dialog.findViewById(R.id.btn_no_qd);

                tv_message.setText("Do you want to remove " + name + " from the cart?");
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        Cart_py.callAttr("RemoveItemFromCart", cartID);
                        startActivity(new Intent(ItemDetail2.this, Cart.class));
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

        // Button Cart
        btn_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ItemDetail2.this, Cart.class));
            }
        });

        // Button Back (Arrow)
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ItemDetail2.this, Cart.class));
            }
        });

        // Button Menu (Triple Dot)
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(ItemDetail2.this, view);
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
    }
    // Back Function
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        startActivity(new Intent(ItemDetail2.this, Cart.class)); // Go to cart
    }
}
