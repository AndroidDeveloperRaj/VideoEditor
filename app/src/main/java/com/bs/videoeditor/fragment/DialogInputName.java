package com.bs.videoeditor.fragment;


import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bs.videoeditor.R;
import com.bs.videoeditor.listener.IInputNameFile;
import com.bs.videoeditor.utils.Utils;

import static com.bs.videoeditor.utils.Utils.isStringHasCharacterSpecial;

/**
 * Created by Hung on 11/16/2018.
 */

public class DialogInputName extends AbsDialog {
    private IInputNameFile mCallback;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private int resource;
    private Context context;
    private EditText edtNameFile;
    private String nameDefault = null;
    private String title = null;
    private TextView tvTitle;

    public DialogInputName(Context context, IInputNameFile mCallback, String nameDefault, String title) {
        super(context);
        this.context = context;
        this.mCallback = mCallback;
        this.nameDefault = nameDefault;
        this.title = title;
    }

    @Override
    public int initLayout() {
        return R.layout.dialog_save_file;
    }

    public void initDialog() {
        super.initDialog();
        tvTitle = getView().findViewById(R.id.ss);
        tvTitle.setText(title);
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
            mCallback.onFileNameEmpty();
            return;
        }

        if (isStringHasCharacterSpecial(nameFile)) {
            mCallback.onFileNameHasSpecialCharacter();
            return;
        }

        mCallback.onApplySelect(edtNameFile.getText().toString().trim());

    }

    private void cancelInput() {
        mCallback.onCancelSelect();
    }
}
