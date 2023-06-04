package com.xayup.multipad.load.thread;

import android.app.Activity;
import android.content.Context;
import com.xayup.multipad.load.Project;
import com.xayup.multipad.project.autoplay.AutoPlay;
import com.xayup.multipad.project.keyled.KeyLED;
import com.xayup.multipad.project.keysounds.KeySounds;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LoadProject extends Project implements Runnable {
    
    protected Activity context;
    /* For Read Leds */
    protected List<File> ledT1;
    protected List<File> ledT2;
    byte total_threads = 2;
    byte ended_threads = 0;

    public LoadProject(Context context) {
        this.context = (Activity) context;
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (keysound_path != null && sample_path != null) {
            mKeySounds = new KeySounds(context);
            mKeySounds.parse(keysound_path, sample_path);
        }
        if (autoplay_path != null) {
            mAutoPlay = new AutoPlay();
            mAutoPlay.parse(autoplay_path);
        }
        if (keyleds_paths != null) {
            mKeyLED = new KeyLED();
            int ident = 0;
            for (File path : keyleds_paths) { // Order
                List<File> files = Arrays.asList(path.listFiles());
                Collections.sort(
                        files,
                        new Comparator<File>() {
                            @Override
                            public int compare(File f1, File f2) {
                                return f1.getName().compareTo(f2.getName());
                            }
                        });
                for (File led : files) {
                    if (ident == 0) {
                        ledT1.add(led);
                        ident = 1;
                    } else {
                        ledT2.add(led);
                        ident = 0;
                    }
                }
            }
            total_threads = 2;
            ended_threads = 0;
            new Thread(
                            () -> {
                                ledRead(ledT1);
                                checkEndThread();
                            })
                    .start();
            ledRead(ledT2);
            checkEndThread();
            ;
        }
    }

    protected void checkEndThread() {
        ended_threads++;
        if (!(ended_threads < total_threads)) {
            mPlayProject.onLoadedProject(project_properties);
        }
    }

    protected Runnable ledRead(List<File> led_files) {
        return () -> {
            for (File led : led_files) {
                mKeyLED.parse(led);
            }
        };
    }
}
