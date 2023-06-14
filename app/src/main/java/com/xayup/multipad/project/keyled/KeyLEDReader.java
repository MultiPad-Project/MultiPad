package com.xayup.multipad.project.keyled;

import android.util.Log;
import com.google.android.exoplayer2.C;
import com.google.common.io.Files;
import com.xayup.debug.XLog;
import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.project.MapData;
import com.xayup.multipad.project.keyled.LedMap;
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

    public void read(File led, KeyLEDMap map, LoadProject.LoadingProject mLoadingProject) {
        if (led != null)
            try {
                List<String> led_lines = Files.readLines(led, StandardCharsets.UTF_8);
                int[][] frames = new int[led_lines.size()][5];
                mLoadingProject.onStartReadFile(led.getName());
                XLog.v("Led file: ", led.getName());
                char[] led_name_char = led.getName().toCharArray();
                int chain = 0;
                int x = 0;
                int y = 0;
                int loop = 0;
                int sequence = 0;
                int index = 0;
                String value = "";
                file_name_loop:
                for (int i = 0; i < led_name_char.length; i++) {
                    if (Character.isDigit(led_name_char[i])) {
                        value += Character.toString(led_name_char[i]);
                        continue;
                    }
                    if (Character.toString(led_name_char[i]).equals(" ")
                            || led_name_char.length - 1 >= led_name_char.length) {
                        switch (index) {
                            case 0:
                                {
                                    chain = Integer.parseInt(value);
                                    break;
                                }
                            case 1:
                                {
                                    x = Integer.parseInt(value);
                                    break;
                                }
                            case 2:
                                {
                                    y = Integer.parseInt(value);
                                    break;
                                }
                            case 3:
                                {
                                    loop = Integer.parseInt(value);
                                    break file_name_loop;
                                }
                        }
                        value = "";
                        index++;
                    }
                }
                sequence = map.sequenceCount(chain, x, y);
                index = 0;
                value = null;
                for (int f = 0; f < led_lines.size(); f++) {
                    String[] chars = led_lines.get(f).split("\\s");
                    if (chars.length < 2) continue;
                    switch (chars[0]) {
                        case "delay":
                        case "d":
                            {
                                frames[f][FRAME_TYPE] = FRAME_TYPE_DELAY;
                                frames[f][FRAME_VALUE] = Integer.parseInt(chars[1]);
                                continue;
                            }
                        case "on":
                        case "o":
                            {
                                frames[f][FRAME_TYPE] = FRAME_TYPE_ON;
                                break;
                            }
                        case "off":
                        case "f":
                            {
                                frames[f][FRAME_TYPE] = FRAME_TYPE_OFF;
                                frames[f][FRAME_PAD_X] = Integer.parseInt(chars[1]);
                                frames[f][FRAME_PAD_Y] = Integer.parseInt(chars[2]);
                                continue;
                            }
                    }
                    if (chars[1].equals("l")) {
                        frames[f][FRAME_PAD_X] = 0;
                        frames[f][FRAME_PAD_Y] = 9;
                        frames[f][FRAME_VALUE] = Integer.parseInt(chars[3]);
                    } else if (chars[1].matches("mc|\\*")) {
                        int mc = Integer.parseInt(chars[2]);
                        if (mc > 24){
                            frames[f][FRAME_PAD_X] = 33 - mc;
                            frames[f][FRAME_PAD_Y] = 0;
                        } else
                        if (mc > 16) {
                            frames[f][FRAME_PAD_X] = 9;
                            frames[f][FRAME_PAD_Y] = 25 - mc;
                        } else if (mc > 8) {
                            frames[f][FRAME_PAD_X] = mc - 8;
                            frames[f][FRAME_PAD_Y] = 9;
                        } else {
                            frames[f][FRAME_PAD_X] = 0;
                            frames[f][FRAME_PAD_Y] = mc;
                        }
                        frames[f][FRAME_VALUE] = Integer.parseInt(chars[4]);
                    } else {
                        frames[f][FRAME_PAD_X] = Integer.parseInt(chars[1]);
                        frames[f][FRAME_PAD_Y] = Integer.parseInt(chars[2]);
                        frames[f][FRAME_VALUE] = Integer.parseInt(chars[4]);
                    }
                }
                map.putSequence(chain, x, y, frames);
            } catch (IOException e) {
                Log.e("KeyLEDReader: ", e.toString());
            }
    }

    public interface KeyLEDMap {
        public interface Variables {}

        public void putFrame(
                int chain, int x, int y, int sequence, int[ /*TYPE, VALUE, PAD_ID*/] led_frame);

        public void putSequence(
                int chain,
                int x,
                int y,
                int[ /*FRAME*/][ /*TYPE, VALUE, PAD_ID*/] led_frames_sequence);

        public int[ /*FRAMES*/][ /*TYPE, VALUE, PAD_ID*/] getLedData(
                int chain, int x, int y, int sequence);

        public int framesCount(int chain, int x, int y, int sequence);

        public int sequenceCount(int chain, int x, int y);

        public int ledsCount();
        
        public void clear();
    }
}
