package com.AlForce.android.runvolution;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private PetNameUpdaterTask updateTask = null;
    private static Context settingsContext;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsContext = this.getApplicationContext();
        getLayoutInflater().inflate(R.layout.toolbar, (ViewGroup)findViewById(android.R.id.content));
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();
        int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin) + 10, getResources().getDisplayMetrics());
        getListView().setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
    }

    public static Context getAppContext() {
        return settingsContext;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        if(header.fragmentArguments == null)
        {
            header.fragmentArguments = new Bundle();
        }
        SharedPreferences preferences = getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
        String name = preferences.getString("name","John Doe");
        String email = preferences.getString("email", "johndoe@email.com");
        header.fragmentArguments.putString("name", name);
        header.fragmentArguments.putString("email", email);
        super.onHeaderClick(header, position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AccountPreferenceFragment.class.getName().equals(fragmentName)
                || PetPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AccountPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_account);
            setHasOptionsMenu(true);

            SharedPreferences preferences = AccountPreferenceFragment.this.getActivity().getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
            String name = preferences.getString("name", "John Doe");
            String email = preferences.getString("email", "johndoe@example.com");

            Preference accountName = findPreference("account_name");
            Preference accountEmail = findPreference("account_email");

            accountName.setSummary(name);
            accountEmail.setSummary(email);

            Preference signoutPref = findPreference("action_signout");
            signoutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences preferences = AccountPreferenceFragment.this.getActivity().getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
                    preferences.edit().remove("logged").apply();
                    preferences.edit().remove("name").apply();
                    preferences.edit().remove("email").apply();
                    startActivity(new Intent(SettingsActivity.getAppContext(),LoginActivity.class));
                    return true;
                }
            });
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            View rootView = getView();
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin), getResources().getDisplayMetrics());
            list.setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
            list.setDivider(null);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PetPreferenceFragment extends PreferenceFragment  {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Preference.OnPreferenceChangeListener bindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    String newPetName = value.toString();
                    //SharedPreferences preferences = PetPreferenceFragment.this.getActivity().getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
                    if (isPetNameValid(newPetName)) {
                        //preferences.edit().putString(preference.getKey(),newPetName).apply();
                        preference.setSummary(newPetName);
                        //int petId = preferences.getInt("petId",0);
                        //new PetNameUpdaterTask(petId,newPetName).execute((Void) null);
                    } else {
                        preference.setSummary("Please fill a proper pet name");
                    }
                    return true;
                }
            };
            addPreferencesFromResource(R.xml.pref_pet);
            setHasOptionsMenu(true);

            SharedPreferences preferences = PetPreferenceFragment.this.getActivity().getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
            String petName = preferences.getString("petName","Bobby");

            final Preference petNamePref = findPreference("petName");
            petNamePref.setSummary(petName);
            petNamePref.setOnPreferenceChangeListener(bindPreferenceSummaryToValueListener);
            bindPreferenceSummaryToValueListener.onPreferenceChange(petNamePref, preferences.getString("petName",""));
            Preference savePref = findPreference("action_save");
            savePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences preferences = PetPreferenceFragment.this.getActivity().getSharedPreferences(getString(R.string.sharedpref_file), MODE_PRIVATE);
                    int petId = preferences.getInt("petId",0);
                    String newPetName = petNamePref.getSummary().toString();
                    preferences.edit().putString("petName",newPetName).apply();
                    new PetNameUpdaterTask(petId,newPetName).execute((Void) null);
                    return true;
                }
            });
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            View rootView = getView();
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin), getResources().getDisplayMetrics());
            list.setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
            list.setDivider(null);
        }


        public Boolean isPetNameValid(String petName) {
            if (petName.length() == 0 || petName.matches(".*\\d+.*")) {
                return false;
            }
            return true;
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class PetNameUpdaterTask extends AsyncTask<Void, Void, Boolean> {
        private final int petID;
        @NonNull
        private final String newName;

        PetNameUpdaterTask(int petID, String newName) {
            this.petID = petID;
            this.newName = newName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String address = "https://runvolution.herokuapp.com/updatepetname";
            String param = "?petid=" + petID + "&name=" + newName;
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
                Toast.makeText(SettingsActivity.getAppContext(), "Pet Name Updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.getAppContext(), "Failed to Update Pet Name", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {
            //updateTask = null;
        }
    }
}
