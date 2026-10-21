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

public class ListAdapter_History extends ArrayAdapter<History> {

    public ListAdapter_History(Context context, ArrayList<History> userArrayList){

        super(context,R.layout.history_list,userArrayList);

    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        History history = getItem(position);

        if (convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_list,parent,false);

        }

        TextView storename = convertView.findViewById(R.id.tv_storename_historylist);
        TextView spend = convertView.findViewById(R.id.tv_spend_historylist);
        TextView date = convertView.findViewById(R.id.tv_date_historylist);

        storename.setText(history.StoreName);
        spend.setText("Spend: " + history.TotalSpend);
        date.setText(history.Date);

        return convertView;
    }
}
