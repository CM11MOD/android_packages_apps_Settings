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

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
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

import com.android.internal.util.slim.AppHelper;
import com.android.internal.util.slim.ButtonsConstants;
import com.android.internal.util.slim.DeviceUtils;
import com.android.internal.util.slim.DeviceUtils.FilteredDeviceFeaturesArray;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.slim.util.ShortcutPickerHelper;
import com.android.settings.Utils;

public class ShakeEvents extends SettingsPreferenceFragment
        implements CompoundButton.OnCheckedChangeListener,
        Preference.OnPreferenceChangeListener,
        ShortcutPickerHelper.OnPickListener {

    private static final String KEY_ENABLE_SHAKE_EVENTS = "enable_shake_events";
    private static final String KEY_SHAKE_EVENT_X = "shake_event_x";
    private static final String KEY_SHAKE_EVENT_Y = "shake_event_y";
    private static final String KEY_SHAKE_EVENT_Z = "shake_event_z";

    private static final int DLG_SLIM_ACTIONS = 0;

    private String mShakeEventPicked = null;

    private CheckBoxPreference mShakeEnabled;
    private Preference mShakeX;
    private Preference mShakeY;
    private Preference mShakeZ;

    private ShortcutPickerHelper mPicker;

    private SettingsObserver mSettingsObserver = new SettingsObserver(new Handler());

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
                Settings.System.SHAKE_LISTENER_ENABLED, 0) == 1);
    }

    @Override
    public void onStop() {
        super.onStop();
        final Activity activity = getActivity();
        activity.getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getActionBar().setCustomView(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.shake_event_settings);

        mPicker = new ShortcutPickerHelper(getActivity(), this);

        PreferenceScreen prefSet = getPreferenceScreen();

        mShakeX = (Preference) prefSet.findPreference(KEY_SHAKE_EVENT_X);
        mShakeY = (Preference) prefSet.findPreference(KEY_SHAKE_EVENT_Y);
        mShakeZ = (Preference) prefSet.findPreference(KEY_SHAKE_EVENT_Z);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSettings();
        mSettingsObserver.observe();
        updateEnabledState();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    private void updateSettings() {
        mShakeX.setSummary(returnFriendlyName(0));
        mShakeY.setSummary(returnFriendlyName(1));
        mShakeZ.setSummary(returnFriendlyName(2));
    }

    private String returnFriendlyName(int setting) {
        final String uri = Settings.System.getString(
                getContentResolver(),
                Settings.System.SHAKE_EVENTS_REGULAR[setting]);

        if (uri != null) {
            if (uri.startsWith("**")) {
                FilteredDeviceFeaturesArray
                        finalActionDialogArray = new FilteredDeviceFeaturesArray();
                finalActionDialogArray =
                    DeviceUtils.filterUnsupportedDeviceFeatures(getActivity(),
                    getResources().getStringArray(getResources().getIdentifier(
                    "shortcut_action_values", "array", "com.android.settings")),
                    getResources().getStringArray(getResources().getIdentifier(
                    "shortcut_action_entries", "array", "com.android.settings")));
                final String[] dialogValues = finalActionDialogArray.values;
                final String[] dialogEntries = finalActionDialogArray.entries;
                String slimDescription = null;
                for (int i = 0; i < dialogValues.length; i++) {
                    if (uri.equals(dialogValues[i])) {
                        slimDescription = dialogEntries[i];
                    }
                }
                return slimDescription;
            } else {
                return AppHelper.getShortcutPreferred(
                        getActivity(), getActivity().getPackageManager(), uri);
            }
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mShakeX) {
            mShakeEventPicked = KEY_SHAKE_EVENT_X;
            showDialogInner(DLG_SLIM_ACTIONS);
            return true;
        } else if (preference == mShakeY) {
            mShakeEventPicked = KEY_SHAKE_EVENT_Y;
            showDialogInner(DLG_SLIM_ACTIONS);
            return true;
        } else if (preference == mShakeZ) {
            mShakeEventPicked = KEY_SHAKE_EVENT_Z;
            showDialogInner(DLG_SLIM_ACTIONS);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void shortcutPicked(String uri,
            String friendlyName, Bitmap bmp, boolean isApplication) {
        if (uri == null) {
            return;
        }

        savePickedEvent(uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void savePickedEvent(final String uri) {
        if (mShakeEventPicked != null) {
            if (mShakeEventPicked.equals(KEY_SHAKE_EVENT_X)) {
                Settings.System.putString(getContentResolver(),
                        Settings.System.SHAKE_EVENTS_REGULAR[0], uri);
                mShakeX.setSummary(returnFriendlyName(0));
            } else if (mShakeEventPicked.equals(KEY_SHAKE_EVENT_Y)) {
                Settings.System.putString(getContentResolver(),
                        Settings.System.SHAKE_EVENTS_REGULAR[1], uri);
                mShakeY.setSummary(returnFriendlyName(1));
            } else if (mShakeEventPicked.equals(KEY_SHAKE_EVENT_Z)) {
                Settings.System.putString(getContentResolver(),
                        Settings.System.SHAKE_EVENTS_REGULAR[2], uri);
                mShakeZ.setSummary(returnFriendlyName(2));
            }
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

        ShakeEvents getOwner() {
            return (ShakeEvents) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_SLIM_ACTIONS:
                    FilteredDeviceFeaturesArray
                            finalActionDialogArray = new FilteredDeviceFeaturesArray();
                    finalActionDialogArray =
                        DeviceUtils.filterUnsupportedDeviceFeatures(getOwner().getActivity(),
                        getResources().getStringArray(getOwner().getResources().getIdentifier(
                        "shortcut_action_values", "array", "com.android.settings")),
                        getOwner().getResources().getStringArray(getResources().getIdentifier(
                        "shortcut_action_entries", "array", "com.android.settings")));
                    final String[] dialogValues = finalActionDialogArray.values;
                    final String[] dialogEntries = finalActionDialogArray.entries;

                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.shortcut_action_select_action)
                    .setNegativeButton(R.string.cancel, null)
                    .setItems(dialogEntries,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (dialogValues[item].equals(ButtonsConstants.ACTION_APP)) {
                                if (getOwner().mPicker != null) {
                                    getOwner().mPicker.pickShortcut(getOwner().getId());
                                }
                            } else {
                                getOwner().savePickedEvent(dialogValues[item]);
                            }
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

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = getActivity().getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SHAKE_LISTENER_ENABLED), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateSettings();
            updateEnabledState();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mEnabledSwitch) {

            boolean value = ((Boolean)isChecked).booleanValue();
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SHAKE_LISTENER_ENABLED,
                    value ? 1 : 0);           
        }
    }

    private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.SHAKE_LISTENER_ENABLED, 0) == 1;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }
}
