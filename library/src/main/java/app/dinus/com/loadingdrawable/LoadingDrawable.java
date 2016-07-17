package app.dinus.com.loadingdrawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class LoadingDrawable extends Drawable implements Animatable {
  private LoadingRenderer mLoadingRender;

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
  public void draw(Canvas canvas) {
    mLoadingRender.draw(canvas, getBounds());
  }

  @Override
  public void setAlpha(int alpha) {
    mLoadingRender.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    mLoadingRender.setColorFilter(cf);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void start() {
    mLoadingRender.start();
  }

  @Override
  public void stop() {
    mLoadingRender.stop();
  }

  @Override
  public boolean isRunning() {
    return mLoadingRender.isRunning();
  }

  @Override
  public int getIntrinsicHeight() {
    return (int) mLoadingRender.getHeight();
  }

  @Override
  public int getIntrinsicWidth() {
    return (int) mLoadingRender.getWidth();
  }
}
