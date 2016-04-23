package app.dinus.com.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import app.dinus.com.loadingdrawable.render.circle.jump.CollisionLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.GearLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.LevelLoadingRenderer;
import app.dinus.com.loadingdrawable.LoadingDrawable;
import app.dinus.com.loadingdrawable.render.circle.rotate.MaterialLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.jump.SwapLoadingRenderer;
import app.dinus.com.loadingdrawable.render.circle.rotate.WhorlLoadingRenderer;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button mBtnCircleJump;
    private Button mBtnCircleRotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnCircleJump = (Button) findViewById(R.id.circle_jump);
        mBtnCircleRotate = (Button) findViewById(R.id.circle_rotate);

        mBtnCircleJump.setOnClickListener(this);
        mBtnCircleRotate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.circle_jump:
                Intent footerIntent = new Intent(MainActivity.this, CircleJumpActivity.class);
                startActivity(footerIntent);
                break;
            case R.id.circle_rotate:
                Intent headerIntent = new Intent(MainActivity.this, CircleRotateActivity.class);
                startActivity(headerIntent);
                break;
            default:
                break;
        }
    }
}
