package com.xayup.multipad.projects.project.keyled;

import android.graphics.Color;
import android.util.Log;
import com.google.common.io.Files;
import com.xayup.debug.XLog;
import com.xayup.multipad.projects.thread.LoadProject;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.projects.project.MapData;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyLEDReader implements MapData {
    public boolean checkLedFrameFormat(String line) {
        return line.matches("");
    }

    public void read(File[] leds, KeyLEDMap map, LoadProject.LoadingProject mLoadingProject) {
        if (leds != null){
            String file_name = "";
            boolean checked_name = false;
            int chain = 0;
            int x = 0;
            int y = 0;
            boolean loop = false;
            List<List<String>> leds_list = new ArrayList<>();
            for (File led : leds) {
                if (led != null) {
                    if (!checked_name) {
                        String[] chars = (file_name = led.getName()).split("\\s");
                        XLog.v("Name", file_name);
                        mLoadingProject.onStartReadFile(file_name);
                        try {
                            if (chars.length < 3) throw new NumberFormatException();
                            chain = Integer.parseInt(chars[0]);
                            x = Integer.parseInt(chars[1]);
                            y = Integer.parseInt(chars[2]);
                            loop = chars[3].equals("0");

                        } catch (NumberFormatException n) {
                            mLoadingProject.onFileError(leds[0].getName(), 0, "Name format");
                            return;
                        }
                    }
                    try {
                        leds_list.add(Files.readLines(led, StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        leds_list.add(null);
                        Log.e("KeyLEDReader: ", e.toString());
                    }
                }
            }
            /*Synchronize leds*/
            KeyLEDData ledData = map.newFrameData();
            ledData.setTypeLoop(loop);
            int[] delays = new int[leds_list.size()];
            int delay = 0;
            int line;
            while(true) {
                line = 0;
                list_loop: for (int i = 0; i < leds_list.size(); i++) {
                    while (!leds_list.get(i).isEmpty()) {
                        if(delays[i] > 0) break;
                        String line_string;
                        String[] chars = (line_string = leds_list.get(i).remove(0)).split("\\s");
                        if (chars.length < 2) continue;
                        int type;
                        int value;
                        int pad_x;
                        int pad_y;
                        try {
                            switch (chars[0]) {
                                case "delay":
                                case "d": {
                                    if (chars[1].equals("0")) continue;
                                    delays[i] = Integer.parseInt(chars[1]);
                                    continue list_loop;
                                }
                                case "on":
                                case "o": {
                                    type = FRAME_TYPE_ON;
                                    break;
                                }
                                case "off":
                                case "f": {
                                    if(chars[1].matches("[m|M][c|C]|\\*")){
                                        int[] xy = MakePads.PadID.getChainXY(Integer.parseInt(chars[2]), 1);
                                        ledData.putFrame(FRAME_TYPE_OFF, 0, xy[0], xy[1], i);
                                    } else if(chars[1].equalsIgnoreCase("l")) {
                                        ledData.putFrame(FRAME_TYPE_OFF, 0, 0, 9, i);
                                    } else {
                                        ledData.putFrame(FRAME_TYPE_OFF, 0, Integer.parseInt(chars[1]), Integer.parseInt(chars[2]), i);
                                    }
                                    continue;
                                }
                                default: {
                                    continue;
                                }
                            }
                            if (chars[1].equalsIgnoreCase("l")) {
                                pad_x = 0;
                                pad_y = 9;
                                value = (chars[2].matches("(\\d|[a-f]|[A-F]){6}")) ?
                                        Color.parseColor("#"+chars[2]) : Integer.parseInt(chars[3]);
                            } else {
                                if (chars[1].matches("[m|M][c|C]|\\*")) {
                                    int mc = Integer.parseInt(chars[2]);
                                    if (mc > 24) {
                                        pad_x = 33 - mc;
                                        pad_y = 0;
                                    } else if (mc > 16) {
                                        pad_x = 9;
                                        pad_y = 25 - mc;
                                    } else if (mc > 8) {
                                        pad_x = mc - 8;
                                        pad_y = 9;
                                    } else {
                                        pad_x = 0;
                                        pad_y = mc;
                                    }
                                } else {
                                    pad_x = Integer.parseInt(chars[1]);
                                    pad_y = Integer.parseInt(chars[2]);
                                }
                                value = (chars[3].matches("(\\d|[a-f]|[A-F]){6}")) ?
                                        Color.parseColor("#"+chars[3]) : Integer.parseInt(chars[4]);
                            }
                            ledData.putFrame(type, value, pad_x, pad_y, i);
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
                            mLoadingProject.onFileError(file_name, line, "Invalid Led Format: " + line_string);
                            Log.e(String.valueOf(Arrays.toString(chars)), e.toString());
                        }
                        line++;
                    }
                }
                /*Get the smallest delay*/
                for(int d : delays) {
                    if (d > 0 && (delay < 1 || d < delay)) {
                        delay = d;
                    }
                }
                if(delay < 1) break;
                /*Subtract the delays by the smallest delay obtained*/
                for(int i = 0; i < delays.length; i++){
                    delays[i] = delays[i] - delay;
                }
                ledData.putFrame(FRAME_TYPE_DELAY, delay, 0, 0, 0);
                delay = 0;
            }
            leds_list.clear();
            map.putSequence(chain, x, y, ledData);
        }
    }

    public interface KeyLEDMap {
        public void putSequence(
                int chain,
                int x,
                int y,
                KeyLEDData ledData);
        public KeyLEDData newFrameData();
        public KeyLEDData getLedData(int chain, int x, int y, int sequence);
        public KeyLEDData getLedData(int chain, int x, int y);
        public int framesCount(int chain, int x, int y, int sequence);
        public int sequenceCount(int chain, int x, int y);
        public int ledsCount();
        public void clear();
    }
}
