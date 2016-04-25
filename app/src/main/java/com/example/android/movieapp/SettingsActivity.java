package com.example.android.movieapp;

/**
 * Created by Andrew on 4/10/2016.
 */

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            Toast.makeText(this, "The range of page number is from 1 to 100",
                    Toast.LENGTH_SHORT).show();
        }

        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sort_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_page_number_key)));
    }

    private void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {

            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {

            preference.setSummary(stringValue);
        }

        if(preference instanceof EditTextPreference){

            EditTextPreference editTextPreference = (EditTextPreference) preference;

            int page = Integer.parseInt(stringValue);
            if(page < 1){
                 Toast.makeText(this,
                        "The minimum number of page number is 1",
                        Toast.LENGTH_SHORT).show();
                editTextPreference.setText("1");

                bindPreferenceSummaryToValue(editTextPreference);
            }
            else if(page > 100){

                Toast.makeText(this,
                        "The maximum number of page number is 100",
                        Toast.LENGTH_SHORT).show();
                editTextPreference.setText("100");

                bindPreferenceSummaryToValue(editTextPreference);
            }

        }
        return true;
    }

}
