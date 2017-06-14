package com.example.aureobeck.nannyapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // ******   Variables  *****
    Context ctx = this;
    private static Button buttonReceiver;
    private static Button buttonNotifier;

    // ******   Inicialization Rotines  *****
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // *****   Inicialize Interface   ******
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);

        // *****   Action Bar   *****
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Nanny App");

        // *****   Inicialize Controls  *****
        findViews();

        // *****   Events   *****
        onButtonNotifierClick();
        onButtonReceiverClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void findViews() {
        buttonReceiver = (Button)findViewById(R.id.buttonReceiver);
        buttonNotifier = (Button)findViewById(R.id.buttonNotifier);
    }

    // ******   Action Rotines  *****

    private void onButtonNotifierClick() {
        buttonNotifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotifierActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onButtonReceiverClick() {
        buttonReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DashboardActivity
                        .class);
                startActivity(intent);
            }
        });
    }


}
