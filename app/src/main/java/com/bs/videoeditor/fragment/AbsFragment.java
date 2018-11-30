package com.bs.videoeditor.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bs.videoeditor.R;
import com.bs.videoeditor.utils.Utils;


public abstract class AbsFragment extends Fragment {
    private Context mContext = null;
    private Toolbar toolbar = null;
    private boolean isPauseFragment = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onPause() {
        super.onPause();
        isPauseFragment = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isPauseFragment = false;
    }

    public boolean isPauseFragment() {
        return isPauseFragment;
    }

    @Override
    public Context getContext() {
        if (mContext != null) return mContext;
        return super.getContext();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();
        initViews();

    }

    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(view -> onBack());
            toolbar.inflateMenu(R.menu.menu_save);
        }
    }

    private void onBack() {
//        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (imm != null && imm.is()) {
//            Utils.closeKeyboard(getActivity());
//        }else {
        getFragmentManager().popBackStack();
//        }

        Utils.closeKeyboard(getActivity());
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public View findViewById(@IdRes int id) {
        return getView().findViewById(id);
    }

    public abstract void initViews();


}
