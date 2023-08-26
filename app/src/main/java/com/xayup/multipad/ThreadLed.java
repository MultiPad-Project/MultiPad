package com.xayup.multipad;

import android.app.*;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import com.xayup.multipad.pads.Render.MakePads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.*;

public class ThreadLed implements Runnable {
    private AtomicBoolean running = new AtomicBoolean(false);
    private Activity context;
    private String cpled;
    private int chain;
    private final int rpt;
    private final MakePads.Pads mPads;
    private int loop = -1;

    public ThreadLed(final Context context, final int chain, int padid, final int rpt, MakePads.Pads mPads) {
        this.context = (Activity) context;
        this.cpled = String.valueOf(chain) + padid;
        this.chain = chain;
        this.rpt = rpt;
        this.mPads = mPads;
    }

    boolean isRunning() {
        return running.get();
    }
    ;

    protected void stop() {
        try {
            running.set(false);
        } catch (NullPointerException n) {
        }
    }

    public void runn() {
        run();
    }

    protected void start() {
        running.set(true);
        new Thread(this).start();
    }
    // Quando o tipo de led e 0: (2 5 1 0 a)
    //                                 ^
    public void stopZeroLooper() {
        if (loop == 0) {
            stop();
        }
    }

    private void offCurrentLedLoop(List<String> led) {
        boolean mc = false; /* chainLed */
        int padId; /* padLed */
        int corcode = 0; /* Color code. 0 = OFF */
        /*
         * haveOff sera usado para armazenar as pads
         * que ja foram desligados para evitar o mesmo
         * processo em uma pad ja processada
         */
        List<Integer> haveOff = new ArrayList<Integer>();
        read:
        for (String line : led) {
            switch (line.substring(0, 1)) {
                case "o":
                    if (line.contains("mc")) {
                        mc = true;
                        padId =
                                VariaveisStaticas.chainCode[
                                        Integer.parseInt(line.substring(3, line.indexOf("a")))];
                    } else if (line.toLowerCase().contains("l")) {
                        padId = 9;
                    } else {
                        padId = Integer.parseInt(line.substring(1, 3));
                    }
                    if (haveOff.contains(padId)) {
                        continue read;
                    } else {
                        haveOff.add(padId);
                        showLed(padId, corcode, 0, mc, false);
                    }
                    break;
            }
        }
        haveOff = null;
    }

    private void showLed(
            final int padid,
            final int color,
            final int color_velocity,
            final boolean MC,
            final boolean hex) {
        context.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        //View pad = context.findViewById(padid);
                        byte NOTE = MidiStaticVars.NOTE_ON;
                        int row = padid/10;
                        int colum = padid%10;
                        if (color_velocity == 0) NOTE = MidiStaticVars.NOTE_OFF;
                        if (PlayPads.glowEf) {
                            ImageView glowEF = mPads.getGlows().getGlow(row, colum);
                            if(color == 0){
                                glowEF.setAlpha(0f);
                                glowEF.setColorFilter(null);
                            } else {
                                if(MC){
                                    glowEF.setAlpha(PlayPads.glowChainIntensity/100f);
                                } else {
                                    glowEF.setAlpha(PlayPads.glowPadIntensity/100f);
                                }
                                glowEF.setColorFilter(color);
                            }
                        }
                        mPads.getPadView(row, colum).findViewById(MakePads.PadInfo.PadLayerType.LED).setBackgroundColor(color);
                        if(MidiStaticVars.midiMessage != null){
                            MidiStaticVars.midiMessage.send((MidiStaticVars.midiOutputReceiver == null) ? MidiStaticVars.MIDI_INPUT : MidiStaticVars.MIDI_RECEIVER, padid, 1, NOTE, color_velocity);
                        }
                    }
                });
    }

    @Override
    public void run() {
        if (PlayPads.ledFiles.get(cpled) != null) {
            long time = SystemClock.uptimeMillis();
            boolean nobreak = true;
            boolean delay = false;
            int indexLoop = 1;
            int padId;
            int corcode;
            int color_velocity;
            int substring_index;
            boolean hex;
            boolean mc;
            try {
                loop = Integer.parseInt(PlayPads.ledFiles.get(cpled).get(rpt).get(0));
            } catch (NumberFormatException n) {
                loop = 1;
            } catch (IndexOutOfBoundsException i) {
            }
            String line;
            looper:
            while (true) {
                for (int i = 1; i < PlayPads.ledFiles.get(cpled).get(rpt).size(); i++) {
                    line = PlayPads.ledFiles.get(cpled).get(rpt).get(i);
                    if (!isRunning() || PlayPads.stopAll) {
                        if (loop == 0) offCurrentLedLoop(PlayPads.ledFiles.get(cpled).get(rpt));
                        else XayUpFunctions.clearLeds(context, mPads);
                        break looper;
                    }
                    //	String line = PlayPads.ledFiles.get(cpled).get(rpt).get(i);
                    delay = false;
                    padId = 0;
                    corcode = 0;
                    color_velocity = 0;
                    mc = false;
                    hex = false;
                    switch (line.substring(0, 1)) {
                        case "o":
                            substring_index = line.lastIndexOf("a");
                            if (substring_index == -1) {
                                substring_index = line.length() - 6;
                                hex = true;
                            }
                            if (line.contains("mc")) {
                                mc = true;
                                padId =
                                        VariaveisStaticas.chainCode[
                                                Integer.parseInt(
                                                        line.substring(3, substring_index))];
                            } else if (line.toLowerCase().contains("l")) {
                                padId = 9;
                            } else {
                                padId = Integer.parseInt(line.substring(1, 3));
                            }
                            if (hex) {
                                String hex_code = line.substring(line.length() - 6);
                                corcode =
                                        (hex_code.equals("000000"))
                                                ? 0
                                                : Color.parseColor(
                                                        "#" + hex_code);
                            } else {
                                color_velocity =
                                        Integer.parseInt(line.substring(substring_index + 1));
                                corcode =
                                        VariaveisStaticas.colorInt(
                                                chain,
                                                color_velocity,
                                                PlayPads.custom_color_table,
                                                PlayPads.oldColors);
                            }
                            break;
                        case "f":
                            if (line.contains("mc")) {
                                mc = true;
                                //	System.out.println(line);
                                padId =
                                        VariaveisStaticas.chainCode[
                                                Integer.parseInt(line.substring(3))];
                            } else if (line.contains("l")) {
                                padId = 9;
                            } else {
                                padId = Integer.parseInt(line.substring(1));
                            }
                            break;
                        case "d":
                            time = SystemClock.uptimeMillis();
                            time += Integer.parseInt(line.substring(1));
                            delay = true;
                            break;
                    }
                    while ((SystemClock.uptimeMillis() < time)
                            && (!PlayPads.stopAll)
                            && isRunning()) {}

                    if (!delay) {
                        showLed(padId, corcode, color_velocity, mc, hex);
                    }
                }
                if (loop != 0) {
                    if (indexLoop < loop) {
                        indexLoop++;
                    } else {
                        break;
                    }
                }
            }
        }
    }
}
