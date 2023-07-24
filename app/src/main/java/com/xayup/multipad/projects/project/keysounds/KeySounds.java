package com.xayup.multipad.projects.project.keysounds;

import android.app.Activity;
import android.content.Context;
import com.xayup.debug.XLog;
import com.xayup.multipad.pads.GridPadsReceptor;
import com.xayup.multipad.projects.Project;
import com.xayup.multipad.projects.thread.LoadProject;
import com.xayup.multipad.pads.PadPressCallInterface;
import com.xayup.multipad.pads.Render.MakePads;

import java.io.File;

public class KeySounds implements Project.SoundInterface, PadPressCallInterface {
    protected Activity context;
    protected SoundLoader mSoundLoader;
    
    public KeySounds(Context context){
        this.context = (Activity) context;
        this.mSoundLoader = new SoundLoader(context);
    }
    
    public void parse(File keysound_path, File sample_path, LoadProject.LoadingProject mLoadingProject){
        new KeySoundsReader().read(keysound_path, sample_path, mSoundLoader, mLoadingProject);
    }
    
    public void clear(){
        mSoundLoader.release();
    }
    
    @Override
    public boolean playSound(int chain, int x, int y) {
        XLog.v("Try play sound", String.valueOf(chain)+x+y);
        return mSoundLoader.playSound(String.valueOf(chain)+x+y);
    }

    @Override
    public boolean stopSound(int chain, int x, int y) {
        return false;
    }
    public void stopAll(){
        mSoundLoader.stopAll();
    }
    public void resetSequencer(){
        mSoundLoader.resetSequencer();
    }

    @Override
    public boolean call(GridPadsReceptor.PadGrid padGrid, MakePads.PadInfo padInfo) {
        return playSound(padGrid.getCurrentChain().getMc(), padInfo.getRow(), padInfo.getColum());
    }
}
