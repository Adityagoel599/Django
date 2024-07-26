package com.example.django.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.django.R;
import com.example.django.models.User;
import com.example.django.network.ApiClient;
import com.example.django.network.ApiService;
import com.example.django.utilities.Constants;
import com.example.django.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {
private PreferenceManager preferenceManager;
private String inviterToken = null;
    FirebaseDatabase db;
    DatabaseReference ref, refs;
    TextView firschar, textusername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);
        firschar= findViewById(R.id.textFirstChar);
        textusername= findViewById(R.id.textUsername);
        Intent intent= getIntent();
        String value = intent.getStringExtra("recievername");
        char values= value.charAt(0);
        String first= String.valueOf(values);
        //
        firschar.setText(
            first
        );
        textusername.setVisibility(View.VISIBLE);
        textusername.setText(value);
        Handler handler= new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    invitationaccepted();
                }
            },10000);
        }

        preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful() && task.getResult()!=null){
                  inviterToken= task.getResult();
                }
            }
        });
        ImageView imageMeetingType= findViewById(R.id.imageMeetingType);
        String meetingType= getIntent().getStringExtra("type");
            if(meetingType!= null ){
                if(meetingType.equals("video")){
                    imageMeetingType.setImageResource(R.drawable.ic_video);
                }
            }
        TextView textFirstChar= findViewById(R.id.textFirstChar);
        TextView textUsername= findViewById(R.id.textUsername);
        TextView textEmail= findViewById(R.id.textEmail);
        User user = (User)getIntent().getSerializableExtra("user");
        if(user!=null){
            textFirstChar.setText(user.firstName.substring(0,1));
            textUsername.setText(String.format("%s %s", user.firstName, user.lastName));
            textEmail.setText(user.email);

        }
        ImageView imageStopInvitation= findViewById(R.id.imageStopInvitation);
        imageStopInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if (meetingType!= null && user!=null){
         initiateMeeting(meetingType, user.token);
        }
    }
    private void initiateMeeting(String meetingType, String recieverToken){
        try {
            JSONArray tokens= new JSONArray();
            tokens.put(recieverToken);
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE,meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);
            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION);
        }catch (Exception exception){
            Toast.makeText(OutgoingInvitationActivity.this, exception.getMessage(),Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void  sendRemoteMessage(String remoteMessageBody, String type ){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if(response.isSuccessful()){
                    Toast.makeText(OutgoingInvitationActivity.this, "Invitation accepted",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(OutgoingInvitationActivity.this, response.message(),Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onFailure(@NonNull Call<String>  call, @NonNull Throwable t) {
                Toast.makeText(OutgoingInvitationActivity.this, "Invitation rejected",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }
    public void invitationaccepted(){
        Intent intent = getIntent();
        String recievername= intent.getStringExtra("recievername");
        db = FirebaseDatabase.getInstance("https://django-7726e-default-rtdb.europe-west1.firebasedatabase.app/");
        ref= db.getReference("users").child(recievername).child("status");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String statusss= snapshot.getValue(String.class);
                 if("Django".equals(statusss)){
                     URL server;
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
                             .setFeatureFlag("welcomepage.enabled", false)
                             .build();
                     JitsiMeetActivity.launch(OutgoingInvitationActivity.this,options);
                 } else{
                     Toast.makeText(OutgoingInvitationActivity.this," Invitation was rejected", Toast.LENGTH_SHORT).show();
                     Intent intent= new Intent(OutgoingInvitationActivity.this, MainActivity.class);
                     startActivity(intent);
                 }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }




}
