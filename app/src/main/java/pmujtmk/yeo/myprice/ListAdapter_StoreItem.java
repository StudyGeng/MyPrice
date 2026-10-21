package pmujtmk.yeo.myprice;

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

public class ListAdapter_StoreItem extends ArrayAdapter<Item> {
    public ListAdapter_StoreItem(Context context, ArrayList<Item> userArrayList){

        super(context,R.layout.storeitem_list, userArrayList);

    }
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Item item = getItem(position);

        if (convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.storeitem_list,parent,false);

        }

        ImageView imageView = convertView.findViewById(R.id.iv_image_storeitem);
        TextView barcode = convertView.findViewById(R.id.tv_barcode_storeitem);
        TextView name = convertView.findViewById(R.id.tv_name_storeitem);

        if (!item.imageStr.equals("Not Available")){
            byte[] encodeByte = Base64.decode(item.imageStr, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            imageView.setImageBitmap(bitmap);
        }
        else{
            imageView.setImageResource(R.drawable.imgnotfound);
        }

        barcode.setText(item.barcode);
        name.setText(item.name);

        return convertView;
    }
}
