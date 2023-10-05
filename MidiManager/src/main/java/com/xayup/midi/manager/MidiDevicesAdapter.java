package com.xayup.midi.manager;

import android.content.Context;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.xayup.midi.R;
import com.xayup.midi.controllers.Index;
import com.xayup.midi.controllers.MidiKeyboard;
import com.xayup.midi.types.Devices;

import java.util.ArrayList;
import java.util.List;

public class MidiDevicesAdapter extends BaseAdapter {

    private Context context;
    private MidiManager midiManager;

    private List<Devices.MidiDevice> devices;

    public MidiDevicesAdapter(Context context, MidiManager midiManager){
        this.context = context;
        this.midiManager = midiManager;
        this.devices = new ArrayList<>();
    }

    public void updateList(){
        devices.clear();
        for(MidiDeviceInfo device : midiManager.getDevices())
            if(device.getInputPortCount() >= 1 && device.getOutputPortCount() >= 1)
                for(MidiDeviceInfo.PortInfo portInfoOutput : device.getPorts())
                    if(portInfoOutput.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                        MidiDeviceInfo.PortInfo portInfoInput = null;
                        for (MidiDeviceInfo.PortInfo portInput : device.getPorts())
                            if (portInput.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT)
                                if (portInput.getName().equals(portInfoOutput.getName())) {
                                    portInfoInput = portInput; break; }

                        Devices.GridDeviceConfig device_config = Index.launchpads.get(device.getProperties().getString(MidiDeviceInfo.PROPERTY_PRODUCT));
                        if (device_config == null) device_config = MidiKeyboard.configs;
                        this.devices.add(new Devices.MidiDevice(device, device.getProperties().getString(MidiDeviceInfo.PROPERTY_PRODUCT), portInfoInput, portInfoOutput, device_config));
                        if(portInfoInput != null) {
                            break;
                        }
                    }
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) view = LayoutInflater.from(context).inflate(R.layout.midi_device_list_item, null);
        Bundle midi_properties = devices.get(i).deviceInfo.getProperties();
        ((TextView) view.findViewById(R.id.midi_device_list_item_product)).setText(midi_properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT));
        ((TextView) view.findViewById(R.id.midi_device_list_item_manufacturer)).setText(midi_properties.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER));
        ((ImageView) view.findViewById(R.id.midi_device_list_item_thumb)).setImageDrawable(Thumb.getThumbFromProduct(context, midi_properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT)));

        return view;
    }
}
