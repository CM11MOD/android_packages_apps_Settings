/*
 * Copyright (C) 2013 The ChameleonOS Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.chameleonos.labs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * Settings for new features that we want to test out with our users should go
 * here.  If a feature is to be accepted into ChaOS permanently we can move
 * the settings for that feature to the appropriate place.  Features that don't
 * get approved can simply be removed.
 */
public class LabSettings extends SettingsPreferenceFragment
        implements OnSharedPreferenceChangeListener {

    private Preference mHeadsUp;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.chaos_lab_prefs);

        mHeadsUp = findPreference(Settings.System.HEADS_UP_NOTIFICATION);

        initUI();
    }

    private void initUI() {
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean headsUpEnabled = Settings.System.getInt(
                getContentResolver(), Settings.System.HEADS_UP_NOTIFICATION, 0) == 1;
        mHeadsUp.setSummary(headsUpEnabled
                ? R.string.summary_heads_up_enabled : R.string.summary_heads_up_disabled);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
