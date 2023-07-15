package com.xayup.multipad;

import android.app.*;
import android.content.*;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.midi.MidiDeviceInfo;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.xayup.debug.Debug;
import com.xayup.filesexplorer.FileExplorerDialog;
import com.xayup.multipad.configs.GlobalConfigs;

import com.xayup.multipad.layouts.loadscreen.LoadScreen;
import com.xayup.multipad.layouts.main.panel.MainPanel;
import com.xayup.multipad.pads.Pad;
import com.xayup.multipad.project.keyled.KeyLED;

import java.io.*;

public class MainActivity extends Activity {
    private final Activity context = this;
    protected final byte INTENT_PLAY_PADS = 0;

    protected LoadScreen mLoadScreen;
    protected File info;

    protected PendingIntent permissionIntent;

    protected static String skinConfig;
    protected static boolean useUnipadFolderConfig;

    protected static int height;
    protected static int width;

    protected File rootFolder;
    protected String ACTION_USB_PERMISSION;

    protected MainPanel mMainPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ACTION_USB_PERMISSION = this.getPackageName()+".USB_PERMISSION";
        this.makeActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_PLAY_PADS){
            mLoadScreen.OnEndAnimation(null);
            mLoadScreen.hide(0);
            mLoadScreen.remove();
            mLoadScreen = null;
        }
    }

    public void makeActivity() {
        //Load and get settings
        XayUpFunctions.hideSystemBars(getWindow());
        GlobalConfigs.loadSharedPreferences(context);
        skinConfig = GlobalConfigs.app_configs.getString("skin", context.getPackageName());
        useUnipadFolderConfig = GlobalConfigs.app_configs.getBoolean("useUnipadFolder", false);
        GlobalConfigs.use_unipad_colors = false;

        if (useUnipadFolderConfig) rootFolder = new File(GlobalConfigs.DefaultConfigs.UNIPAD_PATH);
        else rootFolder = new File(GlobalConfigs.DefaultConfigs.PROJECTS_PATH);

        if (!rootFolder.exists()) rootFolder.mkdirs();

        ViewGroup container = findViewById(R.id.main_container);
        ViewGroup splash = findViewById(R.id.splash);

        // Ready project after render
        Runnable onPost = new Runnable() {
            @Override
            public void run() {
                /*new ProjectsBase(
                this,
                rootFolder,
                rootView,
                getPackageName(),
                "main",
                new PlayProject() {
                    @Override
                    public void onPreLoadProject() {
                        mLoadScreen = new LoadScreen(context, rootView);
                    }

                    @Override
                    public void loadProject(String path) {
                        mLoadScreen.OnEndAnimation(
                                () -> {
                                    Intent intent = new Intent(context, PlayPads.class);;
                                    context.startActivityForResult(
                                            intent.putExtra("project_path", path), INTENT_PLAY_PADS);
                                    context.overridePendingTransition(0, 0);
                                });
                        mLoadScreen.show(500);
                    }
                });*/
                GlobalConfigs.display_height = splash.getMeasuredHeight();
                GlobalConfigs.display_width = splash.getMeasuredWidth();
                hideSplash();
                splash.removeCallbacks(this);
            }
        };
        splash.post(onPost);

        //registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));

        View floating_button = findViewById(R.id.main_floating_menu_button);

        mMainPanel = new MainPanel(context) {
            @Override
            public void onExit() { MainActivity.super.onBackPressed(); }

            @Override
            public KeyLED getKeyLEDInstance() {
                return null;
            }

            @Override
            public Pad getPadInstance() {
                return null;
            }
        };
        floating_button.setOnClickListener((v) -> mMainPanel.showPanel());

    }

    protected void hideSplash() {
        View splash_screen = findViewById(R.id.splash);
        splash_screen.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_splash));
        splash_screen.setVisibility(View.GONE);
    }

    private final BroadcastReceiver usbReceiver =
            new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ACTION_USB_PERMISSION.equals(action)) {
                        synchronized (this) {
                            if (intent.getBooleanExtra(
                                    UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                new UsbDeviceActivity()
                                        .openMidiDevice(context, MidiStaticVars.midiDevice);
                            } else {
                                MidiStaticVars.midiDevice = null;
                            }
                        }
                    }
                }
            };

    @Override
    public void onBackPressed() { mMainPanel.showPanel(); }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onWindowFocusChanged(boolean bool) {
        super.onWindowFocusChanged(bool);
        //XLog.e("Activity Focus Change", String.valueOf(bool));
        if (bool) {
            XayUpFunctions.hideSystemBars(getWindow());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(usbReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
    }
}
