package com.example.django.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.django.R;
import com.example.django.adapters.UserAdapter;
import com.example.django.listeners.UsersListener;
import com.example.django.models.User;
import com.example.django.utilities.Constants;
import com.example.django.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.messaging.FirebaseMessaging;
import com.example.django.utilities.Constants;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionService;

public class MainActivity extends AppCompatActivity  implements UsersListener {
    private PreferenceManager preferenceManager;
    private List<User> users;
    private UserAdapter usersAdapter;
    private TextView textErrorMessage;
   private SwipeRefreshLayout swipeRefreshLayout;
   FirebaseDatabase db;
   DatabaseReference ref, refs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager((getApplicationContext()));
        TextView textTitle= findViewById(R.id.textTitle);
        textTitle.setText(String.format(
                "%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(
                        Constants.KEY_LAST_NAME
                )
        ));
        findViewById(R.id.textSignOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        // doubt
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful() && task.getResult()!= null){
                    sendFCMTokenToDatabase(task.getResult());
                }
            }
        });
        RecyclerView userRecyclerView= findViewById(R.id.usersRecyclerView);
       textErrorMessage= findViewById(R.id.textErrorMessage);

        users= new ArrayList<>();
        usersAdapter= new UserAdapter(users, this);
        userRecyclerView.setAdapter(usersAdapter);
        swipeRefreshLayout=findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);
        getUsers();
    }
    private void getUsers(){
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                String myUserId= preferenceManager.getString(Constants.KEY_USER_ID);
                swipeRefreshLayout.setRefreshing(false);
                if(task.isSuccessful() && task.getResult() != null){
                    users.clear();
                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                        if(myUserId.equals(documentSnapshot.getId())){
                            continue;
                        }
                        User user = new User();
                        user.firstName= documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                        user.lastName=documentSnapshot.getString(Constants.KEY_LAST_NAME);
                        user.email=documentSnapshot.getString(Constants.KEY_EMAIL);
                        user.token= documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                        users.add(user);

                    }
                    if (users.size() >0){
                        usersAdapter.notifyDataSetChanged();
                    }
                    else{
                        textErrorMessage.setText(String.format("%s","No users Available"));
                        textErrorMessage.setVisibility(View.VISIBLE);
                    }
                }else {
                    textErrorMessage.setText(String.format("%s","No users Available"));
                    textErrorMessage.setVisibility(View.VISIBLE);
                }
            }
        });
        scanformeeting();
        reset();
   //addded bby me
//        Users users= new Users("aditya", "null");
//        db = FirebaseDatabase.getInstance("https://django-7726e-default-rtdb.europe-west1.firebasedatabase.app/");
//        ref= db.getReference("users");
//        ref.child("aditya").setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                Toast.makeText(MainActivity.this,"ADDED succuessfully ", Toast.LENGTH_SHORT).show();
//            }
//        });
        //added by me

    }

    private void sendFCMTokenToDatabase( String token){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        DocumentReference documentReference= database.collection(Constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
               // Toast.makeText(MainActivity.this,"Token is added",Toast.LENGTH_SHORT).show();
            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Failed at adding the token "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void signOut(){
        Toast.makeText(MainActivity.this,"Signing Out ....",Toast.LENGTH_SHORT).show();
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        DocumentReference documentReference=  database.collection(Constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                preferenceManager.classPreferences();
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Unable to Sign Out ",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void initiateVideoMeeting(User user) {
        if(user.token== null || user.token.trim().isEmpty()){
            Toast.makeText(MainActivity.this,"no video"+user.firstName+" "+user.lastName,Toast.LENGTH_SHORT).show();
          //  String a =user.


        }else{

            Toast.makeText(MainActivity.this,"Video Meeting with"+user.firstName+""+user.lastName,Toast.LENGTH_SHORT).show();
            //added by me
            String a= user.firstName;
            String room = user.firstName+user.lastName;
            Users users= new Users("aditya", "null");
            db = FirebaseDatabase.getInstance("https://django-7726e-default-rtdb.europe-west1.firebasedatabase.app/");
            ref= db.getReference("users");
            //scanformeeting();
            ref.child(a).child("status").setValue("DjangoInvited");
            URL server ;
            String secretcode="Django";
           ref= db.getReference().child("users").child(a).child("status");
           ref.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                  String statuss= snapshot.getValue(String.class);
                  if("DjangoInvited".equals(statuss)){
                      Intent intent  = new Intent(MainActivity.this , OutgoingInvitationActivity.class);
                      intent.putExtra("recievername",a);
                      startActivity(intent);
                  }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });

//            try{
//                server =new URL("https://konferenz.buehl.digital");
//                JitsiMeetConferenceOptions defaultOptions=
//                        new JitsiMeetConferenceOptions.Builder()
//                                .setServerURL(server)
//                                .setFeatureFlag("welcomepage.enabled", false)
//
//                                .build();
//                JitsiMeet.setDefaultConferenceOptions(defaultOptions);
//
//            }catch (MalformedURLException e){
//                e.printStackTrace();
//            }
//            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
//                    .setRoom(secretcode)
//                    //.setWelcomePageEnabled(false)
//                    .setFeatureFlag("welocmepage.enabled", false)
//                    .build();
//            JitsiMeetActivity.launch(MainActivity.this,options);
            //
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(MainActivity.this,"e.getMessage().toString()",Toast.LENGTH_SHORT).show();
//                }
//            });

//            DatabaseReference database= FirebaseDatabase.getInstance().getReference("users");
//            HashMap User= new HashMap();
//            User.put("status","room");
//            database.child("Aditya").updateChildren(User);
//            database.child("test").child("status").setValue("abcd");

           // Toast.makeText(MainActivity.this,"data daeed",Toast.LENGTH_SHORT).show();
            //added nny me
//            Intent intent = new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
//            intent.putExtra("user", user);
//            intent.putExtra("type", "video");
//            startActivity(intent);
        }
    }

    @Override
    public void intitateAudioMeeting(User user) {
        if(user.token== null || user.token.trim().isEmpty()){
            Toast.makeText(MainActivity.this,"no Audio Meeting"+user.firstName+" "+user.lastName,Toast.LENGTH_SHORT).show();
        }else{
           Intent intent = new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
           intent.putExtra("user", user);
            intent.putExtra("type", "video");
            startActivity(intent);
            // Toast.makeText(MainActivity.this,"Audio Meeting with"+user.firstName+""+user.lastName,Toast.LENGTH_SHORT).show();
        }
    }
    public void scanformeeting(){
        String a= preferenceManager.getString(Constants.KEY_FIRST_NAME);
        db = FirebaseDatabase.getInstance("https://django-7726e-default-rtdb.europe-west1.firebasedatabase.app/");
        ref= db.getReference("users");
        ref.child(a).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    DataSnapshot dataSnapshot=task.getResult();
                    String statusss= String.valueOf(dataSnapshot.child("status").getValue());

                    refs= db.getReference("users").child(a).child("status");
                    refs.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String value=snapshot.getValue(String.class);
                            if("DjangoInvited".equals(value)){
                                //String statussss="Django";
                               String statuss="DjangoInvited";
                                joinmeeting(statuss,a);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //joinmeeting(statusss);
                }
            }
        });

    }
    public void joinmeeting(String statuss, String a){
        if(Objects.equals(statuss, "DjangoInvited")){
            String Sendername= a;
            Toast.makeText(MainActivity.this, "statusss found", Toast.LENGTH_SHORT).show();
            Intent intent= new Intent(MainActivity.this, IncomingInvitationActivity.class);
           intent.putExtra("sender", Sendername);
            intent.putExtra("key",statuss);
            startActivity(intent);
        }
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
}