package pmujtmk.yeo.myprice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import pmujtmk.yeo.myprice.databinding.ActivityStoreHomeBinding;


public class Store_Home extends AppCompatActivity {
    ActivityStoreHomeBinding binding;

    String LoggedInStoreID, LoggedInStoreName;
    TextView tv_storename, tv_emptymessage, tv_total;
    Button btn_account, btn_add;
    SearchView searchView;
    SwipeRefreshLayout swipeRefreshLayout;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityStoreHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");

        LoggedInStoreID = Database_py.callAttr("GetLoggedIn_ID").toString();

        tv_storename = findViewById(R.id.tv_storename_storeitem);
        tv_emptymessage = findViewById(R.id.tv_empty_storeitem);
        tv_total = findViewById(R.id.tv_total_storehome);
        btn_account = findViewById(R.id.btn_account_storeitem);
        btn_add = findViewById(R.id.btn_add_storeitem);
        swipeRefreshLayout = findViewById(R.id.layout_refresh);
        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.listview2);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });

        Intent intent = this.getIntent();

        if (intent.getStringExtra("refresh").equals("true")){
            Refresh();
        }
        else{
            String Data[][] = Database_py.callAttr("GetStoreItemList").toJava(String[][].class);
            Double Data2[][] = Database_py.callAttr("GetStoreItemList2").toJava(Double[][].class);
            Integer Data3[][] = Database_py.callAttr("GetStoreItemList3").toJava(Integer[][].class);

            String ItemUniqueID[] = Data[0];
            String ItemBarcode[] = Data[1];
            String ItemName[] = Data[2];
            Double ItemPrice[] = Data2[0];
            Double ItemAllDiscount[] = Data2[1];
            Integer ItemAllLimit[] = Data3[0];
            Double ItemMemberDiscount[] = Data2[2];
            Integer ItemMemberLimit[] = Data3[1];
            String ItemStartDay[] = Data[3];
            String ItemEndDay[] = Data[4];
            String ItemImage[] = Data[5];
            String ItemLastUpdate[] = Data[6];
            DisplayItemList(ItemUniqueID, ItemBarcode, ItemName, ItemPrice, ItemAllDiscount, ItemAllLimit, ItemMemberDiscount, ItemMemberLimit,
                    ItemStartDay, ItemEndDay, ItemImage, ItemLastUpdate);
        }

        btn_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(Store_Home.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.account, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.my_account:
                                startActivity(new Intent(Store_Home.this, Account.class));
                                return true;

                            case R.id.log_out:
                                Database_py.callAttr("ResetLoginTable");
                                startActivity(new Intent(Store_Home.this, Login.class));
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Store_Home.this, Store_Add.class));
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                Refresh();
            }
        });
    }

    private void filter(String text) {
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");

        String Data[][] = Database_py.callAttr("GetStoreItemList").toJava(String[][].class);
        Double Data2[][] = Database_py.callAttr("GetStoreItemList2").toJava(Double[][].class);
        Integer Data3[][] = Database_py.callAttr("GetStoreItemList3").toJava(Integer[][].class);

        String ItemUniqueID[] = Data[0];
        String ItemBarcode[] = Data[1];
        String ItemName[] = Data[2];
        Double ItemPrice[] = Data2[0];
        Double ItemAllDiscount[] = Data2[1];
        Integer ItemAllLimit[] = Data3[0];
        Double ItemMemberDiscount[] = Data2[2];
        Integer ItemMemberLimit[] = Data3[1];
        String ItemStartDay[] = Data[3];
        String ItemEndDay[] = Data[4];
        String ItemImage[] = Data[5];
        String ItemLastUpdate[] = Data[6];

        String F_ItemUniqueID[] = {};
        String F_ItemBarcode[] = {};
        String F_ItemName[] = {};
        Double F_ItemPrice[] = {};
        Double F_ItemAllDiscount[] = {};
        Integer F_ItemAllLimit[] = {};
        Double F_ItemMemberDiscount[] = {};
        Integer F_ItemMemberLimit[] = {};
        String F_ItemStartDay[] = {};
        String F_ItemEndDay[] = {};
        String F_ItemImage[] = {};
        String F_ItemLastUpdate[] = {};

        for (int i=0; i< ItemName.length; i++){
            if (ItemName[i].toLowerCase().contains(text.toLowerCase())){
                F_ItemUniqueID = Arrays.copyOf(F_ItemUniqueID, F_ItemUniqueID.length+1);
                F_ItemUniqueID[F_ItemUniqueID.length-1] = ItemUniqueID[i];

                F_ItemBarcode = Arrays.copyOf(F_ItemBarcode, F_ItemBarcode.length+1);
                F_ItemBarcode[F_ItemBarcode.length-1] = ItemBarcode[i];

                F_ItemName = Arrays.copyOf(F_ItemName, F_ItemName.length+1);
                F_ItemName[F_ItemName.length-1] = ItemName[i];

                F_ItemPrice = Arrays.copyOf(F_ItemPrice, F_ItemPrice.length+1);
                F_ItemPrice[F_ItemPrice.length-1] = ItemPrice[i];

                F_ItemAllDiscount = Arrays.copyOf(F_ItemAllDiscount, F_ItemAllDiscount.length+1);
                F_ItemAllDiscount[F_ItemAllDiscount.length-1] = ItemAllDiscount[i];

                F_ItemAllLimit = Arrays.copyOf(F_ItemAllLimit, F_ItemAllLimit.length+1);
                F_ItemAllLimit[F_ItemAllLimit.length-1] = ItemAllLimit[i];

                F_ItemMemberDiscount = Arrays.copyOf(F_ItemMemberDiscount, F_ItemMemberDiscount.length+1);
                F_ItemMemberDiscount[F_ItemMemberDiscount.length-1] = ItemMemberDiscount[i];

                F_ItemMemberLimit = Arrays.copyOf(F_ItemMemberLimit, F_ItemMemberLimit.length+1);
                F_ItemMemberLimit[F_ItemMemberLimit.length-1] = ItemMemberLimit[i];

                F_ItemStartDay = Arrays.copyOf(F_ItemStartDay, F_ItemStartDay.length+1);
                F_ItemStartDay[F_ItemStartDay.length-1] = ItemStartDay[i];

                F_ItemEndDay = Arrays.copyOf(F_ItemEndDay, F_ItemEndDay.length+1);
                F_ItemEndDay[F_ItemEndDay.length-1] = ItemEndDay[i];

                F_ItemImage = Arrays.copyOf(F_ItemImage, F_ItemImage.length+1);
                F_ItemImage[F_ItemImage.length-1] = ItemImage[i];

                F_ItemLastUpdate = Arrays.copyOf(F_ItemLastUpdate, F_ItemLastUpdate.length+1);
                F_ItemLastUpdate[F_ItemLastUpdate.length-1] = ItemLastUpdate[i];
            }
        }
        DisplayItemList(F_ItemUniqueID, F_ItemBarcode, F_ItemName, F_ItemPrice, F_ItemAllDiscount, F_ItemAllLimit, F_ItemMemberDiscount, F_ItemMemberLimit,
                F_ItemStartDay, F_ItemEndDay, F_ItemImage, F_ItemLastUpdate);

    }

    @SuppressLint("SetTextI18n")
    private void DisplayItemList(String[] ItemUniqueID, String[] ItemBarcode, String[] ItemName, Double[] ItemPrice,
                                 Double[] ItemAllDiscount, Integer[] ItemAllLimit, Double[] ItemMemberDiscount, Integer[] ItemMemberLimit,
                                 String[] ItemStartDay, String[] ItemEndDay, String[] ItemImage, String[] ItemLastUpdate) {
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");

        LoggedInStoreName = Database_py.callAttr("GetLoggedIn_StoreName").toString();
        tv_storename.setText(LoggedInStoreName);

        if (ItemBarcode.length == 0){
            tv_emptymessage.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
            tv_total.setText("");
        }
        else{
            tv_emptymessage.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            if (ItemBarcode.length == 1) {
                tv_total.setText("Total: " + ItemBarcode.length + " Item");
            }
            else {
                tv_total.setText("Total: " + ItemBarcode.length + " Items");
            }
            ArrayList<Item> userArrayList = new ArrayList<>();

            for (int i=0; i<ItemBarcode.length; i++){
                Item item = new Item(ItemUniqueID[i], ItemImage[i], ItemBarcode[i], ItemName[i], "0", ItemPrice[i].toString(), ItemAllDiscount[i].toString(), ItemMemberDiscount[i].toString(), "0", ItemLastUpdate[i]);
                userArrayList.add(item);
            }
            ListAdapter_StoreItem listAdapterStoreItem = new ListAdapter_StoreItem(Store_Home.this,userArrayList);

            binding.listview2.setAdapter(listAdapterStoreItem);
            binding.listview2.setClickable(true);
            binding.listview2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent i = new Intent(Store_Home.this,Store_ViewItem.class);
                    i.putExtra("ID", ItemUniqueID[position]);
                    i.putExtra("image", ItemImage[position]);
                    i.putExtra("barcode", ItemBarcode[position]);
                    i.putExtra("name", ItemName[position]);
                    i.putExtra("price", ItemPrice[position].toString());
                    i.putExtra("all_dis", ItemAllDiscount[position].toString());
                    i.putExtra("all_limit", ItemAllLimit[position].toString());
                    i.putExtra("mem_dis", ItemMemberDiscount[position].toString());
                    i.putExtra("mem_limit", ItemMemberLimit[position].toString());
                    i.putExtra("start", ItemStartDay[position].toString());
                    i.putExtra("end", ItemEndDay[position].toString());
                    i.putExtra("last_update", ItemLastUpdate[position]);

                    startActivity(i);
                }
            });
        }
    }

    public void Refresh(){
        Python py = Python.getInstance();
        final PyObject Database_py = py.getModule("Database");
        LoadingDialog loadingDialog = new LoadingDialog(Store_Home.this);
        MessageDialog messageDialog = new MessageDialog(Store_Home.this);

        searchView.setIconified(true);

        String UniqueID = Database_py.callAttr("GetLoggedIn_UniqueID").toString();
        String ID = Database_py.callAttr("GetLoggedIn_ID").toString();
        String email = Database_py.callAttr("GetLoggedIn_Email").toString();
        String password = Database_py.callAttr("GetLoggedIn_Password").toString();

        loadingDialog.Start("Updating...");
        new Thread(){
            @Override
            public void run() {
                String[][] Data = null;
                int LoginResult;
                String StoreName = null;
                String Location = null;
                try {
                    LoginResult = Database_py.callAttr("Login", email, password).toInt();
                    if (LoginResult == 0) {
                        StoreName = Database_py.callAttr("GetLoginInformation", email).toJava(String[].class)[2];
                        Location = Database_py.callAttr("GetLoginInformation", email).toJava(String[].class)[3];
                        Data = Database_py.callAttr("DownloadItemList", LoggedInStoreID).toJava(String[][].class);
                    }
                }
                catch (Exception e){
                    LoginResult = 3;
                }
                try {
                    String[][] finalData = Data;
                    int finalLoginResult = LoginResult;
                    String finalStoreName = StoreName;
                    String finalLocation = Location;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (finalLoginResult) {
                                case 0:
                                    // Login Success
                                    Database_py.callAttr("ResetStoreItemTable");
                                    for (int i = 0; i < finalData.length; i++) {
                                        Database_py.callAttr("SaveStoreItemList", finalData[i][0], finalData[i][1], finalData[i][2], finalData[i][3],
                                                finalData[i][4], finalData[i][5], finalData[i][6], finalData[i][7], finalData[i][8], finalData[i][9],
                                                finalData[i][10], finalData[i][11]);
                                    }
                                    Database_py.callAttr("UpdateAccountInformation2", UniqueID, ID, finalStoreName, finalLocation, email, password);
                                    String Data[][] = Database_py.callAttr("GetStoreItemList").toJava(String[][].class);
                                    Double Data2[][] = Database_py.callAttr("GetStoreItemList2").toJava(Double[][].class);
                                    Integer Data3[][] = Database_py.callAttr("GetStoreItemList3").toJava(Integer[][].class);

                                    String ItemUniqueID[] = Data[0];
                                    String ItemBarcode[] = Data[1];
                                    String ItemName[] = Data[2];
                                    Double ItemPrice[] = Data2[0];
                                    Double ItemAllDiscount[] = Data2[1];
                                    Integer ItemAllLimit[] = Data3[0];
                                    Double ItemMemberDiscount[] = Data2[2];
                                    Integer ItemMemberLimit[] = Data3[1];
                                    String ItemStartDay[] = Data[3];
                                    String ItemEndDay[] = Data[4];
                                    String ItemImage[] = Data[5];
                                    String ItemLastUpdate[] = Data[6];

                                    loadingDialog.Dismiss();

                                    DisplayItemList(ItemUniqueID, ItemBarcode, ItemName, ItemPrice, ItemAllDiscount, ItemAllLimit, ItemMemberDiscount, ItemMemberLimit,
                                            ItemStartDay, ItemEndDay, ItemImage, ItemLastUpdate);
                                    break;

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
                                            startActivity(new Intent(Store_Home.this, Login.class));
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
                                            startActivity(new Intent(Store_Home.this, Login.class));
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        startActivity(new Intent(Store_Home.this, Home.class));
    }
}
