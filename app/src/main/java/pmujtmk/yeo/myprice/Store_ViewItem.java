package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class Store_ViewItem extends AppCompatActivity {

    TextView tv_LastUpdate, tv_barcode, tv_name, tv_price, tv_PromotionData, tv_Promotion_1, tv_Promotion_2;
    ImageView iv_image;
    Button btn_update, btn_remove, btn_back;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_store_view_item);
        LoadingDialog loadingDialog = new LoadingDialog(Store_ViewItem.this);
        MessageDialog messageDialog = new MessageDialog(Store_ViewItem.this);

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        final PyObject Tools_py = py.getModule("Tools");

        String StoreID = Database_py.callAttr("GetLoggedIn_ID").toString();

        tv_LastUpdate = findViewById(R.id.tv_lastupdate_storeview);
        tv_barcode = findViewById(R.id.tv_barcode_storeview);
        tv_name = findViewById(R.id.tv_name_storeview);
        tv_price = findViewById(R.id.tv_price_storeview);
        tv_PromotionData = findViewById(R.id.tv_promotiondate_storeview);
        tv_Promotion_1 = findViewById(R.id.tv_promotion1_storeview);
        tv_Promotion_2 = findViewById(R.id.tv_promotion2_storeview);
        iv_image = findViewById(R.id.iv_image_storeview);
        btn_update = findViewById(R.id.btn_update_storeview);
        btn_remove = findViewById(R.id.btn_remove_storeview);
        btn_back = findViewById(R.id.btn_back_storeviewitem);

        Intent intent = this.getIntent();

        String lastupdate = intent.getStringExtra("last_update");
        String ID = intent.getStringExtra("ID");
        String image = intent.getStringExtra("image");
        String barcode = intent.getStringExtra("barcode");
        String name = intent.getStringExtra("name");
        String price = intent.getStringExtra("price");
        String all_discount = intent.getStringExtra("all_dis");
        String all_limit = intent.getStringExtra("all_limit");
        String member_discount = intent.getStringExtra("mem_dis");
        String member_limit = intent.getStringExtra("mem_limit");
        String start_day = intent.getStringExtra("start");
        String end_day = intent.getStringExtra("end");

        tv_LastUpdate.setText(lastupdate);
        tv_barcode.setText(barcode);
        tv_name.setText(name);
        tv_price.setText(Tools_py.callAttr("GetRMForm", price).toString());

        if (!image.equals("Not Available")){
            byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            iv_image.setImageBitmap(bitmap);
        }
        else{
            iv_image.setImageResource(R.drawable.imgnotfound);
        }

        // No Promotion
        if (all_discount.equals(price) && member_discount.equals(price)){
            tv_PromotionData.setText("");
            tv_Promotion_1.setText("No promotions available");
            tv_Promotion_2.setText("");
        }
        else {
            // Have Promotion
            tv_PromotionData.setText(start_day + " - " + end_day);
            if (!all_discount.equals(price) && !member_discount.equals(price)) { // Both promotion available
                String all_discountRM = Tools_py.callAttr("GetRMForm", all_discount).toString();
                int off = (int)Math.round(100-((Double.parseDouble(all_discount)/Double.parseDouble(price))*100));
                String member_discountRM = Tools_py.callAttr("GetRMForm", member_discount).toString();
                int off2 = (int)Math.round(100-((Double.parseDouble(member_discount)/Double.parseDouble(price))*100));

                if (all_limit.equals("0")) {
                    tv_Promotion_1.setText("For All: " + all_discountRM + " | " + off + "%");
                }
                else {
                    tv_Promotion_1.setText("For All: " + all_discountRM + " | " + off + "% | Limit: " + all_limit);
                }
                if (member_limit.equals("0")) {
                    tv_Promotion_2.setText("For Member: " + member_discountRM + " | " + off2 + "%");
                }
                else {
                    tv_Promotion_2.setText("For Member: " + member_discountRM + " | " + off2 + "% | Limit: " + member_limit);
                }
            }
            else if (!member_discount.equals(price)) { // Member discount only
                String member_discountRM = Tools_py.callAttr("GetRMForm", member_discount).toString();
                int off = (int)Math.round(100-(Double.parseDouble(member_discount)/Double.parseDouble(price) *100));
                if (member_limit.equals("0")) {
                    tv_Promotion_1.setText("For Member: " + member_discountRM + " | " + off + "%");
                }
                else {
                    tv_Promotion_1.setText("For Member: " + member_discountRM + " | " + off + "% | Limit: " + member_limit);
                }
                tv_Promotion_2.setText("");
            }
            else { // Discount for all
                String all_discountRM = Tools_py.callAttr("GetRMForm", all_discount).toString();
                int off = (int)Math.round(100-((Double.parseDouble(all_discount)/Double.parseDouble(price))*100));
                if (all_limit.equals("0")) {
                    tv_Promotion_1.setText("For All: " + all_discountRM + " | " + off + "%");
                }
                else {
                    tv_Promotion_1.setText("For All: " + all_discountRM + " | " + off + "% | Limit: " + all_limit);
                }
                tv_Promotion_2.setText("");
            }
        }

        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(Store_ViewItem.this);
                dialog.setContentView(R.layout.question_dialog);
                dialog.setCancelable(false);
                Button btn_yes, btn_no;
                TextView tv_message;
                tv_message = dialog.findViewById(R.id.tv_message_qd);
                btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                btn_no = dialog.findViewById(R.id.btn_no_qd);
                tv_message.setText("Do you want to remove " + name + "?");

                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        String email = Database_py.callAttr("GetLoggedIn_Email").toString();
                        String password = Database_py.callAttr("GetLoggedIn_Password").toString();
                        loadingDialog.Start("Removing Item...");
                        new Thread(){
                            @Override
                            public void run() {
                                int LoginResult;
                                try {
                                    LoginResult = Database_py.callAttr("Login", email, password).toInt();
                                    if (LoginResult == 0) {
                                        Database_py.callAttr("RemoveItem", StoreID, ID);
                                    }
                                }
                                catch (Exception e){
                                    LoginResult = 3;
                                }

                                try{
                                    int finalLoginResult = LoginResult;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadingDialog.Dismiss();
                                            switch (finalLoginResult) {
                                                case 0:
                                                    // Login Success
                                                    Toast.makeText(Store_ViewItem.this, "Item Removed!", Toast.LENGTH_LONG).show();
                                                    Database_py.callAttr("RemoveItem2", ID);
                                                    Intent i = new Intent(Store_ViewItem.this, Store_Home.class);
                                                    i.putExtra("refresh", "false");
                                                    startActivity(i);break;

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
                                                            startActivity(new Intent(Store_ViewItem.this, Login.class));
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
                                                            startActivity(new Intent(Store_ViewItem.this, Login.class));
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
                                }
                                catch (final Exception ex){}
                            }
                        }.start();
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

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Store_ViewItem.this, Store_Update.class);
                i.putExtra("ID", ID);
                i.putExtra("image", image);
                i.putExtra("barcode", barcode);
                i.putExtra("name", name);
                i.putExtra("price", price);
                i.putExtra("all_dis", all_discount);
                i.putExtra("all_limit", all_limit);
                i.putExtra("mem_dis", member_discount);
                i.putExtra("mem_limit", member_limit);
                i.putExtra("start", start_day);
                i.putExtra("end", end_day);
                i.putExtra("last_update", lastupdate);
                startActivity(i);
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Store_ViewItem.this, Store_Home.class);
                i.putExtra("refresh", "false");
                startActivity(i);
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        Intent i = new Intent(Store_ViewItem.this, Store_Home.class);
        i.putExtra("refresh", "false");
        startActivity(i);
    }
}