package pmujtmk.yeo.myprice;

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
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;

import pmujtmk.yeo.myprice.databinding.ActivityCartBinding;
import pmujtmk.yeo.myprice.databinding.ActivityPurchaseDetailsBinding;

public class PurchaseDetails extends AppCompatActivity {

    ActivityPurchaseDetailsBinding binding;
    Button btn_back, btn_menu, btn_menu_symbol;
    TextView tv_storename, tv_membership, tv_date, tv_budget, tv_spend, tv_discount;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable dark mode
        binding = ActivityPurchaseDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        // Declare python function
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database"); // Link to Database.py
        final PyObject Tools_py = py.getModule("Tools"); // Link to Tools.py

        btn_back = findViewById(R.id.btn_back_purchase);
        btn_menu = findViewById(R.id.btn_menu_purchase);
        btn_menu_symbol = findViewById(R.id.btn_menu_itemdetail3);
        tv_storename = findViewById(R.id.tv_storename_purchase);
        tv_membership = findViewById(R.id.tv_membership_purchase);
        tv_date = findViewById(R.id.tv_date_purchase);
        tv_budget = findViewById(R.id.tv_budget_purchase);
        tv_spend = findViewById(R.id.tv_spend_purchase);
        tv_discount = findViewById(R.id.tv_discount_purchase);

        Intent intent = this.getIntent();
        String HistoryID = intent.getStringExtra("ID");
        String Type = intent.getStringExtra("Type");

        String[] History = Database_py.callAttr("GetHistory", HistoryID).toJava(String[].class);
        String[][] HistoryDetails = Database_py.callAttr("GetHistoryDetails", HistoryID).toJava(String[][].class);
        String[] ItemName = HistoryDetails[0];
        String[] Price = HistoryDetails[1];
        String[] All_Discount = HistoryDetails[2];
        String[] Member_Discount = HistoryDetails[3];
        String[] Start = HistoryDetails[4];
        String[] End = HistoryDetails[5];
        String[] Quantity = HistoryDetails[6];
        String[] Image = HistoryDetails[7];

        if (Type.equals("Checkout")){
            btn_menu.setVisibility(View.GONE);
            btn_menu_symbol.setVisibility(View.GONE);
        }

        tv_storename.setText(History[0]);
        tv_date.setText(History[5]);
        tv_budget.setText(": " + History[2]);
        tv_spend.setText(": " +History[3]);
        tv_discount.setText(": " + History[4]);

        if (History[1].equals("1")){
            tv_membership.setText("Shop as Member");
        }
        else{
            tv_membership.setText("Shop as Non-Member");
        }

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(PurchaseDetails.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.purchase, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.remove:
                                //Add Action
                                Dialog dialog = new Dialog(PurchaseDetails.this);
                                dialog.setContentView(R.layout.question_dialog);
                                dialog.setCancelable(false);

                                Button btn_yes, btn_no;
                                TextView tv_message;
                                tv_message = dialog.findViewById(R.id.tv_message_qd);
                                btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                                btn_no = dialog.findViewById(R.id.btn_no_qd);

                                tv_message.setText("Do you sure to remove this history?");
                                btn_yes.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Database_py.callAttr("RemoveHistory", HistoryID);
                                        dialog.dismiss();
                                        Toast.makeText(PurchaseDetails.this, "History Removed", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(PurchaseDetails.this, ViewHistory.class));
                                    }
                                });

                                btn_no.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ArrayList<Item> userArrayList = new ArrayList<>();

        for(int i = 0; i< ItemName.length; i++){
            String Displayed_Price, OfferStr;
            double OriPrice, OfferPrice;
            int  OfferD;
            if (!Start[i].equals("Tap to set") && Tools_py.callAttr("PromotionValidation", Start[i], End[i]).toBoolean()) {
                if (!All_Discount[i].equals(Price[i]) && !Member_Discount[i].equals(Price[i])) {
                    // Non-Member
                    if (History[1].equals("0")) {
                        OriPrice = Tools_py.callAttr("GetDoubleForm", Price[i]).toDouble();
                        OfferPrice = Tools_py.callAttr("GetDoubleForm", All_Discount[i]).toDouble();
                        OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                        OfferStr = " (" + OfferD + "% off)";
                        String DisplayedRM = Tools_py.callAttr("GetRMForm", All_Discount[i]).toString();
                        Displayed_Price = DisplayedRM + OfferStr;
                        System.out.println("1");
                    }
                    // Member
                    else {
                        if (Tools_py.callAttr("GetDoubleForm", All_Discount[i]).toDouble() >= Tools_py.callAttr("GetDoubleForm", Member_Discount[i]).toDouble()) {
                            OriPrice = Tools_py.callAttr("GetDoubleForm", Price[i]).toDouble();
                            OfferPrice = Tools_py.callAttr("GetDoubleForm", Member_Discount[i]).toDouble();
                            OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                            OfferStr = " (" + OfferD + "% off)";
                            String DisplayedRM = Tools_py.callAttr("GetRMForm", Member_Discount[i]).toString();
                            Displayed_Price = DisplayedRM + OfferStr;
                            System.out.println("2");
                        } else {
                            OriPrice = Tools_py.callAttr("GetDoubleForm", Price[i]).toDouble();
                            OfferPrice = Tools_py.callAttr("GetDoubleForm", All_Discount[i]).toDouble();
                            OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                            OfferStr = " (" + OfferD + "% off)";
                            String DisplayedRM = Tools_py.callAttr("GetRMForm", All_Discount[i]).toString();
                            Displayed_Price = DisplayedRM + OfferStr;
                            System.out.println("3");
                        }
                    }
                }
                // Only Member Price
                else if (!Member_Discount[i].equals(Price[i]) && Member_Discount.equals("1")) {
                    OriPrice = Tools_py.callAttr("GetDoubleForm", Price[i]).toDouble();
                    OfferPrice = Tools_py.callAttr("GetDoubleForm", Member_Discount[i]).toDouble();
                    OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                    OfferStr = " (" + OfferD + "% off)";
                    String DisplayedRM = Tools_py.callAttr("GetRMForm", Member_Discount[i]).toString();
                    Displayed_Price = DisplayedRM + OfferStr;
                    System.out.println("4");
                }
                // Only Discounted Price
                else if (!All_Discount[i].equals(Price[i])) {
                    OriPrice = Tools_py.callAttr("GetDoubleForm", Price[i]).toDouble();
                    OfferPrice = Tools_py.callAttr("GetDoubleForm", All_Discount[i]).toDouble();
                    OfferD = (int) Math.round(((OriPrice - OfferPrice) / OriPrice) * 100);
                    OfferStr = " (" + OfferD + "% off)";
                    String DisplayedRM = Tools_py.callAttr("GetRMForm", All_Discount[i]).toString();
                    Displayed_Price = DisplayedRM + OfferStr;
                    System.out.println("5");
                }
                // Both offer unavailable
                else {
                    Displayed_Price = Tools_py.callAttr("GetRMForm", Price[i]).toString();
                    System.out.println("6");
                }
            }
            else{
                Displayed_Price = Tools_py.callAttr("GetRMForm", Price[i]).toString();
                System.out.println("7");
            }

            Item item = new Item("", Image[i], "", ItemName[i], Quantity[i], Price[i], All_Discount[i], Member_Discount[i], Displayed_Price, "");
            userArrayList.add(item);

        }

        ListAdapter_Cart listAdapterCart = new ListAdapter_Cart(PurchaseDetails.this,userArrayList);

        binding.listview.setAdapter(listAdapterCart);
        binding.listview.setClickable(false);
    }
}