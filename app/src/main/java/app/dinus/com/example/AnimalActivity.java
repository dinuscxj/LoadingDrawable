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
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, AnimalActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal);
    }
}
