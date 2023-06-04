package com.xayup.multipad.project.keysounds;

import android.app.Activity;
import android.content.Context;
import com.xayup.multipad.load.Project;
import java.io.File;
import java.util.List;

public class KeySounds implements Project.SoundInterface {
    SoundLoader mSoundLoader;
    
    public KeySounds(Context context){
        mSoundLoader = new SoundLoader(context);
        
    }
    
    public List<String[]> parse(File keysound_path, File sample_path){
        List<String[]> problems = new KeySoundsReader().read(keysound_path, sample_path, mSoundLoader);
        mSoundLoader.prepare();
        return  problems;
    }
    
    @Override
    public boolean playSound(Activity context, int chain, int pad) {
        mSoundLoader.playSound(chain+""+pad);
        return false;
    }

    @Override
    public boolean stopSound(Activity context, int chain, int p) {
        mSoundLoader.stopSounds();
        return false;
    }
}
