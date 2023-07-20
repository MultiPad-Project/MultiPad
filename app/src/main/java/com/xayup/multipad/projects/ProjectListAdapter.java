package com.xayup.multipad.projects;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.xayup.multipad.R;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProjectListAdapter extends BaseAdapter {

    protected List<Project> projects;
    protected Context context;

    public ProjectListAdapter(Context context, List<Project> projects) {
        this.context = context;
        this.projects = projects;
    }

    public boolean isEmpty() {
        return projects == null;
    }

    public boolean isExternalStorageManager() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager() || context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int getCount() { return projects.size(); }

    @Override
    public Project getItem(int p1) { return projects.get(p1); }

    @Override
    public long getItemId(int p1) { return p1; }

    @Override
    public View getView(int p1, View p2, ViewGroup p3) {
        if(p2 == null) {
            p2 = LayoutInflater.from(context).inflate(R.layout.project_item, p3, false);

            TextView producerName = p2.findViewById(R.id.project_item_author);
            TextView title = p2.findViewById(R.id.project_item_title);
            ProgressBar progress = p2.findViewById(R.id.project_item_progress);
            progress.setProgress(0);
        // Description
            TextView description_date = p2.findViewById(R.id.project_item_description_date);
            TextView description_size = p2.findViewById(R.id.project_item_description_size);
            TextView description_difficult = p2.findViewById(R.id.project_item_description_difficulty);
            TextView description_project_status = p2.findViewById(R.id.project_item_description_project_status);
            TextView description_led_count = p2.findViewById(R.id.project_item_description_led_count);
            TextView description_sound_count = p2.findViewById(R.id.project_item_description_sound_count);
            TextView description_autoplay_exists = p2.findViewById(R.id.project_item_description_autoplay_exists);
        // Status
            View project_status = p2.findViewById(R.id.project_item_status_project_view);
            View led_status = p2.findViewById(R.id.project_item_status_led_view);
            View sound_status = p2.findViewById(R.id.project_item_status_sound_view);
            View autoplay_status = p2.findViewById(R.id.project_item_status_autoplay_view);

            Project project = getItem(p1);
            if(project != null) {
                progress.setProgress((project.getStatus() == Project.STATUS_LOADED ? 100 : 0));
                title.setText(project.getTitle());
                producerName.setText(project.getProducerName());
            // Set status view and Description
                description_project_status.setText(
                        context.getString(R.string.project_item_text_description_project).concat(" " + applyStatusColor(
                        project_status,
                        (project.getKeySoundPath() == null && project.getKeyLedPath(0) == null) ?
                                ProjectIndexes.PROJECT_STATE_USELESS :
                                (project.getKeySoundPath() == null || project.getKeyLedPath(0) == null) ?
                                        ProjectIndexes.PROJECT_STATE_BAD : ProjectIndexes.PROJECT_STATE_GOOD
                        )));
                boolean autoplay = project.getAutoplayPath() != null;
                applyStatusColor(autoplay_status, autoplay ?
                        ProjectIndexes.PROJECT_STATE_GOOD : ProjectIndexes.PROJECT_STATE_USELESS);
                applyStatusColor(led_status, project.getKeyLedPath(0) != null ?
                        ProjectIndexes.PROJECT_STATE_GOOD : ProjectIndexes.PROJECT_STATE_USELESS);
                applyStatusColor(sound_status, project.getKeySoundPath() != null ?
                        ProjectIndexes.PROJECT_STATE_GOOD : ProjectIndexes.PROJECT_STATE_USELESS);

                description_date.setText(context.getString(R.string.project_item_text_description_date).concat(" " + "--/--/----"));
                description_size.setText(context.getString(R.string.project_item_text_description_size).concat(" " + "0mb"));
                description_autoplay_exists.setText(
                        context.getString(R.string.project_item_text_description_autoplay).concat(" " + (autoplay ?
                                context.getString(R.string.yes) : context.getString(R.string.no))));
                description_difficult.setText(context.getString(R.string.project_item_text_description_difficult).concat(" " + "--/10"));
                description_led_count.setText(context.getString(R.string.project_item_text_description_led).concat(" " + "00"));
                description_sound_count.setText(context.getString(R.string.project_item_text_description_sound).concat(" " + "--/--/----"));
            }
        }
        return p2;
    }

    protected String applyStatusColor(View view, byte status){
        switch (status){
            case ProjectIndexes.PROJECT_STATE_GOOD: {
                view.setBackgroundColor(context.getColor(R.color.project_status_good));
                return context.getString(R.string.project_status_good);
            }
            case ProjectIndexes.PROJECT_STATE_BAD: {
                view.setBackgroundColor(context.getColor(R.color.project_status_bad));
                return context.getString(R.string.project_status_bad);}
            default: {
                view.setBackgroundColor(context.getColor(R.color.project_status_useless));
                return context.getString(R.string.project_status_useless);
            }
        }
    }
}
