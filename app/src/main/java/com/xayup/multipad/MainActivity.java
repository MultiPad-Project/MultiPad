package com.xayup.multipad;

import android.app.*;
import android.content.*;
import android.hardware.usb.UsbManager;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.xayup.debug.XLog;
import com.xayup.multipad.configs.GlobalConfigs;

import com.xayup.multipad.layouts.grid.FloatingWindowGridResize;
import com.xayup.multipad.layouts.loadscreen.LoadScreen;
import com.xayup.multipad.layouts.main.panel.MainPanel;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.projects.Project;
import com.xayup.multipad.projects.ProjectManager;
import com.xayup.multipad.projects.Projects;
import com.xayup.multipad.pads.GridPadsReceptor;
import com.xayup.multipad.projects.project.keyled.Colors;
import com.xayup.multipad.projects.project.keyled.KeyLED;
import com.xayup.multipad.projects.thread.LoadProject;
import com.xayup.ui.options.FluctuateOptionsView;
import com.xayup.ui.options.OptionsItem;
import com.xayup.ui.options.OptionsItemInterface;
import com.xayup.ui.options.OptionsPage;

import java.io.*;
import java.util.List;

public class MainActivity extends Activity {
    public View content_view_root;
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

    protected Colors colors;

    PlayPads mPlayPads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        this.content_view_root = findViewById(R.id.main_activity);
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

        ViewGroup splash = findViewById(R.id.splash);

        // Read project after render
        splash.post(new Runnable() {
            @Override
            public void run() {
                GlobalConfigs.display_height = content_view_root.getMeasuredHeight();
                GlobalConfigs.display_width = content_view_root.getMeasuredWidth();
                XLog.v("Extract", "Default color tables");
                colors = new Colors(context);

                XLog.v("Projects", "Reading");
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

                //registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));

                View floating_button = findViewById(R.id.main_floating_menu_button);

                XLog.v("Grid", "Make default grid");
                mPlayPads = new PlayPads(context, context.findViewById(R.id.main_pads_to_add));
                newGrid();
                defaultPadClick(mPlayPads.getPads().getActivePads());

                FloatingWindowGridResize gridResizer = new FloatingWindowGridResize(context, context.findViewById(R.id.main_container)) {
                    @Override
                    public GridPadsReceptor getGridPadsReceptor() { return mPlayPads.getPads(); }

                };

                mMainPanel = new MainPanel(context) {
                    @Override
                    public void onExit() { killApp(); }

                    @Override
                    public KeyLED getKeyLEDInstance() {
                        return null;
                    }

                    @Override
                    public GridPadsReceptor getPadInstance() {
                        return mPlayPads.getPads();
                    }

                    @Override
                    public List<ProjectManager> getProjects() {
                        return mProjects.getProjects();
                    }

                    @Override
                    public void addNewGrid() {
                        newGrid();
                        defaultPadClick(mPlayPads.getPads().getActivePads());
                    }

                    @Override
                    public void loadProject(ProjectManager projectManager, ProgressBar progressBar) {
                        if(projectManager.getProject().getStatus() == Project.STATUS_UNLOADED) {
                            projectManager.loadProject(context, new LoadProject.LoadingProject() {
                                @Override
                                public void onStartLoadProject() {
                                    progressBar.setProgress(0);
                                    progressBar.setMax(
                                            ((projectManager.getProject().keysound_path != null) ? 1 + projectManager.getProject().sample_count : 0)
                                                    + projectManager.getProject().keyled_count + ((projectManager.getProject().autoplay_path != null) ? 1 : 0));
                                    projectManager.getProject().setStatus(Project.STATUS_LOADING); }
                                @Override
                                public void onStartReadFile(String file_name) { progressBar.incrementProgressBy(1); }
                                @Override
                                public void onFileError(String file_name, int line, String cause) { progressBar.incrementProgressBy(1);  }
                                @Override
                                public void onFinishLoadProject() {
                                    progressBar.setProgress(progressBar.getMax());
                                    projectManager.getProject().setStatus(Project.STATUS_LOADED);
                                    mPlayPads.getPads().notifyProject(projectManager);
                                    mProjects.addLoadedProject(projectManager);
                                    mProjects.alphabeticOrder(mProjects.getLoadedProjects()); }
                             });
                        } else if (projectManager.getProject().getStatus() == Project.STATUS_LOADED){ // Project loaded
                        }
                    }
                    @Override
                    public void showGridResize(boolean show) {
                        if(show){
                            gridResizer.show();
                            GlobalConfigs.floating_window_grid_resize_visible = true;
                        } else {
                            gridResizer.hide();
                            GlobalConfigs.floating_window_grid_resize_visible = false;
                        }
                    }

                    @Override
                    public void onGriditemClick(GridPadsReceptor.PadGrid padGrid) {
                        padGridMenu(padGrid);
                    }
                };
                mMainPanel.updates();
                mMainPanel.home();

                floating_button.setOnClickListener((v) -> mMainPanel.showPanel());

                hideSplash(() -> mMainPanel.showPanel());

                splash.removeCallbacks(this);
            }
        });
    }

    public void newGrid(){
        RelativeLayout to_add = this.findViewById(R.id.main_pads_to_add);
        mPlayPads.getPads().newPads(GlobalConfigs.PlayPadsConfigs.skin_package, (byte) 10, (byte) 10, colors.getDefaultTable());
        to_add.addView(mPlayPads.getPads().getActivePads().getContainer(), new ViewGroup.LayoutParams(GlobalConfigs.display_height, GlobalConfigs.display_height));
    }

    public void defaultPadClick(GridPadsReceptor.PadGrid active_pad){
        active_pad.forAllPads((pad, mPadGrid) -> {
            if(pad.getType() == MakePads.PadInfo.PadInfoIdentifier.PAD_LOGO){
                active_pad.getPads().getPadView(pad.getRow(), pad.getColum()).setOnTouchListener((pad_view, event) -> {
                    pad_view.performClick();
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        padGridMenu(mPadGrid);
                        return true;
                    }
                    return false;
                });
            }
        });
    }

    public void padGridMenu(GridPadsReceptor.PadGrid padGrid){
        int page_switch_duration = (int)(context.getResources().getInteger(R.integer.swipe_animation_velocity) * Ui.getSettingsAnimationScale(context));
        FluctuateOptionsView window = new FluctuateOptionsView(context);
        //Home page
        OptionsPage home_page = window.getPage(window.newPage(padGrid.getName()));
        OptionsPage projects_loaded = window.getPage(window.newPage(context.getString(R.string.pad_grid_menu_choose_project_title)));
        OptionsPage color_table_page = window.getPage(window.newPage(context.getString(R.string.pad_grid_menu_project_settings_title)));
        OptionsPage project_settings_page = window.getPage(window.newPage(context.getString(R.string.pad_grid_menu_project_settings_title)));

        //Home page options
        OptionsItem set_current_project = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_ARROW);
        home_page.putOption(set_current_project);
        set_current_project.setTitle(context.getString(R.string.pad_grid_menu_choose_project_title));
        set_current_project.setDescription(context.getString(R.string.pad_grid_menu_choose_project_description));
        set_current_project.setOnClick((item_view) -> {
            projects_loaded.clear();
            //Get loaded projects
            OptionsItem none_project_item = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
            none_project_item.setTitle(context.getString(R.string.none));
            projects_loaded.putOption(none_project_item);
            none_project_item.setOnClick((item) -> {
                if(padGrid.getProject().getProjectManager() != null)
                    padGrid.getProject().getProjectManager().removeGrid(padGrid);
            });
            if(mProjects.getLoadedProjects() != null) {
                for (ProjectManager projectManager : mProjects.getLoadedProjects()) {
                    OptionsItem project_item = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
                    project_item.setTitle(projectManager.getProject().getTitle());
                    project_item.setDescription(projectManager.getProject().getProducerName());
                    projects_loaded.putOption(project_item);
                    project_item.setOnClick((item) -> {
                        padGrid.setProject(projectManager);
                    });
                }
            }
            window.switchTo(projects_loaded.getPageIndex(), false, page_switch_duration);
        });

        OptionsItem set_id = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
        home_page.putOption(set_id);
        set_id.setTitle(context.getString(R.string.pad_grid_menu_change_the_id_title));
        set_id.setDescription(context.getString(R.string.pad_grid_menu_change_the_id_description));
        set_id.setOnClick(set_id_view -> {
            View dialog_layout = context.getLayoutInflater().inflate(R.layout.dialog_with_edittext, null);
            AlertDialog dialog = new AlertDialog.Builder(context).setView(dialog_layout).create();
            Button btn1 = dialog_layout.findViewById(R.id.dwe_btn1);
            Button btn2 = dialog_layout.findViewById(R.id.dwe_btn2);
            EditText edt1 = dialog_layout.findViewById(R.id.dwe_editText);
            edt1.setText(String.valueOf(padGrid.getId()));
            edt1.setInputType(InputType.TYPE_CLASS_NUMBER);
            btn1.setVisibility(View.VISIBLE);
            btn1.setText(context.getString(R.string.ok));
            btn1.setOnClickListener((b_v) -> {
                if(edt1.getText().length() > 0) {
                    padGrid.setId(Integer.parseInt(edt1.getText().toString()));
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Incorrect", Toast.LENGTH_SHORT).show();
                }
            });
            btn2.setVisibility(View.VISIBLE);
            btn2.setText(context.getString(R.string.cancel));
            btn2.setOnClickListener((b_v) -> {
                dialog.dismiss();
            });
            dialog.show();
        });

        OptionsItem set_color_table = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_ARROW);
        home_page.putOption(set_color_table);
        set_color_table.setTitle(context.getString(R.string.pad_grid_menu_choose_color_table_title));
        set_color_table.setDescription(context.getString(R.string.pad_grid_menu_choose_color_table_description));
        set_color_table.setOnClick(v -> {
            color_table_page.clear();
            // switchTo(EXIT_PAGE_LIST_COLOR_TABLE, false);
            File root = new File(GlobalConfigs.DefaultConfigs.COLOR_TABLE_PATH);
            if(root.exists()) for(File file : root.listFiles()){
                OptionsItem item = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
                byte format_index = (byte) file.getName().lastIndexOf(".");
                item.setTitle((format_index == -1) ?
                        file.getName() :
                        file.getName().replace(file.getName().substring(format_index), ""));
                item.setOnClick((view1)->{
                    XLog.v("Color Table name", file.getName());
                    padGrid.setColors(colors.getTable(file));
                });
                color_table_page.putOption(item);
            }
            window.switchTo(color_table_page.getPageIndex(), false, page_switch_duration);
        });
        OptionsItem project_settings = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_ARROW);
        home_page.putOption(project_settings);
        project_settings.setTitle(context.getString(R.string.pad_grid_menu_project_settings_title));
        project_settings.setDescription(context.getString(R.string.pad_grid_menu_project_settings_description));
        project_settings.setOnClick(v -> {
            project_settings_page.clear();
            OptionsItem reverse_rows = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_CHECKBOX);
            OptionsItem reverse_columns = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_CHECKBOX);
            reverse_rows.setTitle("Reverse rows");
            reverse_columns.setTitle("Reverse columns");
            reverse_rows.setChecked(padGrid.isReversedRow());
            reverse_columns.setChecked(padGrid.isReversedColum());
            reverse_rows.setOnClick(item_view -> {
                reverse_rows.setChecked(!reverse_rows.getCheckBox().isChecked());
                padGrid.reverseRow(reverse_rows.getCheckBox().isChecked());
            });
            reverse_columns.setOnClick(item_view -> {
                reverse_columns.setChecked(!reverse_columns.getCheckBox().isChecked());
                padGrid.reverseColum(reverse_columns.getCheckBox().isChecked());
            });
            project_settings_page.putOption(reverse_rows);
            project_settings_page.putOption(reverse_columns);
            window.switchTo(project_settings_page.getPageIndex(), false, page_switch_duration);
        });

        //Back button
        window.getBackButton().setOnClickListener(v -> {
            int page = window.getCurrentPageIndex();
            if (page == projects_loaded.getPageIndex() ||
                    page == color_table_page.getPageIndex() ||
                    page == project_settings_page.getPageIndex()){
                window.switchTo(home_page.getPageIndex(), true, page_switch_duration);
            }
        });
        window.switchTo(0, false, 0);
        window.show();
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
        );
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
