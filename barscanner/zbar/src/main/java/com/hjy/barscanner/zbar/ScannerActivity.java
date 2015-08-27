package com.hjy.barscanner.zbar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;


public class ScannerActivity extends Activity implements ZBarScannerView.ResultHandler, View.OnClickListener {

    private static String TAG = "ScannerActivity";
    private static final int REQ_CODE_SELECT_PHOTO = 10;

    private ZBarScannerView mScannerView;

    private ImageView mImgTorch;
    private boolean mTorchOn = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.hjy_activity_zbar_scanner);
        mScannerView = (ZBarScannerView) findViewById(R.id.ZBarScannerView);

        mScannerView.setupLayout();
        mScannerView.setupScanner();

        findViewById(R.id.ImageButton_Scanner_Back).setOnClickListener(this);
        mImgTorch = (ImageView) findViewById(R.id.ImageView_Scanner_Torch);
        mImgTorch.setOnClickListener(this);
        findViewById(R.id.Button_Scanner_Gallery).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.adb
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        if(rawResult == null || TextUtils.isEmpty(rawResult.getContents())) {
            Toast.makeText(this, "扫描失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, rawResult.getContents() + "," + rawResult.getBarcodeFormat().getName(), Toast.LENGTH_SHORT).show();
        }
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
                            int sampleSize = (int) (minLen / (float) 800);
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

                            byte[] yuvData = Utils.rgb2YCbCr420(pixels, w, h);
                            Image barcode = new Image(w, h, "Y800");
                            barcode.setData(yuvData);

                            ImageScanner scanner = mScannerView.getScanner();
                            int result = scanner.scanImage(barcode);
                            if (result != 0) {
                                SymbolSet syms = scanner.getResults();
                                Result rawResult = new Result();
                                for (Symbol sym : syms) {
                                    String symData = sym.getData();
                                    if (!TextUtils.isEmpty(symData)) {
                                        rawResult.setContents(symData);
                                        rawResult.setBarcodeFormat(BarcodeFormat.getFormatById(sym.getType()));
                                        break;
                                    }
                                }
                                handleResult(rawResult);
                            } else {
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
