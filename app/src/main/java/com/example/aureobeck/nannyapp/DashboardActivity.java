package com.example.aureobeck.nannyapp;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class DashboardActivity extends AppCompatActivity {

    // ******   Variables  *****
    Context ctx = this;
    private static SeekBar seekBarFrequency;
    private static SeekBar seekBarInterval;
    private static SeekBar seekBarIntensity;

    private static TextView textViewFrequencyConfiguration;
    private static TextView textViewIntervalConfiguration;
    private static TextView textViewIntensityConfiguration;

    private static TextView textViewFrequencyOutput;
    private static TextView textViewIntervalOutput;
    private static TextView textViewIntensityOutput;

    private static Integer currentFrequencyThreshold = 0;
    private static Integer currentIntervalThreshold = 0;
    private static Integer currentIntensityThreshold = 0;

    private static Boolean evaluatingFrequency = false;
    private static Boolean evaluatingIntensity = false;

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

        // *****  Events  *****
        setFrequencyThreshold();
        setFrequencyController();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void findViews(){
        seekBarFrequency = (SeekBar) findViewById(R.id.seekBarFrequency);
        seekBarIntensity = (SeekBar) findViewById(R.id.seekBarIntensity);
        seekBarInterval = (SeekBar) findViewById(R.id.seekBarInterval);

        textViewFrequencyConfiguration = (TextView) findViewById(R.id.textViewFrequencyConfiguration);
        textViewIntervalConfiguration = (TextView) findViewById(R.id.textViewIntervalConfiguration);
        textViewIntensityConfiguration = (TextView) findViewById(R.id.textViewIntensityConfiguration);

        textViewFrequencyOutput = (TextView) findViewById(R.id.textViewFrequencyOutput);
        textViewIntervalOutput = (TextView) findViewById(R.id.textViewIntervalOutput);
        textViewIntensityOutput = (TextView) findViewById(R.id.textViewIntensityOutput);
    }

    private void setFrequencyController(){
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewFrequencyOutput.setText(pitchInHz+" Hz");
                        if (pitchInHz > currentFrequencyThreshold && evaluatingFrequency == false) {
                            textViewFrequencyOutput.setTextColor(getResources().getColor(R.color.red_01));
                        }
                    }
                });
            }
        }));
        new Thread(dispatcher,"Audio Dispatcher").start();
    }

    private void setFrequencyThreshold(){
        seekBarFrequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentFrequencyThreshold = progress*95;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
