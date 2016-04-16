package app.dinus.com.loadingdrawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.Interpolator;

public class MaterialLoadingRenderer extends LoadingRenderer {
  private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();

  private static final float FULL_ROTATION = 1080.0f;
  private static final float ROTATION_FACTOR = 0.25f;
  private static final float MAX_PROGRESS_ARC = 0.8f;
  private static final float COLOR_START_DELAY_OFFSET = 0.8f;
  private static final float START_TRIM_DURATION_OFFSET = 0.5f;
  private static final float END_TRIM_START_DELAY_OFFSET = 0.5f;

  private static final int DEGREE_360 = 360;
  private static final int NUM_POINTS = 5;

  private static final int[] DEFAULT_COLORS = new int[] {
      Color.RED, Color.GREEN, Color.BLUE
  };

  private final Paint mPaint = new Paint();
  private final RectF mTempBounds = new RectF();

  private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
    @Override
    public void onAnimationRepeat(Animator animator) {
      super.onAnimationRepeat(animator);
      storeOriginals();
      goToNextColor();

      mStartTrim = mEndTrim;
      mRotationCount = (mRotationCount + 1) % (NUM_POINTS);
    }

    @Override
    public void onAnimationStart(Animator animation) {
      super.onAnimationStart(animation);
      mRotationCount = 0;
    }
  };

  private int[] mColors;
  private int mColorIndex;
  private int mCurrentColor;

  private float mStrokeInset;

  private float mEndTrim;
  private float mRotation;
  private float mStartTrim;
  private float mRotationCount;
  private float mGroupRotation;
  private float mOriginEndTrim;
  private float mOriginRotation;
  private float mOriginStartTrim;

  public MaterialLoadingRenderer(Context context) {
    super(context);
    setupPaint();
    addRenderListener(mAnimatorListener);
  }

  private void setupPaint() {
    mColors = DEFAULT_COLORS;

    mPaint.setAntiAlias(true);
    mPaint.setStrokeWidth(getStrokeWidth());
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeCap(Paint.Cap.ROUND);

    setColorIndex(0);
    setInsets((int) getWidth(), (int) getHeight());
  }

  @Override
  public void draw(Canvas canvas, Rect bounds) {
    mPaint.setColor(mCurrentColor);

    int saveCount = canvas.save();

    canvas.rotate(mGroupRotation, bounds.exactCenterX(), bounds.exactCenterY());
    RectF arcBounds = mTempBounds;
    arcBounds.set(bounds);
    arcBounds.inset(mStrokeInset, mStrokeInset);

    if (mStartTrim == mEndTrim) {
      mStartTrim = mEndTrim + getMinProgressArc();
    }

    float startAngle = (mStartTrim + mRotation) * DEGREE_360;
    float endAngle = (mEndTrim + mRotation) * DEGREE_360;
    float sweepAngle = endAngle - startAngle;

    canvas.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint);

    canvas.restoreToCount(saveCount);
  }

  @Override
  public void computeRender(float renderProgress) {
    final float minProgressArc = getMinProgressArc();
    final float originEndTrim = mOriginEndTrim;
    final float originStartTrim = mOriginStartTrim;
    final float originRotation = mOriginRotation;

    updateRingColor(renderProgress);

    // Moving the start trim only occurs in the first 50% of a
    // single ring animation
    if (renderProgress <= START_TRIM_DURATION_OFFSET) {
      float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
      mStartTrim = originStartTrim + ((MAX_PROGRESS_ARC - minProgressArc) * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress));
    }

    // Moving the end trim starts after 50% of a single ring
    // animation completes
    if (renderProgress > END_TRIM_START_DELAY_OFFSET) {
      float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (1.0f - START_TRIM_DURATION_OFFSET);
      mEndTrim = originEndTrim + ((MAX_PROGRESS_ARC - minProgressArc) * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress));
    }

    mGroupRotation = ((FULL_ROTATION / NUM_POINTS) * renderProgress) + (FULL_ROTATION * (mRotationCount / NUM_POINTS));
    mRotation = originRotation + (ROTATION_FACTOR * renderProgress);

    invalidateSelf();
  }

  @Override
  public void setAlpha(int alpha) {
    mPaint.setAlpha(alpha);
    invalidateSelf();
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    mPaint.setColorFilter(cf);
    invalidateSelf();
  }

  @Override
  public void reset() {
    resetOriginals();
  }

  public void setColors(@NonNull int[] colors) {
    mColors = colors;
    setColorIndex(0);
  }

  public void setColor(int color) {
    mCurrentColor = color;
  }

  public void setColorIndex(int index) {
    mColorIndex = index;
    mCurrentColor = mColors[mColorIndex];
  }

  public int getNextColor() {
    return mColors[getNextColorIndex()];
  }

  private int getNextColorIndex() {
    return (mColorIndex + 1) % (mColors.length);
  }

  public void goToNextColor() {
    setColorIndex(getNextColorIndex());
  }

  @Override
  public void setStrokeWidth(float strokeWidth) {
    super.setStrokeWidth(strokeWidth);
    mPaint.setStrokeWidth(strokeWidth);
    invalidateSelf();
  }

  public void setStartTrim(float startTrim) {
    mStartTrim = startTrim;
    invalidateSelf();
  }

  public float getStartTrim() {
    return mStartTrim;
  }

  public void setEndTrim(float endTrim) {
    mEndTrim = endTrim;
    invalidateSelf();
  }

  public float getEndTrim() {
    return mEndTrim;
  }

  public void setRotation(float rotation) {
    mRotation = rotation;
    invalidateSelf();
  }

  public float getRotation() {
    return mRotation;
  }

  public void setInsets(int width, int height) {
    final float minEdge = (float) Math.min(width, height);
    float insets;
    if (getCenterRadius() <= 0 || minEdge < 0) {
      insets = (float) Math.ceil(getStrokeWidth() / 2.0f);
    } else {
      insets = minEdge / 2.0f - getCenterRadius();
    }
    mStrokeInset = insets;
  }

  private void storeOriginals() {
    mOriginStartTrim = mStartTrim;
    mOriginEndTrim = mEndTrim;
    mOriginRotation = mRotation;
  }

  private void resetOriginals() {
    mOriginStartTrim = 0;
    mOriginEndTrim = 0;
    mOriginRotation = 0;
    setStartTrim(0);
    setEndTrim(0);
    setRotation(0);
  }

  private int getStartingColor() {
    return mColors[mColorIndex];
  }

  private float getMinProgressArc() {
    return (float) Math.toRadians(getStrokeWidth() / (2 * Math.PI * getCenterRadius()));
  }

  private void updateRingColor(float interpolatedTime) {
    if (interpolatedTime > COLOR_START_DELAY_OFFSET) {
      setColor(evaluateColorChange((interpolatedTime - COLOR_START_DELAY_OFFSET)
          / (1.0f - COLOR_START_DELAY_OFFSET), getStartingColor(), getNextColor()));
    }
  }

  private int evaluateColorChange(float fraction, int startValue, int endValue) {
    int startA = (startValue >> 24) & 0xff;
    int startR = (startValue >> 16) & 0xff;
    int startG = (startValue >> 8) & 0xff;
    int startB = startValue & 0xff;

    int endA = (endValue >> 24) & 0xff;
    int endR = (endValue >> 16) & 0xff;
    int endG = (endValue >> 8) & 0xff;
    int endB = endValue & 0xff;

    return ((startA + (int) (fraction * (endA - startA))) << 24)
        | ((startR + (int) (fraction * (endR - startR))) << 16)
        | ((startG + (int) (fraction * (endG - startG))) << 8)
        | ((startB + (int) (fraction * (endB - startB))));
  }
}
