package com.xayup.multipad.layouts.options;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.*;
import android.icu.math.BigDecimal;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;
import android.widget.ViewFlipper;

import com.xayup.multipad.MakeGlows;
import com.xayup.multipad.R;
import com.xayup.multipad.Readers;
import com.xayup.multipad.XayUpFunctions;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.layouts.loadscreen.LoadScreen;
import com.xayup.multipad.load.Project;
import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.pads.Pad;
import com.xayup.multipad.pads.PadInteraction;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.ui.options.FluctuateOptionsView;
import com.xayup.ui.options.OptionsItem;
import com.xayup.ui.options.OptionsPage;

import java.io.*;
import java.util.*;

public abstract class PlayPadsOptions extends GlobalConfigs.PlayPadsConfigs {

    private final int PICK_PHANTOM_IMG = 0;
    private final int PICK_PHANTOMC_IMG = 1;
    private final int PICK_CHAIN_IMG = 2;
    private final int PICK_LOGO_IMG = 3;
    private final int PICK_BACKGROUND_IMG = 4;
    private final int PICK_BTN_IMG = 5;
    private final int PICK_LOGO_BG_IMG = 6;

    private final int EXIT_PAGE_LISTSKIN = 0;
    private final int EXIT_PAGE_CONFIGS = 1;
    private final int EXIT_PAGE_UI_IMG_SELECTOR = 2;
    private final int EXIT_PAGE_LIST_COLOR_TABLE = 3;
    private final int EXIT_PAGE_PAD_GRIDS = 4;

    protected Activity context;
    protected AlertDialog alertDialog;

    private String resizeLayer(final View layer, Double increment) {
        float new_size =
                BigDecimal.valueOf(layer.getScaleY())
                        .add(BigDecimal.valueOf(increment))
                        .floatValue();
        layer.setScaleY(new_size);
        layer.setScaleX(new_size);
        layer.setVisibility(View.GONE);
        layer.setVisibility(View.VISIBLE);
        return String.valueOf(new_size);
    }

    public void switShowNext(ViewFlipper swit) {
        swit.setInAnimation(context, R.anim.move_in_to_left);
        swit.setOutAnimation(context, R.anim.move_out_to_left);
        swit.showNext();
    }

    public void switShowbyIndex(ViewFlipper swit, boolean Previous, int page) {
        if (Previous) {
            swit.setInAnimation(context, R.anim.move_in_to_right);
            swit.setOutAnimation(context, R.anim.move_out_to_right);
        } else {
            swit.setInAnimation(context, R.anim.move_in_to_left);
            swit.setOutAnimation(context, R.anim.move_out_to_left);
        }
        swit.setDisplayedChild(page);
    }

    public abstract void onExit();    
    public void show(){
        XayUpFunctions.showDiagInFullscreen(alertDialog);
        alertDialog
                .getWindow()
                .setLayout(GlobalConfigs.display_height, WindowManager.LayoutParams.MATCH_PARENT);
        alertDialog.getWindow().setGravity(Gravity.RIGHT);
    }

    public PlayPadsOptions(Activity context) {
        this.context = context;
        FluctuateOptionsView mFlutuantOptions = new FluctuateOptionsView(context);
        OptionsPage page = mFlutuantOptions.getPage(mFlutuantOptions.newPage(context.getString(R.string.alert_exit_title)));
        OptionsItem item = new OptionsItem(context);
        item.setTitle();

        // Layout
        View onExitDialog = context.getLayoutInflater().inflate(R.layout.alertexit_dialog, null);
        // Botões principais
        RelativeLayout onExitButton = onExitDialog.findViewById(R.id.alertExitButtonExit);
        Button exit_config = onExitDialog.findViewById(R.id.alert_configs_buttom);
        Button prev = onExitDialog.findViewById(R.id.alertExit_prev);
        Button color_table = onExitDialog.findViewById(R.id.alert_color_table_buttom);
        Button pad_grids = onExitDialog.findViewById(R.id.alert_exit_pad_grids);
        // Select Skin page
        ListView listSkins = onExitDialog.findViewById(R.id.alertExitListSkins);
        TextView barTitle = onExitDialog.findViewById(R.id.alertExitTitle);
        ViewFlipper swit = onExitDialog.findViewById(R.id.exitMenuSwitcher);

        // Color table page
        ListView color_table_files = onExitDialog.findViewById(R.id.alertExit_list_color_table);
        Button default_color_table =
                onExitDialog.findViewById(R.id.alert_default_color_table_buttom);

        // Grid pads page
        ListView pad_grids_list = onExitDialog.findViewById(R.id.alert_exit_pad_grids_list);

        // Option page
        View sound_spam_item = onExitDialog.findViewById(R.id.alertExit_spam_sound_item);
        CheckBox sound_spam = onExitDialog.findViewById(R.id.soundSpam);

        View hide_buttoms = onExitDialog.findViewById(R.id.alertExit_hide_buttoms);
        CheckBox hide_check = onExitDialog.findViewById(R.id.hide_buttoms_check);

        View alertExit_layout_decoration_item =
                onExitDialog.findViewById(R.id.alertExit_layer_decoration_item);
        CheckBox decoration_show = onExitDialog.findViewById(R.id.layer_decoration_check);

        View ui_img_select = onExitDialog.findViewById(R.id.alertExit_ui_img_select_item);

        View glowShow = onExitDialog.findViewById(R.id.alertExit_glow_item);
        CheckBox glow_check = onExitDialog.findViewById(R.id.glow_check);

        View glow_cfg_show = onExitDialog.findViewById(R.id.alertExit_glow_cfg_item);
        CheckBox glow_cfg_check = onExitDialog.findViewById(R.id.glow_cfg_check);

        View led_spam_item = onExitDialog.findViewById(R.id.alertExit_spam_led_item);
        CheckBox led_spam_check = onExitDialog.findViewById(R.id.ledSpam);

        View slide_mode = onExitDialog.findViewById(R.id.alertExit_slide_mode);
        CheckBox slide_check = onExitDialog.findViewById(R.id.slide_mode_check);

        View rec_autoplay = onExitDialog.findViewById(R.id.alertExit_rec_autoplay);
        CheckBox rec_ap_check = onExitDialog.findViewById(R.id.rec_autoplay_check);

        View old_colors = onExitDialog.findViewById(R.id.alertExit_old_colors);
        CheckBox old_colors_check = onExitDialog.findViewById(R.id.old_colors_check);

        View item_customHeight = onExitDialog.findViewById(R.id.alertExit_item_layer_cfg);

        // Restore saved from app configs
        if (glow_enabled) glow_cfg_show.setAlpha(1.0f);

        glow_cfg_check.setChecked(glow_cfg_visible);
        glow_check.setChecked(glow_enabled);
        sound_spam.setChecked(spamSounds);
        hide_check.setChecked(hide_buttoms_b);
        decoration_show.setChecked(layer_decoration);
        rec_ap_check.setChecked(recAutoplay);
        slide_check.setChecked(slideMode);

        if (layer_decoration) alertExit_layout_decoration_item.setAlpha(1.0f);
        item_customHeight.setAlpha(1.0f);
        // Ir para a pagina de configuraçoes
        exit_config.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        barTitle.setText(context.getString(R.string.alert_exit_options));
                        exit_config.setVisibility(View.GONE);
                        switShowbyIndex(swit, false, EXIT_PAGE_CONFIGS);
                    }
                });
        // botao voltar
        prev.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        switch (swit.getDisplayedChild()) {
                            case EXIT_PAGE_LISTSKIN:
                                break;
                            case EXIT_PAGE_CONFIGS:
                                switShowbyIndex(swit, true, EXIT_PAGE_LISTSKIN);
                                barTitle.setText(context.getString(R.string.alert_exit_title));
                                exit_config.setVisibility(View.VISIBLE);
                                break;
                            case EXIT_PAGE_UI_IMG_SELECTOR:
                                switShowbyIndex(swit, true, EXIT_PAGE_CONFIGS);
                                barTitle.setText(context.getString(R.string.alert_exit_options));
                                break;
                            case EXIT_PAGE_LIST_COLOR_TABLE:
                                switShowbyIndex(swit, true, EXIT_PAGE_LISTSKIN);
                                barTitle.setText(context.getString(R.string.alert_exit_title));
                                color_table.setVisibility(View.VISIBLE);
                                default_color_table.setVisibility(View.GONE);
                                exit_config.setVisibility(View.VISIBLE);
                                break;
                            case EXIT_PAGE_PAD_GRIDS:
                                switShowbyIndex(swit, true, EXIT_PAGE_LISTSKIN);
                                barTitle.setText(context.getString(R.string.alert_exit_title));
                                color_table.setVisibility(View.VISIBLE);
                                exit_config.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                });

        // Voltar para a lista de Unipacks
        onExitButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onExit();
                    }
                });
        // Ver a lista de pad grids
        pad_grids.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        exit_config.setVisibility(View.GONE);
                        color_table.setVisibility(View.GONE);
                    }
                });

        // Botao color_table
        color_table.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        barTitle.setText(context.getString(R.string.color_table_title));
                        color_table.setVisibility(View.GONE);
                        default_color_table.setVisibility(View.VISIBLE);
                        switShowbyIndex(swit, false, EXIT_PAGE_LIST_COLOR_TABLE);
                        color_table_files.setAdapter(Readers.listColorTable(context));
                        color_table_files.setOnItemClickListener(
                                new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(
                                            AdapterView<?> adapter, View view, int pos, long id) {}
                                });
                    }
                });

        // Option page functions
        // Pad/Chain/Background/Logo select
        ui_img_select.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        //	View ui_img_select_page =
                        // getLayoutInflater().inflate(R.layout.ui_img_select, null);
                        /*Phatom*/ onExitDialog
                                .findViewById(R.id.alertExit_phantom_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images
                                                                        .Media
                                                                        .EXTERNAL_CONTENT_URI);
                                                context.startActivityForResult(
                                                        get_image_from_gallery, PICK_PHANTOM_IMG);
                                            }
                                        });
                        /*Phatom Center*/ onExitDialog
                                .findViewById(R.id.alertExit_phantomC_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images
                                                                        .Media
                                                                        .EXTERNAL_CONTENT_URI);
                                                context.startActivityForResult(
                                                        get_image_from_gallery, PICK_PHANTOMC_IMG);
                                            }
                                        });
                        /*Logo*/ onExitDialog
                                .findViewById(R.id.alertExit_logo_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images
                                                                        .Media
                                                                        .EXTERNAL_CONTENT_URI);
                                                context.startActivityForResult(
                                                        get_image_from_gallery, PICK_LOGO_IMG);
                                            }
                                        });
                        /*Logo Color*/ onExitDialog
                                .findViewById(R.id.alertExit_logo_bg_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images
                                                                        .Media
                                                                        .EXTERNAL_CONTENT_URI);
                                                context.startActivityForResult(
                                                        get_image_from_gallery, PICK_LOGO_BG_IMG);
                                            }
                                        });

                        /*Background*/ onExitDialog
                                .findViewById(R.id.alertExit_background_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images
                                                                        .Media
                                                                        .EXTERNAL_CONTENT_URI);
                                                context.startActivityForResult(
                                                        get_image_from_gallery,
                                                        PICK_BACKGROUND_IMG);
                                            }
                                        });
                        /*Chain*/ onExitDialog
                                .findViewById(R.id.alertExit_chain_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images
                                                                        .Media
                                                                        .EXTERNAL_CONTENT_URI);
                                                context.startActivityForResult(
                                                        get_image_from_gallery, PICK_CHAIN_IMG);
                                            }
                                        });
                        /*Buttom color*/ onExitDialog
                                .findViewById(R.id.alertExit_btn_select)
                                .setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                Intent get_image_from_gallery =
                                                        new Intent(
                                                                Intent.ACTION_PICK,
                                                                android.provider.MediaStore.Images
                                                                        .Media
                                                                        .EXTERNAL_CONTENT_URI);
                                                context.startActivityForResult(
                                                        get_image_from_gallery, PICK_BTN_IMG);
                                            }
                                        });
                        switShowbyIndex(swit, false, EXIT_PAGE_UI_IMG_SELECTOR);
                        barTitle.setText(context.getString(R.string.ui_img_select_title));
                    }
                });

        // sound spam
        sound_spam_item.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (spamSounds) {
                            spamSounds = false;
                            sound_spam.setChecked(spamSounds);
                        } else {
                            sound_spam.setChecked(false);
                            AlertDialog.Builder spam_alert = new AlertDialog.Builder(context);
                            spam_alert.setCancelable(false);
                            spam_alert.setMessage(R.string.sound_spam_dialog);
                            spam_alert.setPositiveButton(
                                    R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            spamSounds = true;
                                            sound_spam.setChecked(spamSounds);
                                        }
                                    });
                            spam_alert.setNegativeButton(R.string.no, null);
                            spam_alert.create().show();
                        }
                    }
                });
        // hide buttons
        hide_buttoms.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (hide_check.isChecked()) {
                            hide_check.setChecked(false);

                            hide_buttoms_b = false;
                        } else {
                            hide_check.setChecked(true);
                            hide_buttoms_b = true;
                        }
                    }
                });
        // Slide mode
        slide_mode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (slideMode) {
                            slide_check.setChecked(false);
                            slideMode = false;
                        } else {
                            slide_check.setChecked(true);
                            slideMode = true;
                        }
                        GlobalConfigs.app_configs.edit().putBoolean("slideMode", slideMode);
                    }
                });
        // record to autoplay
        rec_autoplay.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (recAutoplay) {
                            recAutoplay = false;
                            rec_ap_check.setChecked(false);
                            stopRecAutoplay.setVisibility(View.GONE);
                        } else {
                            recAutoplay = true;
                            rec_ap_check.setChecked(true);
                            stopRecAutoplay.setVisibility(View.VISIBLE);
                        }
                    }
                });
        // Show glow effect
        glowShow.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (glow_enabled) {
                            glow_check.setChecked(false);
                            glow_cfg_show.setAlpha(0.6f);
                            // glows.setOffGlows();
                            glow_enabled = false;
                        } else {
                            glow_check.setChecked(true);
                            glow_cfg_show.setAlpha(1.0f);
                            /*

                            MAKE GLOW HERE

                            */
                            glow_enabled = true;
                        }
                        GlobalConfigs.app_configs.edit().putBoolean("glowEf", glow_enabled);
                    }
                });
        // Show glow configs (if Show Glows Effect is enabled)
        glow_cfg_show.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        final View glow_cfg_window = context.findViewById(R.id.glow_cfg_window);
                        if (glow_enabled)
                            if (glow_cfg_visible) {
                                glow_cfg_window.setVisibility(View.GONE);
                                glow_cfg_check.setChecked(false);
                                glow_cfg_visible = false;
                            } else {
                                // change window position
                                RelativeLayout.LayoutParams params =
                                        new RelativeLayout.LayoutParams(
                                                glow_cfg_window.getLayoutParams().width,
                                                glow_cfg_window.getLayoutParams().height);
                                params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                                params.addRule(
                                        RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                                glow_cfg_window.setLayoutParams(params);
                                // set data and functions

                                EditText radius =
                                        glow_cfg_window.findViewById(R.id.glow_config_edit_radius);
                                radius.setText("");
                                glow_cfg_window
                                        .findViewById(R.id.glow_cfg_r_minus)
                                        .setOnClickListener(
                                                new Button.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        radius.setText(
                                                                ""
                                                                        + (Integer.parseInt(
                                                                                        radius.getText()
                                                                                                .toString())
                                                                                - 1));
                                                    }
                                                });
                                glow_cfg_window
                                        .findViewById(R.id.glow_cfg_r_plus)
                                        .setOnClickListener(
                                                new Button.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        radius.setText(
                                                                ""
                                                                        + (Integer.parseInt(
                                                                                        radius.getText()
                                                                                                .toString())
                                                                                + 1));
                                                    }
                                                });

                                EditText intensity =
                                        glow_cfg_window.findViewById(
                                                R.id.glow_config_edit_intensidade);
                                intensity.setText("" + glowIntensity);
                                glow_cfg_window
                                        .findViewById(R.id.glow_cfg_i_minus)
                                        .setOnClickListener(
                                                new Button.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        intensity.setText(
                                                                ""
                                                                        + BigDecimal.valueOf(
                                                                                        Double
                                                                                                .parseDouble(
                                                                                                        intensity
                                                                                                                .getText()
                                                                                                                .toString()))
                                                                                .subtract(
                                                                                        BigDecimal
                                                                                                .valueOf(
                                                                                                        0.01))
                                                                                .floatValue());
                                                    }
                                                });
                                glow_cfg_window
                                        .findViewById(R.id.glow_cfg_i_plus)
                                        .setOnClickListener(
                                                new Button.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (BigDecimal.valueOf(
                                                                                Double.parseDouble(
                                                                                        intensity
                                                                                                .getText()
                                                                                                .toString()))
                                                                        .add(
                                                                                BigDecimal.valueOf(
                                                                                        0.01))
                                                                        .floatValue()
                                                                <= 1.0f)
                                                            intensity.setText(
                                                                    ""
                                                                            + BigDecimal.valueOf(
                                                                                            Double
                                                                                                    .parseDouble(
                                                                                                            intensity
                                                                                                                    .getText()
                                                                                                                    .toString()))
                                                                                    .add(
                                                                                            BigDecimal
                                                                                                    .valueOf(
                                                                                                            0.01))
                                                                                    .floatValue());
                                                    }
                                                });
                                // widgets
                                boolean chainGlow = false;
                                Switch padOrChain =
                                        glow_cfg_window.findViewById(R.id.glow_cfg_switch);
                                Button hide = glow_cfg_window.findViewById(R.id.glow_cfg_exit);
                                Button change =
                                        glow_cfg_window.findViewById(R.id.glow_cfg_changecfgs);
                                hide.setOnClickListener(
                                        new Button.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                glow_cfg_window.setVisibility(View.GONE);
                                                glow_cfg_visible = false;
                                            }
                                        });
                                padOrChain.setChecked(false);
                                padOrChain.setOnCheckedChangeListener(
                                        new Switch.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(
                                                    CompoundButton arg0, boolean arg1) {
                                                radius.setText("");

                                                if (arg1) {
                                                    glow_cfg_window
                                                            .findViewById(R.id.textPad)
                                                            .setAlpha(0.5f);
                                                    glow_cfg_window
                                                            .findViewById(R.id.textChain)
                                                            .setAlpha(1.0f);
                                                    intensity.setText("" + glowChainIntensity);
                                                    // glowRadius =
                                                    // Integer.parseInt(radius.getText().toString());
                                                } else {
                                                    glow_cfg_window
                                                            .findViewById(R.id.textChain)
                                                            .setAlpha(0.5f);
                                                    glow_cfg_window
                                                            .findViewById(R.id.textPad)
                                                            .setAlpha(1.0f);
                                                    intensity.setText("" + glowIntensity);
                                                }
                                            }
                                        });
                                change.setOnClickListener(
                                        new Button.OnClickListener() {
                                            @Override
                                            public void onClick(View arg0) {
                                                if (radius.getText()
                                                        .toString()
                                                        .matches("([1-9]|[1-9][0-9]{0,3})")) {
                                                    float radiusSize =
                                                            Float.parseFloat(
                                                                    radius.getText().toString());
                                                    if (intensity
                                                            .getText()
                                                            .toString()
                                                            .matches("(0.[0-9]?[1-9]|1.0)")) {
                                                        float intensityAlpha =
                                                                Float.parseFloat(
                                                                        intensity
                                                                                .getText()
                                                                                .toString());

                                                        if (padOrChain.isChecked()) {
                                                            glowChainRadius =
                                                                    Integer.parseInt(
                                                                            radius.getText()
                                                                                    .toString());
                                                        } else {
                                                            glowPadRadius =
                                                                    Integer.parseInt(
                                                                            radius.getText()
                                                                                    .toString());
                                                        }
                                                        GlobalConfigs.app_configs
                                                                .edit()
                                                                .putInt(
                                                                        "glowChainRadius",
                                                                        glowChainRadius);
                                                        GlobalConfigs.app_configs
                                                                .edit()
                                                                .putInt(
                                                                        "glowPadRadius",
                                                                        glowPadRadius);
                                                        GlobalConfigs.app_configs
                                                                .edit()
                                                                .putFloat(
                                                                        "glowChainIntensity",
                                                                        glowChainIntensity);
                                                        GlobalConfigs.app_configs
                                                                .edit()
                                                                .putFloat(
                                                                        "glowPadIntensity",
                                                                        glowIntensity);
                                                    } else {
                                                        Toast.makeText(
                                                                        context,
                                                                        context.getString(
                                                                                R.string
                                                                                        .invalid_intensity),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                                    context,
                                                                    context.getString(
                                                                            R.string
                                                                                    .invalid_radius),
                                                                    Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                            }
                                        });
                                glow_cfg_window.setVisibility(View.VISIBLE);
                                glow_cfg_check.setChecked(true);
                                glow_cfg_visible = true;
                            }
                    }
                });
        // Buscar a imagem overlay
        alertExit_layout_decoration_item.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (decoration_show.isChecked()) {
                            int PICK_IMG = 12;
                            Intent get_image_from_gallery =
                                    new Intent(
                                            Intent.ACTION_PICK,
                                            android.provider.MediaStore.Images.Media
                                                    .EXTERNAL_CONTENT_URI);
                            context.startActivityForResult(get_image_from_gallery, PICK_IMG);
                        }
                    }
                });
        // Permitir a camada PNG de launchpad ou sei la..
        final ImageView layer = context.findViewById(R.id.launchpadOverride);
        final View window = context.findViewById(R.id.layer_cfg_window);
        decoration_show.setOnCheckedChangeListener(
                new CheckBox.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        if (!arg1) {
                            decoration_show.setChecked(false);
                            layer_decoration = false;
                            ((ImageView) context.findViewById(R.id.launchpadOverride))
                                    .setVisibility(View.GONE);
                            window.setVisibility(View.GONE);
                            alertExit_layout_decoration_item.setAlpha(0.6f);
                            item_customHeight.setAlpha(0.6f);
                            ((LinearLayout)
                                            context.findViewById(
                                                    R.id.layer_decoration_scale_background))
                                    .setVisibility(View.GONE);
                        } else {
                            decoration_show.setChecked(true);
                            layer_decoration = true;
                            item_customHeight.setAlpha(1.0f);
                            layer.getLayoutParams().height = GlobalConfigs.display_height;
                            layer.getLayoutParams().width = GlobalConfigs.display_width;
                            layer.setVisibility(View.VISIBLE);
                            //	window.setVisibility(View.VISIBLE);
                            alertExit_layout_decoration_item.setAlpha(1.0f);
                        }
                    }
                });
        // Tamanho das pads
        item_customHeight.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (layer_decoration) {
                            // Busque as View e Obtenha as configuracoes atuais
                            EditText layer_size_w =
                                    context.findViewById(R.id.layer_cfg_edit_size_w);
                            EditText layer_size_h =
                                    context.findViewById(R.id.layer_cfg_edit_size_h);
                            EditText pads_size = context.findViewById(R.id.pads_cfg_edit_size);
                            Button layer_size_plus = context.findViewById(R.id.layer_cfg_s_plus);
                            Button layer_size_minus = context.findViewById(R.id.layer_cfg_s_minus);
                            Button pads_size_plus = context.findViewById(R.id.pads_cfg_s_plus);
                            Button pads_size_minus = context.findViewById(R.id.pads_cfg_s_minus);
                            Button close = context.findViewById(R.id.layer_cfg_exit);
                            Button change = context.findViewById(R.id.layer_cfg_changecfgs);

                            View window = context.findViewById(R.id.layer_cfg_window);

                            float layer_h = layer.getScaleX();
                            // int pads_hw = MakePads.layoutpads.getLayoutParams().height;

                            layer_size_h.setText("" + layer_h);
                            pads_size.setText("" + 0);

                            layer_size_w.setVisibility(View.GONE);

                            layer_size_plus.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {
                                            layer_size_h.setText(resizeLayer(layer, 0.01));
                                        }
                                    });
                            layer_size_minus.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {
                                            layer_size_h.setText(resizeLayer(layer, -0.01));
                                        }
                                    });

                            pads_size_plus.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {

                                            GlobalConfigs.display_height = 1;
                                            if (mk2) {
                                                // padWH = pads_hw / 9;
                                            } else {
                                                // padWH = pads_hw / 10;
                                            }
                                        }
                                    });
                            pads_size_minus.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {

                                            GlobalConfigs.display_height = 0;
                                            if (mk2) {
                                                // padWH = pads_hw / 9;
                                            } else {
                                                // padWH = pads_hw / 10;
                                            }
                                        }
                                    });
                            change.setOnClickListener(
                                    new View.OnClickListener() {

                                        @Override
                                        public void onClick(View arg0) {
                                            String lh = layer_size_h.getText().toString();
                                            String lw = layer_size_w.getText().toString();
                                            String phw = pads_size.getText().toString();
                                            if (!lw.isEmpty()) {
                                                if (!lh.isEmpty()) {
                                                    if (!phw.isEmpty()) {
                                                    } else {
                                                        lh = "0";
                                                        Toast.makeText(
                                                                        context,
                                                                        "Pads size error",
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                    }
                                                } else {
                                                    lh = "0";
                                                    Toast.makeText(
                                                                    context,
                                                                    "Layer height error",
                                                                    Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            } else {
                                                lw = "0";
                                                Toast.makeText(
                                                                context,
                                                                "Layer width error",
                                                                Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                            int layer_h = Integer.parseInt(lh);
                                            int layer_w = Integer.parseInt(lw);
                                            int pads_hw = Integer.parseInt(phw);
                                            layer.setScaleX(layer_h);
                                            layer.setScaleY(layer_w);

                                            pads_size.setText("" + pads_hw);

                                            layer.setVisibility(View.GONE);
                                            layer.setVisibility(View.VISIBLE);

                                            GlobalConfigs.display_height = pads_hw;
                                            if (mk2) {
                                                // padWH = pads_hw / 9;
                                            } else {
                                                // padWH = pads_hw / 10;
                                            }
                                        }
                                    });
                            close.setOnClickListener(
                                    new View.OnClickListener() {

                                        @Override
                                        public void onClick(View arg0) {
                                            window.setVisibility(View.GONE);
                                        }
                                    });
                            window.setVisibility(View.VISIBLE);
                        }
                    }
                });
        // Permitir spam de leds..
        led_spam_item.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (spamLeds) {
                            spamLeds = false;
                            led_spam_check.setChecked(spamSounds);
                        } else {
                            led_spam_check.setChecked(false);
                            AlertDialog.Builder spam_alert = new AlertDialog.Builder(context);
                            spam_alert.setCancelable(false);
                            spam_alert.setMessage(R.string.led_spam_dialog);
                            spam_alert.setPositiveButton(
                                    R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            spamLeds = true;
                                            led_spam_check.setChecked(spamLeds);
                                        }
                                    });
                            spam_alert.setNegativeButton(R.string.no, null);
                            spam_alert.create().show();
                        }
                    }
                });
        // Finalmente criar e mostrar a Janela
        AlertDialog.Builder alertExit = new AlertDialog.Builder(context);
        alertExit.setView(onExitDialog);
        alertDialog = alertExit.create();
    }
}
