package com.example.elvis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_TIMEOUT = 2000;

    Animation splashAnimation;
    View splashView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.parseColor("#1f1f1d"));
        getWindow().setExitTransition(null);
        setContentView(R.layout.activity_main);

        splashAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        splashView = findViewById(R.id.splashLogo);
        splashView.setAnimation(splashAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sessionManager newSessionManager = new sessionManager(MainActivity.this);
                if(!newSessionManager.isLoggedIn()) {
                    Intent intent = new Intent(MainActivity.this, login.class);
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, splashView, ViewCompat.getTransitionName(splashView));
                    startActivity(intent, optionsCompat.toBundle());
                } else {
                    Intent intent;
                    if (newSessionManager.isNewUser()) {
                        intent = new Intent(MainActivity.this, initProfile.class);
                    } else {
                        intent = new Intent(MainActivity.this, profile.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, splashView, ViewCompat.getTransitionName(splashView));
                    startActivity(intent, optionsCompat.toBundle());
                }
            }
        }, SPLASH_TIMEOUT);
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }
}