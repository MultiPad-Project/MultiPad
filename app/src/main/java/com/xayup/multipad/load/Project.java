package com.xayup.multipad.load;

import android.app.Activity;
import android.content.Context;
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
    
    public PlayProject mPlayProject;
    
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

    public Map<Byte, Object> project_properties;
    
    /* Project Data */
    public KeySounds mKeySounds = null;
    public KeyLED mKeyLED = null;
    public AutoPlay mAutoPlay = null;
    
    public List<String[]> project_loaded_problems;
    
    public void loadProject(Context context){
        project_loaded_problems = new ArrayList<>();
        new LoadProject(context);
    }
    
    public interface AutoPlayInterface {
        public boolean startAutoPlay();
        public boolean pauseAutoPlay();
        public boolean resumeAutoPlay();
        public float advanceAutoPlay();
        public float regressAutoPlay();
    }
    public interface KeyLEDInterface {
        public boolean showLed(Activity context, int chain, int pad);
        public boolean breakLed(Activity context, int chain, int pad);
        public boolean breakAll(Activity context);
    }
    public interface SoundInterface {
        public boolean playSound(Activity context, int chain, int pad);
        public boolean stopSound(Activity context, int chain, int pad);
    }
}
