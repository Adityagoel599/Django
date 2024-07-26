package com.example.django.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.django.R;
import com.example.django.utilities.Constants;
import com.example.django.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private EditText inputFirstname, inputLastName, inputEmail, inputPassword, inputConfirmPassword;
    private MaterialButton buttonSignUp;
    private ProgressBar signUpProgressBar;
    private PreferenceManager preferenceManager;
    // added ny me




    // added  by me
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        preferenceManager = new PreferenceManager(getApplicationContext());
        findViewById(R.id.imageBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.TextSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
            inputFirstname=findViewById(R.id.inputFirstName);
            inputLastName=findViewById(R.id.inputLastName);
            inputEmail=findViewById(R.id.inputEmail);
            inputPassword=findViewById(R.id.inputPassword);
            inputConfirmPassword=findViewById(R.id.inputConfirmPassword);
            buttonSignUp= findViewById(R.id.buttonSignUp);
            signUpProgressBar=findViewById(R.id.singUpProgressBar);
            buttonSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(inputFirstname.getText().toString().trim().isEmpty()){
                        Toast.makeText(SignUpActivity.this,"Enter the First Name ",Toast.LENGTH_SHORT).show();
                    } else if (inputLastName.getText().toString().trim().isEmpty()) {
                        Toast.makeText(SignUpActivity.this,"Enter the Last Name ",Toast.LENGTH_SHORT).show();
                    } else if (inputEmail.getText().toString().trim().isEmpty()) {

                        Toast.makeText(SignUpActivity.this,"Enter the Email ",Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                        Toast.makeText(SignUpActivity.this,"Enter the Email Correctly ",Toast.LENGTH_SHORT).show();
                    }else if(inputPassword.getText().toString().trim().isEmpty()){
                        Toast.makeText(SignUpActivity.this,"Enter the Password ",Toast.LENGTH_SHORT).show();
                    }
                    else if (inputConfirmPassword.getText().toString().trim().isEmpty()) {
                        Toast.makeText(SignUpActivity.this,"Enter the Field ",Toast.LENGTH_SHORT).show();
                    } else if (!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())) {
                        Toast.makeText(SignUpActivity.this,"passwords  do not match ",Toast.LENGTH_SHORT).show();
                    } else {
                            signUp();
                    }
                }
            });
    }
            private void signUp(){
            buttonSignUp.setVisibility(View.INVISIBLE);
            signUpProgressBar.setVisibility(View.VISIBLE);
                FirebaseFirestore database= FirebaseFirestore.getInstance();
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_FIRST_NAME, inputFirstname.getText().toString());
                user.put(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
                user.put(Constants.KEY_EMAIL, inputEmail.getText().toString());
                user.put(Constants.KEY_PASSWORD, inputPassword.getText().toString());
                database.collection(Constants.KEY_COLLECTION_USER).add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME, inputFirstname.getText().toString());
                        preferenceManager.putString(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
                        preferenceManager.putString(Constants.KEY_EMAIL, inputEmail.getText().toString());
                        //preferenceManager.putString(Constants.KEY_PASSWORD, inputPassword.getText().toString());
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                buttonSignUp.setVisibility(View.VISIBLE);
                                signUpProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(SignUpActivity.this,"Error :"+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
        }


}