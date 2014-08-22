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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.view.WindowManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import com.android.internal.widget.LockPatternUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.vanir.AppMultiSelectListPreference;
import com.android.settings.vanir.NumberPickerPreference;
import com.android.settings.widget.SeekBarPreference2;
import com.android.internal.util.slim.DeviceUtils;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class ActiveNotificationSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "ActiveDisplaySettings";

    // common settings
    private static final String KEY_POCKET_MODE = "pocket_mode";
    private static final String KEY_DISMISS_ALL = "dismiss_all";
    private static final String KEY_HIDE_LOW_PRIORITY = "hide_low_priority";
    private static final String KEY_HIDE_NON_CLEARABLE = "hide_non_clearable";
    private static final String KEY_QUIET_HOURS = "quiet_hours";

    // active display
    private static final String KEY_ANNOYING = "ad_annoying";
    private static final String KEY_BRIGHTNESS = "ad_brightness";
    private static final String KEY_BYPASS_CONTENT = "ad_bypass";
    private static final String KEY_REDISPLAY = "ad_redisplay";
    private static final String KEY_SHAKE_EVENT = "active_display_shake_event";
    private static final String KEY_SHAKE_THRESHOLD = "ad_shake_threshold";
    private static final String KEY_SHAKE_LONGTHRESHOLD = "ad_shake_long_threshold";
    private static final String KEY_SHAKE_TIMEOUT = "ad_shake_timeout";
    private static final String KEY_SHOW_AMPM = "ad_show_ampm";
    private static final String KEY_SUNLIGHT_MODE = "ad_sunlight_mode";
    private static final String KEY_TIMEOUT = "ad_timeout";
    private static final String KEY_THRESHOLD = "ad_threshold";
    private static final String KEY_TURNOFF_MODE = "ad_turnoff_mode";

    // lockscreen notification
    private static final String KEY_OFFSET_TOP = "offset_top";
    private static final String KEY_EXPANDED_VIEW = "expanded_view";
    private static final String KEY_FORCE_EXPANDED_VIEW = "force_expanded_view";
    private static final String KEY_NOTIFICATIONS_HEIGHT = "notifications_height";
    private static final String KEY_WAKE_ON_NOTIFICATION = "wake_on_notification";
    private static final String KEY_NOTIFICATION_COLOR = "notification_color";

    //peek
    private static final String KEY_PEEK_PICKUP_TIMEOUT = "peek_pickup_timeout";
    private static final String KEY_PEEK_WAKE_TIMEOUT = "peek_wake_timeout";

    private CheckBoxPreference mBypassPref;
    private CheckBoxPreference mDismissAll;
    private CheckBoxPreference mExpandedView;
    private CheckBoxPreference mForceExpandedView;
    private CheckBoxPreference mHideLowPriority;
    private CheckBoxPreference mHideNonClearable;
    private CheckBoxPreference mShowAmPmPref;
    private CheckBoxPreference mSunlightModePref;
    private CheckBoxPreference mTurnOffModePref;
    private CheckBoxPreference mWakeOnNotification;

    private ColorPickerPreference mNotificationColor;

    private ListPreference mPocketModePref;
    private ListPreference mDisplayTimeout;
    private ListPreference mProximityThreshold;
    private ListPreference mPeekPickupTimeout;
    private ListPreference mPeekWakeTimeout;
    private ListPreference mRedisplayPref;

    private NumberPickerPreference mNotificationsHeight;

    private SeekBarPreference2 mAnnoyingNotification;
    private SeekBarPreference2 mBrightnessLevel;
    private SeekBarPreference mOffsetTop;
    private SeekBarPreference2 mShakeThreshold;
    private SeekBarPreference2 mShakeLongThreshold;
    private SeekBarPreference2 mShakeTimeout;

    private int mMinimumBacklight;
    private int mMaximumBacklight;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.active_notification_settings);
        mContext = getActivity().getApplicationContext();
        root = getPreferenceScreen();
        PreferenceScreen prefs = getPreferenceScreen();

        final ContentResolver cr = getActivity().getContentResolver();
        
        int view = Settings.System.getInt(cr,
                            Settings.System.ACTIVE_NOTIFICATIONS_MODE, 0);
 
        int resid = 0;
        switch (view) {
            case 0:
                resid = R.xml.active_notifications_off;
                break;
            case 1:
                resid = R.xml.active_notifications_ad;
                break;
            case 2:
                resid = R.xml.active_notifications_ln;
                break;
            case 3:
                resid = R.xml.active_notifications_peek;
            break;
        }
        addPreferencesFromResource(resid);

        mPocketModePref = (ListPreference) root.findPreference(KEY_POCKET_MODE);
        if (mPocketModePref != null) {
            if (!DeviceUtils.deviceSupportsProximitySensor(mContext)) {
                root.removePreference(mPocketModePref);
            } else {
                int mode = Settings.System.getInt(cr,
                        Settings.System.ACTIVE_NOTIFICATIONS_POCKET_MODE, 0);
                mPocketModePref.setValue(String.valueOf(mode));
                updatePocketModeSummary(mode);
                mPocketModePref.setOnPreferenceChangeListener(this);
            }
        }

        mDismissAll = (CheckBoxPreference) root.findPreference(KEY_DISMISS_ALL);
        if (mDismissAll != null) {
            mDismissAll.setChecked(Settings.System.getInt(cr,
                        Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_ALL, 1) == 1);
        }

        mHideLowPriority = (CheckBoxPreference) root.findPreference(KEY_HIDE_LOW_PRIORITY);
        if (mHideLowPriority != null) {
            mHideLowPriority.setChecked(Settings.System.getInt(cr,
                    Settings.System.ACTIVE_NOTIFICATIONS_HIDE_LOW_PRIORITY, 0) == 1);
        }

        mHideNonClearable = (CheckBoxPreference) root.findPreference(KEY_HIDE_NON_CLEARABLE);
        if (mHideNonClearable != null) {
            mHideNonClearable.setChecked(Settings.System.getInt(cr,
                        Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_NON_CLEARABLE, 0) == 1);
        }

        mBypassPref = (CheckBoxPreference) root.findPreference(KEY_BYPASS_CONTENT);
        mProximityThreshold = (ListPreference) root.findPreference(KEY_THRESHOLD);
        mTurnOffModePref = (CheckBoxPreference) findPreference(KEY_TURNOFF_MODE);

        if (!DeviceUtils.deviceSupportsProximitySensor(mContext)) {
            if (mBypassPref != null) {
                root.removePreference(mBypassPref);
            }
            if (mProximityThreshold != null) {
                root.removePreference(mProximityThreshold);
            }
            if (mTurnOffModePref != null) {
                root.removePreference(mTurnOffModePref);
            }
        } else {
            if (mBypassPref != null) {
                mBypassPref.setChecked((Settings.System.getInt(cr,
                    Settings.System.ACTIVE_DISPLAY_BYPASS, 1) != 0));
            }
            if (mProximityThreshold != null) {
                long threshold = Settings.System.getLong(cr,
                        Settings.System.ACTIVE_DISPLAY_THRESHOLD, 5000L);
                mProximityThreshold.setValue(String.valueOf(threshold));
                mProximityThreshold.setSummary(mProximityThreshold.getEntry());
                mProximityThreshold.setOnPreferenceChangeListener(this);
            }
            if (mTurnOffModePref != null) {
                mTurnOffModePref.setChecked((Settings.System.getInt(cr,
                        Settings.System.ACTIVE_DISPLAY_TURNOFF_MODE, 0) == 1));
            }
        }

        if (mShowAmPmPref != null) {
            mShowAmPmPref = (CheckBoxPreference) root.findPreference(KEY_SHOW_AMPM);
            mShowAmPmPref.setChecked((Settings.System.getInt(cr,
                    Settings.System.ACTIVE_DISPLAY_SHOW_AMPM, 0) == 1));
            mShowAmPmPref.setEnabled(!is24Hour());
        }

        mSunlightModePref = (CheckBoxPreference) root.findPreference(KEY_SUNLIGHT_MODE);
        if (mSunlightModePref != null) {
            if (!DeviceUtils.deviceSupportsLightSensor(mContext)) {
                root.removePreference(mSunlightModePref);
            } else {
                mSunlightModePref.setChecked((Settings.System.getInt(cr,
                        Settings.System.ACTIVE_DISPLAY_SUNLIGHT_MODE, 0) == 1));
            }
        }

        mRedisplayPref = (ListPreference) root.findPreference(KEY_REDISPLAY);
        if (mRedisplayPref != null) {
            long timeout = Settings.System.getLong(cr,
                    Settings.System.ACTIVE_DISPLAY_REDISPLAY, 0);
            mRedisplayPref.setValue(String.valueOf(timeout));
            mRedisplayPref.setSummary(mRedisplayPref.getEntry());
            mRedisplayPref.setOnPreferenceChangeListener(this);
        }

        mAnnoyingNotification = (SeekBarPreference2) root.findPreference(KEY_ANNOYING);
        if (mAnnoyingNotification != null) {
            mAnnoyingNotification.setValue(Settings.System.getInt(cr,
                    Settings.System.ACTIVE_DISPLAY_ANNOYING, 0));
            mAnnoyingNotification.setOnPreferenceChangeListener(this);
        }
    
        mShakeThreshold = (SeekBarPreference2) root.findPreference(KEY_SHAKE_THRESHOLD);
        if (mShakeThreshold != null) {
            mShakeThreshold.setValue(Settings.System.getInt(cr,
                    Settings.System.ACTIVE_DISPLAY_SHAKE_THRESHOLD, 10));
            mShakeThreshold.setOnPreferenceChangeListener(this);
        }

        mShakeLongThreshold = (SeekBarPreference2) root.findPreference(KEY_SHAKE_LONGTHRESHOLD);
        if (mShakeLongThreshold != null) {
            mShakeLongThreshold.setValue(Settings.System.getInt(cr,
                    Settings.System.ACTIVE_DISPLAY_SHAKE_LONGTHRESHOLD, 2));
            mShakeLongThreshold.setOnPreferenceChangeListener(this);
        }

        mShakeTimeout = (SeekBarPreference2) root.findPreference(KEY_SHAKE_TIMEOUT);
        if (mShakeTimeout != null) {
            mShakeTimeout.setValue(Settings.System.getInt(cr,
                    Settings.System.ACTIVE_DISPLAY_SHAKE_TIMEOUT, 3));
            mShakeTimeout.setOnPreferenceChangeListener(this);
        }

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mMinimumBacklight = pm.getMinimumScreenBrightnessSetting();
        mMaximumBacklight = pm.getMaximumScreenBrightnessSetting();

        mBrightnessLevel = (SeekBarPreference2) root.findPreference(KEY_BRIGHTNESS);
        if (mBrightnessLevel != null) {
            int brightness = Settings.System.getInt(cr,
                    Settings.System.ACTIVE_DISPLAY_BRIGHTNESS, mMaximumBacklight);
            int realBrightness =  (int)(((float)brightness / (float)mMaximumBacklight) * 100);
            mBrightnessLevel.setValue(realBrightness);
            mBrightnessLevel.setOnPreferenceChangeListener(this);

            try {
                if (Settings.System.getInt(cr,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    mBrightnessLevel.setEnabled(false);
                    mBrightnessLevel.setSummary(R.string.ad_autobrightness_mode_on);
                }
                } catch (SettingNotFoundException e) {
            }
        }

        mDisplayTimeout = (ListPreference) root.findPreference(KEY_TIMEOUT);
        if (mDisplayTimeout != null) {
            long timeout = Settings.System.getLong(cr,
                    Settings.System.ACTIVE_DISPLAY_TIMEOUT, 8000L);
            mDisplayTimeout.setValue(String.valueOf(timeout));
            mDisplayTimeout.setSummary(mDisplayTimeout.getEntry());
            mDisplayTimeout.setOnPreferenceChangeListener(this);
        }

        boolean ad_enabled = Settings.System.getInt(cr,
                Settings.System.ACTIVE_NOTIFICATIONS_MODE, 0) == 1;

        mWakeOnNotification = (CheckBoxPreference) root.findPreference(KEY_WAKE_ON_NOTIFICATION);
        if (mWakeOnNotification != null) {
            mWakeOnNotification.setChecked(Settings.System.getInt(cr,
            Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION, 0) == 1);

            if (ad_enabled) {
                mWakeOnNotification.setEnabled(false);
                mWakeOnNotification.setSummary(R.string.wake_on_notification_disable);
            } else {
                mWakeOnNotification.setEnabled(true);
                mWakeOnNotification.setSummary(R.string.wake_on_notification_summary);
            }
        }

        mOffsetTop = (SeekBarPreference) root.findPreference(KEY_OFFSET_TOP);
        if (mOffsetTop != null) {
            mOffsetTop.setProgress((int)(Settings.System.getFloat(cr,
                        Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP, 0.3f) * 100));
            mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + mOffsetTop.getProgress() + "%");
            mOffsetTop.setOnPreferenceChangeListener(this);
        }

        mExpandedView = (CheckBoxPreference) root.findPreference(KEY_EXPANDED_VIEW);
        if (mExpandedView != null) {
            mExpandedView.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW, 1) == 1);
            mExpandedView.setOnPreferenceChangeListener(this);
        }

        mForceExpandedView = (CheckBoxPreference) root.findPreference(KEY_FORCE_EXPANDED_VIEW);
        if (mForceExpandedView != null) {
            mForceExpandedView.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW, 0) == 1);
            mForceExpandedView.setOnPreferenceChangeListener(this);
        }

        mNotificationsHeight = (NumberPickerPreference) root.findPreference(KEY_NOTIFICATIONS_HEIGHT);
        if (mNotificationsHeight != null) {
            mNotificationsHeight.setValue(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, 4));

            Point displaySize = new Point();
            ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
            int max = Math.round((float)displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                    (float)mContext.getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
            mNotificationsHeight.setMinValue(1);
            mNotificationsHeight.setMaxValue(max);
            mNotificationsHeight.setOnPreferenceChangeListener(this);
        }

        mNotificationColor = (ColorPickerPreference) root.findPreference(KEY_NOTIFICATION_COLOR);
        if (mNotificationColor != null) {
            mNotificationColor.setAlphaSliderEnabled(true);
            int color = Settings.System.getInt(cr,
            Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, 0x55555555);
            String hexColor = String.format("#%08x", (0xffffffff & color));
            mNotificationColor.setSummary(hexColor);
            mNotificationColor.setDefaultValue(color);
            mNotificationColor.setNewPreviewColor(color);
            mNotificationColor.setOnPreferenceChangeListener(this);
        }

        mPeekPickupTimeout = (ListPreference) root.findPreference(KEY_PEEK_PICKUP_TIMEOUT);
        if (mPeekPickupTimeout != null) {
            int peekPickupTimeout = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.PEEK_PICKUP_TIMEOUT, 10000, UserHandle.USER_CURRENT);
            mPeekPickupTimeout.setValue(String.valueOf(peekPickupTimeout));
            mPeekPickupTimeout.setSummary(mPeekPickupTimeout.getEntry());
            mPeekPickupTimeout.setOnPreferenceChangeListener(this);
        }

        mPeekWakeTimeout = (ListPreference) root.findPreference(KEY_PEEK_WAKE_TIMEOUT);
        if (mPeekWakeTimeout != null) {
            int peekWakeTimeout = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.PEEK_WAKE_TIMEOUT, 5000, UserHandle.USER_CURRENT);
            mPeekWakeTimeout.setValue(String.valueOf(peekWakeTimeout));
            mPeekWakeTimeout.setSummary(mPeekWakeTimeout.getEntry());
            mPeekWakeTimeout.setOnPreferenceChangeListener(this);
        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        createPreferenceHierarchy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private boolean isKeyguardSecure() {
        LockPatternUtils mLockPatternUtils = new LockPatternUtils(getActivity());
        boolean isSecure = mLockPatternUtils.isSecure();
        return isSecure;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver cr = getActivity().getContentResolver();
        boolean value;
        if (preference == mPocketModePref) {
            int mode = Integer.valueOf((String) newValue);
            updatePocketModeSummary(mode);
            return true;
        } else if (preference == mHideLowPriority) {
            value = (Boolean) newValue;
            Settings.System.putInt(cr,
                    Settings.System.ACTIVE_NOTIFICATIONS_HIDE_LOW_PRIORITY,
                    value ? 1 : 0);
			return true;
        } else if (preference == mHideNonClearable) {
            value = (Boolean) newValue;
            Settings.System.putInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_NON_CLEARABLE,
                    value ? 1 : 0);
            mDismissAll.setEnabled(!mHideNonClearable.isChecked());
			return true;
        } else if (preference == mDismissAll) {
            value = (Boolean) newValue;
            Settings.System.putInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_ALL,
                    value ? 1 : 0);
			return true;
        } else if (preference == mExpandedView) {
            value = (Boolean) newValue;
            Settings.System.putInt(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW,
                value ? 1 : 0);
            mForceExpandedView.setEnabled(mExpandedView.isChecked());
			return true;
        } else if (preference == mForceExpandedView) {
            value = (Boolean) newValue;
            Settings.System.putInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW,
                    value ? 1 : 0);
			return true;
        } else if (preference == mWakeOnNotification) {
            value = (Boolean) newValue;
            Settings.System.putInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION,
                    value ? 1 : 0);
			return true;
        } else if (preference == mSunlightModePref) {
            value = mSunlightModePref.isChecked();
            Settings.System.putInt(cr,
                    Settings.System.ACTIVE_DISPLAY_SUNLIGHT_MODE,
                    value ? 1 : 0);
			return true;
        } else if (preference == mTurnOffModePref) {
            value = mTurnOffModePref.isChecked();
            Settings.System.putInt(cr,
                    Settings.System.ACTIVE_DISPLAY_TURNOFF_MODE,
                    value ? 1 : 0);
			return true;
        } else if (preference == mBypassPref) {
            value = mBypassPref.isChecked();
            Settings.System.putInt(cr,
                    Settings.System.ACTIVE_DISPLAY_BYPASS,
                    value ? 1 : 0);
			return true;
        } else if (preference == mShowAmPmPref) {
            value = mShowAmPmPref.isChecked();
            Settings.System.putInt(cr,
                    Settings.System.ACTIVE_DISPLAY_SHOW_AMPM,
                    value ? 1 : 0);
			return true;
        } else if (preference == mRedisplayPref) {
            int val = Integer.parseInt((String) newValue);
            int index = mRedisplayPref.findIndexOfValue((String) newValue);
            Settings.System.putInt(cr,
                Settings.System.ACTIVE_DISPLAY_REDISPLAY, val);
            mRedisplayPref.setSummary(mRedisplayPref.getEntries()[index]);
            return true;
        } else if (preference == mAnnoyingNotification) {
            int annoying = ((Integer)newValue).intValue();
            Settings.System.putInt(cr,
                    Settings.System.ACTIVE_DISPLAY_ANNOYING, annoying);
            return true;
        } else if (preference == mShakeThreshold) {
            int threshold = ((Integer)newValue).intValue();
            Settings.System.putInt(cr,
                    Settings.System.ACTIVE_DISPLAY_SHAKE_THRESHOLD, threshold);
            return true;
        } else if (preference == mShakeLongThreshold) {
            long longThreshold = (long)(1000 * ((Integer)newValue).intValue());
            Settings.System.putLong(cr,
                    Settings.System.ACTIVE_DISPLAY_SHAKE_LONGTHRESHOLD, longThreshold);
            return true;
        } else if (preference == mShakeTimeout) {
            int timeout = ((Integer)newValue).intValue();
            Settings.System.putInt(cr,
            Settings.System.ACTIVE_DISPLAY_SHAKE_TIMEOUT, timeout);
            return true;
        } else if (preference == mBrightnessLevel) {
            int brightness = ((Integer)newValue).intValue();
            int realBrightness =  Math.max(mMinimumBacklight, (int)(((float)brightness / (float)100) * mMaximumBacklight));                   
            Settings.System.putInt(cr, Settings.System.ACTIVE_DISPLAY_BRIGHTNESS, realBrightness);
            return true;
        } else if (preference == mNotificationsHeight) {
            Settings.System.putInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, (Integer)newValue);
            return true;
        } else if (preference == mDisplayTimeout) {
            int val = Integer.parseInt((String) newValue);
            int index = mDisplayTimeout.findIndexOfValue((String) newValue);
            Settings.System.putInt(cr,
                Settings.System.ACTIVE_DISPLAY_TIMEOUT, val);
            mDisplayTimeout.setSummary(mDisplayTimeout.getEntries()[index]);
            return true;
        } else if (preference == mProximityThreshold) {
            int val = Integer.parseInt((String) newValue);
            int index = mProximityThreshold.findIndexOfValue((String) newValue);
            Settings.System.putInt(cr,
                Settings.System.ACTIVE_DISPLAY_THRESHOLD, val);
            mProximityThreshold.setSummary(mProximityThreshold.getEntries()[index]);
            return true;
        } else if (preference == mNotificationColor) {
            String hex = ColorPickerPreference.convertToARGB(
            Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(cr,
            Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, intHex);
            return true;
        } else if (preference == mOffsetTop) {
            Settings.System.putFloat(cr,
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
        } else if (preference == mPeekWakeTimeout) {
            int index = mPeekWakeTimeout.findIndexOfValue((String) newValue);
            int peekWakeTimeout = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.PEEK_WAKE_TIMEOUT,
                    peekWakeTimeout, UserHandle.USER_CURRENT);
            mPeekWakeTimeout.setSummary(mPeekWakeTimeout.getEntries()[index]);
            return true;
        } else {
            return false;
        }
    }

    private void updatePocketModeSummary(int value) {
        mPocketModePref.setSummary(
                mPocketModePref.getEntries()[mPocketModePref.findIndexOfValue("" + value)]);
        Settings.System.putInt(getContentResolver(),
                Settings.System.ACTIVE_NOTIFICATIONS_POCKET_MODE, value);
    }

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(mContext);
    }
}
