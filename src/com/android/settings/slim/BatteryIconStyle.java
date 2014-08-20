/*
 * Copyright (C) 2012 Slimroms Project
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
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.Date;

public class BatteryIconStyle extends SettingsPreferenceFragment
    implements OnPreferenceChangeListener {

    private static final String TAG = "BatteryIconStyle";

    private static final String PREF_STATUS_BAR_BATTERY = "battery_icon";
    private static final String PREF_STATUS_BAR_BATTERY_COLOR = "battery_color";
    private static final String PREF_STATUS_BAR_BATTERY_TEXT_COLOR = "battery_text_color";
    private static final String PREF_STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR = "battery_text_charging_color";
    private static final String PREF_STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED = "circle_battery_animation_speed";
    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "battery_bar_animate";
    private static final String PREF_BATT_STAT_CIRCLE_DOTTED = "battery_circle_dotted";
    private static final String PREF_BATT_STAT_CIRCLE_DOT_LENGTH = "battery_circle_dot_length";
    private static final String PREF_BATT_STAT_CIRCLE_DOT_INTERVAL = "battery_circle_dot_interval";
    private static final String PREF_BATT_STAT_CIRCLE_DOT_OFFSET = "battery_circle_dot_offset";

    private static final int MENU_RESET = Menu.FIRST;

    private static final int DLG_RESET = 0;

    private ListPreference mStatusBarBattery;
    private ListPreference mBatteryBar;
    private ListPreference mBatteryBarStyle;
    private ListPreference mBatteryBarThickness;
    private CheckBoxPreference mBatteryBarChargingAnimation;
    private ColorPickerPreference mBatteryBarColor;
    private ColorPickerPreference mBatteryColor;
    private ColorPickerPreference mBatteryTextColor;
    private ColorPickerPreference mBatteryTextChargingColor;
    private ListPreference mCircleAnimSpeed;
    private CheckBoxPreference mCircleDotted;
    private ListPreference mCircleDotLength;
    private ListPreference mCircleDotInterval;
    private ListPreference mCircleDotOffset;

    private boolean mCheckPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createCustomView();
    }

    private PreferenceScreen createCustomView() {
        mCheckPreferences = false;
        PreferenceScreen prefSet = getPreferenceScreen();
        if (prefSet != null) {
            prefSet.removeAll();
        }

        addPreferencesFromResource(R.xml.slim_battery_style);
        prefSet = getPreferenceScreen();

        int intColor;
        String hexColor;

        PackageManager pm = getPackageManager();
        Resources systemUiResources;
        try {
            systemUiResources = pm.getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            Log.e(TAG, "can't access systemui resources",e);
            return null;
        }

        mStatusBarBattery = (ListPreference) prefSet.findPreference(PREF_STATUS_BAR_BATTERY);
        mStatusBarBattery.setOnPreferenceChangeListener(this);
        int statusBarBattery = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY, 0);
        mStatusBarBattery.setValue(String.valueOf(statusBarBattery));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());

        boolean isCircleDottedEnabled = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_CIRCLE_DOTTED, 0) == 1;

        mCircleDotted = (CheckBoxPreference) findPreference(PREF_BATT_STAT_CIRCLE_DOTTED);
        if (statusBarBattery == 3 || statusBarBattery == 4) {
            mCircleDotted.setChecked(isCircleDottedEnabled);
            mCircleDotted.setOnPreferenceChangeListener(this);
        } else {
            removePreference(PREF_BATT_STAT_CIRCLE_DOTTED);
        }

        mCircleDotLength = (ListPreference) findPreference(PREF_BATT_STAT_CIRCLE_DOT_LENGTH);
        mCircleDotInterval = (ListPreference) findPreference(PREF_BATT_STAT_CIRCLE_DOT_INTERVAL);
        mCircleDotOffset = (ListPreference) findPreference(PREF_BATT_STAT_CIRCLE_DOT_OFFSET);

        if ((statusBarBattery == 3 || statusBarBattery == 4) && isCircleDottedEnabled) {
            int circleDotLength = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_DOT_LENGTH, 3);
            mCircleDotLength.setValue(String.valueOf(circleDotLength));
            mCircleDotLength.setSummary(mCircleDotLength.getEntry());
            mCircleDotLength.setOnPreferenceChangeListener(this);

            int circleDotInterval = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_DOT_INTERVAL, 2);
            mCircleDotInterval.setValue(String.valueOf(circleDotInterval));
            mCircleDotInterval.setSummary(mCircleDotInterval.getEntry());
            mCircleDotInterval.setOnPreferenceChangeListener(this);

            int circleDotOffset = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_DOT_OFFSET, 0);
            mCircleDotOffset.setValue(String.valueOf(circleDotOffset));
            mCircleDotOffset.setSummary(mCircleDotOffset.getEntry());
            mCircleDotOffset.setOnPreferenceChangeListener(this);
        } else {
            removePreference(PREF_BATT_STAT_CIRCLE_DOT_LENGTH);
            removePreference(PREF_BATT_STAT_CIRCLE_DOT_INTERVAL);
            removePreference(PREF_BATT_STAT_CIRCLE_DOT_OFFSET);
        }

        mBatteryColor = (ColorPickerPreference) findPreference(PREF_STATUS_BAR_BATTERY_COLOR);
        mBatteryColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_COLOR, -2);
        if (intColor == -2) {
            intColor = systemUiResources.getColor(systemUiResources.getIdentifier(
                    "com.android.systemui:color/batterymeter_charge_color", null, null));
            mBatteryColor.setSummary(getResources().getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryColor.setSummary(hexColor);
        }
        mBatteryColor.setNewPreviewColor(intColor);

        mBatteryTextColor =
            (ColorPickerPreference) findPreference(PREF_STATUS_BAR_BATTERY_TEXT_COLOR);
        mBatteryTextColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_TEXT_COLOR, -2);
        mBatteryTextColor.setSummary(getResources().getString(R.string.default_string));
        if (intColor == -2 && statusBarBattery == 2) {
            intColor = systemUiResources.getColor(systemUiResources.getIdentifier(
                    "com.android.systemui:color/batterymeter_bolt_color", null, null));
        } else if (intColor == -2) {
            intColor = systemUiResources.getColor(systemUiResources.getIdentifier(
                    "com.android.systemui:color/batterymeter_charge_color", null, null));
        } else {
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryTextColor.setSummary(hexColor);
        }
        mBatteryTextColor.setNewPreviewColor(intColor);

        mBatteryTextChargingColor = (ColorPickerPreference)
            findPreference(PREF_STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR);
        mBatteryTextChargingColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR, -2);
        mBatteryTextChargingColor.setSummary(getResources().getString(R.string.default_string));
        if (intColor == -2 && (statusBarBattery == 2 || statusBarBattery == 0)) {
            intColor = systemUiResources.getColor(systemUiResources.getIdentifier(
                    "com.android.systemui:color/batterymeter_bolt_color", null, null));
        } else if (intColor == -2) {
            intColor = systemUiResources.getColor(systemUiResources.getIdentifier(
                    "com.android.systemui:color/batterymeter_charge_color", null, null));
        } else {
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryTextChargingColor.setSummary(hexColor);
        }
        mBatteryTextChargingColor.setNewPreviewColor(intColor);

        mCircleAnimSpeed =
            (ListPreference) findPreference(PREF_STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED);
        mCircleAnimSpeed.setOnPreferenceChangeListener(this);
        mCircleAnimSpeed.setValue((Settings.System
                .getInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED, 3))
                + "");
        mCircleAnimSpeed.setSummary(mCircleAnimSpeed.getEntry());

        updateBatteryIconOptions(statusBarBattery);

        mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
        mBatteryBar.setOnPreferenceChangeListener(this);
        mBatteryBar.setValue((Settings.System.getInt(getActivity().getContentResolver(),
                                Settings.System.STATUSBAR_BATTERY_BAR, 0)) + "");
        mBatteryBar.setSummary(mBatteryBar.getEntry());

        mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
        mBatteryBarStyle.setOnPreferenceChangeListener(this);
        mBatteryBarStyle.setValue((Settings.System.getInt(getActivity().getContentResolver(),
                                    Settings.System.STATUSBAR_BATTERY_BAR_STYLE, 0)) + "");
        mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());

        mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
        mBatteryBarColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_COLOR, -2);
        if (intColor == -2) {
            intColor = systemUiResources.getColor(systemUiResources.getIdentifier(
                    "com.android.systemui:color/batterymeter_charge_color", null, null));
        } else {
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mBatteryBarColor.setSummary(hexColor);
        }
        mBatteryBarColor.setNewPreviewColor(intColor);

        mBatteryBarChargingAnimation = (CheckBoxPreference) findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                                                    Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);

        mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
        mBatteryBarThickness.setOnPreferenceChangeListener(this);
        mBatteryBarThickness.setValue((Settings.System.getInt(getActivity().getContentResolver(),
                                        Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, 1)) + "");
        mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntry());

        updateBatteryBarOptions();
        setHasOptionsMenu(true);
        mCheckPreferences = true;
        return prefSet;
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
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCheckPreferences) {
            return false;
        }
        if (preference == mStatusBarBattery) {
            int statusBarBattery = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY, statusBarBattery);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            createCustomView();
            return true;
        } else if (preference == mBatteryColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_COLOR, intHex);
            return true;
        } else if (preference == mBatteryTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mBatteryTextChargingColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR, intHex);
            return true;
        } else if (preference == mCircleAnimSpeed) {
            int val = Integer.parseInt((String) newValue);
            int index = mCircleAnimSpeed.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_BATTERY_ANIMATIONSPEED, val);
            mCircleAnimSpeed.setSummary(mCircleAnimSpeed.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBar) {
            int val = Integer.valueOf((String) newValue);
            int index = mBatteryBar.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR, val);
            mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
            updateBatteryBarOptions();
            return true;
        } else if (preference == mBatteryBarStyle) {
            int val = Integer.valueOf((String) newValue);
            int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_STYLE, val);
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarThickness) {
            int val = Integer.valueOf((String) newValue);
            int index = mBatteryBarThickness.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, val);
            mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntries()[index]);
            return true;
        } else if (preference == mCircleDotted) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_DOTTED, value ? 1 : 0);
            createCustomView();
            return true;
        } else if (preference == mCircleDotLength) {
            int circleDotLength = Integer.valueOf((String) newValue);
            int index = mCircleDotLength.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_DOT_LENGTH, circleDotLength);
            mCircleDotLength.setSummary(mCircleDotLength.getEntries()[index]);
            return true;
        } else if (preference == mCircleDotInterval) {
            int circleDotInterval = Integer.valueOf((String) newValue);
            int index = mCircleDotInterval.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_DOT_INTERVAL, circleDotInterval);
            mCircleDotInterval.setSummary(mCircleDotInterval.getEntries()[index]);
            return true;
        } else if (preference == mCircleDotOffset) {
            int circleDotOffset = Integer.valueOf((String) newValue);
            int index = mCircleDotOffset.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CIRCLE_DOT_OFFSET, circleDotOffset);
            mCircleDotOffset.setSummary(mCircleDotOffset.getEntries()[index]);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mBatteryBarChargingAnimation) {
            value = mBatteryBarChargingAnimation.isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateBatteryIconOptions(int batteryIconStat) {
        mBatteryTextChargingColor.setTitle(R.string.battery_bolt_color);
        if (batteryIconStat == 0) {
            mBatteryColor.setEnabled(true);
            mBatteryTextColor.setEnabled(false);
            mBatteryTextChargingColor.setEnabled(true);
            mCircleAnimSpeed.setEnabled(false);
            mCircleDotted.setEnabled(false);
        } else if (batteryIconStat == 2 || batteryIconStat == 7) {
            mBatteryColor.setEnabled(true);
            mBatteryTextColor.setEnabled(true);
            mBatteryTextChargingColor.setEnabled(true);
            mCircleAnimSpeed.setEnabled(false);
            mCircleDotted.setEnabled(false);
        } else if (batteryIconStat == 3) {
			mBatteryColor.setEnabled(true);
			mCircleDotted.setEnabled(true);
            mBatteryTextColor.setEnabled(false);
            mBatteryTextChargingColor.setEnabled(true);
            mCircleAnimSpeed.setEnabled(true);
            mBatteryTextChargingColor.setTitle(R.string.battery_circle_charging_color);
		} else if (batteryIconStat == 4) {
			mCircleDotted.setEnabled(true);
			mBatteryColor.setEnabled(true);
            mBatteryTextColor.setEnabled(true);
            mBatteryTextChargingColor.setEnabled(true);
            mCircleAnimSpeed.setEnabled(true);
            mBatteryTextChargingColor.setTitle(R.string.battery_circle_charging_color);			
		} else if (batteryIconStat == 8) {
			mCircleDotted.setEnabled(false);
            mBatteryColor.setEnabled(false);
            mBatteryTextColor.setEnabled(false);
            mBatteryTextChargingColor.setEnabled(false);
            mCircleAnimSpeed.setEnabled(false);
        } else {
            mBatteryColor.setEnabled(false);
            mCircleDotted.setEnabled(false);
            mBatteryTextColor.setEnabled(true);
            mBatteryTextChargingColor.setEnabled(true);
            mBatteryTextChargingColor.setTitle(R.string.battery_text_charging_color);
            mCircleAnimSpeed.setEnabled(false);
        }
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        BatteryIconStyle getOwner() {
            return (BatteryIconStyle) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.battery_style_reset_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getActivity().getContentResolver(),
                                Settings.System.STATUS_BAR_BATTERY_COLOR, -2);
                            Settings.System.putInt(getActivity().getContentResolver(),
                                Settings.System.STATUS_BAR_BATTERY_TEXT_COLOR, -2);
                            Settings.System.putInt(getActivity().getContentResolver(),
                                Settings.System.STATUS_BAR_BATTERY_TEXT_CHARGING_COLOR, -2);
                            Settings.System.putInt(getActivity().getContentResolver(),
                                Settings.System.STATUSBAR_BATTERY_BAR_COLOR, -2);
                            getOwner().createCustomView();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }

    private void updateBatteryBarOptions() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
               Settings.System.STATUSBAR_BATTERY_BAR, 0) == 0) {
            mBatteryBarStyle.setEnabled(false);
            mBatteryBarThickness.setEnabled(false);
            mBatteryBarChargingAnimation.setEnabled(false);
            mBatteryBarColor.setEnabled(false);
        } else {
            mBatteryBarStyle.setEnabled(true);
            mBatteryBarThickness.setEnabled(true);
            mBatteryBarChargingAnimation.setEnabled(true);
            mBatteryBarColor.setEnabled(true);
        }
    }

}
