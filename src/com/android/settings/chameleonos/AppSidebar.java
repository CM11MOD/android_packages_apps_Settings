/*
 * Copyright (C) 2012 CyanogenMod
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

package com.android.settings.chameleonos;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.slim.SeekBarPreference;

public class AppSidebar extends SettingsPreferenceFragment implements
        CompoundButton.OnCheckedChangeListener, OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = "AppSidebar";

    private static final String KEY_TRANSPARENCY = "sidebar_transparency";
    private static final String KEY_SETUP_ITEMS = "sidebar_setup_items";
    private static final String KEY_POSITION = "sidebar_position";
    private static final String KEY_HIDE_LABELS = "sidebar_hide_labels";
    private static final String KEY_TRIGGER_WIDTH = "trigger_width";
    private static final String KEY_TRIGGER_TOP = "trigger_top";
    private static final String KEY_TRIGGER_BOTTOM = "trigger_bottom";

    private SeekBarPreference mTransparencyPref;
    private ListPreference mPositionPref;
    private CheckBoxPreference mHideLabelsPref;
    private SeekBarPreference mTriggerWidthPref;
    private SeekBarPreference mTriggerTopPref;
    private SeekBarPreference mTriggerBottomPref;

    private Switch mEnabledSwitch;
    private boolean mEnabledPref;

    private ViewGroup mPrefsContainer;
    private View mDisabledText;

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
                Settings.System.APP_SIDEBAR_ENABLED, 0) == 1);
    }

    @Override
    public void onStop() {
        super.onStop();
        final Activity activity = getActivity();
        activity.getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getActionBar().setCustomView(null);
    }

    private SettingsObserver mSettingsObserver = new SettingsObserver(new Handler());
    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = getActivity().getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.APP_SIDEBAR_ENABLED), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateEnabledState();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.app_sidebar_settings);

        mHideLabelsPref = (CheckBoxPreference) findPreference(KEY_HIDE_LABELS);
        mHideLabelsPref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.APP_SIDEBAR_DISABLE_LABELS, 0) == 1));

        PreferenceScreen prefSet = getPreferenceScreen();
        mPositionPref = (ListPreference) prefSet.findPreference(KEY_POSITION);
        mPositionPref.setOnPreferenceChangeListener(this);
        int position = Settings.System.getInt(getContentResolver(), Settings.System.APP_SIDEBAR_POSITION, 0);
        mPositionPref.setValue(String.valueOf(position));
        updatePositionSummary(position);

        mTransparencyPref = (SeekBarPreference) findPreference(KEY_TRANSPARENCY);
        mTransparencyPref.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.APP_SIDEBAR_TRANSPARENCY, 0));
        mTransparencyPref.setOnPreferenceChangeListener(this);

        mTriggerWidthPref = (SeekBarPreference) findPreference(KEY_TRIGGER_WIDTH);
        mTriggerWidthPref.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.APP_SIDEBAR_TRIGGER_WIDTH, 10));
        mTriggerWidthPref.setOnPreferenceChangeListener(this);

        mTriggerTopPref = (SeekBarPreference) findPreference(KEY_TRIGGER_TOP);
        mTriggerTopPref.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.APP_SIDEBAR_TRIGGER_TOP, 0));
        mTriggerTopPref.setOnPreferenceChangeListener(this);

        mTriggerBottomPref = (SeekBarPreference) findPreference(KEY_TRIGGER_BOTTOM);
        mTriggerBottomPref.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.APP_SIDEBAR_TRIGGER_HEIGHT, 100));
        mTriggerBottomPref.setOnPreferenceChangeListener(this);

        findPreference(KEY_SETUP_ITEMS).setOnPreferenceClickListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTransparencyPref) {
            int transparency = ((Integer)newValue).intValue();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.APP_SIDEBAR_TRANSPARENCY, transparency);
            return true;
        } else if (preference == mTriggerWidthPref) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.APP_SIDEBAR_TRIGGER_WIDTH, width);
            return true;
        } else if (preference == mTriggerTopPref) {
            int top = ((Integer)newValue).intValue();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.APP_SIDEBAR_TRIGGER_TOP, top);
            return true;
        } else if (preference == mTriggerBottomPref) {
            int bottom = ((Integer)newValue).intValue();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.APP_SIDEBAR_TRIGGER_HEIGHT, bottom);
            return true;
        } else if (preference == mPositionPref) {
            int position = Integer.valueOf((String) newValue);
            updatePositionSummary(position);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mHideLabelsPref) {
            value = mHideLabelsPref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.APP_SIDEBAR_DISABLE_LABELS,
                    value ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals(KEY_SETUP_ITEMS)) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName("com.android.systemui",
                    "com.android.systemui.statusbar.sidebar.SidebarConfigurationActivity"));
            getActivity().startActivity(intent);
            return true;
        }
        return false;
    }

    private void updatePositionSummary(int value) {
        mPositionPref.setSummary(mPositionPref.getEntries()[mPositionPref.findIndexOfValue("" + value)]);
        Settings.System.putInt(getContentResolver(),
                Settings.System.APP_SIDEBAR_POSITION, value);
    }

    @Override
    public void onPause() {
        super.onPause();
        Settings.System.putInt(getContentResolver(),
                Settings.System.APP_SIDEBAR_SHOW_TRIGGER, 0);

        getActivity().getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    @Override
    public void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        Settings.System.putInt(getContentResolver(),
                Settings.System.APP_SIDEBAR_SHOW_TRIGGER, 1);

        mSettingsObserver.observe();
        updateEnabledState();

        // If running on a phone, remove padding around container
        // and the preference listview
        if (!Utils.isTablet(getActivity())) {
            mPrefsContainer.setPadding(0, 0, 0, 0);
            getListView().setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mEnabledSwitch) {

            boolean value = ((Boolean)isChecked).booleanValue();
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.APP_SIDEBAR_ENABLED,
                    value ? 1 : 0);           
        }
    }

    private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.APP_SIDEBAR_ENABLED, 0) == 1;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }
}
