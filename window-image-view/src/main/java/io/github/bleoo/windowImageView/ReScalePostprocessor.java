package io.github.bleoo.windowImageView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * Created by bleoo on 2017/11/13.
 */

public class ReScalePostprocessor extends BasePostprocessor {

    private int width;
    private ProcessListener listener;

    private int scaledWidth;
    private int scaledHeight;

    public ReScalePostprocessor(int width, ProcessListener listener) {
        this.width = width;
        this.listener = listener;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        float scale = 1.0f * width / sourceBitmap.getWidth();
        Log.e("ReScalePostprocessor", "scale:" + scale);

        scaledWidth = (int) (sourceBitmap.getWidth() * scale);
        scaledHeight = (int) (sourceBitmap.getHeight() * scale);

        listener.onProcessFinished(scaledWidth, scaledHeight);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
        CloseableReference<Bitmap> bitmapRef = bitmapFactory.createBitmap(bitmap);
        try {
            return CloseableReference.cloneOrNull(bitmapRef);
        } finally {
            CloseableReference.closeSafely(bitmapRef);
        }
    }
}
