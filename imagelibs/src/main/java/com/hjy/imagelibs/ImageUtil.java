package com.hjy.imagelibs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {

    public static Bitmap decodeFile(String file, int minSideLength, int maxNumOfPixels) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);
        int inSampleSize = computeSampleSize(options, minSideLength, maxNumOfPixels);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        Bitmap bmp = BitmapFactory.decodeFile(file, options);
        return bmp;
    }

    public static Bitmap decodeFile(String file, int rotation, int minSideLength, int maxNumOfPixels) {
        Bitmap bm = decodeFile(file, minSideLength, maxNumOfPixels);
        if(rotation == 0)
            return bm;
        Matrix m = new Matrix();
        m.postRotate(rotation);
        try {
            Bitmap rotateBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            if(bm != rotateBmp) {
                bm.recycle();
                bm = null;
            }
            return rotateBmp;
        } catch (OutOfMemoryError e) {
            return bm;
        }
    }


    /**
     *
     * 解析图片时，考虑图片的选择等信息
     *
     * @param file 图片的路径
     * @param minSideLength 最小边长 不限制则填-1
     * @param maxNumOfPixels 最大像素 不限制则填-1
     *
     * @return Bitmap
     */
    public static Bitmap decodeFileAndConsiderExif(String file, int minSideLength, int maxNumOfPixels) {
        //处理图片大小
        try {
            String filePath = file;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            int sampleSize = computeSampleSize(options, minSideLength, maxNumOfPixels);
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            //获取图片的旋转信息
            ExifInfo exifInfo = ExifInfo.defineExifOrientation(filePath);
            if (exifInfo.rotation != 0 || exifInfo.flipHorizontal) {
                Matrix m = new Matrix();
                // Flip bitmap if need
                boolean flipHorizontal = exifInfo.flipHorizontal;
                int rotation = exifInfo.rotation;
                if (flipHorizontal) {
                    m.postScale(-1, 1);
                }
                // Rotate bitmap if need
                if (rotation != 0) {
                    m.postRotate(rotation);
                }

                try {
                    Bitmap rotateBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                    if(bmp != rotateBmp) {
                        bmp.recycle();
                        bmp = null;
                    }
                    bmp = rotateBmp;
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }

            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 计算图片decode时的inSampleSize
     *
     * @param options BitmapFactory.Options
     * @param minSideLength 图片最小宽或高，不限制则填-1
     * @param maxNumOfPixels 图片最大像素，不限制则填-1
     *
     * @return inSampleSize
     */
    public static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8 ) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength),  Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 将Bitmap转化成byte数组
     *
     * @param bmp Bitmap
     * @param recycle 是否recycle掉原图
     *
     * @return PNG格式的byte数组
     */
    public static byte[] convertBmp2Bytes(Bitmap bmp, boolean recycle) {
        if (bmp == null)
            return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(recycle) {
                try {
                    if (bmp != null && !bmp.isRecycled())
                        bmp.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 将Bitmap保存在文件里
     *
     * @param bmp Bitmap
     * @param quality 质量 0-100
     * @param file 保存的文件
     */
    public static void saveImage2File(Bitmap bmp, int quality,  File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存图片到系统相册里
     *
     * @param context Context
     * @param bitmap Bitmap
     * @param filePath 保存的文件路径
     *
     * @throws IOException 保存失败
     */
    public static void saveImage2Gallery(Context context, Bitmap bitmap, String filePath) throws IOException {
        File file = new File(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        updateSystemGallery(context, file);
        bos.flush();
        bos.close();
    }

    /**
     * 通知系统相册，扫描新图片
     *
     * @param context Context
     * @param file 图片文件
     */
    public static void updateSystemGallery(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /**
     * 获取图片的宽、高
     *
     * @param file 图片文件路径
     *
     * @return [0]:width, [1]:height
     */
    public static int[] getImageWidthAndHeight(String file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);
        return new int[]{options.outWidth, options.outHeight};
    }
}