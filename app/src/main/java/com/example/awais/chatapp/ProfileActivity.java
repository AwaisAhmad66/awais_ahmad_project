package com.example.awais.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private String receiveUserID, currentState, senderUserID;
    private TextView userName, userEmail,userPhone;
    private ImageView userProfileImage;
    private Button sendRequest, declineRequst;
    private DatabaseReference databaseRef;
    private FirebaseAuth myauth;
    private DatabaseReference chatRequestRef, contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_profile );
        databaseRef = FirebaseDatabase.getInstance().getReference().child( "Users" );
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child( "Chat Requests" );
        contactsRef = FirebaseDatabase.getInstance().getReference().child( "Contacts" );

//        setting toolbar
        android.support.v7.widget.Toolbar mToolbar= findViewById( R.id.visitProfileBar );
        setSupportActionBar(mToolbar);
        Objects.requireNonNull( getSupportActionBar() ).setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setDisplayShowHomeEnabled( true );
        getSupportActionBar().setTitle( "Profile" );
        Intent intent = getIntent();
        receiveUserID =intent.getStringExtra( "visitProfileId" );
        myauth= FirebaseAuth.getInstance();
        senderUserID = myauth.getCurrentUser().getUid();
        initialization();
        retrieveUserData();

    }
//retrieving the user data for displaying on the profile
    private void retrieveUserData() {
        databaseRef.child( receiveUserID ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ( (dataSnapshot.exists()) && (dataSnapshot.hasChild( "image" ))){
                    String userImage = dataSnapshot.child( "image" ).getValue(String.class);
                    String username = dataSnapshot.child( "name" ).getValue(String.class);
                    String useremail = dataSnapshot.child( "email" ).getValue(String.class);
                    String userphone = dataSnapshot.child( "phone" ).getValue(String.class);
                    Picasso.get().load( userImage ).placeholder( R.drawable.profile_image ).into( userProfileImage );
                    userName.setText( username );
                    userEmail.setText( useremail );
                    userPhone.setText( userphone );
                    manageChatRequest();


                }
                else{
                    String username = dataSnapshot.child( "name" ).getValue(String.class);
                    String useremail = dataSnapshot.child( "email" ).getValue(String.class);
                    String userphone = dataSnapshot.child( "phone" ).getValue(String.class);
                    userName.setText( username );
                    userEmail.setText( useremail );
                    userPhone.setText( userphone );
                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }
//Managing the chat requests and storing in the database
    private void manageChatRequest() {

        chatRequestRef.child( senderUserID ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild( receiveUserID )){
                    String reqestType = dataSnapshot.child( receiveUserID ).child( "requestType" ).getValue(String.class);
                    if (reqestType.equals( "sent" )){
                        currentState = "request_sent";
                        sendRequest.setText( "Cancel Chat Request" );
                    }
                    else if (reqestType.equals( "received" )){
                        currentState="requestReceived";
                        sendRequest.setText( "Accept Request" );
                        sendRequest.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                acceptChatRequest();
                            }
                        } );
                        declineRequst.setVisibility( View.VISIBLE );
                        declineRequst.setText( "Decline Request" );
                        declineRequst.setEnabled( true );
                        declineRequst.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelRequest();

                            }
                        } );
                    }
                }
                else{
                    contactsRef.child( senderUserID ).addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiveUserID  )){
                                currentState = "friends";
                                sendRequest.setText( "Remove Contact" );
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    } );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

//checking if the sender is the current user or not
        if (!senderUserID.equals( receiveUserID )){

            sendRequest.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequest.setEnabled( false );
                    if (currentState.equals( "new" )){

                        sendChatRequest();
                    }
                    if (currentState.equals( "request_sent" )){
                        cancelRequest();
                    }
                    if (currentState.equals( "request_received" )){
                        acceptChatRequest();
                        Toast.makeText( ProfileActivity.this, "Freinds woth each other", Toast.LENGTH_SHORT ).show();
                    }
                    if (currentState.equals( "friends" )){
                        removeSpecificContact();
                    }
                }
            } );


        }else {
            sendRequest.setVisibility( View.INVISIBLE );
        }

    }
//removing the contact from the contact list
    private void removeSpecificContact() {
        contactsRef.child( senderUserID ).child( receiveUserID ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                   contactsRef.child( receiveUserID ).child( senderUserID ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendRequest.setEnabled( true );
                                currentState = "new";
                                sendRequest.setText( "send Message Request" );
                            }



                        }
                    } );
                }

            }
        } );

    }


    //accept chat request function

    private void acceptChatRequest() {

        contactsRef.child( senderUserID ).child( receiveUserID ).child( "Contacts" ).setValue( "saved" )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            contactsRef.child( receiveUserID ).child( senderUserID ).child( "Contacts" ).setValue( "saved" )
                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                chatRequestRef.child( senderUserID ).child( receiveUserID ).removeValue()
                                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){

                                                                chatRequestRef.child( receiveUserID ).child( senderUserID ).removeValue()
                                                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){

                                                                                    sendRequest.setEnabled( true );
                                                                                    currentState = "friends";
                                                                                    sendRequest.setText( "Remove Contact" );
                                                                                    declineRequst.setVisibility( View.INVISIBLE );
                                                                                    declineRequst.setEnabled( false );

                                                                                }
                                                                            }
                                                                        } );

                                                            }
                                                            }
                                                        } );


                                            }

                                        }
                                    } );

                        }

                    }
                } );

    }
//cancel the chat request function
    private void cancelRequest() {
        //removing the request data
        chatRequestRef.child( senderUserID ).child( receiveUserID ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    chatRequestRef.child( receiveUserID ).child( senderUserID ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendRequest.setEnabled( true );
                                currentState = "new";
                                sendRequest.setText( "send Message Request" );
                            }



                        }
                    } );
                }

            }
        } );
    }

    //sending chat Request to the receiver user
    private void sendChatRequest() {
        chatRequestRef.child( senderUserID ).child( receiveUserID )
                .child( "requestType" ).setValue( "sent" )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child( receiveUserID ).child( senderUserID )
                                    .child( "requestType" ).setValue( "received" ).addOnCompleteListener( new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        sendRequest.setEnabled( true );
                                        currentState = "request_sent";
                                        sendRequest.setText( "Cancel Chat Request" );
                                    }
                                }
                            } );

                        }
                    }
                } );


    }

//initializing the values
    private void initialization() {
        userName = findViewById( R.id.profileUserName );
        userEmail=findViewById( R.id.profileUserEmail );
        userPhone = findViewById( R.id.profileUserPhone );
        userProfileImage = findViewById( R.id.profileImage );
        sendRequest = findViewById( R.id.sendRequest );
        declineRequst = findViewById( R.id.declineRequest );
        currentState = "new";

    }
}
