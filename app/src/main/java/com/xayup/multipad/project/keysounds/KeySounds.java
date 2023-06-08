package com.xayup.multipad.project.keysounds;

import android.app.Activity;
import android.content.Context;
import com.xayup.multipad.load.Project;
import com.xayup.multipad.load.thread.LoadProject;
import java.io.File;
import java.util.List;

public class KeySounds implements Project.SoundInterface {
    SoundLoader mSoundLoader;
    
    public KeySounds(Context context){
        mSoundLoader = new SoundLoader(context);
        
    }
    
    public void parse(File keysound_path, File sample_path, LoadProject.LoadingProject mLoadingProject){
        new KeySoundsReader().read(keysound_path, sample_path, mSoundLoader, mLoadingProject);
        mSoundLoader.prepare();
    }
    
    @Override
    public boolean playSound(Activity context, int chain, int x, int y) {
        mSoundLoader.playSound(chain+""+((x*10)+y));
        return false;
    }

    @Override
    public boolean stopSound(Activity context, int chain, int x, int y) {
        mSoundLoader.stopSounds();
        return false;
    }
}
