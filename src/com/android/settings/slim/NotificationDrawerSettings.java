/*
 * Copyright (C) 2012 Slimroms
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.util.slim.DeviceUtils;

import com.android.settings.quicklaunch.BookmarkPicker;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.slim.quicksettings.QuickSettingsUtil;
import com.android.settings.R;
import com.android.settings.widget.SeekBarPreference;

import java.net.URISyntaxException;

import com.android.settings.omnirom.omnigears.preference.AppSelectListPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NotificationDrawerSettings extends SettingsPreferenceFragment
            implements OnPreferenceChangeListener  {

    public static final String TAG = "NotificationDrawerSettings";

    private static final String PREF_NOTIFICATION_HIDE_LABELS =
            "notification_hide_labels";
    private static final String PREF_NOTIFICATION_ALPHA =
            "notification_alpha";

    private static final String PREF_NOTIFICATION_CW_LABEL_COLOR =
            "notification_carrier_wifi_label_color";

    private static final String KEY_NOTIFICATION_DRAWER = "notification_drawer";
    private static final String KEY_NOTIFICATION_DRAWER_TABLET = "notification_drawer_tablet";
    private static final String CLOCK_SHORTCUT = "clock_shortcut";
    private static final String CALENDAR_SHORTCUT = "calendar_shortcut";

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DEFAULT_LABEL_COLOR = 0xff999999;

    private PreferenceScreen mPhoneDrawer;
    private PreferenceScreen mTabletDrawer;

    ListPreference mHideLabels;
    SeekBarPreference mNotificationAlpha;
    private AppSelectListPreference mClockShortcut;
    private AppSelectListPreference mCalendarShortcut;

    private ColorPickerPreference mNotificationCwLabelColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.notification_drawer_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        int intColor;
        String hexColor;

        mHideLabels = (ListPreference) findPreference(PREF_NOTIFICATION_HIDE_LABELS);
        int hideCarrier = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_HIDE_LABELS, 0);
        mHideLabels.setValue(String.valueOf(hideCarrier));
        mHideLabels.setOnPreferenceChangeListener(this);
        updateHideNotificationLabelsSummary(hideCarrier);

        PackageManager pm = getPackageManager();
        boolean isMobileData = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

        /* Tablet case in handled in PhoneStatusBar
        if (!DeviceUtils.isPhone(getActivity())
            || !DeviceUtils.deviceSupportsMobileData(getActivity())) {
            // Nothing for tablets, large screen devices and non mobile devices which doesn't show
            // information in notification drawer.....remove options
            prefs.removePreference(mHideCarrier);
        }*/

        float transparency;
        try{
            transparency = Settings.System.getFloat(getContentResolver(),
                    Settings.System.NOTIFICATION_ALPHA);
        } catch (Exception e) {
            transparency = 0;
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.NOTIFICATION_ALPHA, 0.0f);
        }
        mNotificationAlpha = (SeekBarPreference) findPreference(PREF_NOTIFICATION_ALPHA);
        mNotificationAlpha.setInitValue((int) (transparency * 100));
        mNotificationAlpha.setOnPreferenceChangeListener(this);

    	mPhoneDrawer = (PreferenceScreen) findPreference(KEY_NOTIFICATION_DRAWER);
        mTabletDrawer = (PreferenceScreen) findPreference(KEY_NOTIFICATION_DRAWER_TABLET);

        /*if (Utils.isTablet(getActivity())) {
            if (mPhoneDrawer != null) {
                getPreferenceScreen().removePreference(mPhoneDrawer);
            }
        } else*/ {
            if (mTabletDrawer != null) {
                getPreferenceScreen().removePreference(mTabletDrawer);
            }
        }

        mClockShortcut = (AppSelectListPreference)prefs.findPreference(CLOCK_SHORTCUT);
        mClockShortcut.setOnPreferenceChangeListener(this);

        mCalendarShortcut = (AppSelectListPreference)prefs.findPreference(CALENDAR_SHORTCUT);
        mCalendarShortcut.setOnPreferenceChangeListener(this);

        mNotificationCwLabelColor = (ColorPickerPreference) findPreference(PREF_NOTIFICATION_CW_LABEL_COLOR);
        intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_CARRIER_WIFI_LABEL_COLOR, DEFAULT_LABEL_COLOR);
        mNotificationCwLabelColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xff999999 & intColor));
        mNotificationCwLabelColor.setSummary(hexColor);
        mNotificationCwLabelColor.setOnPreferenceChangeListener(this);

        updateClockCalendarSummary();
        updatePreference();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset_default_message)
                .setIcon(R.drawable.ic_settings_backup)
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
        alertDialog.setTitle(R.string.shortcut_action_reset);
        alertDialog.setMessage(R.string.reset_settings_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        Settings.System.putInt(getContentResolver(),
                Settings.System.NOTIFICATION_CARRIER_WIFI_LABEL_COLOR, DEFAULT_LABEL_COLOR);
        String bgColor = String.format("#%08x", (0xff999999 & DEFAULT_LABEL_COLOR));
        mNotificationCwLabelColor.setSummary(bgColor);
        mNotificationCwLabelColor.setNewPreviewColor(DEFAULT_LABEL_COLOR);
    }

    private void updatePreference() {
        int hideCarrier = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_HIDE_LABELS, 0);

        if (hideCarrier == 0)  {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0);
            mNotificationCwLabelColor.setEnabled(false);
        } else {
            mNotificationCwLabelColor.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreference();
    }

    @Override
    public void onPause() {
        super.onResume();
        updatePreference();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHideLabels) {
            int hideLabels = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_HIDE_LABELS,
                    hideLabels);
            updateHideNotificationLabelsSummary(hideLabels);
            updatePreference();
            return true;
        } else if (preference == mNotificationAlpha) {
            float valNav = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getContentResolver(),
                    Settings.System.NOTIFICATION_ALPHA, valNav / 100);
            return true;
        } else if (preference == mClockShortcut) {
            String value = (String) newValue;
            // a value of null means to use the default
            Settings.System.putString(getContentResolver(),
                    Settings.System.CLOCK_SHORTCUT, value);
            updateClockCalendarSummary();
        } else if (preference == mCalendarShortcut) {
            String value = (String) newValue;
            // a value of null means to use the default
            Settings.System.putString(getContentResolver(),
                    Settings.System.CALENDAR_SHORTCUT, value);
            updateClockCalendarSummary();
        } else if (preference == mNotificationCwLabelColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NOTIFICATION_CARRIER_WIFI_LABEL_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        }
        return false;
    }

    private void updateHideNotificationLabelsSummary(int value) {
        Resources res = getResources();

        StringBuilder text = new StringBuilder();

        switch (value) {
        case 1  : text.append(res.getString(R.string.notification_hide_labels_carrier));
                break;
        case 2  : text.append(res.getString(R.string.notification_hide_labels_wifi));
                break;
        case 3  : text.append(res.getString(R.string.notification_hide_labels_all));
                break;
        default : text.append(res.getString(R.string.notification_hide_labels_disable));
                break;
        }

        text.append(" " + res.getString(R.string.notification_hide_labels_text));

        mHideLabels.setSummary(text.toString());
    }

    private void updateClockCalendarSummary() {
        final PackageManager packageManager = getPackageManager();

        mClockShortcut.setSummary(getResources().getString(R.string.default_shortcut));
        mCalendarShortcut.setSummary(getResources().getString(R.string.default_shortcut));

        String clockShortcutIntentUri = Settings.System.getString(getContentResolver(), Settings.System.CLOCK_SHORTCUT);
        if (clockShortcutIntentUri != null) {
            Intent clockShortcutIntent = null;
            try {
                clockShortcutIntent = Intent.parseUri(clockShortcutIntentUri, 0);
            } catch (URISyntaxException e) {
                clockShortcutIntent = null;
            }

            if(clockShortcutIntent != null) {
                ResolveInfo info = packageManager.resolveActivity(clockShortcutIntent, 0);
                if (info != null) {
                    mClockShortcut.setSummary(info.loadLabel(packageManager));
                }
            }
        }

        String calendarShortcutIntentUri = Settings.System.getString(getContentResolver(), Settings.System.CALENDAR_SHORTCUT);
        if (calendarShortcutIntentUri != null) {
            Intent calendarShortcutIntent = null;
            try {
                calendarShortcutIntent = Intent.parseUri(calendarShortcutIntentUri, 0);
            } catch (URISyntaxException e) {
                calendarShortcutIntent = null;
            }

            if(calendarShortcutIntent != null) {
                ResolveInfo info = packageManager.resolveActivity(calendarShortcutIntent, 0);
                if (info != null) {
                    mCalendarShortcut.setSummary(info.loadLabel(packageManager));
                }
            }
        }
    }
}
