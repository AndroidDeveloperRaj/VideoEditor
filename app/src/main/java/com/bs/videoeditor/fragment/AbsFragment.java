package com.bs.videoeditor.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bs.videoeditor.R;


public abstract class AbsFragment extends Fragment {
    private Context mContext = null;
    private Toolbar toolbar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(view -> getFragmentManager().popBackStack());
        toolbar.inflateMenu(R.menu.menu_save);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }


    public View findViewById(@IdRes int id) {
        return getView().findViewById(id);
    }

    public abstract void initViews();


}
