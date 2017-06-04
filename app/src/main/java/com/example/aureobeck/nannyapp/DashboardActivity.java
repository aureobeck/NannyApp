package com.example.aureobeck.nannyapp;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class DashboardActivity extends AppCompatActivity {

    // ******   Views  *****

    private static SeekBar seekBarFrequency;
    private static SeekBar seekBarInterval;
    private static SeekBar seekBarIntensity;

    private static TextView textViewFrequencyThreshold;
    private static TextView textViewIntervalThreshold;
    private static TextView textViewIntensityThreshold;

    private static TextView textViewFrequencyOutput;
    private static TextView textViewIntervalOutput;
    private static TextView textViewIntensityOutput;

    private static ProgressBar progressBarResult;

    // ******   Variables  *****

    Context ctx = this;

    private static Integer currentFrequencyThreshold = 5000;
    private static Integer currentIntervalThreshold = 60;
    private static Integer currentIntensityThreshold = 0;

    private final Long FREQUENCY_EVALUATION_INTERVAL = (long) 5000;

    private static CountDownTimer countDownTimerFrequency;
    private static CountDownTimer countDownTimerIntensity;

    private static Double currentFrequencyIncrement = 1.0;
    private static Double currentFrequencyAccumulated = 1.0;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");


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
        setFrequencyProcessor();

        setIntervalThreshold(currentIntervalThreshold);
        setFrequencyThreshold(currentFrequencyThreshold/95);

        onFrequencyThresholdSeekBarChanged();
        onIntervalThresholdSeekBarChanged();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void findViews() {
        seekBarFrequency = (SeekBar) findViewById(R.id.seekBarFrequency);
        seekBarIntensity = (SeekBar) findViewById(R.id.seekBarIntensity);
        seekBarInterval = (SeekBar) findViewById(R.id.seekBarInterval);

        textViewFrequencyThreshold = (TextView) findViewById(R.id.textViewFrequencyConfiguration);
        textViewIntervalThreshold = (TextView) findViewById(R.id.textViewIntervalConfiguration);
        textViewIntensityThreshold = (TextView) findViewById(R.id.textViewIntensityConfiguration);

        textViewFrequencyOutput = (TextView) findViewById(R.id.textViewFrequencyOutput);
        textViewIntervalOutput = (TextView) findViewById(R.id.textViewIntervalOutput);
        textViewIntensityOutput = (TextView) findViewById(R.id.textViewIntensityOutput);

        progressBarResult = (ProgressBar) findViewById(R.id.progressBarResult);
    }

    private void setFrequencyProcessor() {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewFrequencyOutput.setText(pitchInHz + " Hz");
                            if (pitchInHz > currentFrequencyThreshold) {
                                textViewFrequencyOutput.setTextColor(getResources().getColor(R.color.red_01));
                                setFrequencyEvaluatingCountDown();
                            } else {
                                textViewFrequencyOutput.setTextColor(getResources().getColor(R.color.green_01));
                            }
                    }
                });
            }
        }));
        new Thread(dispatcher, "Audio Dispatcher").start();
    }

    private void onFrequencyThresholdSeekBarChanged() {
        seekBarFrequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setFrequencyThreshold(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void onIntervalThresholdSeekBarChanged() {
        seekBarInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setIntervalThreshold(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setFrequencyThreshold(int progress) {
        currentFrequencyThreshold = progress * 95;
        textViewFrequencyThreshold.setText(currentFrequencyThreshold + " Hz");
    }

    private void setIntervalThreshold(int progress) {

        currentIntervalThreshold = progress;
        Date date = new Date("(currentFrequencyIncrement * 1000+"));
//                textViewIntervalThreshold.setText(simpleDateFormat.format(date));
        textViewIntervalThreshold.setText(currentIntervalThreshold.toString());

        currentFrequencyIncrement = (double) currentIntervalThreshold / 100;
    }

    private void setFrequencyEvaluatingCountDown() {
        countDownTimerFrequency = new CountDownTimer(FREQUENCY_EVALUATION_INTERVAL, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Still Crying
                currentFrequencyAccumulated = currentFrequencyAccumulated+currentFrequencyIncrement;
                progressBarResult.setProgress(Integer.getInteger(progressBarResult.getProgress()+currentFrequencyIncrement+""));
            }

            @Override
            public void onFinish() {
                // Stoped Crying
                progressBarResult.setProgress(Integer.getInteger(progressBarResult.getProgress()+currentFrequencyAccumulated+""));
                currentFrequencyAccumulated = 0.0;
            }
        }.start();
    }

}
