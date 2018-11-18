package com.bs.videoeditor.listener;


import com.bs.videoeditor.model.VideoModel;

import java.util.List;



public interface IListSongChanged {
    void onNoteListChanged(List<VideoModel> videoModelList);
}
