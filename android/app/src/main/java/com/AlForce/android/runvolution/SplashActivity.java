package com.AlForce.android.runvolution;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SplashActivity extends AppCompatActivity {
    private UserDataLoader loadTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
        Boolean logged = preferences.getBoolean("logged", false);
        if (logged) {
            String email = preferences.getString("email", null);
            if (email != null) {
                new UserDataLoader(email).execute((Void) null);
            }
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    }

    public class UserDataLoader extends AsyncTask<Void, Void, ArrayList<String>> {
        @NonNull
        private final String email;

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
            loadTask = null;
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            if (jsonData != null) {
                String petData = jsonData.get(1);
                int petLevel = 1;
                int petXP = 0;
                try {
                    JSONObject rawPetData = new JSONObject(petData);
                    petLevel = rawPetData.getInt("level");
                    petXP = rawPetData.getInt("xp");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SharedPreferences preferences = getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("petLevel", petLevel);
                editor.putInt("petXP", petXP);
                intent.putExtra("userData",jsonData.get(0));
                intent.putExtra("petData",jsonData.get(1));
            }
            startActivity(intent);
            finish();
        }

        @Override
        protected void onCancelled() {
            loadTask = null;
        }
    }
}
