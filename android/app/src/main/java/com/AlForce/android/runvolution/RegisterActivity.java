package com.AlForce.android.runvolution;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class RegisterActivity extends AppCompatActivity {
    private UserRegisterTask regTask = null;
    private EditText nameView;
    private EditText emailView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private View progressView;
    private View registerFormView;
    private String LOG_TAG = RegisterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        nameView = (EditText) findViewById(R.id.name);
        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        confirmPasswordView = (EditText) findViewById(R.id.confirm_password);
        confirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
        nameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
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
        confirmPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "Tapped sign in");
                NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    attemptRegister();
                } else {
                    Toast.makeText(RegisterActivity.this, "You are not connected to the internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
        registerFormView = findViewById(R.id.register_form);
        progressView = findViewById(R.id.register_progress);
    }

    private void attemptRegister() {
        nameView.setError(null);
        emailView.setError(null);
        passwordView.setError(null);
        confirmPasswordView.setError(null);
        String name = nameView.getText().toString();
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        String confirmPassword = confirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid name
        if (TextUtils.isEmpty(name)) {
            nameView.setError(getString(R.string.error_field_required));
            focusView = nameView;
            cancel = true;
        } else if (!isNameValid(name)) {
            nameView.setError(getString(R.string.error_invalid_name));
            focusView = nameView;
            cancel = true;
        }

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

        // Check for a valid confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = confirmPasswordView;
            cancel = true;
        } else if (!isPasswordValid(confirmPassword)) {
            confirmPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = confirmPasswordView;
            cancel = true;
        } else if (! confirmPassword.equals(password)) {
            confirmPasswordView.setError(getString(R.string.error_password_did_not_match));
            focusView = confirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            regTask = new UserRegisterTask(name, email, password);
            regTask.execute((Void) null);
        }
    }
    private boolean isNameValid(String name) {
        return !name.matches(".*\\d+.*");
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            registerFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void launchLogin(View view) {
        Log.d(LOG_TAG,"Tapped already have account string");
        //Toast.makeText(this,"Login with your account", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this,LoginActivity.class));
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {
        @NonNull
        private final String name;
        @NonNull
        private final String email;
        @NonNull
        private final String password;

        UserRegisterTask(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String address = "https://runvolution.herokuapp.com/register";
            HttpsURLConnection httpsPost = null;
            BufferedReader buffer = null;
            String msg = null;

            try {
                URL urlAddress = new URL(address);
                httpsPost = (HttpsURLConnection) urlAddress.openConnection();
                httpsPost.setRequestMethod("POST");
                httpsPost.setDoOutput(true);
                DataOutputStream writer = new DataOutputStream(httpsPost.getOutputStream());
                writer.writeBytes("name=" + name + "&email=" + email + "&password=" + password);
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
            regTask = null;
            showProgress(false);

            if (success) {
                SharedPreferences preferences = getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("logged",true);
                editor.putString("email",email);
                editor.putString("password",password);
                editor.apply();
                Toast.makeText(RegisterActivity.this, "Registration Success",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                //new LoginActivity.UserDataLoader(email).execute((Void) null);
                //startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            regTask = null;
            showProgress(false);
        }
    }
}
