package com.xayup.multipad.project.autoplay;

import android.app.Activity;
import android.graphics.Color;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoPlay
        implements Project.AutoPlayInterface, MapData, Runnable, PadPressCallInterface {
    protected Activity context;
    protected int autoplay_index;
    protected int forecast_max_count;
    protected boolean paused;
    protected List<int[]> auto_play_map;
    protected AtomicBoolean running;
    protected Thread mThread;
    protected Ui.Touch mTouch;
    protected AutoPlayChanges mAutoPlayChanges;
    protected View[] forecasts;
    protected PadPressCallInterface pausedCall;
    protected int[] practical_request;
    protected PadPressCallInterface callInterface;

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
        if(forecasts != null) Arrays.fill(forecasts, null);
        running.set(false);
        paused = false;
    }

    /**
     * Start autoplay
     * @param autoPlayChanges .
     * @return PadPressCallInterface to add call
     */
    public PadPressCallInterface startAutoPlay(AutoPlayChanges autoPlayChanges) {
        if(auto_play_map.isEmpty()) return null;

        XLog.v("Try start autoplay", "");

        this.running.set(true);
        this.paused = false;
        this.mAutoPlayChanges = autoPlayChanges;
        this.autoplay_index = 0;
        if(mTouch == null) this.mTouch = new Ui.Touch();
        (mThread = new Thread(this)).start();
        return (chain, x, y)->{
            if(isPaused()){
                checkFramePractical(chain, x, y);
                return true;
            }
            return false;
        };
    }

    public void pauseAutoPlay(int forecast_max_count) {
        this.forecast_max_count = forecast_max_count;
        forecasts = new View[forecast_max_count];
        practical_request = new int[3];
        paused = true;
    }

    public void setFramePractical(){
        int retry_loop = 0;
        for (int i = 0; i < forecast_max_count; i++) {
            int[] frame = auto_play_map.get(autoplay_index+retry_loop);
            if(frame == null) break;
            switch (frame[FRAME_TYPE]) {
                case FRAME_TYPE_ON:
                case FRAME_TYPE_TOUCH:
                case FRAME_TYPE_CHAIN:
                {
                    View view = mAutoPlayChanges.getViewShowPracticalMark(frame[FRAME_PAD_X], frame[FRAME_PAD_Y]);
                    final int final_i = i;
                    context.runOnUiThread( ()-> {
                        view.setBackgroundColor(Color.BLUE);
                        view.setAlpha(1f - (final_i * 0.2f));
                    });
                    forecasts[i] = view;
                    if(i == 0){
                        practical_request[0] = frame[FRAME_VALUE];
                        practical_request[1] = frame[FRAME_PAD_X];
                        practical_request[2] = frame[FRAME_PAD_Y];
                    }
                    break;
                }
                default: {
                    i--;
                    break;
                }
            }
            retry_loop++;
        }
    }

    public void checkFramePractical(int chain, int row, int colum) {
        if(!(chain == practical_request[0] &&
                row == practical_request[1] &&
                colum == practical_request[2])) return;

        autoplay_index++;
        if(forecasts != null && forecasts[0] != null){
                context.runOnUiThread(()-> {
                forecasts[0].setBackgroundColor(Color.WHITE);
                forecasts[0].setAlpha(0);
            });
        }
        setFramePractical();
    }

    public void removeForecast(){
        if (forecasts != null) {
            for (View v : forecasts) {
                if(v == null) continue;
                v.setBackgroundColor(Color.WHITE);
                v.setAlpha(0f);
            }
        }
        forecasts = null;
    }

    @Override
    public boolean stopAutoPlay() {
        return false;
    }

    public PadPressCallInterface stopAutoPlayy() {
        running.set(false);
        autoplay_index = 0;
        mTouch = null;
        removeForecast();
        return callInterface;
    }

    @Override
    public boolean pauseAutoPlay() {
        return false;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Deprecated
    @Override
    public boolean startAutoPlay() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public boolean resumeAutoPlay() {
        paused = false;
        mThread.start();
        removeForecast();
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
                case FRAME_TYPE_ON: // ACTION_DOWN
                    {
                        if(frame[FRAME_VALUE] != mAutoPlayChanges.getCurrentChainId()) context.runOnUiThread(() -> mTouch.touchAndRelease(mAutoPlayChanges.getPadToTouch(frame[FRAME_PAD_X], frame[FRAME_PAD_Y])));
                        context.runOnUiThread(() -> mTouch.touch(mAutoPlayChanges.getPadToTouch(frame[FRAME_PAD_X], frame[FRAME_PAD_Y])));
                        break;
                    }
                case FRAME_TYPE_OFF: // ACTION_UP
                    {
                        if(frame[FRAME_VALUE] != mAutoPlayChanges.getCurrentChainId()) context.runOnUiThread(() -> mTouch.touchAndRelease(mAutoPlayChanges.getPadToTouch(frame[FRAME_PAD_X], frame[FRAME_PAD_Y])));
                        context.runOnUiThread(() -> mTouch.release(mAutoPlayChanges.getPadToTouch(frame[FRAME_PAD_X], frame[FRAME_PAD_Y])));
                        break;
                    }
                case FRAME_TYPE_TOUCH: // Touch in pad button
                case FRAME_TYPE_CHAIN: // Touch in chain button
                    {
                        context.runOnUiThread(() -> mTouch.touchAndRelease(mAutoPlayChanges.getPadToTouch(frame[FRAME_PAD_X], frame[FRAME_PAD_Y])));
                        break;
                    }
            }
        }
        if (autoplay_index >= auto_play_map.size()) {
            autoplay_index = 0;
        } else if (paused) {
            setFramePractical();
        }
    }

    @Override
    public boolean call(int chain, int x, int y) {
        return startAutoPlay();
    }

    public interface AutoPlayChanges {
        View getViewShowPracticalMark(int r, int c);
        View getPadToTouch(int r, int c);
        int getCurrentChainId();
    }
}
