package com.bs.videoeditor.fragment;


import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bs.videoeditor.R;
import com.bs.videoeditor.listener.IInputNameFile;
import com.bs.videoeditor.utils.Utils;

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


    public DialogInputName(Context context, IInputNameFile callback, String nameDefault) {
        super(context);
        this.context = context;
        this.callback = callback;
        this.nameDefault = nameDefault;
    }

    @Override
    public int initLayout() {
        return R.layout.dialog_save_file;
    }

    public void initDialog() {
        super.initDialog();
        edtNameFile = getView().findViewById(R.id.edt_name_file);
        edtNameFile.setText(nameDefault);
        edtNameFile.setSelection(edtNameFile.getText().length());
        getView().findViewById(R.id.btn_local_ok).setOnClickListener(v -> applyInput());
        getView().findViewById(R.id.btn_local_cancel).setOnClickListener(v -> cancelInput());
    }

    private void applyInput() {

        hideDialog();

        String nameFile = edtNameFile.getText().toString().trim();

        if (nameFile.isEmpty()) {
            callback.onFileNameEmpty();
            return;
        }

        if (Utils.isStringHasCharacterSpecial(nameFile)) {
            callback.onFileNameHasSpecialCharacter();
            return;
        }

        callback.onApplySelect(edtNameFile.getText().toString().trim());

    }

    private void cancelInput() {
        callback.onCancelSelect();
    }


}
