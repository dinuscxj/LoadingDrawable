package app.dinus.com.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import app.dinus.com.loadingdrawable.LoadingDrawable;
import app.dinus.com.loadingdrawable.render.circle.rotate.GearLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.LevelLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.MaterialLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.WhorlLoadingRenderer;
import app.dinus.com.loadingdrawable.render.scenery.ElectricFanLoadingRenderer;

public class SceneryActivity extends AppCompatActivity {
    private LoadingDrawable mElectricFanDrawable;

    private ImageView mIvElectricFan;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SceneryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenery);

        mIvElectricFan = (ImageView) findViewById(R.id.electric_fan_view);

        mElectricFanDrawable = new LoadingDrawable(new ElectricFanLoadingRenderer(this));

        mIvElectricFan.setImageDrawable(mElectricFanDrawable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mElectricFanDrawable.start();
    }

    @Override
    protected void onStop() {
        mElectricFanDrawable.stop();
        super.onStop();
    }
}
