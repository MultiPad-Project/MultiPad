package com.xayup.multipad.projects;

import android.content.Context;
import com.xayup.multipad.projects.project.keyled.KeyLED;
import com.xayup.multipad.projects.project.autoplay.AutoPlay;
import com.xayup.multipad.projects.thread.LoadProject;
import com.xayup.multipad.projects.project.keysounds.KeySounds;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Project implements ProjectIndexes {

    public String title, producerName, difficulty;
    public File path, info_path, sample_path, autoplay_path, keysound_path;
    public int keyled_count, sample_count = 0;
    public byte state = 0;
    public List<File> keyleds_paths;

    // Project Opened
    public KeySounds mKeySounds;
    public KeyLED mKeyLED;
    public AutoPlay mAutoPlay;

    public List<String> project_loaded_problems;

    public Project(){
        this.keyleds_paths = new ArrayList<>();
        this.project_loaded_problems = new ArrayList<>();
    }

    protected void setTitle(String title){ this.title = title; }
    protected void setProducerName(String producerName){ this.producerName = producerName; }
    protected void setDifficulty(String difficulty){ this.difficulty = difficulty; }
    protected void setPath(String path){ this.path = (path == null) ? null : new File(path); }
    protected void setInfoPath(String info_path){ this.info_path = (info_path == null) ? null : new File(info_path); }
    protected void setSamplePath(String sample_path){ this.sample_path = (sample_path == null) ? null : new File(sample_path); }
    protected void setAutoplayPath(String autoplay_path){ this.autoplay_path = (autoplay_path == null) ? null : new File(autoplay_path); }
    protected void setKeySoundPath(String keysound_path){ this.keysound_path = (keysound_path == null) ? null : new File(keysound_path); }
    protected void addKeyLedPath(File keyled_path){ this.keyleds_paths.add(keyled_path); }
    protected void setKeyLedCount(int keyled_count){ this.keyled_count = keyled_count; }
    protected void setSampleCount(int sample_count){ this.sample_count = sample_count; }
    protected void setState(byte state){ this.state = state; }

    public String getTitle(){ return title; }
    public String getProducerName(){ return producerName; }
    public String getDifficulty(){ return difficulty; }
    public File getPath(){ return path; }
    public File getInfoPath(){ return info_path; }
    public File getAutoplayPath(){ return autoplay_path; }
    public File getKeySoundPath(){ return keysound_path; }
    public File getSamplePath(){ return sample_path; }
    public File getKeyLedPath(int index){return (keyleds_paths.size() == 0 || index >= keyleds_paths.size()) ? null : keyleds_paths.get(index); }
    public int getKeyLedCount(){ return keyled_count; }
    public int getSampleCount(){ return sample_count; }
    public byte getState(){ return state; }

    /// TEST ///
    public boolean loaded = false;
    public boolean loaded(){ return loaded; } ///TESTE///mKeyLED != null || mKeySounds != null; }

    /**
     * Feche este projeto e libere memoria
     */
    public void release(){

    }
    public interface AutoPlayInterface {
        public boolean isRunning();
        public boolean startAutoPlay();
        public boolean stopAutoPlay();

        public boolean pauseAutoPlay();
        public boolean isPaused();

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
}
