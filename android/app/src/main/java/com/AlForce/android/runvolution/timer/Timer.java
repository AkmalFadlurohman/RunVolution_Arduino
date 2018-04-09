package com.AlForce.android.runvolution.timer;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by fariz on 2/17/2018.
 */

public class Timer {
    public TextView timerTextView;
    public long startTime = 0;
    public long millis;

    public Timer(TextView timerTextView) {
        this.timerTextView = timerTextView;
    }

    // runs without a timer by reposting this handler at the end of the runnable
    public Handler timerHandler = new Handler();
    public Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };
}
