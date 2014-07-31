
package com.android.settings.slim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.util.CMDProcessor;
import com.android.settings.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;


public class UserInterface extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "UserInterface";

    private static final String RECENTS_STYLE = "recents_style";
    private static final String RECENT_MENU_CLEAR_ALL = "recent_menu_clear_all";
    private static final String RECENT_MENU_CLEAR_ALL_LOCATION = "recent_menu_clear_all_location";
    private static final String KEY_RECENTS_RAM_BAR = "recents_ram_bar";
    private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
    private static final String KEY_REVERSE_DEFAULT_APP_PICKER = "reverse_default_app_picker";
    private static final String RECENT_PANEL_LEFTY_MODE = "recent_panel_lefty_mode";
    private static final String RECENT_PANEL_SHOW_TOPMOST = "recent_panel_show_topmost";
    private static final String RECENT_PANEL_SCALE = "recent_panel_scale";
    private static final String RECENT_PANEL_EXPANDED_MODE = "recent_panel_expanded_mode";
    private static final String RECENT_PANEL_BG_COLOR =	"recent_panel_bg_color";
    private static final String FORCE_MULTI_PANE = "force_multi_pane";
    private static final String BUBBLE_MODE = "bubble_mode";
    private static final String PREF_RECENTS_SWIPE_FLOATING = "recents_swipe";

    public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";

    public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN)
			.setClassName(OMNISWITCH_PACKAGE_NAME, OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");

    private ListPreference mRecentStyle;
    private CheckBoxPreference mRecentClearAll;
    private ListPreference mRecentClearAllPosition;
    private CheckBoxPreference mUseAltResolver;
    private CheckBoxPreference mReverseDefaultAppPicker;
    private CheckBoxPreference mRecentPanelLeftyMode;
    private CheckBoxPreference mRecentsShowTopmost;
    private ListPreference mRecentPanelScale;
    private ListPreference mRecentPanelExpandedMode;
    private ColorPickerPreference mRecentPanelBgColor;
    private ListPreference mBubbleMode;
    private CheckBoxPreference mMultiPane;
    private CheckBoxPreference mRecentsSwipe;

    private Preference mRamBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.user_interface_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mRamBar = findPreference(KEY_RECENTS_RAM_BAR);
        updateRamBar();

        mRecentStyle = (ListPreference) prefSet.findPreference(RECENTS_STYLE);
        mRecentStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.RECENTS_STYLE,
                0)));
        mRecentStyle.setSummary(mRecentStyle.getEntry());
        mRecentStyle.setOnPreferenceChangeListener(this);

        mRecentClearAll = (CheckBoxPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL);
        mRecentClearAll.setChecked(Settings.System.getInt(resolver,
                Settings.System.SHOW_CLEAR_RECENTS_BUTTON, 1) == 1);
        mRecentClearAll.setOnPreferenceChangeListener(this);

        mRecentClearAllPosition = (ListPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL_LOCATION);
        String recentClearAllPosition = Settings.System.getString(resolver,
                Settings.System.CLEAR_RECENTS_BUTTON_LOCATION);
        if (recentClearAllPosition != null) {
            mRecentClearAllPosition.setValue(recentClearAllPosition);
        }
        mRecentClearAllPosition.setOnPreferenceChangeListener(this);

        mRecentPanelLeftyMode = (CheckBoxPreference) findPreference(RECENT_PANEL_LEFTY_MODE);
        mRecentPanelLeftyMode.setOnPreferenceChangeListener(this);
        final boolean recentLeftyMode = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_GRAVITY, Gravity.RIGHT) == Gravity.LEFT;
        mRecentPanelLeftyMode.setChecked(recentLeftyMode);

        mRecentsShowTopmost = (CheckBoxPreference) findPreference(RECENT_PANEL_SHOW_TOPMOST);
        mRecentsShowTopmost.setChecked(Settings.System.getInt(resolver,
                Settings.System.RECENT_PANEL_SHOW_TOPMOST, 0) == 1);
        mRecentsShowTopmost.setOnPreferenceChangeListener(this);

        mMultiPane = (CheckBoxPreference) prefSet.findPreference(FORCE_MULTI_PANE);
        mMultiPane.setOnPreferenceChangeListener(this);
        mMultiPane.setChecked(Settings.System.getInt(resolver,
                Settings.System.FORCE_MULTI_PANE, 0) == 1);

        mRecentPanelScale = (ListPreference) findPreference(RECENT_PANEL_SCALE);
        mRecentPanelScale.setOnPreferenceChangeListener(this);
        final int recentScale = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_SCALE_FACTOR, 100);
        mRecentPanelScale.setValue(recentScale + "");

        mRecentPanelExpandedMode = (ListPreference) findPreference(RECENT_PANEL_EXPANDED_MODE);
        mRecentPanelExpandedMode.setOnPreferenceChangeListener(this);
        final int recentExpandedMode = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_EXPANDED_MODE, 0);
        mRecentPanelExpandedMode.setValue(recentExpandedMode + "");

        // Recent panel background color
        mRecentPanelBgColor = (ColorPickerPreference) findPreference(RECENT_PANEL_BG_COLOR);
        mRecentPanelBgColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_BG_COLOR, 0x80f5f5f5);
        String hexColor = String.format("#%08x", (0x80f5f5f5 & intColor));
        mRecentPanelBgColor.setSummary(hexColor);
        mRecentPanelBgColor.setNewPreviewColor(intColor);

        mBubbleMode = (ListPreference) prefSet.findPreference(BUBBLE_MODE);
        int bubble_mode = Settings.System.getInt(getContentResolver(),
                Settings.System.BUBBLE_RECENT, 0);
        mBubbleMode.setValue(String.valueOf(bubble_mode));
        mBubbleMode.setSummary(mBubbleMode.getEntry());
        mBubbleMode.setOnPreferenceChangeListener(this);

        // Recents swipe
        mRecentsSwipe = (CheckBoxPreference) prefSet.findPreference(PREF_RECENTS_SWIPE_FLOATING);
        mRecentsSwipe.setOnPreferenceChangeListener(this);
        mRecentsSwipe.setChecked(Settings.System.getInt(resolver,
                Settings.System.RECENTS_SWIPE_FLOATING, 0) == 1);

        mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
        mUseAltResolver.setOnPreferenceChangeListener(this);
        mUseAltResolver.setChecked(Settings.System.getInt(resolver,
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0) == 1);

        mReverseDefaultAppPicker = (CheckBoxPreference) findPreference(KEY_REVERSE_DEFAULT_APP_PICKER);
        mReverseDefaultAppPicker.setOnPreferenceChangeListener(this);
        mReverseDefaultAppPicker.setChecked(Settings.System.getInt(resolver,
                Settings.System.REVERSE_DEFAULT_APP_PICKER, 0) == 1);

        updatePreference();
    }

    private void updateRamBar() {
        int ramBarMode = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.RECENTS_RAM_BAR_MODE, 0);
        if (ramBarMode != 0)
            mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_enabled));
        else
            mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_disabled));
    }

    private void updatePreference() {
        int altResolver = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0);
        int reverse = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.REVERSE_DEFAULT_APP_PICKER, 0);
        int recentStyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.RECENTS_STYLE, 0);

        if (altResolver == 1)  {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0);
            mReverseDefaultAppPicker.setEnabled(false);
        } else {
            mReverseDefaultAppPicker.setEnabled(true);
        }
        if (reverse == 1) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0);
            mUseAltResolver.setEnabled(false);
        } else {
            mUseAltResolver.setEnabled(true);
        }

        if (recentStyle !=5) {
            mBubbleMode.setEnabled(false);
        }

        switch (recentStyle) {
            case 0:
            case 3:
            case 4:
            case 5:
                mRecentClearAll.setEnabled(true);
                mRamBar.setEnabled(true);
                mRecentPanelLeftyMode.setEnabled(false);
                mRecentPanelScale.setEnabled(false);
                mRecentPanelExpandedMode.setEnabled(false);
                mRecentPanelBgColor.setEnabled(false);
                mRecentsShowTopmost.setEnabled(false);
                mRecentsSwipe.setEnabled(true);
                break;
            case 1:
                mRecentClearAll.setEnabled(false);
                mRamBar.setEnabled(false);
                mRecentPanelLeftyMode.setEnabled(true);
                mRecentPanelScale.setEnabled(true);
                mRecentPanelExpandedMode.setEnabled(true);
                mRecentPanelBgColor.setEnabled(true);
                mRecentsShowTopmost.setEnabled(true);
                mRecentsSwipe.setEnabled(false);
                break;
            case 2:
                if (!isOmniSwitchInstalled()) return;
                mRecentClearAll.setEnabled(false);
                mRamBar.setEnabled(false);
                mRecentPanelLeftyMode.setEnabled(false);
                mRecentPanelScale.setEnabled(false);
                mRecentPanelExpandedMode.setEnabled(false);
                mRecentPanelBgColor.setEnabled(false);
                mRecentsShowTopmost.setEnabled(false);
                mRecentsSwipe.setEnabled(false);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRamBar();
        updatePreference();
    }

    @Override
    public void onPause() {
        super.onResume();
        updateRamBar();
        updatePreference();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mRecentStyle) {
            int recentStyle = Integer.valueOf((String) newValue);
            int index = mRecentStyle.findIndexOfValue((String) newValue);

            if (recentStyle == 2 && !isOmniSwitchInstalled()) {
                openOmniSwitchNotInstalledWarning();
                return true;
            }

            Settings.System.putInt(resolver,
                    Settings.System.RECENTS_STYLE, recentStyle);
            mRecentStyle.setSummary(mRecentStyle.getEntries()[index]);
            updatePreference();
            Helpers.restartSystemUI();

            if (recentStyle == 2) {
                openOmniSwitchEnabledWarning();
            }
            return true;
        } else if (preference == mBubbleMode) {
            int BubbleMode = Integer.valueOf((String) newValue);
            int index = mBubbleMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BUBBLE_RECENT, BubbleMode);
            mBubbleMode.setSummary(mBubbleMode.getEntries()[index]);
            return true;
        } else if (preference == mRecentClearAll) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.SHOW_CLEAR_RECENTS_BUTTON, value ? 1 : 0);
            return true;
        } else if (preference == mRecentClearAllPosition) {
            String value = (String) newValue;
            Settings.System.putString(resolver, Settings.System.CLEAR_RECENTS_BUTTON_LOCATION, value);
            return true;
        } else if (preference == mRecentsSwipe) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.RECENTS_SWIPE_FLOATING, value ? 1 : 0);
            return true;
        } else if (preference == mUseAltResolver) {
            boolean value = (Boolean) newValue;
			Settings.System.putInt(resolver, Settings.System.ACTIVITY_RESOLVER_USE_ALT, value ? 1 : 0);
            updatePreference();
            return true;
        } else if (preference == mReverseDefaultAppPicker) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.REVERSE_DEFAULT_APP_PICKER, value ? 1 : 0);
            return true;
        } else if (preference == mRecentPanelScale) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_SCALE_FACTOR, value);
            return true;
        } else if (preference == mRecentPanelLeftyMode) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_GRAVITY,
                    ((Boolean) newValue) ? Gravity.LEFT : Gravity.RIGHT);
            return true;
        } else if (preference == mRecentPanelExpandedMode) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContentResolver(),
            Settings.System.RECENT_PANEL_EXPANDED_MODE, value);
            return true;
        } else if (preference == mRecentPanelBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_BG_COLOR,
            intHex);
            return true;
        } else if (preference == mRecentsShowTopmost) {
            boolean value = (Boolean) newValue;
			Settings.System.putInt(resolver, Settings.System.RECENT_PANEL_SHOW_TOPMOST, value ? 1 : 0);
            return true;
        } else if (preference == mMultiPane) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.FORCE_MULTI_PANE, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void openOmniSwitchNotInstalledWarning() {
        new AlertDialog.Builder(getActivity())
        .setTitle(getResources().getString(R.string.omniswitch_warning_title))
        .setMessage(getResources().getString(R.string.omniswitch_not_installed_message))
        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
        }).show();
    }

    private void openOmniSwitchEnabledWarning() {
        new AlertDialog.Builder(getActivity())
        .setTitle(getResources().getString(R.string.omniswitch_warning_title))
        .setMessage(getResources().getString(R.string.omniswitch_enabled_message))
        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                                startActivity(INTENT_OMNISWITCH_SETTINGS);
                        }
        }).show();
    }

    private boolean isOmniSwitchInstalled() {
        final PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(OMNISWITCH_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
