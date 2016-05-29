package app.dinus.com.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button mBtnGoods;
    private Button mBtnAnimal;
    private Button mBtnScenery;
    private Button mBtnCircleJump;
    private Button mBtnCircleRotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnGoods = (Button) findViewById(R.id.goods);
        mBtnAnimal = (Button) findViewById(R.id.animal);
        mBtnScenery = (Button) findViewById(R.id.scenery);
        mBtnCircleJump = (Button) findViewById(R.id.circle_jump);
        mBtnCircleRotate = (Button) findViewById(R.id.circle_rotate);

        mBtnGoods.setOnClickListener(this);
        mBtnAnimal.setOnClickListener(this);
        mBtnScenery.setOnClickListener(this);
        mBtnCircleJump.setOnClickListener(this);
        mBtnCircleRotate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.goods:
                GoodsActivity.startActivity(this);
                break;
            case R.id.animal:
                AnimalActivity.startActivity(this);
                break;
            case R.id.scenery:
                SceneryActivity.startActivity(this);
                break;
            case R.id.circle_jump:
                CircleJumpActivity.startActivity(this);
                break;
            case R.id.circle_rotate:
                CircleRotateActivity.startActivity(this);
                break;
            default:
                break;
        }
    }
}
