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

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, GoodsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods);

    }
}
