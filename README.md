# ImageSelector
Android图片选择器，仿微信的图片选择器的样式和效果。支持图片的单选、限数量的多选和不限数量的多选。支持图片预览和图片文件夹的切换。支持图片单选并剪裁。

先上效果图：

![相册](https://github.com/donkingliang/ImageSelector/blob/master/%E6%95%88%E6%9E%9C%E5%9B%BE/%E7%9B%B8%E5%86%8C.jpg)  ![文件夹](https://github.com/donkingliang/ImageSelector/blob/master/%E6%95%88%E6%9E%9C%E5%9B%BE/%E6%96%87%E4%BB%B6%E5%A4%B9.jpg)  ![预览](https://github.com/donkingliang/ImageSelector/blob/master/%E6%95%88%E6%9E%9C%E5%9B%BE/%E9%A2%84%E8%A7%88.jpg)

**1、引入依赖**

在Project的build.gradle在添加以下代码

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
在Module的build.gradle在添加以下代码

```
	implementation 'com.github.donkingliang:ImageSelector:1.6.7'
```
ImageSelector从1.5.0版本开始使用了Glide 4.x的版本，由于Glide 3.x版本和4.x版本在使用上有所差异，如果你的项目使用了Glide 3.x版本，而又不想升级到4.x,那么你也可以使用ImageSelector:1.4.0版本，它和新的版本在使用和功能上都会有所差异。[ImageSelector 1.4.0](https://github.com/donkingliang/ImageSelector/blob/master/README1.4.0.md)

**2、配置AndroidManifest.xml**
```xml
//储存卡的读写权限
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
//调用相机权限
<uses-permission android:name="android.permission.CAMERA" />

//图片选择Activity
<activity android:name="com.donkingliang.imageselector.ImageSelectorActivity"
	//去掉Activity的ActionBar。
	//使用者可以根据自己的项目去配置，不一定要这样写，只要不Activity的ActionBar去掉就可以了。
    android:theme="@style/Theme.AppCompat.Light.NoActionBar"
    //横竖屏切换处理。
    //如果要支持横竖屏切换，一定要加上这句，否则在切换横竖屏的时候会发生异常。
    android:configChanges="orientation|keyboardHidden|screenSize"/>
    
//图片预览Activity
<activity android:name="com.donkingliang.imageselector.PreviewActivity"
    android:theme="@style/Theme.AppCompat.Light.NoActionBar"
    android:configChanges="orientation|keyboardHidden|screenSize"/>

//图片剪切Activity
<activity
    android:name="com.donkingliang.imageselector.ClipImageActivity"
    android:theme="@style/Theme.AppCompat.Light.NoActionBar" />

<!-- Android 7.0 文件共享配置，必须配置 -->
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```
在res/xml文件夹下创建file_paths.xml文件(名字可以自己定义)

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>

    <!-- 这个是保存拍照图片的路径,必须配置。 -->
    <external-path
        name="images"
        path="Pictures" />
</paths>
```

**3、调起图片选择器**

ImageSelector支持图片的单选、限数量的多选和不限数量的多选。还可以设置是否使用相机、是否剪切图片等配置。ImageSelector提供了统一的调起相册的方法。
```java
 //单选
 ImageSelector.builder()
        .useCamera(true) // 设置是否使用拍照
        .setSingle(true)  //设置是否单选
	.setViewImage(true) //是否点击放大图片查看,，默认为true
        .start(this, REQUEST_CODE); // 打开相册

//限数量的多选(比喻最多9张)
ImageSelector.builder()
        .useCamera(true) // 设置是否使用拍照
        .setSingle(false)  //设置是否单选
        .setMaxSelectCount(9) // 图片的最大选择数量，小于等于0时，不限数量。
	.setSelected(selected) // 把已选的图片传入默认选中。
	.setViewImage(true) //是否点击放大图片查看,，默认为true
        .start(this, REQUEST_CODE); // 打开相册

//不限数量的多选
ImageSelector.builder()
        .useCamera(true) // 设置是否使用拍照
        .setSingle(false)  //设置是否单选
        .setMaxSelectCount(0) // 图片的最大选择数量，小于等于0时，不限数量。
	.setSelected(selected) // 把已选的图片传入默认选中。
	.setViewImage(true) //是否点击放大图片查看,，默认为true
        .start(this, REQUEST_CODE); // 打开相册

//单选并剪裁
ImageSelector.builder()
       .useCamera(true) // 设置是否使用拍照
       .setCrop(true)  // 设置是否使用图片剪切功能。
       .setSingle(true)  //设置是否单选
       .setViewImage(true) //是否点击放大图片查看,，默认为true
       .start(this, REQUEST_CODE); // 打开相册
```
REQUEST_CODE就是调用者自己定义的启动Activity时的requestCode，这个相信大家都能明白。selected可以在再次打开选择器时，把原来已经选择过的图片传入，使这些图片默认为选中状态。

**4、接收选择器返回的数据**

在Activity的onActivityResult方法中接收选择器返回的数据。
```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {
	    //获取选择器返回的数据
            ArrayList<String> images = data.getStringArrayListExtra(
            ImageSelectorUtils.SELECT_RESULT);
        }
    }
```
ImageSelectorUtils.SELECT_RESULT是接收数据的key。数据是以ArrayList的字符串数组返回的，就算是单选，返回的也是ArrayList数组，只不过这时候ArrayList只有一条数据而已。ArrayList里面的数据就是选中的图片的文件路径。

想要了解ImageSelector的实现思路和核心代码的同学请看这里：[Android 实现一个仿微信的图片选择器](http://blog.csdn.net/u010177022/article/details/70147243)
