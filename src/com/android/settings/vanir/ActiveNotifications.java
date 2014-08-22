package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.vanir.AppMultiSelectListPreference;

import com.android.internal.util.slim.DeviceUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ActiveNotifications extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, CompoundButton.OnCheckedChangeListener,
        DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private static final String KEY_NOTIFICATION_MODE = "active_notification_mode";
    private static final String KEY_ADDITIONAL = "additional_options";
    private static final String KEY_QUIET_HOURS = "quiet_hours";
    private static final String KEY_EXCLUDED_APPS = "ad_excluded_apps";
    private static final String KEY_EXCLUDED_NOTIF_APPS = "excluded_apps";

    private Switch mEnabledSwitch;
    private boolean mDialogClicked;
    private Dialog mEnableDialog;

    private CheckBoxPreference mQuietHours;
    private ListPreference mNotiModePref;
    private AppMultiSelectListPreference mExcludedAppsPref;
    private AppMultiSelectListPreference mNotifAppsPref;

    private Preference mAdditional;
    private boolean mActiveNotifications;

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
                Settings.System.ACTIVE_NOTIFICATIONS, 0) == 1);
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

        addPreferencesFromResource(R.xml.active_notifications);
        PreferenceScreen prefs = getPreferenceScreen();
        final ContentResolver cr = getActivity().getContentResolver();

        mNotiModePref = (ListPreference) prefs.findPreference(KEY_NOTIFICATION_MODE);
        int view = Settings.System.getInt(cr,
                        Settings.System.ACTIVE_NOTIFICATIONS_MODE, 0);
        mNotiModePref.setValue(String.valueOf(view));
        mNotiModePref.setSummary(mNotiModePref.getEntry());
        mNotiModePref.setOnPreferenceChangeListener(this);

        mQuietHours = (CheckBoxPreference) prefs.findPreference(KEY_QUIET_HOURS);
        mQuietHours.setChecked(Settings.System.getInt(cr,
                Settings.System.ACTIVE_NOTIFICATIONS_QUIET_HOURS, 0) == 1);
        mQuietHours.setOnPreferenceChangeListener(this);

        mExcludedAppsPref = (AppMultiSelectListPreference) findPreference(KEY_EXCLUDED_APPS);
        Set<String> excludedApps = getExcludedApps();
        if (excludedApps != null) {
            mExcludedAppsPref.setValues(excludedApps);
        }
        mExcludedAppsPref.setOnPreferenceChangeListener(this);

        mNotifAppsPref = (AppMultiSelectListPreference) findPreference(KEY_EXCLUDED_NOTIF_APPS);
        Set<String> excludedNotifApps = getExcludedNotifApps();
        if (excludedNotifApps != null) {
            mNotifAppsPref.setValues(excludedNotifApps);
        }
        mNotifAppsPref.setOnPreferenceChangeListener(this);

        mAdditional = (PreferenceScreen) prefs.findPreference(KEY_ADDITIONAL);

        updateDependency();
    }

    private boolean isKeyguardSecure() {
        LockPatternUtils mLockPatternUtils = new LockPatternUtils(getActivity());
        boolean isSecure = mLockPatternUtils.isSecure();
        return isSecure;
    }

    private void updateDependency() {
        mActiveNotifications = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.ACTIVE_NOTIFICATIONS, 0) == 1;
        int view = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.ACTIVE_NOTIFICATIONS_MODE, 0);

        mAdditional.setEnabled(mActiveNotifications);

        if (mActiveNotifications && (view == 1)) {
            mExcludedAppsPref.setEnabled(true);
            mNotifAppsPref.setEnabled(false);
            mQuietHours.setEnabled(true);
        } else if (mActiveNotifications && (view == 2)) {
            mNotifAppsPref.setEnabled(true);
            mExcludedAppsPref.setEnabled(false);
            mQuietHours.setEnabled(true);
        } else {
            mNotifAppsPref.setEnabled(false);
            mExcludedAppsPref.setEnabled(false);
            mQuietHours.setEnabled(false);
        }
    }
       
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value;
        if (preference == mNotiModePref) {
            int mode = Integer.valueOf((String) newValue);
            int index = mNotiModePref.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ACTIVE_NOTIFICATIONS_MODE, mode);
            mNotiModePref.setSummary(mNotiModePref.getEntries()[index]);
            updateDependency();
            return true;
        } else if (preference == mQuietHours) {
            value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ACTIVE_NOTIFICATIONS_QUIET_HOURS,
            value ? 1 : 0);
            return true;
        } else if (preference == mExcludedAppsPref) {
            storeExcludedApps((Set<String>) newValue);
            return true;
        } else if (preference == mNotifAppsPref) {
			storeExcludedNotifApps((Set<String>) newValue);
			return true;
        } else {
            return false;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final boolean lesConditionnelles = isKeyguardSecure();
        if (buttonView == mEnabledSwitch) {
            if (isChecked && lesConditionnelles) {
                if (isChecked) {
                    mDialogClicked = false;
                    if (mEnableDialog != null) {
                        dismissDialogs();
                    }
                    mEnableDialog = new AlertDialog.Builder(getActivity()).setMessage(
                            getActivity().getResources().getString(
                                    R.string.lockscreen_notifications_dialog_message))
                            .setTitle(R.string.lockscreen_notifications_dialog_title)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setPositiveButton(android.R.string.yes, this)
                            .show();
                    mEnableDialog.setOnDismissListener(this);
                }
            }
            boolean value = ((Boolean)isChecked).booleanValue();
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.ACTIVE_NOTIFICATIONS,
                    value ? 1 : 0);

            updateDependency();
        }
    }

    private void dismissDialogs() {
        if (mEnableDialog != null) {
            mEnableDialog.dismiss();
            mEnableDialog = null;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mEnableDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mDialogClicked = true;
            }
        }
    }

    private Set<String> getExcludedApps() {
        String excluded = Settings.System.getString(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_EXCLUDED_APPS);
        if (TextUtils.isEmpty(excluded))
            return null;

        return new HashSet<String>(Arrays.asList(excluded.split("\\|")));
    }

    private void storeExcludedApps(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_EXCLUDED_APPS, builder.toString());
    }

    private Set<String> getExcludedNotifApps() {
        String excludedNotif = Settings.System.getString(getContentResolver(),
        Settings.System.LOCKSCREEN_NOTIFICATIONS_EXCLUDED_APPS);
        if (TextUtils.isEmpty(excludedNotif)) return null;

        return new HashSet<String>(Arrays.asList(excludedNotif.split("\\|")));
    }

    private void storeExcludedNotifApps(Set<String> values) {
        StringBuilder Notifbuilder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
			Notifbuilder.append(delimiter);
			Notifbuilder.append(value);
			delimiter = "|";
        }
        Settings.System.putString(getContentResolver(),
			Settings.System.LOCKSCREEN_NOTIFICATIONS_EXCLUDED_APPS, Notifbuilder.toString());
    }

    public void onDismiss(DialogInterface dialog) {
        // ahh!
    }
}
