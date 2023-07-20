package com.xayup.multipad;

import android.app.*;
import android.content.*;
import android.hardware.usb.UsbManager;
import android.os.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import com.xayup.debug.XLog;
import com.xayup.multipad.configs.GlobalConfigs;

import com.xayup.multipad.layouts.loadscreen.LoadScreen;
import com.xayup.multipad.layouts.main.panel.MainPanel;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.projects.Project;
import com.xayup.multipad.projects.Projects;
import com.xayup.multipad.pads.GridPads;
import com.xayup.multipad.projects.project.keyled.KeyLED;
import com.xayup.multipad.projects.thread.LoadProject;
import com.xayup.ui.options.FluctuateOptionsView;
import com.xayup.ui.options.OptionsItem;
import com.xayup.ui.options.OptionsItemInterface;
import com.xayup.ui.options.OptionsPage;

import java.io.*;
import java.util.List;

public class MainActivity extends Activity {
    private Activity context;
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
        this.context = this;
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

        PlayPads mPlayPads = new PlayPads(context, context.findViewById(R.id.main_pads_to_add));
        defaultPadClick(mPlayPads.getPads().getActivePads());
        mMainPanel = new MainPanel(context) {
            @Override
            public void onExit() { killApp(); }

            @Override
            public KeyLED getKeyLEDInstance() {
                return null;
            }

            @Override
            public GridPads getPadInstance() {
                return null;
            }

            @Override
            public List<Project> getProjects() {
                return mProjects.projects;
            }

            @Override
            public void loadProject(Project project, ProgressBar progressBar) {
                if(project.getStatus() == Project.STATUS_UNLOADED) {
                    project.loadProject(context, new LoadProject.LoadingProject() {
                        @Override
                        public void onStartLoadProject() {
                            progressBar.setProgress(0);
                            progressBar.setMax(
                                    ((project.keysound_path != null) ? 1 + project.sample_count : 0)
                                            + project.keyled_count + ((project.autoplay_path != null) ? 1 : 0));
                            project.setStatus(Project.STATUS_LOADING);
                        }

                        @Override
                        public void onStartReadFile(String file_name) {
                            progressBar.incrementProgressBy(1);
                        }

                        @Override
                        public void onFileError(String file_name, int line, String cause) {
                            progressBar.incrementProgressBy(1);
                        }

                        @Override
                        public void onFinishLoadProject() {
                            progressBar.setProgress(progressBar.getMax());
                            project.setStatus(Project.STATUS_LOADED);
                        }
                    });
                } else if (project.getStatus() == Project.STATUS_LOADED){
                    // Project loaded
                }
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
                types[mProjects.FLAG_AUTOPLAY_DIFICULTY] = true;
                types[mProjects.TYPE_KEYLED_FOLDERS] = true;
                types[mProjects.FLAG_KEYLED_COUNT] = true;
                types[mProjects.TYPE_SAMPLE_FOLDER] = true;
                types[mProjects.FLAG_SAMPLE_COUNT] = true;
                mProjects.readProjectsPath(rootFolder, types);
                mMainPanel.updates();
                mMainPanel.home();
                hideSplash(() -> mMainPanel.showPanel());
                splash.removeCallbacks(this);
            }
        };
        splash.post(onPost);

    }

    public void defaultPadClick(GridPads.PadGrid active_pad){
        active_pad.forAllPads((pad, mPadGrid) -> {
            XLog.e("defaultPadClick", "Grid Childs count " + active_pad.getGridPads().getChildCount());
            XLog.e("defaultPadClick", "Pad type " + ((MakePads.PadInfo) pad.getTag()).getType());
            if(((MakePads.PadInfo) pad.getTag()).getType() == MakePads.PadInfo.PadInfoIdentifier.PAD_LOGO){
                pad.setOnTouchListener((pad_view, event) -> {
                    pad_view.performClick();
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        FluctuateOptionsView window = new FluctuateOptionsView(context);
                        //Home page
                        OptionsPage home_page = window.getPage(window.newPage(mPadGrid.getName()));
                        OptionsPage projects_loaded = window.getPage(window.newPage("Use the project"));
                        //Get loaded projects
                        for(Project project : mProjects.projects){
                            if(project.getStatus() == Project.STATUS_LOADED){
                                OptionsItem project_item = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
                                project_item.setTitle(project.getTitle());
                                project_item.setDescription(project.getProducerName());
                                projects_loaded.putOption(project_item);
                                project_item.setOnClick((item_view) -> {
                                    active_pad.setProject(project);
                                });
                            }
                        }
                        //Home page options
                        OptionsItem set_current_project = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_ARROW);
                        set_current_project.setTitle("Use the project");
                        set_current_project.setOnClick((item_view) -> {
                            window.switchTo(projects_loaded.getPageIndex(), false,
                                    (int)(context.getResources().getInteger(R.integer.swipe_animation_velocity) * Ui.getSettingsAnimationScale(context)));
                        });
                        home_page.putOption(set_current_project);
                        window.switchTo(0, false, 0);
                        window.show();
                        return true;
                    }
                    return false;
                });
            }
        });
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

    public void killApp(){
        this.finishAffinity();
        System.exit(0);
    }

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
