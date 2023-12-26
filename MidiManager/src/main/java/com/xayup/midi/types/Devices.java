package com.xayup.midi.types;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

public class Devices {

    public static class DeviceKeyID {
        protected final Object keyID;
        public DeviceKeyID(Object[][] keyID){ this.keyID = keyID; }
        public DeviceKeyID(int keyID){ this.keyID = keyID; }

        public Object getId(int x, int y){
            return (isArray()) ? ((Object[][])keyID)[x][y] : keyID; };

        public boolean isArray(){ return  keyID.getClass().isArray(); }
    }

    public static class KeyID{
        protected final int[] key;
        public KeyID(@KeyType byte type){ key = new int[]{type, 0}; }
        public KeyID(int x, int y){ key = new int[]{x, y}; }
        public KeyID(int id){ key = new int[]{id, -127}; }

        /**
         * @return -1 if XY
         */
        public int getId(){ return (isArray()) ? -1: key[0]; }

        public String getType(){
            switch (key[0]){
                case KeyType.Note:          return "Note";
                case KeyType.CC:            return "Control Change";
                case KeyType.Sysex:         return "Sys-ex";
                case KeyType.SPECIAL_LED:   return "Special Led";
                case KeyType.CHAIN:         return "Chain";
            }
            return null;
        }

        public int[] getXY(){ return key; }

        public boolean isArray(){ return key[1] != -127; }
    }

    public interface NoteToXY { int[] notToXY(int note); }
    public interface RgbSysexGen { int[] rgbSysexGen(int[][] keyID, int[][][] color); }

    @IntDef({KeyType.Note, KeyType.CC, KeyType.Sysex, KeyType.SPECIAL_LED, KeyType.CHAIN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface KeyType {
        byte Note = 0;
        byte CC = 1;
        byte Sysex = 2;
        byte SPECIAL_LED = 3;
        byte CHAIN = 4;
    }

    public static class GridDeviceConfig {
        public String name = null;
        public Map<String, Integer> paletteChannel;
        public String midiNameRegex = "";
        public Object[][] keymap;
        /** [Width, Height] */
        public int[] dimension;
        /** Grid Only: [Width, Height] */
        public int[] gridDimension;
        /** [X, Y] */
        public int[] gridOffset;
        public int[][] chainKey;
        public NoteToXY noteToXY;
        public @Nullable KeyID specialLED;
        public @Nullable RgbSysexGen rgbSysexGen;
        public byte[][] initializationSysex;
    }

    public static class MidiDevice {

        public final UsbDevice usbDevice;
        public final String name;
        public final UsbInterface usbInterface;
        public final UsbEndpoint input;
        public final UsbEndpoint output;
        public final GridDeviceConfig config;

        public MidiDevice(
                @NonNull UsbDevice usbDevice,
                String name,
                UsbInterface usbInterface,
                UsbEndpoint input,
                UsbEndpoint output,
                @NonNull GridDeviceConfig config
        ) {
            this.usbDevice = usbDevice;
            this.name = name;
            this.usbInterface = usbInterface;
            this.input = input;
            this.output = output;
            this.config = config;
        }
    }
}
