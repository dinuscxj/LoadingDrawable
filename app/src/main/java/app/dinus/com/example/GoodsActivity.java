package app.dinus.com.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import app.dinus.com.loadingdrawable.LoadingDrawable;
import app.dinus.com.loadingdrawable.render.animal.GhostsEyeLoadingRenderer;
import app.dinus.com.loadingdrawable.render.goods.BalloonLoadingRenderer;
import app.dinus.com.loadingdrawable.render.goods.WaterBottleLoadingRenderer;

public class GoodsActivity extends AppCompatActivity {
    private LoadingDrawable mBalloonDrawable;
    private LoadingDrawable mWaterBottleDrawable;

    private ImageView mIvBalloon;
    private ImageView mIvWaterBottle;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, GoodsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods);

        mIvBalloon = (ImageView) findViewById(R.id.balloon_view);
        mIvWaterBottle = (ImageView) findViewById(R.id.water_bottle_view);

        mBalloonDrawable = new LoadingDrawable(new BalloonLoadingRenderer(this));
        mWaterBottleDrawable = new LoadingDrawable(new WaterBottleLoadingRenderer(this));

        mIvBalloon.setImageDrawable(mBalloonDrawable);
        mIvWaterBottle.setImageDrawable(mWaterBottleDrawable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBalloonDrawable.start();
        mWaterBottleDrawable.start();
    }

    @Override
    protected void onStop() {
        mBalloonDrawable.stop();
        mWaterBottleDrawable.stop();
        super.onStop();
    }
}
