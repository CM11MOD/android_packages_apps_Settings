package com.android.settings.pa;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PieTargets extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PA_PIE_POWER = "pa_pie_power";
    private static final String PA_PIE_MENU = "pa_pie_menu";
    private static final String PA_PIE_SEARCH = "pa_pie_search";
    private static final String PA_PIE_LASTAPP = "pa_pie_lastapp";
    private static final String PA_PIE_KILLTASK = "pa_pie_killtask";
    private static final String PA_PIE_SCREENSHOT = "pa_pie_screenshot";
    private static final String PA_PIE_ACTNOTIF = "pa_pie_actnotif";
    private static final String PA_PIE_ACTQS = "pa_pie_actqs";
    private static final String PA_PIE_TORCH = "pa_pie_torch";

    private CheckBoxPreference mPieMenu;
    private CheckBoxPreference mPiePower;
    private CheckBoxPreference mPieSearch;
    private CheckBoxPreference mPieLastApp;
    private CheckBoxPreference mPieKillTask;
    private CheckBoxPreference mPieActNotif;
    private CheckBoxPreference mPieActQs;
    private CheckBoxPreference mPieScreenShot;
    private CheckBoxPreference mPieTorch;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pa_pie_targets);

        PreferenceScreen prefSet = getPreferenceScreen();

        Context context = getActivity();
        mResolver = context.getContentResolver();

        mPieMenu = (CheckBoxPreference) prefSet.findPreference(PA_PIE_MENU);
        mPieMenu.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MENU, 1) == 1);

        mPieSearch = (CheckBoxPreference) prefSet.findPreference(PA_PIE_SEARCH);
        mPieSearch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_SEARCH, 1) == 1);

        mPiePower = (CheckBoxPreference) prefSet.findPreference(PA_PIE_POWER);
        mPiePower.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.PIE_POWER, 0) == 1);

        mPieLastApp = (CheckBoxPreference) prefSet.findPreference(PA_PIE_LASTAPP);
        mPieLastApp.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_LAST_APP, 0) == 1);

        mPieKillTask = (CheckBoxPreference) prefSet.findPreference(PA_PIE_KILLTASK);
        mPieKillTask.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_KILL_TASK, 0) == 1);

        mPieScreenShot = (CheckBoxPreference) prefSet.findPreference(PA_PIE_SCREENSHOT);
        mPieScreenShot.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_SCREENSHOT, 0) == 1);

        mPieActNotif = (CheckBoxPreference) prefSet.findPreference(PA_PIE_ACTNOTIF);
        mPieActNotif.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ACT_NOTIF, 0) == 1);

        mPieActQs = (CheckBoxPreference) prefSet.findPreference(PA_PIE_ACTQS);
        mPieActQs.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ACT_QS, 0) == 1);

        mPieTorch = (CheckBoxPreference) prefSet.findPreference(PA_PIE_TORCH);
        mPieTorch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_TORCH, 0) == 1);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPieMenu) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_MENU,
                    mPieMenu.isChecked() ? 1 : 0);
        } else if (preference == mPieSearch) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_SEARCH,
                    mPieSearch.isChecked() ? 1 : 0);
        } else if (preference == mPiePower) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_POWER,
                    mPiePower.isChecked() ? 1 : 0);
        } else if (preference == mPieLastApp) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_LAST_APP,
                    mPieLastApp.isChecked() ? 1 : 0);
        } else if (preference == mPieKillTask) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_KILL_TASK, mPieKillTask.isChecked() ? 1 : 0);
        } else if (preference == mPieScreenShot) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_SCREENSHOT, mPieScreenShot.isChecked() ? 1 : 0);
        } else if (preference == mPieActNotif) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_ACT_NOTIF, mPieActNotif.isChecked() ? 1 : 0);
        } else if (preference == mPieActQs) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_ACT_QS, mPieActQs.isChecked() ? 1 : 0);
        } else if (preference == mPieTorch) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_TORCH, mPieTorch.isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
