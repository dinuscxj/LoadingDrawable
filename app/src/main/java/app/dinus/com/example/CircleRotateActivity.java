package app.dinus.com.example;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.LoadingView;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.GearLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.LevelLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.MaterialLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.WhorlLoadingRenderer;

public class CircleRotateActivity extends AppCompatActivity {
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, CircleRotateActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_rotate);

        LoadingView loadingView = (LoadingView) findViewById(R.id.gear_view);

        LoadingRenderer loadingRenderer = new GearLoadingRenderer.Builder(this)
                .setColor(Color.BLUE)
                .setGearCount(3)
                .setGearSwipeDegrees(100)
                .build();
        loadingView.setLoadingRenderer(loadingRenderer);
    }
}
