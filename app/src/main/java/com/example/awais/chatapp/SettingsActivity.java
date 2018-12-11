package com.example.awais.chatapp;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    Button profileSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar mToolbar = findViewById(R.id.toolbar_settings_page);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        profileSettings = findViewById(R.id.profile_setting);

        profileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(SettingsActivity.this, AccountSettingActivity.class);
                startActivity(profileIntent);
            }
        });

        if (findViewById(R.id.fragment_container) !=null){
            if (savedInstanceState !=null){
                return;
            }
//
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new NotificationActivity.NotificationPreferenceFragment()).commit();
//



        }



        }


}
