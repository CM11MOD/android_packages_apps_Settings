/*
 * Copyright (C) 2014 The OmniROM Project
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

package com.android.settings.omni;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.settings.omni.batterysaver.BatterySaverHelper;

public class BatterySaverEnabler implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "BatterySaverEnabler";
    private final Context mContext;
    private Switch mSwitch;
    private boolean mStateMachineEvent;

    public BatterySaverEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
    }

    public void resume() {
        mSwitch.setOnCheckedChangeListener(this);
        setSwitchState();
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) return;
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
        setSwitchState();
    }

    private void setSwitchState() {
        boolean enabled = Settings.Global.getInt(
                mContext.getContentResolver(),
                        Settings.Global.BATTERY_SAVER_OPTION, 0) != 0;
        mStateMachineEvent = true;
        mSwitch.setChecked(enabled);
        mStateMachineEvent = false;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean value) {
        if (mStateMachineEvent) {
            return;
        }
        // Handle a switch change
        Settings.Global.getInt(mContext.getContentResolver(),
                     Settings.Global.BATTERY_SAVER_OPTION, value ? 1 : 0);

        BatterySaverHelper.setBatterySaverActive(mContext, value ? 1 : 0);
        BatterySaverHelper.scheduleService(mContext);
    }
}
