/*
 * Copyright (C) 2013 The ChameleonOS Project
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

package com.android.settings.vanir;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.WindowManager;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.slim.DeviceUtils;
import com.android.settings.widget.SeekBarPreference2;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class ActiveNotificationSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "ActiveDisplaySettings";

    private static final String KEY_REDISPLAY = "ad_redisplay";
    private static final String KEY_SHOW_AMPM = "ad_show_ampm";
    private static final String KEY_BRIGHTNESS = "ad_brightness";
    private static final String KEY_TIMEOUT = "ad_timeout";
    private static final String KEY_THRESHOLD = "ad_threshold";
    private static final String KEY_OFFSET_TOP = "offset_top";
    private static final String KEY_EXPANDED_VIEW = "expanded_view";
    private static final String KEY_FORCE_EXPANDED_VIEW = "force_expanded_view";
    private static final String KEY_NOTIFICATIONS_HEIGHT = "notifications_height";
    private static final String KEY_WAKE_ON_NOTIFICATION = "wake_on_notification";
    private static final String KEY_NOTIFICATION_COLOR = "notification_color";
    private static final String KEY_SUNLIGHT_MODE = "ad_sunlight_mode";
    private static final String KEY_TURNOFF_MODE = "ad_turnoff_mode";
    private static final String KEY_BYPASS_CONTENT = "ad_bypass";
    private static final String KEY_ANNOYING = "ad_annoying";
    private static final String KEY_SHAKE_THRESHOLD = "ad_shake_threshold";
    private static final String KEY_SHAKE_LONGTHRESHOLD = "ad_shake_long_threshold";
    private static final String KEY_SHAKE_TIMEOUT = "ad_shake_timeout";

    //peek
    private static final String KEY_PEEK_PICKUP_TIMEOUT = "peek_pickup_timeout";

    private ContentResolver mResolver;
    private Context mContext;

    private SeekBarPreference2 mAnnoyingNotification;
    private SeekBarPreference2 mShakeThreshold;
    private SeekBarPreference2 mShakeLongThreshold;
    private SeekBarPreference2 mShakeTimeout;
    private SeekBarPreference mOffsetTop;
    private CheckBoxPreference mWakeOnNotification;
    private CheckBoxPreference mExpandedView;
    private CheckBoxPreference mForceExpandedView;
    private NumberPickerPreference mNotificationsHeight;
    private ColorPickerPreference mNotificationColor;
    private CheckBoxPreference mBypassPref;
    private CheckBoxPreference mShowAmPmPref;
    private CheckBoxPreference mSunlightModePref;
    private CheckBoxPreference mTurnOffModePref;
    private SeekBarPreference2 mBrightnessLevel;
    private ListPreference mDisplayTimeout;
    private ListPreference mProximityThreshold;
    private ListPreference mRedisplayPref;
    private int mMinimumBacklight;
    private int mMaximumBacklight;

    private ListPreference mPeekPickupTimeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.active_notification_settings);

        mContext = getActivity().getApplicationContext();
        mResolver = mContext.getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();

        boolean AmonAmarth = Settings.System.getInt(mResolver,
                Settings.System.ENABLE_ACTIVE_DISPLAY, 0) == 1;

        mBypassPref = (CheckBoxPreference) prefSet.findPreference(KEY_BYPASS_CONTENT);
        mProximityThreshold = (ListPreference) prefSet.findPreference(KEY_THRESHOLD);
        mTurnOffModePref = (CheckBoxPreference) findPreference(KEY_TURNOFF_MODE);

        if (!DeviceUtils.deviceSupportsProximitySensor(mContext)) {
            prefSet.removePreference(mBypassPref);
            prefSet.removePreference(mProximityThreshold);
            prefSet.removePreference(mTurnOffModePref);
        } else {
            mBypassPref.setChecked((Settings.System.getInt(mResolver,
                Settings.System.ACTIVE_DISPLAY_BYPASS, 1) != 0));

            long threshold = Settings.System.getLong(mResolver,
                    Settings.System.ACTIVE_DISPLAY_THRESHOLD, 5000L);
            mProximityThreshold.setValue(String.valueOf(threshold));
            mProximityThreshold.setSummary(mProximityThreshold.getEntry());
            mProximityThreshold.setOnPreferenceChangeListener(this);

            mTurnOffModePref.setChecked((Settings.System.getInt(mResolver,
                    Settings.System.ACTIVE_DISPLAY_TURNOFF_MODE, 0) == 1));
        }

        mShowAmPmPref = (CheckBoxPreference) prefSet.findPreference(KEY_SHOW_AMPM);
        mShowAmPmPref.setChecked((Settings.System.getInt(mResolver,
                Settings.System.ACTIVE_DISPLAY_SHOW_AMPM, 0) == 1));
        mShowAmPmPref.setEnabled(!is24Hour());

        mSunlightModePref = (CheckBoxPreference) prefSet.findPreference(KEY_SUNLIGHT_MODE);
        if (!DeviceUtils.deviceSupportsLightSensor(mContext)) {
            prefSet.removePreference(mSunlightModePref);
        } else {
            mSunlightModePref.setChecked((Settings.System.getInt(mResolver,
                    Settings.System.ACTIVE_DISPLAY_SUNLIGHT_MODE, 0) == 1));
        }

        mRedisplayPref = (ListPreference) prefSet.findPreference(KEY_REDISPLAY);
        long timeout = Settings.System.getLong(mResolver,
                Settings.System.ACTIVE_DISPLAY_REDISPLAY, 0);
        mRedisplayPref.setValue(String.valueOf(timeout));
        mRedisplayPref.setSummary(mRedisplayPref.getEntry());
        mRedisplayPref.setOnPreferenceChangeListener(this);

        mAnnoyingNotification = (SeekBarPreference2) prefSet.findPreference(KEY_ANNOYING);
        mAnnoyingNotification.setValue(Settings.System.getInt(mResolver,
                Settings.System.ACTIVE_DISPLAY_ANNOYING, 0));
        mAnnoyingNotification.setOnPreferenceChangeListener(this);

        mShakeThreshold = (SeekBarPreference2) prefSet.findPreference(KEY_SHAKE_THRESHOLD);
        mShakeThreshold.setValue(Settings.System.getInt(mResolver,
        Settings.System.ACTIVE_DISPLAY_SHAKE_THRESHOLD, 10));
        mShakeThreshold.setOnPreferenceChangeListener(this);

        mShakeLongThreshold = (SeekBarPreference2) prefSet.findPreference(KEY_SHAKE_LONGTHRESHOLD);
        mShakeLongThreshold.setValue(Settings.System.getInt(mResolver,
        Settings.System.ACTIVE_DISPLAY_SHAKE_LONGTHRESHOLD, 2));
        mShakeLongThreshold.setOnPreferenceChangeListener(this);

        mShakeTimeout = (SeekBarPreference2) prefSet.findPreference(KEY_SHAKE_TIMEOUT);
        mShakeTimeout.setValue(Settings.System.getInt(mResolver,
        Settings.System.ACTIVE_DISPLAY_SHAKE_TIMEOUT, 3));
        mShakeTimeout.setOnPreferenceChangeListener(this);

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mMinimumBacklight = pm.getMinimumScreenBrightnessSetting();
        mMaximumBacklight = pm.getMaximumScreenBrightnessSetting();

        mBrightnessLevel = (SeekBarPreference2) prefSet.findPreference(KEY_BRIGHTNESS);
        int brightness = Settings.System.getInt(mResolver,
                Settings.System.ACTIVE_DISPLAY_BRIGHTNESS, mMaximumBacklight);
        int realBrightness =  (int)(((float)brightness / (float)mMaximumBacklight) * 100);
        mBrightnessLevel.setValue(realBrightness);
        mBrightnessLevel.setOnPreferenceChangeListener(this);

        try {
            if (Settings.System.getInt(mResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mBrightnessLevel.setEnabled(false);
                mBrightnessLevel.setSummary(R.string.ad_autobrightness_mode_on);
            }
            } catch (SettingNotFoundException e) {
        }

        mDisplayTimeout = (ListPreference) prefSet.findPreference(KEY_TIMEOUT);
        timeout = Settings.System.getLong(mResolver,
                Settings.System.ACTIVE_DISPLAY_TIMEOUT, 8000L);
        mDisplayTimeout.setValue(String.valueOf(timeout));
        mDisplayTimeout.setSummary(mDisplayTimeout.getEntry());
        mDisplayTimeout.setOnPreferenceChangeListener(this);

        mWakeOnNotification = (CheckBoxPreference) prefSet.findPreference(KEY_WAKE_ON_NOTIFICATION);
        mWakeOnNotification.setChecked(Settings.System.getInt(mResolver,
        Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION, 0) == 1);

        mOffsetTop = (SeekBarPreference) findPreference(KEY_OFFSET_TOP);
        mOffsetTop.setProgress((int)(Settings.System.getFloat(mResolver,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP, 0.3f) * 100));
        mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + mOffsetTop.getProgress() + "%");
        mOffsetTop.setOnPreferenceChangeListener(this);

        mExpandedView = (CheckBoxPreference) findPreference(KEY_EXPANDED_VIEW);
        mExpandedView.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW, 1) == 1);

        mForceExpandedView = (CheckBoxPreference) findPreference(KEY_FORCE_EXPANDED_VIEW);
        mForceExpandedView.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW, 0) == 1);

        mNotificationsHeight = (NumberPickerPreference) findPreference(KEY_NOTIFICATIONS_HEIGHT);
        mNotificationsHeight.setValue(Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, 4));

        Point displaySize = new Point();
        ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
        int max = Math.round((float)displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                (float)mContext.getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
        mNotificationsHeight.setMinValue(1);
        mNotificationsHeight.setMaxValue(max);
        mNotificationsHeight.setOnPreferenceChangeListener(this);

        mNotificationColor = (ColorPickerPreference) prefSet.findPreference(KEY_NOTIFICATION_COLOR);
        mNotificationColor.setAlphaSliderEnabled(true);
        int color = Settings.System.getInt(mResolver,
        Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, 0x55555555);
        String hexColor = String.format("#%08x", (0xffffffff & color));
        mNotificationColor.setSummary(hexColor);
        mNotificationColor.setDefaultValue(color);
        mNotificationColor.setNewPreviewColor(color);
        mNotificationColor.setOnPreferenceChangeListener(this);

        if (AmonAmarth) {
            mWakeOnNotification.setEnabled(false);
            mWakeOnNotification.setSummary(R.string.wake_on_notification_disable);
        } else {
            mWakeOnNotification.setEnabled(true);
            mWakeOnNotification.setSummary(R.string.wake_on_notification_summary);
        }

        mPeekPickupTimeout = (ListPreference) prefSet.findPreference(KEY_PEEK_PICKUP_TIMEOUT);
        int peekTimeout = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT, 10000, UserHandle.USER_CURRENT);
        mPeekPickupTimeout.setValue(String.valueOf(peekTimeout));
        mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntry());
        mPeekPickupTimeout.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRedisplayPref) {
            int val = Integer.parseInt((String) newValue);
            int index = mRedisplayPref.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.ACTIVE_DISPLAY_REDISPLAY, val);
            mRedisplayPref.setSummary(mRedisplayPref.getEntries()[index]);
            return true;
        } else if (preference == mAnnoyingNotification) {
            int annoying = ((Integer)newValue).intValue();
            Settings.System.putInt(mResolver,
                    Settings.System.ACTIVE_DISPLAY_ANNOYING, annoying);
            return true;
        } else if (preference == mShakeThreshold) {
            int threshold = ((Integer)newValue).intValue();
            Settings.System.putInt(mResolver,
                    Settings.System.ACTIVE_DISPLAY_SHAKE_THRESHOLD, threshold);
            return true;
        } else if (preference == mShakeLongThreshold) {
            long longThreshold = (long)(1000 * ((Integer)newValue).intValue());
            Settings.System.putLong(mResolver,
                    Settings.System.ACTIVE_DISPLAY_SHAKE_LONGTHRESHOLD, longThreshold);
            return true;
        } else if (preference == mShakeTimeout) {
            int timeout = ((Integer)newValue).intValue();
            Settings.System.putInt(mResolver,
            Settings.System.ACTIVE_DISPLAY_SHAKE_TIMEOUT, timeout);
            return true;
        } else if (preference == mBrightnessLevel) {
            int brightness = ((Integer)newValue).intValue();
            int realBrightness =  Math.max(mMinimumBacklight, (int)(((float)brightness / (float)100) * mMaximumBacklight));                   
            Settings.System.putInt(mResolver, Settings.System.ACTIVE_DISPLAY_BRIGHTNESS, realBrightness);
            return true;
        } else if (preference == mNotificationsHeight) {
            Settings.System.putInt(mResolver,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, (Integer)newValue);
            return true;
        } else if (preference == mDisplayTimeout) {
            int val = Integer.parseInt((String) newValue);
            int index = mDisplayTimeout.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.ACTIVE_DISPLAY_TIMEOUT, val);
            mDisplayTimeout.setSummary(mDisplayTimeout.getEntries()[index]);
            return true;
        } else if (preference == mProximityThreshold) {
            int val = Integer.parseInt((String) newValue);
            int index = mProximityThreshold.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.ACTIVE_DISPLAY_THRESHOLD, val);
            mProximityThreshold.setSummary(mProximityThreshold.getEntries()[index]);
            return true;
        } else if (preference == mNotificationColor) {
            String hex = ColorPickerPreference.convertToARGB(
            Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
            Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, intHex);
            return true;
        } else if (preference == mOffsetTop) {
            Settings.System.putFloat(mResolver,
                     Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP,
                    (Integer)newValue / 100f);
            mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + (Integer)newValue + "%");
            Point displaySize = new Point();
            ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
            int max = Math.round((float)displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                    (float)mContext.getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
            mNotificationsHeight.setMaxValue(max);
            return true;
        } else if (preference == mPeekPickupTimeout) {
            int index = mPeekPickupTimeout.findIndexOfValue((String) newValue);
            int peekTimeout = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.PEEK_PICKUP_TIMEOUT,
                    peekTimeout, UserHandle.USER_CURRENT);
            mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mExpandedView) {
            Settings.System.putInt(mResolver, Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW,
                    mExpandedView.isChecked() ? 1 : 0);
            mForceExpandedView.setEnabled(mExpandedView.isChecked());
        } else if (preference == mForceExpandedView) {
            Settings.System.putInt(mResolver, Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW,
                    mForceExpandedView.isChecked() ? 1 : 0);
        } else if (preference == mWakeOnNotification) {
            Settings.System.putInt(mResolver, Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION,
                    mWakeOnNotification.isChecked() ? 1 : 0);
        } else if (preference == mSunlightModePref) {
            value = mSunlightModePref.isChecked();
            Settings.System.putInt(mResolver,
                    Settings.System.ACTIVE_DISPLAY_SUNLIGHT_MODE,
                    value ? 1 : 0);
        } else if (preference == mTurnOffModePref) {
            value = mTurnOffModePref.isChecked();
            Settings.System.putInt(mResolver,
                    Settings.System.ACTIVE_DISPLAY_TURNOFF_MODE,
                    value ? 1 : 0);
        } else if (preference == mBypassPref) {
            value = mBypassPref.isChecked();
            Settings.System.putInt(mResolver,
                    Settings.System.ACTIVE_DISPLAY_BYPASS,
                    value ? 1 : 0);
        } else if (preference == mShowAmPmPref) {
            value = mShowAmPmPref.isChecked();
            Settings.System.putInt(mResolver,
                    Settings.System.ACTIVE_DISPLAY_SHOW_AMPM,
                    value ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(mContext);
    }
}
