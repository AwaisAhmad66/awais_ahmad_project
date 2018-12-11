package com.example.awais.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {


    //Declaration of Buttons and EditTexts fields and ProgressBar variables

    private Button registerUser;
    private EditText userName, userEmail;
    private CircleImageView imageView;
    ProgressDialog progressDialog;
    private FirebaseUser currentUser;
    private FirebaseAuth myauth;
    private DatabaseReference databaseRef;
    public static final int galleryPick=1;
    private StorageReference userProfileImageReference;
    private final int gallerypic=1;
    String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_registration );

        //setting Toolbar
        android.support.v7.widget.Toolbar mToolbar= findViewById( R.id.registrationAppBar );
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setDisplayShowHomeEnabled( true );
        getSupportActionBar().setTitle( "Register Your Account" );

        myauth=FirebaseAuth.getInstance();
        currentUser = myauth.getCurrentUser();
        currentUserID = currentUser.getUid();
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child( "image" );
        databaseRef = FirebaseDatabase.getInstance().getReference();


        //calling the initialization method to initialize inputs fields and accessing them

        initializeFields();

        //sending the user to the gallery

        imageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction( Intent.ACTION_GET_CONTENT );
                galleryIntent.setType( "image/*" );
                startActivityForResult( galleryIntent,galleryPick );
            }
        } );

        //Adding functionality to the register user button in the Register button in layout
        registerUser.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (currentUser!=null){
                   createNewAccount();
               }

            }
        } );


    }

    //Validating and sending data to the database
    private void createNewAccount() {

        String email = userEmail.getText().toString().trim();
        String name = userName.getText().toString().trim();

        //fatching the profile image





        //applying validation to the user.

        if (name.isEmpty()) {
            userName.setError( "Name Required" );
            userName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            userEmail.setError( "Email Required" );
            userEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            userEmail.setError( "Email in not valid" );
            userEmail.requestFocus();
            return;
        }

        progressDialog.setTitle( "Creating Account" );
        progressDialog.setMessage( "Please wait..." );
        progressDialog.setCanceledOnTouchOutside( false );
        progressDialog.show();

        //storing user data
           Intent intent = getIntent();
         String phoneNumber = intent.getStringExtra( "phone" );
         String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        HashMap<String,Object>profileMap= new HashMap<>(  );
        profileMap.put( "uId",userId );
        profileMap.put( "name",name );
        profileMap.put( "email",email );
        profileMap.put( "phone",phoneNumber );

        FirebaseDatabase.getInstance().getReference( "Users" ).child( Objects.requireNonNull( FirebaseAuth.getInstance().getCurrentUser() ).getUid() )
                .updateChildren( profileMap ).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText( RegistrationActivity.this, "Registered Successfully", Toast.LENGTH_SHORT ).show();
                    progressDialog.dismiss();
                    //sending user to Main Activity
                    sendUserToMainActivity();

                } else {
                    progressDialog.dismiss();
                    Toast.makeText( RegistrationActivity.this, "Error:" + task.getException(), Toast.LENGTH_LONG ).show();
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
            assert ImageUri != null;
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
                                            Toast.makeText( RegistrationActivity.this, "profile image updated", Toast.LENGTH_SHORT ).show();
                                            progressDialog.dismiss();
                                        }
                                        else{
                                            Toast.makeText( RegistrationActivity.this, "Error:"+task.getException(), Toast.LENGTH_SHORT ).show();
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



    //initializing the input variables
    private void initializeFields() {
        registerUser = findViewById( R.id.signUp );
        userName = findViewById( R.id.user_name );
        userEmail =  findViewById( R.id.user_email );
        imageView = findViewById( R.id.image);
        progressDialog = new ProgressDialog( RegistrationActivity.this );


    }
    //method of sending user to main activity
    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent( RegistrationActivity.this,MainActivity.class );
        mainIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity( mainIntent );
        finish();
}

private void retrieveUserImage(){

    databaseRef.child( "Users" ).child( currentUserID ).addValueEventListener( new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if ((dataSnapshot.exists()) && (dataSnapshot.hasChild( "image" )) && (dataSnapshot.hasChild("name"  ))&& (dataSnapshot.hasChild( "email" ))){

                String retrieveuserName = dataSnapshot.child( "name" ).getValue(String.class);
                String retrieveuserEmail = dataSnapshot.child( "email" ).getValue(String.class);
                String retrieveImage = dataSnapshot.child( "image" ).getValue(String.class);
                Picasso.get().load(retrieveImage).into(imageView);
                userName.setText(retrieveuserName);
                userEmail.setText(retrieveuserEmail);
            }
            else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild( "name" )) && (dataSnapshot.hasChild( "name" ))){
                String retrieveUserName = dataSnapshot.child( "name" ).getValue(String.class);
                String retrieveUserEmail = dataSnapshot.child( "email" ).getValue(String.class);
                userName.setText( retrieveUserName );
                userEmail.setText( retrieveUserEmail );
            }
            else{
                Toast.makeText( RegistrationActivity.this,  "please update your account and profile image", Toast.LENGTH_SHORT ).show();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    } );




}

}
