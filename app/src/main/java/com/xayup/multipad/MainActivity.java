package com.xayup.multipad;

import android.app.*;
import android.content.*;
import android.hardware.usb.UsbManager;
import android.os.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.xayup.multipad.configs.GlobalConfigs;

import com.xayup.multipad.layouts.loadscreen.LoadScreen;
import com.xayup.multipad.layouts.main.panel.MainPanel;
import com.xayup.multipad.projects.Projects;
import com.xayup.multipad.pads.Pad;
import com.xayup.multipad.projects.project.keyled.KeyLED;

import java.io.*;
import java.util.List;
import java.util.Map;

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

    protected Projects mProjects;

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

            @Override
            public List<Map<Byte, Object>> getProjects() {
                return mProjects.projects;
            }


        };
        floating_button.setOnClickListener((v) -> mMainPanel.showPanel());

        // Ready project after render
        Runnable onPost = new Runnable() {
            @Override
            public void run() {
                GlobalConfigs.display_height = splash.getMeasuredHeight();
                GlobalConfigs.display_width = splash.getMeasuredWidth();

                mProjects = new Projects();
                boolean[] types = new boolean[mProjects.FLAG_SIZE];
                types[mProjects.FLAG_TITLE] = true;
                types[mProjects.FLAG_PRODUCER_NAME] = true;
                types[mProjects.TYPE_AUTOPLAY_FILE] = true;
                types[mProjects.TYPE_KEYLED_FOLDERS] = true;
                types[mProjects.TYPE_SAMPLE_FOLDER] = true;
                types[mProjects.FLAG_ITEM_AUTOPLAY_DIFFICULTY] = true;
                mProjects.readProjectsPath(rootFolder, types);
                mMainPanel.updates();
                hideSplash(() -> mMainPanel.showPanel());
                splash.removeCallbacks(this);
            }
        };
        splash.post(onPost);

    }

    protected void hideSplash(Runnable after_hide) {
        View splash_screen = findViewById(R.id.splash);
        splash_screen.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_splash));
        splash_screen.setVisibility(View.GONE);
        if(after_hide != null) splash_screen.getAnimation().setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }
                    @Override
                    public void onAnimationEnd(Animation animation) { after_hide.run(); }
                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                }
        );;
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
