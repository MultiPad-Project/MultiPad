package com.xayup.midi.manager;

import android.content.Context;
import android.hardware.usb.*;
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
    private UsbManager midiManager;

    //private List<Devices.MidiDevice> devices;
    private List<Devices.MidiDevice> devices;

    public MidiDevicesAdapter(Context context, UsbManager midiManager){
        this.context = context;
        this.midiManager = midiManager;
        this.devices = new ArrayList<>();
    }

    public void updateList(){
        devices.clear();
        if(midiManager == null) return;
        Log.v("USB List", "Get list");
        for(UsbDevice device : midiManager.getDeviceList().values()) {
            Log.v("UsbDevice", device.getProductName());
            for (int i = 0; i < device.getInterfaceCount(); i++) {
                UsbInterface usbInterface = device.getInterface(i);
                Log.v("UsbInterface", usbInterface.getName() + ": Class: " + usbInterface.getInterfaceClass());
                if (usbInterface.getEndpointCount() > 0 && usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_AUDIO) {
                    Log.v("USbInterface",  "is type Audio");
                    Log.v("UsbEndpoint count", String.valueOf(usbInterface.getEndpointCount()));
                    UsbEndpoint output = null;
//                    for (i = 0; i < usbInterface.getEndpointCount(); i++) {
                        UsbEndpoint input = usbInterface.getEndpoint(i);
                       /* if (input.getDirection() == UsbConstants.USB_DIR_IN) {
                            Log.v("UsbEndpoint", "Input");
                            for (int ei = 0; ei < usbInterface.getEndpointCount(); ei++) {
                                if (i != ei) {
                                    output = usbInterface.getEndpoint(ei);
                                    if (output.getDirection() != UsbConstants.USB_DIR_OUT) {
                                        output = null;
                                        continue;
                                    }
                                    Log.v("UsbEndpoint", "Output");
                                }
                            }*/
                            Devices.GridDeviceConfig device_config = Index.launchpads.get(device.getProductName());
                            if (device_config == null) device_config = MidiKeyboard.configs;
                            this.devices.add(new Devices.MidiDevice(device, device.getProductName(), usbInterface, input, output, device_config));
                            /*if (output != null) {
                                break;
                            }
                        }
                    }*/
                    break;
                }
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
        UsbDevice midi_properties = devices.get(i).usbDevice;
        ((TextView) view.findViewById(R.id.midi_device_list_item_product)).setText(midi_properties.getProductName());
        ((TextView) view.findViewById(R.id.midi_device_list_item_manufacturer)).setText(midi_properties.getManufacturerName());
        ((ImageView) view.findViewById(R.id.midi_device_list_item_thumb)).setImageDrawable(Thumb.getThumbFromProduct(context, midi_properties.getProductName()));

        return view;
    }
}
