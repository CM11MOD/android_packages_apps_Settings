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

package com.android.settings.slim;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

public class QuietHoursEnabler implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "QuietHoursEnabler";
    private final Context mContext;
    private Switch mSwitch;
    private boolean mStateMachineEvent;

    public QuietHoursEnabler(Context context, Switch switch_) {
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
        int mode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUIET_HOURS_ENABLED, 0);
        mStateMachineEvent = true;
        mSwitch.setChecked(mode != 0);
        mStateMachineEvent = false;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mStateMachineEvent) {
            return;
        }
        // Handle a switch change

        int mode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUIET_HOURS_STATE, 1);

        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.QUIET_HOURS_ENABLED, isChecked ? mode : 0);
     }
}
