package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    EditText username, email_address, register_password, confirm_password;
    Button register_button;
    TextView back_to_login;
    FirebaseAuth m_auth;
    ProgressBar progress_bar;
    FirebaseFirestore f_store;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        email_address = findViewById(R.id.email_address);
        register_password = findViewById(R.id.register_password);
        confirm_password = findViewById(R.id.confirm_password);
        m_auth = FirebaseAuth.getInstance();
        f_store = FirebaseFirestore.getInstance();
        progress_bar = findViewById(R.id.progress_bar);
        register_button = findViewById(R.id.register_button);
        back_to_login = findViewById(R.id.back_to_login_activity);
        register_button.setOnClickListener(v -> checkCredentials());

        if (m_auth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        }

        back_to_login.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        });

    }

    private void checkCredentials() {
        String checkUsername = username.getText().toString().trim();
        String checkEmailAddress = email_address.getText().toString().trim();
        String checkPassword = register_password.getText().toString().trim();
        String checkConfirmedPassword = confirm_password.getText().toString().trim();

        boolean isValid = true;

        if (checkUsername.isEmpty()) {
            showError(username, "Please enter your username");
            isValid = false;
        }
        else if (checkUsername.length() <= 5) {
            showError(username, "Your username length must be at least 6 characters");
            isValid = false;
        }
        else if (checkEmailAddress.isEmpty()) {
            showError(email_address, "Please enter your email");
            isValid = false;
        }
        else if (!checkEmailAddress.contains("@")) {
            showError(email_address, "Please enter a valid email address");
            isValid = false;
        }
        else if (checkPassword.isEmpty()) {
            showError(register_password, "Please enter your password");
            isValid = false;
        }
        else if (checkPassword.length() < 8) {
            showError(register_password, "Your password length must be at least 8 characters");
            isValid = false;
        }
        else if (checkPassword.length() > 64) {
            showError(register_password, "Your password can have at most 64 characters");
            isValid = false;
        }
        else if (checkConfirmedPassword.isEmpty() || !checkConfirmedPassword.equals(checkPassword)) {
            showError(confirm_password, "Your password doesn't match the previous one");
            isValid = false;
        }

        progress_bar.setVisibility(View.VISIBLE);

        if (isValid) {
            m_auth.createUserWithEmailAndPassword(checkEmailAddress, checkPassword)
                    .addOnCompleteListener(this, task -> {
                        progress_bar.setVisibility(ViewStub.GONE);

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(RegisterActivity.this, "You have successfully registered",
                                    Toast.LENGTH_SHORT).show();

                            userID = Objects.requireNonNull(m_auth.getCurrentUser()).getUid();
                            DocumentReference documentReference = f_store.collection("all my users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("Username", checkUsername);
                            user.put("Email address", checkEmailAddress);

                            documentReference.set(user).addOnSuccessListener(unused -> Log.d(TAG, "User profile has been created for " + userID)).addOnFailureListener(e -> Log.d(TAG, e.toString()));

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            progress_bar.setVisibility(ViewStub.GONE);
        }
    }

    private void showError(EditText input, String errorText) {
        input.setError(errorText);
        input.requestFocus();
    }
}