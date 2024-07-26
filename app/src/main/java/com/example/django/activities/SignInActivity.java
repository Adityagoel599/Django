package com.example.django.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.django.R;
import com.example.django.utilities.Constants;
import com.example.django.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {
    private EditText inputEmail, inputPassword;
    private MaterialButton buttonSignIn;
    private ProgressBar SignInprogressBar;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        FirebaseApp.initializeApp(this);
        preferenceManager= new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent= new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        findViewById(R.id.TextSignUp).setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
            }
        });

inputEmail= findViewById(R.id.inputEmail);
inputPassword= findViewById(R.id.inputPassword);
buttonSignIn= findViewById(R.id.buttonSignIn);
SignInprogressBar= findViewById(R.id.signInProgressBar);
   buttonSignIn.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {
           if(inputEmail.getText().toString().trim().isEmpty()){
               Toast.makeText(SignInActivity.this, "Please enter your Email", Toast.LENGTH_SHORT).show();
           }else if (inputPassword.getText().toString().trim().isEmpty()){
               Toast.makeText(SignInActivity.this, "Please enter your Password", Toast.LENGTH_SHORT).show();
           }else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
               Toast.makeText(SignInActivity.this,"Enter the Email Correctly ",Toast.LENGTH_SHORT).show();
       }else{
                SignIn();
           }
       }
   });
    }
    private void SignIn(){
        buttonSignIn.setVisibility(View.INVISIBLE);
        SignInprogressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_EMAIL,inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                            DocumentSnapshot documentSnapshot= task.getResult().getDocuments().get(0);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                            preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME));
                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                            Intent intent= new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else {
                            buttonSignIn.setVisibility(View.VISIBLE);
                            SignInprogressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(SignInActivity.this,"Unable to Sign In ",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}