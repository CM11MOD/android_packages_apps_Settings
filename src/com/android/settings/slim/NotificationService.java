package com.android.settings.slim;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NotificationService extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private Preference mHeadsUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notifications_settings);

        mHeadsUp = findPreference(Settings.System.HEADS_UP_NOTIFICATION);

    }

    @Override
    public void onResume() {
        super.onResume();
        boolean headsUpEnabled = Settings.System.getInt(
                getContentResolver(), Settings.System.HEADS_UP_NOTIFICATION, 0) == 1;
        mHeadsUp.setSummary(headsUpEnabled
                ? R.string.summary_heads_up_enabled : R.string.summary_heads_up_disabled);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}

