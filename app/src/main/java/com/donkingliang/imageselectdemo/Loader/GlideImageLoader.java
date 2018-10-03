package com.donkingliang.imageselectdemo.Loader;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.donkingliang.imageselectdemo.R;
import com.donkingliang.imageselector.loader.BaseImageLoader;

import java.io.File;

public class GlideImageLoader implements BaseImageLoader {

    @Override
    public void displayImage(Context context, String path, ImageView imageView) {
                Glide.with(context)
                .load(path)
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(imageView);
    }

    @Override
    public void displayImage(Context context, String path, ImageView imageView, int width, int height) {

    }

    @Override
    public void displayImagePreview(Context context, String path, ImageView imageView, int width, int height) {

    }
}
