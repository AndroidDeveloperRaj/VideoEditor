package com.bs.videoeditor.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.bs.videoeditor.R;
import com.bs.videoeditor.utils.PixelUtil;
import com.bs.videoeditor.utils.VideoUtil;

import java.util.ArrayList;


public class VideoTimelineView extends View {

    private static final Object sync = new Object();
    Drawable lineLeft, lineRight;
    boolean isMoving;
    Rect rect = new Rect();
    private long videoLength = 0;
    private float progressLeft = 0;
    private float progressRight = 1;
    private Paint paint;
    private Paint paint2;
    private boolean pressedLeft = false;
    private boolean pressedRight = false;
    private float pressDx = 0;
    private MediaMetadataRetriever mediaMetadataRetriever = null;
    private VideoTimelineViewDelegate delegate = null;
    private ArrayList<Bitmap> frames = new ArrayList<>();
    private AsyncTask<Integer, Integer, Bitmap> currentTask = null;
    private long frameTimeOffset = 0;
    //    private Drawable pickDrawable = null;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private int framesToLoad = 0;
    private int textHeight, rectYTop, rectYBottom;
    private Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);

    public VideoTimelineView(Context context) {
        super(context);
        init(context);
    }

    public VideoTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public long getVideoLength() {
        return videoLength;
    }

    private Context context;

    private void init(Context context) {
        this.context = context;
        lineLeft = getResources().getDrawable(R.drawable.a);
        lineRight = getResources().getDrawable(R.drawable.b);

        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.colorAccent));
        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setColor(Color.WHITE);
//        pickDrawable = getResources().getDrawable(R.drawable.videotrimmer);

        timePaint.setColor(Color.WHITE);
        timePaint.setAntiAlias(true);
        timePaint.setTypeface(Typeface.DEFAULT);
        timePaint.setTextAlign(Paint.Align.CENTER);
        int textSize = getResources().getDimensionPixelSize(R.dimen._10ssp);
        timePaint.setTextSize(textSize);

        textHeight = textSize + PixelUtil.dpToPx(getContext(), 10);

        rectYTop = textHeight;
        rectYBottom = rectYTop + getResources().getDimensionPixelOffset(R.dimen._45sdp);
    }

    public float getLeftProgress() {
        return progressLeft;
    }

    public float getRightProgress() {
        return progressRight;
    }

    public void resetProgress() {
        this.progressLeft = 0;
        this.progressRight = 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = rectYBottom + getResources().getDimensionPixelSize(R.dimen._10ssp);
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(getMeasuredWidth(), height);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        if (event == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();

        int width = getMeasuredWidth() - PixelUtil.dpToPx(getContext(), 32);
        int startX = (int) (width * progressLeft) + PixelUtil.dpToPx(getContext(), 16);
//        int startX = (int) (width * progressLeft);

        int endX = (int) (width * progressRight) + PixelUtil.dpToPx(getContext(), 16);
//        int endX = (int) (width * progressRight);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int additionWidth = PixelUtil.dpToPx(getContext(), 20);
            if (startX - additionWidth <= x && x <= startX + additionWidth / 4 && y >= 0 && y <= getMeasuredHeight()) {
                pressedLeft = true;
                pressDx = (int) (x - startX);
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            } else if (endX - additionWidth / 4 <= x && x <= endX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                pressedRight = true;
                pressDx = (int) (x - endX);
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {

            if (pressedLeft) {
                pressedLeft = false;
                return true;
            } else if (pressedRight) {
                pressedRight = false;
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (pressedLeft) {

                startX = (int) (x - pressDx);
                if (startX < PixelUtil.dpToPx(getContext(), 16)) {
                    startX = PixelUtil.dpToPx(getContext(), 16);
                } else if (startX > endX) {
                    startX = endX;
                }
                progressLeft = (float) (startX - PixelUtil.dpToPx(getContext(), 16)) / (float) width;
                if (delegate != null) {
                    delegate.onProgressChanged(true, (long) (progressLeft * videoLength));
                }

                invalidate();
                return true;
            } else if (pressedRight) {

                endX = (int) (x - pressDx);
                if (endX < startX) {
                    endX = startX;
                } else if (endX > width + PixelUtil.dpToPx(getContext(), 16)) {
                    endX = width + PixelUtil.dpToPx(getContext(), 16);
                }
                progressRight = (float) (endX - PixelUtil.dpToPx(getContext(), 16)) / (float) width;
                if (delegate != null) {
                    delegate.onProgressChanged(false, (long) (progressRight * videoLength));
                }
                invalidate();
                return true;
            }
        }
        return false;
    }

    public void setVideoPath(String path) {
        if (mediaMetadataRetriever != null) {
            mediaMetadataRetriever.release();
            mediaMetadataRetriever = null;
        }
        mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(path);
            String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoLength = Long.parseLong(duration);
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }


        resetProgress();
    }

    public void setOnProgressChangeListener(VideoTimelineViewDelegate listener) {
        this.delegate = listener;
    }

    private void reloadFrames(int frameNum) {
        if (mediaMetadataRetriever == null) {
            return;
        }
        if (frameNum == 0) {
            frameHeight = getResources().getDimensionPixelOffset(R.dimen._45sdp);
            framesToLoad = (getMeasuredWidth() - PixelUtil.dpToPx(getContext(), 16)) / frameHeight;
            frameWidth = (int) Math.ceil((float) (getMeasuredWidth() - PixelUtil.dpToPx(getContext(), 16)) / (float) framesToLoad);
            frameTimeOffset = videoLength / framesToLoad;
        }
        currentTask = new AsyncTask<Integer, Integer, Bitmap>() {
            private int frameNum = 0;

            @Override
            protected Bitmap doInBackground(Integer... objects) {
                frameNum = objects[0];
                Bitmap bitmap = null;
                if (isCancelled()) {
                    return null;
                }
                try {
                    bitmap = mediaMetadataRetriever.getFrameAtTime(frameTimeOffset * frameNum * 1000, MediaMetadataRetriever.OPTION_NEXT_SYNC);
                    if (isCancelled()) {
                        return null;
                    }
                    if (bitmap != null) {
                        Bitmap result = Bitmap.createBitmap(frameWidth, frameHeight, bitmap.getConfig());
                        Canvas canvas = new Canvas(result);
                        float scaleX = (float) frameWidth / (float) bitmap.getWidth();
                        float scaleY = (float) frameHeight / (float) bitmap.getHeight();
                        float scale = scaleX > scaleY ? scaleX : scaleY;
                        int w = (int) (bitmap.getWidth() * scale);
                        int h = (int) (bitmap.getHeight() * scale);
                        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        Rect destRect = new Rect((frameWidth - w) / 2, (frameHeight - h) / 2, w, h);
                        canvas.drawBitmap(bitmap, srcRect, destRect, null);
                        bitmap.recycle();
                        bitmap = result;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (!isCancelled()) {
                    frames.add(bitmap);
                    invalidate();
                    if (frameNum < framesToLoad) {
                        reloadFrames(frameNum + 1);
                    }
                }
            }
        };
        currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, frameNum, null, null);
    }

    public void destroy() {
        synchronized (sync) {
            try {
                if (mediaMetadataRetriever != null) {
                    mediaMetadataRetriever.release();
                    mediaMetadataRetriever = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Bitmap bitmap : frames) {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        frames.clear();
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
    }

    public void clearFrames() {
        for (Bitmap bitmap : frames) {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        frames.clear();
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth() - PixelUtil.dpToPx(getContext(), 46);

        Log.d("ccccccc", " " + width);
        int startX = (int) (width * progressLeft) + PixelUtil.dpToPx(getContext(), 16);
        int endX = (int) (width * progressRight) + PixelUtil.dpToPx(getContext(), 16);

        canvas.save();
        canvas.clipRect(PixelUtil.dpToPx(getContext(), 16), rectYTop, width + PixelUtil.dpToPx(getContext(), 20), rectYBottom + PixelUtil.dpToPx(getContext(), 2));
        if (frames.isEmpty() && currentTask == null) {
            reloadFrames(0);
        } else {
            int offset = 0;
            for (Bitmap bitmap : frames) {
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, PixelUtil.dpToPx(getContext(), 16) + offset * frameWidth, rectYTop, null);
                }
                offset++;
            }
        }

        //canvas.drawRect(PixelUtil.dpToPx(getContext(), 16), PixelUtil.dpToPx(getContext(), 2), startX, PixelUtil.dpToPx(getContext(), 42), paint2);
        //canvas.drawRect(endX + PixelUtil.dpToPx(getContext(), 4), PixelUtil.dpToPx(getContext(), 2), PixelUtil.dpToPx(getContext(), 16) + width + PixelUtil.dpToPx(getContext(), 4), PixelUtil.dpToPx(getContext(), 42), paint2);

        canvas.drawRect(startX, rectYTop, startX + PixelUtil.dpToPx(getContext(), 5), rectYBottom + PixelUtil.dpToPx(getContext(), 2), paint);
//        lineLeft.setBounds(startX, rectYTop, startX + PixelUtil.dpToPx(getContext(), 30), rectYBottom + PixelUtil.dpToPx(getContext(), 2));
//        lineLeft.draw(canvas);

        canvas.drawRect(endX - PixelUtil.dpToPx(getContext(), 1), rectYTop, endX + PixelUtil.dpToPx(getContext(), 4), rectYBottom + PixelUtil.dpToPx(getContext(), 2), paint);
        canvas.drawRect(startX + PixelUtil.dpToPx(getContext(), 2), rectYTop, endX + PixelUtil.dpToPx(getContext(), 4),
                rectYTop + PixelUtil.dpToPx(getContext(), 2), paint);
        canvas.drawRect(startX + PixelUtil.dpToPx(getContext(), 2),
                rectYBottom, endX + PixelUtil.dpToPx(getContext(), 4), rectYBottom + PixelUtil.dpToPx(getContext(), 2), paint);


//        canvas.rotate(10, rect.centerX() - rect.width()/2, rect.centerY() - rect.height()/2);
//        canvas.drawRect(rect, paint2);

        canvas.restore();

        lineLeft.setBounds(
//                startX - PixelUtil.dpToPx(getContext(), 19),
                startX - PixelUtil.dpToPx(getContext(), 9),
//                rectYTop + (rectYBottom - rectYTop) / 6,
                rectYTop,
//                startX + PixelUtil.dpToPx(getContext(), 13),
                startX + PixelUtil.dpToPx(getContext(), 13),
//                rectYTop + 5 * (rectYBottom - rectYTop) / 6 + PixelUtil.dpToPx(getContext(), 2)
                rectYBottom + PixelUtil.dpToPx(getContext(), 2)
        );
        lineLeft.draw(canvas);

        lineRight.setBounds(
//                endX - PixelUtil.dpToPx(getContext(), 16),
                endX - PixelUtil.dpToPx(getContext(), 9),
//                rectYTop + (rectYBottom - rectYTop) / 6,
                rectYTop,
//                endX + PixelUtil.dpToPx(getContext(), 17),
                endX + PixelUtil.dpToPx(getContext(), 13),
//                rectYTop + 5 * (rectYBottom - rectYTop) / 6 + PixelUtil.dpToPx(getContext(), 2)
                rectYBottom + PixelUtil.dpToPx(getContext(), 2)
        );
        lineRight.draw(canvas);
//        int drawableWidth = pickDrawable.getIntrinsicWidth();
//        int drawableHeight = pickDrawable.getIntrinsicHeight();
//        pickDrawable.setBounds(startX - drawableWidth / 2, getMeasuredHeight() - drawableHeight,
//                startX + drawableWidth / 2, getMeasuredHeight());
//        pickDrawable.draw(canvas);
//
//        pickDrawable.setBounds(endX - drawableWidth / 2 + PixelUtil.dpToPx(getContext(), 4),
//                getMeasuredHeight() - drawableHeight, endX + drawableWidth / 2 + PixelUtil.dpToPx(getContext(), 4), getMeasuredHeight());
//        pickDrawable.draw(canvas);

        //draw time
        Log.d("xxxxxxx", " " + PixelUtil.dpToPx(getContext(), 40) + "__" + width + PixelUtil.dpToPx(getContext(), 20));

        canvas.drawText(VideoUtil.normalizeTime((long) (progressLeft * videoLength)), PixelUtil.dpToPx(getContext(), 30), textHeight - timePaint.getTextSize() / 2, timePaint);
        canvas.drawText(VideoUtil.normalizeTime((long) (progressRight * videoLength)), width + PixelUtil.dpToPx(getContext(), 20), textHeight - timePaint.getTextSize() / 2, timePaint);
        canvas.drawText(VideoUtil.normalizeTime((long) ((progressRight - progressLeft) * videoLength)), getWidth() / 2, textHeight - timePaint.getTextSize() / 2, timePaint);

    }

    public interface VideoTimelineViewDelegate {
        void onProgressChanged(boolean leftChange, long currentMili);
    }
}