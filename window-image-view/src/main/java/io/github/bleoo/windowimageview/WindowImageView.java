package io.github.bleoo.windowimageview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by bleoo on 2017/11/1.
 */

public class WindowImageView extends View {

    private static final String TAG = "WindowImageView";

    private Context mContext;
    private Paint mPaint;
    private Bitmap mBitmap;         // bitmap before scale
    private Bitmap mScaleBitmap;    // bitmap after scale
    private Matrix mMatrix;

    private float mMimDisPlayTop;   // min draw top
    private float disPlayTop;       // current draw top
    private int[] location;

    public WindowImageView(Context context) {
        super(context);
        init(context, null);
    }

    public WindowImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WindowImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WindowImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WindowImageView);
            int resId = typedArray.getResourceId(R.styleable.WindowImageView_src, 0);
            setImageBitmap(BitmapFactory.decodeResource(context.getResources(), resId));
            typedArray.recycle();
        }
        mPaint = new Paint();
        mMatrix = new Matrix();
        location = new int[2];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = measureHanlder(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = measureHanlder(getSuggestedMinimumHeight(), heightMeasureSpec);
        Log("width : " + width + ", height: " + height);
        setMeasuredDimension(width, height);
    }

    private int measureHanlder(int defaultSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY || specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else {
            result = defaultSize;
        }
        return result;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        reScaleBitmap();

        if (mScaleBitmap != null) {
            canvas.save();
            canvas.drawBitmap(mScaleBitmap, 0, disPlayTop, mPaint);
            Log("displayTop : " + disPlayTop);
            canvas.restore();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        if (mScaleBitmap != null) {
            mScaleBitmap.recycle();
        }
        super.finalize();
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        invalidate();
    }

    private void reScaleBitmap() {
        if (mBitmap == null) {
            return;
        }
        float scale = 1.0f * getWidth() / mBitmap.getWidth();
        Log.e(TAG, "scale : " + scale);
        mMatrix.reset();
        mMatrix.postScale(scale, scale);
        mScaleBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), mMatrix, true);
        mBitmap.recycle();
        mBitmap = null;
        resetTransMultiple();
        disPlayTop -= (location[1] - rvLocation[1]) * translationMultiple;
        boundTop();
    }

    private void boundTop() {
        if (disPlayTop > 0) {
            disPlayTop = 0;
        }
        if (disPlayTop < mMimDisPlayTop) {
            disPlayTop = mMimDisPlayTop;
        }
    }

    // ----------------------------------- bind -------------------------------------------

    private RecyclerView recyclerView;
    private RecyclerView.OnScrollListener rvScrollListener;
    private float translationMultiple = 1.0f;
    private int[] rvLocation;

    public void bindRecyclerView(RecyclerView recyclerView, int position) {
        this.recyclerView = recyclerView;
        rvLocation = new int[2];
        recyclerView.getLocationInWindow(rvLocation);
        recyclerView.addOnScrollListener(rvScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e(TAG, "dx : " + dx + ", dy : " + dy);
                getLocationInWindow(location);
                if (getTopDistance() > 0 && getTopDistance() + getHeight() < recyclerView.getBottom()) {
                    disPlayTop += dy * translationMultiple;
                    boundTop();
                    invalidate();
                }
            }
        });
    }

    public void unbindRecyclerView() {
        if (recyclerView != null) {
            if (rvScrollListener != null) {
                recyclerView.removeOnScrollListener(rvScrollListener);
            }
            recyclerView = null;
        }
    }

    private void resetTransMultiple() {
        if (recyclerView != null && mScaleBitmap != null) {
            int height = recyclerView.getBottom() - recyclerView.getTop();
            Log("getBottom() : " + recyclerView.getBottom() + ", getTop() : " + recyclerView.getTop());
            Log("height : " + height);
            /*
                |------------------------| recyclerView
                |----| item
                     |-------------------| can move length : recyclerViewHeight - thisHeight

                |----------------| bitmap
              or
                |-----------------------------| bitmap

                bitmap draw top : 0 ~ bitmapHeight - thisHeight
             */
            mMimDisPlayTop = -mScaleBitmap.getHeight() + getHeight();
            translationMultiple = 1.0f * -mMimDisPlayTop / (height - getHeight());
            Log("translationMultiple : " + translationMultiple);
        }
    }

    /**
     * Distance to RecyclerView
     * Calculate by window location
     *
     * @return
     */
    private int getTopDistance() {
        return location[1] - rvLocation[1];
    }

    private void Log(String msg) {
        Log.d(TAG, msg);
    }
}
