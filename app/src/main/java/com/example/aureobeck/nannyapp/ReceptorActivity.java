package com.example.aureobeck.nannyapp;

import android.content.Context;

import java.util.ArrayList;
import java.util.Random;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.aureobeck.nannyapp.Utils.FirebaseClient;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class ReceptorActivity extends AppCompatActivity {

    // ******   Views  *****

    private static RelativeLayout relativeLayoutConnection;
    private static Switch switchConnection;
    private static TextView textViewSwitch;

    private static SeekBar seekBarFrequency;
    private static SeekBar seekBarInterval;
    private static SeekBar seekBarIntensity;

    private static TextView textViewFrequencyThreshold;
    private static TextView textViewIntervalThreshold;
    private static TextView textViewIntensityThreshold;

    private static TextView textViewFrequencyOutput;
    private static Chronometer chronometerIntervalOutput;
    private static TextView textViewIntensityOutput;

    private static ProgressBar progressBarResult;
    private static Button buttonDefaultParameters;

    // ******   Variables  *****

    Context ctx = this;

    private static final int FREQUENCY_THRESHOLD_DEFAULT = 400;
    private static final int INTENSITY_THRESHOLD_DEFAULT = -60;
    private static final int INTERVAL_THRESHOLD_DEFAULT = 10;

    private static final Long FREQUENCY_EVALUATION_INTERVAL = (long) 5000;
    private static final Integer SAMPLE_RATE = 22050;

    private static Integer currentFrequencyThreshold = FREQUENCY_THRESHOLD_DEFAULT;
    private static Integer currentIntensityThreshold = INTENSITY_THRESHOLD_DEFAULT;
    private static Integer currentIntervalThreshold = INTERVAL_THRESHOLD_DEFAULT;

    private static CountDownTimer countDownTimer;
    private static long currentChronometerValue = 0;

    private static Double currentIncrement = 1.0;
    private static Double currentAccumulator = 0.0;

    private static Firebase firebaseRef;
    private static AudioDispatcher dispatcher;
    private static SilenceDetector silenceDetector;

    private static boolean isChronometerRunning = false;

    // ******   Inicialization Methods  *****

    // TODO: STRING
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // *****   Inicialize Interface   ******
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptor);

        // *****   Action Bar   *****
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Nanny App");

        // *****   Firebase   *****
        Firebase.setAndroidContext(this);

        // *****   Inicialize Controls  *****
        findViews();
        configureControls();

        // *****   Inicialize Processing  *****
        setFrequencyEvaluatingCountDown();
        resetDefaultParameters();
        setDispatcher();
        setIntensityProcessor();
        setFrequencyProcessor();

        // *****  Events  *****
        onConnectionSwitchPressed();
        onFrequencyThresholdSeekBarChanged();
        onIntensityThresholdSeekBarChanged();
        onIntervalThresholdSeekBarChanged();
        onResetParametersButtonClick();
        onChronometerTick();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void findViews() {
        relativeLayoutConnection = (RelativeLayout) findViewById(R.id.relativeLayoutConnection);
        switchConnection = (Switch) findViewById(R.id.switchConnection);

        seekBarFrequency = (SeekBar) findViewById(R.id.seekBarFrequency);
        seekBarIntensity = (SeekBar) findViewById(R.id.seekBarIntensity);
        seekBarInterval = (SeekBar) findViewById(R.id.seekBarInterval);

        textViewFrequencyThreshold = (TextView) findViewById(R.id.textViewFrequencyConfiguration);
        textViewIntensityThreshold = (TextView) findViewById(R.id.textViewIntensityConfiguration);
        textViewIntervalThreshold = (TextView) findViewById(R.id.textViewIntervalConfiguration);

        textViewFrequencyOutput = (TextView) findViewById(R.id.textViewFrequencyOutput);
        textViewIntensityOutput = (TextView) findViewById(R.id.textViewIntensityOutput);
        chronometerIntervalOutput = (Chronometer) findViewById(R.id.chronometerIntervalOutput);

        progressBarResult = (ProgressBar) findViewById(R.id.progressBarResult);
        buttonDefaultParameters = (Button) findViewById(R.id.buttonDefaultParameters);
    }

    private void configureControls() {

        if (getSharedPreferencesFirebaseWriteId().equals("")) {
            relativeLayoutConnection.setBackgroundResource(R.drawable.custom_border_red);
            switchConnection.setText("Sem Conexão");
        } else {
            relativeLayoutConnection.setBackgroundResource(R.drawable.custom_border_green);

            // TODO: Check Connection
            setFirebaseConnectionOn();
        }

        seekBarFrequency.setProgress(currentFrequencyThreshold / 95);
        seekBarIntensity.setProgress(-currentIntensityThreshold);
        seekBarInterval.setProgress(currentIntervalThreshold);

    }

    // ******   DSP Methods  *****

    private void setDispatcher() {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                SAMPLE_RATE,
                1024,
                0
        );
    }

    private void setFrequencyProcessor() {
        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, SAMPLE_RATE, 1024, new PitchDetectionHandler() {

            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewFrequencyOutput.setText(String.format("%.2f", pitchInHz) + " Hz");
                        Double intensityInDb = silenceDetector.currentSPL();
                        textViewIntensityOutput.setText(String.format("%.2f", intensityInDb)+" dB");

                        if (intensityInDb > currentIntensityThreshold) {
                            textViewIntensityOutput.setTextColor(getResources().getColor(R.color.red_01));
                            if (pitchInHz > currentFrequencyThreshold) {
                                // Is Crying - frequency and intensity greater than threshold
                                startChronometer();
                                resetEvaluatingCountDown();
                            }
                        } else {
                            textViewIntensityOutput.setTextColor(getResources().getColor(R.color.green_01));
                        }

                        if (pitchInHz > currentFrequencyThreshold) {
                            textViewFrequencyOutput.setTextColor(getResources().getColor(R.color.red_01));
                        } else {
                            textViewFrequencyOutput.setTextColor(getResources().getColor(R.color.green_01));
                        }
                    }
                });
            }
        }));
        new Thread(dispatcher, "Audio Dispatcher").start();
    }

    private void setIntensityProcessor() {
        silenceDetector = new SilenceDetector(currentIntensityThreshold, false);
        dispatcher.addAudioProcessor(silenceDetector);
    }

    // ******  Chronometer  Methods  *****

    private void startChronometer() {
        if (!isChronometerRunning) {
            chronometerIntervalOutput.setBase(SystemClock.elapsedRealtime()-currentChronometerValue);
            chronometerIntervalOutput.start();
            isChronometerRunning = true;
        }
    }

    private void stopChronometer() {
        currentChronometerValue = SystemClock.elapsedRealtime() - chronometerIntervalOutput.getBase();
        chronometerIntervalOutput.stop();
        isChronometerRunning = false;
    }

    private void resetChronometer() {
        currentChronometerValue = 0;
        chronometerIntervalOutput.setBase(SystemClock.elapsedRealtime());
        chronometerIntervalOutput.stop();
    }

    private void onChronometerTick() {
        chronometerIntervalOutput.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                currentAccumulator += currentIncrement;
                progressBarResult.setProgress(currentAccumulator.intValue());
                if ((SystemClock.elapsedRealtime() - chronometer.getBase()) >= (currentIntervalThreshold*1000) && firebaseRef != null) {
                    firebaseRef.setValue("1");
                    resetNotCrying();
                }
            }
        });
    }

    // ******   Action Methods  *****

    public void onConnectionSwitchPressed() {
        switchConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!switchConnection.isChecked()) {
                    relativeLayoutConnection.setBackgroundResource(R.drawable.custom_border_red);
                    switchConnection.setText("Sem conexão");
                } else {
                    if (getSharedPreferencesFirebaseWriteId().equals("")) {
                        // No id saved, a new one will be created
                        setNewFirebaseId();
                    } else {
                        // Gets the id saved and turns the connection on
                        setFirebaseConnectionOn();
                    }
                }
            }
        });
    }

    private void onFrequencyThresholdSeekBarChanged() {
        seekBarFrequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setFrequencyThreshold((double)progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void onIntensityThresholdSeekBarChanged() {
        seekBarIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setIntensityThreshold(progress);
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

    private void onResetParametersButtonClick() {
        buttonDefaultParameters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDefaultParameters();
            }
        });
    }

    private void resetNotCrying() {
        stopChronometer();
        resetChronometer();
        progressBarResult.setProgress(0);
        currentAccumulator = 0.0;
        countDownTimer.cancel();
        Log.i("LOG", "FINISH_COUNT");
    }

    // ******   Firebase Methods  *****

    private void setNewFirebaseId() {
        Firebase ref = new Firebase("https://nanny-app-205da.firebaseio.com/").child("clients");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> value = (ArrayList<String>) dataSnapshot.getValue();
                createFirebaseClient(value.size());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void createFirebaseClient(Integer code) {
        // Cria cliente Firebase
        Firebase.setAndroidContext(this);
        Firebase ref = new Firebase("https://nanny-app-205da.firebaseio.com/").child("clients");
        Firebase refClient = ref.child(code.toString());
        FirebaseClient firebaseClient = new FirebaseClient("0");
        refClient.setValue(firebaseClient);

        saveSharedPreferencesFirebaseWriteId(code.toString());
        setFirebaseConnectionOn();
    }

    // TODO: Disconnect Firebase
    private void setFirebaseConnectionOn() {
        // TODO: Check Connection
        firebaseRef = new Firebase("https://nanny-app-205da.firebaseio.com/").child("clients").child(getSharedPreferencesFirebaseWriteId()).child("alert");
        Integer writeCode = 54321 - Integer.parseInt(getSharedPreferencesFirebaseWriteId());
        switchConnection.setText("Conectado - Id: " + writeCode);
        relativeLayoutConnection.setBackgroundResource(R.drawable.custom_border_green);
        switchConnection.setChecked(true);
    }

    private String getSharedPreferencesFirebaseWriteId() {
        return readFromPreferences(ctx, "firebaseWriteCode", "", "userConfig");
    }

    private void saveSharedPreferencesFirebaseWriteId(String firebaseId) {
        saveToPreferences(ctx, "firebaseWriteCode", firebaseId, "userConfig");
    }

    // ******   Config Methods  *****

    private void setFrequencyThreshold(Double progress) {
        double frequencyDouble = progress*95;
        currentFrequencyThreshold = (int) Math.round(frequencyDouble);
        textViewFrequencyThreshold.setText(currentFrequencyThreshold + " Hz");
    }

    private void setIntervalThreshold(int progress) {
        currentIntervalThreshold = progress;
        textViewIntervalThreshold.setText(currentIntervalThreshold.toString()+" seg");
        currentIncrement = (((double) 100 / ((double) currentIntervalThreshold)));
    }

    private void setIntensityThreshold (int progress) {
        currentIntensityThreshold = progress-100;
        textViewIntensityThreshold.setText(currentIntensityThreshold.toString()+" dB");
    }

    private void setFrequencyEvaluatingCountDown() {
        countDownTimer = new CountDownTimer(FREQUENCY_EVALUATION_INTERVAL, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                resetNotCrying();
            }
        }.start();
    }

    private void resetEvaluatingCountDown() {
        countDownTimer.cancel();
        countDownTimer.start();
    }

    private void resetDefaultParameters() {
        seekBarFrequency.setProgress(FREQUENCY_THRESHOLD_DEFAULT/95);
        setFrequencyThreshold((double)FREQUENCY_THRESHOLD_DEFAULT/95);
        seekBarIntensity.setProgress(100+INTENSITY_THRESHOLD_DEFAULT);
        setIntensityThreshold(100+INTENSITY_THRESHOLD_DEFAULT);
        seekBarInterval.setProgress(INTERVAL_THRESHOLD_DEFAULT);
        setIntervalThreshold(INTERVAL_THRESHOLD_DEFAULT);
    }

    // ******   General Methods  *****

    private void saveToPreferences(Context context, String preferenceName, String preferenceValue, String preferenceFile) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    private String readFromPreferences(Context context, String preferenceName, String defaultValue, String preferenceFile) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

}
