package com.xayup.multipad.project.keysounds;

import android.app.Activity;
import android.content.Context;
import com.xayup.debug.XLog;
import com.xayup.multipad.load.Project;
import com.xayup.multipad.load.thread.LoadProject;
import com.xayup.multipad.pads.PadPressCallInterface;

import java.io.File;
import java.util.List;

public class KeySounds implements Project.SoundInterface, PadPressCallInterface {
    SoundLoader mSoundLoader;
    Activity context;
    
    public KeySounds(Context context){
        this.context = (Activity) context;
        mSoundLoader = new SoundLoader(context);
        
    }
    
    public void parse(File keysound_path, File sample_path, LoadProject.LoadingProject mLoadingProject){
        new KeySoundsReader().read(keysound_path, sample_path, mSoundLoader, mLoadingProject);
        mSoundLoader.prepare();
    }
    
    @Override
    public boolean playSound(int chain, int x, int y) {
        XLog.v("Try play sound", "");
        mSoundLoader.playSound(chain+""+((x*10)+y));
        return false;
    }

    @Override
    public boolean stopSound(int chain, int x, int y) {
        mSoundLoader.stopSounds();
        return false;
    }

    @Override
    public void call(int chain, int x, int y) {
        playSound(chain, x, y);
    }
}
