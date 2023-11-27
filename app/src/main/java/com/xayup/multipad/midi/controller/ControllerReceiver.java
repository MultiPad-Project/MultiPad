package com.xayup.multipad.midi.controller;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.util.Log;
import com.xayup.midi.controllers.LaunchpadProMK2;

import java.io.IOException;
import java.util.Arrays;

public abstract class ControllerReceiver {

    protected MidiOutputPort output;
    protected Receiver receiver;
    protected ControllerManager.DeviceCfg deviceCfg;

    protected class Receiver extends MidiReceiver {
        int STATUS, MESSAGETYPE, NOTE, VELOCITY;
        int[] xy;

        @Override
        public void onSend(byte[] bytes, int offset, int count, long timestamp) throws IOException {
            STATUS = bytes[offset] & 0xFF;
            //final int CHANNEL = STATUS & 0x0F;
            MESSAGETYPE = (STATUS & 0xF0) >> 4;
            NOTE = bytes[1 + offset] & 0xFF;
            VELOCITY = bytes[2 + offset] & 0xFF;
            xy = deviceCfg.launchpadCfg.noteToXY.notToXY(NOTE);
            showLed(xy[0], xy[1], (MESSAGETYPE == 9 && VELOCITY > 0) ? VELOCITY : 0);
        }
    }
    
    public ControllerReceiver(ControllerManager.DeviceCfg deviceCfg, MidiOutputPort output){
        this.deviceCfg = deviceCfg;
        this.output = output;
    }

    public void start(){
        output.connect(receiver = new Receiver());
    }

    public void stop(){
        output.disconnect(receiver);
    }

    public abstract void showLed(int row, int colum, int velocity);
}
