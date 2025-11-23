package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

public class HeightActivity extends AppCompatActivity {

    EditText inputHeight;
    Button btnSubmitHeight, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height);

        inputHeight = findViewById(R.id.inputHeight);
        btnSubmitHeight = findViewById(R.id.btnSubmitHeight);
        btnBack = findViewById(R.id.btnBack);

        // Tombol Submit → lanjut ke WeightActivity
        btnSubmitHeight.setOnClickListener(v -> {
            String heightText = inputHeight.getText().toString().trim();
            if (heightText.isEmpty()) {
                Toast.makeText(this, "Please enter your height", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(HeightActivity.this, WeightActivity.class);
                intent.putExtra("USER_EMAIL", getIntent().getStringExtra("USER_EMAIL"));
                intent.putExtra("USER_NAME", getIntent().getStringExtra("USER_NAME"));
                intent.putExtra("USER_AGE", getIntent().getStringExtra("USER_AGE")); // ✅ Konsisten
                intent.putExtra("USER_HEIGHT", heightText);
                startActivity(intent);
            }
        });

        // Tombol Back → kembali ke halaman sebelumnya
        btnBack.setOnClickListener(v -> finish());
    }
}