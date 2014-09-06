
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
import com.android.settings.DialogCreatable;
import com.android.settings.util.CMDProcessor;
import com.android.settings.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;


public class UserInterface extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "UserInterface";

    private static final String RECENTS_STYLE = "recents_style";
    private static final String RECENT_MENU_CLEAR_ALL = "recent_menu_clear_all";
    private static final String RECENT_MENU_CLEAR_ALL_LOCATION = "recent_menu_clear_all_location";
    private static final String RECENT_CLEAR_ALL_BTN_COLOR = "recent_clear_all_button_color";
    private static final String KEY_RECENTS_RAM_BAR = "recents_ram_bar";
    private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
    private static final String KEY_REVERSE_DEFAULT_APP_PICKER = "reverse_default_app_picker";
    private static final String RECENT_PANEL_LEFTY_MODE = "recent_panel_lefty_mode";
    private static final String RECENT_PANEL_SHOW_TOPMOST = "recent_panel_show_topmost";
    private static final String RECENT_PANEL_SCALE = "recent_panel_scale";
    private static final String RECENT_PANEL_EXPANDED_MODE = "recent_panel_expanded_mode";
    private static final String RECENT_PANEL_BG_COLOR =	"recent_panel_bg_color";
    private static final String RECENT_PANEL_COLOR = "recents_stock_bg_color";
    private static final String RECENT_CARD_BG_COLOR = "recent_card_bg_color";
    private static final String RECENT_CARD_TEXT_COLOR = "recent_card_text_color";
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
    private ColorPickerPreference mRecentsStockBgColor;
    private ColorPickerPreference mRecentPanelBgColor;
    private ColorPickerPreference mRecentCardBgColor;
    private ColorPickerPreference mRecentCardTextColor;
    private ColorPickerPreference mRecentsClearAllBtnColor;
    private ListPreference mBubbleMode;
    private CheckBoxPreference mMultiPane;
    private CheckBoxPreference mRecentsSwipe;

    private Preference mRamBar;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DEFAULT_BACKGROUND_COLOR = 0x80f5f5f5;
    private static final int RECENTS_BACKGROUND_COLOR = 0xe0000000;
    private static final int DEFAULT_SLIM_COLOR =0x00ffffff;
    private static final int DEFAULT_RECENT_CLEAR_ALL_BTN_COLOR = 0xffffffff;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.user_interface_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

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
        intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_BG_COLOR, 0x80f5f5f5);
        hexColor = String.format("#%08x", (0x80f5f5f5 & intColor));
        mRecentPanelBgColor.setSummary(hexColor);
        mRecentPanelBgColor.setNewPreviewColor(intColor);

        // Recent card background color
        mRecentCardBgColor =
                (ColorPickerPreference) findPreference(RECENT_CARD_BG_COLOR);
        mRecentCardBgColor.setOnPreferenceChangeListener(this);
        final int intColorCard = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_CARD_BG_COLOR, 0x00ffffff);
        String hexColorCard = String.format("#%08x", (0x00ffffff & intColorCard));
        if (hexColorCard.equals("#00ffffff")) {
            mRecentCardBgColor.setSummary(R.string.trds_default_color);
        } else {
            mRecentCardBgColor.setSummary(hexColorCard);
        }
        mRecentCardBgColor.setNewPreviewColor(intColorCard);

        // Recent card text color
        mRecentCardTextColor =
                (ColorPickerPreference) findPreference(RECENT_CARD_TEXT_COLOR);
        mRecentCardTextColor.setOnPreferenceChangeListener(this);
        final int intColorText = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_CARD_TEXT_COLOR, 0x00ffffff);
        String hexColorText = String.format("#%08x", (0x00ffffff & intColorText));
        if (hexColorText.equals("#00ffffff")) {
            mRecentCardTextColor.setSummary(R.string.trds_default_color);
        } else {
            mRecentCardTextColor.setSummary(hexColorText);
        }
        mRecentCardTextColor.setNewPreviewColor(intColorText);


        // Recent Clear All Button Color
        mRecentsClearAllBtnColor =
                (ColorPickerPreference) findPreference(RECENT_CLEAR_ALL_BTN_COLOR);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CLEAR_RECENTS_BUTTON_COLOR, DEFAULT_RECENT_CLEAR_ALL_BTN_COLOR);
        mRecentsClearAllBtnColor.setNewPreviewColor(intColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mRecentsClearAllBtnColor.setSummary(hexColor);
        mRecentsClearAllBtnColor.setOnPreferenceChangeListener(this);

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

        mRecentsStockBgColor = (ColorPickerPreference) findPreference(RECENT_PANEL_COLOR);
        mRecentsStockBgColor.setOnPreferenceChangeListener(this);

        updatePreference();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset_default_message)
                .setIcon(R.drawable.ic_settings_backup)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.shortcut_action_reset);
        alertDialog.setMessage(R.string.reset_settings_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        String hexColor;

        Settings.System.putInt(getContentResolver(),
                Settings.System.RECENT_PANEL_BG_COLOR, DEFAULT_BACKGROUND_COLOR);
        hexColor = String.format("#%08x", (0x80f5f5f5 & DEFAULT_BACKGROUND_COLOR));
        mRecentPanelBgColor.setSummary(hexColor);
        mRecentPanelBgColor.setNewPreviewColor(DEFAULT_BACKGROUND_COLOR);

        Settings.System.putInt(getContentResolver(),
                Settings.System.RECENTS_PANEL_STOCK_COLOR, RECENTS_BACKGROUND_COLOR);
        hexColor = String.format("#%08x", (0xe0000000 & RECENTS_BACKGROUND_COLOR));
        mRecentsStockBgColor.setSummary(hexColor);
        mRecentsStockBgColor.setNewPreviewColor(RECENTS_BACKGROUND_COLOR);

        Settings.System.putInt(getContentResolver(),
                Settings.System.RECENT_CARD_BG_COLOR, DEFAULT_BACKGROUND_COLOR);
        hexColor = String.format("#%08x", (0xffffffff & DEFAULT_BACKGROUND_COLOR));
        mRecentCardBgColor.setSummary(hexColor);
        mRecentCardBgColor.setNewPreviewColor(DEFAULT_BACKGROUND_COLOR);

        Settings.System.putInt(getContentResolver(),
                Settings.System.RECENT_CARD_BG_COLOR, DEFAULT_SLIM_COLOR);
        mRecentCardBgColor.setNewPreviewColor(DEFAULT_SLIM_COLOR);
        mRecentCardBgColor.setSummary(R.string.trds_default_color);

        Settings.System.putInt(getContentResolver(),
                Settings.System.RECENT_CARD_TEXT_COLOR, DEFAULT_SLIM_COLOR);
        mRecentCardTextColor.setNewPreviewColor(DEFAULT_SLIM_COLOR);
        mRecentCardTextColor.setSummary(R.string.trds_default_color);

        Settings.System.putInt(getContentResolver(),
                Settings.System.CLEAR_RECENTS_BUTTON_COLOR, DEFAULT_RECENT_CLEAR_ALL_BTN_COLOR);
        hexColor = String.format("#%08x", (0xffffffff & DEFAULT_RECENT_CLEAR_ALL_BTN_COLOR));
        mRecentsClearAllBtnColor.setSummary(hexColor);
        mRecentsClearAllBtnColor.setNewPreviewColor(DEFAULT_RECENT_CLEAR_ALL_BTN_COLOR);
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
                mRecentsClearAllBtnColor.setEnabled(true);
                mRamBar.setEnabled(true);
                mRecentPanelLeftyMode.setEnabled(false);
                mRecentPanelScale.setEnabled(false);
                mRecentPanelExpandedMode.setEnabled(false);
                mRecentPanelBgColor.setEnabled(false);
                mRecentsShowTopmost.setEnabled(false);
                mRecentsSwipe.setEnabled(true);
                mRecentsStockBgColor.setEnabled(true);
                mRecentCardBgColor.setEnabled(false);
                mRecentCardTextColor.setEnabled(false);
                break;
            case 1:
                mRecentClearAll.setEnabled(false);
                mRecentsClearAllBtnColor.setEnabled(false);
                mRamBar.setEnabled(false);
                mRecentPanelLeftyMode.setEnabled(true);
                mRecentPanelScale.setEnabled(true);
                mRecentPanelExpandedMode.setEnabled(true);
                mRecentPanelBgColor.setEnabled(true);
                mRecentsShowTopmost.setEnabled(true);
                mRecentsSwipe.setEnabled(false);
                mRecentsStockBgColor.setEnabled(false);
                mRecentCardBgColor.setEnabled(true);
                mRecentCardTextColor.setEnabled(true);
                break;
            case 2:
                if (!isOmniSwitchInstalled()) return;
                mRecentClearAll.setEnabled(false);
                mRecentsClearAllBtnColor.setEnabled(false);
                mRamBar.setEnabled(false);
                mRecentPanelLeftyMode.setEnabled(false);
                mRecentPanelScale.setEnabled(false);
                mRecentPanelExpandedMode.setEnabled(false);
                mRecentPanelBgColor.setEnabled(false);
                mRecentsShowTopmost.setEnabled(false);
                mRecentsSwipe.setEnabled(false);
                mRecentsStockBgColor.setEnabled(false);
                mRecentCardBgColor.setEnabled(false);
                mRecentCardTextColor.setEnabled(false);
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
            if (hex.equals("#80f5f5f5")) {
                preference.setSummary(R.string.trds_default_color);
            } else {
                preference.setSummary(hex);
            }
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
        } else if (preference == mRecentsStockBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENTS_PANEL_STOCK_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mRecentCardBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#00ffffff")) {
                preference.setSummary(R.string.trds_default_color);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_CARD_BG_COLOR,
                    intHex);
            return true;
        } else if (preference == mRecentCardTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#00ffffff")) {
                preference.setSummary(R.string.trds_default_color);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_CARD_TEXT_COLOR,
                    intHex);
            return true;
        } else if (preference == mRecentsClearAllBtnColor) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.CLEAR_RECENTS_BUTTON_COLOR, intHex);
            preference.setSummary(hex);
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
