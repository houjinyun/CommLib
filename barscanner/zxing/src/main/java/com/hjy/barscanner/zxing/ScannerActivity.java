package com.hjy.barscanner.zxing;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;

public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler, View.OnClickListener{

    private static String TAG = "ScannerActivity";
    private static final int REQ_CODE_SELECT_PHOTO = 10;

    private ZXingScannerView mScannerView;

    private ImageView mImgTorch;
    private boolean mTorchOn = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.hjy_activity_zxing_scanner);
        mScannerView = (ZXingScannerView) findViewById(R.id.ZBarScannerView);

        findViewById(R.id.ImageButton_Scanner_Back).setOnClickListener(this);
        mImgTorch = (ImageView) findViewById(R.id.ImageView_Scanner_Torch);
        mImgTorch.setOnClickListener(this);
        findViewById(R.id.Button_Scanner_Gallery).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
/*        if(rawResult == null) {
            Toast.makeText(this, "扫描失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, rawResult.getText() + '，' + rawResult.getBarcodeFormat().toString(),  Toast.LENGTH_LONG).show();
        }*/

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ImageButton_Scanner_Back) {
            finish();
        } else if(v.getId() == R.id.ImageView_Scanner_Torch) {
            if(mTorchOn) {
                mScannerView.setFlash(false);
                mTorchOn = false;
                mImgTorch.setImageResource(R.drawable.barcode_torch_off);
            } else {
                mScannerView.setFlash(true);
                mTorchOn = true;
                mImgTorch.setImageResource(R.drawable.barcode_torch_on);
            }
        } else if(v.getId() == R.id.Button_Scanner_Gallery) {
            pickPhotoFromGallery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_SELECT_PHOTO) {
            if(resultCode == RESULT_OK) {
                if (data == null)
                    return;
                Uri uri = data.getData();
                if (uri != null) {
                    Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION}, null, null, null);
                    try {
                        if(cursor != null && cursor.moveToFirst()) {
                            String path = cursor.getString(0);

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true; // 先获取原大小
                            BitmapFactory.decodeFile(path, options);
                            options.inJustDecodeBounds = false; // 获取新的大小
                            int minLen = Math.min(options.outWidth, options.outHeight);
                            int sampleSize = (int) (minLen / (float) 200);
                            if (sampleSize <= 0)
                                sampleSize = 1;
                            options.inSampleSize = sampleSize;
                            Bitmap bmp = BitmapFactory.decodeFile(path, options);

                            int w = bmp.getWidth();
                            int h = bmp.getHeight();
                            int[] pixels = new int[w * h];
                            bmp.getPixels(pixels, 0, w, 0, 0, w, h);

                            if(bmp != null && !bmp.isRecycled()) {
                                bmp.recycle();
                                bmp = null;
                            }

                            Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
                            hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码
                            RGBLuminanceSource source = new RGBLuminanceSource(w, h, pixels);
                            BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
                            try {
                                Reader reader = mScannerView.getReader();
                                Result result = reader.decode(bitmap1, hints);
                                if(result != null) {
                                    handleResult(result);
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                handleResult(null);
                            }
                        }
                    } finally {
                        if(cursor != null)
                            cursor.close();
                    }
                }
            }
        }
    }

    public void pickPhotoFromGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQ_CODE_SELECT_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
