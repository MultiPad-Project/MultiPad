package com.xayup.multipad.projects.thread;

import android.app.Activity;
import android.content.Context;
import com.xayup.debug.XLog;
import com.xayup.multipad.projects.Project;
import com.xayup.multipad.projects.ProjectManager;
import com.xayup.multipad.projects.project.autoplay.AutoPlay;
import com.xayup.multipad.projects.project.keyled.KeyLED;
import com.xayup.multipad.projects.project.keysounds.KeySounds;
import java.io.File;
import java.util.*;

public abstract class LoadProject implements Runnable {

    protected Activity context;
    protected ProjectManager projectManager;
    protected LoadingProject mLoadingProject;
    /* For Read Leds */
    protected List<File[]> ledT1;
    protected List<File[]> ledT2;
    protected byte total_threads = 2;
    protected byte ended_threads = 0;

    public abstract void onFinish();

    public interface LoadingProject {
        public void onStartLoadProject();

        public void onStartReadFile(String file_name);

        public void onFileError(String file_name, int line, String cause);

        public void onFinishLoadProject();
    }

    public LoadProject(Context context, LoadingProject mLoadingProject, ProjectManager projectManager) {
        this.context = (Activity) context;
        this.projectManager = projectManager;
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
        if (projectManager.getProject().keysound_path != null && projectManager.getProject().sample_path != null) {
            XLog.e("Try read keysound/samples", "Readings...");
            projectManager.keySoundsInstance(context);
            projectManager.getKeySounds().parse(
                    projectManager.getProject().keysound_path, projectManager.getProject().sample_path, mLoadingProject);
        }
        if (projectManager.getProject().autoplay_path != null) {
            XLog.e("Try read autoplay", "Readings...");
            projectManager.autoPlayInstance(context);
            projectManager.getAutoPlay().parse(projectManager.getProject().autoplay_path, mLoadingProject);
        }
        if (projectManager.getProject().keyleds_paths != null) {
            XLog.e("Try read keyleds", "Readings...");
            List<List<File>> keyled_folders = new ArrayList<>(); //Cada lista será uma pasta e em cada "Pasta" terá os arquivos, já ordenados
            List<File[]> led_file_name_equals = new ArrayList<>();

            for(File paths : projectManager.getProject().keyleds_paths){
                List<File> led_files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(paths.listFiles())));
                keyled_folders.add(led_files);
            }
            for(int fd = 0; fd < keyled_folders.size(); fd++){
                List<File> keyled_folder = keyled_folders.get(fd);
                while(!keyled_folder.isEmpty()){
                    File[] leds = new File[keyled_folders.size()];
                    leds[fd] = keyled_folder.remove(0);
                    for(int ofd = fd+1; ofd < keyled_folders.size(); ofd++){
                        List<File> other_folder = keyled_folders.get(ofd);
                        for(int fi = 0; fi < other_folder.size(); fi++){
                            if(leds[fd].getName().equals(other_folder.get(fi).getName())){
                                leds[ofd] = other_folder.remove(fi);
                                break;
                            }
                        }
                    }
                    led_file_name_equals.add(leds);
                }
            }
            keyled_folders.clear();
            Collections.sort(led_file_name_equals, (File[] af1, File[] af2) -> {return af1[0].getName().compareTo(af2[0].getName());});
            projectManager.keyLEDInstance(context);
            ledT1 = new ArrayList<>();
            ledT2 = new ArrayList<>();
            total_threads = 2;
            ended_threads = 0;
            int list_size = led_file_name_equals.size() / total_threads;
            while (!led_file_name_equals.isEmpty()){
                if(list_size < ledT1.size()){
                    ledT1.add(led_file_name_equals.remove(0));
                } else {
                    ledT2.add(led_file_name_equals.remove(0));
                }
            }
            new Thread(
                            () -> {
                                ledRead(ledT1);
                                ended_threads++;
                            })
                    .start();
            ledRead(ledT2);
            ended_threads++;
            while (ended_threads < total_threads) {
                ;
            }
            ledT1.clear();
            ledT1 = null;
            ledT2.clear();
            ledT2 = null;
        }
        onFinish();
        mLoadingProject.onFinishLoadProject();
    }

    protected void ledRead(List<File[]> led_files) {
        for (File[] led : led_files) {
            projectManager.getKeyLED().parse(led, mLoadingProject);
        }
    }
}
