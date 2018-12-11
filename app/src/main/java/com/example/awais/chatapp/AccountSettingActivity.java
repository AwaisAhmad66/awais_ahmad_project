package com.example.awais.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import javax.xml.transform.Result;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingActivity extends AppCompatActivity {

    private EditText editName,editEmail;
    private Button updateSettings;
    private CircleImageView imageView;
    private String change_name,change_email;
    private FirebaseAuth  myAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;
    private static final int gallerypic=1;
    private String currentUserID;
    ProgressDialog progressDialog;
    private StorageReference userProfileImageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_account_setting );



        myAuth = FirebaseAuth.getInstance();
        currentUser = myAuth.getCurrentUser();
        currentUserID= currentUser.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child( "profile images" );


        Toolbar mToolbar = findViewById( R.id.toolbar_settings_page );
        setSupportActionBar( mToolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setDisplayShowHomeEnabled( true );
        getSupportActionBar().setTitle( "Account Settings" );
        retrieveUserData();
        editName = findViewById( R.id.change_name );
        updateSettings = findViewById( R.id.change_settings_btn);
        editEmail = findViewById( R.id.edit_email );
        imageView = findViewById( R.id.change_profile_image );
        progressDialog = new ProgressDialog( this );

        //sending the user to the gallery
        imageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(  );
                galleryIntent.setAction( Intent.ACTION_GET_CONTENT );
                galleryIntent.setType( "image/*" );
                startActivityForResult( galleryIntent,gallerypic );
            }
        } );



        //updating the settings of the user on the click
        updateSettings.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_name = editName.getText().toString();
                change_email = editEmail.getText().toString();
                if (change_name.isEmpty()){
                    editName.setError( "Name is Empty" );
                    editName.requestFocus();
                    return;
                }
                if (change_email.isEmpty()){
                    editEmail.setError( "Email is Empty" );
                    editEmail.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(change_email).matches()){
                    editEmail.setError( "Email in not valid" );
                    editEmail.requestFocus();
                    return;
                }
                else{

                    HashMap<String, Object> profileMap = new HashMap<>(  );
                    profileMap.put( "name",change_name );
                    profileMap.put( "email",change_email );
                    databaseRef.child( "Users" ).child( currentUserID ).updateChildren( profileMap ).addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText( AccountSettingActivity.this, "Account Updated Successfully", Toast.LENGTH_SHORT ).show();
                                sendUserToMainActivity();
                            }else{
                                String msg = task.getException().toString();
                                Toast.makeText( AccountSettingActivity.this, "Error: "+msg, Toast.LENGTH_SHORT ).show();
                            }

                        }
                    } );


                }
            }

        } );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == gallerypic && resultCode == RESULT_OK && data !=null)
        {
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio( 1,1 )
                    .start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult( data );
            if (resultCode == RESULT_OK){
                progressDialog.setTitle( "setting profile Image" );
                progressDialog.setMessage( "Please Wait" );
                progressDialog.setCanceledOnTouchOutside( false );
                progressDialog.show();
                Uri resultUri = result.getUri();
                final StorageReference filePath = userProfileImageReference.child( currentUserID+".jpg" );

                filePath.putFile( resultUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                        final String downloadUrl = uri.toString();
                        databaseRef.child( "Users" ).child( currentUserID ).child( "image" ).setValue( downloadUrl ).addOnCompleteListener( new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText( AccountSettingActivity.this, "profile image updated", Toast.LENGTH_SHORT ).show();
                                    progressDialog.dismiss();
                                }
                                else{
                                    Toast.makeText( AccountSettingActivity.this, "Error:"+task.getException(), Toast.LENGTH_SHORT ).show();
                                    progressDialog.dismiss();
                                }
                            }
                        } );
                        }
                    } );
                    }
                } );

            }
        }
    }

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent( AccountSettingActivity.this,MainActivity.class );
        mainIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity( mainIntent );
        finish();
    }

//Retrieving user data

    private void retrieveUserData(){

        databaseRef.child( "Users" ).child( currentUserID ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild( "image" )) && (dataSnapshot.hasChild("name"  ))&& (dataSnapshot.hasChild( "email" ))){

                    String retrieveuserName = dataSnapshot.child( "name" ).getValue(String.class);
                    String retrieveuserEmail = dataSnapshot.child( "email" ).getValue(String.class);
                    String retrieveImage = dataSnapshot.child( "image" ).getValue(String.class);
                    Picasso.get().load(retrieveImage).placeholder( R.drawable.profile_image ).into(imageView);
                    editName.setText(retrieveuserName);
                    editEmail.setText(retrieveuserEmail);
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild( "name" )) && (dataSnapshot.hasChild( "name" ))){
                    String retrieveUserName = dataSnapshot.child( "name" ).getValue(String.class);
                    String retrieveUserEmail = dataSnapshot.child( "email" ).getValue(String.class);
                    editName.setText( retrieveUserName );
                    editEmail.setText( retrieveUserEmail );
                }
                else{
                    Toast.makeText( AccountSettingActivity.this,  "please update your account and profile image", Toast.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

    }




}


