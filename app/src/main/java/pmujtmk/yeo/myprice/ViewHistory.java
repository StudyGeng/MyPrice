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
import java.util.Vector;

import pmujtmk.yeo.myprice.databinding.ActivityCartBinding;
import pmujtmk.yeo.myprice.databinding.ActivityViewHistoryBinding;

public class ViewHistory extends AppCompatActivity {

    Button btn_menu, btn_back;
    ActivityViewHistoryBinding binding;
    TextView tv_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable dark mode
        binding = ActivityViewHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        // Declare python function
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database"); // Link to Database.py

        btn_menu = findViewById(R.id.btn_menu_history);
        btn_back = findViewById(R.id.btn_back_history);
        tv_empty = findViewById(R.id.tv_emptyhistory_history);

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(ViewHistory.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.history, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.clearall:
                                //Add Action
                                Dialog dialog = new Dialog(ViewHistory.this);
                                dialog.setContentView(R.layout.question_dialog);
                                dialog.setCancelable(false);

                                Button btn_yes, btn_no;
                                TextView tv_message;
                                tv_message = dialog.findViewById(R.id.tv_message_qd);
                                btn_yes = dialog.findViewById(R.id.btn_yes_qd);
                                btn_no = dialog.findViewById(R.id.btn_no_qd);

                                tv_message.setText("Do you sure to clear all the history?");
                                btn_yes.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Database_py.callAttr("ClearHistory");
                                        dialog.dismiss();
                                        Toast.makeText(ViewHistory.this, "All history cleared.", Toast.LENGTH_SHORT).show();
                                        Refresh();
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
                startActivity(new Intent(ViewHistory.this, Home.class));
            }
        });

        Refresh();
    }

    private void Refresh(){
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database"); // Link to Database.py
        String[][] AllHistory = Database_py.callAttr("GetAllHistory").toJava(String[][].class);
        String[] ID = AllHistory[0];
        String[] StoreName = AllHistory[1];
        String[] Membership = AllHistory[2];
        String[] Budget = AllHistory[3];
        String[] TotalSpend = AllHistory[4];
        String[] TotalDiscount = AllHistory[5];
        String[] Date = AllHistory[6];

        ArrayList<History> userArrayList = new ArrayList<>();

        if (ID.length == 0){
            tv_empty.setVisibility(View.VISIBLE);
        }
        else {
            tv_empty.setVisibility(View.GONE);
            for (int i = 0; i < ID.length; i++) {
                History history = new History(ID[i], StoreName[i], Membership[i], Budget[i], TotalSpend[i], TotalDiscount[i], Date[i]);
                userArrayList.add(history);
            }
        }

        ListAdapter_History listAdapter_history = new ListAdapter_History(ViewHistory.this, userArrayList);

        binding.listview.setAdapter(listAdapter_history);
        binding.listview.setClickable(true);
        binding.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ViewHistory.this, PurchaseDetails.class);
                i.putExtra("ID", ID[position]);
                i.putExtra("Type", "History");
                startActivity(i);
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        startActivity(new Intent(ViewHistory.this, Home.class));
    }
}