package com.xayup.multipad.load;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Projects extends Project {
    
    public boolean[] flags;
    
    public final byte TYPE_KEYLED_FOLDERS = 0;
    public final byte TYPE_SAMPLE_FOLDER = 1;
    public final byte TYPE_AUTOPLAY_FILE = 2;

    public final byte FLAG_SAMPLE_COUNT = 3;
    public final byte FLAG_KEYLED_COUNT = 4;
    public final byte FLAG_AUTOPLAY_DIFICULTY = 5;
    public final byte FLAG_TITLE = 6;
    public final byte FLAG_PRODUCER_NAME = 7;
    public final byte FLAG_COVER = 8;
    public final byte FLAG_ITEM_SAMPLE_COUNT = 9;
    public final byte FLAG_ITEM_KEYLED_COUNT = 10;
    public final byte FLAG_ITEM_AUTOPLAY_DIFICULTY = 11;
    public final byte FLAG_ITEM_TITLE = 12;
    public final byte FLAG_ITEM_PRODUCER_NAME = 13;
    public final byte FLAG_ITEM_COVER = 14;
    public final byte FLAG_ITEM_STATE_VIEW = 15;
    public final byte FLAG_STATE_VIEW = 16;
    public final byte FLAG_ITEM_STATE_TEXT = 17;
    public final byte FLAG_STATE_TEXT = 18;
    
    public final byte FLAG_SIZE = 19; /* Array flag size */

    public Map<Integer, Object> projects = null;

    public void onProjectClicked(Map<Byte, Object> project_properties) {
        String[] tmp =
                new String[] {
                    (String) this.project_properties.get(PROJECT_PATH), // 0
                    (String) this.project_properties.get(PROJECT_SAMPLES_PATH), // 1
                    (String) this.project_properties.get(PROJECT_KEYSOUND_PATH), // 2
                    (String) this.project_properties.get(PROJECT_KEYLEDS_PATHS) // 3
                };
        this.project_properties = project_properties;
        this.title = (String) this.project_properties.get(PROJECT_TITLE);
        this.producerName = (String) this.project_properties.get(PROJECT_PRODUCER_NAME);
        this.state = (String) this.project_properties.get(PROJECT_STATE);
        this.path = (!tmp[0].equals("")) ? tmp[0] : "";
        this.sample_path = (!tmp[1].equals("")) ? new File(tmp[1]) : null;
        this.keysound_path = (!tmp[2].equals("")) ? new File(tmp[2]) : null;
        if (!tmp[3].equals("")) {
            keyleds_paths = new ArrayList<>();
            for (String leds_path : tmp[3].split(";")) {
                keyleds_paths.add(new File(leds_path));
            }
        }
    }

    /*
     * Change vars from Project class
     */

    public void readProjectsPath(File path) {
        if(path == null) return;
        File[] folders = path.listFiles();
        if (folders.length > 0) {
            projects = new HashMap<>();
            for (File folder : folders) {
                File info = new File(folder, "info");
                Log.v("Project folder name", folder.getName());
                if (info.exists()) {
                    try {
                        FileReader reader = new FileReader(info);
                        BufferedReader buffer = new BufferedReader(reader);
                        if (buffer.ready()) {
                            path = folder;
                            String line = "";
                            while ((line = buffer.readLine()) != null) {
                                if (line.toLowerCase().contains("title=")) {
                                    title = line.substring(line.indexOf("=") + 1);
                                } else if (line.toLowerCase().contains("producername=")) {
                                    producerName = line.substring(line.indexOf("=") + 1);
                                }
                            }
                            buffer.close();
                            reader.close();
                            
                            project_properties = new HashMap<>();
                            project_properties.put(PROJECT_TITLE, title);
                            project_properties.put(PROJECT_PRODUCER_NAME, producerName);
                            project_properties.put(PROJECT_PATH, folder.getPath());
                            projects.put(projects.size(), project_properties);
                            
                        } else {
                            new Throwable("ready() return false.");
                        }
                    } catch (IOException io) {
                        Log.v("Project info reader", io.toString());
                    }
                }
            }
            if (!(projects.size() > 0)) projects = null;
        } else {
            projects = null;
        }
        project_properties = null;
    }

    public void readInProject(boolean[] flags) {        
        /* KeyLED op */
        if (flags[TYPE_KEYLED_FOLDERS] || flags[FLAG_KEYLED_COUNT]) {
            keyleds_paths = new ArrayList<>();
            for (File file : new File(path).listFiles()) {
                if (file.isDirectory() && file.getName().toLowerCase().indexOf("keyled") == 0) {
                    keyleds_paths.add(file);
                    if (flags[FLAG_KEYLED_COUNT]) {
                        keyled_count += file.list().length;
                        project_properties.put(PROJECT_KEYLEDS_COUNT, keyled_count);
                    }
                }
            }
            if (!flags[TYPE_KEYLED_FOLDERS]) {
                keyleds_paths = null;
            }
        }
        
        /* Autoplay op */
        if (flags[TYPE_SAMPLE_FOLDER] || flags[FLAG_SAMPLE_COUNT]) {
            sample_path = new File(path, "sounds");
            if (sample_path.exists()) {
                if (flags[FLAG_SAMPLE_COUNT]) {
                    sample_count += sample_path.list().length;
                }

                if (!flags[TYPE_SAMPLE_FOLDER]) {
                    sample_path = null;
                }
            }
        }
        
        /* Dificulty op */
        if (flags[TYPE_AUTOPLAY_FILE] || flags[FLAG_AUTOPLAY_DIFICULTY]) {
            autoplay_path = new File(path, "autoplay");
            if (autoplay_path.exists()) {
                if (flags[FLAG_AUTOPLAY_DIFICULTY]) {
                    dificulty = "--/10";
                }

                if (!flags[TYPE_AUTOPLAY_FILE]) {
                    autoplay_path = null;
                }
            }
        }
    }
}
