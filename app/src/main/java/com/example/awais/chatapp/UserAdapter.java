package com.example.awais.chatapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    private Context mContext;
    private List<User> mUser;
    String lstMessage;
    private boolean ischat;

    public UserAdapter(Context mContext, List<User> mUser, boolean ischat){
        this.mContext = mContext;
        this.mUser = mUser;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_user_display_layout,viewGroup,false);

        return new UserAdapter.viewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull viewHolder viewHolder, int i) {

        final User user = mUser.get(i);
        viewHolder.userName.setText(user.getName());
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(viewHolder.userImage);

        if (ischat){
            lastMessage(user.getuId(),viewHolder.lastSeenMsg);
        }else {
            viewHolder.lastSeenMsg.setVisibility(View.GONE);
        }


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("user_id", user.getuId());
                mContext.startActivity(intent);
            }
        });


    }



    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

         private TextView userName, lastSeenMsg;
         private  CircleImageView userImage;


        public viewHolder(@NonNull View itemView) {
            super(itemView);

            userImage=itemView.findViewById(R.id.chat_users_profile_image);
            userName=itemView.findViewById(R.id.chat_users_profile_name);
            lastSeenMsg = itemView.findViewById(R.id.lastSeenMsg);
            itemView.setOnCreateContextMenuListener(this);


        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), 121, 0, "Delete");


        }
    }

    //check for last message
    private  void lastMessage(final String userid, final TextView lastSeenMsg){
        lstMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference  reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                        lstMessage = chat.getMessage();
                    }
                }
                switch (lstMessage){
                    case "default":
                        lastSeenMsg.setText("No Message");
                        break;
                        default:
                            lastSeenMsg.setText(lstMessage);
                            break;
                }
                lstMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }





    public void removeChat(final int position) {
        final User user = mUser.get(position);
        final String firebaseUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser);
            reference.child(user.getuId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        FirebaseDatabase.getInstance().getReference("Chats").child(firebaseUser).child(user.getuId());

                        Toast.makeText(mContext, "Chat Deleted", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();

                    }
                }
            });

    }

}
