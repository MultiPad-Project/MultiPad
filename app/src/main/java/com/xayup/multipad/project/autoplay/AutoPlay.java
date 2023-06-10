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
import com.xayup.multipad.project.MapData;
import com.xayup.multipad.project.autoplay.AutoPlayReader;
import com.xayup.multipad.load.Project;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AutoPlay implements Project.AutoPlayInterface, MapData, Runnable, PadPressCallInterface {
    protected Activity context;
    protected List<int[]> auto_play_map;
    protected AtomicBoolean running;
    protected boolean paused;
    protected Thread mThread;
    protected int autoplay_index;
    
    
    public AutoPlay(Activity context){
        this.context = context;
        running = new AtomicBoolean(false);
    }
    
    public void parse(File autoplay_file, LoadProject.LoadingProject mLoadingProject){
        new AutoPlayReader().read(autoplay_file, auto_play_map, mLoadingProject);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean startAutoPlay() {
        XLog.v("Try start autoplay", "");
        (mThread = new Thread(this)).start();
        return true;
    }

    @Override
    public boolean stopAutoPlay() {
        running.set(false);
        return true;
    }

    @Override
    public boolean pauseAutoPlay() {
        paused = true;
        return false;
    }

    @Override
    public boolean inPaused() {
        return paused;
    }

    @Override
    public boolean resumeAutoPlay() {
        paused = false;
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
    
    @Override
    public void run(){
        onAutoPlayStarted(auto_play_map.size());
        next_frame: for(int[] frame: auto_play_map){
            switch(frame[FRAME_TYPE]){
                case FRAME_TYPE_DELAY: {
                    if (!paused){
                        while(running.get() && !paused){}
                    }
                    continue next_frame;
                }
                case FRAME_TYPE_ON: {
                    // ACTION_DOWN
                    break;
                }
                case FRAME_TYPE_OFF: {
                    // ACTION_UP
                    break;
                }
                case FRAME_TYPE_TOUCH: {
                    // ACTION_DOWN and ACTION_UP
                }
            }
        }
    }
    @Override
    public void call(int chain, int x, int y){
        startAutoPlay();
    }
    
    public abstract void onAutoPlayProgress(int percent);
    public abstract void onAutoPlayStarted(int lenght);
    public abstract void onAutoPlayStoped();
}
