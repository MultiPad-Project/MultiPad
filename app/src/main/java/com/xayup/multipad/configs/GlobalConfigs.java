package com.xayup.multipad.configs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;
import android.widget.Button;
import android.widget.VerticalSeekBar;
import com.xayup.debug.XLog;
import com.xayup.multipad.VariaveisStaticas;
import com.xayup.multipad.pads.Render.MakePads;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalConfigs {
    public static SharedPreferences app_configs;

    public static void loadSharedPreferences(Activity context) {
        app_configs = context.getSharedPreferences("app_configs", 0 /*MODE_PRIVATE*/);
        app_configs.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
                        arg0.edit().commit();
                    }
                });
    }

    // Configurações do app (Default startup)
    public static String projects_root_folder = VariaveisStaticas.PROJECTS_PATH;
    public static boolean use_old_main_layout = false;
    public static int display_height;
    public static int display_width;

    /* Persistent */

    // PlayPads
    public static class PlayPadsConfigs {

        public int glowPadRadius = 180;
        public int glowChainRadius = 160;
        public float glowIntensity = 0.9f;
        public float glowChainIntensity = 0.6f;
        public boolean mk2,
                autoPlayCheck,
                spamSounds,
                spamLeds,
                pressLed,
                layer_decoration,
                recAutoplay,
                glow_cfg_visible = false;
        /* Persistent */
        public boolean slideMode;
        public boolean hide_buttoms_b;
        public boolean glow_enabled;
        public static String skin_package;
        /* Widgets */
        public VerticalSeekBar progressAutoplay;
        public Button stopRecAutoplay;
        
        
    }

    Map<String, Object> last_changes = new HashMap<>();

    public void saveAllLAstChanges() {
        if (last_changes.isEmpty()) return;
        for (String key : last_changes.keySet()) {
            if (last_changes.get(key) instanceof String) {}
            if (last_changes.get(key) instanceof Boolean) {}
            if (last_changes.get(key) instanceof Integer) {}
        }
    }
}
