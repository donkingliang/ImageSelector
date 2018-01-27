package com.donkingliang.imageselectdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.donkingliang.imageselectdemo.adapter.ImageAdapter;
import com.donkingliang.imageselector.utils.ImageSelectorUtils;

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {
            ArrayList<String> images = data.getStringArrayListExtra(ImageSelectorUtils.SELECT_RESULT);
            mAdapter.refresh(images);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_single:
                //单选
                ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, true, 0);
                break;

            case R.id.btn_limit:
                //多选(最多9张)
                ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, false, 9);
//                ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, false, 9, mAdapter.getImages()); // 把已选的传入。
                break;

            case R.id.btn_unlimited:
                //多选(不限数量)
                ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE);
//                ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, mAdapter.getImages()); // 把已选的传入。
                //或者
//                ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, false, 0);
//                ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, false, 0, mAdapter.getImages()); // 把已选的传入。
                break;

            case R.id.btn_clip:
                //单选并剪裁
                ImageSelectorUtils.openPhotoAndClip(MainActivity.this, REQUEST_CODE);
                break;
        }
    }
}
