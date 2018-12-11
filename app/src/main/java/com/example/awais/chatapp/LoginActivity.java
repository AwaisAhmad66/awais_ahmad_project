package com.example.awais.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private Button login;
    private EditText userEmail, userPassword;
    private TextView forgetPassword,newAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );
        initializeFields();
        newAccount.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendUserRegisterActivity();
            }
        } );


    }

    private void sendUserRegisterActivity() {
        Intent registerIntent = new Intent( LoginActivity.this,RegistrationActivity.class );
        startActivity( registerIntent );
    }

    private void initializeFields() {
        login = findViewById( R.id.login_btn );
        userEmail = findViewById( R.id.login_email );
        userPassword = findViewById( R.id.login_password );
        forgetPassword = findViewById( R.id.forget_password_link );
        newAccount = findViewById(R.id.create_account_link);
    }



    protected void onStart() {
        super.onStart();
        if (currentUser!=null){
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent( LoginActivity.this,MainActivity.class );
        startActivity( mainIntent );
    }
}
