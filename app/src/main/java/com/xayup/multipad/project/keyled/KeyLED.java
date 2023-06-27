package com.xayup.multipad.project.keyled;

import android.app.*;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.xayup.debug.XLog;
import com.xayup.multipad.R;
import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.pads.PadPressCallInterface;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.project.MapData;
import com.xayup.multipad.project.keyled.KeyLEDReader;
import com.xayup.multipad.load.Project;
import com.xayup.multipad.load.ProjectMapData;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.*;

public class KeyLED extends Project
        implements MapData, Project.KeyLEDInterface, PadPressCallInterface, Runnable {
    protected Activity context;
    protected LedMap mLedMap;
    protected AtomicBoolean running;
    protected boolean added_new_led;
    protected Thread led_thread;
    protected List<List<int[]>> leds_standby;
    protected List<List<int[]>> new_leds_standby;
    protected List<int[]> delays;
    protected Colors colors;
    protected ToShowLed mToShowLed;

    public interface ToShowLed {
        void showLed(int x, int y, int real_color, int lp_index);
    }

    public KeyLED(Activity context) {
        this.context = context;
        this.mLedMap = new LedMap();
        this.running = new AtomicBoolean(false);
        this.leds_standby = new ArrayList<>();
        this.new_leds_standby = new ArrayList<>();
        this.delays = new ArrayList<>();
        this.colors = new Colors(context);
        this.led_thread = new Thread(this);
        this.added_new_led = false;
        this.mToShowLed = null;
    }

    public void setColorTable(File color_table){
        try {
            colors.getTable(color_table);
        } catch (IOException io){
            XLog.e("Set color table error", io.toString());
        }
    }

    public void parse(File[] keyled_file, LoadProject.LoadingProject mLoadingProject) {
        new KeyLEDReader().read(keyled_file, mLedMap, mLoadingProject);
    }

    public void setToShowLed(ToShowLed mToShowLed){
        this.mToShowLed = mToShowLed;
    };

    @Override
    public boolean showLed(int chain, int x, int y) {
        XLog.v("Try show led", "");
        KeyLEDData ledData = mLedMap.getLedData(chain, x, y);
        if (ledData != null && ledData.length() > 0) {
            new_leds_standby.add(new ArrayList<>(List.of(ledData.getFrames())));
            added_new_led = true;
            if (!running.get()) {
                running.set(true);
                led_thread.start();
            }
            XLog.v("Success show led", "");

            return true;
        }
        XLog.v("Error show led", "");

        return false;
    }

    public void resetSequence(){
        mLedMap.resetSequencesIndex();
    }
    
    public void clear(){
        mLedMap.clear();
        leds_standby.clear();
        new_leds_standby.clear();
        running.set(false);
    }

    @Override
    public boolean breakLed(int chain, int x, int y) {
        return false;
    }

    @Override
    public boolean breakAll() {
        running.set(false);
        return false;
    }

    @Override
    public boolean call(int chain, int x, int y) {
        return showLed(chain, x, y);
    }

    @Override
    public void run() {
        XLog.v("Run thread led", "");
        long delay_array_delay = 0;
        while (running.get()) {
            while (new_leds_standby.isEmpty()) {}
            while (!new_leds_standby.isEmpty()) {
                delays.add(null);
                leds_standby.add(new_leds_standby.remove(0));
            }
            while (!leds_standby.isEmpty()) {
                for (int l = 0; l < leds_standby.size(); l++) {
                    if ((leds_standby.get(l) != null)) {
                        while ((!leds_standby.get(l).isEmpty())) {
                            if (delays.get(l) == null) {
                                int[] frame = leds_standby.get(l).remove(0);
                                if (frame[FRAME_TYPE] == FRAME_TYPE_DELAY) {
                                    delays.remove(l);
                                    delays.add(l, new int[]{frame[FRAME_VALUE]});
                                } else {
                                    if(mToShowLed != null) mToShowLed.showLed(
                                            frame[FRAME_PAD_X],
                                            frame[FRAME_PAD_Y],
                                            colors.colorFromVelocity((byte) frame[FRAME_VALUE]), frame[FRAME_LP_INDEX]);
                                    continue;
                                }
                            }
                            break;
                        }
                        if (leds_standby.get(l).isEmpty()) {
                            leds_standby.remove(l);
                            delays.remove(l);
                            l--;
                        }
                    }
                }
                if (!delays.isEmpty()) {
                    for (int d = 0; d < delays.size(); d++) {
                        if ((delays.get(d) != null)
                                && (delay_array_delay <= 0
                                || delays.get(d)[0] < delay_array_delay)) {
                            delay_array_delay = delays.get(d)[0];
                        }
                    }
                    long delay_time = SystemClock.uptimeMillis() + delay_array_delay;
                    while (SystemClock.uptimeMillis() < delay_time
                            && running.get()
                            && !added_new_led) {
                    }
                    added_new_led = false;
                    int time = 0;
                    if ((time = (int) (delay_time - SystemClock.uptimeMillis())) > 0) {
                        delay_array_delay -= time;
                    }
                    for (int d = 0; d < delays.size(); d++) {
                        if (delays.get(d) != null) {
                            delays.get(d)[0] -= delay_array_delay;
                            if (delays.get(d)[0] <= 0) {
                                delays.remove(d);
                                delays.add(d, null);
                            }
                        }
                    }
                }
                delay_array_delay = 0;
                if (!new_leds_standby.isEmpty()) {
                    break;
                }
            }
        }
        XLog.v("Finish thread led", "");
    }
}
