package io.github.bleoo.windowImageView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created by bleoo on 2017/11/14.
 */

public class DrawableController {

    private static final String TAG = "DrawableController";

    private Context mContext;
    private WindowImageView mView;

    private Drawable sourceDrawable;
    private Bitmap sourceBitmap;
    private Drawable targetDrawable;
    private Bitmap targetBitmap;

    private Matrix mMatrix;
    private int processedWidth;
    private int processedHeight;

    private boolean frescoEnable;
    private DraweeHolder<GenericDraweeHierarchy> mDraweeHolder;
    private ReScalePostprocessor reScalePostprocessor;

    private ProcessListener listener;

    // status
    private boolean isProcessing;
    private boolean hasNewProcessIn;

    public DrawableController(Context context, WindowImageView view) {
        mContext = context;
        mView = view;

        mMatrix = new Matrix();
    }

    public void process() {
        if (frescoEnable) {
            Uri uri = mView.getUri();
            if (uri == null) {
                return;
            }
            initDraweeHolder();
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(mView.getUri())
                    .setPostprocessor(getReScalePostprocessor())
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(getDraweeController())
                    .build();
            setDraweeController(controller);
        } else {
            if (isProcessing) {
                return;
            }
            isProcessing = true;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    int drawableResId = mView.getImageResource();
                    if (drawableResId == 0) {
                        isProcessing = false;
                        return;
                    }

                    Resources resources = mContext.getResources();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeResource(resources, drawableResId, options);

                    // options.outWidth is dp, need do dp -> px
                    int outWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, options.outWidth, resources.getDisplayMetrics());
                    int outHeightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, options.outHeight, resources.getDisplayMetrics());

                    float scale = 1.0f * mView.getRealWidth() / outWidthPx;
                    Log.e(TAG, "mView.getWidth() : " + mView.getRealWidth());
                    Log.e(TAG, "options.outWidth : " + outWidthPx);
                    Log.e(TAG, "scale : " + scale);
                    processedWidth = (int) (scale * outWidthPx);
                    processedHeight = (int) (scale * outHeightPx);

                    options.inSampleSize = calculateInSampleSize(outWidthPx, outHeightPx, processedWidth, processedHeight);
                    options.inJustDecodeBounds = false;
                    // 低版本有全黑问题
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
//                    }

                    if (mView == null) {
                        isProcessing = false;
                        return;
                    }

                    sourceBitmap = BitmapFactory.decodeResource(resources, drawableResId, options);

                    mMatrix.reset();
                    mMatrix.postScale(scale, scale);
                    Log.e(TAG, "sourceBitmap.getWidth(): " + sourceBitmap.getWidth());
                    Log.e(TAG, "sourceBitmap.getHeight(): " + sourceBitmap.getHeight());

                    if (mView == null) {
                        isProcessing = false;
                        return;
                    }

                    targetBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), mMatrix, true);
                    releaseTargetDrawable();
                    targetDrawable = new BitmapDrawable(targetBitmap);

                    listener.onProcessFinished(processedWidth, processedHeight);

                    if (sourceBitmap != null) {
                        sourceBitmap.recycle();
                        sourceBitmap = null;
                    }

                    isProcessing = false;
                }
            }).start();
        }
    }

    private void initDraweeHolder() {
        if (mDraweeHolder == null) {
            GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(mContext.getResources())
                    .build();
            mDraweeHolder = DraweeHolder.create(hierarchy, mContext);
        }
    }

    private void releaseTargetDrawable() {
        if (!frescoEnable && targetDrawable != null && targetDrawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) targetDrawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (!bitmap.isRecycled()) {
                bitmap.isRecycled();
            }
            targetDrawable = null;
        }
    }

    public Drawable getTargetDrawable() {
        if (frescoEnable) {
            if (mDraweeHolder != null) {
                return mDraweeHolder.getTopLevelDrawable();
            } else {
                return null;
            }
        } else {
            return targetDrawable;
        }
    }

    public void setProcessListener(ProcessListener listener) {
        this.listener = listener;
    }

    public void setFrescoEnable(boolean enable) {
        frescoEnable = enable;
        initDraweeHolder();
    }

    private ReScalePostprocessor getReScalePostprocessor() {
        if (reScalePostprocessor == null) {
            reScalePostprocessor = new ReScalePostprocessor(mView.getRealWidth(), listener);
        }
        return reScalePostprocessor;
    }

    public void setDraweeController(DraweeController controller) {
        if (mDraweeHolder == null) {
            return;
        }
        mDraweeHolder.setController(controller);
    }

    public DraweeController getDraweeController() {
        if (mDraweeHolder == null) {
            return null;
        }
        return mDraweeHolder.getController();
    }

    public void doDetach() {
        if (mDraweeHolder != null) {
            mDraweeHolder.onDetach();
        }
    }

    public void doAttach() {
        if (mDraweeHolder != null) {
            mDraweeHolder.onAttach();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (sourceBitmap != null) {
            sourceBitmap.recycle();
            sourceBitmap = null;
        }
        if (targetBitmap != null) {
            targetBitmap.recycle();
            targetBitmap = null;
        }
        super.finalize();
    }

    private int calculateInSampleSize(int sourceWidth, int sourceHeight, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (sourceWidth > reqWidth || sourceHeight > reqHeight) {
            int halfWidth = sourceWidth / 2;
            int halfHeight = sourceHeight / 2;
            while ((halfWidth / inSampleSize > reqWidth)
                    && (halfHeight / inSampleSize > reqHeight)) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
