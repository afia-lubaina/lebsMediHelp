package com.example.medihelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginDoctorActivity extends AppCompatActivity {

    private static final String TAG = "LoginDoctor";
    EditText editTextEmail, editTextPassword;
    Button buttonLogin, changeRoleButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;


    static final int Req_Call=1;

    TextView textView,textViewforgotPass;



    @Override
    public void onStart() {
        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivityDoctor.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_doctor);
        Log.d("Hel","Start at login");
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
//        progressBar = findViewById(R.id.progressBar);
        changeRoleButton = findViewById(R.id.btnChangeRole);
        textView = findViewById(R.id.signup);
        textViewforgotPass = findViewById(R.id.forgotpass);

        changeRoleButton.setOnClickListener(view -> {
            Intent intent = new Intent(this,SelectUserTypeActivity.class);
            startActivity(intent);
            finish();
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUpDoctorActivity.class);
                startActivity(intent);
                finish();
            }
        });

        textViewforgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(intent);
                finish();
            }
        });


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                String email, password;
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginDoctorActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginDoctorActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
//                Intent intent= new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//                finish();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(LoginDoctorActivity.this, "Login Successful.",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent= new Intent(getApplicationContext(), MainActivityDoctor.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    //FIX THIS
                                    Toast.makeText(LoginDoctorActivity.this, "Authentication failed",
                                            Toast.LENGTH_SHORT).show();
//                                    Intent intent= new Intent(getApplicationContext(), MainActivity.class);
//                                    startActivity(intent);
//                                    finish();
                                }
                            }
                        });
            }
        });
    }

}