package com.mazesolver.androidapp;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

// Set this color (#50CDC8) code to Splash activity xml background color to match (sky color with  PNG image)

public class SplashActivity extends AppCompatActivity {

    static final int restoreFadeText = 0;
    static final int fadeText = 1;
    Animation topAnimation, bottomAnimation;
    ImageView splashImage;
    TextView logo, slogan, continueText;
    private int currentApiVersion;
    ConstraintLayout myLayout = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hides Navigational bar as well as title bar (Permanently) making splash activity a full screened activity

        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+

        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will show up and won't hide.

            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }


        // Loading Animations from xml files we created in res file with the name of 'anim'

        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);


        // Castings

        logo = findViewById(R.id.logo);
        slogan = findViewById(R.id.slogan);
        splashImage = findViewById(R.id.splashImage);
        myLayout = findViewById(R.id.myLayout);
        continueText = findViewById(R.id.continueText);


        // Setting Animations

        splashImage.setAnimation(topAnimation);
        slogan.setAnimation(bottomAnimation);
        logo.setAnimation(bottomAnimation);
        continueText.setAnimation(bottomAnimation);
        handler.sendEmptyMessageDelayed(fadeText, 0);


        // Touching the screen will take you to MainActivity

        myLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Intent mainActivity = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainActivity);
                finish();

                return false;
            }
        });


    }


    // Handler here is used to create blinking effect to the text (PRESS ANYWHERE).

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case fadeText:
                    continueText.animate().alpha((float) 0.1);
                    handler.sendEmptyMessageDelayed(restoreFadeText, 630);
                    break;
                case restoreFadeText:
                    continueText.animate().alpha((float) 1.0);
                    handler.sendEmptyMessageDelayed(fadeText, 630);
                    break;

            }

        }
    };


}
