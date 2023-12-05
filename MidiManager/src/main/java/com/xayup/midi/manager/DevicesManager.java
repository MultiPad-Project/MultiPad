package com.xayup.midi.manager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.xayup.midi.BuildConfig;
import com.xayup.midi.controllers.Index;
import com.xayup.midi.controllers.MidiKeyboard;
import com.xayup.midi.types.Devices;

import java.util.ArrayList;
import java.util.List;

public class DevicesManager {

    public static final String PERMISSION_ACCESS_USB = BuildConfig.LIBRARY_PACKAGE_NAME.concat(".USB_PERMISSION");

    private Context context;
    public interface OpenedDeviceCallback {
        void onDeviceOpened(UsbDeviceConnection device);
    }

    private final MidiDevicesAdapter devicesAdapter;
    //private final MidiManager midiManager;
    private final UsbManager midiManager;

    // Callbacks
    protected List<OpenedDeviceCallback> openedMidiDeviceCallbacks;

    public DevicesManager(Context context){
        this.devicesAdapter = new MidiDevicesAdapter(this.context = context, midiManager = (UsbManager) context.getSystemService(Context.USB_SERVICE));
        this.openedMidiDeviceCallbacks = new ArrayList<>();
        devicesAdapter.updateList();
    }

    public MidiDevicesAdapter getListAdapter(){ return devicesAdapter; }

    //public MidiDevice getOpenedMidiDevice(){ return openedMidiDevice; }

    // Open midi device callbacks manager
    public boolean callWhenMidiDeviceOpened(OpenedDeviceCallback callback){
        return openedMidiDeviceCallbacks.add(callback); }
    public boolean removeCallWhenMidiDeviceOpened(OpenedDeviceCallback callback){;
        return openedMidiDeviceCallbacks.remove(callback); }

    public boolean openDevice(UsbDevice midiDevice){
        if(!midiManager.hasPermission(midiDevice)){
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent.getAction().equals(PERMISSION_ACCESS_USB)) {
                        //UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            openGrantedDevice(midiDevice);
                        } else {
                            Log.e("Usb permission", "Denied");
                        }
                    }
                    context.unregisterReceiver(this);
                }
            }, new IntentFilter(PERMISSION_ACCESS_USB));
            midiManager.requestPermission(midiDevice, PendingIntent.getBroadcast(context, 0, new Intent(PERMISSION_ACCESS_USB), PendingIntent.FLAG_MUTABLE));
            return false;
        } else { openGrantedDevice(midiDevice);}
        return true;
    }

    protected void openGrantedDevice(UsbDevice midiDevice){
        UsbDeviceConnection deviceConnection = midiManager.openDevice(midiDevice);
        Log.e("Usb permission", "Granted");
        if(deviceConnection != null)
            for(int i = 0; i < openedMidiDeviceCallbacks.size(); i++)
                openedMidiDeviceCallbacks.get(i).onDeviceOpened(deviceConnection);
    }
}
