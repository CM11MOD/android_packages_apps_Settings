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

package com.android.settings.aokp.animations;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.ViewConfiguration;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;
import android.text.TextUtils;

import com.android.settings.widget.SeekBarPreference2;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class ScrollAnimation extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ScrollAnimation";

    private static final String ANIMATION_FLING_VELOCITY = "animation_fling_velocity";
    private static final String ANIMATION_SCROLL_FRICTION = "animation_scroll_friction";
    private static final String ANIMATION_OVERSCROLL_DISTANCE = "animation_overscroll_distance";
    private static final String ANIMATION_OVERFLING_DISTANCE = "animation_overfling_distance";
    private static final float MULTIPLIER_SCROLL_FRICTION = 10000f;
    private static final String ANIMATION_NO_SCROLL = "animation_no_scroll";
    private static final String OVERSCROLL_GLOW_COLOR = "overscroll_glow_color";
    private static final String OVERSCROLL_PREF = "overscroll_effect";
    private static final String OVERSCROLL_WEIGHT_PREF = "overscroll_weight";
    private static final String TOUCH_SLOP = "touch_slop";

    private static final int MENU_RESET = Menu.FIRST;

    private SeekBarPreference2 mAnimationFling;
    private SeekBarPreference2 mAnimationScroll;
    private SeekBarPreference2 mAnimationOverScroll;
    private SeekBarPreference2 mAnimationOverFling;
    private SeekBarPreference2 mTouchSlop;
    private SwitchPreference mAnimNoScroll;
    private ListPreference mOverscrollPref;
    private ListPreference mOverscrollWeightPref;
    private ColorPickerPreference mOverScrollGlowColor;

    private static final int defaultColor = 0xffffffff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.animation_scroll_interface);

        ContentResolver resolver = getActivity().getContentResolver();

        mAnimNoScroll = (SwitchPreference) findPreference(ANIMATION_NO_SCROLL);
        mAnimNoScroll.setChecked(Settings.System.getInt(resolver,
                Settings.System.ANIMATION_CONTROLS_NO_SCROLL, 0) == 1);
        mAnimNoScroll.setOnPreferenceChangeListener(this);

        float defaultScroll = Settings.System.getFloat(resolver,
                Settings.System.CUSTOM_SCROLL_FRICTION, ViewConfiguration.DEFAULT_SCROLL_FRICTION);
        mAnimationScroll = (SeekBarPreference2) findPreference(ANIMATION_SCROLL_FRICTION);
        mAnimationScroll.setValue((int) (defaultScroll * MULTIPLIER_SCROLL_FRICTION));
        mAnimationScroll.setOnPreferenceChangeListener(this);

        int defaultFling = Settings.System.getInt(resolver,
                Settings.System.CUSTOM_FLING_VELOCITY, ViewConfiguration.DEFAULT_MAXIMUM_FLING_VELOCITY);
        mAnimationFling = (SeekBarPreference2) findPreference(ANIMATION_FLING_VELOCITY);
        mAnimationFling.setValue(defaultFling);
        mAnimationFling.setOnPreferenceChangeListener(this);

        int defaultSlop = Settings.System.getInt(resolver,
                Settings.System.CUSTOM_TOUCH_SLOP, ViewConfiguration.DEFAULT_TOUCH_SLOP);
        mTouchSlop = (SeekBarPreference2) findPreference(TOUCH_SLOP);
        mTouchSlop.setValue(defaultSlop);
        mTouchSlop.setOnPreferenceChangeListener(this);

        int defaultOverScroll = Settings.System.getInt(resolver,
                Settings.System.CUSTOM_OVERSCROLL_DISTANCE, ViewConfiguration.DEFAULT_OVERSCROLL_DISTANCE);
        mAnimationOverScroll = (SeekBarPreference2) findPreference(ANIMATION_OVERSCROLL_DISTANCE);
        mAnimationOverScroll.setValue(defaultOverScroll);
        mAnimationOverScroll.setOnPreferenceChangeListener(this);

        int defaultOverFling = Settings.System.getInt(resolver,
                Settings.System.CUSTOM_OVERFLING_DISTANCE, ViewConfiguration.DEFAULT_OVERFLING_DISTANCE);
        mAnimationOverFling = (SeekBarPreference2) findPreference(ANIMATION_OVERFLING_DISTANCE);
        mAnimationOverFling.setValue(defaultOverFling);
        mAnimationOverFling.setOnPreferenceChangeListener(this);

        mOverscrollPref = (ListPreference) findPreference(OVERSCROLL_PREF);
        int overscrollEffect = Settings.System.getInt(getContentResolver(),
                Settings.System.OVERSCROLL_EFFECT, 1);
        mOverscrollPref.setValue(String.valueOf(overscrollEffect));
        mOverscrollPref.setOnPreferenceChangeListener(this);

        mOverscrollWeightPref = (ListPreference) findPreference(OVERSCROLL_WEIGHT_PREF);
        int overscrollWeight = Settings.System.getInt(getContentResolver(),
                                    Settings.System.OVERSCROLL_WEIGHT, 5);
        mOverscrollWeightPref.setValue(String.valueOf(overscrollWeight));
        mOverscrollWeightPref.setOnPreferenceChangeListener(this);

        // Overscroll customize
        mOverScrollGlowColor = (ColorPickerPreference) findPreference(OVERSCROLL_GLOW_COLOR);
        mOverScrollGlowColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.OVERSCROLL_GLOW_COLOR, defaultColor);
        mOverScrollGlowColor.setNewPreviewColor(intColor);

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.reset);
        alertDialog.setMessage(R.string.animation_settings_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetAllValues();
                resetAllSettings();
                ResetOverScroll();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetAllValues() {
        mAnimationFling.setValue(ViewConfiguration.DEFAULT_MAXIMUM_FLING_VELOCITY);
        mTouchSlop.setValue(ViewConfiguration.DEFAULT_TOUCH_SLOP);
        mAnimationScroll.setValue((int) (ViewConfiguration.DEFAULT_SCROLL_FRICTION * MULTIPLIER_SCROLL_FRICTION));
        mAnimationOverScroll.setValue(ViewConfiguration.DEFAULT_OVERSCROLL_DISTANCE);
        mAnimationOverFling.setValue(ViewConfiguration.DEFAULT_OVERFLING_DISTANCE);
        mAnimNoScroll.setChecked(false);
    }

    private void resetAllSettings() {
        setProperVal(mAnimationFling, ViewConfiguration.DEFAULT_MAXIMUM_FLING_VELOCITY);
        setProperVal(mTouchSlop, ViewConfiguration.DEFAULT_TOUCH_SLOP);
        Settings.System.putFloat(getActivity().getContentResolver(),
                   Settings.System.CUSTOM_SCROLL_FRICTION, ViewConfiguration.DEFAULT_SCROLL_FRICTION);
        setProperVal(mAnimationOverScroll, ViewConfiguration.DEFAULT_OVERSCROLL_DISTANCE);
        setProperVal(mAnimationOverFling, ViewConfiguration.DEFAULT_OVERFLING_DISTANCE);
        setProperVal(mAnimNoScroll, 0);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mAnimNoScroll) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.ANIMATION_CONTROLS_NO_SCROLL, value ? 1 : 0);
        } else if (preference == mAnimationScroll) {
            int val = ((Integer)objValue).intValue();
            Settings.System.putFloat(resolver,
                   Settings.System.CUSTOM_SCROLL_FRICTION,
                   ((float) (val / MULTIPLIER_SCROLL_FRICTION)));
        } else if (preference == mAnimationFling) {
            int val = ((Integer)objValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.CUSTOM_TOUCH_SLOP,
                    val);
        } else if (preference == mTouchSlop) {
            int val = ((Integer)objValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.CUSTOM_FLING_VELOCITY,
                    val);
        } else if (preference == mAnimationOverScroll) {
            int val = ((Integer)objValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.CUSTOM_OVERSCROLL_DISTANCE,
                    val);
        } else if (preference == mAnimationOverFling) {
            int val = ((Integer)objValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.CUSTOM_OVERFLING_DISTANCE,
                    val);
        } else if (preference == mOverScrollGlowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.OVERSCROLL_GLOW_COLOR, intHex);
        } else if (preference == mOverscrollPref) {
            int overscrollEffect = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OVERSCROLL_EFFECT, overscrollEffect);
        } else if (preference == mOverscrollWeightPref) {
            int overscrollWeight = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(), Settings.System.OVERSCROLL_WEIGHT, overscrollWeight);
        } else {
            return false;
        }
        return true;
    }

    private void ResetOverScroll() {
        int overscrollWeight = Settings.System.getInt(getContentResolver(),
                                    Settings.System.OVERSCROLL_WEIGHT, 5);

        int overscrollEffect = Settings.System.getInt(getContentResolver(),
                Settings.System.OVERSCROLL_EFFECT, 1);

        Settings.System.putInt(getContentResolver(), Settings.System.OVERSCROLL_EFFECT, 1);
        Settings.System.putInt(getContentResolver(), Settings.System.OVERSCROLL_WEIGHT, 5);
        Settings.System.putInt(getContentResolver(), Settings.System.OVERSCROLL_GLOW_COLOR, defaultColor);

        mOverscrollPref.setValue(String.valueOf(overscrollEffect));
        mOverscrollWeightPref.setValue(String.valueOf(overscrollWeight));
        mOverScrollGlowColor.setNewPreviewColor(defaultColor);
    }

    private void setProperVal(Preference preference, int val) {
        String mString = "";
        if (preference == mAnimNoScroll) {
            mString = Settings.System.ANIMATION_CONTROLS_NO_SCROLL;
        } else if (preference == mAnimationFling) {
            mString = Settings.System.CUSTOM_FLING_VELOCITY;
        } else if (preference == mTouchSlop) {
			mString = Settings.System.CUSTOM_TOUCH_SLOP;
        } else if (preference == mAnimationOverScroll) {
            mString = Settings.System.CUSTOM_OVERSCROLL_DISTANCE;
        } else if (preference == mAnimationOverFling) {
            mString = Settings.System.CUSTOM_OVERFLING_DISTANCE;
        }

        Settings.System.putInt(getActivity().getContentResolver(), mString, val);
    }

}
