/*
 * Copyright (C) 2013 SlimRoms
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

package com.android.settings.slim;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class XposedHook extends SettingsPreferenceFragment {

    private static final String TAG = "XposedHook";

    // Package name of the xposed frameworks
    public static final String XPOSED_PACKAGE_NAME = "de.robv.android.xposed.installer";

    // Package name of the app settings
    public static final String APP_SETTINGS_PACKAGE_NAME = "de.robv.android.xposed.mods.appsettings";
    // Intent for launching the omniswitch settings actvity
    public static Intent INTENT_APP_SETTINGS = new Intent(Intent.ACTION_MAIN)
            .setClassName(APP_SETTINGS_PACKAGE_NAME, APP_SETTINGS_PACKAGE_NAME + ".XposedModActivity");

    private static final String PER_APP_SETTINGS = "xposed_per_app_settings";
    private Preference mXposedFramework;
    private Preference mPerAppSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.xposed_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mPerAppSettings = (Preference)
                prefSet.findPreference(PER_APP_SETTINGS);
        updateSettings();
    }

    private void updateSettings() {
        mPerAppSettings.setEnabled(isAppSettingInstalled());
        mPerAppSettings.setSummary(isAppSettingInstalled() ?
                getResources().getString(R.string.xposed_per_app_summary) :
                getResources().getString(R.string.appsetting_not_installed_message));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPerAppSettings){
            if (!isAppSettingInstalled()) {
                AppSettingNotInstalledWarning();
                return false;
            } else {
                startActivity(INTENT_APP_SETTINGS);
                return true;
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
    }

    private void AppSettingNotInstalledWarning() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.appsetting_not_installed_title))
                .setMessage(getResources().getString(R.string.appsetting_not_installed_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                }).show();
    }

    private boolean isAppSettingInstalled() {
        final PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(APP_SETTINGS_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
