package pmujtmk.yeo.myprice;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PromotionDialog{

    Button btn_back,btn_next,btn_close;
    TextView tv_item, tv_price, tv_date, tv_type1, tv_type2, tv_newprice1, tv_newprice2, tv_limit1, tv_limit2, tv_page, tv_off;
    ImageView img_item;
    Context context;
    Dialog dialog;
    int CurrentPosition = 0;

    PromotionDialog(Context context){
        this.context = context;
    }

    protected void Show() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.promotion_dialog);
        dialog.setCancelable(false);

        Python py = Python.getInstance();
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py
        String[] ItemName = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[0];

        btn_back = dialog.findViewById(R.id.btn_back_promotion);
        btn_next = dialog.findViewById(R.id.btn_next_promotion);
        btn_close = dialog.findViewById(R.id.btn_close_promotion);
        tv_item = dialog.findViewById(R.id.tv_name_promotion);
        tv_price = dialog.findViewById(R.id.tv_price_promotion);
        tv_date = dialog.findViewById(R.id.tv_date_promotion);
        tv_type1 = dialog.findViewById(R.id.tv_type1_promotion);
        tv_type2 = dialog.findViewById(R.id.tv_type2_promotion);
        tv_newprice1 = dialog.findViewById(R.id.tv_newprice1_promotion);
        tv_newprice2 = dialog.findViewById(R.id.tv_newprice2_promotion);
        tv_limit1 = dialog.findViewById(R.id.tv_limit1_promotion);
        tv_limit2 = dialog.findViewById(R.id.tv_limit2_promotion);
        tv_page = dialog.findViewById(R.id.tv_page_promotion);
        tv_off = dialog.findViewById(R.id.tv_off_promotion);
        img_item = dialog.findViewById(R.id.img_item_promotion);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrentPosition += 1;
                if (CurrentPosition > ItemName.length - 1){
                    CurrentPosition = CurrentPosition - ItemName.length;
                }
                DisplayPromotion();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrentPosition -= 1;
                if (CurrentPosition < 0){
                    CurrentPosition = ItemName.length + CurrentPosition;
                }
                DisplayPromotion();
            }
        });

        DisplayPromotion();
        dialog.create();
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    void DisplayPromotion(){
        Python py = Python.getInstance();
        final PyObject Store_py = py.getModule("Store"); // Link to Store.py
        final PyObject Tools_py = py.getModule("Tools"); // Link to Tools.py
        String[] ItemName = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[0];
        String[] ItemPrice = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[1];
        String[] ItemAllDiscount = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[2];
        String[] ItemAllLimit = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[3];
        String[] ItemMemberDiscount = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[4];
        String[] ItemMemberLimit = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[5];
        String[] StartPromotion = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[6];
        String[] EndPromotion = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[7];
        String[] ItemImage = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[8];
        String[] OffPercentage = Store_py.callAttr("GetPromotionList").toJava(String[][].class)[9];

        if (!ItemImage[CurrentPosition].equals("Not Available")){
            // Image available
            byte[] encodeByte = Base64.decode(ItemImage[CurrentPosition], Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            img_item.setImageBitmap(bitmap);
        }
        else{
            // Image not available - Set to default image
            img_item.setImageResource(R.drawable.imgnotfound);
        }

        tv_item.setText(ItemName[CurrentPosition]);
        tv_price.setText(Tools_py.callAttr("GetRMForm", ItemPrice[CurrentPosition]).toString());
        tv_off.setText((int) (Double.parseDouble(OffPercentage[CurrentPosition])) + "%");
        tv_date.setText("Valid until " + EndPromotion[CurrentPosition]);
        tv_page.setText((CurrentPosition+1) + "/" + ItemName.length);

        // Both Promotion Available
        if ((!ItemAllDiscount[CurrentPosition].equals(ItemPrice[CurrentPosition])) && (!ItemMemberDiscount[CurrentPosition].equals(ItemPrice[CurrentPosition]))){
            tv_type1.setText("For All:");
            tv_newprice1.setText(Tools_py.callAttr("GetRMForm", ItemAllDiscount[CurrentPosition]).toString());
            if (!ItemAllLimit[CurrentPosition].equals("0")){
                tv_limit1.setText("(Limit: " + ItemAllLimit[CurrentPosition] + ")");
            }
            else{
                tv_limit1.setText("");
            }

            tv_type2.setText("For Member:");
            tv_newprice2.setText(Tools_py.callAttr("GetRMForm", ItemMemberDiscount[CurrentPosition]).toString());
            if (!ItemMemberLimit[CurrentPosition].equals("0")){
                tv_limit2.setText("(Limit: " + ItemMemberLimit[CurrentPosition] + ")");
            }
            else{
                tv_limit2.setText("");
            }
        }
        // Only All Discount Available
        else if (!ItemAllDiscount[CurrentPosition].equals(ItemPrice[CurrentPosition])){
            tv_type2.setText("");
            tv_newprice2.setText("");
            tv_limit2.setText("");
            tv_type1.setText("For All:");
            tv_newprice1.setText(Tools_py.callAttr("GetRMForm", ItemAllDiscount[CurrentPosition]).toString());
            if (!ItemAllLimit[CurrentPosition].equals("0")){
                tv_limit1.setText("(Limit: " + ItemAllLimit[CurrentPosition] + ")");
            }
            else{
                tv_limit1.setText("");
            }
        }
        // Only Member Discount Available
        else{
            tv_type2.setText("");
            tv_newprice2.setText("");
            tv_limit2.setText("");
            tv_type1.setText("For Member:");
            tv_newprice1.setText(Tools_py.callAttr("GetRMForm", ItemMemberDiscount[CurrentPosition]).toString());
            if (!ItemMemberLimit[CurrentPosition].equals("0")){
                tv_limit1.setText("(Limit: " + ItemMemberLimit[CurrentPosition] + ")");
            }
            else{
                tv_limit1.setText("");
            }
        }
    }
}
