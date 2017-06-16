package com.example.aureobeck.nannyapp;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.ArrayList;

/**
 * Created by moblee on 6/14/17.
 */

public class CountDownTimerSkipFirst extends CountDownTimer {

    public Boolean mFirstTick = true;
    private Double currentFrequencyAccumulated = 0.0;
    private ProgressBar progressBarResult;
    private Double currentFrequencyIncrement = 0.0;

    public CountDownTimerSkipFirst(long millisInFuture,
                                   long countDownInterval,
                                   Boolean mFirstTick,
                                   Double currentFrequencyAccumulated,
                                   ProgressBar progressBarResult,
                                   Double currentFrequencyIncrement) {
        super(millisInFuture, countDownInterval);

        this.mFirstTick = mFirstTick;
        this.currentFrequencyAccumulated = currentFrequencyAccumulated;
        this.progressBarResult = progressBarResult;
        this.currentFrequencyIncrement = currentFrequencyIncrement;

    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (!mFirstTick) {
            incrementProgressBar();
        }
        mFirstTick = false;
    }

    @Override
    public void onFinish() {
        currentFrequencyAccumulated = 0.0;
        onFinishedInterface.onFinishedCount();
    }

    private void incrementProgressBar() {
        currentFrequencyAccumulated = currentFrequencyAccumulated + currentFrequencyIncrement;
        progressBarResult.setProgress(currentFrequencyAccumulated.intValue());
        Log.i("LOG_FREQ_INC: ", "" + currentFrequencyIncrement);
        Log.i("LOG_CURRENT_FREQ_ACC: ", "" + currentFrequencyAccumulated);
    }

    // ******   Interface  *****
    public interface OnFinishedInterface {
        void onFinishedCount();
    }

    public OnFinishedInterface onFinishedInterface = null;
}
