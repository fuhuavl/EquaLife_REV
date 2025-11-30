package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

public class SignupActivity extends AppCompatActivity {

    EditText etSignupEmail, etSignupPassword, etConfirmPassword;
    Button btnSignupSubmit;
    TextView tvGoLogin;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = new DatabaseHelper(this);

        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignupSubmit = findViewById(R.id.btnSignupSubmit);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        btnSignupSubmit.setOnClickListener(v -> {
            String email = etSignupEmail.getText().toString().trim();
            String password = etSignupPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.checkEmailExists(email)) {
                Toast.makeText(this, "Email already registered. Please login.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = db.insertUser(email, password);
            if (inserted) {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SignupActivity.this, IntroActivity.class);
                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);
                finish(); // Tutup SignupActivity
            } else {
                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        });
    }
}