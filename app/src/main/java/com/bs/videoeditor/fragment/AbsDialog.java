package com.bs.videoeditor.fragment;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.bs.videoeditor.R;

/**
 * Created by Hung on 11/19/2018.
 */

public abstract class AbsDialog {
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private Context context;

    public AbsDialog(Context context) {
        this.context = context;
        initDialog();
    }

    public abstract int initLayout();

    public void initDialog() {
        View view = LayoutInflater.from(context).inflate(initLayout(), null);
        builder = new AlertDialog.Builder(context);
        builder.setView(view);
    }

    public void showDialog(){
        alertDialog.show();
    }

    public void hideDialog(){
        alertDialog.dismiss();
    }
}
