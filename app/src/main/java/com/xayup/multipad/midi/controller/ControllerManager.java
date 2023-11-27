package com.xayup.multipad.midi.controller;

import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiOutputPort;
import androidx.annotation.NonNull;
import com.xayup.midi.controllers.MidiKeyboard;
import com.xayup.midi.types.Devices;

import java.io.IOException;

public abstract class ControllerManager {

    protected ControllerSend send;
    protected ControllerReceiver receiver;

    protected MidiInputPort inputPort;
    protected MidiOutputPort outputPort;

    public final DeviceCfg deviceCfg = new DeviceCfg();

    public class DeviceCfg {
        protected Devices.GridDeviceConfig launchpadCfg = MidiKeyboard.configs;
        public void setLaunchpadCfg(@NonNull Devices.GridDeviceConfig device){
            this.launchpadCfg = device;
        }

        public Devices.GridDeviceConfig getLaunchpadCfg() {
            return launchpadCfg;
        }
    }

    public ControllerManager(MidiDevice midiDevice, MidiDeviceInfo.PortInfo input, MidiDeviceInfo.PortInfo output){
        if(input != null){
            this.inputPort = midiDevice.openInputPort(input.getPortNumber());
            send = new ControllerSend(deviceCfg, inputPort);
        }
        if(output != null){
            this.outputPort = midiDevice.openOutputPort(output.getPortNumber());
            this.receiver = new ControllerReceiver(deviceCfg, outputPort) {
                @Override
                public void showLed(int row, int colum, int velocity) {
                    received(row, colum, velocity);
                }
            };
            this.receiver.start();
        }
    }

    public boolean send(int row, int colum, int velocity){
        if(send != null){
            try{
                send.sendNote(row, colum, velocity);
                return true;
            } catch (IOException e){
                e.printStackTrace(System.out);
                return false;
            }
        }
        return false;
    }


    public abstract void received(int row, int colum, int velocity);

    public final void close(){

    }
}
