package com.cheerchip.shipingjianji;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cheerchip.shipingjianji.utils.KiwiTrackWrapper;
import com.kiwi.ui.StickerConfigMgr;
import com.kiwi.ui.widget.KwControlView;
import com.qiniu.pili.droid.shortvideo.PLAudioEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLCameraSetting;
import com.qiniu.pili.droid.shortvideo.PLFaceBeautySetting;
import com.qiniu.pili.droid.shortvideo.PLFocusListener;
import com.qiniu.pili.droid.shortvideo.PLMicrophoneSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordStateListener;
import com.qiniu.pili.droid.shortvideo.PLShortVideoRecorder;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoFilterListener;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import view.FocusIndicator;
import view.SquareGLSurfaceView;

/**
 * Created by noname on 2017/9/8.
 */

public class CameraActivity1 extends BaseActivity implements PLRecordStateListener , PLFocusListener{


    @BindView(R.id.squareglview)
    SquareGLSurfaceView squareglview;
    @BindView(R.id.kiwi_control_layout)
    KwControlView kiwiControlLayout;
    @BindView(R.id.focus_indicator)
    FocusIndicator focusIndicator;
    @BindView(R.id.startrecord)
    Button startrecord;
    @BindView(R.id.stoprecord)
    Button stoprecord;
    @BindView(R.id.savemovie)
    Button savemovie;
    @BindView(R.id.xianshi)
    Button xianshi;
    private PLShortVideoRecorder mShortVideoRecorder;
    private ProgressDialog dialog;
    private static final boolean USE_KIWI = true;
    private KiwiTrackWrapper mKiwiTrackWrapper;
    private String TAG="CameraActivity1";
    private GestureDetector mGestureDetector;
    private int mFocusIndicatorX;
    private int mFocusIndicatorY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recordlayout);
        ButterKnife.bind(this);
        dialog = new ProgressDialog(this);
        dialog.setMessage("正在生成文件.....");
        mShortVideoRecorder = new PLShortVideoRecorder();
        mShortVideoRecorder.setRecordStateListener(this);

        // 摄像头采集选项
        PLCameraSetting cameraSetting = new PLCameraSetting();
        PLCameraSetting.CAMERA_FACING_ID facingId = chooseCameraFacingId();
        cameraSetting.setCameraId(facingId);
        cameraSetting.setCameraId(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT);
        cameraSetting.setCameraPreviewSizeRatio(PLCameraSetting.CAMERA_PREVIEW_SIZE_RATIO.RATIO_4_3);
        cameraSetting.setCameraPreviewSizeLevel(PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL.PREVIEW_SIZE_LEVEL_480P);
       // 麦克风采集选项
        PLMicrophoneSetting microphoneSetting = new PLMicrophoneSetting();
       // 视频编码选项
        PLVideoEncodeSetting videoEncodeSetting = new PLVideoEncodeSetting(this);
        videoEncodeSetting.setEncodingSizeLevel(PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_480P_1); // 480x480
        videoEncodeSetting.setEncodingBitrate(1000 * 1024); // 1000kbps
        videoEncodeSetting.setEncodingFps(25);
        // 音频编码选项
        PLAudioEncodeSetting audioEncodeSetting = new PLAudioEncodeSetting();
        // 美颜选项
        PLFaceBeautySetting faceBeautySetting = new PLFaceBeautySetting(1.0f, 0.5f, 0.5f);
       // 录制选项
        PLRecordSetting recordSetting = new PLRecordSetting();
        recordSetting.setMaxRecordDuration(10 * 1000); // 10s
        recordSetting.setVideoCacheDir("/sdcard/ShortVideo/videos");
        recordSetting.setVideoFilepath("/sdcard/ShortVideo/videos/record.mp4");
        mShortVideoRecorder.prepare(squareglview, cameraSetting, microphoneSetting,
                videoEncodeSetting, audioEncodeSetting, USE_KIWI ? null : faceBeautySetting, recordSetting);
        if (USE_KIWI) {
            StickerConfigMgr.setSelectedStickerConfig(null);

            mKiwiTrackWrapper = new KiwiTrackWrapper(this, cameraSetting.getCameraId().ordinal());
            mKiwiTrackWrapper.onCreate(this);

//            findViewById(R.id.btn_camera_effect).setVisibility(View.VISIBLE);

            kiwiControlLayout = (KwControlView) findViewById(R.id.kiwi_control_layout);
            kiwiControlLayout.setOnEventListener(mKiwiTrackWrapper.initUIEventListener());
            kiwiControlLayout.setOnPanelCloseListener(new KwControlView.OnPanelCloseListener() {
                @Override
                public void onClosed() {
                    switchKiwiPanel(false);
                }
            });

            mShortVideoRecorder.setVideoFilterListener(new PLVideoFilterListener() {
                private int surfaceWidth;
                private int surfaceHeight;
                private boolean isTrackerOnSurfaceChangedCalled;

                @Override
                public void onSurfaceCreated() {
                    mKiwiTrackWrapper.onSurfaceCreated(CameraActivity1.this);
                }

                @Override
                public void onSurfaceChanged(int width, int height) {
                    surfaceWidth = width;
                    surfaceHeight = height;
                }

                @Override
                public void onSurfaceDestroy() {
                    mKiwiTrackWrapper.onSurfaceDestroyed();
                }

                @Override
                public int onDrawFrame(int texId, int texWidth, int texHeight, long timeStampNs, float[] transformMatrix) {
                    if (!isTrackerOnSurfaceChangedCalled) {
                        isTrackerOnSurfaceChangedCalled = true;
                        mKiwiTrackWrapper.onSurfaceChanged(surfaceWidth, surfaceHeight, texWidth, texHeight);
                    }
                    return mKiwiTrackWrapper.onDrawFrame(texId, texWidth, texHeight);
                }
            });
        }

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                mFocusIndicatorX =  (int) e.getX() - focusIndicator.getWidth() / 2;
                mFocusIndicatorY = (int) e.getY() - focusIndicator.getHeight() / 2;
                mShortVideoRecorder.manualFocus(focusIndicator.getWidth(), focusIndicator.getHeight(), (int) e.getX(), (int) e.getY());
                return false;

            }
        });
        squareglview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
        mShortVideoRecorder.setFocusListener(this);
        Log.e("onCreate: ","112" );
    }

    private void switchKiwiPanel(boolean show) {
        // findViewById(R.id.btns).setVisibility(show ? View.GONE : View.VISIBLE);
        // findViewById(R.id.btn_camera_effect).setVisibility(show ? View.GONE : View.VISIBLE);
        kiwiControlLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mKiwiTrackWrapper != null) {
            mKiwiTrackWrapper.onResume(this);
        }
        mShortVideoRecorder.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mKiwiTrackWrapper != null) {
            mKiwiTrackWrapper.onPause(this);
        }
        mShortVideoRecorder.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mShortVideoRecorder.destroy();
    }




    @Override
    public void onManualFocusStart(boolean result) {
        if (result) {
            Log.i(TAG, "manual focus begin success");
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) focusIndicator.getLayoutParams();
            lp.leftMargin = mFocusIndicatorX;
            lp.topMargin = mFocusIndicatorY;
            focusIndicator.setLayoutParams(lp);
            focusIndicator.focus();
        } else {
            focusIndicator.focusCancel();
            Log.i(TAG, "manual focus not supported");
        }
    }

    @Override
    public void onManualFocusStop(boolean result) {
        Log.i(TAG, "manual focus end result: " + result);
        if (result) {
            focusIndicator.focusSuccess();
        } else {
            focusIndicator.focusFail();
        }
    }

    @Override
    public void onManualFocusCancel() {
        Log.i(TAG, "manual focus canceled");
        focusIndicator.focusCancel();
    }

    @Override
    public void onAutoFocusStart() {
        Log.i(TAG, "auto focus start");
    }

    @Override
    public void onAutoFocusStop() {
        Log.i(TAG, "auto focus stop");
    }

    @Override
    public void onReady() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onDurationTooShort() {

    }

    @Override
    public void onRecordStarted() {

    }

    @Override
    public void onRecordStopped() {

    }

    @Override
    public void onSectionIncreased(long l, long l1, int i) {

    }

    @Override
    public void onSectionDecreased(long l, long l1, int i) {

    }

    @Override
    public void onRecordCompleted() {

    }


    @OnClick({R.id.startrecord, R.id.stoprecord, R.id.savemovie})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.startrecord:
                ((Button) view).setText("正在录制");
                // 开始一段视频
                mShortVideoRecorder.beginSection();
                break;
            case R.id.stoprecord:
                // 结束一段视频
                mShortVideoRecorder.endSection();
                startrecord.setText("开始录制");
                break;
            case R.id.savemovie:
                // 合成和保存所有的视频片段
                //loading
                dialog.show();
                mShortVideoRecorder.concatSections(new PLVideoSaveListener() {
                    @Override
                    public void onSaveVideoSuccess(String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                    Toast.makeText(CameraActivity1.this, "成功", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onSaveVideoFailed(int i) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                    Toast.makeText(CameraActivity1.this, "失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onSaveVideoCanceled() {

                    }

                    @Override
                    public void onProgressUpdate(float v) {

                    }
                });
                break;
        }
    }

    @OnClick(R.id.xianshi)
    public void onViewClicked() {
        kiwiControlLayout.setVisibility(View.VISIBLE);
    }
    private PLCameraSetting.CAMERA_FACING_ID chooseCameraFacingId() {
        if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD;
        } else if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        }
    }
}
