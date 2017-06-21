package com.example.aureobeck.nannyapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // ******   Variables  *****
    Context ctx = this;
    private static Button buttonReceiver;
    private static Button buttonNotifier;

    // ******   Inicialization Methods  *****
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
        buttonReceiver = (Button) findViewById(R.id.buttonReceiver);
        buttonNotifier = (Button) findViewById(R.id.buttonNotifier);
    }

    // ******   Action Methods  *****

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
                // *****   Permissions   ******
                if (!checkIfAlreadyhavePermission()) {
                    requestMicrophonePermission();
                } else {
                    Intent intent = new Intent(MainActivity.this, ReceptorActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void requestMicrophonePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, ReceptorActivity.class);
                    startActivity(intent);
                } else {
                    showToast("A permissão é necessária para o funcionamento do app", 4000);
                    NavUtils.navigateUpFromSameTask(this);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void showToast(String message, Integer length) {
        final Toast toast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, length);
    }

}
