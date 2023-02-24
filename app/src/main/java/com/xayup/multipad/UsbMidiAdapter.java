package com.xayup.multipad;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Map;

public class UsbMidiAdapter extends BaseAdapter {
	Context context;
	boolean MIDIs;
	UsbDeviceActivity usbDeviceActivity;
	
	public UsbMidiAdapter(Context context, boolean MIDIs){
		this.MIDIs = MIDIs;
		this.context = context;
        MidiStaticVars.manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		usbDeviceActivity = new UsbDeviceActivity();
		usbDeviceActivity.getMidiListDevices(context); //else usbDeviceActivity.getUsbListDevices(context);
	}
	
	@Override
	public int getCount() {
	    return MidiStaticVars.midiDeviceInfo.length;
	//	else return UsbDeviceActivity.usbDeviceList.size();
	}

	@Override
	public Object getItem(int position) {
	    return MidiStaticVars.midiDeviceInfo[position];
	//	else return (UsbDevice) UsbDeviceActivity.usbDeviceList.values().toArray()[position];
	}

	@Override
	public long getItemId(int position) {
	    return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.skinstheme_layout, null);
		
		ImageView imagem = convertView.findViewById(R.id.skinthemelogo);
		TextView name = convertView.findViewById(R.id.skinthemeName);
		TextView manufaturer = convertView.findViewById(R.id.skinthemeVersion);
		Bundle midi = MidiStaticVars.midiDeviceInfo[position].getProperties();
		
		imagem.setImageDrawable(usbDeviceActivity.getMidiImagem(context, midi.get(MidiDeviceInfo.PROPERTY_PRODUCT).toString()));
		name.setText(midi.get(MidiDeviceInfo.PROPERTY_PRODUCT).toString());
		manufaturer.setText(midi.get(MidiDeviceInfo.PROPERTY_MANUFACTURER).toString());
	    
		return convertView;
	}
	
}