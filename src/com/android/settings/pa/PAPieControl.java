package com.android.settings.pa;

import android.app.AlertDialog;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
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

import com.android.internal.util.slim.SlimActions;
import com.vanir.util.Helpers;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PAPieControl extends SettingsPreferenceFragment implements 
        CompoundButton.OnCheckedChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "PAPieControl";

    private static final String PA_PIE_GRAVITY = "pa_pie_gravity";
    private static final String PA_PIE_MODE = "pa_pie_mode";
    private static final String PA_PIE_SIZE = "pa_pie_size";
    private static final String PA_PIE_TRIGGER = "pa_pie_trigger";
    private static final String PA_PIE_ANGLE = "pa_pie_angle";
    private static final String PA_PIE_GAP = "pa_pie_gap";
    private static final String PA_PIE_POWER = "pa_pie_power";
    private static final String PA_PIE_MENU = "pa_pie_menu";
    private static final String PA_PIE_SEARCH = "pa_pie_search";
    private static final String PA_PIE_LASTAPP = "pa_pie_lastapp";
    private static final String PA_PIE_KILLTASK = "pa_pie_killtask";
    private static final String PA_PIE_SCREENSHOT = "pa_pie_screenshot";
    private static final String PA_PIE_ACTNOTIF = "pa_pie_actnotif";
    private static final String PA_PIE_ACTQS = "pa_pie_actqs";
    private static final String PA_PIE_CENTER = "pa_pie_center";
    private static final String PA_PIE_NOTIFICATIONS = "pa_pie_notifications";
    private static final String PA_PIE_RESTART = "pa_pie_restart_launcher";

    private ListPreference mPieMode;
    private ListPreference mPieSize;
    private ListPreference mPieGravity;
    private CheckBoxPreference mPieCenter;
    private ListPreference mPieTrigger;
    private ListPreference mPieAngle;
    private ListPreference mPieGap;
    private CheckBoxPreference mPieMenu;
    private CheckBoxPreference mPiePower;
    private CheckBoxPreference mPieSearch;
    private CheckBoxPreference mPieLastApp;
    private CheckBoxPreference mPieKillTask;
    private CheckBoxPreference mPieActNotif;
    private CheckBoxPreference mPieActQs;
    private CheckBoxPreference mPieScreenShot;
    private CheckBoxPreference mPieNotifi;
    private CheckBoxPreference mPieRestart;

    private Switch mEnabledSwitch;
    private boolean mEnabledPref;

    private ViewGroup mPrefsContainer;
    private View mDisabledText;

    private Context mContext;
    private int mAllowedLocations;

    protected Handler mHandler;
    private SettingsObserver mSettingsObserver;

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
                Settings.System.PIE_CONTROLS, 0) == 1);
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

        addPreferencesFromResource(R.xml.pa_pie_controls);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity().getApplicationContext();
        ContentResolver resolver = mContext.getContentResolver();

        mSettingsObserver = new SettingsObserver(new Handler());

        mPieCenter = (CheckBoxPreference) prefSet.findPreference(PA_PIE_CENTER);
        mPieCenter.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_CENTER, 1) == 1);

        mPieGravity = (ListPreference) prefSet.findPreference(PA_PIE_GRAVITY);
        int pieGravity = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GRAVITY, 2);
        mPieGravity.setValue(String.valueOf(pieGravity));
        mPieGravity.setOnPreferenceChangeListener(this);

        mPieMode = (ListPreference) prefSet.findPreference(PA_PIE_MODE);
        int pieMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MODE, 2);
        mPieMode.setValue(String.valueOf(pieMode));
        mPieMode.setOnPreferenceChangeListener(this);

        mPieSize = (ListPreference) prefSet.findPreference(PA_PIE_SIZE);
        mPieTrigger = (ListPreference) prefSet.findPreference(PA_PIE_TRIGGER);
        try {
            float pieSize = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.PIE_SIZE, 1.2f);
            mPieSize.setValue(String.valueOf(pieSize));
  
            float pieTrigger = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.PIE_TRIGGER);
            mPieTrigger.setValue(String.valueOf(pieTrigger));
        } catch(SettingNotFoundException ex) {
            // So what
        }

        mPieSize.setOnPreferenceChangeListener(this);
        mPieTrigger.setOnPreferenceChangeListener(this);

        mPieGap = (ListPreference) prefSet.findPreference(PA_PIE_GAP);
        int pieGap = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GAP, 3);
        mPieGap.setValue(String.valueOf(pieGap));
        mPieGap.setOnPreferenceChangeListener(this);

        mPieAngle = (ListPreference) prefSet.findPreference(PA_PIE_ANGLE);
        int pieAngle = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ANGLE, 12);
        mPieAngle.setValue(String.valueOf(pieAngle));
        mPieAngle.setOnPreferenceChangeListener(this);

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

        mPieNotifi = (CheckBoxPreference) prefSet.findPreference(PA_PIE_NOTIFICATIONS);
        mPieNotifi.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_NOTIFICATIONS, 0) == 1));

        mPieRestart = (CheckBoxPreference) prefSet.findPreference(PA_PIE_RESTART);
        mPieRestart.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.EXPANDED_DESKTOP_RESTART_LAUNCHER, 1) == 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateEnabledState();

        // If running on a phone, remove padding around container
        // and the preference listview
        if (!Utils.isTablet(getActivity())) {
            mPrefsContainer.setPadding(0, 0, 0, 0);
            getListView().setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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
        } else if (preference == mPieCenter) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_CENTER, mPieCenter.isChecked() ? 1 : 0);
        //} else if (preference == mPieStick) {
        // Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
        // Settings.System.PIE_STICK, mPieStick.isChecked() ? 1 : 0);
        } else if (preference == mPieNotifi) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.PIE_NOTIFICATIONS, mPieNotifi.isChecked() ? 1 : 0);
            //Helpers.restartSystemUI();
        } else if (preference == mPieRestart) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_DESKTOP_RESTART_LAUNCHER, mPieRestart.isChecked() ? 1 : 0);
            //Helpers.restartSystemUI();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieMode) {
            int pieMode = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_MODE, pieMode);
            return true;
        } else if (preference == mPieSize) {
            float pieSize = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_SIZE, pieSize);
            return true;
        } else if (preference == mPieGravity) {
            int pieGravity = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GRAVITY, pieGravity);
            return true;
        } else if (preference == mPieAngle) {
            int pieAngle = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_ANGLE, pieAngle);
            return true;
        } else if (preference == mPieGap) {
            int pieGap = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GAP, pieGap);
            return true;
        } else if (preference == mPieTrigger) {
            float pierigger = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_TRIGGER, pierigger);
            return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mEnabledSwitch) {

            boolean value = ((Boolean)isChecked).booleanValue();
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.PIE_CONTROLS,
                    value ? 1 : 0);           
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            observe();
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PIE_CONTROLS), false,
                    this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PIE_GRAVITY), false,
                    this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.NAVIGATION_BAR_SHOW), false,
                    this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            update();
            updateEnabledState();
        }

        void update() {
            ContentResolver resolver = mContext.getContentResolver();

            int pieOn = Settings.System.getInt(resolver,
                Settings.System.PIE_CONTROLS, 0);
            int navbarOn = Settings.System.getInt(resolver,
                Settings.System.NAVIGATION_BAR_SHOW, 1);
            int pieGravity = Settings.System.getInt(resolver,
                Settings.System.PIE_GRAVITY, 2);

            try {
                if (SlimActions.isNavBarDefault(getActivity())) {
                    if (SlimActions.isNavBarEnabled(getActivity()) && (pieOn == 1 && pieGravity == 3)) {
                            Settings.System.putInt(resolver,
                                Settings.System.NAVIGATION_BAR_SHOW, 0);
                    }
                    if (!SlimActions.isNavBarEnabled(getActivity()) && (pieGravity != 3 || pieOn == 0)) {
                        Settings.System.putInt(resolver,
                            Settings.System.NAVIGATION_BAR_SHOW, 1);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
            Settings.System.PIE_CONTROLS, 0) == 1;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }
}
