/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.aokp;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;

public class AnimationInterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AnimationInterfaceSettings";

    private static final String KEY_TOAST_ANIMATION = "toast_animation";

    private ListPreference mToastAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.animation_settings_interface);

        PreferenceScreen prefSet = getPreferenceScreen();

        mToastAnimation = (ListPreference) findPreference(KEY_TOAST_ANIMATION);
    	mToastAnimation.setSummary(mToastAnimation.getEntry());
    	int CurrentToastAnimation = Settings.System.getInt(getContentResolver(),
                                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[10], 1);
    	mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
    	mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
    	mToastAnimation.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mToastAnimation) {
            int index = mToastAnimation.findIndexOfValue((String) objValue);
            Settings.System.putString(getContentResolver(),
                    Settings.System.ACTIVITY_ANIMATION_CONTROLS[10], (String) objValue);
            mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
            Toast.makeText(mContext, "Toast Test", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}
