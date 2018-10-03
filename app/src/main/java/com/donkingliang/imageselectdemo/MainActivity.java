package com.donkingliang.imageselectdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.donkingliang.imageselectdemo.Loader.GlideImageLoader;
import com.donkingliang.imageselectdemo.Loader.PicassoImageLoader;
import com.donkingliang.imageselectdemo.adapter.ImageAdapter;
import com.donkingliang.imageselector.ImageSelector;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 0x00000011;
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView rvImage;
    private ImageAdapter mAdapter;
    private RadioButton mButtonGlide,mButtonPicasso,mButtonFresco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonGlide =  findViewById(R.id.rb_glide);
        mButtonPicasso = findViewById(R.id.rb_picasso);
        mButtonFresco = findViewById(R.id.rb_fresco);

        mButtonGlide.setOnClickListener(this);
        mButtonPicasso.setOnClickListener(this);
        mButtonFresco.setOnClickListener(this);

        // 默认用Glide加载
        ImageSelector.builder()
                .setImageLoader(new GlideImageLoader());

        rvImage = findViewById(R.id.rv_image);
        rvImage.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new ImageAdapter(this);
        rvImage.setAdapter(mAdapter);

        findViewById(R.id.btn_single).setOnClickListener(this);
        findViewById(R.id.btn_limit).setOnClickListener(this);
        findViewById(R.id.btn_unlimited).setOnClickListener(this);
        findViewById(R.id.btn_clip).setOnClickListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {
            ArrayList<String> images = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);
            mAdapter.refresh(images);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_glide:
                Log.i(TAG, "onCreate: mGlide");
                ImageSelector.builder()
                        .setImageLoader(new GlideImageLoader());
                break;
            case R.id.rb_picasso:
                Log.i(TAG, "onCreate: mButtonPicasso");
                ImageSelector.builder()
                        .setImageLoader(new PicassoImageLoader());
                break;
            case R.id.rb_fresco:
                Log.i(TAG, "onCreate: mButtonFresco");
                ImageSelector.builder()
                        .setImageLoader(new GlideImageLoader());
                break;
            case R.id.btn_single:
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(true)  //设置是否单选
                        .setViewImage(true) //是否点击放大图片查看,，默认为true
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_limit:
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(false)  //设置是否单选
                        .setViewImage(true) //是否点击放大图片查看,，默认为true
                        .setMaxSelectCount(9) // 图片的最大选择数量，小于等于0时，不限数量。
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_unlimited:
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(false)  //设置是否单选
                        .setViewImage(true) //是否点击放大图片查看,，默认为true
                        .setMaxSelectCount(0) // 图片的最大选择数量，小于等于0时，不限数量。
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_clip:
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setCrop(true)  // 设置是否使用图片剪切功能。
                        .setSingle(true)  //设置是否单选
                        .setViewImage(true) //是否点击放大图片查看,，默认为true
                        .start(this, REQUEST_CODE); // 打开相册
                break;
        }
    }
}
