package com.AlForce.android.runvolution;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    private UserLoginTask authTask = null;
    private EditText emailView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;
    private String LOG_TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        emailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        passwordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "Tapped sign in");
                NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    FirebaseMessaging.getInstance().subscribeToTopic("pet");
                    String token = FirebaseInstanceId.getInstance().getToken();
                    Log.d(LOG_TAG, "User FCM token : " + token);
                    attemptLogin();
                } else {
                    Toast.makeText(LoginActivity.this, "You are not connected to the internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

    }

    private void attemptLogin() {
        emailView.setError(null);
        passwordView.setError(null);
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            authTask = new UserLoginTask(email, password);
            authTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void launchRegister(View view) {
        Log.d(LOG_TAG,"Tapped dont have account string");
        //Toast.makeText(this,"Register new account", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this,RegisterActivity.class));
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        @NonNull
        private final String email;
        @NonNull
        private final String password;

        UserLoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String address = "https://runvolution.herokuapp.com/login";
            HttpsURLConnection httpsPost = null;
            BufferedReader buffer = null;
            String msg = null;

            try {
                URL urlAddress = new URL(address);
                httpsPost = (HttpsURLConnection) urlAddress.openConnection();
                httpsPost.setRequestMethod("POST");
                httpsPost.setDoOutput(true);
                DataOutputStream writer = new DataOutputStream(httpsPost.getOutputStream());
                writer.writeBytes("email=" + email + "&password=" + password);
                writer.flush();
                writer.close();
                buffer = new BufferedReader(new InputStreamReader(httpsPost.getInputStream()));
                String inputLine;
                StringBuilder res = new StringBuilder();
                int respCode = httpsPost.getResponseCode();
                while ((inputLine = buffer.readLine()) != null) {
                    res.append(inputLine);
                }
                msg = res.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (httpsPost != null) {
                    httpsPost.disconnect();
                }
                if (buffer != null) {
                    try {
                        buffer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if  (msg == null) {
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
            authTask = null;
            showProgress(false);

            if (success) {
                new UserDataLoader(email).execute((Void) null);
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
        }
    }

    public class UserDataLoader extends AsyncTask<Void, Void, ArrayList<String>> {
        @NonNull private final String email;

        UserDataLoader(String email) {
            this.email = email;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> jsonData = new ArrayList<String>();
            String address = "https://runvolution.herokuapp.com/fetchuser";
            String param = "?email=" + email;
            HttpsURLConnection httpsGet = null;
            BufferedReader reader = null;
            String msg = null;

            try {
                URL urlAddress = new URL(address + param);
                httpsGet = (HttpsURLConnection) urlAddress.openConnection();
                httpsGet.setRequestMethod("GET");
                httpsGet.connect();
                reader = new BufferedReader(new InputStreamReader(httpsGet.getInputStream()));
                String inputLine;
                StringBuilder buffer = new StringBuilder();
                int respCode = httpsGet.getResponseCode();
                while ((inputLine = reader.readLine()) != null) {
                    buffer.append(inputLine);
                }
                msg = buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (httpsGet != null) {
                    httpsGet.disconnect();
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
            jsonData.add(msg);
            int petId = 0;
            try {
                JSONObject rawAccountData = new JSONObject(msg);
                petId = rawAccountData.getInt("pet_id");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (petId != 0) {
                address = "https://runvolution.herokuapp.com/fetchpet";
                param = "?petid=" + petId;
                httpsGet = null;
                reader = null;
                msg = null;

                try {
                    URL urlAddress = new URL(address + param);
                    httpsGet = (HttpsURLConnection) urlAddress.openConnection();
                    httpsGet.setRequestMethod("GET");
                    httpsGet.connect();
                    reader = new BufferedReader(new InputStreamReader(httpsGet.getInputStream()));
                    String inputLine;
                    StringBuilder buffer = new StringBuilder();
                    int respCode = httpsGet.getResponseCode();
                    while ((inputLine = reader.readLine()) != null) {
                        buffer.append(inputLine);
                    }
                    msg = buffer.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (httpsGet != null) {
                        httpsGet.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if  (msg == null) {
                        Log.d("The server responded with : ", "Message is null");
                    } else {
                        Log.d("The server responded with : ", msg);
                    }
                }
                jsonData.add(msg);
            }
            return jsonData;
        }
        @Override
        protected void onPostExecute(final ArrayList<String> jsonData) {
            String userData = jsonData.get(0);
            String petData = jsonData.get(1);
            String name = null;
            String email = null;
            float previousRecord = -1;
            float currentRecord = -1;
            String petName = null;
            int petLevel = 1;
            int petXP = 0;
            int petId = 0;
            try {
                JSONObject rawAccountData = new JSONObject(userData);
                JSONObject rawPetData = new JSONObject(petData);
                name = rawAccountData.getString("name");
                email = rawAccountData.getString("email");
                previousRecord = (float) rawAccountData.getDouble("previous_record");
                currentRecord = (float) rawAccountData.getDouble("current_record");
                petId = rawPetData.getInt("id");
                petName = rawPetData.getString("name");
                petLevel = rawPetData.getInt("level");
                petXP = rawPetData.getInt("xp");
            } catch (Exception e) {
                e.printStackTrace();
            }
            SharedPreferences preferences = getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("logged",true);
            editor.putString("name",name);
            editor.putString("email",email);
            editor.putFloat("previousRecord",previousRecord);
            editor.putFloat("currentRecord",currentRecord);
            editor.putInt("petId", petId);
            editor.putString("petName", petName);
            editor.putInt("petLevel", petLevel);
            editor.putInt("petXP", petXP);
            editor.apply();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userData",userData);
            intent.putExtra("petData",petData);
            startActivity(intent);
            finish();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

