package com.bs.videoeditor.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class VideoModel implements Parcelable, Comparable<VideoModel> {

    public static final Creator<VideoModel> CREATOR = new Creator<VideoModel>() {
        @Override
        public VideoModel createFromParcel(Parcel in) {
            return new VideoModel(in);
        }

        @Override
        public VideoModel[] newArray(int size) {
            return new VideoModel[size];
        }
    };

    private String id;
    private String nameAudio;
    private String nameArtist;
    private String nameAlbum;
    private String duration;
    private String path;
    private String relution;
    private long size;
    private boolean isCheck;


    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public VideoModel(String id, String nameAudio, String nameArtist, String nameAbum, String duration, String path, String relution, long size) {
        this.id = id;
        this.nameAudio = nameAudio;
        this.nameArtist = nameArtist;
        this.nameAlbum = nameAbum;
        this.duration = duration;
        this.path = path;
        this.relution = relution;
        this.size = size;
    }

    protected VideoModel(Parcel in) {
        id = in.readString();
        nameAudio = in.readString();
        nameArtist = in.readString();
        nameAlbum = in.readString();
        duration = in.readString();
        path = in.readString();
        relution = in.readString();
        size = in.readLong();
        isCheck = in.readByte() != 0;
    }


    public String getRelution() {
        return relution;
    }

    public void setRelution(String relution) {
        this.relution = relution;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameAudio() {
        return nameAudio;
    }

    public void setNameAudio(String nameAudio) {
        this.nameAudio = nameAudio;
    }

    public String getNameArtist() {
        return nameArtist;
    }

    public void setNameArtist(String nameArtist) {
        this.nameArtist = nameArtist;
    }

    public String getNameAlbum() {
        return nameAlbum;
    }

    public void setNameAlbum(String nameAbum) {
        this.nameAlbum = nameAbum;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nameAudio);
        dest.writeString(nameArtist);
        dest.writeString(nameAlbum);
        dest.writeString(duration);
        dest.writeString(path);
        dest.writeString(relution);
        dest.writeLong(size);
        dest.writeByte((byte) (isCheck ? 1 : 0));
    }

    @Override
    public int compareTo(@NonNull VideoModel videoModel) {
        return this.getNameAudio().compareTo(videoModel.getNameAudio());
    }
}
