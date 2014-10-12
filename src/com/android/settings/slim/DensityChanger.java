/*
 * Copyright (C) 2013 SlimRoms
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
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.slim.util.CMDProcessor;
import com.android.settings.slim.util.CMDProcessor.CommandResult;
import com.android.settings.util.Helpers;

public class DensityChanger extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "DensityChanger";

    private static final String PROP_DISPLAY_DENSITY = "persist.sf.lcd_density";
    private static final String KEY_DISPLAY_DENSITY = "display_density";

    private EditTextPreference mDisplayDensity;
    private Preference mReboot;
    private Preference mClearMarketData;

    private static final int MSG_DATA_CLEARED = 500;

    protected Context mContext;

    int newDensityValue;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DATA_CLEARED:
                    mClearMarketData.setSummary(R.string.clear_market_data_cleared);
                    break;
            }

        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.density_changer);

        mDisplayDensity = (EditTextPreference) findPreference(KEY_DISPLAY_DENSITY);
        mDisplayDensity.setText(SystemProperties.get(PROP_DISPLAY_DENSITY, "0"));
        mDisplayDensity.setOnPreferenceChangeListener(this);

        mReboot = findPreference("reboot");
        mClearMarketData = findPreference("clear_market_data");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mReboot) {
            PowerManager pm = (PowerManager) getActivity()
                    .getSystemService(Context.POWER_SERVICE);
            pm.reboot("Resetting density");
            return true;

        } else if (preference == mClearMarketData) {

            new ClearMarketDataTask().execute("");
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_DISPLAY_DENSITY.equals(key)) {
            final int max = getResources().getInteger(R.integer.display_density_max);
            final int min = getResources().getInteger(R.integer.display_density_min);

            int value = SystemProperties.getInt(PROP_DISPLAY_DENSITY, 0);
            try {
                value = Integer.parseInt((String) objValue);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid input", e);
            }

            // 0 disables the custom density, so do not check for the value, else…
            if (value != 0) {
                // …cap the value
                if (value < min) {
                    value = min;
                } else if (value > max) {
                    value = max;
                }
            }

            setLcdDensity(Integer.parseInt(String.valueOf(value)));
            mDisplayDensity.setText(String.valueOf(value));

            // we handle it, return false
            return false;
        }
        return true;
    }

    private void setLcdDensity(int newDensity) {
        Helpers.getMount("rw");
        new CMDProcessor().su.runWaitFor("busybox sed -i 's|persist.sf.lcd_density=.*|"
                + "persist.sf.lcd_density" + "=" + newDensity + "|' " + "/system/build.prop");
        Helpers.getMount("ro");
    }

    class ClearUserDataObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            mHandler.sendEmptyMessage(MSG_DATA_CLEARED);
        }
    }

    private class ClearMarketDataTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... stuff) {
            String vending = "/data/data/com.android.vending/";
            String gms = "/data/data/com.google.android.gms/";
            String gsf = "/data/data/com.google.android.gsf/";

            CommandResult cr = new CMDProcessor().su.runWaitFor("ls " + vending);
            CommandResult cr_gms = new CMDProcessor().su.runWaitFor("ls " + gms);
            CommandResult cr_gsf = new CMDProcessor().su.runWaitFor("ls " + gsf);

            if (cr.stdout == null || cr_gms.stdout == null || cr_gsf.stdout == null)
                return false;

            for (String dir : cr.stdout.split("\n")) {
                if (!dir.equals("lib")) {
                    String c = "rm -r " + vending + dir;
                    // Log.i(TAG, c);
                    if (!new CMDProcessor().su.runWaitFor(c).success())
                        return false;
                }
            }

            for (String dir_gms : cr_gms.stdout.split("\n")) {
                if (!dir_gms.equals("lib")) {
                    String c_gms = "rm -r " + gms + dir_gms;
                    // Log.i(TAG, c);
                    if (!new CMDProcessor().su.runWaitFor(c_gms).success())
                        return false;
                }
            }

            for (String dir_gsf : cr_gsf.stdout.split("\n")) {
                if (!dir_gsf.equals("lib")) {
                    String c_gsf = "rm -r " + gsf + dir_gsf;
                    // Log.i(TAG, c);
                    if (!new CMDProcessor().su.runWaitFor(c_gsf).success())
                        return false;
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            mClearMarketData.setSummary(result ? getResources().getString(R.string.clear_market_data_cleared)
                    : getResources().getString(R.string.clear_market_data_donot_cleared));
        }
    }
}
