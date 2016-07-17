package app.dinus.com.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import app.dinus.com.loadingdrawable.LoadingDrawable;
import app.dinus.com.loadingdrawable.render.shapechange.CircleBroodLoadingRenderer;
import app.dinus.com.loadingdrawable.render.shapechange.CoolWaitLoadingRenderer;

public class ShapeChangeActivity extends AppCompatActivity {
    private LoadingDrawable mCircleBroodDrawable;
    private LoadingDrawable mCoolWaitDrawable;

    private ImageView mIvCircleBrood;
    private ImageView mIvCoolWait;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ShapeChangeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape_change);

        mIvCircleBrood = (ImageView) findViewById(R.id.circle_brood_view);
        mIvCoolWait = (ImageView) findViewById(R.id.cool_wait_view);

        mCircleBroodDrawable = new LoadingDrawable(new CircleBroodLoadingRenderer(this));
        mCoolWaitDrawable = new LoadingDrawable(new CoolWaitLoadingRenderer(this));

        mIvCircleBrood.setImageDrawable(mCircleBroodDrawable);
        mIvCoolWait.setImageDrawable(mCoolWaitDrawable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCircleBroodDrawable.start();
        mCoolWaitDrawable.start();
    }

    @Override
    protected void onStop() {
        mCircleBroodDrawable.stop();
        mCoolWaitDrawable.stop();
        super.onStop();
    }
}
