package com.bs.videoeditor.fragment;

import android.content.Context;
import android.view.View;

import com.bs.videoeditor.R;

/**
 * Created by ADMIN on 11/19/2018.
 */

public class DialogDetail extends AbsDialog {
    public DialogDetail(Context context) {
        super(context);
    }

    @Override
    public int initLayout() {
        return R.layout.dialog_detail;
    }

    public void setOnClickBtnOk(View.OnClickListener onClickBtnOk) {
        getView().findViewById(R.id.btn_yes_detail).setOnClickListener(onClickBtnOk);
    }
}
