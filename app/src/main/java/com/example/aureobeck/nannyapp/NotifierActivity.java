package com.example.aureobeck.nannyapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class NotifierActivity extends AppCompatActivity {

    // ******   Variables  *****
    Context ctx = this;
    private static TextView textViewReceiver;
    private Firebase firebaseRef;


    // ******   Inicialization Rotines  *****

    // TODO: STRING
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // *****   Inicialize Interface   ******
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        // *****   Action Bar   *****
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Receptor");

        // *****   Inicialize Controls  *****
        findViews();

        // *****   Firebase   *****
        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase("https://nanny-app-205da.firebaseio.com/").child("clients").child("1").child("alert");
        Log.i("LOG_FIREBASE",firebaseRef.getKey());
        // *****   Events   *****
        onFirebaseChildEvent();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void findViews() {
        textViewReceiver = (TextView) findViewById(R.id.receiverText);
    }

    // ******   Action Rotines  *****

    public void onFirebaseChildEvent() {
        firebaseRef.addChildEventListener(new com.firebase.client.ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String valueRetrived = dataSnapshot.getValue(String.class);
                if (valueRetrived.equals("1")) {
                    textViewReceiver.setText("ON");
                    Log.i("LOG_FIREBASE", "ON");
                } else {
                    textViewReceiver.setText("OFF");
                    Log.i("LOG_FIREBASE", "OFF");
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

}


