package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvSignUp;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        // Tombol Login
        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean valid = db.checkUser(email, password);
            if (valid) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                // ========== CEK APAKAH USER SUDAH COMPLETE ONBOARDING ==========
                boolean isCompleted = db.isOnboardingCompleted(email);

                Intent intent;
                if (isCompleted) {
                    // User sudah pernah isi data -> langsung ke Home
                    intent = new Intent(MainActivity.this, HomeActivity.class);
                } else {
                    // User belum pernah isi data -> mulai onboarding
                    intent = new Intent(MainActivity.this, IntroActivity.class);
                }

                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);
                finish(); // Tutup MainActivity supaya gak bisa back
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Klik "Don't have an account? Sign Up"
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        });
    }
}