package com.xayup.multipad.project.autoplay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.annotation.IntegerRes;
import com.xayup.multipad.project.autoplay.AutoPlayReader;
import com.xayup.multipad.load.Project;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoPlay implements Project.AutoPlayInterface {
    
    List<int[]> auto_play_map;
    
    public AutoPlay(){
    }
    
    public List<String[]> parse(File autoplay_file){
        return new AutoPlayReader().read(autoplay_file, auto_play_map);
    }
    
    @Override
    public boolean startAutoPlay() {
        return false;
    }

    @Override
    public boolean pauseAutoPlay() {
        return false;
    }

    @Override
    public boolean resumeAutoPlay() {
        return false;
    }

    @Override
    public float advanceAutoPlay() {
        return 0;
    }

    @Override
    public float regressAutoPlay() {
        return 0;
    }
}
