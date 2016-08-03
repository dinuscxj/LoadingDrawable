package app.dinus.com.loadingdrawable.render;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class LoadingDrawable extends Drawable implements Animatable {
    private final LoadingRenderer mLoadingRender;

    private final Callback mCallback = new Callback() {
        @Override
        public void invalidateDrawable(Drawable d) {
            invalidateSelf();
        }

        @Override
        public void scheduleDrawable(Drawable d, Runnable what, long when) {
            scheduleSelf(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable d, Runnable what) {
            unscheduleSelf(what);
        }
    };

    public LoadingDrawable(LoadingRenderer loadingRender) {
        this.mLoadingRender = loadingRender;
        this.mLoadingRender.setCallback(mCallback);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mLoadingRender.setBounds(bounds);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!getBounds().isEmpty()) {
            this.mLoadingRender.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.mLoadingRender.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        this.mLoadingRender.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void start() {
        this.mLoadingRender.start();
    }

    @Override
    public void stop() {
        this.mLoadingRender.stop();
    }

    @Override
    public boolean isRunning() {
        return this.mLoadingRender.isRunning();
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) this.mLoadingRender.mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) this.mLoadingRender.mWidth;
    }
}
