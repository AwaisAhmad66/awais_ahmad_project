package com.example.awais.chatapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView myContactList;
    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mauth;
    String currentUserID;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView =  inflater.inflate( R.layout.fragment_contacts, container, false );
        myContactList = contactsView.findViewById( R.id.contactsList );
        myContactList.setLayoutManager( new LinearLayoutManager( getContext() ) );
            mauth = FirebaseAuth.getInstance();
        currentUserID = mauth.getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child( "Contacts" ).child( currentUserID );
        userRef = FirebaseDatabase.getInstance().getReference().child( "Users" );





        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //using the recycler view options for displaying contacts
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery( contactsRef,Contacts.class ).build();

        FirebaseRecyclerAdapter<Contacts, contactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, contactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final contactsViewHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById( R.id.acceptRequestButton1 ).setVisibility( View.INVISIBLE );
                holder.itemView.findViewById( R.id.DeclineRequestButton ).setVisibility( View.INVISIBLE );

                final String userIDs = getRef( position ).getKey();
                final String[] profileImage = new String[1];
                userRef.child( userIDs ).addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists())){
                            if (dataSnapshot.hasChild( "image" )) {
                                profileImage[0] = dataSnapshot.child( "image" ).getValue( String.class );
                                Picasso.get().load( profileImage[0] ).placeholder( R.drawable.profile_image ).into( holder.userProfileImage );
                            }
                            final String username = dataSnapshot.child( "name" ).getValue(String.class);
                            holder.userName.setText( username );
                            holder.itemView.setOnClickListener( new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent messageIntent = new Intent( getContext(), ChatActivity.class );
                                    messageIntent.putExtra( "user_id",userIDs );
                                    startActivity( messageIntent );

                                }
                            } );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                } );


            }

            @NonNull
            @Override
            public contactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from( viewGroup.getContext() ).inflate( R.layout.users_display_layout, viewGroup, false );
                contactsViewHolder viewHolder = new contactsViewHolder(view);
                return viewHolder;

            }
        };
        myContactList.setAdapter( adapter );
        adapter.startListening();



    }
//accessing the user display layout
    public static class contactsViewHolder extends RecyclerView.ViewHolder{
        TextView userName;
        CircleImageView userProfileImage;
    Button acceptRequestbtn, declineRequestbtn;

        public contactsViewHolder(@NonNull View itemView) {
            super( itemView );
            userName =itemView.findViewById( R.id.users_profile_name );
            userProfileImage = itemView.findViewById( R.id.users_profile_image );
            acceptRequestbtn = itemView.findViewById( R.id.acceptRequestButton1 );
            declineRequestbtn = itemView.findViewById( R.id.DeclineRequestButton );

        }
    }



}
