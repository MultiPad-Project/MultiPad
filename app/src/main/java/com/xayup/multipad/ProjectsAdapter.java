package com.xayup.multipad;

import android.content.*;
import android.view.*;
import android.widget.*;

import java.util.*;

public class ProjectsAdapter extends BaseAdapter {
    Map<String, Map> projects;
    Context context;

    public ProjectsAdapter(Context contexto, Map<String, Map> map) {
        this.context = contexto;
        this.projects = map;
    }

    public String getPath(int index) {
        return (String) Objects.requireNonNull(projects.get(getItem(index))).get("local");
    }

    public String getTitle(int index) {
        return (String) Objects.requireNonNull(projects.get(getItem(index))).get("title");
    }

    public String getProducerName(int index) {
        return (String) Objects.requireNonNull(projects.get(getItem(index))).get("producerName");
    }

    public boolean isEmpty(){
        return getCount() == 1 && getItem(0).equals("Empty");
    }

    public boolean isBad(int index) throws NullPointerException {
        return Objects.equals(Objects.requireNonNull(projects.get(getItem(index))).get("bad"), "true");
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
        p2 = LayoutInflater.from(context).inflate(R.layout.custom_list_projects, p3, false);

        TextView producerName = p2.findViewById(R.id.projectAutor);
        TextView title = p2.findViewById(R.id.projectTitle);
        View currentState = p2.findViewById(R.id.currentItemState);

        if (getItem(p1).equals("pr")) {
            title.setText(context.getString(R.string.get_storage));
            producerName.setText(context.getString(R.string.get_storage_subtitle));
            currentState.setTag(2);
        } else {
            TextView path = p2.findViewById(R.id.pathText);
            RelativeLayout itemList = p2.findViewById(R.id.itemInfoList);
            currentState.setTag(1); //BAD = 1, NOT BAD = 0, STORAGE_REQUEST = 2

            if (getItem(p1).equals("Empty")) {
                title.setText(context.getString(R.string.without_projects));
                producerName.setText(context.getString(R.string.without_project_subtitle));
                itemList.setAlpha(0.5f);
                currentState.setAlpha(0);
            } else {
                Map project;
                if ((project = projects.get(getItem(p1))) != null) {
                    String t = project.get("title").toString();
                    String p = project.get("producerName").toString();

                    if (!Objects.equals(project.get("bad"), "true")) {
                        title.setText(t);
                        producerName.setText(p);
                        currentState.setTag(0);
                    } else {
                        title.setText(t);
                        producerName.setText(p);
                        currentState.setBackground(context.getDrawable(R.drawable.project_file_state_bad));
                    }
                }
            }
        }
        return p2;
    }
}
