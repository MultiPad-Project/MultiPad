package com.xayup.multipad;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.view.*;
import android.widget.*;

import java.util.*;

public class ProjectsAdapter extends BaseAdapter {
    public static final int TITLE = 0;
    public static final int PRODUCER_NAME = 1;
    public static final int PATH = 2;
    public static final int STATE = 3;

    protected Map<Object, Map> projects;
    protected Context context;

    public ProjectsAdapter(Context contexto, Map<Object, Map> map) {
        this.context = contexto;
        this.projects = map;
    }

    public String getPath(int index) {
        return (String) Objects.requireNonNull(projects.get(getItem(index))).get(PATH);
    }

    public String getTitle(int index) {
        return (String) Objects.requireNonNull(projects.get(getItem(index))).get(TITLE);
    }

    public String getProducerName(int index) {
        return (String) Objects.requireNonNull(projects.get(getItem(index))).get(PRODUCER_NAME);
    }

    public boolean isEmpty() {
        return projects == null;
    }

    public boolean isBad(int index) {
        return Objects.equals(Objects.requireNonNull(projects.get(getItem(index))).get(STATE), true);
    }

    public Map getProperties(int index) {
        return projects.get(getItem(index));
    }

    public boolean isExternalStorageManager() {
        return (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager() ||
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        );
    }

    @Override
    public int getCount() {
        // TODO: Implement this method
        return projects.size();
    }

    @Override
    public String getItem(int p1) {
        // TODO: Implement this method
        return (String) projects.keySet().toArray()[p1];
    }

    @Override
    public long getItemId(int p1) {
        // TODO: Implement this method
        return 0;
    }

    @Override
    public View getView(int p1, View p2, ViewGroup p3) {
        p2 = LayoutInflater.from(context).inflate(R.layout.main_item, p3, false);

        TextView producerName = p2.findViewById(R.id.projectAutor);
        TextView title = p2.findViewById(R.id.projectTitle);
        View currentState = p2.findViewById(R.id.currentItemState);

        if (isEmpty()) {
            if (isExternalStorageManager()) {
                title.setText(context.getString(R.string.get_storage));
                producerName.setText(context.getString(R.string.get_storage_subtitle));
                currentState.setTag(2);
            } else {
                title.setText(context.getString(R.string.without_projects));
                producerName.setText(context.getString(R.string.without_project_subtitle));
                p2.setAlpha(0.5f);
                currentState.setAlpha(0);
            }
        } else {
            Map properties = getProperties(p1);
            currentState.setTag(1); //BAD = 1, NOT BAD = 0, STORAGE_REQUEST = 2
            String t = Objects.requireNonNull(properties.get(TITLE)).toString();
            String p = Objects.requireNonNull(properties.get(PRODUCER_NAME)).toString();
            if (Objects.equals(properties.get(STATE), 0)) {
                title.setText(t);
                producerName.setText(p);
                currentState.setTag(0);
            } else {
                title.setText(t);
                producerName.setText(p);
                currentState.setBackground(context.getDrawable(R.drawable.project_file_state_bad));
            }
        }
        return p2;
    }
}
