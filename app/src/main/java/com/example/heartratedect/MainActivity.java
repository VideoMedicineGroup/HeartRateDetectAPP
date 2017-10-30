package com.example.heartratedect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

/**
 * welcome layout Activity
 * 设置APP启动动画
 * @author GqGAO
 */
public class MainActivity extends AppCompatActivity {

    private ImageView welcomeImg = null;
    Vibrator vibrator;

    @Override
    protected void onStop() {
        if(null != vibrator){
            vibrator.cancel();
        }
        super.onStop();
    }

  /*  @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){

        }
        return super.onTouchEvent(event);
    }*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initImage();
    }
    private void initImage(){
        welcomeImg = (ImageView)findViewById(R.id.welcome_img);
        welcomeImg.setBackgroundResource(R.drawable.hfutlogo);
        ScaleAnimation scaleAnimation =new ScaleAnimation( 1.4f, 1.0f, 1.4f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(2000);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        welcomeImg.startAnimation(scaleAnimation);
    }

    private void startActivity(){
        Intent intent =new Intent(MainActivity.this,CameraActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {800,50,400,30};// OFF/ON/OFF/ON...
        vibrator.vibrate(pattern,2);
        finish();
    }

}
