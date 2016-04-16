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
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class LevelLoadingRenderer extends LoadingRenderer{
  private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
  private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
  private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
  private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

  private static final float START_TRIM_DURATION_OFFSET = 0.5f;
  private static final float END_TRIM_START_DELAY_OFFSET = 0.5f;

  private static final float NUM_POINTS = 5f;
  private static final float FULL_ROTATION = 1080.0f;
  private static final float MAX_PROGRESS_ARC = 0.8f;

  private static final int DEFAULT_COLOR = Color.WHITE;

  private final Paint mPaint = new Paint();
  private final RectF mTempBounds = new RectF();

  private final Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
    @Override
    public void onAnimationRepeat(Animator animator) {
      super.onAnimationRepeat(animator);
      storeOriginals();

      mStartTrim = mEndTrim;
      mRotationCount = (mRotationCount + 1) % (NUM_POINTS);
    }

    @Override
    public void onAnimationStart(Animator animation) {
      super.onAnimationStart(animation);
      mRotationCount = 0;
    }
  };

  private float mStrokeInset = 2.5f;

  private float mEndTrim = 0.0f;
  private float mRotation = 0.0f;
  private float mStartTrim = 0.0f;

  private boolean mIsRenderingFirstHalf;

  private int mBottomColor;
  private int mMiddleColor;
  private int mTopColor;

  private float mRotationCount;
  private float mGroupRotation;

  private float mOriginEndTrim;
  private float mOriginRotation;
  private float mOriginStartTrim;

  public LevelLoadingRenderer(Context context) {
    super(context);
    setupPaint();
    addRenderListener(mAnimatorListener);
  }

  private void setupPaint() {
    mBottomColor = oneThirdAlphaColor(DEFAULT_COLOR);
    mMiddleColor = twoThirdAlphaColor(DEFAULT_COLOR);
    mTopColor = DEFAULT_COLOR;

    mPaint.setAntiAlias(true);
    mPaint.setStrokeWidth(getStrokeWidth());
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeCap(Paint.Cap.ROUND);

    setInsets((int) getWidth(), (int) getHeight());
  }

  @Override
  public void draw(Canvas canvas, Rect bounds) {
    int saveCount = canvas.save();

    canvas.rotate(mGroupRotation, bounds.exactCenterX(), bounds.exactCenterY());
    RectF arcBounds = mTempBounds;
    arcBounds.set(bounds);
    arcBounds.inset(mStrokeInset, mStrokeInset);

    float startAngle = (mStartTrim + mRotation) * 360;
    float endAngle = (mEndTrim + mRotation) * 360;
    float sweepAngle = endAngle - startAngle;

    if (sweepAngle == 0) {
      return;
    }

    if (mIsRenderingFirstHalf) {
      float renderPercentage = Math.abs(mStartTrim - mEndTrim) / MAX_PROGRESS_ARC;

      float topIncrement = DECELERATE_INTERPOLATOR.getInterpolation(renderPercentage) - LINEAR_INTERPOLATOR.getInterpolation(renderPercentage);
      float bottomIncrement = ACCELERATE_INTERPOLATOR.getInterpolation(renderPercentage) - LINEAR_INTERPOLATOR.getInterpolation(renderPercentage);

      mPaint.setColor(mBottomColor);
      canvas.drawArc(arcBounds, endAngle, -sweepAngle * (1 + topIncrement), false, mPaint);
      mPaint.setColor(mMiddleColor);
      canvas.drawArc(arcBounds, endAngle, -sweepAngle * 7.0f / 8.0f, false, mPaint);
      mPaint.setColor(mTopColor);
      canvas.drawArc(arcBounds, endAngle, -sweepAngle * 5.0f / 8.0f * (1 + bottomIncrement), false, mPaint);
    } else {
      float renderPercentage = Math.abs(mStartTrim - mEndTrim) / MAX_PROGRESS_ARC;
      float totalSweepAngle = MAX_PROGRESS_ARC * 360;

      if (renderPercentage > 7.0f / 8.0f) {
        mPaint.setColor(mBottomColor);
        canvas.drawArc(arcBounds, endAngle, -sweepAngle, false, mPaint);

        mPaint.setColor(mMiddleColor);
        canvas.drawArc(arcBounds, endAngle, totalSweepAngle * 7.0f / 8.0f, false, mPaint);

        mPaint.setColor(mTopColor);
        canvas.drawArc(arcBounds, endAngle, totalSweepAngle * 5.0f / 8.0f, false, mPaint);
      }  else if (renderPercentage > 5.0f / 8.0f) {
        mPaint.setColor(mMiddleColor);
        canvas.drawArc(arcBounds, endAngle, -sweepAngle, false, mPaint);

        mPaint.setColor(mTopColor);
        canvas.drawArc(arcBounds, endAngle, totalSweepAngle * 5.0f / 8.0f, false, mPaint);
      } else {
        mPaint.setColor(mTopColor);
        canvas.drawArc(arcBounds, endAngle, -sweepAngle, false, mPaint);
      }
    }

    canvas.restoreToCount(saveCount);
  }

  @Override
  public void computeRender(float renderProgress) {
    final float minProgressArc = getMinProgressArc();
    final float originEndTrim = mOriginEndTrim;
    final float originStartTrim = mOriginStartTrim;
    final float originRotation = mOriginRotation;

    // Moving the start trim only occurs in the first 50% of a
    // single ring animation
    if (renderProgress <= START_TRIM_DURATION_OFFSET) {
      float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
      mStartTrim = originStartTrim + ((MAX_PROGRESS_ARC - minProgressArc) * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress));

      mIsRenderingFirstHalf = true;
    }

    // Moving the end trim starts after 50% of a single ring
    // animation completes
    if (renderProgress > END_TRIM_START_DELAY_OFFSET) {
      float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (1.0f - START_TRIM_DURATION_OFFSET);
      mEndTrim = originEndTrim + ((MAX_PROGRESS_ARC - minProgressArc) * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress));

      mIsRenderingFirstHalf = false;
    }

    mRotation = originRotation + (0.25f * renderProgress);

    mGroupRotation = ((FULL_ROTATION / NUM_POINTS) * renderProgress) + (FULL_ROTATION * (mRotationCount / NUM_POINTS));

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

  public void setColor(int color) {
    mBottomColor = oneThirdAlphaColor(color);
    mMiddleColor = twoThirdAlphaColor(color);
    mTopColor = color;
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

  public float getOriginRotation() {
    return mOriginRotation;
  }

  public void storeOriginals() {
    mOriginStartTrim = mStartTrim;
    mOriginEndTrim = mEndTrim;
    mOriginRotation = mRotation;
  }

  public void resetOriginals() {
    mOriginStartTrim = 0;
    mOriginEndTrim = 0;
    mOriginRotation = 0;
    setStartTrim(0);
    setEndTrim(0);
    setRotation(0);
  }

  private float getMinProgressArc() {
    return (float) Math.toRadians(getStrokeWidth() / (2 * Math.PI * getCenterRadius()));
  }

  private int oneThirdAlphaColor(int colorValue) {
    int startA = (colorValue >> 24) & 0xff;
    int startR = (colorValue >> 16) & 0xff;
    int startG = (colorValue >> 8) & 0xff;
    int startB = colorValue & 0xff;

    return (startA / 3 << 24)
        | (startR << 16)
        | (startG << 8)
        | startB;
  }

  private int twoThirdAlphaColor(int colorValue) {
    int startA = (colorValue >> 24) & 0xff;
    int startR = (colorValue >> 16) & 0xff;
    int startG = (colorValue >> 8) & 0xff;
    int startB = colorValue & 0xff;

    return (startA * 2 / 3 << 24)
        | (startR << 16)
        | (startG << 8)
        | startB;
  }
}
