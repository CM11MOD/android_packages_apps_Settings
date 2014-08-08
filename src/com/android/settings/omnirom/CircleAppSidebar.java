/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.android.settings.omnirom;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;

import android.app.AlertDialog;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.view.IWindowManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.settings.chameleonos.AppMultiSelectListPreference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CircleAppSidebar extends SettingsPreferenceFragment implements
        CompoundButton.OnCheckedChangeListener, Preference.OnPreferenceChangeListener {
    private static final String TAG = "CircleAppSidebar";

    private static final String PREF_INCLUDE_APP_CIRCLE_BAR_KEY = "app_circle_bar_included_apps";
    private static final String PREF_APP_CIRCLE_BAR_TRIGGER_WIDTH_KEY = "app_circle_bar_trigger_width";
    private AppMultiSelectListPreference mIncludedAppCircleBar;
    private ListPreference mTriggerWidth;

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
                Settings.System.ENABLE_APP_CIRCLE_BAR, 0) == 1);
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
                    Settings.System.ENABLE_APP_CIRCLE_BAR), false, this,
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
        addPreferencesFromResource(R.xml.circle_app_sidebar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mIncludedAppCircleBar = (AppMultiSelectListPreference) prefSet.findPreference(PREF_INCLUDE_APP_CIRCLE_BAR_KEY);
        Set<String> includedApps = getIncludedApps();
        if (includedApps != null) mIncludedAppCircleBar.setValues(includedApps);
        mIncludedAppCircleBar.setOnPreferenceChangeListener(this);

        mTriggerWidth = (ListPreference) prefSet.findPreference(PREF_APP_CIRCLE_BAR_TRIGGER_WIDTH_KEY);
        int width = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.APP_CIRCLE_SIDEBAR_TRIGGER_WIDTH, 15);
        mTriggerWidth.setValue(String.valueOf(width));
        mTriggerWidth.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mIncludedAppCircleBar) {
            storeIncludedApps((Set<String>) objValue);
            return true;
        } else if (preference == mTriggerWidth) {
            int width = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.APP_CIRCLE_SIDEBAR_TRIGGER_WIDTH, width);
            return true;
        }
        return false;
    }

    private Set<String> getIncludedApps() {
        String included = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.WHITELIST_APP_CIRCLE_BAR);
        if (TextUtils.isEmpty(included)) {
            return null;
        }
        return new HashSet<String>(Arrays.asList(included.split("\\|")));
    }

    private void storeIncludedApps(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getActivity().getContentResolver(),
                Settings.System.WHITELIST_APP_CIRCLE_BAR, builder.toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
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
                    Settings.System.ENABLE_APP_CIRCLE_BAR,
                    value ? 1 : 0);           
        }
    }

    private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.ENABLE_APP_CIRCLE_BAR, 0) == 1;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }
}
