package com.xayup.multipad.load;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Projects extends Project {
    
    public boolean[] flags;
    /*
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

    public List<Object> projects = null;

    /*
     * Change vars from Project class
     */

    public void readProjectsPath(File path) {
        if(path == null) return;
        File[] folders = path.listFiles();
        if (folders != null && folders.length > 0) {
            projects = new ArrayList<>();
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
                            projects.add(project_properties);
                            
                        } else {
                            throw new IOException("ready() return false.");
                        }
                    } catch (IOException io) {
                        Log.v("Project info reader", io.toString());
                    }
                }
            }
            if (!(projects.size() > 0)){
                projects = null;
            } else {
                /*Alphabetic order*/
                Collections.sort(projects, (m1, m2) -> ((String) ((HashMap) m1).get(PROJECT_TITLE)).toLowerCase().compareTo(((String) ((HashMap) m2).get(PROJECT_TITLE)).toLowerCase()));
            }
        } else {
            projects = null;
        }
        project_properties = null;
    }

    public void readInProjecttt(boolean[] flags) {        
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
            if (!(keyleds_paths.size() > 0)) keyleds_paths = null;
        }
        
        /* Autoplay op */
        if (flags[TYPE_SAMPLE_FOLDER] || flags[FLAG_SAMPLE_COUNT]) {
            sample_path = new File(path, "sounds");
            keysound_path = new File(path, "keysound");
            if (sample_path.exists()) {
                if (flags[FLAG_SAMPLE_COUNT]) {
                    sample_count += sample_path.list().length;
                }

                if (!flags[TYPE_SAMPLE_FOLDER]) {
                    sample_path = null;
                }
            } else {
                sample_path = null;
            }
            if (!keysound_path.exists()) keysound_path = null;
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
            } else {
                autoplay_path = null;
            }
        }
    }
}
