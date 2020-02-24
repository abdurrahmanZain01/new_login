package com.example.new_login.splashScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.new_login.LoginActivity;
import com.example.new_login.R;

public class splashScreen extends AppCompatActivity {

    private int loading_time = 2500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(splashScreen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        },loading_time);

    }
}
