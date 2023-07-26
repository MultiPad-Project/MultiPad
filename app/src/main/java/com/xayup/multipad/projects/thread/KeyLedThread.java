package com.xayup.multipad.projects.thread;

import android.os.SystemClock;
import com.xayup.debug.XLog;
import com.xayup.multipad.projects.project.MapData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyLedThread implements Runnable, MapData {
    protected AtomicBoolean running;
    protected boolean added_new_led;
    protected Thread led_thread;
    protected List<LedFrames> leds_standby;
    protected List<LedFrames> new_leds_standby;
    protected List<int[]> delays;

    protected List<ShowLed> callbacks;

    public class LedFrames {
        int id;
        List<int[]> frames;

        public LedFrames(List<int[]> frames, int id){
            this.frames = frames;
            this.id = id;
        }
    }

    public void addCallback(ShowLed showLed){
        XLog.v("KeyLedThread", "Add Callback");
        callbacks.add(showLed);
    }

    public void removeCallback(ShowLed showLed){
        XLog.v("KeyLedThread", "Remove Callback");
        callbacks.remove(showLed);
    }


    public interface ShowLed {
        /**
         * @param row grid row
         * @param colum grid colum
         * @param color HEX (RGB) or Velocity(byte)
         * @param grid_id grid id
         */
        void onShowLed(int row, int colum, int color, int grid_id);
    }

    public KeyLedThread(){
        leds_standby = Collections.synchronizedList(new ArrayList<>());
        new_leds_standby = Collections.synchronizedList(new ArrayList<>());
        callbacks = new ArrayList<>();
        delays = new ArrayList<>();
        running = new AtomicBoolean(false);
        added_new_led = false;
    }

    public void loop(){
        running.set(true);
        (this.led_thread = new Thread(this)).start();
    }

    public void addLed(List<int[]> led_frames, int id){
        new_leds_standby.add(new LedFrames(led_frames, id));
        added_new_led = true;
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
                        while ((!leds_standby.get(l).frames.isEmpty())) {
                            if (delays.get(l) == null) {
                                int[] frame = leds_standby.get(l).frames.remove(0);
                                if (frame[FRAME_TYPE] == FRAME_TYPE_DELAY) {
                                    delays.remove(l);
                                    delays.add(l, new int[]{frame[FRAME_VALUE]});
                                } else {
                                    XLog.v("KeyLedThread: Thread", String.valueOf(callbacks.size()));
                                    for(ShowLed showLed : callbacks) {
                                        showLed.onShowLed(
                                                frame[FRAME_PAD_X],
                                                frame[FRAME_PAD_Y],
                                                frame[FRAME_VALUE],
                                                frame[FRAME_LP_INDEX]);
                                    }
                                    continue;
                                }
                            }
                            break;
                        }
                        if (leds_standby.get(l).frames.isEmpty()) {
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
