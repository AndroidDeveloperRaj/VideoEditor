package com.bs.videoeditor.video;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;


public class VideoSection {
    private Header header;
    private List<VideoTile> tiles = new ArrayList<>();

    public VideoSection() {

    }

    public List<VideoTile> getTiles() {
        return tiles;
    }

    public VideoTile[] getTile() {
        VideoTile[] array = new VideoTile[tiles.size()];
        return tiles.toArray(array);
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public static abstract class VideoBase {
        public int headerPosition;
    }

    public static class VideoTile extends VideoBase {
        public String category;
        public float speed;
        private int id;
        private int duration;
        private long dateCreated;
        private String name;
        private Bitmap thumbnail;
        private String path;
        private boolean isChecked = false;
        public VideoTile(int duration, long dateCreated, String name, Bitmap thumbnail, String path, int id) {
            this.duration = duration;
            this.dateCreated = dateCreated;
            this.name = name;
            this.thumbnail = thumbnail;
            this.path = path;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public long getDateCreated() {
            return dateCreated;
        }

        public void setDateCreated(long dateCreated) {
            this.dateCreated = dateCreated;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setIsChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Bitmap getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(Bitmap thumbnail) {
            this.thumbnail = thumbnail;
        }
    }

    public static class Header extends VideoBase {
        private String title;

        public Header(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}
