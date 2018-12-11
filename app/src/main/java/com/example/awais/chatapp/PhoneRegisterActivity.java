package com.example.awais.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneRegisterActivity extends AppCompatActivity {
    public static final String TAG = "ChatApp";
    CountryCodePicker cpp;
    private EditText number, verifyNumber;
    private Button sndbtn, verifybtn;
    private FirebaseAuth myauth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    ProgressDialog progressBar;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        myauth = FirebaseAuth.getInstance();
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_phone_register );

        initialization();
        sndbtn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                String phoneNumber = cpp.getFullNumberWithPlus();

                //validating the phone number is not empty

                if (phoneNumber.isEmpty()) {
                    number.setError( "Phone Number Required" );
                    number.requestFocus();
                }
                else {

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,                                        // Phone number to verify
                            60,                                             // Timeout duration
                            TimeUnit.SECONDS,                                 // Unit of timeout
                            PhoneRegisterActivity.this,               // Activity (for callback binding)
                            mCallbacks
                    );


                }
            }
        } );

        //setting Onclick Listener to the verify button

        verifybtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cpp.setVisibility( View.INVISIBLE );
                number.setVisibility( View.INVISIBLE );
                sndbtn.setVisibility( View.INVISIBLE );
                String verificationCode = verifyNumber.getText().toString();
                if (verificationCode.isEmpty()) {
                    verifyNumber.setError( "verification code required" );
                    verifyNumber.requestFocus();
                } else {
                    progressBar.setTitle( " authenticating code" );
                    progressBar.setMessage( "please wait..." );
                    progressBar.setCanceledOnTouchOutside( false );
                    progressBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential( mVerificationId, verificationCode );
                    signInWithPhoneAuthCredential( credential );
                }

            }
        } );

        //Verifying the phone Number

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential( phoneAuthCredential );
                verifybtn.setVisibility( View.VISIBLE );
                verifyNumber.setVisibility( View.VISIBLE );
                cpp.setVisibility( View.INVISIBLE );
                number.setVisibility( View.INVISIBLE );
                sndbtn.setVisibility( View.INVISIBLE );
                progressBar.dismiss();

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText( PhoneRegisterActivity.this,
                        "invalid phone number please enter the correct number",
                        Toast.LENGTH_SHORT ).show();
                verifybtn.setVisibility( View.INVISIBLE );
                verifyNumber.setVisibility( View.INVISIBLE );
                cpp.setVisibility( View.INVISIBLE );
                number.setVisibility( View.INVISIBLE );
                sndbtn.setVisibility( View.VISIBLE );
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                Log.d( TAG, "onCodeSent:" + verificationId );

                mVerificationId = verificationId;
                mResendToken = token;

                verifybtn.setVisibility( View.VISIBLE );
                verifyNumber.setVisibility( View.VISIBLE );
                cpp.setVisibility( View.INVISIBLE );
                number.setVisibility( View.INVISIBLE );
                sndbtn.setVisibility( View.INVISIBLE );
                progressBar.dismiss();
            }
        };
    }

    //initialization of the variables of the Layout File
    private void initialization() {

        number =  findViewById( R.id.phoneNumber );
        verifyNumber = findViewById( R.id.verifyCode );
        sndbtn = findViewById( R.id.sendCodebtn );
        verifybtn = findViewById( R.id.verifyCodebtn );
        progressBar = new ProgressDialog( PhoneRegisterActivity.this );

        cpp =findViewById( R.id.cpp );
        cpp.registerCarrierNumberEditText( number);

        //setting progress bar and numbers to invisible
        verifybtn.setVisibility( View.INVISIBLE );
        verifyNumber.setVisibility( View.INVISIBLE );


    }


    //SignIn the user
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        myauth.signInWithCredential( credential )
                .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d( TAG, "signInWithCredential:success" );

                            FirebaseUser user = task.getResult().getUser();
                            String number = user.getPhoneNumber();
                            sendUserToRegisterActivity(number);


                        } else {

                            Log.w( TAG, "signInWithCredential:failure", task.getException() );
//                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//
//                            }
                        }
                    }
                } );
    }
private void sendUserToRegisterActivity(String number){

    Intent Registerintent = new Intent( PhoneRegisterActivity.this, RegistrationActivity.class );
    Registerintent.putExtra( "phone", number );
    Registerintent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK );
    startActivity( Registerintent );
    finish();




}

}
