/*
 * Copyright (C) 2014 Slimroms
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

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.IWindowManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.internal.util.slim.SlimActions;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class PieControl extends SettingsPreferenceFragment implements 
        CompoundButton.OnCheckedChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "PieControl";

    private static final String PIE_BUTTON = "pie_button";
    private static final String PIE_SHOW_SNAP = "pie_show_snap";
    private static final String PIE_MENU = "pie_menu";
    private static final String PIE_SHOW_TEXT = "pie_show_text";
    private static final String PIE_SHOW_BACKGROUND = "pie_show_background";
    private static final String PIE_STYLE = "pie_style";
    private static final String PIE_TRIGGER = "pie_trigger";

    private CheckBoxPreference mShowSnap;
    private ListPreference mPieMenuDisplay;
    private CheckBoxPreference mShowText;
    private CheckBoxPreference mShowBackground;
    private PreferenceScreen mStyle;
    private PreferenceScreen mTrigger;
    private PreferenceScreen mButton;

    private Switch mEnabledSwitch;
    private boolean mEnabledPref;

    private ViewGroup mPrefsContainer;
    private View mDisabledText;

    private ContentObserver mSlimPieObserver = new ContentObserver(new Handler()) {
    @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateEnabledState();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        mEnabledSwitch = new Switch(activity);

        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mEnabledSwitch.setPaddingRelative(0, 0, padding, 0);
        mEnabledSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.disable_fragment, container, false);
        mPrefsContainer = (ViewGroup) v.findViewById(R.id.prefs_container);
        mDisabledText = v.findViewById(R.id.disabled_text);

        View prefs = super.onCreateView(inflater, mPrefsContainer, savedInstanceState);
        mPrefsContainer.addView(prefs);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        final Activity activity = getActivity();
        activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getActionBar().setCustomView(mEnabledSwitch, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.END));
        mEnabledSwitch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SPIE_CONTROLS, 0) == 1);
        updateSettings();
    }

    @Override
    public void onStop() {
        super.onStop();
        final Activity activity = getActivity();
        activity.getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getActionBar().setCustomView(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSettings();

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SPIE_CONTROLS),
                true, mSlimPieObserver);
        updateEnabledState();

        // If running on a phone, remove padding around container
        // and the preference listview
        if (!Utils.isTablet(getActivity())) {
            mPrefsContainer.setPadding(0, 0, 0, 0);
            getListView().setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mSlimPieObserver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pie_control);

        PreferenceScreen prefSet = getPreferenceScreen();

        mShowSnap = (CheckBoxPreference) prefSet.findPreference(PIE_SHOW_SNAP);
        mShowSnap.setOnPreferenceChangeListener(this);
        mShowText = (CheckBoxPreference) prefSet.findPreference(PIE_SHOW_TEXT);
        mShowText.setOnPreferenceChangeListener(this);
        mShowBackground = (CheckBoxPreference) prefSet.findPreference(PIE_SHOW_BACKGROUND);
        mShowBackground.setOnPreferenceChangeListener(this);
        mStyle = (PreferenceScreen) prefSet.findPreference(PIE_STYLE);
        mTrigger = (PreferenceScreen) prefSet.findPreference(PIE_TRIGGER);
        mButton = (PreferenceScreen) prefSet.findPreference(PIE_BUTTON);
        mPieMenuDisplay = (ListPreference) prefSet.findPreference(PIE_MENU);
        mPieMenuDisplay.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mShowSnap) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_SHOW_SNAP, (Boolean) newValue ? 1 : 0);
        } else if (preference == mPieMenuDisplay) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SPIE_MENU, Integer.parseInt((String) newValue));
        } else if (preference == mShowText) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_SHOW_TEXT, (Boolean) newValue ? 1 : 0);
        } else if (preference == mShowBackground) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_SHOW_BACKGROUND, (Boolean) newValue ? 1 : 0);
        }
        return true;
    }

    private void updateSettings() {
        mPieMenuDisplay.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.SPIE_MENU,
                2) + "");
        mShowSnap.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_SHOW_SNAP, 1) == 1);
        mShowText.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_SHOW_TEXT, 1) == 1);
        mShowBackground.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_SHOW_BACKGROUND, 1) == 1);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mEnabledSwitch) {
            boolean value = ((Boolean)isChecked).booleanValue();
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SPIE_CONTROLS,
                    value ? 1 : 0);
        }
    }

    private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
            Settings.System.SPIE_CONTROLS, 0) == 1;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }
}
