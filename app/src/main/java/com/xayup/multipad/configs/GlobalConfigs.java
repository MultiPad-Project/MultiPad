package com.xayup.multipad.configs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.VerticalSeekBar;
import com.xayup.multipad.BuildConfig;
import com.xayup.multipad.VariaveisStaticas;

public class GlobalConfigs {
    public static SharedPreferences app_configs;

    // App settings (Default startup)
    public static String projects_root_folder = VariaveisStaticas.PROJECTS_PATH;
    public static boolean use_old_main_layout = false;
    public static int display_height;
    public static int display_width;
    public static boolean use_unipad_colors = false;
    public static boolean use_unipad_folder = false;
    public static String color_table_file_dir = "";

    public static boolean floating_window_grid_resize_visible = false;

    /* Persistent */

    // PlayPads
    public static int glowPadRadius = 180;
    public static int glowChainRadius = 160;
    public static float glowIntensity = 0.9f;
    public static float glowChainIntensity = 0.6f;
    public static boolean mk2,
                        autoPlayCheck,
                        spamSounds,
                        spamLeds,
                        pressLed,
                        layer_decoration,
                        recAutoplay,
                        glow_cfg_visible = false;
    /* Persistent */
    public static boolean slideMode;
    public static boolean hide_buttoms_b;
    public static boolean glow_enabled;
    public static boolean reverse_rows = false;
    public static boolean reverse_columns = false;
    public static String skin_package = BuildConfig.APPLICATION_ID;
    public static String current_table = "EyeDrop_default.ct";
    public static String default_table_name = "EyeDrop_default.ct";
    public String table_path = DefaultConfigs.MULTIPAD_PATH;
    /* Widgets */
    public VerticalSeekBar progressAutoplay;
    public static Button stopRecAutoplay;

    public interface DefaultConfigs {
        public String MULTIPAD_PATH = Environment.getExternalStorageDirectory() + "/MultiPad";
        public String UNIPAD_PATH = Environment.getExternalStorageDirectory() + "/Unipad"; //Android 10--
        public String COLOR_TABLE_PATH = MULTIPAD_PATH + "/LCT";
        public String PROJECTS_PATH = MULTIPAD_PATH + "/Projects";
    }

    public static void loadSharedPreferences(Context context) {
        app_configs = context.getSharedPreferences("app_configs", Context.MODE_PRIVATE /*MODE_PRIVATE*/);
        /*app_configs.registerOnSharedPreferenceChangeListener(
                (arg0, arg1) -> arg0.edit().commit());*/

        GlobalConfigs.use_unipad_folder = app_configs.getBoolean("useUnipadFolder", false);
        GlobalConfigs.skin_package = app_configs.getString("skin", BuildConfig.APPLICATION_ID);
        Log.v("Skin saved", GlobalConfigs.skin_package);
    }

    public static void saveSkin(String skin_package){
        app_configs.edit().putString("skin", (GlobalConfigs.skin_package = skin_package)).apply();
    }
}
