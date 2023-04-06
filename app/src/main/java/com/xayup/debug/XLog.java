package com.xayup.debug;

import android.util.Log;

public class XLog {
  protected static boolean enabled = true;

  public static Void debug(boolean enable) {
    enabled = enable;
    return null;
  }

  public static void e(String t, String m) {
    if (enabled) {
      Log.e(t, m);
    }
  }

  public static void v(String t, String m) {
    if (enabled) {
      Log.v(t, m);
    }
  }
}
