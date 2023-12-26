package com.xayup.multipad.midi.controller;

import android.content.Context;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ControllerReceiver {

    protected Context context;
    protected MidiOutputPort output;
    protected Receiver receiver;
    protected ControllerManager.DeviceCfg deviceCfg;
    protected ProcessData processor;

    protected class ProcessData extends Thread {
        protected LinkedBlockingQueue<byte[]> messages;

        protected int STATUS, MESSAGETYPE, NOTE, VELOCITY;
        protected int[] xy;
        protected byte[] bytes;

        boolean running;

        public ProcessData(){
            messages = new LinkedBlockingQueue<>();
        }

        @Override
        public void run() {
            running = true;
            Log.v("ProcessorData", "Started");
            while(running) {
                try {
                    if ((bytes = messages.take()) != null) {
                        STATUS = bytes[0] & 0xFF;
                        //final int CHANNEL = STATUS & 0x0F;
                        MESSAGETYPE = (STATUS & 0xF0) >> 4;
                        NOTE = bytes[1] & 0xFF;
                        VELOCITY = bytes[2] & 0xFF;
                        xy = deviceCfg.launchpadCfg.noteToXY.notToXY(NOTE);
                        //Log.v("MIDI Message", "Status: " + STATUS + ", Message Type: " + MESSAGETYPE + ", Note: " + NOTE + " " + Arrays.toString(xy) + ", Velocity: " + VELOCITY);
                        showLed(xy[0], xy[1], ((STATUS >= 144 && STATUS <= 176) && VELOCITY > 0) ? VELOCITY : 0);
                    }
                } catch(InterruptedException ie){
                    ie.printStackTrace(System.out);
                }
            }
            Log.e("ProcessorData", "Stopped");
            interrupt();
        }

        public void add(byte data1, byte data2, byte data3){
            messages.offer(new byte[]{data1, data2, data3});
        }

        @Override
        public void interrupt(){
            running = false;
            super.interrupt();
        }
    }

    protected class Receiver extends MidiReceiver {
        public Receiver () {
            processor = new ProcessData();
            processor.start();
        }

        @Override
        public void onSend(byte[] bytes, int offset, int count, long timestamp) throws IOException {
            //Log.v("MIDI data", Arrays.toString(bytes) + "\noffset " + offset);
            processor.add(bytes[offset++], bytes[offset++], bytes[offset]);
        }
    }
    
    public ControllerReceiver(Context context, ControllerManager.DeviceCfg deviceCfg, MidiOutputPort output){
        this.context = context;
        this.deviceCfg = deviceCfg;
        this.output = output;
    }

    public void start(){
        output.connect(receiver = new Receiver());
    }

    public void stop(){
        output.disconnect(receiver);
        processor.interrupt();
    }

    public abstract void showLed(int row, int colum, int velocity);
}
