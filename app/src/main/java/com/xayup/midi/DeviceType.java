package com.xayup.midi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

public class DeviceType {
    public interface NoteToXY { int[] notToXY(int note); }
    public interface RgbSysexGen { int[] rgbSysexGen(int[][] keyID, int[][][] color); }

    @StringDef({KeyType.Note, KeyType.CC, KeyType.Sysex})
    @Retention(RetentionPolicy.SOURCE)
    @interface KeyType {
        String Note = "N";
        String CC = "C";
        String Sysex = "X";
    }

    public static class DeviceKeyID {
        final Object id;
        public DeviceKeyID(int id){ this.id = id; }
        public DeviceKeyID(Map<KeyType, Integer> id){ this.id = id; }
    }

    public abstract static class GridDeviceInfo {
        public String name;
        public Map<String, Integer> paletteChannel;
        public @Nullable String midiNameRegex;
        public int[][] keymap;
        /** [Width, Height] */
        public int[] dimension;
        /** Grid Only: [Width, Height] */
        public int[] gridDimension;
        /** [X, Y] */
        public int[] gridOffset;
        public int[][] chainKey;
        public NoteToXY noteToXY;
        public @Nullable int[] specialLED;
        public @Nullable RgbSysexGen rgbSysexGen;
        public int[][] initializationSysex;
    }
}
