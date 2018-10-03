package com.donkingliang.imageselector.loader;

import android.content.Context;
import android.widget.ImageView;

import java.io.Serializable;

public interface BaseImageLoader extends Serializable {

    void displayImage(Context context, String path, ImageView imageView);

    void displayImage(Context context, String path, ImageView imageView, int width, int height);

    void displayImagePreview(Context context, String path, ImageView imageView, int width, int height);
}