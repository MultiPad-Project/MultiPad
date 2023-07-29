package com.xayup.multipad.layouts.main.panel;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.xayup.debug.Debug;
import com.xayup.multipad.R;
import com.xayup.multipad.Ui;
import com.xayup.multipad.XayUpFunctions;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.projects.Project;
import com.xayup.multipad.projects.ProjectListAdapter;
import com.xayup.multipad.pads.GridPadsReceptor;
import com.xayup.multipad.projects.ProjectManager;
import com.xayup.multipad.projects.project.keyled.KeyLED;
import com.xayup.multipad.skin.SkinAdapter;
import com.xayup.multipad.skin.SkinManager;
import com.xayup.multipad.skin.SkinProperties;
import com.xayup.ui.options.FluctuateOptionsView;
import com.xayup.ui.options.OptionsItem;
import com.xayup.ui.options.OptionsItemInterface;
import com.xayup.ui.options.OptionsPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class MainPanel {
    protected Context context;
    protected SkinAdapter mSkinAdapter;
    protected AlertDialog windowShow;
    protected ProjectListAdapter mProjectItem;
    protected ValueAnimator vAnimatior;

    protected View panel;

    protected Animation out;
    protected Animation in;

    protected View project_tab;

    public abstract void onExit();
    public abstract KeyLED getKeyLEDInstance();
    public abstract GridPadsReceptor getPadInstance();
    public abstract List<ProjectManager> getProjects();
    public abstract void addNewGrid();
    public abstract void loadProject(ProjectManager project, ProgressBar progressBar);
    public abstract void showGridResize(boolean show);
    public abstract void onGriditemClick(GridPadsReceptor.PadGrid padGrid);

    public MainPanel(Context context){
        this.context = context;
        this.mSkinAdapter = new SkinAdapter(context);
        out = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        in = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

        // Projects Page
        OptionsPage projects_page = new OptionsPage(context, false);
        // Skin Page
        OptionsPage skins_page = new OptionsPage(context, false);
        // Grids page
        OptionsPage grids_page = new OptionsPage(context, false);
        // Settings page
        OptionsPage settings_page = new OptionsPage(context, false);
        //// Options Item
        OptionsItem item_useUnipadFolder = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_CHECKBOX);
        item_useUnipadFolder.setTitle(context.getString(R.string.use_unipad_folder));
        item_useUnipadFolder.setDescription(context.getString(R.string.use_unipad_folder_subtitle));
        settings_page.putOption(item_useUnipadFolder);

        OptionsItem glows_switch_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        glows_switch_item.setTitle(context.getString(R.string.glow_title));
        glows_switch_item.setDescription(context.getString(R.string.glow_subtitle));
        settings_page.putOption(glows_switch_item);

        OptionsItem glow_configs_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        glow_configs_item.setTitle(context.getString(R.string.glow_cfg_title));
        glow_configs_item.setDescription(context.getString(R.string.glow_cfg_subtitle));
        settings_page.putOption(glow_configs_item);

        OptionsItem slider_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        slider_item.setTitle(context.getString(R.string.slide_mode));
        slider_item.setDescription(context.getString(R.string.slide_mode_subtitle));
        settings_page.putOption(slider_item);

        OptionsItem rec_autoplay_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        rec_autoplay_item.setTitle(context.getString(R.string.recordAutoplay));
        settings_page.putOption(rec_autoplay_item);

        OptionsItem layer_decoration_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        layer_decoration_item.setTitle(context.getString(R.string.layer_decoration));
        layer_decoration_item.setDescription(context.getString(R.string.layer_decoration_subtitle));
        layer_decoration_item.getCheckBox().setClickable(true);
        settings_page.putOption(layer_decoration_item);

        OptionsItem layer_decoration_configs_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        layer_decoration_configs_item.setTitle(context.getString(R.string.layer_cfg));
        layer_decoration_configs_item.setDescription(context.getString(R.string.layer_cfg_subtitle));
        settings_page.putOption(layer_decoration_configs_item);

        OptionsItem led_spam_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        led_spam_item.setTitle(context.getString(R.string.led_spam));
        led_spam_item.setDescription(context.getString(R.string.led_spam_subtitle));
        settings_page.putOption(led_spam_item);

        OptionsItem sound_spam_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        sound_spam_item.setTitle(context.getString(R.string.sound_spam));
        sound_spam_item.setDescription(context.getString(R.string.sounds_spam_subtitle));
        settings_page.putOption(sound_spam_item);

        item_useUnipadFolder.setOnClick((v) -> {
            if (GlobalConfigs.use_unipad_folder) {
                GlobalConfigs.app_configs.edit().putBoolean("useUnipadFolder", false).apply();
            } else {
                item_useUnipadFolder.setChecked(true);
                GlobalConfigs.app_configs.edit().putBoolean("useUnipadFolder", true).apply();
            }
        });

        // About page

        OptionsPage about_page = new OptionsPage(context, false);
        OptionsItem item_sourceCode = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_CHECKBOX);
        item_sourceCode.setTitle(context.getString(R.string.sourcecode));
        item_sourceCode.setDescription(context.getString(R.string.sourcecode_subtitle));
        about_page.putOption(item_sourceCode);

        OptionsItem item_myChannel = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_CHECKBOX);
        item_sourceCode.setTitle(context.getString(R.string.mychannel));
        item_sourceCode.setDescription(context.getString(R.string.mychannel_subtitle));
        about_page.putOption(item_myChannel);

        OptionsItem item_manual = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_CHECKBOX);
        item_manual.setTitle(context.getString(R.string.manual));
        item_manual.setDescription(context.getString(R.string.manual_subtitle));
        about_page.putOption(item_manual);

        OptionsItem item_genLogFile = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_CHECKBOX);
        item_genLogFile.setTitle(context.getString(R.string.item_gen_log_file));
        item_genLogFile.setDescription(context.getString(R.string.item_gen_log_file_subtitle));
        about_page.putOption(item_genLogFile);

        OptionsItem item_crash = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE_WITH_CHECKBOX);
        item_crash.setTitle(context.getString(R.string.item_crash_app));
        item_crash.setDescription(context.getString(R.string.item_crash_app_subtitle));
        about_page.putOption(item_crash);

        item_sourceCode.setOnClick((v) -> {
            Intent source_code_page = new Intent(Intent.ACTION_VIEW);
            source_code_page.setData(Uri.parse("https://github.com/XayUp/MultiPad"));
            context.startActivity(source_code_page);
        });
        item_myChannel.setOnClick((v) -> {
            Intent my_channel_page = new Intent(Intent.ACTION_VIEW);
            my_channel_page.setData(
                    Uri.parse("https://youtube.com/channel/UCQUG1PVbnmIIYRDbC-qYTqA"));
            context.startActivity(my_channel_page);
        });
        item_manual.setOnClick((v) -> {
            AlertDialog.Builder manual =
                    new AlertDialog.Builder(
                            context, R.style.alertdialog_transparent);
            ImageView manualImg = new ImageView(context);
            manualImg.setImageDrawable(context.getDrawable(R.drawable.manual));
            manual.setView(manualImg);
            Dialog show_manual = manual.create();
            XayUpFunctions.showDiagInFullscreen(show_manual);
            manualImg.setOnClickListener((view) -> show_manual.dismiss());
        });
        item_genLogFile.setOnClick((v) -> {
            try {
                File log_share = new File(GlobalConfigs.DefaultConfigs.MULTIPAD_PATH, "MultiPad_log.txt");
                if(log_share.exists()) log_share.delete(); else log_share.getParentFile().mkdirs();
                log_share.createNewFile();
                FileOutputStream fos = new FileOutputStream(log_share);
                fos.write(Debug.genAppLog().toString().getBytes());
                fos.close();
                Toast.makeText(context, "Save on MultiPad path. Please send it to @XayUp", Toast.LENGTH_LONG).show();
                // Share
                /*
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/*");
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse(log_share.getPath()));
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(share, "Please send it to @XayUp"));
                 */
            } catch (IOException e) {
                Toast.makeText(context, "Error generating log file", Toast.LENGTH_SHORT).show();
            }
        });

        // DEBUG //
        item_crash.setOnClick(v -> { throw new RuntimeException("Crash app (TEST)"); });

        panel = LayoutInflater.from(context).inflate(R.layout.main_panel, null);
        ScrollView right_scroll = panel.findViewById(R.id.main_panel_right_scroll);

        //PROJECTS
        (project_tab = panel.findViewById(R.id.main_panel_tab_projects)).setOnClickListener((v)->{
            hideThis(right_scroll);
            right_scroll.removeAllViews();
            right_scroll.addView(projects_page.getPageView());
            projects_page.clear();
            ViewGroup projects_page_layout = (ViewGroup) projects_page.getPageView();
            for(int i = 0; i < mProjectItem.getCount(); i++) {
                View item = mProjectItem.getView(i, null, null);
                projects_page_layout.addView(item);
                View description_view = item.findViewById(R.id.project_item_background);
                if(mProjectItem.getItem(i).getProject().getStatus() != Project.STATUS_BROKEN) {
                    description_view.setOnClickListener(view -> {
                        View descriptions = item.findViewById(R.id.project_item_description_background);
                        vAnimatior = (ViewDraw.getMargin(descriptions, ViewDraw.MARGIN_TOP) > 0) ?
                                ValueAnimator.ofInt(view.getLayoutParams().height, 0) :
                                ValueAnimator.ofInt(0, view.getLayoutParams().height);
                        vAnimatior.addUpdateListener((valueAnimator -> ViewDraw.setMargins(descriptions, 0, (int) valueAnimator.getAnimatedValue(), 0, 0)));
                        vAnimatior.setDuration((long) (500 * Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)));
                        vAnimatior.start();
                        View play = descriptions.findViewById(R.id.project_item_description_play);
                        play.setOnClickListener((view_play) -> loadProject(
                                mProjectItem.getItem(projects_page_layout.indexOfChild(item)), view.findViewById(R.id.project_item_progress)));
                    });
                } else {
                    // A descrição do projeto é inútil se o projeto estiver quebrado, então é melhor remover
                    projects_page_layout.removeView(description_view);
                }
            }
            showThis(right_scroll);
        });

        //SKINS
        panel.findViewById(R.id.main_panel_tab_skins).setOnClickListener((v)->{
            hideThis(right_scroll);
            right_scroll.removeAllViews();
            right_scroll.addView(skins_page.getPageView());
            skins_page.clear();
            // UPDATE LIST
            mSkinAdapter.updateList();
            while(mSkinAdapter.getCount() > 0){
                SkinProperties skin_properties = mSkinAdapter.remove(0);
                OptionsItem item = new OptionsItem(context, OptionsItem.TYPE_CENTER_WITH_IMAGE);
                item.setTitle(skin_properties.name);
                item.setDescription(skin_properties.author);
                item.setTag(skin_properties);
                item.setImg(skin_properties.icon);
                item.setOnClick((view)-> {
                    FluctuateOptionsView window = new FluctuateOptionsView(context);
                    window.getBackButton().setVisibility(View.GONE);
                    OptionsPage page = window.getPage(window.newPage(context.getString(R.string.list_skin_apply_title)));
                    OptionsItem apply_for_all = new OptionsItem(context, OptionsItem.TYPE_SIMPLE);
                    apply_for_all.setTitle(context.getString(R.string.list_skin_apply_for_all));
                    apply_for_all.setOnClick((view1)->{
                        List<GridPadsReceptor.PadGrid> list_pads = getPadInstance().getAllPadsList();
                        while(!list_pads.isEmpty()) {
                            list_pads.remove(0).applySkin(SkinManager.getSkinProperties(context, skin_properties.package_name));
                        }
                    });
                    List<GridPadsReceptor.PadGrid> list_pads = getPadInstance().getAllPadsList();
                    while(!list_pads.isEmpty()) {
                        GridPadsReceptor.PadGrid padGrid = list_pads.remove(0);
                        OptionsItem pads_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE);
                        pads_item.setTitle(padGrid.getName());
                        pads_item.setTag(padGrid);
                        page.putOption(pads_item);
                        pads_item.setOnClick((view1)->{
                            padGrid.applySkin(SkinManager.getSkinProperties(context, skin_properties.package_name));
                        });
                    }

                    window.show();
                });
                skins_page.putOption(item);
            }
            showThis(right_scroll);
        });

        //GRIDS
        panel.findViewById(R.id.main_panel_tab_grids).setOnClickListener(v -> {
            hideThis(right_scroll);
            right_scroll.removeAllViews();
            right_scroll.addView(grids_page.getPageView());
            grids_page.clear();
            List<GridPadsReceptor.PadGrid> padGrids = getPadInstance().getAllPadsList();
            OptionsItem add_new = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_ARROW);
            add_new.setTitle("+ Add new");
            ((ImageView) add_new.getItemView().findViewById(com.xayup.ui.R.id.options_item_identify_arrow)).setImageResource(R.drawable.icon_plus);
            add_new.setOnClick(add_new_view -> {
                addNewGrid();
                v.callOnClick(); });
            grids_page.putOption(add_new);
            for(GridPadsReceptor.PadGrid padGrid : padGrids){
                OptionsItem item = new OptionsItem(context, OptionsItemInterface.TYPE_CENTER_WITH_IMAGE);
                item.setTitle(padGrid.getName());
                item.setDescription(String.valueOf(padGrid.getId()));
                item.setOnClick(grid_item -> onGriditemClick(padGrid));
                switch(padGrid.getLayoutMode()){
                    case GridPadsReceptor.PadLayoutMode.LAYOUT_PRO_MODE: {
                        item.setImg(context.getDrawable(R.drawable.lp_pro));
                        break;
                    }
                    case GridPadsReceptor.PadLayoutMode.LAYOUT_MK2_MODE: {
                        item.setImg(context.getDrawable(R.drawable.lp_mk2));
                        break;
                    }
                    case GridPadsReceptor.PadLayoutMode.LAYOUT_UNIPAD_MODE: {
                        item.setImg(context.getDrawable(R.drawable.lp_pro_mk3));
                        break;
                    }
                    case GridPadsReceptor.PadLayoutMode.LAYOUT_MATRIX_MODE: {
                        item.setImg(context.getDrawable(R.drawable.lp_x));
                        break;
                    }
                }
                grids_page.putOption(item);
            }
            showThis(right_scroll);
        });

        panel.findViewById(R.id.main_panel_tab_settings).setOnClickListener((v)->{
            hideThis(right_scroll);
            right_scroll.removeAllViews();
            right_scroll.addView(settings_page.getPageView());
            showThis(right_scroll);
        });

        panel.findViewById(R.id.main_panel_tab_about).setOnClickListener((v)->{
            hideThis(right_scroll);
            right_scroll.removeAllViews();
            right_scroll.addView(about_page.getPageView());
            showThis(right_scroll);
        });

        panel.findViewById(R.id.main_panel_tab_exit).setOnClickListener(v -> {
            onExit();
        });

        // LEFT OPTIONS
        panel.findViewById(R.id.main_panel_left_grid_resize_background).setOnClickListener(v -> {
            ((CheckBox) panel.findViewById(R.id.main_panel_left_grid_resize_checkbox)).setChecked(
                    !GlobalConfigs.floating_window_grid_resize_visible );
            showGridResize(!GlobalConfigs.floating_window_grid_resize_visible);
        });

        AlertDialog.Builder window = new AlertDialog.Builder(context);
        window.setView(panel);
        windowShow = window.create();
        windowShow.getWindow().setGravity(Gravity.END);
        windowShow.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        windowShow.setOnCancelListener(dialogInterface -> home());
    }

    public static class ViewDraw {

        public static final int MARGIN_LEFT = 0;
        public static final int MARGIN_TOP = 1;
        public static final int MARGIN_RIGHT = 2;
        public static final int MARGIN_BOTTOM = 3;

        /**
         * <a href=https://stackoverflow.com/a/4472612>Original code from here</a>
         * @param v View
         * @param l Left margin (px)
         * @param t Top margin (px)
         * @param r Right margin (px)
         * @param b Bottom margin (px)
         */
        public static void setMargins (View v, int l, int t, int r, int b) {
            if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                p.setMargins(l, t, r, b);
                v.requestLayout();
            }
        }

        /**
         * @param v View
         * @param type Get margin from
         * @return View margin, else -1.
         */
        public static int getMargin (View v, int type) {
            if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                switch (type){
                    case MARGIN_LEFT: { return ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).getMarginStart(); }
                    case MARGIN_TOP: { return ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).topMargin; }
                    case MARGIN_RIGHT: { return ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).getMarginEnd(); }
                    case MARGIN_BOTTOM: { return ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).bottomMargin; }
                }
            }
            return -1;
        }
    }

    public void home(){ project_tab.callOnClick(); }

    protected void hideThis(View view){
        view.startAnimation(out);
        view.setVisibility(View.INVISIBLE);
    }
    protected void showThis(View view){
        view.startAnimation(in);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Use this after obtained data's
     */
    public void updates(){
        // UPDATE LIST
        mProjectItem = new ProjectListAdapter(context, getProjects());
    }

    public void updateOptions(){
        ((CheckBox) panel.findViewById(R.id.main_panel_left_grid_resize_checkbox)).setChecked(GlobalConfigs.floating_window_grid_resize_visible);
    }

    public void showPanel(){
        updateOptions();
        windowShow.show();
        windowShow.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }
}
