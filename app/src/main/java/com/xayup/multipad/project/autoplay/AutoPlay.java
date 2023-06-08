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
import com.xayup.debug.XLog;
import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.pads.PadPressCallInterface;
import com.xayup.multipad.project.autoplay.AutoPlayReader;
import com.xayup.multipad.load.Project;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoPlay implements Project.AutoPlayInterface {
    protected Activity context;
    protected List<int[]> auto_play_map;
    protected AtomicBoolean runnig;
    protected boolean paused;
    public AutoPlay(Activity context){
        this.context = context;
        runnig = new AtomicBoolean(false);
    }
    
    public void parse(File autoplay_file, LoadProject.LoadingProject mLoadingProject){
        new AutoPlayReader().read(autoplay_file, auto_play_map, mLoadingProject);
    }

    @Override
    public boolean isRunning() {
        return runnig.get();
    }

    @Override
    public boolean startAutoPlay() {
        XLog.v("Try start autoplay", "");

        return true;
    }

    @Override
    public boolean stopAutoPlay() {
        runnig.set(false);
        return true;
    }

    @Override
    public boolean pauseAutoPlay() {
        return false;
    }

    @Override
    public boolean inPaused() {
        return paused;
    }

    @Override
    public boolean resumeAutoPlay() {
        return true;
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
