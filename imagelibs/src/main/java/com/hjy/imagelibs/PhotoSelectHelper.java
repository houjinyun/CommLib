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
public class PhotoSelectHelper {

    public interface OnPhotoSelectListener {
        /**
         * 图片获取成功
         *
         * @param intent Intent
         * @param file 选择的图片文件
         */
        public void onPhotoSelectSucc(Intent intent, File file);

        /**
         * 处理失败
         *
         * @param errorType 错误类型
         * @param errorMsg 错误信息
         */
        public void onPhotoSelectFail(int errorType, String errorMsg);
    }

    /**
     * 拍照
     */
    public int REQ_CODE_TAKE_PHOTO = 0;

    /**
     * 相册选择图片
     */
    public int REQ_CODE_SELECT_PHOTO = 0;
    /**
     * 剪切图片
     */
    public int REQ_CODE_CROP_IMAGE = 0;

    protected Activity mActivity;
    protected Fragment mFragment;
    protected File mCacheDir;           //图片文件的缓存目录
    protected int mCropWidth;           //裁切的宽度，不需要裁切则为0
    protected int mCropHeight;          //裁切的高度，不需要裁切则为0

    protected File mCacheFile;          //拍照时的输出文件
    /**
     * 拍照获得的图片名字是否隐藏文件类型，true-则图片名字以.jpeg结尾，false-则图片名字没有文件类型后缀名，一般系统相机不会扫描到该图片
     */
    private boolean mHideFileType = false;

    /**
     * 拍照或者从相册里选择图片时，是否直接返回原图，默认会对原图进行压缩处理，返回合适大小的图片，避免OOM
     */
    private boolean mIsReturnOriginalImage = false;

    protected OnPhotoSelectListener mOnPhotoSelectListener;

    public PhotoSelectHelper(Activity activity, File cacheDir) {
        this(activity, null, cacheDir);
    }

    public PhotoSelectHelper(Activity activity, Fragment fragment, File cacheDir) {
        this(activity, fragment, cacheDir, 0, 0);
    }

    /**
     * 如果是在Fragment里调用系统相机拍照程序，则fragment参数不能为null，否则不能处理拍照后的返回结果<br>
     * 如果需要拍照后或者剪切图片后对其进行裁剪，则必须传入cropWidth, cropHeight参数指明裁剪的大小
     *
     * @param activity 启动图片选择时所在的Activity，不能为null
     * @param fragment 启动图片选择时所在的Fragment，如不为null，则采用Fragment的startActivityForResult()方法
     * @param cacheDir 文件缓存目录，不能为空
     * @param cropWidth 需要裁减的宽度，如不需要则为0
     * @param cropHeight 需要裁减的高度，如不需要则为0
     */
    public PhotoSelectHelper(Activity activity, Fragment fragment, File cacheDir, int cropWidth, int cropHeight) {
        mActivity = activity;
        mFragment = fragment;
        mCacheDir = cacheDir;
        mCropWidth = cropWidth;
        mCropHeight = cropHeight;

        REQ_CODE_TAKE_PHOTO = R.string.hjy_req_take_photo;
        REQ_CODE_SELECT_PHOTO = R.string.hjy_req_pick_image_from_gallery;
        REQ_CODE_CROP_IMAGE = R.string.hjy_req_crop_image;
    }

    public void setOnPhotoSelectListener(OnPhotoSelectListener listener) {
        mOnPhotoSelectListener = listener;
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

    public void setHideFileType(boolean isHideFileType) {
        mHideFileType = isHideFileType;
    }

    public void setReturnOriginalImage(boolean returnOriginalImage) {
        mIsReturnOriginalImage = returnOriginalImage;
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
                deleteCacheFile();
                if(null != mOnPhotoSelectListener) {
                    mOnPhotoSelectListener.onPhotoSelectFail(ErrorType.ERROR_USER_CANCELLED, null);
                }
                return true;
            }
            if(mCropWidth > 0 && mCropHeight > 0) {
                //需要裁减
                cropImage(Uri.fromFile(mCacheFile), mCacheFile, mCropWidth, mCropHeight);
                return true;
            } else {
                if(mIsReturnOriginalImage) {
                    if(null != mOnPhotoSelectListener) {
                        mOnPhotoSelectListener.onPhotoSelectSucc(data, mCacheFile);
                    }
                    return true;
                }
                DisplayMetrics dm = mActivity.getResources().getDisplayMetrics();
                int w = dm.widthPixels;
                int h = dm.heightPixels;
                //限制输出图片大小
                Bitmap bmp = ImageUtil.decodeFileAndConsiderExif(mCacheFile.getAbsolutePath(), Math.min(w, h), w * h);
                if(bmp != null) {
                    //图片保存在文件里
                    ImageUtil.saveImage2File(bmp, 100, mCacheFile);
                    bmp.recycle();
                }
                if(null != mOnPhotoSelectListener) {
                    mOnPhotoSelectListener.onPhotoSelectSucc(data, mCacheFile);
                }
                return true;
            }
        } else if (requestCode == REQ_CODE_SELECT_PHOTO) {          //相册选择图片
            if(resultCode != Activity.RESULT_OK) {
                deleteCacheFile();
                if(null != mOnPhotoSelectListener) {
                    mOnPhotoSelectListener.onPhotoSelectFail(ErrorType.ERROR_USER_CANCELLED, null);
                }
                return true;
            }
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                Cursor cursor = mActivity.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                try {
                    if(cursor != null && cursor.moveToFirst()) {
                        String path = cursor.getString(0);
                        if(mCropWidth > 0 && mCropHeight > 0) {
                            //表示需要裁减
                            cropImage(Uri.fromFile(new File(path)), mCacheFile, mCropWidth, mCropHeight);
                            return true;
                        }

                        if(mIsReturnOriginalImage) {
                            deleteCacheFile();
                            if(null != mOnPhotoSelectListener) {
                                mOnPhotoSelectListener.onPhotoSelectSucc(data, new File(path));
                            }
                            return true;
                        }

                        DisplayMetrics dm = mActivity.getResources().getDisplayMetrics();
                        //限制输出图片大小
                        int w = dm.widthPixels;
                        int h = dm.heightPixels;
                        Bitmap bmp = ImageUtil.decodeFileAndConsiderExif(mCacheFile.getAbsolutePath(), Math.min(w, h), w * h);
                        if(bmp != null) {
                            //图片保存在文件里
                            ImageUtil.saveImage2File(bmp, 100, mCacheFile);
                            bmp.recycle();
                        }
                        if(null != mOnPhotoSelectListener) {
                            mOnPhotoSelectListener.onPhotoSelectSucc(data, new File(path));
                        }
                        return true;
                    } else {
                        deleteCacheFile();
                        if(null != mOnPhotoSelectListener) {
                            mOnPhotoSelectListener.onPhotoSelectFail(ErrorType.ERROR_PHOTO_NOT_EXIST, null);
                        }
                        return true;
                    }
                } finally {
                    if(cursor != null)
                        cursor.close();
                }
            }
            deleteCacheFile();
            if(null != mOnPhotoSelectListener) {
                mOnPhotoSelectListener.onPhotoSelectFail(ErrorType.ERROR_UNKNOWN, null);
            }
            return true;
        } else if (requestCode == REQ_CODE_CROP_IMAGE) {            //裁减图片
            if(resultCode != Activity.RESULT_OK) {
                deleteCacheFile();
                if(null != mOnPhotoSelectListener) {
                    mOnPhotoSelectListener.onPhotoSelectFail(ErrorType.ERROR_USER_CANCELLED, null);
                }
                return true;
            }
            if (null != mOnPhotoSelectListener) {
                mOnPhotoSelectListener.onPhotoSelectSucc(data, mCacheFile);
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
            deleteCacheFile();
            if(mOnPhotoSelectListener != null)
                mOnPhotoSelectListener.onPhotoSelectFail(ErrorType.ERROR_NO_GALLERY, e.getMessage());
        }
    }

    /**
     * 调用启动系统相机拍照
     */
    public void takePhoto() {
        if(!ImageUtil.hasCamera(mActivity)) {
            deleteCacheFile();
            if(mOnPhotoSelectListener != null) {
                mOnPhotoSelectListener.onPhotoSelectFail(ErrorType.ERROR_NO_CAMERA, null);
            }
            return;
        }
        try {
            mCacheFile = generateCacheFile(mHideFileType);
            Intent localIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
            localIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCacheFile));
            if(mFragment != null)
                mFragment.startActivityForResult(localIntent, REQ_CODE_TAKE_PHOTO);
            else {
                mActivity.startActivityForResult(localIntent, REQ_CODE_TAKE_PHOTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            deleteCacheFile();
            if(mOnPhotoSelectListener != null)
                mOnPhotoSelectListener.onPhotoSelectFail(ErrorType.ERROR_NO_CAMERA, e.getMessage());
        }
    }

    /**
     * 剪切图片
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
            deleteCacheFile();
            if(mOnPhotoSelectListener != null) {
                mOnPhotoSelectListener.onPhotoSelectFail(ErrorType.ERROR_NO_CROP_APP, e.getMessage());
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

    private void deleteCacheFile() {
        if(mCacheFile.exists()) {
            mCacheFile.delete();
        }
    }

}
