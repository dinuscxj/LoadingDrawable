package app.dinus.com.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import app.dinus.com.loadingdrawable.LoadingDrawable;
import app.dinus.com.loadingdrawable.render.circle.jump.CollisionLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.jump.DanceLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.jump.GuardLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.jump.SwapLoadingRenderer;

public class CircleJumpActivity extends AppCompatActivity {
    private LoadingDrawable mSwapDrawable;
    private LoadingDrawable mGuardDrawable;
    private LoadingDrawable mDanceDrawable;
    private LoadingDrawable mCollisionDrawable;

    private ImageView mIvSwap;
    private ImageView mIvGuard;
    private ImageView mIvDance;
    private ImageView mIvCollision;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, CircleJumpActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_jump);

        mIvSwap = (ImageView) findViewById(R.id.swap_view);
        mIvGuard = (ImageView) findViewById(R.id.guard_view);
        mIvDance = (ImageView) findViewById(R.id.dance_view);
        mIvCollision = (ImageView) findViewById(R.id.collision_view);

        mSwapDrawable = new LoadingDrawable(new SwapLoadingRenderer(this));
        mGuardDrawable = new LoadingDrawable(new GuardLoadingRenderer(this));
        mDanceDrawable = new LoadingDrawable(new DanceLoadingRenderer(this));
        mCollisionDrawable = new LoadingDrawable(new CollisionLoadingRenderer(this));

        mIvSwap.setImageDrawable(mSwapDrawable);
        mIvGuard.setImageDrawable(mGuardDrawable);
        mIvDance.setImageDrawable(mDanceDrawable);
        mIvCollision.setImageDrawable(mCollisionDrawable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSwapDrawable.start();
        mGuardDrawable.start();
        mDanceDrawable.start();
        mCollisionDrawable.start();
    }

    @Override
    protected void onStop() {
        mSwapDrawable.stop();
        mGuardDrawable.stop();
        mDanceDrawable.stop();
        mCollisionDrawable.stop();
        super.onStop();
    }
}
