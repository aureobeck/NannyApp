package com.example.aureobeck.nannyapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class NotifierActivity extends AppCompatActivity {

    // ******   Views  *****

    private static Switch notificationSwitch;
    private static TextView textViewStatus;
    private static ImageView imageViewStatus;

    // ******   Variables  *****
    Context ctx = this;
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
        firebaseRef = new Firebase("https://nanny-app-205da.firebaseio.com/").child("clients").child("1");

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
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        notificationSwitch = (Switch) findViewById(R.id.switchNotification);
        imageViewStatus = (ImageView) findViewById(R.id.imageViewStatus);
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
                    configControlsBabyCrying();
                } else {
                    configControlsBabyNotCrying();
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

    private void configControlsBabyNotCrying() {
        textViewStatus.setText("Está tudo bem!");
        imageViewStatus.setBackgroundResource(R.mipmap.happy_icon);
    }

    private void configControlsBabyCrying() {
        textViewStatus.setText("Bebê Chorando!");
        imageViewStatus.setBackgroundResource(R.mipmap.sad_icon);
        openNotifierDialog();
    }

    // TODO: String
    private void openNotifierDialog() {
        final Dialog dialogNotifier = new Dialog(ctx);
        dialogNotifier.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNotifier.setContentView(R.layout.dialog_one_button);
        dialogNotifier.show();
        dialogNotifier.setCancelable(false);

        final TextView textViewTitle = (TextView) dialogNotifier.findViewById(R.id.textViewTitle);
        textViewTitle.setText("Atenção");

        final TextView textViewDescription = (TextView) dialogNotifier.findViewById(R.id.textViewDescription);
        textViewDescription.setText("Seu bebê está chorando!!");

        final Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        startVibrator(vibrator);

        final Button buttonOk = (Button) dialogNotifier.findViewById(R.id.buttonOk);
        buttonOk.setText("Ok");
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVibrator(vibrator);
                configControlsBabyNotCrying();
                dialogNotifier.cancel();
            }
        });

    }

    private void startVibrator(Vibrator vibrator) {
        long[] pattern = {0, 2000, 1000};
        vibrator.vibrate(pattern, 0);
    }

    private void stopVibrator(Vibrator vibrator) {
        vibrator.cancel();
    }

}


