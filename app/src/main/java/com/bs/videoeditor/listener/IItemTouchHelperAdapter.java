package com.bs.videoeditor.listener;


public interface IItemTouchHelperAdapter {
    void onItemDismiss(int position);
    boolean onItemMove(int fromPosition, int toPosition);
}
