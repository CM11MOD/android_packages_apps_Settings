package com.android.settings.slim;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NotificationService extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "NotificationService";

    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_LOCK_NOTIFICATIONS = "lockscreen_notifications";
    private static final String PREF_LOCK_NOTI = "active_notifications";
    private static final String PREF_HALO = "halo_settings";
    private static final String PREF_HOVER = "hover_settings";
    private static final String PREF_HEADSUP = "heads_up_enabled";


    private static final String KEY_LIGHT_OPTIONS = "category_light_options";
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_NOTIFICATION_LIGHT = "notification_light";
    private static final String KEY_BATTERY_LIGHT = "battery_light";

    private PreferenceGroup mNotification;
    private PreferenceGroup mLockNotification;
    private Preference mLockNoti;
    private Preference mHeadsUp;
    private Preference mHalo;
    private Preference mHover;

    private CheckBoxPreference mNotificationPulse;
    private PreferenceCategory mLightOptions;
    private PreferenceScreen mNotificationLight;
    private PreferenceScreen mBatteryPulse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notifications_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        ContentResolver resolver = getActivity().getContentResolver();

        mNotification = (PreferenceGroup) prefSet.findPreference(KEY_NOTIFICATIONS);
        mLockNotification = (PreferenceGroup) prefSet.findPreference(KEY_LOCK_NOTIFICATIONS);

        if (mNotification != null) {
            mHeadsUp = prefSet.findPreference(PREF_HEADSUP);
            mHalo = prefSet.findPreference(PREF_HALO);
            mHover = prefSet.findPreference(PREF_HOVER);
        }

        if (mLockNotification != null) {
            mLockNoti = prefSet.findPreference(PREF_LOCK_NOTI);
        }

        mLightOptions = (PreferenceCategory) prefSet.findPreference(KEY_LIGHT_OPTIONS);
        mNotificationPulse = (CheckBoxPreference) findPreference(KEY_NOTIFICATION_PULSE);
        mNotificationLight = (PreferenceScreen) findPreference(KEY_NOTIFICATION_LIGHT);
        mBatteryPulse = (PreferenceScreen) findPreference(KEY_BATTERY_LIGHT);
        if (mNotificationPulse != null && mNotificationLight != null && mBatteryPulse != null) {
            if (getResources().getBoolean(
                    com.android.internal.R.bool.config_intrusiveNotificationLed)) {
                 if (getResources().getBoolean(
                         com.android.internal.R.bool.config_multiColorNotificationLed)) {
                     mLightOptions.removePreference(mNotificationPulse);
                     updateLightPulseDescription();
                 } else {
                     mLightOptions.removePreference(mNotificationLight);
                     try {
                         mNotificationPulse.setChecked(Settings.System.getInt(resolver,
                                 Settings.System.NOTIFICATION_LIGHT_PULSE) == 1);
                     } catch (SettingNotFoundException e) {
                         e.printStackTrace();
                     }
                 }
            } else {
                 mLightOptions.removePreference(mNotificationPulse);
                 mLightOptions.removePreference(mNotificationLight);
            }

            if (!getResources().getBoolean(
                    com.android.internal.R.bool.config_intrusiveBatteryLed)) {
                mLightOptions.removePreference(mBatteryPulse);
            } else {
                updateBatteryPulseDescription();
            }

            //If we're removed everything, get rid of the category
            if (mLightOptions.getPreferenceCount() == 0) {
                prefSet.removePreference(mLightOptions);
            }
        }
        updateLightPulseDescription();
        updateBatteryPulseDescription();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateheadsupState();
        updatehaloState();
        updatehoverState();
        updatelockNotiState();
        updateLightPulseDescription();
        updateBatteryPulseDescription();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mNotificationPulse) {
            boolean value = mNotificationPulse.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_LIGHT_PULSE,
                    value ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    private void updateheadsupState() {
        boolean headsUpEnabled = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HEADS_UP_NOTIFICATION, 0, ActivityManager.getCurrentUser()) == 1;
        mHeadsUp.setSummary(headsUpEnabled
                ? R.string.summary_heads_up_enabled : R.string.summary_heads_up_disabled);
    }

    private void updatehaloState() {
        boolean haloEnabled = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HALO_ENABLED, 0, ActivityManager.getCurrentUser()) == 1;
        boolean active = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HALO_ACTIVE, 0, ActivityManager.getCurrentUser()) == 1;
        if (haloEnabled && active) {
            mHalo.setSummary(R.string.halo_enabled_summary);
        } else {
            mHalo.setSummary(R.string.halo_disabled_summary);
        }
    }

    private void updatehoverState() {
        boolean hoverEnabled = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HOVER_ENABLED, 0, ActivityManager.getCurrentUser()) == 1;
        boolean active = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HOVER_ACTIVE, 0, ActivityManager.getCurrentUser()) == 1;
        if (hoverEnabled && active) {
            mHover.setSummary(R.string.hover_enabled_summary);
        } else {
            mHover.setSummary(R.string.hover_disabled_summary);
        }
    }

    private void updatelockNotiState() {
        boolean enabled = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ACTIVE_NOTIFICATIONS, 0, ActivityManager.getCurrentUser()) == 1;
        int mode = Settings.System.getIntForUser(getContentResolver(),
                        Settings.System.ACTIVE_NOTIFICATIONS_MODE, 0, ActivityManager.getCurrentUser());

        if (enabled) {
            if (mode == 0) {
                mLockNoti.setSummary(R.string.pocket_mode_off);
            } else if (mode == 1) {
                mLockNoti.setSummary(R.string.ad_settings_title);
            } else if (mode == 2) {
                mLockNoti.setSummary(R.string.lockscreen_notifications_title);
            } else if (mode == 3) {
                mLockNoti.setSummary(R.string.notification_peek_title);
            }
        } else {
            mLockNoti.setSummary(R.string.pocket_mode_off);
        }
    }

    private void updateLightPulseDescription() {
        if (mNotificationPulse == null) {
            return;
        }
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_PULSE, 0) == 1) {
            mNotificationLight.setSummary(getString(R.string.enabled));
        } else {
            mNotificationLight.setSummary(getString(R.string.disabled));
        }
    }

    private void updateBatteryPulseDescription() {
        if (mBatteryPulse == null) {
            return;
        }
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BATTERY_LIGHT_ENABLED, 1) == 1) {
            mBatteryPulse.setSummary(getString(R.string.enabled));
        } else {
            mBatteryPulse.setSummary(getString(R.string.disabled));
        }
     }

}
