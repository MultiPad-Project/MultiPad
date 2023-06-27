package com.xayup.multipad.layouts;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.xayup.debug.XLog;
import com.xayup.multipad.MainActivity;
import com.xayup.multipad.R;
import com.xayup.multipad.load.Projects;
import com.xayup.net.ReadCovers;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProjectsBase extends Projects implements ProjectsBaseInterface {
    protected final String default_layout_name = "main";
    protected Activity context;
    protected PlayProject mPlayProject;
    protected File projects_folder;
    protected ViewGroup list, rootGroup;
    protected String item_layout_resource_name, package_name;
    protected OnClickListener item_onClick;
    protected ReadCovers read_covers;
    protected TextView text_title,
            text_producerName,
            text_dificult,
            text_sampleCount,
            text_ledCount,
            text_state;
    protected View view_state;
    protected ImageView image_cover;

    public ProjectsBase(
            Context context,
            File projects_folder,
            ViewGroup view_root,
            String package_name,
            String layout_name,
            PlayProject mPlayProject) {
        this.context = (Activity) context;
        this.mPlayProject = mPlayProject;
        this.projects_folder = projects_folder;
        this.rootGroup = getLayout((this.package_name = package_name), layout_name, view_root, true);
        this.identifyInLayout(
                new ArrayList<>(
                        Arrays.asList(
                                new ViewGroup[] {
                                    rootGroup,
                                    getLayout(
                                            (this.item_layout_resource_name =
                                                    layout_name.concat("_item")))
                                })));
        if (this.list == null) {
            this.identifyInLayout(
                    new ArrayList<>(
                            Arrays.asList(
                                    new ViewGroup[] {
                                        (rootGroup = getLayout((this.package_name = context.getPackageName()), default_layout_name)),
                                        getLayout(
                                                (this.item_layout_resource_name =
                                                        default_layout_name.concat("_item")))
                                    })));
        }
        this.context.getLayoutInflater().inflate(R.layout.splash_screen, view_root, true);
        this.updateList();
    }

    public void identifyInLayout(List<ViewGroup> groups) {
        flags = new boolean[FLAG_SIZE];
        ViewGroup group;
        while (!groups.isEmpty()) {
            group = groups.remove(0);
            setFromTag(group);
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof ViewGroup) groups.add((ViewGroup) child);
                setFromTag(child);
            }
        }
    }
    protected ViewGroup getLayout(String layout_name) {
        return getLayout(package_name, layout_name, null, false);
    }
    
    public ViewGroup getLayout(String package_name, String layout_name) {
        return getLayout(package_name, layout_name, null, false);
    }
    
    protected ViewGroup getLayout(String layout_name, ViewGroup root, boolean attachToRoot) {
        return getLayout(package_name, layout_name, root, attachToRoot);
    }

    public ViewGroup getLayout(String package_name, String layout_name, ViewGroup root, boolean attachToRoot) {
        return (ViewGroup)
                context.getLayoutInflater()
                        .inflate(
                                context.getResources()
                                        .getLayout(
                                                context.getResources()
                                                        .getIdentifier(
                                                                layout_name,
                                                                "layout",
                                                                package_name)),
                                root, attachToRoot);
    }


    protected void setFromTag(View child) {
        Object tag = child.getTag();
        XLog.v("Tag", String.valueOf(tag));
        if (tag != null) {
            switch (Byte.parseByte((String) tag)) {
                    /*Info Space*/
                case INFO_TITLE:
                    {
                        flags[FLAG_TITLE] = true;
                        text_title = (TextView) child;
                        break;
                    }
                case INFO_PRODUCER:
                    {
                        flags[FLAG_PRODUCER_NAME] = true;
                        text_producerName = (TextView) child;
                        break;
                    }
                case INFO_STATE_VIEW:
                    {
                        flags[FLAG_STATE_VIEW] = true;
                        text_producerName = (TextView) child;
                        break;
                    }
                case INFO_STATE_TEXT:
                    {
                        flags[FLAG_STATE_TEXT] = true;
                        text_producerName = (TextView) child;
                        break;
                    }
                case INFO_DIFICULT:
                    {
                        flags[FLAG_AUTOPLAY_DIFICULTY] = true;
                        text_dificult = (TextView) child;
                        break;
                    }
                case INFO_LEDS_COUNT:
                    {
                        flags[FLAG_KEYLED_COUNT] = true;
                        text_ledCount = (TextView) child;
                        break;
                    }
                case INFO_SAMPLES_COUNT:
                    {
                        flags[FLAG_SAMPLE_COUNT] = true;
                        text_sampleCount = (TextView) child;
                        break;
                    }
                case INFO_COVER:
                    {
                        flags[FLAG_COVER] = true;
                        image_cover = (ImageView) child;
                        break;
                    }
                case BUTTON_INFO_PLAY:
                    {
                        child.setOnClickListener(buttonClickPlay());
                        break;
                    }
                case BUTTON_INFO_DELETE:
                    {
                        child.setOnClickListener(buttonClickDelete());
                        break;
                    }
                case BUTTON_INFO_REMAP:
                    {
                        child.setOnClickListener(buttonClickRemap());
                        break;
                    }
                case PROJECTS_LIST:
                    {
                        list = (ViewGroup) child;
                        break;
                    }
                    /* Item infos */
                case ITEM_CLICK_INFO:
                    {
                        item_onClick = itemClick(false);
                        break;
                    }
                case ITEM_CLICK_PLAY:
                    {
                        item_onClick = itemClick(true);
                        break;
                    }
                case ITEM_COVER:
                    {
                        flags[FLAG_ITEM_COVER] = true;
                        if (read_covers == null) {
                            read_covers = new ReadCovers(context);
                        }
                        break;
                    }
                case ITEM_TITLE:
                    {
                        flags[FLAG_ITEM_TITLE] = true;
                        break;
                    }
                case ITEM_PRODUCER:
                    {
                        flags[FLAG_ITEM_PRODUCER_NAME] = true;
                        break;
                    }
                case ITEM_DIFICULT:
                    {
                        flags[FLAG_ITEM_AUTOPLAY_DIFICULTY] = true;
                        break;
                    }
                case ITEM_LEDS_COUNT:
                    {
                        flags[FLAG_ITEM_KEYLED_COUNT] = true;
                        break;
                    }
                case ITEM_SAMPLES_COUNT:
                    {
                        flags[FLAG_ITEM_SAMPLE_COUNT] = true;
                        break;
                    }
            }
        }
    }

    public void updateList() {
        readProjectsPath(projects_folder);
        if (projects == null) return;
        if (list.getChildCount() > 0) {
            list.removeAllViews();
        }
        for (Object properties : projects) {
            list.addView(getViewFromProperties((Map<Byte, Object>) properties));
        }
    }

    public View getViewFromProperties(Map<Byte, Object> propertie) {
        View item_view = getLayout(item_layout_resource_name, list, false);
        item_view.setOnClickListener(item_onClick);
        item_view.setId(list.getChildCount());
        readInProject(flags);
        if (flags[FLAG_ITEM_TITLE]) {
            ((TextView) item_view.findViewWithTag(String.valueOf(ITEM_TITLE)))
                    .setText((String) propertie.get(PROJECT_TITLE));
        }

        if (flags[FLAG_ITEM_PRODUCER_NAME]) {
            ((TextView) item_view.findViewWithTag(String.valueOf(ITEM_PRODUCER)))
                    .setText((String) propertie.get(PROJECT_PRODUCER_NAME));
        }
        if (flags[FLAG_ITEM_STATE_VIEW] || flags[FLAG_ITEM_STATE_TEXT]) {
            int state = (int) propertie.get(PROJECT_STATE);
            if (flags[FLAG_ITEM_STATE_VIEW]) {

            } else if (flags[FLAG_ITEM_STATE_TEXT]) {

            }
        }
        if (flags[FLAG_ITEM_COVER]) {
            read_covers.findAndSetCover(
                    (ImageView) item_view.findViewWithTag(String.valueOf(ITEM_COVER)),
                    (String) propertie.get(PROJECT_PATH),
                    (String) propertie.get(PROJECT_TITLE));
        }

        if (flags[FLAG_ITEM_KEYLED_COUNT]) {
            ((TextView) item_view.findViewWithTag(String.valueOf(ITEM_LEDS_COUNT)))
                    .setText((String) propertie.get(PROJECT_KEYLEDS_COUNT));
        }

        if (flags[FLAG_ITEM_SAMPLE_COUNT]) {
            ((TextView) item_view.findViewWithTag(String.valueOf(ITEM_SAMPLES_COUNT)))
                    .setText((String) propertie.get(PROJECT_SAMPLES_COUNT));
        }

        if (flags[FLAG_ITEM_AUTOPLAY_DIFICULTY]) {
            ((TextView) item_view.findViewWithTag(String.valueOf(ITEM_DIFICULT))).setText("--/10");
        }
        return item_view;
    }

    protected void loadProject() {
        XLog.v("Properties", project_properties+"");
        mPlayProject.onPreLoadProject();
        mPlayProject.loadProject(path);
    }

    protected OnClickListener itemClick(boolean click_and_play) {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                project_properties = (Map<Byte, Object>) projects.get(view.getId());
                path = (String) project_properties.get(PROJECT_PATH);
                if (click_and_play) {
                    if (project_properties != null) loadProject();
                    return;
                }
                title = (String) project_properties.get(PROJECT_TITLE);
                producerName = (String) project_properties.get(PROJECT_PRODUCER_NAME);
                readInProject(flags);
                if (flags[FLAG_TITLE]) {
                    text_title.setText(title);
                    Toast.makeText(context, title, 0).show();
                }
                if (flags[FLAG_PRODUCER_NAME]) {
                    text_producerName.setText(producerName);
                }
                if (flags[FLAG_STATE_VIEW] || flags[FLAG_STATE_TEXT]) {
                    int state = (int) project_properties.get(PROJECT_STATE);
                    if (flags[FLAG_STATE_VIEW]) {

                    } else if (flags[FLAG_STATE_TEXT]) {

                    }
                }
                if (flags[FLAG_AUTOPLAY_DIFICULTY]) {
                    text_producerName.setText(dificulty);
                }
                if (flags[FLAG_KEYLED_COUNT]) {
                    text_producerName.setText("" + keyled_count);
                }
                if (flags[FLAG_SAMPLE_COUNT]) {
                    text_producerName.setText("" + sample_count);
                }
                if (flags[FLAG_COVER]) {
                    read_covers.findAndSetCover(image_cover, title, path);
                }
            }
        };
    }

    protected OnClickListener buttonClickPlay() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (project_properties != null) loadProject();
            }
        };
    }

    protected OnClickListener buttonClickDelete() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "delete", 0).show();
            }
        };
    }

    protected OnClickListener buttonClickRemap() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "remap", 0).show();
            }
        };
    }
}
