package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class Checkout extends AppCompatActivity {

    TextView TV_TotalPrice, TV_TotalItem, TV_Spend, TV_Storename;
    Button Btn_Exit, Btn_Detail;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable dark mode
        setContentView(R.layout.activity_checkout);

        // Make sure python is ready
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        // Declare python function
        Python py = Python.getInstance();
        final PyObject Cart_py = py.getModule("Cart"); // Link to Cart.py
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py
        final PyObject Tools_py = py.getModule("Tools"); // Link to Tools.py
        final PyObject Database_py = py.getModule("Database"); // Link to Database.py

        // Assign id
        TV_TotalPrice = findViewById(R.id.tv_totalprice_co);
        TV_TotalItem = findViewById(R.id.tv_totalitem_co);
        TV_Spend = findViewById(R.id.tv_spend_co);
        TV_Storename = findViewById(R.id.tv_storename_co);
        Btn_Exit = findViewById(R.id.btn_home_co);
        Btn_Detail = findViewById(R.id.btn_detail_co);

        PyObject totalPrice = Cart_py.callAttr("CheckSpend", true); // Get total spend in RM form
        PyObject totalItem = Cart_py.callAttr("GetTotalItem"); // Get total Item
        if (totalItem.toInt() <= 0) {
            startActivity(new Intent(Checkout.this, Home.class));
            finish();
            return;
        }
        PyObject budget = Cart_py.callAttr("CheckBudget", false); // Get budget in double form
        PyObject storeName = Store_py.callAttr("GetStoreName"); // Get store name
        PyObject membership = Cart_py.callAttr("CheckMembership");
        PyObject budgetRM = Cart_py.callAttr("CheckBudget", true); // Get budget in RM form
        PyObject TotalSpend = Cart_py.callAttr("CheckSpend", true); // get total spend in RM form
        PyObject TotalDiscount = Cart_py.callAttr("CheckSavesAmount"); // get saved amount

        String HistoryID = Tools_py.callAttr("GenerateHistoryID").toString();
        Database_py.callAttr("SaveHistory", HistoryID, storeName.toString(), membership.toInt(), budgetRM.toString(), TotalSpend.toString(), TotalDiscount.toString());
        Database_py.callAttr("ResetTable"); // Clear all selected table in local database (sqlite3)

        double D_totalPrice = Double.parseDouble(Tools_py.callAttr("GetDoubleForm", totalPrice.toString()).toString());
        double D_budget = Double.parseDouble(budget.toString());
        int percentage = (int)Math.round((D_totalPrice/D_budget)*100);

        TV_TotalPrice.setText("Total Price: " + totalPrice);
        TV_TotalItem.setText("Total Item: "+ totalItem.toString());
        TV_Spend.setText("You spent " + percentage + "% of");
        TV_Storename.setText("in " + storeName.toString() + ".");

        // Button Exit
        Btn_Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Checkout.this, Home.class));
            }
        });

        Btn_Detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Checkout.this, PurchaseDetails.class);
                i.putExtra("ID", HistoryID);
                i.putExtra("Type", "Checkout");
                startActivity(i);
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
