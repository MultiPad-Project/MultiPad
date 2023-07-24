package com.xayup.multipad.projects;

import android.util.Log;
import com.xayup.debug.XLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Projects implements ProjectIndexes {
    protected List<ProjectManager> projects = null;
    protected List<ProjectManager> projects_loaded = null;
    /**
     * Isto fará a leitura dos projetos.
     * @param path pasta onde estão os projetos
     * @param types determina o que será obtido.
     */
    public void readProjectsPath(File path, boolean[] types) {
        if(path == null) return;
        File[] folders = path.listFiles();
        if (folders != null && folders.length > 0) {
            if(projects != null) return;
            else projects = new ArrayList<>();
            for (File folder : folders) {
                Project project = new Project();
                project.setInfoPath(folder + File.separator + "info");
                XLog.v("Project folder name", folder.getName());
                if (project.getInfoPath().exists()) {
                    if(types[FLAG_TITLE] || types[FLAG_PRODUCER_NAME]) {
                        try {
                            FileReader reader = new FileReader(project.getInfoPath());
                            BufferedReader buffer = new BufferedReader(reader);
                            if (buffer.ready()) {
                                String line;
                                while ((line = buffer.readLine()) != null) {
                                    if (types[FLAG_TITLE] && line.toLowerCase().contains("title=")) {
                                        project.setTitle(line.substring(line.indexOf("=") + 1));
                                    } else if (types[FLAG_PRODUCER_NAME] && line.toLowerCase().contains("producername=")) {
                                        project.setProducerName(line.substring(line.indexOf("=") + 1));
                                    }
                                }
                                buffer.close();
                                reader.close();
                            } else {
                                throw new IOException("ready() return false.");
                            }
                        } catch (IOException io) {
                            Log.v("Project info reader", io.toString());
                        }
                    }
                } else {
                    project.setTitle("...");
                    project.setProducerName("...");
                }
                // KeyLED op
                if (types[TYPE_KEYLED_FOLDERS] || types[FLAG_KEYLED_COUNT]) {
                    for (File file : folder.listFiles()) {
                        if (file.isDirectory() && file.getName().toLowerCase().indexOf("keyled") == 0) {
                            project.addKeyLedPath(file);
                            if (types[FLAG_KEYLED_COUNT]) {
                                project.setKeyLedCount(project.getKeyLedCount() + file.list().length);
                            }
                        }
                    }
                }

                // Sound op
                if (types[TYPE_SAMPLE_FOLDER] || types[FLAG_SAMPLE_COUNT]) {
                    project.setSamplePath(folder + File.separator + "sounds");
                    project.setKeySoundPath(folder + File.separator + "keysound");
                    if (project.getSamplePath().exists()) {
                        if (types[FLAG_SAMPLE_COUNT]) {
                            project.setSampleCount(project.getSamplePath().list().length);
                        }
                    } else project.setSamplePath(null);
                    if (!project.getKeySoundPath().exists()) project.setKeySoundPath(null);
                }

                // Difficulty op
                if (types[TYPE_AUTOPLAY_FILE] || types[FLAG_AUTOPLAY_DIFICULTY]) {
                    project.setAutoplayPath(folder + File.separator + "autoplay");
                    if (project.getAutoplayPath().exists()) {
                        if (types[FLAG_AUTOPLAY_DIFICULTY]) project.setDifficulty("--/10");
                        if (!types[TYPE_AUTOPLAY_FILE]) project.setAutoplayPath(null);
                    } else project.setAutoplayPath(null);
                }
                if((project.getKeyLedPath(0) != null || project.getKeyLedPath(0) != null || project.getInfoPath() != null)) {
                    projects.add(new ProjectManager(project));
                }
            }
            if (projects.size() > 0){
                // Alphabetic order
                alphabeticOrder(projects);
            } else {
                projects = null;
            }
        }
    }

    public void alphabeticOrder(List<ProjectManager> list){
        Collections.sort(list, (m1, m2) -> m1.getProject().getTitle().compareToIgnoreCase(m2.getProject().getTitle()));
    }

    public List<ProjectManager> getProjects(){
        return projects;
    }
    public List<ProjectManager> getLoadedProjects(){ return projects_loaded; }
    public void addLoadedProject(ProjectManager project){
        if(projects_loaded == null) projects_loaded = new ArrayList<>();
        projects_loaded.add(project);
    }
    public void removeLoadedProject(ProjectManager project){ if(projects_loaded != null) projects_loaded.remove(project); }
}
