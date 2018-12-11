package com.example.awais.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private DatabaseReference myRef;
    private FirebaseAuth myAuth;
    TextView textView;
    private String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myAuth = FirebaseAuth.getInstance();
        currentUser = myAuth.getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference();
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_start );

        Toolbar mToolbar = findViewById( R.id.toolbar_main_page );
        setSupportActionBar( mToolbar );
        Objects.requireNonNull( getSupportActionBar() ).setTitle( R.string.toolbar_title );
        ViewPager view_pager = findViewById( R.id.main_tabs_pager );
        tabsAccessAdopter tabs_accessor_adopter = new tabsAccessAdopter( getSupportFragmentManager() );
        view_pager.setAdapter( tabs_accessor_adopter );
        TabLayout tab_layout = findViewById( R.id.main_tabs_layout );
        tab_layout.setupWithViewPager( view_pager );
        textView = findViewById( R.id.textdemo );


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d( "breakpoint", "this is the start activity" );
        if (currentUser==null) {
            sendToLoginActivity();
        }
        else{

            VerifyUserExistance();

            }

    }



    //send user to setting activity method
    private void sendUserToSettingsActivity() {
        Intent settingIntent = new Intent( MainActivity.this,SettingsActivity.class );
        startActivity( settingIntent );
    }

    //send user to login activity method
    private void sendToLoginActivity() {
        Intent loginIntent = new Intent( MainActivity.this, PhoneRegisterActivity.class );
        loginIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity( loginIntent );
        finish();
    }

    //creating option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu( menu );
        getMenuInflater().inflate( R.menu.option_menu,menu );
        return true;
    }

    //describing behaviour of the option menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected( item );
         if (item.getItemId()==R.id.find_friend_main_menu){
            Intent findFriendIntent = new Intent(MainActivity.this,FindFriendsActivity.class );
            startActivity( findFriendIntent );
             return true;

         }
        if (item.getItemId()==R.id.settings_main_menu){

             sendUserToSettingsActivity();
             return true;
        }

         return true;
    }



    //verify user
    private void VerifyUserExistance() {

          currentUserID= myAuth.getCurrentUser().getUid();
          myRef.child( "Users" ).child( currentUserID ).addValueEventListener( new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot data) {
                  if(!(data.child( "name" ).exists())){


                      sendUserToRegistrationActivity();

                  }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {


              }
          } );

    }


    //send user to login activity method
    private void sendUserToRegistrationActivity() {
        Intent RegisterIntent = new Intent( MainActivity.this,RegistrationActivity.class );
        RegisterIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity( RegisterIntent );
        finish();
    }


}
