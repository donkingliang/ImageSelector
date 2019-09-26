package com.donkingliang.imageselectdemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.donkingliang.imageselectdemo.adapter.ImageAdapter;
import com.donkingliang.imageselector.utils.ImageSelector;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 0x00000011;

    private RecyclerView rvImage;
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvImage = findViewById(R.id.rv_image);
        rvImage.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new ImageAdapter(this);
        rvImage.setAdapter(mAdapter);

        findViewById(R.id.btn_single).setOnClickListener(this);
        findViewById(R.id.btn_limit).setOnClickListener(this);
        findViewById(R.id.btn_unlimited).setOnClickListener(this);
        findViewById(R.id.btn_clip).setOnClickListener(this);
        findViewById(R.id.btn_only_take).setOnClickListener(this);
        findViewById(R.id.btn_take_and_clip).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {
            ArrayList<String> images = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);
            boolean isCameraImage = data.getBooleanExtra(ImageSelector.IS_CAMERA_IMAGE, false);
//            Log.d("ImageSelector", "是否是拍照图片：" + isCameraImage);
            mAdapter.refresh(images);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_single:
                //单选
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(true)  //设置是否单选
                        .canPreview(true) //是否点击放大图片查看,，默认为true
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_limit:
                //多选(最多9张)
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(false)  //设置是否单选
                        .canPreview(true) //是否点击放大图片查看,，默认为true
                        .setMaxSelectCount(9) // 图片的最大选择数量，小于等于0时，不限数量。
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_unlimited:
                //多选(不限数量)
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setSingle(false)  //设置是否单选
                        .canPreview(true) //是否点击放大图片查看,，默认为true
                        .setMaxSelectCount(0) // 图片的最大选择数量，小于等于0时，不限数量。
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_clip:
                //单选并剪裁
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setCrop(true)  // 设置是否使用图片剪切功能。
                        .setSingle(true)  //设置是否单选
                        .canPreview(true) //是否点击放大图片查看,，默认为true
                        .start(this, REQUEST_CODE); // 打开相册
                break;

            case R.id.btn_only_take:
                //仅拍照
                ImageSelector.builder()
                        .onlyTakePhoto(true)  // 仅拍照，不打开相册
                        .start(this, REQUEST_CODE);
                break;

            case R.id.btn_take_and_clip:
                //拍照并剪裁
                ImageSelector.builder()
                        .setCrop(true) // 设置是否使用图片剪切功能。
                        .onlyTakePhoto(true)  // 仅拍照，不打开相册
                        .start(this, REQUEST_CODE);
                break;
        }
    }
}
