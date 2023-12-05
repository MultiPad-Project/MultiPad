package com.xayup.midi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.xayup.midi.controllers.Index;
import com.xayup.midi.manager.Thumb;
import com.xayup.midi.types.Devices;

public class Controllers extends BaseAdapter {

    protected Context context;
    protected String[] devices_name;

    public Controllers(Context context){
        this.context = context;
        this.devices_name = new String[Index.launchpads.size()];
        int i = 0;
        for(String name : Index.launchpads.keySet()){
            devices_name[i] = name;
            i++;
        }
    }

    @Override
    public int getCount() {
        return devices_name.length;
    }

    @Override
    public Devices.GridDeviceConfig getItem(int i) {
        return Index.launchpads.get(devices_name[i]);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) view = LayoutInflater.from(context).inflate(R.layout.midi_device_list_item, null);
        ((TextView) view.findViewById(R.id.midi_device_list_item_product)).setText(Index.launchpads.get(devices_name[i]).name);
        ((TextView) view.findViewById(R.id.midi_device_list_item_manufacturer)).setVisibility(View.GONE);
        ((ImageView) view.findViewById(R.id.midi_device_list_item_thumb)).setImageDrawable(Thumb.getThumbFromProduct(context, Index.launchpads.get(devices_name[i]).name));
        return view;
    }
}
