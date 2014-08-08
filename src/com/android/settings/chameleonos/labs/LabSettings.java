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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
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

    private static final String KEY_SHORTCUT = "shortcut";
    private static final String PREF_APP_SIDEBAR = "app_sidebar";
    private static final String PREF_CIRCLE_SIDEBAR = "circle_app_sidebar";

    private PreferenceGroup mShortcut;
    private Preference mAppSidebar;
    private Preference mCircleSidebar;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.chaos_lab_prefs);

        PreferenceScreen prefSet = getPreferenceScreen();
        mShortcut = (PreferenceGroup) prefSet.findPreference(KEY_SHORTCUT);

        if (mShortcut != null) {
            mAppSidebar = prefSet.findPreference(PREF_APP_SIDEBAR);
            mCircleSidebar = prefSet.findPreference(PREF_CIRCLE_SIDEBAR);
        }

        initUI();
    }

    private void initUI() {
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAppsidebarState();
        updateCirclesidebarState();
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

    private void updateAppsidebarState() {
        boolean Enabled = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.APP_SIDEBAR_ENABLED, 0, ActivityManager.getCurrentUser()) == 1;
        mAppSidebar.setSummary(Enabled
                ? R.string.enabled : R.string.disabled);
    }

    private void updateCirclesidebarState() {
        boolean Enabled = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ENABLE_APP_CIRCLE_BAR, 0, ActivityManager.getCurrentUser()) == 1;
        mCircleSidebar.setSummary(Enabled
                ? R.string.enabled : R.string.disabled);
    }
}
