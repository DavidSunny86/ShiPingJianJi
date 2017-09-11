package com.cheerchip.shipingjianji;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cheerchip.shipingjianji.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTrimmer;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by noname on 2017/9/8.
 */

public class VideoTrimActivity extends BaseActivity {


    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right)
    TextView right;
    @BindView(R.id.title_layout)
    RelativeLayout titleLayout;
    @BindView(R.id.videoview)
    VideoView videoview;
    @BindView(R.id.linerlayout)
    LinearLayout linerlayout;
    @BindView(R.id.starttime)
    TextView starttime;
    @BindView(R.id.endtime)
    TextView endtime;
    @BindView(R.id.moviesave)
    Button moviesave;
    @BindView(R.id.video_frame_list)
    LinearLayout videoFrameList;
    @BindView(R.id.handler_left)
    View handlerLeft;
    @BindView(R.id.handler_right)
    View handlerRight;
    private static final int SLICE_COUNT = 8;
    @BindView(R.id.selectvideo)
    Button selectvideo;
    private int mSlicesTotalLength;
    private PLShortVideoTrimmer mShortVideoTrimmer;
    private long mDurationMs;
    private long mSelectedEndMs;
    private int mVideoFrameCount;
    private long mSelectedBeginMs;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trimlayout);
        ButterKnife.bind(this);
        //MediaController会占用资源导致裁剪失败
     videoview.setMediaController(new MediaController(this));
       /* MediaController controller=new MediaController(this);
      */
        progressDialog = new ProgressDialog(this);
    }

    private void initframeList() {

        handlerLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;
                updateHandlerLeftPosition(finalX);

                if (action == MotionEvent.ACTION_UP) {
                    calculateRange();
                }

                return true;
            }
        });

        handlerRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;
                updateHandlerRightPosition(finalX);

                if (action == MotionEvent.ACTION_UP) {
                    calculateRange();
                }

                return true;
            }
        });

        videoFrameList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                videoFrameList.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final int sliceEdge = videoFrameList.getWidth() / SLICE_COUNT;
                mSlicesTotalLength = sliceEdge * SLICE_COUNT;
                //  Log.i(TAG, "slice edge: " + sliceEdge);
                final float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

                new AsyncTask<Void, PLVideoFrame, Void>() {
                    @Override
                    protected Void doInBackground(Void... v) {
                        for (int i = 0; i < SLICE_COUNT; ++i) {
                            PLVideoFrame frame = mShortVideoTrimmer.getVideoFrameByTime((long) ((1.0f * i / SLICE_COUNT) * mDurationMs), false, sliceEdge, sliceEdge);
                            publishProgress(frame);
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(PLVideoFrame... values) {
                        super.onProgressUpdate(values);
                        PLVideoFrame frame = values[0];
                        if (frame != null) {
                            View root = LayoutInflater.from(VideoTrimActivity.this).inflate(R.layout.frame_item, null);

                            int rotation = frame.getRotation();
                            ImageView thumbnail = (ImageView) root.findViewById(R.id.thumbnail);
                            thumbnail.setImageBitmap(frame.toBitmap());
                            thumbnail.setRotation(rotation);
                            FrameLayout.LayoutParams thumbnailLP = (FrameLayout.LayoutParams) thumbnail.getLayoutParams();
                            if (rotation == 90 || rotation == 270) {
                                thumbnailLP.leftMargin = thumbnailLP.rightMargin = (int) px;
                            } else {
                                thumbnailLP.topMargin = thumbnailLP.bottomMargin = (int) px;
                            }
                            thumbnail.setLayoutParams(thumbnailLP);

                            LinearLayout.LayoutParams rootLP = new LinearLayout.LayoutParams(sliceEdge, sliceEdge);
                            videoFrameList.addView(root, rootLP);
                        }
                    }
                }.execute();
            }
        });
    }

    private void updateHandlerLeftPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) handlerLeft.getLayoutParams();
        if ((movedPosition + handlerLeft.getWidth()) > handlerRight.getX()) {
            lp.leftMargin = (int) (handlerRight.getX() - handlerLeft.getWidth());
        } else if (movedPosition < 0) {
            lp.leftMargin = 0;
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        handlerLeft.setLayoutParams(lp);
    }

    //路径
    private void init(String path1, String path2) {
        mShortVideoTrimmer = new PLShortVideoTrimmer(this, path1, path2);
        mSelectedEndMs = mDurationMs = mShortVideoTrimmer.getSrcDurationMs();
        //   duration.setText("时长: " + formatTime(mDurationMs));
        //  Log.i(TAG, "video duration: " + mDurationMs);

        mVideoFrameCount = mShortVideoTrimmer.getVideoFrameCount(false);
        //  Log.i(TAG, "video frame count: " + mVideoFrameCount);

        videoview.setVideoPath(path1);
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //  play();
            }
        });

        initframeList();
    }

    private void calculateRange() {
        float beginPercent = 1.0f * ((handlerLeft.getX() + handlerLeft.getWidth() / 2) - videoFrameList.getX()) / mSlicesTotalLength;
        float endPercent = 1.0f * ((handlerRight.getX() + handlerRight.getWidth() / 2) - videoFrameList.getX()) / mSlicesTotalLength;
        beginPercent = clamp(beginPercent);
        endPercent = clamp(endPercent);

        //  Log.i(TAG, "begin percent: " + beginPercent + " end percent: " + endPercent);

        mSelectedBeginMs = (long) (beginPercent * mDurationMs);
        mSelectedEndMs = (long) (endPercent * mDurationMs);
        starttime.setText(formatTime(mSelectedBeginMs));
        endtime.setText(formatTime(mSelectedEndMs));
        //  Log.i(TAG, "new range: " + mSelectedBeginMs + "-" + mSelectedEndMs);
        //  play();
    }

    private float clamp(float origin) {
        if (origin < 0) {
            return 0;
        }
        if (origin > 1) {
            return 1;
        }
        return origin;
    }

    private void updateHandlerRightPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) handlerRight.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (movedPosition < (handlerLeft.getX() + handlerLeft.getWidth())) {
            lp.leftMargin = (int) (handlerLeft.getX() + handlerLeft.getWidth());
        } else if ((movedPosition + (handlerRight.getWidth() / 2)) > (videoFrameList.getX() + mSlicesTotalLength)) {
            lp.leftMargin = (int) ((videoFrameList.getX() + mSlicesTotalLength) - (handlerRight.getWidth() / 2));
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        handlerRight.setLayoutParams(lp);
    }

    @OnClick({R.id.back, R.id.moviesave,R.id.selectvideo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.moviesave:
                progressDialog.show();
                videoview.setMediaController(null);
                mShortVideoTrimmer.trim(mSelectedBeginMs, mSelectedEndMs, new PLVideoSaveListener() {
                    @Override
                    public void onSaveVideoSuccess(String s) {
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VideoTrimActivity.this,"储存成功",Toast.LENGTH_SHORT).show();
                                if (progressDialog.isShowing()){
                                    progressDialog.dismiss();
                                }
                                videoview.setMediaController(new MediaController(VideoTrimActivity.this));
                            }
                        });

                    }

                    @Override
                    public void onSaveVideoFailed(int i) {
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VideoTrimActivity.this,"储存失败",Toast.LENGTH_SHORT).show();
                                if (progressDialog.isShowing()){
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }

                    @Override
                    public void onSaveVideoCanceled() {
                        Toast.makeText(VideoTrimActivity.this,"储存停止",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgressUpdate(float v) {

                    }
                });
                break;
            case R.id.selectvideo:
                Intent intent=new Intent();
                if (Build.VERSION.SDK_INT < 19) {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                } else {
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("video/*");
                }
                startActivityForResult(Intent.createChooser(intent, "选择要导入的视频"), 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        videoview.setVideoURI(data.getData());
        videoview.start();
        int a= (int) (Math.random()*10000);
        init(GetPathFromUri.getPath(VideoTrimActivity.this,data.getData()),"/sdcard/ShortVideo/videos/"+a+".mp4");
        initframeList();
    }
    //时间转换
    private String formatTime(long timeMs) {
        return String.format(Locale.CHINA, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeMs),
                TimeUnit.MILLISECONDS.toSeconds(timeMs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMs))
        );
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mShortVideoTrimmer != null) {
            mShortVideoTrimmer.destroy();
        }
    }
    Handler threadHandler=new Handler(Looper.getMainLooper());
    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            threadHandler.post(runnable);
        }
    }
}
