package com.AlForce.android.runvolution;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.AlForce.android.runvolution.history.HistoryDAO;
import com.AlForce.android.runvolution.history.HistoryItem;
import com.AlForce.android.runvolution.history.HistoryStatistics;
import com.AlForce.android.runvolution.location.LocationService;
import com.AlForce.android.runvolution.sensor.StepDetector;
import com.AlForce.android.runvolution.timer.Timer;
import com.AlForce.android.runvolution.utils.DatabaseOpenHelper;
import com.AlForce.android.runvolution.utils.DatabaseUpdateListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String TAG_TOTAL_DISTANCE = "totalDistance";
    private static final String TAG_BUTTON_TEXT = "button";
    private static final String TAG_STEP = "step";
    private static final String TAG_DISTANCE = "distance";
    private static final String TAG_TIMER = "timer";
    private static final String LINE_TEXT_SCHEME = "line://msg/text/?";
    private static final String DISTANCE_UNIT = " m";

    private TextView nameView;
    private TextView welcomeView;
    private ImageButton shareButton;
    private Button timerButton;
    private TextView timerTextView;
    private TextView distanceTextView;
    private TextView stepTextView;
    private TextView totalDistanceTextView;
    private Timer timer;

    private DatabaseOpenHelper dbHelper;
    private DatabaseUpdateListener updateListener;
    private HistoryStatistics statistics;
    private HistoryDAO historyDAO;

    /* Location Service Variables */
    private LocationService mLocationService;
    private Location mCurrentLocation;
    private float totalDistance;
    private float currentDistance;

    /* Step Counter Variables */
    private SensorManager mSensorManager;
    private Sensor mStepCounter;
    private StepDetector mStepDetector;
    private int currentSteps;
    private int cumulativeSteps;

    /* Bluetooth and AArduino Attributes */
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initializeHistoryAccess();
        initializeStepCounter();
        if (LocationService.isGooglePlayServicesAvailable(getContext())){
            initializeLocationService();
        } else {
            getActivity().finish();
        }
        initializeBluetooth();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(TAG_TOTAL_DISTANCE, totalDistance);
        outState.putInt(TAG_STEP, currentSteps);
        outState.putFloat(TAG_DISTANCE, currentDistance);
        outState.putCharSequence(TAG_BUTTON_TEXT, timerButton.getText());
        outState.putLong(TAG_TIMER, timer.millis);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ScrollView scrollView = (ScrollView) getView().findViewById(R.id.home_scroll_view);
        int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin) + 10, getResources().getDisplayMetrics());
        scrollView.setPadding(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
        welcomeView = (TextView) getView().findViewById(R.id.welcomeMessage);
        nameView = (TextView) getView().findViewById(R.id.welcomeMessage_user);
        shareButton = (ImageButton) getView().findViewById(R.id.distanceShareButton);
        timerButton = (Button) getView().findViewById(R.id.timerButton);
        timerTextView = (TextView) getView().findViewById(R.id.timerView);
        distanceTextView = (TextView) getView().findViewById(R.id.distanceView);
        stepTextView = (TextView) getView().findViewById(R.id.stepCounterView);
        totalDistanceTextView = (TextView) getView().findViewById(R.id.totalDistanceView);

        initializeHistoryAccess();

        timer = new Timer(timerTextView);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                if (button.getText().equals("STOP")) {
                    stopRecording();
                    button.setText("START");
                } else {
                    startRecording();
                    button.setText("STOP");
                }
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareButtonHandler(v);
            }
        });

        SharedPreferences preferences = this.getActivity().getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
        String name = preferences.getString("name",null);
        if (name != null) {
            if (name.contains(" ")) {
                String nickName = name.substring(0, name.indexOf(" "));
                nameView.setText(nickName);
            }
        }

        if (savedInstanceState != null) {
            int step = savedInstanceState.getInt(TAG_STEP);
            float distance = savedInstanceState.getFloat(TAG_DISTANCE);
            long millis = savedInstanceState.getLong(TAG_TIMER);

            timerButton.setText(savedInstanceState.getCharSequence(TAG_BUTTON_TEXT));
            stepTextView.setText(Integer.toString(step));
            distanceTextView.setText(formatFloatToString(distance));
            timer.startTime = millis;
            timer.timerHandler.postDelayed(timer.timerRunnable, 0);
        } else {
            timerButton.setText("START");
        }

        totalDistance = statistics.getTotalDistance();
        totalDistanceTextView.setText(formatFloatToString(totalDistance));
    }

    private void initializeStepCounter() {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (mStepCounter == null) {
            Log.e(TAG, "initializeStepCounter: Step counter is not supported.");
        }
        mStepDetector = new StepDetector();
        mStepDetector.setOnStepListener(new StepDetector.OnStepListener() {
            @Override
            public void onStep(int count) {
                currentSteps++;
                if (stepTextView != null) {
                    stepTextView.setText(Integer.toString(currentSteps));
                }
                Log.d(TAG, "onStep: " + currentSteps + " steps");
            }
        });
    }

    private void initializeHistoryAccess() {
        dbHelper = new DatabaseOpenHelper(getContext());
        historyDAO = new HistoryDAO(dbHelper);
        statistics = new HistoryStatistics(historyDAO);
        updateListener = new DatabaseUpdateListener() {
            @Override
            public void onDatabaseUpdate() {
                totalDistance = statistics.getTotalDistance();
                totalDistanceTextView.setText(formatFloatToString(totalDistance));
                SharedPreferences preferences = HomeFragment.this.getActivity().getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
                float currentRecord = preferences.getFloat("currentRecord",-999);
                if ((currentRecord != -999) && (totalDistance > currentRecord)) {
                    String email = preferences.getString("email",null);
                    RecordUpdaterTask updater = new RecordUpdaterTask(email,totalDistance);
                    updater.execute((Void) null);
                }
            }
        };
        historyDAO.setListener(updateListener);

    }

    private void initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(),"This device does not support bluetooth communication",Toast.LENGTH_LONG).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                Log.d("Bluetooth test","deviceName : " + deviceName + "deviceHardwareAddress : " + deviceHardwareAddress);
            }
        }
    }

    private void startRecording() {
        currentDistance = 0;
        currentSteps = 0;
        stepTextView.setText(formatFloatToString(currentSteps));
        distanceTextView.setText(formatFloatToString(currentDistance));

        mSensorManager.registerListener(
                mStepDetector,
                mStepCounter,
                SensorManager.SENSOR_DELAY_UI
        );

        if (mLocationService != null) {
            mLocationService.startLocationUpdates();
        }
        timer.startTime = System.currentTimeMillis();
        timer.timerHandler.postDelayed(timer.timerRunnable, 0);
    }

    private void stopRecording() {
        if (mLocationService != null) {
            if (mLocationService.isConnected()) {
                mLocationService.stopLocationUpdates();
            }
        }

        mSensorManager.unregisterListener(mStepDetector);

        timer.timerHandler.removeCallbacks(timer.timerRunnable);
        saveCurrentRecord();
    }

    private void saveCurrentRecord() {
        HistoryItem record = new HistoryItem();
        record.setDate(new Date(new Date().getTime()));
        record.setDistance(currentDistance);
        record.setSteps(currentSteps);

        long newId = historyDAO.insert(record);
        Log.d(TAG, "saveCurrentRecord: Created item with id="+newId);
    }

    private void initializeLocationService() {
        mLocationService = new LocationService(getContext());
        mLocationService.setLocationServiceListener(
                new LocationService.LocationServiceListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (mCurrentLocation == null) {
                            currentDistance = 0;
                        } else {
                            currentDistance += mCurrentLocation.distanceTo(location);
                        }
                        mCurrentLocation = location;
                        distanceTextView.setText(formatFloatToString(currentDistance));
                        Log.d(TAG, "onLocationChanged: " + currentDistance + " meters.");
                    }
                });
        Log.d(TAG, "initializeLocationService: initialized.");
        Log.d(TAG, "initializeLocationService: " + mLocationService.isConnected());
    }

    public void shareButtonHandler(View view) {
        String message = getString(R.string.shareMessage) + totalDistance
                + getString(R.string.distanceUnit);
        Uri lineMessage = Uri.parse(LINE_TEXT_SCHEME + message);
        Intent lineIntent = new Intent(Intent.ACTION_VIEW, lineMessage);

        PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(lineIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (activities.size() > 0) {
            startActivity(lineIntent);
            Log.d(TAG, "shareButtonHandler: Intent handler exists.");
        } else {
            Log.d(TAG, "shareButtonHandler: Intent handler does not exist.");
        }
    }

    private String formatFloatToString(float unformattedFloat) {
        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(unformattedFloat) + DISTANCE_UNIT;
    }

    public static class RecordUpdaterTask extends AsyncTask<Void, Void, Boolean> {
        @NonNull
        private final String email;
        private final float record;

        RecordUpdaterTask(String email, float record) {
            this.email = email;
            this.record = record;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String address = "https://runvolution.herokuapp.com/updaterecord";
            String param = "?email=" + email + "&record=" + record;
            HttpsURLConnection httpsPatch = null;
            BufferedReader reader = null;
            String msg = null;

            try {
                URL urlAddress = new URL(address + param);
                httpsPatch = (HttpsURLConnection) urlAddress.openConnection();
                httpsPatch.setRequestMethod("PATCH");
                httpsPatch.connect();
                reader = new BufferedReader(new InputStreamReader(httpsPatch.getInputStream()));
                String inputLine;
                StringBuilder buffer = new StringBuilder();
                int respCode = httpsPatch.getResponseCode();
                while ((inputLine = reader.readLine()) != null) {
                    buffer.append(inputLine);
                }
                msg = buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (httpsPatch != null) {
                    httpsPatch.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (msg == null) {
                    Log.d("The server responded with : ", "Message is null");
                } else {
                    Log.d("The server responded with : ", msg);
                }

            }
            if (msg != null) {
                return "OK".equals(msg);
            } else {
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Log.d(TAG, "Updated user record on server database");
                //Toast.makeText(HomeFragment.this.getActivity().getApplicationContext(), "Congratulations, You just achieved a new record", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG,"Failed to update record");
                //Toast.makeText(HomeFragment.this.getActivity().getApplicationContext(), "Update record failed", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {
            //Empty
        }

    }

}
