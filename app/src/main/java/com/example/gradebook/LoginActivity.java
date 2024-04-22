package com.example.gradebook;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        TextView error = findViewById(R.id.login_error);
        Button login = findViewById(R.id.login_btn);

        login.setOnClickListener(v -> {
            if (username.getText().toString().equals("sdu") && password.getText().toString().equals("sdu")) {
                username.setText("");
                password.setText("");
                startActivity(new Intent(this, MainActivity.class));
            } else {
                username.setText("");
                password.setText("");
                error.setText("Wrong username/password!");
            }
        });
    }
}