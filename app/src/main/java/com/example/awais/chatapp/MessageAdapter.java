package com.example.awais.chatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.awais.chatapp.Notifications.Data;
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


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.viewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private List<Chat> mChats;
    private FirebaseUser fUser;
    private String imageurl;
    private Context mContext;


    public MessageAdapter(List<Chat> mChats, String imageurl, Context mContext)

    {

        this.mChats = mChats;
        this.imageurl = imageurl;
        this.mContext = mContext;


    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == MSG_TYPE_RIGHT) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_message_right, viewGroup, false);
            return new viewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_message_left, viewGroup, false);
            return new viewHolder(view);
        }

    }


    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.viewHolder holder, int i) {

      Chat chat = mChats.get(i);
        holder.show_message.setText(chat.getMessage());

        Picasso.get().load(imageurl).into(holder.receiverImage);
        if (i == mChats.size() - 1) {
            if (chat.isIsseen()) {
                holder.textSeen.setText("Seen: " + chat.getSendTime());
            } else {
                holder.textSeen.setText("Delivered: " + chat.getSendTime());
            }
        } else {
            holder.textSeen.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }


    //class for message view holder
    public class viewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private TextView show_message, textSeen;
        private CircleImageView receiverImage;


        public viewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            receiverImage = itemView.findViewById(R.id.receiver_image);
            textSeen = itemView.findViewById(R.id.seen_message);
            itemView.setOnCreateContextMenuListener(this);


        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {


            menu.add(this.getAdapterPosition(), 121, 0, "Delete");
            menu.add(this.getAdapterPosition(), 122, 1, "Edit");
            menu.add(this.getAdapterPosition(), 123, 2, "info");


        }
    }


    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChats.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }


    public void removemessage(final int position) {
        final Chat chat = mChats.get(position);
        String firebaseUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (firebaseUser.equals(chat.getSender())) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
            reference.child(chat.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {


                        Toast.makeText(mContext, "Message Deleted", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }





}