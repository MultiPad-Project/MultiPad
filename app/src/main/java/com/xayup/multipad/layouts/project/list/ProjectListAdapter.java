package com.xayup.multipad.layouts.project.list;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.xayup.multipad.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProjectListAdapter extends BaseAdapter {
    public static final byte TITLE = 0;
    public static final byte PRODUCER_NAME = 1;
    public static final byte PATH = 2;
    public static final byte STATE = 3;

    protected List<Map<Byte, Object>> projects;
    protected Context context;

    public ProjectListAdapter(Context context, List<Map<Byte, Object>> map) {
        this.context = context;
        this.projects = map;
    }

    public String getPath(int index) {
        return (String) Objects.requireNonNull(getItem(index).get(PATH));
    }

    public String getTitle(int index) {
        return (String) Objects.requireNonNull(getItem(index).get(TITLE));
    }

    public String getProducerName(int index) {
        return (String) Objects.requireNonNull(getItem(index).get(PRODUCER_NAME));
    }

    public boolean isEmpty() {
        return projects == null;
    }

    public boolean isBad(int index) {
        return (boolean) Objects.requireNonNull(getItem(index).get(STATE));
    }

    public Map<Byte, Object> getProperties(int index) {
        return projects.get(index);
    }

    public boolean isExternalStorageManager() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager() || context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int getCount() {
        // TODO: Implement this method
        return projects.size();
    }

    @Override
    public Map<Byte, Object> getItem(int p1) {
        // TODO: Implement this method
        return projects.get(p1);
    }

    @Override
    public long getItemId(int p1) {
        // TODO: Implement this method
        return 0;
    }

    @Override
    public View getView(int p1, View p2, ViewGroup p3) {
        p2 = LayoutInflater.from(context).inflate(R.layout.project_item, p3, false);

        TextView producerName = p2.findViewById(R.id.project_item_author);
        TextView title = p2.findViewById(R.id.project_item_title);

        if (isEmpty()) {
            if (isExternalStorageManager()) {
                title.setText(context.getString(R.string.get_storage));
                producerName.setText(context.getString(R.string.get_storage_subtitle));
                p2.setTag(2);
            } else {
                title.setText(context.getString(R.string.without_projects));
                producerName.setText(context.getString(R.string.without_project_subtitle));
                p2.setAlpha(0.5f);
            }
        } else {
            Map<Byte, Object> properties = getProperties(p1);
            p2.setTag(1); //BAD = 1, NOT BAD = 0, STORAGE_REQUEST = 2
            String t = Objects.requireNonNull(properties.get(TITLE)).toString();
            String p = Objects.requireNonNull(properties.get(PRODUCER_NAME)).toString();
            if (Objects.equals(properties.get(STATE), 0)) {
                title.setText(t);
                producerName.setText(p);
                p2.setTag(0);
            } else {
                title.setText(t);
                producerName.setText(p);
            }
        }
        return p2;
    }
}
