package com.example.awais.chatapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.awais.chatapp.Notifications.Client;
import com.example.awais.chatapp.Notifications.Data;
import com.example.awais.chatapp.Notifications.MyResponse;
import com.example.awais.chatapp.Notifications.Sender;
import com.example.awais.chatapp.Notifications.Token;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

     public  String receive;
    TextView messageSenderNameAppbar ;
    CircleImageView senderProfileImageAppbar;
    FirebaseUser fuser;
    ImageButton sendMessageBtn;
    TextView typeMessage;
    DatabaseReference reference;
    List<Chat> mChat;
    MessageAdapter messageAdapter;
    LinearLayoutManager linearLayoutManager;
    RecyclerView  recyclerView;
    ValueEventListener seenListner;
    APIService apiService;
    boolean notify = false;
    public SimpleDateFormat dateFormat;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        //setting toolbar
        android.support.v7.widget.Toolbar customizeChatAppBar = (android.support.v7.widget.Toolbar) findViewById(R.id.message_activity_toolbar);
        setSupportActionBar(customizeChatAppBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);
        //actionbar imported

        //getting intent values
        Intent getintent = getIntent();
       final String  messageReceiverID = getintent.getStringExtra("user_id");
       receive = messageReceiverID;
        messageSenderNameAppbar = (TextView) actionBarView.findViewById(R.id.custom_profile_name);
        senderProfileImageAppbar = (CircleImageView) actionBarView.findViewById(R.id.custom_profile_image);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        typeMessage = findViewById( R.id.type_message );
        sendMessageBtn = findViewById( R.id.send_message_btn );
        //setting up recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.message_adapter);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        mChat = new ArrayList<>();
         dateFormat = new SimpleDateFormat("HH:mm");



        sendMessageBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = typeMessage.getText().toString();
                if (!msg.isEmpty()) {
                    sendMessage( fuser.getUid(), messageReceiverID, msg );
                }else {
                    typeMessage.setError( "cant send empty message" );
                    typeMessage.requestFocus();

                }
                typeMessage.setText( null );
            }
        } );
try {
    reference = FirebaseDatabase.getInstance().getReference().child("Users").child(messageReceiverID);
}catch (Exception e ) {

}
    reference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            messageSenderNameAppbar.setText(user.getName());
            Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(senderProfileImageAppbar);
            readMessage(fuser.getUid(), messageReceiverID, user.getImage());

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
    seenMessage(messageReceiverID);



    }//Oncreate method end bracket




        //send Message method
        private void sendMessage(String sender, final String receiver, String message) {
        String modifytime = "default";


            String currentDateTimeString = dateFormat.format(new Date());
             String id = reference.child( "Chats" ).push().getKey();


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            HashMap<String, Object> hashMap = new HashMap<>(  );
            hashMap.put("id",id);
            hashMap.put( "receiver", receiver );
            hashMap.put( "message", message );
            hashMap.put( "sender", sender );
            hashMap.put( "isseen", false );
            hashMap.put( "sendTime", currentDateTimeString );
            hashMap.put("modify",modifytime);




            reference.child( "Chats" ).child(id).setValue( hashMap );



            //add user to chat Fragment
            final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                    .child(fuser.getUid()).child(receiver);
            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()){
                        chatRef.child("id").setValue(receiver);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            final  String msg = message;
              reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);

                    if (notify) sendNotification(receiver, user.getName(), msg);
                    notify=false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }//end of send message method

    private void sendNotification(final String receiver, final String username, final String message) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");

        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){

                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.drawable.ic_launcher,username+":"+message,"New Message",receiver);


                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code()==200){
                                if (response.body().success != 1){
                                    Toast.makeText(ChatActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    //Read Message method
    private void readMessage( final String myid, final String receiverid, final String imageurl  ) {


        reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {



                    Chat chat = snapshot.getValue(Chat.class);


                    if (chat.getReceiver().equals(myid)&& chat.getSender().equals(receiverid) ||
                            chat.getReceiver().equals(receiverid) && chat.getSender().equals(myid)) {


                            mChat.add(chat);



                    }
                    messageAdapter = new MessageAdapter(mChat, imageurl,ChatActivity.this);
                    recyclerView.setAdapter(messageAdapter);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //check the message is seen or not
    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListner = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }


    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListner);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case 121:
                messageAdapter.removemessage(item.getGroupId());
                messageAdapter.notifyDataSetChanged();
                return true;
            case 122:
                updateMessage(item.getGroupId());
                return true;
            case 123:
                infomessage(item.getGroupId());
                return true;

            default:
                return super.onContextItemSelected(item);




        }


    }
    public void displayMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


private void updateMessage( int itemid){
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    LayoutInflater inflater = getLayoutInflater();
    View dialogView  = inflater.inflate(R.layout.update_message,null);
    dialogBuilder.setView(dialogView);
    final EditText updatemessage = dialogView.findViewById(R.id.update_message);
    Button updatebtn = dialogView.findViewById(R.id.update_btn);
    dialogBuilder.setTitle("Update Message");
    final AlertDialog alertDialog = dialogBuilder.create();
    alertDialog.show();


    final Chat chat = mChat.get(itemid);
    updatemessage.setText(chat.getMessage());

    updatebtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String updatedmessage = updatemessage.getText().toString();
            String currentDateTimeString = dateFormat.format(new Date());

            Chat chat1 = new Chat(chat.getId(),fuser.getUid(),receive,updatedmessage,false, chat.getSendTime(), currentDateTimeString);
            FirebaseDatabase.getInstance().getReference("Chats").child(chat.getId()).setValue(chat1)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ChatActivity.this, "Message Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            messageAdapter.notifyDataSetChanged();
            alertDialog.dismiss();


        }
    });




}




private  void infomessage(int itemid){


    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    LayoutInflater inflater = getLayoutInflater();
    View dialogView  = inflater.inflate(R.layout.message_details,null);
    dialogBuilder.setView(dialogView);
    final TextView receiveTime = dialogView.findViewById(R.id.receive_time);
    final TextView seenmessage = dialogView.findViewById(R.id.seen_status);
    final TextView modifyTime = dialogView.findViewById(R.id.modify_time);
    Button closeBtn = dialogView.findViewById(R.id.closeBtn);
    dialogBuilder.setTitle("Message Details");
    final AlertDialog alertDialog = dialogBuilder.create();
    alertDialog.show();


    Chat chat = mChat.get(itemid);

    receiveTime.setText(chat.getSendTime());

    if (!chat.getModify().equals("default")){
        modifyTime.setText(chat.getModify());
    }




    if (chat.isIsseen()){
        seenmessage.setText("Seen");
    }else {
        seenmessage.setText("Not Seen Yet");
    }
    closeBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            alertDialog.dismiss();
        }
    });


}






}//end of appcompact activity
