/*
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.pa;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.util.Helpers;

public class Hover extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "HoverSettings";
    private static final String PREF_HOVER_LONG_FADE_OUT_DELAY = "hover_long_fade_out_delay";
    private static final String PREF_HOVER_MICRO_FADE_OUT_DELAY = "hover_micro_fade_out_delay";

    ListPreference mHoverLongFadeOutDelay;
    ListPreference mHoverMicroFadeOutDelay;

    private Switch mActionBarSwitch;
    private HoverEnabler mHoverEnabler;

    private ViewGroup mPrefsContainer;
    private View mDisabledText;

    private ContentObserver mHoverObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateEnabledState();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get launch-able applications
        addPreferencesFromResource(R.xml.hover_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mHoverLongFadeOutDelay = (ListPreference) prefSet.findPreference(PREF_HOVER_LONG_FADE_OUT_DELAY);
        int hoverLongFadeOutDelay = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HOVER_LONG_FADE_OUT_DELAY, 5000, UserHandle.USER_CURRENT);
        mHoverLongFadeOutDelay.setValue(String.valueOf(hoverLongFadeOutDelay));
        mHoverLongFadeOutDelay.setSummary(mHoverLongFadeOutDelay.getEntry());
        mHoverLongFadeOutDelay.setOnPreferenceChangeListener(this);

        mHoverMicroFadeOutDelay = (ListPreference) prefSet.findPreference(PREF_HOVER_MICRO_FADE_OUT_DELAY);
        int hoverMicroFadeOutDelay = Settings.System.getIntForUser(getContentResolver(),
        Settings.System.HOVER_MICRO_FADE_OUT_DELAY, 1250, UserHandle.USER_CURRENT);
        mHoverMicroFadeOutDelay.setValue(String.valueOf(hoverMicroFadeOutDelay));
        mHoverMicroFadeOutDelay.setSummary(mHoverMicroFadeOutDelay.getEntry());
        mHoverMicroFadeOutDelay.setOnPreferenceChangeListener(this);

    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        // We don't call super.onActivityCreated() here, since it assumes we already set up
        // Preference (probably in onCreate()), while ProfilesSettings exceptionally set it up in
        // this method.
        // On/off switch
        Activity activity = getActivity();
        //Switch
        mActionBarSwitch = new Switch(activity);

        if (activity instanceof PreferenceActivity) {
            final int padding = activity.getResources().getDimensionPixelSize(
                    R.dimen.action_bar_switch_padding);
            mActionBarSwitch.setPaddingRelative(0, 0, padding, 0);
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(mActionBarSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }

        mHoverEnabler = new HoverEnabler(activity, mActionBarSwitch);
        // After confirming PreferenceScreen is available, we call super.
        super.onActivityCreated(icicle);
        setHasOptionsMenu(true);
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

    public void onDestroyView() {
        getActivity().getActionBar().setCustomView(null);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHoverEnabler != null) {
            mHoverEnabler.resume();
        }
        getActivity().invalidateOptionsMenu();

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.HOVER_ENABLED),
                true, mHoverObserver);
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
        if (mHoverEnabler != null) {
            mHoverEnabler.pause();
        }
        getContentResolver().unregisterContentObserver(mHoverObserver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHoverLongFadeOutDelay) {
            int index = mHoverLongFadeOutDelay.findIndexOfValue((String) newValue);
            int hoverLongFadeOutDelay = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.HOVER_LONG_FADE_OUT_DELAY,
                    hoverLongFadeOutDelay, UserHandle.USER_CURRENT);
            mHoverLongFadeOutDelay.setSummary(mHoverLongFadeOutDelay.getEntries()[index]);
            return true;
        } else if (preference == mHoverMicroFadeOutDelay) {
            int index = mHoverMicroFadeOutDelay.findIndexOfValue((String) newValue);
            int hoverMicroFadeOutDelay = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
            Settings.System.HOVER_MICRO_FADE_OUT_DELAY,
            hoverMicroFadeOutDelay, UserHandle.USER_CURRENT);
            mHoverMicroFadeOutDelay.setSummary(mHoverMicroFadeOutDelay.getEntries()[index]);
            return true;
        }
        return false;
    }

    private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.HOVER_ENABLED, 0) != 0;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }
}
