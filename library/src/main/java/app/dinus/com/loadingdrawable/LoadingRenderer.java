package app.dinus.com.loadingdrawable;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public abstract class LoadingRenderer {
  private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

  private static final int ANIMATION_DURATION = 1333;

  private static final float DEFAULT_SIZE = 56.0f;
  private static final float DEFAULT_CENTER_RADIUS = 12.5f;
  private static final float DEFAULT_STROKE_WIDTH = 2.5f;

  protected float mWidth;
  protected float mHeight;
  protected float mStrokeWidth;
  protected float mCenterRadius;

  private Drawable.Callback mCallback;
  private ValueAnimator mRenderAnimator;

  public LoadingRenderer(Context context) {
    setupDefaultParams(context);
    setupAnimators();
  }

  public abstract void draw(Canvas canvas, Rect bounds);
  public abstract void computeRender(float renderProgress);
  public abstract void setAlpha(int alpha);
  public abstract void setColorFilter(ColorFilter cf);
  public abstract void reset();

  public void start() {
    mRenderAnimator.setDuration(ANIMATION_DURATION);
    mRenderAnimator.start();
  }

  public void stop() {
    mRenderAnimator.cancel();
    reset();
  }

  public boolean isRunning() {
    return mRenderAnimator.isRunning();
  }

  public void setCallback(Drawable.Callback callback) {
    this.mCallback = callback;
  }

  protected void invalidateSelf() {
    mCallback.invalidateDrawable(null);
  }

  private void setupDefaultParams(Context context) {
    final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    final float screenDensity = metrics.density;

    mWidth = DEFAULT_SIZE * screenDensity;
    mHeight = DEFAULT_SIZE * screenDensity;
    mStrokeWidth = DEFAULT_STROKE_WIDTH * screenDensity;
    mCenterRadius = DEFAULT_CENTER_RADIUS * screenDensity;
  }

  private void setupAnimators() {
    mRenderAnimator = ValueAnimator.ofFloat(0, 1);
    mRenderAnimator.setRepeatCount(Animation.INFINITE);
    mRenderAnimator.setRepeatMode(Animation.RESTART);
    mRenderAnimator.setInterpolator(LINEAR_INTERPOLATOR);
    mRenderAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        computeRender((float) animation.getAnimatedValue());
        invalidateSelf();
      }
    });
  }

  protected void addRenderListener(Animator.AnimatorListener animatorListener) {
    mRenderAnimator.addListener(animatorListener);
  }

  public void setCenterRadius(float centerRadius) {
    mCenterRadius = centerRadius;
  }

  public float getCenterRadius() {
    return mCenterRadius;
  }

  public void setStrokeWidth(float strokeWidth) {
    mStrokeWidth = strokeWidth;
  }

  public float getStrokeWidth() {
    return mStrokeWidth;
  }

  public float getWidth() {
    return mWidth;
  }

  public void setWidth(float width) {
    this.mWidth = width;
  }

  public float getHeight() {
    return mHeight;
  }

  public void setHeight(float height) {
    this.mHeight = height;
  }
}
