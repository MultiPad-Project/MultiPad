package com.xayup.multipad.project.autoplay;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import com.xayup.debug.XLog;
import com.xayup.multipad.Ui;
import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.pads.PadPressCallInterface;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.pads.Render.PadSkinData;
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

    public void pauseAutoPlay(int forecast_max_count) {
        this.forecast_max_count = forecast_max_count;
        forecasts = new View[forecast_max_count];
        practical_request = new int[3];
        paused = true;
    }

    protected void setFramePractical(int forecast_max, int offset, int increment_index){
        int autoplay_next_index = autoplay_index;
        for (int i = offset; i < forecast_max; i++) {
            if(autoplay_next_index >= auto_play_map.size()) break;
            int[] frame = auto_play_map.get(autoplay_next_index);
            switch (frame[FRAME_TYPE]) {
                case FRAME_TYPE_ON:
                case FRAME_TYPE_TOUCH:
                case FRAME_TYPE_CHAIN:
                {
                    View view = mAutoPlayChanges.getViewShowPracticalMark(frame[FRAME_PAD_X], frame[FRAME_PAD_Y]);
                    setPractical_request(view, i);
                    forecasts[i] = view;
                    if(i == 0) {
                        practical_request[0] = frame[FRAME_VALUE];
                        practical_request[1] = frame[FRAME_PAD_X];
                        practical_request[2] = frame[FRAME_PAD_Y];
                        XLog.e("Request touch", Arrays.toString(practical_request));
                    }
                    if (i == increment_index){
                        autoplay_index = autoplay_next_index;
                    }
                    break;
                }
                default: {
                    i--;
                    break;
                }
            }
            autoplay_next_index++;
        }
    }

    protected void setPractical_request(View view, int level){
        context.runOnUiThread( ()-> {
            view.setAlpha(1f);
            ((ImageView) view).setImageDrawable(new ColorDrawable((level == 0) ?
                    mAutoPlayChanges.getSkinData().color_autoplay_practical_1:
                    mAutoPlayChanges.getSkinData().color_autoplay_practical_2));
        });
    }

    /**
     *  .
     * @param pad .
     */
    public void checkFramePractical(MakePads.PadInfo pad) {
        boolean chain_request = practical_request[0] == -1;
        boolean is_chain = pad.getType() == MakePads.PadInfo.PadInfoIdentifier.CHAIN;

        XLog.e("Check Frame Practical", (is_chain) ? "Chain" : "Pad");

        if(pad.getRow() == practical_request[1] && pad.getColum() == practical_request[2] &&
                (mAutoPlayChanges.getCurrentChainProperties().getMc() == practical_request[0] ||
                (is_chain && chain_request))){
            XLog.e("Accept Button Practical", (is_chain) ? "Chain" : "Pad");

            removeForecast();
            setFramePractical(forecast_max_count, 0, (chain_request) ? 0 : 1);
        } else if(is_chain && !chain_request){
            XLog.e("Request chain", "Chain");

            removeForecast();
            int[] request_chain = MakePads.PadID.getChainXY(practical_request[0], 9);
            practical_request[0] = -1;
            practical_request[1] = request_chain[0];
            practical_request[2] = request_chain[1];
            setPractical_request(forecasts[0] = mAutoPlayChanges.getViewShowPracticalMark(request_chain[0], request_chain[1]), 0);
            setFramePractical(forecast_max_count-1, 1, 0);
        }
    }

    public void removeForecast(){
        if (forecasts != null) {
            for (View v : forecasts) {
                if(v == null) continue;
                ((ImageView) v).setImageDrawable(mAutoPlayChanges.getSkinData().draw_btn_);
                v.setAlpha(0f);
            }
        }
    }

    @Override
    public boolean stopAutoPlay() {
        running.set(false);
        paused = false;
        autoplay_index = 0;
        mTouch = null;
        removeForecast();
        forecasts = null;
        practical_request = null;
        mAutoPlayChanges.onStopped(callInterface);
        return true;
    }

    @Override
    public boolean pauseAutoPlay() {
        return false;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean startAutoPlay() {
        return false;
    }

    /**
     * Start autoplay
     *
     * @param autoPlayChanges .
     */
    public void startAutoPlay(AutoPlayChanges autoPlayChanges) {
        if(auto_play_map.isEmpty()) return;

        XLog.v("Try start autoplay", "");

        this.running.set(true);
        this.paused = false;
        this.mAutoPlayChanges = autoPlayChanges;
        this.autoplay_index = 0;
        if(mTouch == null) this.mTouch = new Ui.Touch();
        (mThread = new Thread(this)).start();
        mAutoPlayChanges.onStarted((chain, pad)->{
            if(isPaused()){
                checkFramePractical(pad);
            }
            return true;
        });
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public boolean resumeAutoPlay() {
        paused = false;
        (mThread = new Thread(this)).start();
        removeForecast();
        forecasts = null;
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
                        if(frame[FRAME_VALUE] != mAutoPlayChanges.getCurrentChainProperties().getId()) context.runOnUiThread(() ->
                                mTouch.touchAndRelease(mAutoPlayChanges.getPadToTouch(frame[FRAME_PAD_X], frame[FRAME_PAD_Y])));
                        context.runOnUiThread(() -> mTouch.touch(mAutoPlayChanges.getPadToTouch(frame[FRAME_PAD_X], frame[FRAME_PAD_Y])));
                        break;
                    }
                case FRAME_TYPE_OFF: // ACTION_UP
                    {
                        if(frame[FRAME_VALUE] != mAutoPlayChanges.getCurrentChainProperties().getId()) context.runOnUiThread(() ->
                                mTouch.touchAndRelease(mAutoPlayChanges.getPadToTouch(frame[FRAME_PAD_X], frame[FRAME_PAD_Y])));
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
            stopAutoPlay();
        } else if (paused) {
            setFramePractical(forecast_max_count, 0, 1);
        }
    }

    @Override
    public boolean call(MakePads.ChainInfo chain, MakePads.PadInfo pad) {
        return startAutoPlay();
    }

    public interface AutoPlayChanges {
        View getViewShowPracticalMark(int r, int c);
        View getPadToTouch(int r, int c);
        MakePads.ChainInfo getCurrentChainProperties();
        PadSkinData getSkinData();

        void onStopped(PadPressCallInterface call);
        void onStarted(PadPressCallInterface call);
    }
}
