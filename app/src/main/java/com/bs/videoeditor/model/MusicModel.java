package com.bs.videoeditor.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicModel implements Parcelable {
    private String id;
    private String title;
    private String filePath;
    private long duration;

    public MusicModel() {
    }

    public MusicModel(String id, String title, String filePath, long duration) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.duration = duration;
    }

    protected MusicModel(Parcel in) {
        id = in.readString();
        title = in.readString();
        filePath = in.readString();
        duration = in.readLong();
    }

    public static final Creator<MusicModel> CREATOR = new Creator<MusicModel>() {
        @Override
        public MusicModel createFromParcel(Parcel in) {
            return new MusicModel(in);
        }

        @Override
        public MusicModel[] newArray(int size) {
            return new MusicModel[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(filePath);
        dest.writeLong(duration);
    }
}
