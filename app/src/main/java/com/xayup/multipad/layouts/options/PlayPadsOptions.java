package com.xayup.multipad.layouts.options;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xayup.debug.XLog;
import com.xayup.multipad.R;
import com.xayup.multipad.XayUpFunctions;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.pads.GridPadsReceptor;
import com.xayup.multipad.projects.project.keyled.KeyLED;
import com.xayup.multipad.skin.SkinAdapter;
import com.xayup.multipad.skin.SkinManager;
import com.xayup.multipad.skin.SkinProperties;
import com.xayup.ui.options.FluctuateOptionsView;
import com.xayup.ui.options.OptionsItem;
import com.xayup.ui.options.OptionsItemInterface;
import com.xayup.ui.options.OptionsPage;

import java.io.File;
import java.util.List;

public abstract class PlayPadsOptions extends FluctuateOptionsView {
        /*Identify*/
    /*Activity action*/
    private final byte PICK_PHANTOM_IMG = 0;
    private final byte PICK_PHANTOM__IMG = 1;
    private final byte PICK_CHAIN_IMG = 2;
    private final byte PICK_LOGO_IMG = 3;
    private final byte PICK_BACKGROUND_IMG = 4;
    private final byte PICK_BTN_IMG = 5;
    private final byte PICK_LOGO_BG_IMG = 6;

    /*Page index*/
    final byte EXIT_PAGE_LISTSKIN = 0;
    final byte EXIT_PAGE_CONFIGS = 1;
    final byte EXIT_PAGE_LIST_COLOR_TABLE = 2;
    final byte EXIT_PAGE_PAD_GRIDS = 3;

    protected Activity context;
    protected SkinAdapter mSkinAdapter;

    /*Abstracts*/
    public abstract void onExit();
    public abstract KeyLED getKeyLEDInstance();
    public abstract GridPadsReceptor getPadInstance();

    /*Pages*/
    public OptionsPage skin_page;

    /*Main Buttons*/
    protected Button exit;
    protected Button configs;
    protected Button color_table;
    protected Button pads_list;
    protected Button new_pads;

    @Override
    public void show(){
        defaultScreen();
        XayUpFunctions.showDiagInFullscreen(mAlertDialog);
        mAlertDialog
                .getWindow()
                .setLayout(GlobalConfigs.display_width/2, WindowManager.LayoutParams.MATCH_PARENT);
        mAlertDialog.getWindow().setGravity(Gravity.END);
    }

    /**
     * Just to "Reset" the view to the main page, when the menu is opened
     */
    public void defaultScreen(){
        updateSkinList();
        exit.setVisibility(View.VISIBLE);
        configs.setVisibility(View.VISIBLE);
        color_table.setVisibility(View.VISIBLE);
        pads_list.setVisibility(View.VISIBLE);
        new_pads.setVisibility(View.GONE);
        flipper.setInAnimation(null);
        flipper.setOutAnimation(null);
        flipper.setDisplayedChild(0);
    }

    /**
     * Update the list skins
     */
    protected void updateSkinList(){
        mSkinAdapter.updateList();
        skin_page.clear();
        while(mSkinAdapter.getCount() > 0){
            SkinProperties skin_properties = mSkinAdapter.remove(0);
            OptionsItem item = new OptionsItem(context, OptionsItem.TYPE_CENTER_WITH_IMAGE);
            item.setTitle(skin_properties.name);
            item.setDescription(skin_properties.author);
            item.setTag(skin_properties);
            item.setImg(skin_properties.icon);
            item.setOnClick((v)-> {
                FluctuateOptionsView window = new FluctuateOptionsView(context);
                window.getBackButton().setVisibility(View.GONE);
                OptionsPage page = window.getPage(window.newPage(context.getString(R.string.list_skin_apply_title)));
                OptionsItem apply_for_all = new OptionsItem(context, OptionsItem.TYPE_SIMPLE);
                apply_for_all.setTitle(context.getString(R.string.list_skin_apply_for_all));
                apply_for_all.setOnClick((view)->{
                    List<GridPadsReceptor.PadGrid> list_pads = getPadInstance().getAllPadsList();
                    while(!list_pads.isEmpty()) {
                        list_pads.remove(0).applySkin(SkinManager.getSkinProperties(context, skin_properties.package_name));
                    }
                    list_pads = null;
                });
                List<GridPadsReceptor.PadGrid> list_pads = getPadInstance().getAllPadsList();
                while(!list_pads.isEmpty()) {
                    GridPadsReceptor.PadGrid padGrid = list_pads.remove(0);
                    OptionsItem pads_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE);
                    pads_item.setTitle(padGrid.getName());
                    pads_item.setTag(padGrid);
                    page.putOption(pads_item);
                    pads_item.setOnClick((view)->{
                        padGrid.applySkin(SkinManager.getSkinProperties(context, skin_properties.package_name));
                    });
                }

                window.show();
            });
            skin_page.putOption(item);
        }
    }

    public PlayPadsOptions(Activity context) {
        super(context);
        this.context = context;
        this.mSkinAdapter = new SkinAdapter(context);

        /*Main Buttons*/
        (this.exit = new Button(context)).setBackground(context.getDrawable(R.drawable.exit_button));
        (this.configs = new Button(context)).setBackground(context.getDrawable(R.drawable.icon_settings));
        (this.color_table = new Button(context)).setBackground(context.getDrawable(R.drawable.ic_color_table));
        (this.pads_list = new Button(context)).setBackground(context.getDrawable(R.drawable.pad_grids));
        (this.new_pads = new Button(context)).setBackground(context.getDrawable(R.drawable.icon_plus));
        
        addViewToBottomBar(configs);
        addViewToBottomBar(color_table);
        addViewToBottomBar(pads_list);
        addViewToBottomBar(new_pads);
        /*Separator*/
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(-1, 1);
        param.weight = 1f;
        addViewToBottomBar(new View(context), param);

        addViewToBottomBar(exit);

        /*Pages*/
        // Select Skin page
        skin_page = new OptionsPage(context, true);
        skin_page.setTitle(context.getString(R.string.skins));

        // Option page
        OptionsPage options_page = new OptionsPage(context, true);
        options_page.setTitle(context.getString(R.string.alert_exit_options));

        // Color table page
        OptionsPage color_table_page = new OptionsPage(context, true);
        color_table_page.setTitle(context.getString(R.string.color_table_title));

        // Grid pads page
        OptionsPage grid_pads_page = new OptionsPage(context, true);
        grid_pads_page.setTitle(context.getString(R.string.grid_pads_title));


        /*Options Item*/
        OptionsItem glows_switch_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        glows_switch_item.setTitle(context.getString(R.string.glow_title));
        glows_switch_item.setDescription(context.getString(R.string.glow_subtitle));

        OptionsItem glow_configs_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        glow_configs_item.setTitle(context.getString(R.string.glow_cfg_title));
        glow_configs_item.setDescription(context.getString(R.string.glow_cfg_subtitle));

        OptionsItem slider_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        slider_item.setTitle(context.getString(R.string.slide_mode));
        slider_item.setDescription(context.getString(R.string.slide_mode_subtitle));

        OptionsItem rec_autoplay_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        rec_autoplay_item.setTitle("Record autoplay");
        rec_autoplay_item.setDescription("slaslasla");

        OptionsItem layer_decoration_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        layer_decoration_item.setTitle(context.getString(R.string.layer_decoration));
        layer_decoration_item.setDescription(context.getString(R.string.layer_decoration_subtitle));
        layer_decoration_item.getCheckBox().setClickable(true);

        OptionsItem layer_decoration_configs_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        layer_decoration_configs_item.setTitle(context.getString(R.string.layer_cfg));
        layer_decoration_configs_item.setDescription(context.getString(R.string.layer_cfg_subtitle));

        OptionsItem led_spam_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        led_spam_item.setTitle(context.getString(R.string.led_spam));
        led_spam_item.setDescription(context.getString(R.string.led_spam_subtitle));

        OptionsItem sound_spam_item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE_WITH_CHECKBOX);
        sound_spam_item.setTitle(context.getString(R.string.sound_spam));
        sound_spam_item.setDescription(context.getString(R.string.sounds_spam_subtitle));

        /*Add items to respective pages*/
        options_page.putOption(glows_switch_item);
        options_page.putOption(glow_configs_item);
        options_page.putOption(slider_item);
        options_page.putOption(rec_autoplay_item);
        options_page.putOption(layer_decoration_item);
        options_page.putOption(layer_decoration_configs_item);
        options_page.putOption(led_spam_item);
        options_page.putOption(sound_spam_item);

        /*Add pages to flipper*/
        addPage(skin_page);
        addPage(options_page);
        addPage(color_table_page);
        addPage(grid_pads_page);

        /*Set checkbox changes*/
        glows_switch_item.getCheckBox().setOnCheckedChangeListener((checkbox, check)-> glow_configs_item.setEnabled(check));
        layer_decoration_item.getCheckBox().setOnCheckedChangeListener((checkbox, check)-> layer_decoration_configs_item.setEnabled(check));

        /*Restore saved from app configs*/
        glow_configs_item.setChecked(GlobalConfigs.PlayPadsConfigs.glow_cfg_visible);
        glows_switch_item.setChecked(GlobalConfigs.PlayPadsConfigs.glow_enabled);
        sound_spam_item.setChecked(GlobalConfigs.PlayPadsConfigs.spamSounds);
        layer_decoration_item.setChecked(GlobalConfigs.PlayPadsConfigs.layer_decoration);
        rec_autoplay_item.setChecked(GlobalConfigs.PlayPadsConfigs.recAutoplay);
        slider_item.setChecked(GlobalConfigs.PlayPadsConfigs.slideMode);

        slider_item.setOnClick((v)->{
            slider_item.getCheckBox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    GlobalConfigs.PlayPadsConfigs.slideMode = b;
                    XLog.v("Slide Checkbox Click", String.valueOf(b));
                }
            });
        });

        /*Switch to Options Page*/
        configs.setOnClickListener((view) -> {
            configs.setVisibility(View.GONE);
            color_table.setVisibility(View.GONE);
            pads_list.setVisibility(View.GONE);
            //switchTo(EXIT_PAGE_CONFIGS, false);
        });

        /*Back Button*/
        getBackButton().setOnClickListener((View arg0) -> {
            switch ((byte) getCurrentPageIndex()) {
                case EXIT_PAGE_CONFIGS:
                case EXIT_PAGE_LIST_COLOR_TABLE:
                case EXIT_PAGE_PAD_GRIDS:
              //      switchTo(EXIT_PAGE_LISTSKIN, true);
                    color_table.setVisibility(View.VISIBLE);
                    configs.setVisibility(View.VISIBLE);
                    pads_list.setVisibility(View.VISIBLE);
                    new_pads.setVisibility(View.GONE);
                    break;
                default: break;
            }
        });

        /*Back to the Unipacks list*/
        exit.setOnClickListener((view) -> onExit());

        /*View Pads list*/
        pads_list.setOnClickListener((v) -> {
            configs.setVisibility(View.GONE);
            color_table.setVisibility(View.GONE);
            pads_list.setVisibility(View.GONE);
            new_pads.setVisibility(View.VISIBLE);
            grid_pads_page.clear();
            List<GridPadsReceptor.PadGrid> padGrid_list = getPadInstance().getAllPadsList();
            while(!padGrid_list.isEmpty()){
                GridPadsReceptor.PadGrid padGrid = padGrid_list.remove(0);
                OptionsItem item = new OptionsItem(context, OptionsItem.TYPE_SIMPLE);
                item.setTitle(padGrid.getName());
                item.setDescription(String.valueOf(padGrid.getId()));
                item.setOnClick(padsConfigItemOnClick(padGrid));
                grid_pads_page.putOption(item);
            }
           // switchTo(EXIT_PAGE_PAD_GRIDS, false);
        });

        /*Add bottom buttons*/
        new_pads.setOnClickListener((new_pads_button)->{
            //getPadInstance().newPads(GlobalConfigs.PlayPadsConfigs.skin_package, 10, 10);
            OptionsItem new_item = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
            new_item.setTitle(getPadInstance().getActivePads().getName());
            new_item.setDescription(String.valueOf(getPadInstance().getActivePads().getId()));
            new_item.setOnClick(padsConfigItemOnClick(getPadInstance().getActivePads()));
            grid_pads_page.putOption(new_item);
        });

        // Button color_table
        color_table.setOnClickListener((view) -> {
            configs.setVisibility(View.GONE);
            color_table.setVisibility(View.GONE);
            pads_list.setVisibility(View.GONE);
            color_table_page.clear();
            // switchTo(EXIT_PAGE_LIST_COLOR_TABLE, false);
            File root = new File(GlobalConfigs.DefaultConfigs.COLOR_TABLE_PATH);
            if(root.exists()) for(File file : root.listFiles()){
                OptionsItem item = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
                byte format_index = (byte) file.getName().lastIndexOf(".");
                item.setTitle((format_index == -1) ? file.getName() : file.getName().replace(file.getName().substring(format_index), ""));
                item.setOnClick((view1)->{
                    XLog.v("Color Table name", file.getName());
                    //getKeyLEDInstance().setColorTable(file);
                });
                color_table_page.putOption(item);
            }
        });

        // Options page functions
        // sound spam
        sound_spam_item.setOnClick((view) -> {
            if (!GlobalConfigs.PlayPadsConfigs.spamSounds) {
                AlertDialog.Builder spam_alert = new AlertDialog.Builder(context);
                spam_alert.setCancelable(false);
                spam_alert.setMessage(R.string.sound_spam_dialog);
                spam_alert.setPositiveButton(
                        R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                sound_spam_item.setChecked(GlobalConfigs.PlayPadsConfigs.spamSounds);
                            }
                        });
                spam_alert.setNegativeButton(R.string.no, null);
                spam_alert.create().show();
            }
        });

        // Show glow effect
        glows_switch_item.setOnClick((view) -> {
            if (!GlobalConfigs.PlayPadsConfigs.glow_enabled) {
                glows_switch_item.setChecked(true);
                glow_configs_item.setEnabled(true);
                /*

                MAKE GLOW HERE

                */
            }
            GlobalConfigs.app_configs.edit().putBoolean("glowEf", GlobalConfigs.PlayPadsConfigs.glow_enabled);
        });

        /*Change layer decoration image*/
        layer_decoration_item.setOnClick((view) -> {
            if (layer_decoration_item.getCheckBox().isChecked()) {
                int PICK_IMG = 12;
                Intent get_image_from_gallery =
                        new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media
                                        .EXTERNAL_CONTENT_URI);
                context.startActivityForResult(get_image_from_gallery, PICK_IMG);
            }
        });

        /*Enable layer decoration*/
        layer_decoration_item.getCheckBox().setOnCheckedChangeListener(
        (CompoundButton arg0, boolean arg1) -> {
            View window = context.findViewById(R.id.layer_cfg_window);
            if (!arg1) {
                window.setVisibility(View.GONE);
                GlobalConfigs.PlayPadsConfigs.layer_decoration = false;
                context.findViewById(R.id.launchpadOverride).setVisibility(View.GONE);
                layer_decoration_item.setEnabled(false);
                context.findViewById(R.id.layer_decoration_scale_background).setVisibility(View.GONE);
            } else {
                GlobalConfigs.PlayPadsConfigs.layer_decoration = true;
                ImageView layer = context.findViewById(R.id.launchpadOverride);
                layer.getLayoutParams().height = GlobalConfigs.display_height;
                layer.getLayoutParams().width = GlobalConfigs.display_width;
                layer.setVisibility(View.VISIBLE);
                window.setVisibility(View.VISIBLE);
                layer_decoration_item.setEnabled(true);
            }
        });

        /*Led spam*/
        led_spam_item.setOnClick((view) -> {
            if (GlobalConfigs.PlayPadsConfigs.spamLeds) {
                GlobalConfigs.PlayPadsConfigs.spamLeds = false;
                led_spam_item.setChecked(GlobalConfigs.PlayPadsConfigs.spamSounds);
            } else {
                led_spam_item.setChecked(false);
                AlertDialog.Builder spam_alert = new AlertDialog.Builder(context);
                spam_alert.setCancelable(false);
                spam_alert.setMessage(R.string.led_spam_dialog);
                spam_alert.setPositiveButton(
                        R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                GlobalConfigs.PlayPadsConfigs.spamLeds = true;
                                led_spam_item.setChecked(GlobalConfigs.PlayPadsConfigs.spamLeds);
                            }
                        });
                spam_alert.setNegativeButton(R.string.no, null);
                spam_alert.create().show();
            }
        });

        /*Create AlertDialog. Get with 'mAlertDialog' */
        create();
    }

    protected View.OnClickListener padsConfigItemOnClick(GridPadsReceptor.PadGrid padGrid){
        return (view)->{
            FluctuateOptionsView pad_op = new FluctuateOptionsView(context);
            OptionsPage pad_op_page = pad_op.getPage(pad_op.newPage(context.getString(R.string.alert_exit_options)));

            /*Page UI definitions*/
            pad_op.setTitle(padGrid.getName());
            pad_op.getBackButton().setVisibility(View.GONE);

            /*Create and Place Pads Options*/
            OptionsItem delete = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
            delete.setTitle("Delete");

            OptionsItem rename = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
            rename.setTitle("Rename");

            OptionsItem clone = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
            clone.setTitle("Clone");

            OptionsItem use_id = new OptionsItem(context, OptionsItemInterface.TYPE_SIMPLE);
            use_id.setTitle("Set id");

            pad_op_page.putOption(use_id);
            pad_op_page.putOption(rename);
            pad_op_page.putOption(clone);
            pad_op_page.putOption(delete);

            /*Set items functions*/
            delete.setOnClick((item_view)->{});
            rename.setOnClick((item_view)->{});
            clone.setOnClick((item_view)->{});
            use_id.setOnClick((item_view)->{});

            pad_op.show();
        };
    }
}
