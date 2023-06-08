package com.xayup.multipad.load.thread;

import android.app.Activity;
import android.content.Context;
import com.xayup.debug.XLog;
import com.xayup.multipad.load.Project;
import com.xayup.multipad.project.autoplay.AutoPlay;
import com.xayup.multipad.project.keyled.KeyLED;
import com.xayup.multipad.project.keysounds.KeySounds;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LoadProject implements Runnable {

    protected Activity context;
    protected Project mProject;
    protected LoadingProject mLoadingProject;
    /* For Read Leds */
    protected List<File> ledT1;
    protected List<File> ledT2;
    protected byte total_threads = 2;
    protected byte ended_threads = 0;

    public interface LoadingProject {
        public void onStartLoadProject();

        public void onStartReadFile(String file_name);

        public void onFileError(String file_name, int line, String cause);

        public void onFinishLoadProject();
    }

    public LoadProject(Context context, LoadingProject mLoadingProject, Project mProject) {
        this.context = (Activity) context;
        this.mProject = mProject;
        this.mLoadingProject = mLoadingProject;
        new Thread(getRunnale()).start();
    }

    protected Runnable getRunnale() {
        return this;
    }

    @Override
    // public void run() {}

    public void run() {
        mLoadingProject.onStartLoadProject();
        XLog.e("Try read", "Readings...");
        XLog.v(
                "Projects Files Load: ",
                mProject.sample_path
                        + ", "
                        + mProject.keyleds_paths
                        + ", "
                        + mProject.autoplay_path
                        + ", "
                        + mProject.keysound_path);
        if (mProject.keysound_path != null && mProject.sample_path != null) {
            XLog.e("Try read keysound/samples", "Readings...");
            mProject.mKeySounds = new KeySounds(context);
            mProject.mKeySounds.parse(
                    mProject.keysound_path, mProject.sample_path, mLoadingProject);
        }
        if (mProject.autoplay_path != null) {
            XLog.e("Try read autoplay", "Readings...");
            mProject.mAutoPlay = new AutoPlay();
            mProject.mAutoPlay.parse(mProject.autoplay_path, mLoadingProject);
        }
        if (mProject.keyleds_paths != null) {
            XLog.e("Try read keyleds", "Readings...");
            mProject.mKeyLED = new KeyLED();
            ledT1 = new ArrayList<>();
            ledT2 = new ArrayList<>();
            int ident = 0;
            for (File path : mProject.keyleds_paths) { // Order
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
                                ended_threads++;
                            })
                    .start();
            ledRead(ledT2);
            ended_threads++;
            while (ended_threads < total_threads){;}
            ledT1 = null;
            ledT2 = null;
        }
        mLoadingProject.onFinishLoadProject();
    }

    protected void ledRead(List<File> led_files) {
        for (File led : led_files) {
            mProject.mKeyLED.parse(led, mLoadingProject);
        }
    }
}
