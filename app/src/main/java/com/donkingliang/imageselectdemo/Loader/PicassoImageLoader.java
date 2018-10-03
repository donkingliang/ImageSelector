package com.donkingliang.imageselectdemo.Loader;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.donkingliang.imageselector.loader.BaseImageLoader;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PicassoImageLoader  implements BaseImageLoader {

    @Override
    public void displayImage(Context context, String path, ImageView imageView) {
        int width = 100;
        int height = 100;
        Picasso.get()
                .load(Uri.fromFile(new File(path)))
                .resize(width, height)
                .centerInside()
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)//
                .into(imageView);
    }

    @Override
    public void displayImage(Context context, String path, ImageView imageView, int width, int height) {

    }

    @Override
    public void displayImagePreview(Context context, String path, ImageView imageView, int width, int height) {

    }
}
