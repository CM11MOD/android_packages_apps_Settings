/*
 * Copyright (C) 2014 DarkKat
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

package com.android.settings.aokp.animations;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.slim.util.CMDProcessor;
import com.android.settings.util.Helpers;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BootanimationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "BootanimationSettings";

    private static final String PREF_CUSTOM_BOOTANIMATION =
            "custom_bootanimation";
    private static final String PREF_BOOTANIMATION_PREVIEW_LOOP =
            "bootanimation_preview_loop";

    private static final String BACKUP_ORIGINAL_PATH = "/data/local/bootanimation.orig";
    private static final String BACKUP_CURRENT_PATH  = "/data/local/bootanimation.bak";
    private static final String BOOTAMIMATION_PATH   = "/system/media/bootanimation.zip";

    private static final int REQUEST_PICK_BOOT_ANIMATION = 0;

    private static final int MENU_HELP                   = Menu.FIRST;
    private static final int MENU_RESTORE                = MENU_HELP + 1;

    private static final int DLG_RESTORE                 = 0;
    private static final int DLG_SHOW_HELP_SCREEN        = 1;

    private Preference mCustomBootAnimation;
    private CheckBoxPreference mPreviewLoop;

    ImageView mView;
    TextView mError;
    private AnimationDrawable mAnimationPart1;
    private AnimationDrawable mAnimationPart2;
    private AnimationDrawable mAnimationPart3;
    private AnimationDrawable mAnimationPart4;
    private AnimationDrawable mAnimationPart5;
    private AnimationDrawable mAnimationPart6;
    private boolean mLoopAnimation;
    private String mErrormsg;
    private String mBootAniPath;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.animation_boot_interface);

        mCustomBootAnimation = findPreference(PREF_CUSTOM_BOOTANIMATION);

        mLoopAnimation = Settings.System.getInt(mResolver,
                   Settings.System.BOOTANIMATION_PREVIEW_LOOP, 1) == 1;

        mPreviewLoop =
                (CheckBoxPreference) findPreference(PREF_BOOTANIMATION_PREVIEW_LOOP);
        mPreviewLoop.setChecked(mLoopAnimation);
        mPreviewLoop.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_HELP, 0, R.string.help_label)
                .setIcon(R.drawable.ic_settings_about)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_RESTORE, 0, R.string.dlg_bootanimation_restore_title)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HELP:
                showDialogInner(DLG_SHOW_HELP_SCREEN);
                return true;
            case MENU_RESTORE:
                showDialogInner(DLG_RESTORE);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPreviewLoop) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.BOOTANIMATION_PREVIEW_LOOP,
                    value ? 1 : 0);
            mLoopAnimation = value;
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCustomBootAnimation) {
            PackageManager packageManager = getActivity().getPackageManager();
            Intent test = new Intent(Intent.ACTION_GET_CONTENT);
            test.setType("file/*");
            List<ResolveInfo> list = packageManager.queryIntentActivities(test, PackageManager.GET_ACTIVITIES);
            if (list.size() > 0) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("file/*");
                startActivityForResult(intent, REQUEST_PICK_BOOT_ANIMATION);
            } else {
                //No app installed to handle the intent - file explorer required
                Toast.makeText(getActivity(), R.string.install_file_manager_error, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                if (data==null) {
                    //Nothing returned by user, probably pressed back button in file manager
                    return;
                }

                mBootAniPath = data.getData().getEncodedPath();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.bootanimation_preview);
                builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CMDProcessor.getMount("rw");

                        //Check if original boot animation was already backed up, if not,
                        //backup original boot animation, give proper permissions
                        if (!new File(BACKUP_ORIGINAL_PATH).exists()) {
                            Log.d(TAG, "Backing up original boot animation");
                            new CMDProcessor().su.runWaitFor("cp "+ BOOTAMIMATION_PATH + ' ' + BACKUP_ORIGINAL_PATH);
                            new CMDProcessor().su.runWaitFor("chmod 644 "+ BACKUP_ORIGINAL_PATH);
                        } else {
                            Log.d(TAG, "Original boot animation already backed up");
                        }

                        //backup current boot animation, give proper permissions
                        new CMDProcessor().su.runWaitFor("cp "+ BOOTAMIMATION_PATH + ' ' + BACKUP_CURRENT_PATH);
                        new CMDProcessor().su.runWaitFor("chmod 644 "+ BACKUP_CURRENT_PATH);

                        //Copy new bootanimation, give proper permissions
                        new CMDProcessor().su.runWaitFor("cp "+ mBootAniPath + ' ' + BOOTAMIMATION_PATH);
                        new CMDProcessor().su.runWaitFor("chmod 644 "+ BOOTAMIMATION_PATH);

                        CMDProcessor.getMount("ro");

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(com.android.internal.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.dialog_bootanimation_preview,
                        (ViewGroup) getActivity().findViewById(R.id.bootanimation_layout_root));
                mError = (TextView) layout.findViewById(R.id.textViewError);
                mView = (ImageView) layout.findViewById(R.id.imageViewPreview);
                mView.setVisibility(View.GONE);
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                mView.setLayoutParams(new LinearLayout.LayoutParams(size.x/2, size.y/2));
                mError.setText(R.string.creating_preview);
                builder.setView(layout);
                AlertDialog dialog = builder.create();
                dialog.setOwnerActivity(getActivity());
                dialog.show();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        createPreview(mBootAniPath);
                    }
                });
                thread.start();
            }
        }
    }

    private void createPreview(String path) {
        File zip = new File(path);
        ZipFile zipfile = null;
        String desc = "";
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            zipfile = new ZipFile(zip);
            ZipEntry ze = zipfile.getEntry("desc.txt");
            inputStream = zipfile.getInputStream(ze);
            inputStreamReader = new InputStreamReader(inputStream);
            StringBuilder sb = new StringBuilder(0);
            bufferedReader = new BufferedReader(inputStreamReader);
            String read = bufferedReader.readLine();
            while (read != null) {
                sb.append(read);
                sb.append('\n');
                read = bufferedReader.readLine();
            }
            desc = sb.toString();
        } catch (Exception handleAllException) {
            mErrormsg = getActivity().getString(R.string.error_reading_zip_file);
            errorHandler.sendEmptyMessage(0);
            return;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                // we tried
            }
            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (IOException e) {
                // we tried
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // moving on...
            }
        }

        String[] info = desc.replace("\\r", "").split("\\n");
        // ignore first two ints height and width
        int delay = Integer.parseInt(info[0].split(" ")[2]);
        String partName1 = info[1].split(" ")[3];
        String partName2;
        String partName3;
        String partName4;
        String partName5;
        String partName6;
        try {
            if (info.length > 2) {
                partName2 = info[2].split(" ")[3];
            } else {
                partName2 = "";
            }
            if (info.length > 3) {
                partName3 = info[3].split(" ")[3];
            } else {
                partName3 = "";
            }
            if (info.length > 4) {
                partName4 = info[4].split(" ")[3];
            } else {
                partName4 = "";
            }
            if (info.length > 5) {
                partName5 = info[5].split(" ")[3];
            } else {
                partName5 = "";
            }
            if (info.length > 6) {
                partName6 = info[6].split(" ")[3];
            } else {
                partName6 = "";
            }
        } catch (Exception e) {
            partName2 = "";
            partName3 = "";
            partName4 = "";
            partName5 = "";
            partName6 = "";
        }

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 4;
        mAnimationPart1 = new AnimationDrawable();
        mAnimationPart2 = new AnimationDrawable();
        mAnimationPart3 = new AnimationDrawable();
        mAnimationPart4 = new AnimationDrawable();
        mAnimationPart5 = new AnimationDrawable();
        mAnimationPart6 = new AnimationDrawable();
        try {
            for (Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
                 enumeration.hasMoreElements(); ) {
                ZipEntry entry = enumeration.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String partname = entry.getName().split("/")[0];
                if (partName1.equalsIgnoreCase(partname)) {
                    InputStream partOneInStream = null;
                    try {
                        partOneInStream = zipfile.getInputStream(entry);
                        mAnimationPart1.addFrame(new BitmapDrawable(getResources(),
                                BitmapFactory.decodeStream(partOneInStream,
                                        null, opt)), delay);
                    } finally {
                        if (partOneInStream != null) {
                            partOneInStream.close();
                        }
                    }
                } else if (partName2.equalsIgnoreCase(partname)) {
                    InputStream partTwoInStream = null;
                    try {
                        partTwoInStream = zipfile.getInputStream(entry);
                        mAnimationPart2.addFrame(new BitmapDrawable(getResources(),
                                BitmapFactory.decodeStream(partTwoInStream,
                                        null, opt)), delay);
                    } finally {
                        if (partTwoInStream != null) {
                            partTwoInStream.close();
                        }
                    }
                } else if (partName3.equalsIgnoreCase(partname)) {
                    InputStream partThreeInStream = null;
                    try {
                        partThreeInStream = zipfile.getInputStream(entry);
                        mAnimationPart3.addFrame(new BitmapDrawable(getResources(),
                                BitmapFactory.decodeStream(partThreeInStream,
                                        null, opt)), delay);
                    } finally {
                        if (partThreeInStream != null) {
                            partThreeInStream.close();
                        }
                    }
                } else if (partName4.equalsIgnoreCase(partname)) {
                    InputStream partFourInStream = null;
                    try {
                        partFourInStream = zipfile.getInputStream(entry);
                        mAnimationPart4.addFrame(new BitmapDrawable(getResources(),
                                BitmapFactory.decodeStream(partFourInStream,
                                        null, opt)), delay);
                    } finally {
                        if (partFourInStream != null) {
                            partFourInStream.close();
                        }
                    }
                } else if (partName5.equalsIgnoreCase(partname)) {
                    InputStream partFiveInStream = null;
                    try {
                        partFiveInStream = zipfile.getInputStream(entry);
                        mAnimationPart5.addFrame(new BitmapDrawable(getResources(),
                                BitmapFactory.decodeStream(partFiveInStream,
                                        null, opt)), delay);
                    } finally {
                        if (partFiveInStream != null) {
                            partFiveInStream.close();
                        }
                    }
                } else if (partName6.equalsIgnoreCase(partname)) {
                    InputStream partSixInStream = null;
                    try {
                        partSixInStream = zipfile.getInputStream(entry);
                        mAnimationPart6.addFrame(new BitmapDrawable(getResources(),
                                BitmapFactory.decodeStream(partSixInStream,
                                        null, opt)), delay);
                    } finally {
                        if (partSixInStream != null) {
                            partSixInStream.close();
                        }
                    }
                }
            }
        } catch (IOException e1) {
            mErrormsg = getActivity().getString(R.string.error_creating_preview);
            errorHandler.sendEmptyMessage(0);
            return;
        }

        if (!partName2.isEmpty()) {
            Log.d(TAG, "Multipart Animation part 2");
            mAnimationPart1.setOneShot(false);
            mAnimationPart2.setOneShot(false);
            mAnimationPart1.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "First part finished");
                            mView.setImageDrawable(mAnimationPart2);
                            mAnimationPart1.stop();
                            mAnimationPart2.start();
                        }
                    });
        } else {
            Log.d(TAG, "Singlepart Animation");
            if (!mLoopAnimation) {
                mAnimationPart1.setOneShot(true);
            } else {
                mAnimationPart1.setOneShot(false);
            }
        }
        if (!partName3.isEmpty()) {
            Log.d(TAG, "Multipart Animation part 3");
            mAnimationPart3.setOneShot(false);
            mAnimationPart2.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "Second part finished");
                            mView.setImageDrawable(mAnimationPart3);
                            mAnimationPart2.stop();
                            mAnimationPart3.start();
                        }
                    });
        } else {
            mAnimationPart2.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "Animation finished");
                            mView.setImageDrawable(mAnimationPart1);
                            mAnimationPart2.stop();
                            if (!mLoopAnimation) {
                                mAnimationPart1.stop();
                            }
                        }
                    });
        }
        if (!partName4.isEmpty()) {
            Log.d(TAG, "Multipart Animation part 4");
            mAnimationPart4.setOneShot(false);
            mAnimationPart3.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "Third part finished");
                            mView.setImageDrawable(mAnimationPart4);
                            mAnimationPart3.stop();
                            mAnimationPart4.start();
                        }
                    });

        } else {
            mAnimationPart3.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "Animation finished");
                            mView.setImageDrawable(mAnimationPart1);
                            mAnimationPart3.stop();
                            if (!mLoopAnimation) {
                                mAnimationPart1.stop();
                            }
                        }
                    });
        }
        if (!partName5.isEmpty()) {
            Log.d(TAG, "Multipart Animation part 5");
            mAnimationPart5.setOneShot(false);
            mAnimationPart4.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "Fourth part finished");
                            mView.setImageDrawable(mAnimationPart5);
                            mAnimationPart4.stop();
                            mAnimationPart5.start();
                        }
                    });
        } else {
            mAnimationPart4.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "Animation finished");
                            mView.setImageDrawable(mAnimationPart1);
                            mAnimationPart4.stop();
                            if (!mLoopAnimation) {
                                mAnimationPart1.stop();
                            }
                        }
                    });
        }
        if (!partName6.isEmpty()) {
            Log.d(TAG, "Multipart Animation part 6");
            mAnimationPart6.setOneShot(false);
            mAnimationPart5.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "Fifth part finished");
                            mView.setImageDrawable(mAnimationPart6);
                            mAnimationPart5.stop();
                            mAnimationPart6.start();
                        }
                    });
            mAnimationPart6.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "Animation finished");
                            mView.setImageDrawable(mAnimationPart1);
                            mAnimationPart6.stop();
                            if (!mLoopAnimation) {
                                mAnimationPart1.stop();
                            }
                        }
                    });
        } else {
            mAnimationPart5.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "Animation finished");
                            mView.setImageDrawable(mAnimationPart1);
                            mAnimationPart5.stop();
                            if (!mLoopAnimation) {
                                mAnimationPart1.stop();
                            }
                        }
                    });
        }

        finishedHandler.sendEmptyMessage(0);
    }

    private Handler errorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mView.setVisibility(View.GONE);
            mError.setText(mErrormsg);
        }
    };

    private Handler finishedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mView.setImageDrawable(mAnimationPart1);
            mView.setVisibility(View.VISIBLE);
            mError.setVisibility(View.GONE);
            mAnimationPart1.start();
        }
    };

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        BootanimationSettings getOwner() {
            return (BootanimationSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_SHOW_HELP_SCREEN:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.help_label)
                    .setMessage(R.string.dlg_bootanimation_help_message)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create();
                case DLG_RESTORE:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dlg_bootanimation_restore_title)
                    .setMessage(R.string.dlg_bootanimation_restore_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.dlg_bootanimation_restore_original,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getOwner().restore(true);
                        }
                    })
                    .setPositiveButton(R.string.dlg_bootanimation_restore_last,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getOwner().restore(false);
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }

    public void restore(boolean original) {
        CMDProcessor.getMount("rw");

        if (original) {
            new CMDProcessor().su.runWaitFor(
                    "cp "+ BACKUP_ORIGINAL_PATH + ' ' + BOOTAMIMATION_PATH);
        } else {
            new CMDProcessor().su.runWaitFor(
                    "cp "+ BACKUP_CURRENT_PATH + ' ' + BOOTAMIMATION_PATH);
        }
        new CMDProcessor().su.runWaitFor("chmod 644 "+ BOOTAMIMATION_PATH);

        CMDProcessor.getMount("rw");
    }
}

