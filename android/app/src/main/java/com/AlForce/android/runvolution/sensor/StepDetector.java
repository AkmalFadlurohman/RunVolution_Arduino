package com.AlForce.android.runvolution.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by iqbal on 20/02/18.
 */

public class StepDetector implements SensorEventListener {

    private static final long STEP_LENGTH = 78;
    private static final long KM_TO_METER = 100000;
    private OnStepListener mListener;

    public void setOnStepListener(OnStepListener stepListener){
        this.mListener = stepListener;
    }

    public interface OnStepListener {
        public void onStep(int count);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {
            int count = (int) event.values[0];
            mListener.onStep(count);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // TODO: Move this method to main activity
    private float calculateDistanceInKm(long steps) {
        return (float) (steps*STEP_LENGTH) / (float) KM_TO_METER;
    }
}
