package com.example.aureobeck.nannyapp;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aureobeck.nannyapp.Utils.CountDownTimerSkipFirst;
import com.example.aureobeck.nannyapp.Utils.FirebaseClient;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class ReceptorActivity extends AppCompatActivity implements CountDownTimerSkipFirst.OnFinishedInterface {

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
    private static TextView textViewIntervalOutput;
    private static TextView textViewIntensityOutput;

    private static ProgressBar progressBarResult;

    // ******   Variables  *****

    Context ctx = this;

    private final Long FREQUENCY_EVALUATION_INTERVAL = (long) 5000;
    private final Integer SAMPLE_RATE = 22050;

    private static Integer currentFrequencyThreshold = 1000;
    private static Integer currentIntervalThreshold = 5;
    private static Integer currentIntensityThreshold = 0;

    private static CountDownTimerSkipFirst countDownTimerFrequency;
    private static CountDownTimer countDownTimerIntensity;

    private static Double currentFrequencyIncrement = 1.0;
    private static Double currentFrequencyAccumulated = 1.0;
    private static Integer evaluationAccumulated = 0;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    private static Firebase firebaseRef;

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
        configFrequencyEvaluatingCountDown();
        setIntervalThreshold(currentIntervalThreshold);
        setFrequencyThreshold(currentFrequencyThreshold / 95);
        setFrequencyProcessor();

        // *****  Events  *****
        onFrequencyThresholdSeekBarChanged();
        onIntervalThresholdSeekBarChanged();
        onConnectionSwitchPressed();

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
        textViewIntervalThreshold = (TextView) findViewById(R.id.textViewIntervalConfiguration);
        textViewIntensityThreshold = (TextView) findViewById(R.id.textViewIntensityConfiguration);

        textViewFrequencyOutput = (TextView) findViewById(R.id.textViewFrequencyOutput);
        textViewIntervalOutput = (TextView) findViewById(R.id.textViewIntervalOutput);
        textViewIntensityOutput = (TextView) findViewById(R.id.textViewIntensityOutput);

        progressBarResult = (ProgressBar) findViewById(R.id.progressBarResult);
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
        seekBarInterval.setProgress(currentIntervalThreshold);
    }

    private void setFrequencyProcessor() {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                SAMPLE_RATE,
                1024,
                0
        );

        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, SAMPLE_RATE, 1024, new PitchDetectionHandler() {

            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewFrequencyOutput.setText(pitchInHz + " Hz");
                        if (pitchInHz > currentFrequencyThreshold) {
                            if (evaluationAccumulated >= 5) {
                                evaluationAccumulated = 0;
                                setFrequencyEvaluatingCountDown();
                            } else {
                                evaluationAccumulated++;
                            }
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

    // ******   Action Methods  *****

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

    @Override
    public void onFinishedCount() {
        currentFrequencyAccumulated = 0.0;
        progressBarResult.setProgress(currentFrequencyAccumulated.intValue());
        countDownTimerFrequency.cancel();
        Log.i("LOG", "FINISH_COUNT");
    }

    public void onConnectionSwitchPressed() {
        switchConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!switchConnection.isChecked()) {
                    relativeLayoutConnection.setBackgroundResource(R.drawable.custom_border_red);
                    switchConnection.setText("Sem conexão");
                } else {
                    if (getSharedPreferencesFirebaseWriteId().equals("")) {
                        setNewFirebaseId();
                    } else {
                        setFirebaseConnectionOn();
                    }
                }
            }
        });
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

    private void setFrequencyThreshold(int progress) {
        currentFrequencyThreshold = progress * 95;
        textViewFrequencyThreshold.setText(currentFrequencyThreshold + " Hz");
    }

    private void setIntervalThreshold(int progress) {

        currentIntervalThreshold = progress;
//        Date date = new Date((currentFrequencyIncrement * 1000+""));
//                textViewIntervalThreshold.setText(simpleDateFormat.format(date));
        textViewIntervalThreshold.setText(currentIntervalThreshold.toString());

        currentFrequencyIncrement = ((double) 1 / (double) 100 * currentIntervalThreshold);
    }

    private void setFrequencyEvaluatingCountDown() {
        countDownTimerFrequency.cancel();
        countDownTimerFrequency.mFirstTick = true;
        countDownTimerFrequency.start();
        Log.i("LOG", "START_COUNT");
    }

    private void configFrequencyEvaluatingCountDown() {
        countDownTimerFrequency = new CountDownTimerSkipFirst(FREQUENCY_EVALUATION_INTERVAL, 250, true, currentFrequencyAccumulated, progressBarResult, currentFrequencyIncrement);
        countDownTimerFrequency.onFinishedInterface = this;
    }

    // ******   General Methods  *****

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
