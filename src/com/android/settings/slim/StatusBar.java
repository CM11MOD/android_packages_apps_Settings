/*
 * Copyright (C) 2013 SlimRoms Project
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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.widget.EditText;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.util.Helpers;

import com.android.internal.util.slim.DeviceUtils;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBarSettings";

    private static final String KEY_STATUS_BAR_CLOCK = "clock_style_pref";
    private static final String STATUS_BAR_CARRIER = "status_bar_carrier";
    private static final String STATUS_BAR_CARRIER_COLOR = "status_bar_carrier_color";
    private static final String CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String NO_KEYGUARD_CARRIER = "no_carrier_label";
    private static final String STATUS_BAR_BRIGHTNESS = "statusbar_brightness_slider";
    private static final String SIGNAL_STYLE = "signal_style";
    private static final String HIDE_SIGNAL = "hide_signal";
    private static final String CATEGORY_SIGNAL = "signal";
    private static final String KEY_4G_SHOW_LTE = "statusbar_signal_show_4g_for_lte";
    private static final String STATUS_BAR_NETWORK_ACTIVITY = "status_bar_network_activity";

    static final int DEFAULT_STATUS_ICON_COLOR = 0xffffffff;

    private PreferenceScreen mClockStyle;
    private CheckBoxPreference mStatusBarCarrier;
    private PreferenceScreen mCustomStatusBarCarrierLabel;
    private CheckBoxPreference mNoKeyguardCarrier;
    private CheckBoxPreference mStatusbarSliderPreference;
    private CheckBoxPreference mStatusBarNetworkActivity;
    private ColorPickerPreference mCarrierColorPicker;

    private String mCustomStatusBarCarrierLabelText;
    private ListPreference mDbmStyletyle;
    private CheckBoxPreference mHideSignal;

    private PreferenceGroup mSignalCategory;
    private CheckBoxPreference mShowLTE;

    private static final int MENU_RESET = Menu.FIRST;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int Color1;
        String hexColor;

        mClockStyle = (PreferenceScreen) prefSet.findPreference(KEY_STATUS_BAR_CLOCK);
        if (mClockStyle != null) {
            updateClockStyleDescription();
        }

        mStatusbarSliderPreference = (CheckBoxPreference) findPreference(STATUS_BAR_BRIGHTNESS);
        mStatusbarSliderPreference.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));

        mStatusBarCarrier = (CheckBoxPreference) findPreference(STATUS_BAR_CARRIER);
        mStatusBarCarrier.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CARRIER, 0) == 1));

        mNoKeyguardCarrier = (CheckBoxPreference) findPreference(NO_KEYGUARD_CARRIER);
        mNoKeyguardCarrier.setChecked(Settings.System.getIntForUser(resolver,
                Settings.System.NO_CARRIER_LABEL, 0, UserHandle.USER_CURRENT_OR_SELF) == 1);

        mCarrierColorPicker = (ColorPickerPreference) prefSet.findPreference(STATUS_BAR_CARRIER_COLOR);
        mCarrierColorPicker.setOnPreferenceChangeListener(this);
        Color1 = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER_COLOR, DEFAULT_STATUS_ICON_COLOR);
        hexColor = String.format("#%08x", (0xffffffff & Color1));
        mCarrierColorPicker.setSummary(hexColor);
        mCarrierColorPicker.setNewPreviewColor(Color1);

        mDbmStyletyle = (ListPreference) findPreference("signal_style");
        mDbmStyletyle.setOnPreferenceChangeListener(this);
        mDbmStyletyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_SIGNAL_TEXT,
                0)));

        mHideSignal = (CheckBoxPreference) findPreference("hide_signal");
        mHideSignal.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_HIDE_SIGNAL_BARS,
                0) != 0);
        mHideSignal.setOnPreferenceChangeListener(this);

        mStatusBarNetworkActivity = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_ACTIVITY);
        mStatusBarNetworkActivity.setChecked(Settings.System.getInt(resolver,
        Settings.System.STATUS_BAR_NETWORK_ACTIVITY, 0) == 1);
        mStatusBarNetworkActivity.setOnPreferenceChangeListener(this);

        mSignalCategory = (PreferenceGroup) findPreference(CATEGORY_SIGNAL);

        mShowLTE = (CheckBoxPreference) findPreference(KEY_4G_SHOW_LTE);
        if (mShowLTE != null) {
            mShowLTE.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUSBAR_SIGNAL_SHOW_4G_FOR_LTE,
                0) == 1);
                mShowLTE.setOnPreferenceChangeListener(this);
            if (!DeviceUtils.deviceSupportsLte(getActivity())) {
                mSignalCategory.removePreference(findPreference(KEY_4G_SHOW_LTE));
            }
        }

        if (DeviceUtils.isPhone(getActivity())) {
            PreferenceScreen notifSystemIcons =
                (PreferenceScreen) findPreference("status_bar_notif_system_icons");
            notifSystemIcons.setTitle(R.string.status_bar_notif_system_icons_title_phone);
        }

        mCustomStatusBarCarrierLabel = (PreferenceScreen) findPreference(CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();
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
        alertDialog.setMessage(R.string.progressbar_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                StatusBarReset();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void StatusBarReset() {
        Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CARRIER, 0);
        Settings.System.putInt(getContentResolver(), Settings.System.STATUSBAR_HIDE_SIGNAL_BARS, 0);
        Settings.System.putInt(getContentResolver(), Settings.System.STATUSBAR_SIGNAL_TEXT, 0);
        Settings.System.putInt(getContentResolver(), Settings.System.STATUSBAR_BRIGHTNESS_SLIDER, 0);
        Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CARRIER_COLOR, DEFAULT_STATUS_ICON_COLOR);

        mStatusBarCarrier.setChecked(false);
        mHideSignal.setChecked(false);
        mDbmStyletyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_SIGNAL_TEXT,
                0)));
        mStatusbarSliderPreference.setChecked(false);
        mCarrierColorPicker.setNewPreviewColor(DEFAULT_STATUS_ICON_COLOR);
    }

    private void updateCustomLabelTextSummary() {
        mCustomStatusBarCarrierLabelText = Settings.System.getString(getActivity().getContentResolver(),
            Settings.System.CUSTOM_CARRIER_LABEL);

        if (TextUtils.isEmpty(mCustomStatusBarCarrierLabelText)) {
            mCustomStatusBarCarrierLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomStatusBarCarrierLabel.setSummary(mCustomStatusBarCarrierLabelText);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         final Preference preference) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusbarSliderPreference) {
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, mStatusbarSliderPreference.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mStatusBarCarrier) {
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_CARRIER, mStatusBarCarrier.isChecked() ? 1 : 0);
            return true;
        } else if (preference.getKey().equals(CUSTOM_CARRIER_LABEL)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(TextUtils.isEmpty(mCustomStatusBarCarrierLabelText) ? "" : mCustomStatusBarCarrierLabelText);
            input.setSelection(input.getText().length());
            alert.setView(input);
            alert.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString().trim();
                    Settings.System.putString(resolver, Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
                    getActivity().sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), null);
            alert.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mCarrierColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER_COLOR, intHex);
            return true;
        } else if (preference == mHideSignal) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_HIDE_SIGNAL_BARS, value ? 1 : 0);
            return true;
        } else if (preference == mShowLTE) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_SIGNAL_SHOW_4G_FOR_LTE, value ? 1 : 0);
            return true;
        } else if (preference == mDbmStyletyle) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_SIGNAL_TEXT, val);
            return true;
        } else if (preference == mStatusBarNetworkActivity) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                Settings.System.STATUS_BAR_NETWORK_ACTIVITY, value ? 1 : 0);
            return true;
        } else if (preference == mNoKeyguardCarrier) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                Settings.System.NO_CARRIER_LABEL, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockStyleDescription();
    }

    private void updateClockStyleDescription() {
        if (Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
            mClockStyle.setSummary(getString(R.string.enabled));
        } else {
            mClockStyle.setSummary(getString(R.string.disabled));
         }
    }

}
