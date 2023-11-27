package com.xayup.midi.manager.thread;


import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DataReceiverThread {

    //protected Context context;

    protected UsbDeviceConnection usbDeviceConnection;
    protected UsbEndpoint usbInput;

    protected AtomicBoolean running;

    public DataReceiverThread(Context context, UsbDeviceConnection usbDeviceConnection, UsbEndpoint usbInput){
        //this.context = context;
        this.usbDeviceConnection = usbDeviceConnection;
        this.usbInput = usbInput;
        this.running = new AtomicBoolean(false);
    }

    public void start(){
        running.set(true);
        Log.v("USB Receiver", "start called");
        new Thread(new Runnable(){
            @Override
            public void run(){
                Log.v("USB Receiver", "Thread started");
                byte[] bytes = new byte[4];
                int result;
                while(running.get()) {
                    Arrays.fill(bytes, (byte) 0);
                    result = usbDeviceConnection.bulkTransfer(usbInput, bytes, bytes.length, 1000);
                    Log.v("USB Receiver Result", String.valueOf(result));
                    if (result > 0) {
                        dataReceived(bytes);
                    }
                }
            }
        }).start();
    }

    public void stop(){
        running.set(false);
    }

    public abstract void dataReceived(byte[] bytes);

}
