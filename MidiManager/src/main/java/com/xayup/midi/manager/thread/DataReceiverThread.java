package com.xayup.midi.manager.thread;


import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DataReceiverThread extends AsyncTask<Void, Void, Void> {

    //protected Context context;

    protected UsbDeviceConnection usbDeviceConnection;
    protected UsbEndpoint usbInput;

    protected boolean running;

    public DataReceiverThread(Context context, UsbDeviceConnection usbDeviceConnection, UsbEndpoint usbInput){
        //this.context = context;
        this.usbDeviceConnection = usbDeviceConnection;
        this.usbInput = usbInput;
        this.running = false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i("USB receiver", "Started");
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        Log.i("USB receiver", "Finished");
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        running = true;
        byte[] bytes = new byte[4];
        while(running) {
            Arrays.fill(bytes, (byte) 0);
            if (usbDeviceConnection.bulkTransfer(usbInput, bytes, bytes.length, 0) > 0) {
                dataReceived(bytes);
            }
        }

        return null;
    }

    public void start(){
        this.execute();
    }

    public void stop(){
        running = false;
    }

    public abstract void dataReceived(byte[] bytes);

}
