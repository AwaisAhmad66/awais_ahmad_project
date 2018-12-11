package com.example.awais.chatapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.awais.chatapp.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private String currentUserID;
    private List<User> musers;
    private UserAdapter userAdapter;
    private FirebaseUser fuser;
    private DatabaseReference reference;
    private List<ChatList> userList;
    private List<Chat> mChat;
    boolean addChat = true;


    public ChatsFragment() {
        // Required empty public constructor

    }

    TextView textChat;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);


        fuser = FirebaseAuth.getInstance().getCurrentUser();

        userList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final User[] user = new User[1];
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatList chatList = snapshot.getValue(ChatList.class);

                    userList.add(chatList);

                }
                mchatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());


        return view;
    }

    //updating the token

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }


    private void mchatList() {


        musers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            User user;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    user = snapshot.getValue(User.class);
                    for (ChatList chatList : userList) {
                        if (user.getuId().equals(chatList.getId())) {


                            musers.add(user);

                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), musers, true);
                recyclerView.setAdapter(userAdapter);
                userAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 121:
                userAdapter.removeChat(item.getGroupId());
                userAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }


    }
}
