package pmujtmk.yeo.myprice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoadingDialog {
    Context context;
    Dialog dialog;

    LoadingDialog(Context context){
        this.context = context;
    }

    void Start(String message){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.setCancelable(false);

        TextView tv_message = dialog.findViewById(R.id.tv_loadingtext_loading);
        tv_message.setText(message);
        dialog.create();
        dialog.show();
    }

    void Dismiss(){
        dialog.dismiss();
    }
}
