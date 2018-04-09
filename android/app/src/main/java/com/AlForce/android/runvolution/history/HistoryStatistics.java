package com.AlForce.android.runvolution.history;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.AlForce.android.runvolution.R;
import com.AlForce.android.runvolution.SettingsActivity;
import com.AlForce.android.runvolution.utils.DatabaseUpdateListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by iqbal on 17/02/18.
 */

public class HistoryStatistics implements DatabaseUpdateListener {
    public static final String TAG = HistoryStatistics.class.getSimpleName();

    private int avgSteps;
    private float avgDistance;
    private float totalDistance;

    private HistoryDAO historyDAO;

    public HistoryStatistics(HistoryDAO historyDAO) {
        this.historyDAO = historyDAO;
        this.historyDAO.setListener(this);
    }

    public float getTotalDistance() {
        updateTotalDistance();
        return totalDistance;
    }

    private void updateTotalDistance() {
        long historyCount = historyDAO.getQueryCount();
        float distanceSum = 0;
        for (int i = 0; i < historyCount; i++) {
            distanceSum += historyDAO.query(i).getDistance();
        }
        totalDistance = distanceSum;
    }

    public int getAvgSteps() {
        updateAvgSteps();
        return avgSteps;
    }

    private void updateAvgSteps() {
        long historyCount = historyDAO.getQueryCount();
        long stepSum = 0;
        for (int i = 0; i < historyCount; i++) {
            stepSum += historyDAO.query(i).getSteps();
        }
        avgSteps = (int) (stepSum / historyCount);
    }

    public float getAvgDistance() {
        updateAvgDistance();
        return avgDistance;
    }

    private void updateAvgDistance() {
        long historyCount = historyDAO.getQueryCount();
        float distanceSum = 0;
        for (int i = 0; i < historyCount; i++) {
            distanceSum += historyDAO.query(i).getDistance();
        }
        avgDistance = distanceSum / historyCount;
    }

    @Override
    public void onDatabaseUpdate() {
        updateTotalDistance();
        updateAvgDistance();
        updateAvgSteps();
    }
}
