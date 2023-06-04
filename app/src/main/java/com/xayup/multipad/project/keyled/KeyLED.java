package com.xayup.multipad.project.keyled;

import android.app.*;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.View;
import android.widget.*;

import com.xayup.multipad.project.keyled.KeyLEDReader;
import com.xayup.multipad.load.Project;
import com.xayup.multipad.load.ProjectMapData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.*;

public class KeyLED extends Project implements Project.KeyLEDInterface {
    LedMap mLedLoader;
    public KeyLED(){
        mLedLoader = new LedMap();
    }
    
    public List<String[]> parse(File keyled_file){
        return new KeyLEDReader().read(keyled_file, mLedLoader);
    }

    @Override
    public boolean showLed(Activity context, int chain, int pad) {
        
        return false;
    }

    @Override
    public boolean breakLed(Activity context, int chain, int pad) {
        return false;
    }

    @Override
    public boolean breakAll(Activity context) {
        return false;
    }
}
