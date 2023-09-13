package com.xayup.multipad;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;

public class ColorTableAdapter extends BaseAdapter {
    private final Context context;
    private final File[] tables;
    public ColorTableAdapter(Context context, File[] tables){
        this.context = context;
        this.tables = tables; }

    @Override
    public int getCount() { return tables.length; }

    @Override
    public File getItem(int i) { return tables[i]; }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.simple_list_item_1, null);
            ((TextView) view.findViewById(android.R.id.text1)).setText(getItem(i).getName());
        }
        return  view;
    }
}
