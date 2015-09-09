package com.hjy.imagelibs;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.Random;

/**
 * 处理图片选择，拍照，以及图片剪切等功能
 * 
 * @author Jinyun Hou
 * 
 */
public class PhotoSelectHandler {

    public interface OnImageSelectedListener {
        /**
         * 图片选择回调
         *
         * @param intent Intent
         * @param file 选择的图片文件
         */
        public void onImageSelected(Intent intent, File file);

        /**
         * 处理失败
         */
        public void onFail();
    }

    /**
     * 拍照
     */
    public static final int REQ_CODE_TAKE_PHOTO = 9991;

    /**
     * 相册选择图片
     */
    public static final int REQ_CODE_SELECT_PHOTO = 9992;
    /**
     * 剪切图片
     */
    public static final int REQ_CODE_CROP_IMAGE = 9993;

    protected Activity mActivity;
    protected Fragment mFragment;
    protected File mCacheDir;           //文件缓存目录
    protected int mCropWidth;           //裁切的宽度，不需要裁切则为0
    protected int mCropHeight;          //裁切的高度，不需要裁切则为0

    protected File mCacheFile;          //拍照时的输出文件

    protected OnImageSelectedListener mOnImageSelectedListener;

    public PhotoSelectHandler(Activity activity, File cacheDir) {
        this(activity, null, cacheDir);
    }

    public PhotoSelectHandler(Activity activity, Fragment fragment, File cacheDir) {
        this(activity, fragment, cacheDir, 0, 0);
    }

    /**
     *
     * @param activity 启动图片选择时所在的Activity，不能为null
     * @param fragment 启动图片选择时所在的Fragment，如不为null，则采用Fragment的startActivityForResult()方法
     * @param cacheDir 文件缓存目录，不能为空
     * @param cropWidth 需要裁减的宽度，如不需要则为0
     * @param cropHeight 需要裁减的高度，如不需要则为0
     */
    public PhotoSelectHandler(Activity activity, Fragment fragment, File cacheDir, int cropWidth, int cropHeight) {
        mActivity = activity;
        mFragment = fragment;
        mCacheDir = cacheDir;
        mCropWidth = cropWidth;
        mCropHeight = cropHeight;
    }

    public void setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
        mOnImageSelectedListener = onImageSelectedListener;
    }

    /**
     * 设置裁减的宽度、高度
     *
     * @param width 宽度
     * @param height 高度
     */
    public void setCropWidthAndHeight(int width, int height) {
        mCropWidth = width;
        mCropHeight = height;
    }

    public void setCacheDir(File cacheDir) {
        mCacheDir = cacheDir;
    }

    /**
     * 在Activity或者Fragment中的onActivityResult()方法中必须调用该方法
     *
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 包含返回的图片信息等
     *
     * @return true表示是已经处理过，false表示没有
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_TAKE_PHOTO) {                  //拍照获取图片
            if(resultCode != Activity.RESULT_OK) {
                if(null != mOnImageSelectedListener) {
                    mOnImageSelectedListener.onFail();
                }
                return true;
            }
            if(mCropWidth > 0 || mCropHeight > 0) {
                //需要裁减
                cropImage(Uri.fromFile(mCacheFile), mCacheFile, mCropWidth, mCropHeight);
                return true;
            } else {
                DisplayMetrics dm = mActivity.getResources().getDisplayMetrics();
                //限制输出图片大小
                Bitmap bmp = ImageUtil.decodeFileAndConsiderExif(mCacheFile.getAbsolutePath(), dm.widthPixels, dm.widthPixels * dm.heightPixels);
                if(bmp != null) {
                    //图片保存在文件里
                    ImageUtil.saveImage2File(bmp, 100, mCacheFile);
                    bmp.recycle();
                    bmp = null;
                    if(null != mOnImageSelectedListener) {
                        mOnImageSelectedListener.onImageSelected(data, mCacheFile);
                    }
                    return true;
                } else {
                    if(null != mOnImageSelectedListener) {
                        mOnImageSelectedListener.onImageSelected(data, mCacheFile);
                    }
                    return true;
                }
            }
        } else if (requestCode == REQ_CODE_SELECT_PHOTO) {          //相册选择图片
            if(resultCode != Activity.RESULT_OK) {
                if(null != mOnImageSelectedListener) {
                    mOnImageSelectedListener.onFail();
                }
                return true;
            }
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                Cursor cursor = mActivity.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION}, null, null, null);
                try {
                    if(cursor != null && cursor.moveToFirst()) {
                        String path = cursor.getString(0);
                        if(mCropWidth > 0 || mCropHeight > 0) {
                            //表示需要裁减
                            cropImage(Uri.fromFile(new File(path)), mCacheFile, mCropWidth, mCropHeight);
                            return true;
                        }

                        DisplayMetrics dm = mActivity.getResources().getDisplayMetrics();
                        //限制输出图片大小
                        Bitmap bmp = ImageUtil.decodeFileAndConsiderExif(path, dm.widthPixels, dm.widthPixels * dm.heightPixels);
                        if(bmp != null) {
                            //图片保存在文件里
                            ImageUtil.saveImage2File(bmp, 100, mCacheFile);
                            bmp.recycle();
                            bmp = null;
                            if(null != mOnImageSelectedListener) {
                                mOnImageSelectedListener.onImageSelected(data, mCacheFile);
                            }
                            return true;
                        } else {
                            if(null != mOnImageSelectedListener) {
                                mOnImageSelectedListener.onImageSelected(data, new File(path));
                            }
                            return true;
                        }
                    }
                } finally {
                    if(cursor != null)
                        cursor.close();
                }
            }
            if(null != mOnImageSelectedListener) {
                mOnImageSelectedListener.onFail();
            }
            return true;
        } else if (requestCode == REQ_CODE_CROP_IMAGE) {            //裁减图片
            if(resultCode != Activity.RESULT_OK) {
                if(null != mOnImageSelectedListener) {
                    mOnImageSelectedListener.onFail();
                }
                return true;
            }
            if (null != mOnImageSelectedListener) {
                mOnImageSelectedListener.onImageSelected(data, mCacheFile);
            }
            return true;
        }
        return false;
    }

    /**
     * 从相册中选择照片
     */
    public void pickPhotoFromGallery() {
        try {
            mCacheFile = generateCacheFile(true);
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if(mFragment != null)
                mFragment.startActivityForResult(intent, REQ_CODE_SELECT_PHOTO);
            else {
                mActivity.startActivityForResult(intent, REQ_CODE_SELECT_PHOTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动系统相机拍照
     *
     */
    public void takePhoto() {
        try {
            mCacheFile = generateCacheFile(false);
            Intent localIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
            localIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCacheFile));
            if(mFragment != null)
                mFragment.startActivityForResult(localIntent, REQ_CODE_TAKE_PHOTO);
            else {
                mActivity.startActivityForResult(localIntent, REQ_CODE_TAKE_PHOTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 剪切头像
     *
     * @param paramUri 需要裁减的图片Uri
     * @param file 输出文件
     * @param outX 裁减宽度
     * @param outY 裁减高度
     */
    private void cropImage(Uri paramUri, File file, int outX, int outY) {
        try {
            Intent localIntent = new Intent("com.android.camera.action.CROP");
            localIntent.setDataAndType(paramUri, "image/*");
            localIntent.putExtra("crop", "true");
            localIntent.putExtra("aspectX", 1);
            localIntent.putExtra("aspectY", 1);
            localIntent.putExtra("outputX", outX);
            localIntent.putExtra("outputY", outY);
            localIntent.putExtra("noFaceDetection", true);
            localIntent.putExtra("return-data", false);
            localIntent.putExtra("outputFormat", "JPEG");
            localIntent.putExtra("output", Uri.fromFile(file));
            if(mFragment != null)
                mFragment.startActivityForResult(localIntent, REQ_CODE_CROP_IMAGE);
            else {
                mActivity.startActivityForResult(localIntent, REQ_CODE_CROP_IMAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(mOnImageSelectedListener != null) {
                mOnImageSelectedListener.onFail();
            }
        }
    }

    private File generateCacheFile(boolean hideFileType) {
        if(!mCacheDir.exists())
            mCacheDir.mkdirs();
        File file = new File(mCacheDir, generatePhotoName(hideFileType));
        return file;
    }

    /**
     * 随机生成一个图片名字
     * @param hideFileType 是否隐藏文件类型
     *
     * @return 图片名字
     */
    public static String generatePhotoName(boolean hideFileType) {
        String str = System.currentTimeMillis() + "-" + new Random().nextInt(1000) + (hideFileType ? "" : ".JPEG");
        return str;
    }

}
