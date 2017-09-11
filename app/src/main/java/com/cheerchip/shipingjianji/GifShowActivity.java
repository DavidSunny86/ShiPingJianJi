package com.cheerchip.shipingjianji;

import android.content.Intent;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.qiniu.pili.droid.shortvideo.demo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by noname on 2017/9/5.
 */

public class GifShowActivity extends AppCompatActivity {



    @BindView(R.id.gifview)
    SimpleDraweeView gifview;
    @BindView(R.id.xuanqu)
    Button xuanqu;
    private int a;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.gifview);
        ButterKnife.bind(this);
        showgif("sdcard/ShortVideo/a.gif");
    }

    private SoundPool mSoundPool = null;

    private void showgif(String url){
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)//自动播放动画
                .setUri(Uri.parse("file:///"+url))//路径
                .build();
        gifview.setController(draweeController);
    }

    private void playWaitingTone() {
/*    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder builder=new SoundPool.Builder();
            builder.setMaxStreams(1);
            AudioAttributes.Builder attrBuilder=new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            mSoundPool=builder.build();
        }else{
            mSoundPool=new SoundPool(1,AudioManager.STREAM_MUSIC,0);
        }
        a = mSoundPool.load(this, R.raw.weichat_audio,1);
        gifview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                playWaitingTone();
                return false;
            }
        });*/
        mSoundPool.play(a, 0.8f, 0.8f, 0, 0, 1);
    }

    @OnClick({R.id.gifview, R.id.xuanqu})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.gifview:
                break;
            case R.id.xuanqu:
                Intent intent=new Intent();
                if (Build.VERSION.SDK_INT < 19) {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                } else {
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                }
                startActivityForResult(Intent.createChooser(intent, "选择要导入的图片"), 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                    .setAutoPlayAnimations(true)//自动播放动画
                    .setUri(data.getData())//路径
                    .build();
            gifview.setController(draweeController);
        }else {
            return;
        }
    }
}
