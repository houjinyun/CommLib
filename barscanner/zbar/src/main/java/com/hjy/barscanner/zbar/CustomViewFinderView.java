package com.hjy.barscanner.zbar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import me.dm7.barcodescanner.core.DisplayUtils;
import me.dm7.barcodescanner.core.IViewFinder;

public class CustomViewFinderView extends View implements IViewFinder {
    private static final String TAG = "ViewFinderView";

    private Rect mFramingRect;

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;

    private static final float LANDSCAPE_WIDTH_RATIO = 5f/8;
    private static final float LANDSCAPE_HEIGHT_RATIO = 5f/8;
    private static final int LANDSCAPE_MAX_FRAME_WIDTH = (int) (1920 * LANDSCAPE_WIDTH_RATIO); // = 5/8 * 1920
    private static final int LANDSCAPE_MAX_FRAME_HEIGHT = (int) (1080 * LANDSCAPE_HEIGHT_RATIO); // = 5/8 * 1080

    private static final float PORTRAIT_WIDTH_RATIO = 6f/8;
    private static final float PORTRAIT_HEIGHT_RATIO = 2.5f/8;
    private static final int PORTRAIT_MAX_FRAME_WIDTH = (int) (1080 * PORTRAIT_WIDTH_RATIO); // = 7/8 * 1080
    private static final int PORTRAIT_MAX_FRAME_HEIGHT = (int) (1920 * PORTRAIT_HEIGHT_RATIO); // = 3/8 * 1920

    private static final int POINT_SIZE = 10;

    private final int mDefaultMaskColor = getResources().getColor(R.color.viewfinder_mask);
    private final int mDefaultBorderColor = getResources().getColor(R.color.viewfinder_border);
    private final int mDefaultBorderStrokeWidth = getResources().getInteger(R.integer.viewfinder_border_width);
    private final int mDefaultBorderLineLength = getResources().getInteger(R.integer.viewfinder_border_length);

    protected Paint mFinderMaskPaint;
    protected Paint mBorderPaint;
    protected int mBorderLineLength;

    private int mLaserTop = 0;

    private boolean mShowLaser = true;

    private Drawable mBarLine;

    public CustomViewFinderView(Context context) {
        super(context);
        init();
    }

    public CustomViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //finder mask paint
        mFinderMaskPaint = new Paint();
        mFinderMaskPaint.setColor(mDefaultMaskColor);

        //border paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(mDefaultBorderColor);
        mBorderPaint.setStyle(Paint.Style.FILL);
  //      mBorderPaint.setStyle(Paint.Style.STROKE);
 //       mBorderPaint.setStrokeWidth(mDefaultBorderStrokeWidth);

        mBorderLineLength = mDefaultBorderLineLength;

        mBarLine = getResources().getDrawable(R.drawable.barcode_laser_line);
    }

    public void setLaserDrawable(Drawable laserDrawable) {
        mBarLine = laserDrawable;
    }

    public void setMaskColor(int maskColor) {
        mFinderMaskPaint.setColor(maskColor);
    }

    public void setBorderColor(int borderColor) {
        mBorderPaint.setColor(borderColor);
    }

    public void setBorderStrokeWidth(int borderStrokeWidth) {
        mBorderPaint.setStrokeWidth(borderStrokeWidth);
    }
    public void setBorderLineLength(int borderLineLength) {
        mBorderLineLength = borderLineLength;
    }

    public void setupViewFinder() {
        updateFramingRect();
        invalidate();
    }

    public Rect getFramingRect() {
        return mFramingRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(mFramingRect == null) {
            return;
        }

        drawViewFinderMask(canvas);
        if(mShowLaser)
            drawLaser(canvas);
        drawViewFinderBorder(canvas);
    }

    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        canvas.drawRect(0, 0, width, mFramingRect.top, mFinderMaskPaint);
        canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom + 1, mFinderMaskPaint);
        canvas.drawRect(mFramingRect.right + 1, mFramingRect.top, width, mFramingRect.bottom + 1, mFinderMaskPaint);
        canvas.drawRect(0, mFramingRect.bottom + 1, width, height, mFinderMaskPaint);
    }

    public void drawViewFinderBorder(Canvas canvas) {
        canvas.drawRect(mFramingRect.left - mDefaultBorderStrokeWidth / 2, mFramingRect.top - mDefaultBorderStrokeWidth / 2, mFramingRect.left + mDefaultBorderStrokeWidth / 2, mFramingRect.top - mDefaultBorderStrokeWidth / 2 + mBorderLineLength, mBorderPaint);
        canvas.drawRect(mFramingRect.left - mDefaultBorderStrokeWidth / 2, mFramingRect.top - mDefaultBorderStrokeWidth / 2, mFramingRect.left - mDefaultBorderStrokeWidth / 2 + mBorderLineLength, mFramingRect.top + mDefaultBorderStrokeWidth/2, mBorderPaint);

        canvas.drawRect(mFramingRect.right - mDefaultBorderStrokeWidth / 2, mFramingRect.top - mDefaultBorderStrokeWidth / 2, mFramingRect.right + mDefaultBorderStrokeWidth / 2, mFramingRect.top - mDefaultBorderStrokeWidth/2 + mBorderLineLength, mBorderPaint);
        canvas.drawRect(mFramingRect.right + mDefaultBorderStrokeWidth / 2 - mBorderLineLength, mFramingRect.top - mDefaultBorderStrokeWidth / 2, mFramingRect.right + mDefaultBorderStrokeWidth / 2, mFramingRect.top + mDefaultBorderStrokeWidth/2, mBorderPaint);

        canvas.drawRect(mFramingRect.left - mDefaultBorderStrokeWidth / 2, mFramingRect.bottom + mDefaultBorderStrokeWidth / 2 - mBorderLineLength, mFramingRect.left + mDefaultBorderStrokeWidth / 2, mFramingRect.bottom + mDefaultBorderStrokeWidth / 2, mBorderPaint);
        canvas.drawRect(mFramingRect.left - mDefaultBorderStrokeWidth / 2, mFramingRect.bottom - mDefaultBorderStrokeWidth / 2, mFramingRect.left - mDefaultBorderStrokeWidth / 2 + mBorderLineLength, mFramingRect.bottom + mDefaultBorderStrokeWidth/2, mBorderPaint);

        canvas.drawRect(mFramingRect.right - mDefaultBorderStrokeWidth / 2, mFramingRect.bottom + mDefaultBorderStrokeWidth / 2 - mBorderLineLength, mFramingRect.right + mDefaultBorderStrokeWidth / 2, mFramingRect.bottom + mDefaultBorderStrokeWidth/2 , mBorderPaint);
        canvas.drawRect(mFramingRect.right + mDefaultBorderStrokeWidth / 2 - mBorderLineLength, mFramingRect.bottom - mDefaultBorderStrokeWidth / 2, mFramingRect.right + mDefaultBorderStrokeWidth / 2, mFramingRect.bottom + mDefaultBorderStrokeWidth/2, mBorderPaint);
    }

    public void drawLaser(Canvas canvas) {

        mBarLine.setBounds(mFramingRect);
        canvas.save();
        canvas.clipRect(mFramingRect);
        canvas.translate(0, mLaserTop - mFramingRect.height());

        mBarLine.draw(canvas);

        canvas.restore();

        mLaserTop += 5;
        if(mLaserTop > mFramingRect.height())
            mLaserTop = 0;
        invalidate(mFramingRect.left - POINT_SIZE,
                mFramingRect.top - POINT_SIZE,
                mFramingRect.right + POINT_SIZE,
                mFramingRect.bottom + POINT_SIZE);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    public synchronized void updateFramingRect() {
        Point viewResolution = new Point(getWidth(), getHeight());
        int width;
        int height;
        int orientation = DisplayUtils.getScreenOrientation(getContext());

        if(orientation != Configuration.ORIENTATION_PORTRAIT) {
            width = findDesiredDimensionInRange(LANDSCAPE_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, LANDSCAPE_MAX_FRAME_WIDTH);
            height = findDesiredDimensionInRange(LANDSCAPE_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, LANDSCAPE_MAX_FRAME_HEIGHT);
        } else {
            width = findDesiredDimensionInRange(PORTRAIT_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, PORTRAIT_MAX_FRAME_WIDTH);
            height = findDesiredDimensionInRange(PORTRAIT_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, PORTRAIT_MAX_FRAME_HEIGHT);
        }

        int leftOffset = (viewResolution.x - width) / 2;
        int topOffset = (viewResolution.y - height) / 2;
        mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
    }

    private static int findDesiredDimensionInRange(float ratio, int resolution, int hardMin, int hardMax) {
        int dim = (int) (ratio * resolution);
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    @Override
    public void restartFind() {
        mShowLaser = true;
        mLaserTop = 0;
        invalidate();
    }

    @Override
    public void stopFind() {
        mShowLaser = false;
        invalidate();
    }
}
