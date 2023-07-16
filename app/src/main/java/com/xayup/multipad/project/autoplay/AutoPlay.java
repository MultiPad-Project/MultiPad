package com.xayup.multipad.project.autoplay;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VerticalSeekBar;
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

public class AutoPlay implements Project.AutoPlayInterface, MapData, Runnable, PadPressCallInterface {
    protected Activity context;

    /*States*/
    protected boolean paused;
    protected AtomicBoolean running;
    protected int autoplay_index;

    /*Work*/
    protected List<int[]> auto_play_map;
    protected Thread mThread;
    protected AutoPlayChanges mAutoPlayChanges;
    protected PadPressCallInterface callInterface;

    /*Paused/Practical Mode properties*/
    protected int forecast_max_count;
    protected View[] forecasts;
    protected int[] practical_request;
    protected VerticalSeekBar progress_bar;

    /*Default values*/
    protected final int ADVANCE_FRAME_DISTANCE = 3;
    protected final int REGRESS_FRAME_DISTANCE = 3;


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
        if(running.get()) stopAutoPlay();
        auto_play_map.clear();
        if(forecasts != null) Arrays.fill(forecasts, null);
        running.set(false);
        paused = false;
    }

    public void pauseAutoPlay(int forecast_max_count) {
        if(mAutoPlayChanges == null){
            XLog.e("AutoPlayChanges", "is null");
            return;
        }
        this.forecast_max_count = forecast_max_count;
        forecasts = new View[forecast_max_count];
        practical_request = new int[3];
        paused = true;
    }

    protected void setFramePractical(int forecast_max, int forecast_offset, int autoplay_index, boolean request){
        for (int i = forecast_offset; i < forecast_max; i++) {
            if(autoplay_index >= auto_play_map.size()) break;
            int[] frame = auto_play_map.get(autoplay_index);
            switch (frame[FRAME_TYPE]) {
                case FRAME_TYPE_ON:
                case FRAME_TYPE_TOUCH:
                case FRAME_TYPE_CHAIN:
                {
                    View view = mAutoPlayChanges.getViewShowPracticalMark(frame[FRAME_PAD_X], frame[FRAME_PAD_Y]);
                    forecasts[i] = view;
                    if(request) {
                        if(i == 0) {
                            practical_request[0] = frame[FRAME_VALUE];
                            practical_request[1] = frame[FRAME_PAD_X];
                            practical_request[2] = frame[FRAME_PAD_Y];
                            XLog.e("Request touch", Arrays.toString(practical_request));
                        } else {
                            if(practical_request[0] == frame[FRAME_VALUE]
                            && practical_request[1] == frame[FRAME_PAD_X]
                            && practical_request[2] == frame[FRAME_PAD_Y]
                            ) continue;
                        }
                    }
                    setPractical_request(view, i); //Set color
                    break;
                }
                default: {
                    i--;
                    break;
                }
            }
            autoplay_index++;
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
    protected void checkFramePractical(MakePads.PadInfo pad) {
        boolean chain_request = practical_request[0] == -1;
        boolean is_chain = pad.getType() == MakePads.PadInfo.PadInfoIdentifier.CHAIN;

        XLog.e("Check Frame Practical", (is_chain) ? "Chain" : "Pad");

        if (pad.getRow() == practical_request[1] && pad.getColum() == practical_request[2] &&
                (mAutoPlayChanges.getCurrentChainProperties().getMc() == practical_request[0] ||
                        practical_request[0] == 0 || (is_chain && chain_request))) {
            XLog.e("Accept Button Practical", (is_chain) ? "Chain" : "Pad");

            removeForecast();
            setFramePractical(forecast_max_count, 0, autoplay_index++, true);
        } else if (is_chain){
            if (!(mAutoPlayChanges.getCurrentChainProperties().getRow() == pad.getRow() &&
                    mAutoPlayChanges.getCurrentChainProperties().getColum() == pad.getColum()) &&
                    !chain_request) {
                XLog.e("Request chain", "Chain");

                removeForecast();
                int[] request_chain = MakePads.PadID.getChainXY(practical_request[0], 9);
                practical_request[0] = -1;
                practical_request[1] = request_chain[0];
                practical_request[2] = request_chain[1];
                setPractical_request(forecasts[0] = mAutoPlayChanges.getViewShowPracticalMark(request_chain[0], request_chain[1]), 0);
                setFramePractical(forecast_max_count, 1, (autoplay_index--)-1, false);
            }
        }
    }

    protected void removeForecast(){
        if (forecasts != null) {
            for (View v : forecasts) {
                if(v == null) continue;
                ((ImageView) v).setImageDrawable(mAutoPlayChanges.getSkinData().draw_btn_);
                v.setAlpha(0f);
            }
        }
    }

    protected void touchInChain(int mc){
        int[] xy = MakePads.PadID.getChainXY(mc, 9);
        context.runOnUiThread(() -> Ui.Touch.touchAndRelease(mAutoPlayChanges.getPadToTouch(xy[0], xy[1])));
    }

    @Override
    public boolean stopAutoPlay() {
        running.set(false);
        paused = false;
        autoplay_index = 0;
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
        (mThread = new Thread(this)).start();
        mAutoPlayChanges.onStarted(callInterface = (chain, pad)->{
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
        if(autoplay_index + ADVANCE_FRAME_DISTANCE >= auto_play_map.size()){
            stopAutoPlay();
        } else {
            autoplay_index += ADVANCE_FRAME_DISTANCE;
        }
        if(isPaused()){
            removeForecast();
            setFramePractical(forecast_max_count, 0, autoplay_index++, true);
            touchInChain(practical_request[0]);
        }
        return autoplay_index;
    }

    @Override
    public float regressAutoPlay() {
        if(autoplay_index - REGRESS_FRAME_DISTANCE < 0){
            autoplay_index = 0;
        } else {
            autoplay_index -= REGRESS_FRAME_DISTANCE;
        }
        if(isPaused()){
            removeForecast();
            setFramePractical(forecast_max_count, 0, autoplay_index++, true);
            touchInChain(practical_request[0]);
        }
        return autoplay_index;
    }

    @Override
    public void run() {
        for (;autoplay_index < auto_play_map.size() &&
                !paused && running.get(); autoplay_index++) {

            int[] frame = auto_play_map.get(autoplay_index);

            long delay = SystemClock.uptimeMillis() + frame[FRAME_AUTOPLAY_DELAY];
            while (SystemClock.uptimeMillis() < delay && running.get() && !paused) {}

            if(running.get()) {
                if (frame[FRAME_VALUE] != mAutoPlayChanges.getCurrentChainProperties().getMc()) {
                    touchInChain(frame[FRAME_VALUE]);
                }

                View pad_touch = mAutoPlayChanges.getPadToTouch(frame[FRAME_PAD_X], frame[FRAME_PAD_Y]);
                if(pad_touch == null){
                    Log.e("Pad error", Arrays.toString(frame));
                } else {
                    switch (frame[FRAME_TYPE]) {
                        case FRAME_TYPE_ON: {
                            context.runOnUiThread(() -> Ui.Touch.touch(pad_touch));
                            break;
                        }
                        case FRAME_TYPE_OFF: {
                            context.runOnUiThread(() -> Ui.Touch.release(pad_touch));
                        }
                        case FRAME_TYPE_TOUCH:
                        case FRAME_TYPE_CHAIN: {
                            context.runOnUiThread(() -> Ui.Touch.touchAndRelease(pad_touch));
                        }
                    }
                }
            }
        }

        if (autoplay_index >= auto_play_map.size()) {
            stopAutoPlay();
        } else if (paused) {
            setFramePractical(forecast_max_count, 0, autoplay_index++, true);
        }
    }

    @Override
    public boolean call(MakePads.ChainInfo chain, MakePads.PadInfo pad) {
        return startAutoPlay();
    }

    /*Progress bar*/
    public VerticalSeekBar getProgress_bar(){
        return progress_bar;
    }

    protected void setDefaultForProgressBar(VerticalSeekBar bar){
        bar.setMax(auto_play_map.size());
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
