package app.dinus.com.loadingdrawable.render;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import app.dinus.com.loadingdrawable.DensityUtil;

public abstract class LoadingRenderer {
    private static final long ANIMATION_DURATION = 1333;
    private static final float DEFAULT_SIZE = 56.0f;

    private final ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener
            = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            computeRender((float) animation.getAnimatedValue());
            invalidateSelf();
        }
    };

    /**
     * Whenever {@link LoadingDrawable} boundary changes mBounds will be updated.
     * More details you can see {@link LoadingDrawable#onBoundsChange(Rect)}
     */
    protected final Rect mBounds = new Rect();

    private Drawable.Callback mCallback;
    private ValueAnimator mRenderAnimator;

    protected long mDuration;

    protected float mWidth;
    protected float mHeight;

    public LoadingRenderer(Context context) {
        initParams(context);
        setupAnimators();
    }

    @Deprecated
    protected void draw(Canvas canvas, Rect bounds) {
    }

    protected void draw(Canvas canvas) {
        draw(canvas, mBounds);
    }

    protected abstract void computeRender(float renderProgress);

    protected abstract void setAlpha(int alpha);

    protected abstract void setColorFilter(ColorFilter cf);

    protected abstract void reset();

    protected void addRenderListener(Animator.AnimatorListener animatorListener) {
        mRenderAnimator.addListener(animatorListener);
    }

    void start() {
        reset();
        mRenderAnimator.addUpdateListener(mAnimatorUpdateListener);

        mRenderAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRenderAnimator.setDuration(mDuration);
        mRenderAnimator.start();
    }

    void stop() {
        // if I just call mRenderAnimator.end(),
        // it will always call the method onAnimationUpdate(ValueAnimator animation)
        // why ? if you know why please send email to me (dinus_developer@163.com)
        mRenderAnimator.removeUpdateListener(mAnimatorUpdateListener);

        mRenderAnimator.setRepeatCount(0);
        mRenderAnimator.setDuration(0);
        mRenderAnimator.end();
    }

    boolean isRunning() {
        return mRenderAnimator.isRunning();
    }

    void setCallback(Drawable.Callback callback) {
        this.mCallback = callback;
    }

    void setBounds(Rect bounds) {
        mBounds.set(bounds);
    }

    private void initParams(Context context) {
        mWidth = DensityUtil.dip2px(context, DEFAULT_SIZE);
        mHeight = DensityUtil.dip2px(context, DEFAULT_SIZE);

        mDuration = ANIMATION_DURATION;
    }

    private void setupAnimators() {
        mRenderAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        mRenderAnimator.setRepeatCount(Animation.INFINITE);
        mRenderAnimator.setRepeatMode(Animation.RESTART);
        mRenderAnimator.setDuration(mDuration);
        //fuck you! the default interpolator is AccelerateDecelerateInterpolator
        mRenderAnimator.setInterpolator(new LinearInterpolator());
        mRenderAnimator.addUpdateListener(mAnimatorUpdateListener);
    }

    private void invalidateSelf() {
        mCallback.invalidateDrawable(null);
    }

}
