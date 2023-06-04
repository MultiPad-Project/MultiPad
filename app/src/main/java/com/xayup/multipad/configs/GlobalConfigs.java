package com.xayup.multipad.configs;

import android.os.Environment;
import com.xayup.debug.XLog;
import com.xayup.multipad.VariaveisStaticas;
import java.util.HashMap;
import java.util.Map;

public class GlobalConfigs {
    // Habilite a depuração do aplicativo
    protected Void debug = XLog.debug(true);

    // Configurações do app (Default startup)
    public static String projects_root_folder = VariaveisStaticas.PROJECTS_PATH;
    public static boolean use_old_main_layout = false;

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
