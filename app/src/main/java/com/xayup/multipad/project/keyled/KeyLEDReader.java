package com.xayup.multipad.project.keyled;

import com.xayup.multipad.project.keyled.LedMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KeyLEDReader {
    public List<String[]> read(File keyled_files, LedMap map) {
        List<String[]> problems = new ArrayList<>();
        return problems;
    }

    public interface KeyLEDMap {
        public void putFrame(
                int chain, int pad, int sequence, int[ /*TYPE, VALUE, PAD_ID*/] led_frame);

        public void putSequence(
                int chain, int pad, int[][ /*TYPE, VALUE, PAD_ID*/] led_frames_sequence);

        public int[ /*FRAMES*/][ /*TYPE, VALUE, PAD_ID*/] getLedData(
                int chain, int pad, int sequence);

        public int framesCount(int chain, int pad, int sequence);

        public int sequenceCount(int chain, int pad);

        public int ledsCount();
    }
}
