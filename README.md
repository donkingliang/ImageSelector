# ImageSelector
Android图片选择器，仿微信的图片选择器的样式和效果。支持图片的单选、限数量的多选和不限数量的多选。支持图片预览和图片文件夹的切换。

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
	compile 'com.github.donkingliang:ImageSelector:1.1.1'
```
**2、配置AndroidManifest.xml**
```xml
//储存卡的读取权限
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

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
```
**3、调起图片选择器**

ImageSelector支持图片的单选、限数量的多选和不限数量的多选。在调起图片选择器的时候需要告诉选择器，是那种情况。为了方便大家的使用，我在项目中提供了一个工具类，可以方便地调起选择器。
调起选择器只需要简单的一句代码就可以了。
```java
 //单选
 ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, true, 0);

//限数量的多选(比喻最多9张)
ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, false, 9);

//不限数量的多选
ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE);
//或者
ImageSelectorUtils.openPhoto(MainActivity.this, REQUEST_CODE, false, 0);
```
REQUEST_CODE就是调用者自己定义的启动Activity时的requestCode，这个相信大家都能明白。

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
