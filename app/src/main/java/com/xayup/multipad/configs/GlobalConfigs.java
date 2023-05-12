package com.xayup.multipad.configs;

import com.xayup.debug.XLog;

public class GlobalConfigs {
  // Habilite a depuração do aplicativo
  protected Void debug = XLog.debug(true);

  // Configurações do app (Default startup)
  public static boolean use_old_main_layout = false;
}
