package com.cheerchip.shipingjianji;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.utils.ToastUtils;
import com.cheerchip.shipingjianji.utils.PermissionChecker;
import com.qiniu.pili.droid.shortvideo.PLShortVideoEditor;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTrimmer;
import com.qiniu.pili.droid.shortvideo.demo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.makegif)
    Button makegif;
    @BindView(R.id.showgif)
    Button showgif;
    @BindView(R.id.video)
    Button video;
    @BindView(R.id.trimvideo)
    Button trimvideo;
    private String videopath;
    private String outputpath = "/sdcard/Movies/33.mp4";
    private PLShortVideoEditor mShortVideoEditor;
    private String videoPath = "/sdcard/Movies/22.mp4";
    private PLShortVideoTrimmer mShortVideoTrimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        isPermissionOK() ;
       /* //视频剪辑对象
        mShortVideoTrimmer = new PLShortVideoTrimmer(getApplicationContext(), videoPath, outputpath);
        // beginMs 和 endMs 可参考 从 getKeyFrame() 返回的 PLVideoFrame 中的时间戳
        mShortVideoTrimmer.trim(0, 2 * 60 * 1000, new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(String s) {
                Log.e( "onSaveVideoSuccess: ","1" );
            }

            @Override
            public void onSaveVideoFailed(int i) {
                Log.e( "onSaveVideoSuccess: ","2" );
            }

            @Override
            public void onSaveVideoCanceled() {
                Log.e( "onSaveVideoSuccess: ","3" );
            }

            @Override
            public void onProgressUpdate(float v) {
                Log.e("onProgressUpdate: ","4" );
            }
        });
        //*/
        //mShortVideoTranscoder = new PLShortVideoTranscoder(getApplicationContext(), videoPath, outputpath);

    }

    @OnClick({R.id.makegif, R.id.showgif, R.id.video,R.id.trimvideo,R.id.shipinluzhi,R.id.pinmuluzhi})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.makegif:
                startActivity(new Intent(this, GifMakeActvity.class));
                break;
            case R.id.showgif:
                startActivity(new Intent(this, GifShowActivity.class));
                break;
            case R.id.video:
                startActivity(new Intent(this, VideoActivity.class));
                break;
            case R.id.trimvideo:
                startActivity(new Intent(this, VideoTrimActivity.class));
                break;
            case R.id.shipinluzhi:
                startActivity(new Intent(this, CameraActivity1.class));
                break;
            case R.id.pinmuluzhi:
                startActivity(new Intent(this,ScreenRecordActivity.class));
                break;
        }
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
       //     ToastUtils.s(this, "Some permissions is not approved !!!");.
            Log.e( "isPermissionOK: ","false" );
        }
        Log.e( "isPermissionOK: ","ok" );

        return isPermissionOK;
    }
}
