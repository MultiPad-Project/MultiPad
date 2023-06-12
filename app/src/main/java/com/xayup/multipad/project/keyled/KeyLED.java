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
import java.util.*;
import java.util.concurrent.atomic.*;

public class KeyLED extends Project implements MapData, Project.KeyLEDInterface, PadPressCallInterface, Runnable {
    protected Activity context;
    protected LedMap mLedMap;
    protected AtomicBoolean running;
    protected Thread led_thread;
    protected List<List<int[]>> leds_standby;
    protected List<int[]> delays;
    protected final int DELAYS_INDEX = 0;
    protected final int DELAYS_DELAY = 1;
    protected Colors colors;
    public KeyLED(Activity context){
        this.context = context;
        this.mLedMap = new LedMap();
        this.running = new AtomicBoolean(false);
        this.leds_standby = new ArrayList<>();
        this.delays = new ArrayList<>();
        this.colors = new Colors(context);
        led_thread = new Thread(this);
    }
    
    public void parse(File keyled_file, LoadProject.LoadingProject mLoadingProject){
        new KeyLEDReader().read(keyled_file, mLedMap, mLoadingProject);
    }

    @Override
    public boolean showLed(int chain, int x, int y) {
        XLog.v("Try show led", "");
        int[][] frames = mLedMap.getLedData(chain, x, y, 0);
        if(frames != null && frames.length > 0) {
            XLog.v("Led", Arrays.deepToString(frames));
            leds_standby.add(new ArrayList<>(List.of(frames)));
            if(!running.get()) {
                running.set(true);
                led_thread.start();
            }
        }
        return false;
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
    public void call(int chain, int x, int y) {
        showLed(chain, x, y);
    }

    @Override
    public void run() {
        XLog.v("Run thread led", "");
        int led_index = 0;
        int delay_array_index = 0;
        int delay_array_delay = 0;
        int delay_index = 0;
        while(running.get()) {
            leds_standby: while (!leds_standby.isEmpty()) {
                List<int[]> frames = leds_standby.get(delay_array_index);
                if (frames.isEmpty()) {
                    leds_standby.remove(frames);
                    continue;
                } else {
                    int[] frame = null;
                    while (!frames.isEmpty()) {
                        frame = frames.remove(0);
                        if(frame[FRAME_TYPE] == FRAME_TYPE_DELAY) {
                            delays.add(new int[]{delay_array_index, frame[FRAME_VALUE]});
                            continue leds_standby;
                        } else {
                            onUi(frame[FRAME_PAD_X], frame[FRAME_PAD_Y], (byte) frame[FRAME_VALUE]);
                        }
                    }
                }
                if(!delays.isEmpty()) {
                    for (int[] delay_array : delays) {
                        if (delay_array[DELAYS_DELAY] > delay_array_delay) {
                            delay_array_index = delay_array[DELAYS_INDEX];
                            delay_array_delay = delay_array[DELAYS_DELAY];
                            delay_index = delays.indexOf(delay_array);
                        }
                    }
                    delays.remove(delay_index);
                }
            }
        }
        XLog.v("Finish thread led", "");
    }
    public void onUi(int x, int y, byte velocity){
        context.runOnUiThread(() -> {
            try {
                Log.v("ID", String.valueOf(MakePads.PadID.getId(x, y)));
                context.findViewById(MakePads.PadID.getId(x, y)).findViewById(R.id.led).setBackgroundColor(colors.colorFromVelocity(velocity));
            } catch (NullPointerException n) {
                Log.e("KeyLED", "Null led frame?");
            }
        });
    }
}
