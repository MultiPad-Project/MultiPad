package com.xayup.multipad.midi.controller;

import android.media.midi.MidiInputPort;
import android.util.Log;
import com.xayup.midi.types.Devices;

import java.io.IOException;
import java.util.Arrays;

public class ControllerSend {

    protected MidiInputPort input;
    protected ControllerManager.DeviceCfg deviceCfg;

    public ControllerSend(ControllerManager.DeviceCfg deviceCfg, MidiInputPort input){
        this.deviceCfg = deviceCfg;
        this.input = input;
    }

    public void sendNote(int row, int colum, int velocity)throws IOException {
        Log.v("app to midi device message", "input: " + input + ", KeyMap: " + deviceCfg.launchpadCfg.keymap + ", Palette Channel " + deviceCfg.launchpadCfg.paletteChannel);
        if(input != null && deviceCfg.launchpadCfg.keymap != null && deviceCfg.launchpadCfg.paletteChannel != null) {
            Object note = deviceCfg.launchpadCfg.keymap[row][colum];
            if (note == null) return;
            boolean CC = false;
            if (note instanceof Devices.KeyID) {
                CC = ((Devices.KeyID) note).getType().equals("Control Change");
                note = ((Devices.KeyID) note).getId();
            }
            int status = (CC ? 0xB0 : (velocity == 0) ? 0x80 : 0x90);
            byte[] bytes = new byte[]{
                (byte) ((status >> 4) & 0xFF),                                                              //Status
                    (byte) ((status + (deviceCfg.launchpadCfg.paletteChannel.get("classic") - 1)) & 0xFF), //Channel
                    (byte) (((int) note) & 0xFF),                                                           //Note
                    (byte) (velocity & 0xFF)                                                                //Pressure
            };
            Log.v("Data", "row: " + row + ", colum: " + colum + ", velocity: " + velocity);
            Log.v("Send MIDI", Arrays.toString(bytes));
            input.send(bytes, 0, bytes.length, 0);
            Log.v("MIDI send", "Completed!");
        }
    }

}
