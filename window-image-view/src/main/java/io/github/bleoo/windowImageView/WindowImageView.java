package io.github.bleoo.windowImageView;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.interfaces.DraweeController;

/**
 * Created by bleoo on 2017/11/1.
 */

public class WindowImageView extends View {

    private static final String TAG = "WindowImageView";

    private Context mContext;
    private int resId;
    private boolean frescoEnable;

    private float mMimDisPlayTop;   // min draw top
    private float disPlayTop;       // current draw top
    private int[] location;         // location in window

    private int rescaleHeight;
    private int realWidth;

    private boolean isMeasured;

    private DrawableController mDrawableController;

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
            resId = typedArray.getResourceId(R.styleable.WindowImageView_src, 0);
            frescoEnable = typedArray.getBoolean(R.styleable.WindowImageView_frescoEnable, false);
            typedArray.recycle();
        }
        location = new int[2];

        mDrawableController = new DrawableController(mContext, this);
        mDrawableController.setFrescoEnable(frescoEnable);
        mDrawableController.setProcessListener(new ProcessListener() {
            @Override
            public void onProcessFinished(int width, int height) {
                rescaleHeight = height;
                resetTransMultiple(height);
                getLocationInWindow(location);
                disPlayTop = -(location[1] - rvLocation[1]) * translationMultiple;
                boundTop();
                post(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                });
            }
        });

        // here has a bug: post runnable execute ahead of onMeasure
        // post runnable 先于 onMeasure 执行
        // When looper calls it, view has been initialized
//        post(new Runnable() {
//            @Override
//            public void run() {
//                Log("post runnable, isMeasured:" + isMeasured);
//                if (isMeasured) {
//                    mDrawableController.process();
//                }
//            }
//        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        realWidth = measureHandle(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = measureHandle(getSuggestedMinimumHeight(), heightMeasureSpec);
        Log("width : " + realWidth + ", height: " + height);
        setMeasuredDimension(realWidth, height);

        isMeasured = true;
        mDrawableController.process();
    }

    public int getRealWidth() {
        return realWidth;
    }

    private int measureHandle(int defaultSize, int measureSpec) {
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

        Drawable drawable = mDrawableController.getTargetDrawable();
        if (drawable != null) {
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap().isRecycled()) {
                    return;
                }
            }
            canvas.save();
            canvas.translate(0, disPlayTop);
            drawable.setBounds(0, 0, getWidth(), rescaleHeight);
            drawable.draw(canvas);
            canvas.restore();
        }
    }

    private void boundTop() {
        if (disPlayTop > 0) {
            disPlayTop = 0;
        }
        if (disPlayTop < mMimDisPlayTop) {
            disPlayTop = mMimDisPlayTop;
        }
    }

    public void setImageResource(@DrawableRes int resId) {
        this.resId = resId;
        if (isMeasured && mDrawableController != null && !frescoEnable) {
            mDrawableController.process();
        }
    }

    public int getImageResource() {
        return resId;
    }

    /*
        ----------------------------- 以下为 fresco -----------------------------
     */

    private Uri resUri;

    public void setDraweeController(DraweeController controller) {
        mDrawableController.setDraweeController(controller);
    }

    public DraweeController getDraweeController() {
        return mDrawableController.getDraweeController();
    }

    public void setImageURI(Uri uri) {
        resUri = uri;
        if (isMeasured && mDrawableController != null && frescoEnable) {
            mDrawableController.process();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDrawableController.doDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        mDrawableController.doDetach();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDrawableController.doAttach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        mDrawableController.doAttach();
    }

    public Uri getUri() {
        if (resUri == null) {
            if (resId == 0) {
                return null;
            }
            return UriUtil.getUriForResourceId(resId);
        }
        return resUri;
    }

    public void setFrescoEnable(boolean enable) {
        frescoEnable = enable;
        if (mDrawableController != null) {
            mDrawableController.setFrescoEnable(frescoEnable);
        }
    }

    // ----------------------------- RecyclerView bind -----------------------------

    private RecyclerView recyclerView;
    private RecyclerView.OnScrollListener rvScrollListener;
    private float translationMultiple = 1.0f;
    private int[] rvLocation;
    private int rvHeight;

    public void bindRecyclerView(RecyclerView recyclerView) {
        if (recyclerView == null || recyclerView.equals(this.recyclerView)) {
            return;
        }
        unbindRecyclerView();
        this.recyclerView = recyclerView;
        rvLocation = new int[2];
        rvHeight = recyclerView.getLayoutManager().getHeight();
        recyclerView.getLocationInWindow(rvLocation);
        recyclerView.addOnScrollListener(rvScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (getTopDistance() > 0 && getTopDistance() + getHeight() < rvHeight) {
                    disPlayTop += dy * translationMultiple;
                    boundTop();
                    if (isMeasured) {
                        invalidate();
                    }
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

    private void resetTransMultiple(int scaledHeight) {
        if (recyclerView != null) {
            /*
                |------------------------| recyclerView
                |----| item
                     |-------------------| can move length : recyclerViewHeight - thisHeight

                |----------------| bitmap
              or
                |-----------------------------| bitmap

                bitmap draw top : 0 ~ bitmapHeight - thisHeight
             */
            mMimDisPlayTop = -scaledHeight + getHeight();
            translationMultiple = 1.0f * -mMimDisPlayTop / (rvHeight - getHeight());
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
        getLocationInWindow(location);
        return location[1] - rvLocation[1];
    }

    private void Log(String msg) {
        Log.e(TAG, msg);
    }
}
