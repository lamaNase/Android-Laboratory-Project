package com.example.a1200746_1200190_courseproject;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;


public class SignInActivity extends AppCompatActivity {
        private EditText emailEditText, passwordEditText;
        private CheckBox rememberMeCheckBox;
        private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor_rememberme;
        private static final String PREF_EMAIL_KEY = "email";
        private DataBaseHelper dbHelper;
        private ImageView gifImageView;
        private ToggleButton darkModeSwitch;
        private SharedPreferences darkMode;
        private SharedPreferences.Editor editor;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            darkMode = getSharedPreferences("preferences", MODE_PRIVATE);
            editor = darkMode.edit();

            // Check current theme preference
            if (darkMode.getBoolean("dark_mode", false)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            super.onCreate(savedInstanceState);
            getWindow().setWindowAnimations(0);
            setContentView(R.layout.activity_sign_in);

            //darkModeSwitch = (ToggleButton) findViewById(R.id.darkModeSwitch);

            gifImageView = findViewById(R.id.sign_in_image);
            dbHelper = new DataBaseHelper(this);
            emailEditText = findViewById(R.id.email_edit_text);
            passwordEditText = findViewById(R.id.password_edit_text);
            rememberMeCheckBox = findViewById(R.id.remember_me_checkbox);

            Glide.with(this)
                    .load(R.drawable.login)
                    .into(gifImageView);
					
            Button loginButton = findViewById(R.id.login_button);
            Button signUpButton = findViewById(R.id.sign_up_button);
            Button back_button = findViewById(R.id.back_button);

            // Get shared preferences instance for login
            sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            editor_rememberme = sharedPreferences.edit();
            String savedEmail = sharedPreferences.getString(PREF_EMAIL_KEY, "");
            emailEditText.setText(savedEmail);

            // Set the switch state based on current theme
            /*darkModeSwitch.setChecked(darkMode.getBoolean("dark_mode", false));

            darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean("dark_mode", true);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("dark_mode", false);
                }
                editor.apply();
            });*/

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login();
                }
            });

            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Redirect to sign up activity
                    Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                    startActivity(intent);
                }
            });

            back_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        // Method to handle login process
        private void login() {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Invalid Email Address");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
                return;
            }

            if(!dbHelper.checkEmailExists(email)){
                emailEditText.setError("Email does not exist");
                return;
            }

            // Check if login credentials are valid
            if (dbHelper.checkLogin(email, password)) {
                if (rememberMeCheckBox.isChecked()) {
                    editor_rememberme.putString(PREF_EMAIL_KEY, email);
                    editor_rememberme.commit();
                }
                dbHelper.setUserEmail(email);
                DataBaseHelper.currentUser = dbHelper.getUserByEmail(email);

                // Redirect to home activity (adjust based on user roles if necessary)
                Intent homeIntent = new Intent(SignInActivity.this, HomeActivity.class); // Replace with actual home activity
                startActivity(homeIntent);
                finish();
            } else {
                passwordEditText.setError("Incorrect password");
                Toast.makeText(SignInActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
            }
        }
}

