package com.example.awais.chatapp;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {


    private RecyclerView findFriendRecyclerView;
    private EditText searchInput;
    private ImageButton searchBtn;
    private RadioButton email,phone;
    private  Query query;
    private RadioGroup ss;
    private RadioButton rb;

    private DatabaseReference userRef;

    android.support.v7.widget.SearchView searchView;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_find_friends );
        userRef = FirebaseDatabase.getInstance().getReference( "Users" );
        searchInput = findViewById( R.id.search_input );
        searchBtn = findViewById( R.id.search_btn );

        findFriendRecyclerView = findViewById( R.id.find_friends_recycler_list );
        findFriendRecyclerView.setLayoutManager( new LinearLayoutManager( this ) );
        Toolbar mToolbar = findViewById( R.id.find_friends_toolbar );
        setSupportActionBar( mToolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setDisplayShowHomeEnabled( true );
        getSupportActionBar().setTitle( "Find Friends" );
        final RadioGroup rg = findViewById( R.id.searchadvance );




        searchBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rid = rg.getCheckedRadioButtonId();
                rb = findViewById( rid );

                String searchText = searchInput.getText().toString();
                firebaseSearchPeople(searchText);

            }
        } );

    }

    public boolean firebaseSearchPeople(String search){

        if (rb.getText().equals( "Email" )){
             query = userRef.orderByChild( "email" ).startAt( search ).endAt(search + "\uf8ff") ;
        }
        if (rb.getText().equals( "Phone Number" )){
            query = userRef.orderByChild( "phone" ).startAt( search ).endAt(search + "\uf8ff") ;
        }

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(
                query,
                Contacts.class
                ).build();

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(
                options
        ) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contacts model) {

                //Displaying data to the user
                holder.username.setText(model.getPhone());
                Picasso.get().load( model.getImage()).placeholder( R.drawable.profile_image ).into( holder.userProfileImage );

                //getting the user id
                holder.itemView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_profileID = getRef( position ).getKey();
                        Intent visitProfileIntent = new Intent( FindFriendsActivity.this,ProfileActivity.class );
                        visitProfileIntent.putExtra( "visitProfileId", visit_user_profileID );
                        startActivity( visitProfileIntent );
                    }
                } );

            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from( viewGroup.getContext() ).inflate( R.layout.users_display_layout,viewGroup,false );
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder( view );
                return viewHolder;
            }
        };
        findFriendRecyclerView.setAdapter( adapter );
        adapter.startListening();
        return true;

    }


    public class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView username;
        CircleImageView userProfileImage;


    public FindFriendsViewHolder(@NonNull View itemView) {
        super( itemView );
         username = (TextView) itemView.findViewById(R.id.users_profile_name);
        userProfileImage = (CircleImageView) itemView.findViewById(R.id.users_profile_image);


    }
}


}
