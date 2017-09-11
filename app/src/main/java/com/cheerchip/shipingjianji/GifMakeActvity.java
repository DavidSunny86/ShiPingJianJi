package com.cheerchip.shipingjianji;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.cheerchip.shipingjianji.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.PLShortVideoComposer;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTrimmer;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;

import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by noname on 2017/9/6.
 */

public class GifMakeActvity extends BaseActivity {

    @BindView(R.id.addimage)
    Button addimage;
    @BindView(R.id.gride)
    GridView mGridView;
    @BindView(R.id.addvideo)
    Button addvideo;
    private PLShortVideoTrimmer mTrimmer;
    private PLShortVideoComposer mShortVideoComposer;
    private ArrayList<Integer> mSelectedFrameIndex;
    private LruCache<Integer, Bitmap> mBitmapCache;
    private Map<Integer, LoadFrameTask> mOngoingTasks;
    private ProgressDialog mProcessingDialog;
    private static final float CACHE_FREE_MEMORY_PERCENTAGE = 0.7f;
    private static final int THUMBNAIL_EDGE = 400;
    private ImageAdapter adapter;
    private String GIF_SAVE_PATH="/sdcard/sdcard/ShortVideo/imgs/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addimage);
        ButterKnife.bind(this);
        adapter = new ImageAdapter();

    }


    private void init(String videoPath) {

        mTrimmer = new PLShortVideoTrimmer(this, videoPath, "/sdcard/ShortVideo/imgs/2.mp4");
        mShortVideoComposer = new PLShortVideoComposer(this);
        mSelectedFrameIndex = new ArrayList<>();

        mBitmapCache = new LruCache<>(calculateCacheCount());
        mOngoingTasks = new Hashtable<>();

        final FrameAdapter adapter = new FrameAdapter(this);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int color;
                if (!mSelectedFrameIndex.contains(i)) {
                    mSelectedFrameIndex.add(i);
                    color = R.color.colorAccent;
                } else {
                    mSelectedFrameIndex.remove(mSelectedFrameIndex.indexOf(i));
                    color = R.color.white;
                }
                view.setBackgroundColor(getResources().getColor(color));
            }
        });
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
                Iterator<Integer> i = mOngoingTasks.keySet().iterator();
                while (i.hasNext()) {
                    int p = i.next();
                    LoadFrameTask task = mOngoingTasks.get(p);
                    if (task != null && !(p >= firstVisibleItem && p <= lastVisibleItem)) {
                        task.cancel(true);
                        i.remove();
                        //  Log.i(TAG, "cancel task position: " + p);
                    }
                }
            }
        });

        mProcessingDialog = new ProgressDialog(GifMakeActvity.this);
        mProcessingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProcessingDialog.setCancelable(false);
    }

    @OnClick({R.id.addvideo, R.id.addimage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.addvideo:

                Intent intent = new Intent();
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
            case R.id.addimage:

                Intent intent2 = new Intent();
                if (Build.VERSION.SDK_INT < 19) {
                    intent2.setAction(Intent.ACTION_GET_CONTENT);
                    intent2.setType("image/*");
                } else {
                    intent2.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent2.addCategory(Intent.CATEGORY_OPENABLE);
                    intent2.setType("image/*");
                }
                startActivityForResult(Intent.createChooser(intent2, "选择要导入的图片"), 1);
                break;
            case R.id.creategif:
                mProcessingDialog.setMessage("正在生成");
                mProcessingDialog.show();
                switch (currentMode){
                    case 1:

                        if (list.size() <= 0) {
                            Toast.makeText(this,"请先选择帧",Toast.LENGTH_SHORT).show();
                            mProcessingDialog.dismiss();
                            return;
                        }
                        int a1 = (int) (Math.random()*1000);
                        mShortVideoComposer.composeToGIF(list, 500, true, GIF_SAVE_PATH+a1+".gif", new PLVideoSaveListener() {

                            @Override
                            public void onSaveVideoSuccess(String s) {
                                mProcessingDialog.dismiss();
                                Toast.makeText(GifMakeActvity.this,"成功生成",Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onSaveVideoFailed(int i) {
                                mProcessingDialog.dismiss();
                                Toast.makeText(GifMakeActvity.this,"生成失败",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSaveVideoCanceled() {

                            }

                            @Override
                            public void onProgressUpdate(float v) {

                            }
                        });
                        break;
                    case 0:
                        if (mSelectedFrameIndex.size() <= 0) {
                            Toast.makeText(this,"请先选择帧",Toast.LENGTH_SHORT).show();
                            mProcessingDialog.dismiss();
                            return;
                        }
                        ArrayList<Bitmap> bitmaps = new ArrayList<>();
                        for (int i = 0; i < mSelectedFrameIndex.size(); i++) {
                            bitmaps.add(mTrimmer.getVideoFrameByIndex(mSelectedFrameIndex.get(i), true).toBitmap());
                        }
                        mProcessingDialog.setMessage("正在生成");
                        mProcessingDialog.show();
                        int a = (int) (Math.random()*1000);
                        mShortVideoComposer.composeToGIF(bitmaps, 500, true, GIF_SAVE_PATH+a+".gif", new PLVideoSaveListener() {

                            @Override
                            public void onSaveVideoSuccess(String s) {
                                mProcessingDialog.dismiss();
                                Toast.makeText(GifMakeActvity.this,"成功生成",Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onSaveVideoFailed(int i) {
                                mProcessingDialog.dismiss();
                                Toast.makeText(GifMakeActvity.this,"生成失败",Toast.LENGTH_SHORT).show();
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
                break;
        }
    }

    private class FrameAdapter extends BaseAdapter {
        private Context mContext;

        public FrameAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mTrimmer.getVideoFrameCount(true);
        }

        @Override
        public Object getItem(int position) {
            return mTrimmer.getVideoFrameByIndex(position, true);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setTag(position);
            imageView.setImageDrawable(null);
            imageView.setBackgroundColor(getResources().getColor(mSelectedFrameIndex.contains(position) ? R.color.colorAccent : R.color.white));

            Bitmap cached = mBitmapCache.get(position);
            if (cached != null) {
                imageView.setImageBitmap(cached);
            } else {
                LoadFrameTask task = mOngoingTasks.get(position);
                if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                }
                task = new LoadFrameTask(position, imageView);
                mOngoingTasks.put(position, task);
                task.execute();
            }
            return imageView;
        }
    }

    private class LoadFrameTask extends AsyncTask<Void, Void, Bitmap> {
        private int mIndex;
        private ImageView mImageView;

        public LoadFrameTask(int index, ImageView imageView) {
            mIndex = index;
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... a) {
            Bitmap bmp = mTrimmer.getVideoFrameByIndex(mIndex, true, THUMBNAIL_EDGE, THUMBNAIL_EDGE).toBitmap();
            mBitmapCache.put(mIndex, bmp);
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (((Integer) mImageView.getTag()) == mIndex) {
                mImageView.setImageBitmap(result);
            }
            mOngoingTasks.remove(mIndex);
        }
    }

    private int calculateCacheCount() {
        Runtime runtime = Runtime.getRuntime();
        long freeBytes = runtime.maxMemory() - runtime.totalMemory() - runtime.freeMemory();
        long cacheUseBytes = (long) (freeBytes * CACHE_FREE_MEMORY_PERCENTAGE);
        long perBitmapBytes = THUMBNAIL_EDGE * THUMBNAIL_EDGE * 4;
        int cacheCount = (int) (cacheUseBytes / perBitmapBytes);
        return cacheCount;
    }
    int currentMode=0;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==0){
            currentMode=0;
           // Uri uri= data.getData();
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            init(selectedFilepath);
        }else {
            currentMode=1;
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            initimg(selectedFilepath);
        }
    }

    private void initimg(String path) {
        Bitmap bitmap= BitmapFactory.decodeFile(path);
        list.add(bitmap);
        mGridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    List<Bitmap> list=new ArrayList<>();
    class ImageAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(GifMakeActvity.this);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(list.get(position));
            return imageView;
        }
    }
}
