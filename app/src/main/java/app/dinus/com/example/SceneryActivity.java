package app.dinus.com.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import app.dinus.com.loadingdrawable.LoadingDrawable;
import app.dinus.com.loadingdrawable.render.scenery.DayNightLoadingRenderer;
import app.dinus.com.loadingdrawable.render.scenery.ElectricFanLoadingRenderer;

public class SceneryActivity extends AppCompatActivity {
    private LoadingDrawable mDayNightDrawable;
    private LoadingDrawable mElectricFanDrawable;

    private ImageView mIvDayNight;
    private ImageView mIvElectricFan;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SceneryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenery);

        mIvDayNight = (ImageView) findViewById(R.id.day_night_view);
        mIvElectricFan = (ImageView) findViewById(R.id.electric_fan_view);

        mDayNightDrawable = new LoadingDrawable(new DayNightLoadingRenderer(this));
        mElectricFanDrawable = new LoadingDrawable(new ElectricFanLoadingRenderer(this));

        mIvDayNight.setImageDrawable(mDayNightDrawable);
        mIvElectricFan.setImageDrawable(mElectricFanDrawable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDayNightDrawable.start();
        mElectricFanDrawable.start();
    }

    @Override
    protected void onStop() {
        mDayNightDrawable.stop();
        mElectricFanDrawable.stop();
        super.onStop();
    }
}
