/*
 * Copyright (C) 2013 DarkKat
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
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.util.slim.DeviceUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarNotifSystemIcons extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_CAT_COLORS =
            "status_bar_notif_system_icons_cat_colors";
    private static final String PREF_STATUS_BAR_COLORIZE_NOTIF_ICONS =
            "status_bar_colorize_notif_icons";
    private static final String PREF_STATUS_BAR_SHOW_NOTIF_COUNT =
            "status_bar_show_notif_count";
    private static final String PREF_STATUS_BAR_NOTIF_SYSTEM_ICON_COLOR =
            "status_bar_notif_system_icon_color";
    private static final String PREF_STATUS_BAR_NOTIF_TEXT_COLOR =
            "status_bar_notif_text_color";
    private static final String PREF_STATUS_BAR_NOTIF_COUNT_ICON_COLOR =
            "status_bar_notif_count_icon_color";
    private static final String PREF_STATUS_BAR_NOTIF_COUNT_TEXT_COLOR =
            "status_bar_notif_count_text_color";

    private static final String STATUS_BAR_WIFI_COLOR = "status_bar_wifi_color";
    private static final String STATUS_BAR_DATA_COLOR = "status_bar_data_color";
    private static final String STATUS_BAR_AIRPLANE_COLOR = "status_bar_airplane_color";
    private static final String STATUS_BAR_VOLUME_COLOR = "status_bar_volume_color";
    private static final String STATUS_BAR_TEXT_COLOR = "signal_color";

    private static final int DEFAULT_ICON_COLOR = 0xffffffff;
    private static final int DEFAULT_TEXT_COLOR = 0xffffffff;
    private static final int DEFAULT_COUNT_COLOR = 0xffE5350D;
    private static final int DEFAULT_COUNT_TEXT_COLOR = 0xffffffff;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private CheckBoxPreference mColorizeNotifIcons;
    private CheckBoxPreference mShowNotifCount;
    private ColorPickerPreference mIconColor;
    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mCountColor;
    private ColorPickerPreference mCountTextColor;
    private ColorPickerPreference mSystemColor;
    private ColorPickerPreference mStatusBarWifiColor;
    private ColorPickerPreference mStatusBarDataColor;
    private ColorPickerPreference mStatusBarAirplaneColor;
    private ColorPickerPreference mStatusBarVolumeColor;
    private ColorPickerPreference mSignalColor;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.status_bar_notif_system_icons);

        mResolver = getActivity().getContentResolver();
        int intColor = 0xffffffff;
        String hexColor = String.format("#%08x", (0xffffffff & intColor));

        boolean showCount = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1;

        mColorizeNotifIcons =
                (CheckBoxPreference) findPreference(PREF_STATUS_BAR_COLORIZE_NOTIF_ICONS);
        mColorizeNotifIcons.setChecked(Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_COLORIZE_NOTIF_ICONS, 0) == 1);
        if (DeviceUtils.isPhone(getActivity())) {
            mColorizeNotifIcons.setTitle(R.string.status_bar_colorize_notif_icons_title_phone);
        }
        mColorizeNotifIcons.setOnPreferenceChangeListener(this);

        mShowNotifCount =
                (CheckBoxPreference) findPreference(PREF_STATUS_BAR_SHOW_NOTIF_COUNT);
        mShowNotifCount.setChecked(showCount);
        mShowNotifCount.setOnPreferenceChangeListener(this);

        PreferenceCategory catColors =
                (PreferenceCategory) findPreference(PREF_CAT_COLORS);

        mIconColor =
                (ColorPickerPreference) findPreference(PREF_STATUS_BAR_NOTIF_SYSTEM_ICON_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_NOTIF_SYSTEM_ICON_COLOR,
                DEFAULT_ICON_COLOR);
        mIconColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mIconColor.setSummary(hexColor);
        mIconColor.setOnPreferenceChangeListener(this);

        mStatusBarWifiColor =
                (ColorPickerPreference) findPreference(STATUS_BAR_WIFI_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_WIFI_COLOR,
                DEFAULT_ICON_COLOR);
        mStatusBarWifiColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mStatusBarWifiColor.setSummary(hexColor);
        mStatusBarWifiColor.setOnPreferenceChangeListener(this);

        mStatusBarDataColor =
                (ColorPickerPreference) findPreference(STATUS_BAR_DATA_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_DATA_COLOR,
                DEFAULT_ICON_COLOR);
        mStatusBarDataColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mStatusBarDataColor.setSummary(hexColor);
        mStatusBarDataColor.setOnPreferenceChangeListener(this);

        mStatusBarAirplaneColor =
                (ColorPickerPreference) findPreference(STATUS_BAR_AIRPLANE_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_AIRPLANE_COLOR,
                DEFAULT_ICON_COLOR);
        mStatusBarAirplaneColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mStatusBarAirplaneColor.setSummary(hexColor);
        mStatusBarAirplaneColor.setOnPreferenceChangeListener(this);

        mStatusBarVolumeColor =
                (ColorPickerPreference) findPreference(STATUS_BAR_VOLUME_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_VOLUME_COLOR,
                DEFAULT_ICON_COLOR);
        mStatusBarVolumeColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mStatusBarVolumeColor.setSummary(hexColor);
        mStatusBarVolumeColor.setOnPreferenceChangeListener(this);

        mSignalColor = (ColorPickerPreference) findPreference(STATUS_BAR_TEXT_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR,
                DEFAULT_TEXT_COLOR);
        mSignalColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mSignalColor.setSummary(hexColor);
        mSignalColor.setOnPreferenceChangeListener(this);

        mTextColor =
                (ColorPickerPreference) findPreference(PREF_STATUS_BAR_NOTIF_TEXT_COLOR);
        intColor = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_NOTIF_TEXT_COLOR,
                DEFAULT_TEXT_COLOR); 
        mTextColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mTextColor.setSummary(hexColor);
        mTextColor.setOnPreferenceChangeListener(this);

        mCountColor =
                (ColorPickerPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT_ICON_COLOR);
        mCountTextColor =
                (ColorPickerPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT_TEXT_COLOR);
        if (showCount) {
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR,
                    DEFAULT_COUNT_COLOR); 
            mCountColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mCountColor.setSummary(hexColor);
            mCountColor.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR,
                    DEFAULT_COUNT_TEXT_COLOR); 
            mCountTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mCountTextColor.setSummary(hexColor);
            mCountTextColor.setOnPreferenceChangeListener(this);
        } else {
            // Remove uneeded preferences if notification count is disabled
            catColors.removePreference(mCountColor);
            catColors.removePreference(mCountTextColor);
        }

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
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value;
        int intHex;
        String hex;

        if (preference == mColorizeNotifIcons) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_COLORIZE_NOTIF_ICONS,
                    value ? 1 : 0);
            return true;
        } else if (preference == mShowNotifCount) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT, value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_SYSTEM_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mCountColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mCountTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if ( preference == mStatusBarWifiColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
    		Settings.System.putInt(mResolver,
                		Settings.System.STATUS_BAR_WIFI_COLOR, intHex);
            preference.setSummary(hex);
            return true;
    	} else if ( preference == mStatusBarDataColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_DATA_COLOR, intHex);
            preference.setSummary(hex);
    	    return true;
    	} else if ( preference == mStatusBarAirplaneColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_AIRPLANE_COLOR, intHex);
            preference.setSummary(hex);
    	    return true;
        } else if ( preference == mStatusBarVolumeColor ) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_VOLUME_COLOR, intHex);
            preference.setSummary(hex);
    	    return true;
        } else if (preference == mSignalColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, intHex);
            return true;
        }
        return false;
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

        StatusBarNotifSystemIcons getOwner() {
            return (StatusBarNotifSystemIcons) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.reset_settings_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_COLORIZE_NOTIF_ICONS, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_COUNT, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_SYSTEM_ICON_COLOR,
                                    DEFAULT_ICON_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_TEXT_COLOR,
                                    DEFAULT_TEXT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR,
                                    DEFAULT_COUNT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR,
                                    DEFAULT_COUNT_TEXT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_AIRPLANE_COLOR,
                                    DEFAULT_ICON_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_DATA_COLOR,
                                    DEFAULT_ICON_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_VOLUME_COLOR,
                                    DEFAULT_ICON_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_WIFI_COLOR,
                                    DEFAULT_ICON_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR,
                                    DEFAULT_TEXT_COLOR);
                            getOwner().refreshSettings();
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
}

