package com.xayup.multipad.load;

import com.xayup.multipad.ProjectsAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Project {
    public String title, produceName, state = "";
    public File path, sample_path, autoplay_path, keysound_path, info_path = null;
    public List<File> keyleds_paths = null;

    public final int TITLE = 0;
    public final int PRODUCER_NAME = 1;
    public final int PATH = 2;
    public final int STATE = 3;
    public final int SAMPLES_PATH = 4;
    public final int KEYLEDS_PATHS = 5;
    public final int AUTOPLAY_PATH = 6;
    public final int INFO_PATH = 7;
    public final int KEYSOUND_PATH = 8;

    private Map<Integer, String> project_properties;

    public void onProjectClicked(Map<Integer, String> project_properties) {
        String[] tmp =
                new String[] {
                    this.project_properties.get(PATH),            //0
                    this.project_properties.get(SAMPLES_PATH),    //1
                    this.project_properties.get(KEYSOUND_PATH),   //2
                    this.project_properties.get(KEYLEDS_PATHS)    //3
                };
        this.project_properties = project_properties;
        this.title = this.project_properties.get(TITLE);
        this.produceName = this.project_properties.get(PRODUCER_NAME);
        this.state = this.project_properties.get(STATE);
        this.path = (!tmp[0].equals("")) ? new File(tmp[0]) : null;
        this.sample_path = (!tmp[1].equals("")) ? new File(tmp[1]) : null;
        this.keysound_path = (!tmp[2].equals("")) ? new File(tmp[2]) : null;
        if (!tmp[3].equals("")) {
            keyleds_paths = new ArrayList<>();
            for (String leds_path : tmp[3].split(";")) {
                keyleds_paths.add(new File(leds_path));
            }
        }
    }
}
