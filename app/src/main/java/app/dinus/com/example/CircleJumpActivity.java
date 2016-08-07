package app.dinus.com.example;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ViewUtils;

import app.dinus.com.loadingdrawable.DensityUtil;
import app.dinus.com.loadingdrawable.LoadingView;
import app.dinus.com.loadingdrawable.render.LoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.jump.CollisionLoadingRenderer;

public class CircleJumpActivity extends AppCompatActivity {
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, CircleJumpActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_jump);
    }
}
