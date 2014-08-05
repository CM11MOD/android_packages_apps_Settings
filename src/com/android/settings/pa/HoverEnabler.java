/*
 * Copyright (C) 2014 The CyanogenMod Project
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

package com.android.settings.pa;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;

public class HoverEnabler implements CompoundButton.OnCheckedChangeListener {
    private final Context mContext;
    private Switch mSwitch;
    private boolean mStateMachineEvent;

    private ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            setSwitchState();
        }
    };

    public HoverEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
    }

    public void resume() {
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.HOVER_ENABLED),
                true, mSettingsObserver);
        mSwitch.setOnCheckedChangeListener(this);
        setSwitchState();
    }

    public void pause() {
        mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
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
        boolean enabled = Settings.System.getInt(
                mContext.getContentResolver(),
                Settings.System.HOVER_ENABLED, 0) == 1;
        if (enabled) {
            Settings.System.putInt(
                mContext.getContentResolver(), Settings.System.HEADS_UP_NOTIFICATION, 0);
        }
        mStateMachineEvent = true;
        mSwitch.setChecked(enabled);
        mStateMachineEvent = false;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mStateMachineEvent) {
            return;
        }
        // Handle a switch change
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.HOVER_ENABLED, isChecked ? 1 : 0);

    }

}
