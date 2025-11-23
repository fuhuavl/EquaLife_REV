package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class FinishActivity extends AppCompatActivity {

    Button btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        String email = getIntent().getStringExtra("USER_EMAIL");

        btnFinish = findViewById(R.id.btnFinish);

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Data sudah tersimpan di DietActivity, langsung pindah ke Home
                Intent intent = new Intent(FinishActivity.this, HomeActivity.class);
                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);
                finish(); // ✅ Tutup FinishActivity supaya gak bisa back
            }
        });
    }
}