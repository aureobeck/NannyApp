package com.example.aureobeck.nannyapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;

public class DashboardActivity extends AppCompatActivity {

    // ******   Inicialization Rotines  *****

    // TODO: STRING
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // *****   Inicialize Interface   ******
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // *****   Action Bar   *****
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Nanny App");

        // *****   Inicialize Controls  *****
        findViews();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void findViews(){

    }


}
