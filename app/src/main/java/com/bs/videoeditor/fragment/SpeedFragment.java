package com.bs.videoeditor.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bs.videoeditor.R;

/**
 * Created by Hung on 11/15/2018.
 */

public class SpeedFragment extends AbsFragment {
    @Override
    public void initViews() {

    }

    public static SpeedFragment newInstance(Bundle bundle) {
        SpeedFragment fragment = new SpeedFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speed, container, false);
    }
}
