package app.dinus.com.loadingdrawable.render.goods;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;

public class BalloonLoadingRenderer extends LoadingRenderer {
    private static final String PERCENT_SIGN = "%";

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    private static final float START_INHALE_DURATION_OFFSET = 0.4f;

    private static final float DEFAULT_WIDTH = 200.0f;
    private static final float DEFAULT_HEIGHT = 150.0f;
    private static final float DEFAULT_STROKE_WIDTH = 2.0f;
    private static final float DEFAULT_GAS_TUBE_WIDTH = 48;
    private static final float DEFAULT_GAS_TUBE_HEIGHT = 20;
    private static final float DEFAULT_CANNULA_WIDTH = 13;
    private static final float DEFAULT_CANNULA_HEIGHT = 37;
    private static final float DEFAULT_CANNULA_OFFSET_Y = 3;
    private static final float DEFAULT_CANNULA_MAX_OFFSET_Y = 15;
    private static final float DEFAULT_PIPE_BODY_WIDTH = 16;
    private static final float DEFAULT_PIPE_BODY_HEIGHT = 36;
    private static final float DEFAULT_BALLOON_WIDTH = 38;
    private static final float DEFAULT_BALLOON_HEIGHT = 48;
    private static final float DEFAULT_RECT_CORNER_RADIUS = 2;

    private static final int DEFAULT_BALLOON_COLOR = Color.parseColor("#ffF3C211");
    private static final int DEFAULT_GAS_TUBE_COLOR = Color.parseColor("#ff174469");
    private static final int DEFAULT_PIPE_BODY_COLOR = Color.parseColor("#aa2369B1");
    private static final int DEFAULT_CANNULA_COLOR = Color.parseColor("#ff174469");

    private static final float DEFAULT_TEXT_SIZE = 7.0f;

    private static final long ANIMATION_DURATION = 3333;

    private final Paint mPaint = new Paint();
    private final RectF mCurrentBounds = new RectF();
    private final RectF mGasTubeBounds = new RectF();
    private final RectF mPipeBodyBounds = new RectF();
    private final RectF mCannulaBounds = new RectF();
    private final RectF mBalloonBounds = new RectF();

    private final Rect mProgressBounds = new Rect();

    private float mTextSize;
    private float mProgress;

    private String mProgressText;

    private float mGasTubeWidth;
    private float mGasTubeHeight;
    private float mCannulaWidth;
    private float mCannulaHeight;
    private float mCannulaMaxOffsetY;
    private float mCannulaOffsetY;
    private float mPipeBodyWidth;
    private float mPipeBodyHeight;
    private float mBalloonWidth;
    private float mBalloonHeight;
    private float mRectCornerRadius;
    private float mStrokeWidth;

    private int mBalloonColor;
    private int mGasTubeColor;
    private int mCannulaColor;
    private int mPipeBodyColor;

    private BalloonLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
    }

    private void init(Context context) {
        mTextSize = DensityUtil.dip2px(context, DEFAULT_TEXT_SIZE);

        mWidth = DensityUtil.dip2px(context, DEFAULT_WIDTH);
        mHeight = DensityUtil.dip2px(context, DEFAULT_HEIGHT);
        mStrokeWidth = DensityUtil.dip2px(context, DEFAULT_STROKE_WIDTH);

        mGasTubeWidth = DensityUtil.dip2px(context, DEFAULT_GAS_TUBE_WIDTH);
        mGasTubeHeight = DensityUtil.dip2px(context, DEFAULT_GAS_TUBE_HEIGHT);
        mCannulaWidth = DensityUtil.dip2px(context, DEFAULT_CANNULA_WIDTH);
        mCannulaHeight = DensityUtil.dip2px(context, DEFAULT_CANNULA_HEIGHT);
        mCannulaOffsetY = DensityUtil.dip2px(context, DEFAULT_CANNULA_OFFSET_Y);
        mCannulaMaxOffsetY = DensityUtil.dip2px(context, DEFAULT_CANNULA_MAX_OFFSET_Y);
        mPipeBodyWidth = DensityUtil.dip2px(context, DEFAULT_PIPE_BODY_WIDTH);
        mPipeBodyHeight = DensityUtil.dip2px(context, DEFAULT_PIPE_BODY_HEIGHT);
        mBalloonWidth = DensityUtil.dip2px(context, DEFAULT_BALLOON_WIDTH);
        mBalloonHeight = DensityUtil.dip2px(context, DEFAULT_BALLOON_HEIGHT);
        mRectCornerRadius = DensityUtil.dip2px(context, DEFAULT_RECT_CORNER_RADIUS);

        mBalloonColor = DEFAULT_BALLOON_COLOR;
        mGasTubeColor = DEFAULT_GAS_TUBE_COLOR;
        mCannulaColor = DEFAULT_CANNULA_COLOR;
        mPipeBodyColor = DEFAULT_PIPE_BODY_COLOR;

        mProgressText = 10 + PERCENT_SIGN;

        mDuration = ANIMATION_DURATION;
    }

    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    @Override
    protected void draw(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();

        RectF arcBounds = mCurrentBounds;
        arcBounds.set(bounds);

        //draw draw gas tube
        mPaint.setColor(mGasTubeColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawPath(createGasTubePath(mGasTubeBounds), mPaint);

        //draw balloon
        mPaint.setColor(mBalloonColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(createBalloonPath(mBalloonBounds, mProgress), mPaint);

        //draw progress
        mPaint.setColor(mGasTubeColor);
        mPaint.setTextSize(mTextSize);
        mPaint.setStrokeWidth(mStrokeWidth / 5.0f);
        canvas.drawText(mProgressText, arcBounds.centerX() - mProgressBounds.width() / 2.0f,
                mGasTubeBounds.centerY() + mProgressBounds.height() / 2.0f, mPaint);

        //draw cannula
        mPaint.setColor(mCannulaColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawPath(createCannulaHeadPath(mCannulaBounds), mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(createCannulaBottomPath(mCannulaBounds), mPaint);

        //draw pipe body
        mPaint.setColor(mPipeBodyColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(mPipeBodyBounds, mRectCornerRadius, mRectCornerRadius, mPaint);

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void computeRender(float renderProgress) {
        RectF arcBounds = mCurrentBounds;
        //compute gas tube bounds
        mGasTubeBounds.set(arcBounds.centerX() - mGasTubeWidth / 2.0f, arcBounds.centerY(),
                arcBounds.centerX() + mGasTubeWidth / 2.0f, arcBounds.centerY() + mGasTubeHeight);
        //compute pipe body bounds
        mPipeBodyBounds.set(arcBounds.centerX() + mGasTubeWidth / 2.0f - mPipeBodyWidth / 2.0f, arcBounds.centerY() - mPipeBodyHeight,
                arcBounds.centerX() + mGasTubeWidth / 2.0f + mPipeBodyWidth / 2.0f, arcBounds.centerY());
        //compute cannula bounds
        mCannulaBounds.set(arcBounds.centerX() + mGasTubeWidth / 2.0f - mCannulaWidth / 2.0f, arcBounds.centerY() - mCannulaHeight - mCannulaOffsetY,
                arcBounds.centerX() + mGasTubeWidth / 2.0f + mCannulaWidth / 2.0f, arcBounds.centerY() - mCannulaOffsetY);
        //compute balloon bounds
        float insetX = mBalloonWidth * 0.333f * (1 - mProgress);
        float insetY = mBalloonHeight * 0.667f * (1 - mProgress);
        mBalloonBounds.set(arcBounds.centerX() - mGasTubeWidth / 2.0f - mBalloonWidth / 2.0f + insetX, arcBounds.centerY() - mBalloonHeight + insetY,
                arcBounds.centerX() - mGasTubeWidth / 2.0f + mBalloonWidth / 2.0f - insetX, arcBounds.centerY());

        if (renderProgress <= START_INHALE_DURATION_OFFSET) {
            mCannulaBounds.offset(0, -mCannulaMaxOffsetY * renderProgress / START_INHALE_DURATION_OFFSET);

            mProgress = 0.0f;
            mProgressText = 10 + PERCENT_SIGN;

            mPaint.setTextSize(mTextSize);
            mPaint.getTextBounds(mProgressText, 0, mProgressText.length(), mProgressBounds);
        } else {
            float exhaleProgress = ACCELERATE_INTERPOLATOR.getInterpolation(1.0f - (renderProgress - START_INHALE_DURATION_OFFSET) / (1.0f - START_INHALE_DURATION_OFFSET));
            mCannulaBounds.offset(0, -mCannulaMaxOffsetY * exhaleProgress);

            mProgress = 1.0f - exhaleProgress;
            mProgressText = adjustProgress((int) (exhaleProgress * 100.0f)) + PERCENT_SIGN;

            mPaint.setTextSize(mTextSize);
            mPaint.getTextBounds(mProgressText, 0, mProgressText.length(), mProgressBounds);
        }
    }

    private int adjustProgress(int progress) {
        progress = progress / 10 * 10;
        progress = 100 - progress + 10;
        if (progress > 100) {
            progress = 100;
        }

        return progress;
    }

    private Path createGasTubePath(RectF gasTubeRect) {
        Path path = new Path();
        path.moveTo(gasTubeRect.left, gasTubeRect.top);
        path.lineTo(gasTubeRect.left, gasTubeRect.bottom);
        path.lineTo(gasTubeRect.right, gasTubeRect.bottom);
        path.lineTo(gasTubeRect.right, gasTubeRect.top);
        return path;
    }

    private Path createCannulaHeadPath(RectF cannulaRect) {
        Path path = new Path();
        path.moveTo(cannulaRect.left, cannulaRect.top);
        path.lineTo(cannulaRect.right, cannulaRect.top);
        path.moveTo(cannulaRect.centerX(), cannulaRect.top);
        path.lineTo(cannulaRect.centerX(), cannulaRect.bottom - 0.833f * cannulaRect.width());
        return path;
    }

    private Path createCannulaBottomPath(RectF cannulaRect) {
        RectF cannulaHeadRect = new RectF(cannulaRect.left, cannulaRect.bottom - 0.833f * cannulaRect.width(),
                cannulaRect.right, cannulaRect.bottom);

        Path path = new Path();
        path.addRoundRect(cannulaHeadRect, mRectCornerRadius, mRectCornerRadius, Path.Direction.CCW);
        return path;
    }

    /**
     * Coordinates are approximate, you have better cooperate with the designer's design draft
     */
    private Path createBalloonPath(RectF balloonRect, float progress) {

        Path path = new Path();
        path.moveTo(balloonRect.centerX(), balloonRect.bottom);

        float progressWidth = balloonRect.width() * progress;
        float progressHeight = balloonRect.height() * progress;
        //draw left half
        float leftIncrementX1 = progressWidth * -0.48f;
        float leftIncrementY1 = progressHeight * 0.75f;
        float leftIncrementX2 = progressWidth * -0.03f;
        float leftIncrementY2 = progressHeight * -1.6f;
        float leftIncrementX3 = progressWidth * 0.9f;
        float leftIncrementY3 = progressHeight * -1.0f;

        path.cubicTo(balloonRect.left + balloonRect.width() * 0.25f + leftIncrementX1, balloonRect.centerY() - balloonRect.height() * 0.4f + leftIncrementY1,
                balloonRect.left - balloonRect.width() * 0.20f + leftIncrementX2, balloonRect.centerY() + balloonRect.height() * 1.15f + leftIncrementY2,
                balloonRect.left - balloonRect.width() * 0.4f + leftIncrementX3, balloonRect.bottom + leftIncrementY3);

//        the results of the left final transformation
//        path.cubicTo(balloonRect.left - balloonRect.width() * 0.13f, balloonRect.centerY() + balloonRect.height() * 0.35f,
//                balloonRect.left - balloonRect.width() * 0.23f, balloonRect.centerY() - balloonRect.height() * 0.45f,
//                balloonRect.left + balloonRect.width() * 0.5f, balloonRect.bottom － balloonRect.height());

        //draw right half
        float rightIncrementX1 = progressWidth * 1.51f;
        float rightIncrementY1 = progressHeight * -0.05f;
        float rightIncrementX2 = progressWidth * 0.03f;
        float rightIncrementY2 = progressHeight * 0.5f;
        float rightIncrementX3 = 0.0f;
        float rightIncrementY3 = 0.0f;

        path.cubicTo(balloonRect.left - balloonRect.width() * 0.38f + rightIncrementX1, balloonRect.centerY() - balloonRect.height() * 0.4f + rightIncrementY1,
                balloonRect.left + balloonRect.width() * 1.1f + rightIncrementX2, balloonRect.centerY() - balloonRect.height() * 0.15f + rightIncrementY2,
                balloonRect.left + balloonRect.width() * 0.5f + rightIncrementX3, balloonRect.bottom + rightIncrementY3);

//        the results of the right final transformation
//        path.cubicTo(balloonRect.left + balloonRect.width() * 1.23f, balloonRect.centerY() - balloonRect.height() * 0.45f,
//                balloonRect.left + balloonRect.width() * 1.13f, balloonRect.centerY() + balloonRect.height() * 0.35f,
//                balloonRect.left + balloonRect.width() * 0.5f, balloonRect.bottom);

        return path;
    }

    @Override
    protected void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);

    }

    @Override
    protected void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    protected void reset() {
    }

    public static class Builder {
        private Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public BalloonLoadingRenderer build() {
            BalloonLoadingRenderer loadingRenderer = new BalloonLoadingRenderer(mContext);
            return loadingRenderer;
        }
    }
}