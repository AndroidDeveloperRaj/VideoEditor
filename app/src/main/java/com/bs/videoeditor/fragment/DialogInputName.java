package com.bs.videoeditor.fragment;


import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.bs.videoeditor.R;
import com.bs.videoeditor.listener.IInputNameFile;

/**
 * Created by Hung on 11/16/2018.
 */

public class DialogInputName extends AbsDialog {
    private IInputNameFile callback;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private int resource;
    private Context context;
    private EditText edtNameFile;
    private String nameDefault = null;


    public DialogInputName(Context context,IInputNameFile callback, String nameDefault) {
        super(context);
        AbsDialog.class.getSuperclass();
        this.context = context;
        this.callback = callback;
        this.nameDefault = nameDefault;
    }

    public void showDialog() {
        if (alertDialog != null) {
            alertDialog.show();
        }
    }

    public void hideDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public int initLayout() {
        return 0;
    }

    public void initDialog() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_save_file, null);
        builder = new AlertDialog.Builder(context);
        builder.setView(view);
        alertDialog = builder.create();
        edtNameFile = view.findViewById(R.id.edt_name_file);
        edtNameFile.setText(nameDefault);
        edtNameFile.setSelection(edtNameFile.getText().length());
        view.findViewById(R.id.btn_local_ok).setOnClickListener(v -> applyInput());
        view.findViewById(R.id.btn_local_cancel).setOnClickListener(v -> cancelInput());
        alertDialog.show();
    }

    private void applyInput() {
        callback.onApplySelect(edtNameFile.getText().toString().trim());
        hideDialog();
    }

    private void cancelInput() {
        callback.onCancelSelect();
        hideDialog();
    }
}
