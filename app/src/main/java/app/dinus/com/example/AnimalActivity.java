package app.dinus.com.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import app.dinus.com.loadingdrawable.LoadingDrawable;
import app.dinus.com.loadingdrawable.render.animal.FishLoadingRenderer;
import app.dinus.com.loadingdrawable.render.animal.GhostsEyeLoadingRenderer;

public class AnimalActivity extends AppCompatActivity {
    private LoadingDrawable mFishDrawable;
    private LoadingDrawable mGhostsEyeDrawable;

    private ImageView mIvFish;
    private ImageView mIvGhostsEye;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, AnimalActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal);

        mIvFish = (ImageView) findViewById(R.id.fish_view);
        mIvGhostsEye = (ImageView) findViewById(R.id.ghosts_eye_view);

        mFishDrawable = new LoadingDrawable(new FishLoadingRenderer(this));
        mGhostsEyeDrawable = new LoadingDrawable(new GhostsEyeLoadingRenderer(this));

        mIvFish.setImageDrawable(mFishDrawable);
        mIvGhostsEye.setImageDrawable(mGhostsEyeDrawable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFishDrawable.start();
        mGhostsEyeDrawable.start();
    }

    @Override
    protected void onStop() {
        mFishDrawable.stop();
        mGhostsEyeDrawable.stop();
        super.onStop();
    }
}
