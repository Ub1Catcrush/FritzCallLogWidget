package com.tvc.calllogwidget;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            ListPreference listPreference = findPreference("connectiontype_preference");
            if (listPreference != null) {
                listPreference.setSummaryProvider((Preference.SummaryProvider<ListPreference>) preference -> {
                    String text = preference.getValue();
                    if (TextUtils.isEmpty(text)){
                        return "Not set";
                    }
                    return "Current value: " + text;
                });
            }

            EditTextPreference textPreference = findPreference("connectiondomain_preference");
            if (textPreference != null) {
                textPreference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)){
                        return "Not set";
                    }
                    return "Current value: " + text;
                });
            }

            textPreference = findPreference("connectionport_preference");
            if (textPreference != null) {
                textPreference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)){
                        return "Not set";
                    }
                    return "Current value: " + text;
                });
            }

            textPreference = findPreference("accountname_preference");
            if (textPreference != null) {
                textPreference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)){
                        return "Not set";
                    }
                    return "Current value: " + text;
                });
            }

            textPreference = findPreference("accountpassword_preference");
            if (textPreference != null) {
                textPreference.setOnBindEditTextListener(
                        editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
                textPreference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)){
                        return "Not set";
                    }
                    String password = String.format("%0" + text.length() + "d", 0).replace("0", "*");
                    return "Current value: " + password;
                });
            }

            listPreference = findPreference("refreshperiod_preference");
            if (listPreference != null) {
                listPreference.setSummaryProvider((Preference.SummaryProvider<ListPreference>) preference -> {
                    String text = preference.getValue();
                    if (TextUtils.isEmpty(text)){
                        return "Not set";
                    }
                    return "Current value: " + text;
                });
            }

            textPreference = findPreference("prefixfortelephonenumber_preference");
            if (textPreference != null) {
                if(textPreference.getText().equals("49.0"))
                    textPreference.setText("+49");
                textPreference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)){
                        return "Not set";
                    }
                    return "Current value: " + text;
                });
            }
        }
    }
}