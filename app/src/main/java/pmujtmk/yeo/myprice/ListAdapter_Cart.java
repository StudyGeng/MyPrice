package pmujtmk.yeo.myprice; // Manage display list in Cart

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ListAdapter_Cart extends ArrayAdapter<Item> {

    public ListAdapter_Cart(Context context, ArrayList<Item> userArrayList){

        super(context,R.layout.cart_list,userArrayList);

    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Item item = getItem(position);

        if (convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cart_list,parent,false);

        }

        ImageView imageView = convertView.findViewById(R.id.iv_image_cartlist);
        TextView name = convertView.findViewById(R.id.tv_name_cartlist);
        TextView price = convertView.findViewById(R.id.tv_price_cartlist);
        TextView quantity = convertView.findViewById(R.id.tv_quantity_cartlist);

        if (!item.imageStr.equals("Not Available")){
            byte[] encodeByte = Base64.decode(item.imageStr, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            imageView.setImageBitmap(bitmap);
        }
        else{
            imageView.setImageResource(R.drawable.imgnotfound);
        }

        name.setText(item.name);
        price.setText(item.cart_displayed_price);
        quantity.setText("Quantity: " + (item.quantity));

        return convertView;
    }
}
