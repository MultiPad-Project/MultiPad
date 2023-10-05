package com.xayup.midi.manager;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiManager;
import android.os.Handler;
import android.os.Looper;
import com.xayup.midi.controllers.Index;
import com.xayup.midi.controllers.MidiKeyboard;
import com.xayup.midi.types.Devices;

import java.util.ArrayList;
import java.util.List;

public class DevicesManager {

    public interface OpenedDeviceCallback {
        void onDeviceOpened(MidiDevice device);
    }

    private final MidiDevicesAdapter devicesAdapter;
    private final MidiManager midiManager;

    // Callbacks
    protected List<OpenedDeviceCallback> openedMidiDeviceCallbacks;

    public DevicesManager(Context context){
        this.devicesAdapter = new MidiDevicesAdapter(context, midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE));
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

    public void openDevice(Devices.MidiDevice midiDevice){
        midiManager.openDevice(midiDevice.deviceInfo, (mDevice) -> {
            for(OpenedDeviceCallback callback : openedMidiDeviceCallbacks) callback.onDeviceOpened(mDevice);
        }, new Handler(Looper.getMainLooper()));
    }
}
