package com.xayup.multipad.project.autoplay;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.widget.GridLayout;
import com.xayup.debug.XLog;
import com.xayup.multipad.Ui;
import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.pads.PadPressCallInterface;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.project.MapData;
import com.xayup.multipad.load.Project;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoPlay
        implements Project.AutoPlayInterface, MapData, Runnable, PadPressCallInterface {
    protected Activity context;
    protected List<int[]> auto_play_map;
    protected AtomicBoolean running;
    protected boolean paused;
    protected Thread mThread;
    protected int autoplay_index;
    protected int forecast_max_count;
    protected AutoPlayChange mAutoPlayChange;
    protected GridLayout viewRoot;
    protected Ui.Touch mTouch;

    public AutoPlay(Activity context) {
        this.context = context;
        running = new AtomicBoolean(false);
        this.auto_play_map = new ArrayList<>();
    }

    public void parse(File autoplay_file, LoadProject.LoadingProject mLoadingProject) {
        auto_play_map.clear();
        new AutoPlayReader().read(autoplay_file, auto_play_map, mLoadingProject);
    }

    public void clear() {
        auto_play_map.clear();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean startAutoPlay() {
        return false;
    }

    public boolean startAutoPlay(GridLayout rootView) {
        if(auto_play_map.isEmpty()) return false;
        XLog.v("Try start autoplay", "");
        this.viewRoot = rootView;
        mTouch = new Ui.Touch();
        running.set(true);
        paused = false;
        (mThread = new Thread(this)).start();
        return true;
    }

    @Override
    public boolean stopAutoPlay() {
        running.set(false);
        mTouch = null;
        return true;
    }

    @Override
    public boolean pauseAutoPlay() {
        paused = true;
        return false;
    }

    public PadPressCallInterface praticeModeCall(int forecast_max_count) {
        this.forecast_max_count = forecast_max_count;
        return (chain, x, y) -> {
            nextFramePraticle();
            return true;
        };
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

    public void nextFramePraticle() {
        int[] frame = auto_play_map.get(autoplay_index);
        switch (frame[FRAME_TYPE]) {
            case FRAME_TYPE_ON:
            case FRAME_TYPE_TOUCH:
            case FRAME_TYPE_CHAIN:
                {
                    for (int i = 0; i < forecast_max_count; i++) {
                        mAutoPlayChange.onAutoPlayPraticle(
                                frame[FRAME_PAD_X], frame[FRAME_PAD_Y], i);
                    }
                    break;
                }
        }
        autoplay_index++;
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
    public void run() {
        // mAutoPlayChange.onAutoPlayStarted(auto_play_map.size());
        for (;
                autoplay_index < auto_play_map.size() && !paused && running.get();
                autoplay_index++) {
            //mAutoPlayChange.onAutoPlayProgress(autoplay_index);
            int[] frame = auto_play_map.get(autoplay_index);
            switch (frame[FRAME_TYPE]) {
                case FRAME_TYPE_DELAY:
                    {
                        long delay = SystemClock.uptimeMillis() + frame[FRAME_VALUE];
                        while (SystemClock.uptimeMillis() < delay && running.get() && !paused) {}
                        continue;
                    }
                case FRAME_TYPE_ON:
                    {
                        // ACTION_DOWN
                        context.runOnUiThread(() -> mTouch.touch(
                                viewRoot.getChildAt(MakePads.PadID.getGridIndexFromXY(viewRoot.getColumnCount(), frame[FRAME_PAD_X], frame[FRAME_PAD_Y]))));
                        break;
                    }
                case FRAME_TYPE_OFF:
                    {
                        // ACTION_UP
                        context.runOnUiThread(() -> mTouch.release(
                                        viewRoot.getChildAt(MakePads.PadID.getGridIndexFromXY(viewRoot.getColumnCount(), frame[FRAME_PAD_X], frame[FRAME_PAD_Y]))));
                        break;
                    }
                case FRAME_TYPE_TOUCH:
                case FRAME_TYPE_CHAIN:
                    {
                        // Touch in chain
                        context.runOnUiThread(() -> mTouch.touchAndRelease(
                                viewRoot.getChildAt(MakePads.PadID.getGridIndexFromXY(viewRoot.getColumnCount(), frame[FRAME_PAD_X], frame[FRAME_PAD_Y]))));
                        break;
                    }
            }
        }
        if (autoplay_index >= auto_play_map.size()) {
            autoplay_index = 0;
        } else if (paused) {
            nextFramePraticle();
        }
    }

    @Override
    public boolean call(int chain, int x, int y) {
        return startAutoPlay();
    }

    public interface AutoPlayChange {

        public void onAutoPlayProgress(int percent);

        public void onAutoPlayStarted(int length);

        public void onPauseAutoPlay();

        public void onResumeAutoPlay();

        public void onAutoPlayStopped();

        public void onAutoPlayPraticle(int x_next, int y_next, int forecast_count);
    }
}
