package com.example.awais.chatapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private View requestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference chatRequestRef, userRef,contactsRef;
    private FirebaseAuth mauth;
    private String cuurentUserID;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView =  inflater.inflate( R.layout.fragment_requests, container, false );


        mauth = FirebaseAuth.getInstance();
        cuurentUserID = mauth.getCurrentUser().getUid();
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child( "Chat Requests" );
        contactsRef = FirebaseDatabase.getInstance().getReference().child( "Contacts" );
        userRef = FirebaseDatabase.getInstance().getReference().child( "Users" );


        myRequestList = requestFragmentView.findViewById( R.id.chatRequestList );
        myRequestList.setLayoutManager( new LinearLayoutManager( getContext() ) );





        return requestFragmentView;
    }

    @Override
    public void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery( chatRequestRef.child( cuurentUserID ),Contacts.class )
                        .build();


        FirebaseRecyclerAdapter<Contacts,requestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, requestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final requestViewHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById( R.id.acceptRequestButton1 ).setVisibility( View.VISIBLE );
                holder.itemView.findViewById( R.id.DeclineRequestButton ).setVisibility( View.VISIBLE );

                final String listUsersID = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef( position ).child( "requestType" ).getRef();
                //getting the data of the request sender
                getTypeRef.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (dataSnapshot.exists()){
                            String type = dataSnapshot.getValue(String.class);
                            if (type.equals( "received" )){

                                userRef.child( listUsersID ).addValueEventListener( new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild( "image" )){
                                            String profileimage = dataSnapshot.child( "image" ).getValue(String.class);
                                            Picasso.get().load( profileimage ).placeholder( R.drawable.profile_image ).into( holder.profileImagerequest );
                                            }
                                            String username = dataSnapshot.child( "phone" ).getValue(String.class);
                                        holder.userNamerequest.setText( username );

                                        //accept chat request btn

                                        holder.itemView.findViewById( R.id.acceptRequestButton1 ).setOnClickListener( new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //storing contact in the current user database

                                                contactsRef.child( cuurentUserID ).child( listUsersID )
                                                        .child( "Contacts" ).setValue( "saved" )
                                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    //storing data in the sender database
                                                                    contactsRef.child( listUsersID ).child( cuurentUserID )
                                                                            .child( "Contacts" ).setValue( "saved" )
                                                                            .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        //removing chat request from the current user
                                                                                        chatRequestRef.child( cuurentUserID ).child( listUsersID )
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {


                                                                                                        if (task.isSuccessful()){


                                                                                                            //removing chat request from the sender user
                                                                                                            chatRequestRef.child( listUsersID ).child( cuurentUserID )
                                                                                                                    .removeValue()
                                                                                                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                                                            if (task.isSuccessful()){
                                                                                                                                Toast.makeText( getContext(), "Request Accepted", Toast.LENGTH_SHORT ).show();
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
                                        } );
//                                        decline the request button
                                        holder.itemView.findViewById( R.id.DeclineRequestButton ).setOnClickListener( new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                //removing chat request from the current user
                                                chatRequestRef.child( cuurentUserID ).child( listUsersID )
                                                        .removeValue()
                                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {


                                                                if (task.isSuccessful()){


                                                                    //removing chat request from the sender user
                                                                    chatRequestRef.child( listUsersID ).child( cuurentUserID )
                                                                            .removeValue()
                                                                            .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()){
                                                                                        Toast.makeText( getContext(), "Request Declined", Toast.LENGTH_SHORT ).show();
                                                                                    }



                                                                                }
                                                                            } );




                                                                }

                                                            }
                                                        } );







                                            }
                                        } );


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                } );
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                } );

            }

            @NonNull
            @Override
            public requestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from( viewGroup.getContext() )
                        .inflate( R.layout.users_display_layout,viewGroup,false );
                requestViewHolder holder = new requestViewHolder( view );
                return  holder;
            }
        };
        myRequestList.setAdapter( adapter );
        adapter.startListening();




    }

    public static class requestViewHolder extends  RecyclerView.ViewHolder{
        TextView userNamerequest;
        CircleImageView profileImagerequest;
        Button acceptRequestbtn, declineRequestbtn;
        public requestViewHolder(@NonNull View itemView) {
            super( itemView );
            userNamerequest = itemView.findViewById( R.id.users_profile_name );
            profileImagerequest = itemView.findViewById( R.id.users_profile_image );
            acceptRequestbtn = itemView.findViewById( R.id.acceptRequestButton1 );
            declineRequestbtn = itemView.findViewById( R.id.DeclineRequestButton );



        }
    }


}
