package com.xayup.multipad.load;

import android.app.Activity;
import android.content.Context;
import com.xayup.debug.XLog;
import com.xayup.multipad.project.keyled.KeyLED;
import com.xayup.multipad.project.autoplay.AutoPlay;
import com.xayup.multipad.ProjectsAdapter;
import com.xayup.multipad.layouts.PlayProject;
import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.project.keysounds.KeySounds;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Project {

    public String title, producerName, state, dificulty, path = "";
    public File sample_path, autoplay_path, keysound_path, info_path = null;
    public int keyled_count, sample_count = 0;
    public List<File> keyleds_paths = null;

    public final byte PROJECT_TITLE = 0;
    public final byte PROJECT_PRODUCER_NAME = 1;
    public final byte PROJECT_PATH = 2;
    public final byte PROJECT_STATE = 3;
    public final byte PROJECT_SAMPLES_PATH = 4;
    public final byte PROJECT_KEYLEDS_PATHS = 5;
    public final byte PROJECT_AUTOPLAY_PATH = 6;
    public final byte PROJECT_INFO_PATH = 7;
    public final byte PROJECT_KEYSOUND_PATH = 8;
    public final byte PROJECT_KEYLEDS_COUNT = 9;
    public final byte PROJECT_SAMPLES_COUNT = 10;
    
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

    public Map<Byte, Object> project_properties;

    /* Project Data */
    public KeySounds mKeySounds = null;
    public KeyLED mKeyLED = null;
    public AutoPlay mAutoPlay = null;

    public List<String> project_loaded_problems;

    public interface AutoPlayInterface {
        public boolean isRunning();
        public boolean startAutoPlay();
        public boolean stopAutoPlay();

        public boolean pauseAutoPlay();
        public boolean inPaused();

        public boolean resumeAutoPlay();

        public float advanceAutoPlay();

        public float regressAutoPlay();
    }
    
    public void loadProject(Context context, LoadProject.LoadingProject mLoadingProject){
        project_loaded_problems = new ArrayList<>();
        new LoadProject(context, mLoadingProject, this);
    }

    public interface KeyLEDInterface {
        public boolean showLed(int chain, int x, int y);

        public boolean breakLed(int chain, int x, int y);

        public boolean breakAll();
    }

    public interface SoundInterface {
        public boolean playSound(int chain, int x, int y);

        public boolean stopSound(int chain, int x, int y);
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
                                    XLog.v("Keyled folders", "turn null");
            }
            if (!(keyleds_paths.size() > 0)) keyleds_paths = null;
        }
        
        /* Autoplay op */
        if (flags[TYPE_SAMPLE_FOLDER] || flags[FLAG_SAMPLE_COUNT]) {
            sample_path = new File(path, "sounds");
            keysound_path = new File(path, "keysound");
            XLog.v("Sample: ", sample_path.exists()+"");
            XLog.v("keysound: ", keysound_path.exists()+"");
            if (sample_path.exists()) {
                if (flags[FLAG_SAMPLE_COUNT]) {
                    sample_count += sample_path.list().length;
                }

                if (!flags[TYPE_SAMPLE_FOLDER]) {
                    sample_path = null;
                    XLog.v("Sample folder", "turn null");
                }
            } else {
                sample_path = null;
            }
            if (!keysound_path.exists()) keysound_path = null;
        }
        
        /* Dificulty op */
        if (flags[TYPE_AUTOPLAY_FILE] || flags[FLAG_AUTOPLAY_DIFICULTY]) {
            autoplay_path = new File(path, "autoplay");
                        XLog.v("autoplay: ", autoplay_path.exists()+"");
            if (autoplay_path.exists()) {
                if (flags[FLAG_AUTOPLAY_DIFICULTY]) {
                    dificulty = "--/10";
                }

                if (!flags[TYPE_AUTOPLAY_FILE]) {
                    autoplay_path = null;
                                        XLog.v("Autoplay file", "turn null");
                }
            } else {
                autoplay_path = null;
            }
        }
    }
}
