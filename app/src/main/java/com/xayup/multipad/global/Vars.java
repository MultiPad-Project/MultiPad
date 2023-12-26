package com.xayup.multipad.global;

import android.os.Environment;

public class Vars {
    public final static String MULTIPAD_PATH = Environment.getExternalStorageDirectory().getPath().concat("/MultiPad");
    public final static String MULTIPAD_SKINS_PATH = MULTIPAD_PATH.concat("/Skins");
}
