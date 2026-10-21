package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.chip.Chip;

public class SetBudget extends AppCompatActivity {

    TextView TV_StoreName;
    EditText ET_Budget;
    Button Btn_Next;
    Chip C_Member, C_NonMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable dark mode
        setContentView(R.layout.activity_set_budget);

        // Declare the dialog functions
        MessageDialog messageDialog = new MessageDialog(SetBudget.this);
        PromotionDialog promotionDialog = new PromotionDialog(SetBudget.this);

        // Make sure Python is ready
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        // Declare the python function
        Python py = Python.getInstance();
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py
        final PyObject Cart_py = py.getModule("Cart"); // Link to Cart.py

        PyObject obj = Store_py.callAttr("GetStoreName"); // get the store name
        PyObject budget = Cart_py.callAttr("CheckBudget", false);  // Get budget in double form
        PyObject Pre_membership = Cart_py.callAttr("CheckMembership"); // Check membership

        // Assign id
        TV_StoreName = findViewById(R.id.lbl_storename_sb);
        ET_Budget = findViewById(R.id.txt_budget_sb);
        Btn_Next = findViewById(R.id.btn_next_sb);
        C_Member = findViewById(R.id.c_Member_sb);
        C_NonMember = findViewById(R.id.c_NonMember_sb);

        TV_StoreName.setText(obj.toString());

        // Display previous budget if it's exist
        if (!budget.toString().equals("0.00")){
            ET_Budget.setText(budget.toString());
        }
        else{
            if (Store_py.callAttr("CheckPromotion").toBoolean()){
                promotionDialog.Show();
            }
        }
        // Set checked value based on previous data
        if (Integer.parseInt(Pre_membership.toString()) == 0){
            C_NonMember.setChecked(true);
            C_Member.setChecked(false);
        }
        else{
            C_NonMember.setChecked(false);
            C_Member.setChecked(true);
        }

        // Button Next
        Btn_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make sure budget is already entered
                // If there is no budget entered, the button is disabled
                if(!ET_Budget.getText().toString().equals("")) {
                    // Budget is entered
                    double EnteredBudget = Double.parseDouble(ET_Budget.getText().toString());
                    double TotalSpend = Cart_py.callAttr("CheckSpend", false).toDouble(); // Check total spend
                    int Membership;
                    // Check membership
                    // 1: Member
                    // 0: Non-member
                    if (EnteredBudget >= TotalSpend){
                        // Budget is more than total spend
                        if (C_Member.isChecked()){
                            Membership = 1;
                        }
                        else{
                            Membership = 0;
                        }
                        Store_py.callAttr("SetBudget", Double.parseDouble(ET_Budget.getText().toString()), Membership); // Set the budget

                        if (TotalSpend == 0){
                            // Go to First Scan is there is nothing in the cart yet
                            startActivity(new Intent(SetBudget.this, FirstScan.class));
                        }
                        else {
                            // Go to Cart is there already have something in the cart
                            startActivity(new Intent(SetBudget.this, Cart.class));
                        }
                    }
                    else{
                        // Budget is less than total spend
                        messageDialog.Show("negative", "Sorry, inserted budget is less than your current total spend.");
                        Button btn = messageDialog.dialog.findViewById(R.id.btn_close_msg);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                messageDialog.Dismiss();
                            }
                        });

                    }
                }
            }
        });

        //Chip Box Non-Member
        C_NonMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!C_NonMember.isChecked()){
                    // Make sure the clicked chip box is not unchecked
                    C_NonMember.setChecked(true);
                }
            }
        });
        // Chip Box Member
        C_Member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!C_Member.isChecked()){
                    // Make sure the clicked chip box is not unchecked
                    C_Member.setChecked(true);
                }
            }
        });
    }
}