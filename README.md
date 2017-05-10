##关于
    用于Android开发的一些常用工具类
    Licence: Apache-2.0

##功能
1. 一些常用的加密算法：AES, DES, RSA, MD5等
2. 使用ZBar进行二维码、条形码等扫描
3. 使用ZXing进行二维码、条形码扫描
4. 图片处理工具类，以及拍照或者从相册里选择照片

##怎样使用
###使用加密算法库:
1). 在build.gradle里添加如下依赖

	compile 'com.hjy.library:encryption:1.0.4'

###使用ZBar进行条形码扫描
		
1). 在build.gradle里添加如下依赖

	compile 'com.hjy.library:zbar:1.0.0'	
2). 在AndroidManifest.xml里添加相机权限

	<uses-permission android:name="android.permission.CAMERA" />
3). 最基本的Activity代码如下：

	public class MainActivity extends ScannerActivity {

    	@Override
    	protected void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
    	}

    	@Override
    	public void handleResult(Result rawResult) {
        	super.handleResult(rawResult);
        	if(rawResult == null) {
        		//扫描失败
        	} else {
        		//扫描成功
        	}
    	}
	}
		

###使用ZXing进行条形码扫描
1). 在build.gradle里添加如下依赖

	compile 'com.hjy.library:zxing:1.0.0'	
2). 在AndroidManifest.xml里添加相机权限

	<uses-permission android:name="android.permission.CAMERA" />
3). 最基本的Activity代码如下：

	public class MainActivity extends ScannerActivity {

    	@Override
    	protected void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
    	}

    	@Override
    	public void handleResult(Result rawResult) {
        	super.handleResult(rawResult);
        	if(rawResult == null) {
        		//扫描失败
        	} else {
        		//扫描成功
        	}
    	}
	}

###图片处理工具类
1). 在build.gradle里添加如下依赖

	compile 'com.hjy.library:imagelibs:1.0.0'
	compile "com.android.support:support-v4:21.0.3"

2). ImageUtil.java工具类
	
	//解析出符合特定尺寸的图片，并且考虑图片的选择角度等信息
	public static Bitmap decodeFileAndConsiderExif(String file, int minSideLength, int maxNumOfPixels)
	
	//将图片保存到文件里，并将其加入到系统相册当中
	public static void saveImage2Gallery(Context context, Bitmap bitmap, String filePath)
	
	//通知系统相册，扫描该图片
	public static void updateSystemGallery(Context context, File file)	
	
	//获取图片的宽高，以二维数组形式返回
	public static int[] getImageWidthAndHeight(String file)

	//将图片保存到文件里
	public static void saveImage2File(Bitmap bmp, int quality,  File file)
	
	//将图片转换成byte数组
	public static byte[] convertBmp2Bytes(Bitmap bmp, boolean recycle)


3). 使用PhotoSelectHandler.java
	
该类主要功能有三个：

* 通过拍照来获取图片
* 从相册里选择图片
* 把选择的图片根据制定尺寸裁减后返回

基本使用方法如下：

	public class MainActivity extends Activity {

    private PhotoSelectHelper mPhotoHandler;

    private ImageView mImgPic;
    private Bitmap mBmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImgPic = (ImageView) findViewById(R.id.ImageView_Main);

        mPhotoHandler = new PhotoSelectHelper(this, new File(Environment.getExternalStorageDirectory(), "hjylib"));
        //设置图片需要的裁减尺寸，如不需要裁减，则不用调用此方法
        mPhotoHandler.setCropWidthAndHeight(150, 150);
        //设置图片选择监听
        mPhotoHandler.setOnPhotoSelectListener(new PhotoSelectHelper.OnPhotoSelectListener() {
            @Override
            public void onPhotoSelectSucc(Intent intent, File file) {
                if (mBmp != null && !mBmp.isRecycled())
                    mBmp.recycle();
                mBmp = BitmapFactory.decodeFile(file.getAbsolutePath(), null);
                mImgPic.setImageBitmap(mBmp);
            }
    
            @Override
            public void onPhotoSelectFail(int errType, String msg) {
                Log.d("MainActivity", "errType = " + errType + "\n" + msg);
                Toast.makeText(MainActivity.this, "处理失败", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.Button_TakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoHandler.takePhoto();
            }
        });

        findViewById(R.id.Button_GetPic_From_Gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoHandler.pickPhotoFromGallery();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mPhotoHandler.onActivityResult(requestCode, resultCode, data)) {

        }
    }
    }






