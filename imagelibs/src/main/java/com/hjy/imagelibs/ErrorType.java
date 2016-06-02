package com.hjy.imagelibs;

/**
 * Created by hjy on 6/2/16.<br>
 */
public class ErrorType {

    public static final int ERROR_UNKNOWN = 0;          //未知错误
    public static final int ERROR_NO_CAMERA = 1;        //系统没有可用拍照的相机程序
    public static final int ERROR_NO_GALLERY = 2;       //系统没有可用的相册程序
    public static final int ERROR_NO_CROP_APP = 3;      //系统没可用的图片剪切程序
    public static final int ERROR_USER_CANCELLED = 4;    //用户自己取消操作
    public static final int ERROR_PHOTO_NOT_EXIST = 5;      //从相册里选的图片不存在

}
