package com.example.a1200746_1200190_courseproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private ImageView gifImageView;
    private ToggleButton darkModeSwitch;
    private DataBaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Check current theme preference
        if (sharedPreferences.getBoolean("dark_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_sign_up);

        //darkModeSwitch = (ToggleButton) findViewById(R.id.darkModeSwitch);


        dbHelper = new DataBaseHelper(this);
        // Set the switch state based on current theme
        //darkModeSwitch.setChecked(sharedPreferences.getBoolean("dark_mode", false));

        // Initialize edit text  elements
        emailEditText = findViewById(R.id.email_edit_text);
        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        gifImageView = findViewById(R.id.sign_in_image);



        Glide.with(this)
                .load(R.drawable.signup)
                .into(gifImageView);
				
        Button signUpButton = findViewById(R.id.sign_up_button);
        Button back_button = findViewById(R.id.back_button);

        /*darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("dark_mode", true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("dark_mode", false);
            }
            editor.apply();
        });*/

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Method to handle sign up process
    private void signUp() {
        String email = emailEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (validateInputs(email, firstName, lastName, password, confirmPassword)) {
            // Check if email already exists in the database
            if (dbHelper.checkEmailExists(email)) {
                emailEditText.setError("Email is already exist");
                Toast.makeText(SignUpActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
            } else {
                User user = new User(email, firstName, lastName, password,null);
                Uri profilePic = Uri.parse("android.resource://" + SignUpActivity.this.getPackageName() + "/drawable/updateprofile");
                user.setProfilePicture(profilePic);
                boolean inserted = dbHelper.insertUser(user);


                // Show appropriate message based on insertion status
                if (inserted) {
                    DataBaseHelper.currentUser = dbHelper.getUserByEmail(email);
                    Toast.makeText(SignUpActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Method to validate input fields
    private boolean validateInputs(String email, String firstName, String lastName, String password, String confirmPassword) {
        email = email.trim();
        // Validate email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid Email Address");
            return false;
        }

        // Validate first name
        if (TextUtils.isEmpty(firstName) || firstName.length() < 5 || firstName.length() > 20) {
            firstNameEditText.setError("Enter a valid first name (5-20 characters)");
            return false;
        }

        // Validate last name
        if (TextUtils.isEmpty(lastName) || lastName.length() < 5|| lastName.length() > 20) {
            lastNameEditText.setError("Enter a valid last name (5-20 characters)");
            return false;
        }

        // Validate password
        if (TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 12 || !password.matches(".*\\d.*") || !password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*")) {
            passwordEditText.setError("Password must be 6-12 characters long and contain at least one number, one lowercase letter, and one uppercase letter");
            return false;
        }

        // Validate password confirmation
        if (!TextUtils.equals(password, confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return false;
        }

        return true;
    }
}
