package pmujtmk.yeo.myprice;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageDialog {
    Context context;
    Dialog dialog;

    MessageDialog(Context context){
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    void Show(String type, String message){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.message_dialog);
        dialog.setCancelable(false);

        ImageView image = dialog.findViewById(R.id.iv_image_msg);
        TextView tv_title = dialog.findViewById(R.id.tv_title_msg);
        TextView tv_message = dialog.findViewById(R.id.tv_message_msg);

        if (type.equals("positive")){
            image.setImageResource(R.drawable.correct);
            tv_title.setText("Everything Is Good");
        }
        else if (type.equals("negative")){
            image.setImageResource(R.drawable.wrong);
            tv_title.setText("Bad News For You");
        }
        else{
            image.setImageResource(R.drawable.wrong);
            tv_title.setText("Something Went Wrong");
        }
        tv_message.setText(message);

        dialog.create();
        dialog.show();
    }

    void Dismiss(){
        dialog.dismiss();
    }
}
