package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        View rootLayout = findViewById(android.R.id.content);
        String email = getIntent().getStringExtra("USER_EMAIL");

        rootLayout.setAlpha(0f);
        rootLayout.animate()
                .alpha(1f)
                .setDuration(600)
                .start();

        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, NameActivity.class);
                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);

                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                finish();
            }
        });
    }
}