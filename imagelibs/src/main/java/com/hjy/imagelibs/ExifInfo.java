package com.hjy.imagelibs;

import android.media.ExifInterface;

import java.io.IOException;

/**
 * Created by hjy on 9/9/15.<br>
 */
public class ExifInfo {

    public final int rotation;
    public final boolean flipHorizontal;

    protected ExifInfo() {
        this.rotation = 0;
        this.flipHorizontal = false;
    }

    protected ExifInfo(int rotation, boolean flipHorizontal) {
        this.rotation = rotation;
        this.flipHorizontal = flipHorizontal;
    }


    /**
     * 获取照片的旋转角度等信息
     *
     * @param file 图片文件路径
     *
     * @return 图片的旋转等信息
     */
    public static ExifInfo defineExifOrientation(String file) {
        int rotation = 0;
        boolean flip = false;
        try {
            ExifInterface exif = new ExifInterface(file);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    flip = true;
                case ExifInterface.ORIENTATION_NORMAL:
                    rotation = 0;
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    flip = true;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    flip = true;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    flip = true;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ExifInfo(rotation, flip);
    }
}