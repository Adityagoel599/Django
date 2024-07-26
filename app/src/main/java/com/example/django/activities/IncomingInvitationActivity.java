package com.example.django.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.django.R;
import com.example.django.utilities.Constants;
import com.example.django.utilities.PreferenceManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.common.value.qual.StringVal;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class IncomingInvitationActivity extends AppCompatActivity {
    ImageView accept ,reject;
    FirebaseDatabase db;
    DatabaseReference ref;
    TextView firstchar, textusername;

    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);
        firstchar= findViewById(R.id.textFirstChar);

       textusername= findViewById(R.id.textUsername);
        textusername.setVisibility(View.VISIBLE);
    //
        Intent intent= getIntent();
        String value = intent.getStringExtra("sender");
        char values= value.charAt(0);
        String first = String.valueOf(values);
        textusername.setText(value);
       //
        firstchar.setText(first
        );
        preferenceManager= new PreferenceManager((getApplicationContext()));
        accept= findViewById(R.id.imageAcceptInvitation);
        reject=findViewById(R.id.imageRejectInvitation);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //join meetimng
                accept();
                URL server ;
                String secretcode="Django";
                try{
                    server =new URL("https://konferenz.buehl.digital");
                    JitsiMeetConferenceOptions defaultOptions=
                            new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(server)
                                    .setFeatureFlag("welcomepage.enabled", false)

                                    .build();
                    JitsiMeet.setDefaultConferenceOptions(defaultOptions);

                }catch (MalformedURLException e){
                    e.printStackTrace();
                }
                JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                        .setRoom(secretcode)
                        //.setWelcomePageEnabled(false)
                        .setFeatureFlag("welocmepage.enabled", false)
                        .build();
                JitsiMeetActivity.launch(IncomingInvitationActivity.this,options);

            }
        });
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
                Intent intent=  new Intent(IncomingInvitationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    public void reset (){
          String a= preferenceManager.getString(Constants.KEY_FIRST_NAME);
//        db = FirebaseDatabase.getInstance("https://django-7726e-default-rtdb.europe-west1.firebasedatabase.app/");
//        ref= db.getReference("users");
//        //scanformeeting();
//        ref.child(a).child("status").setValue("null");
        db = FirebaseDatabase.getInstance("https://django-7726e-default-rtdb.europe-west1.firebasedatabase.app/");
        ref= db.getReference("users");
        //scanformeeting();
        ref.child(a).child("status").setValue("null");
    }
    public void accept(){
        String a= preferenceManager.getString(Constants.KEY_FIRST_NAME);
//        db = FirebaseDatabase.getInstance("https://django-7726e-default-rtdb.europe-west1.firebasedatabase.app/");
//        ref= db.getReference("users");
//        //scanformeeting();
//        ref.child(a).child("status").setValue("null");
        db = FirebaseDatabase.getInstance("https://django-7726e-default-rtdb.europe-west1.firebasedatabase.app/");
        ref= db.getReference("users");
        //scanformeeting();
        ref.child(a).child("status").setValue("Django");
    }



}