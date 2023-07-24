package com.xayup.multipad.projects;

import android.app.Activity;
import android.content.Context;
import com.xayup.debug.XLog;
import com.xayup.multipad.pads.GridPadsReceptor;
import com.xayup.multipad.pads.PadPressCall;
import com.xayup.multipad.projects.project.autoplay.AutoPlay;
import com.xayup.multipad.projects.project.keyled.KeyLED;
import com.xayup.multipad.projects.project.keysounds.KeySounds;
import com.xayup.multipad.projects.thread.KeyLedThread;
import com.xayup.multipad.projects.thread.LoadProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectManager implements KeyLedThread.ShowLed {
    protected Project project;
    protected Map<Integer, List<GridPadsReceptor.PadGrid>> grids;
    protected PadPressCall padPressCall;

    //Load
    List<String> project_loaded_problems;

    // Project Opened
    protected KeySounds mKeySounds;
    protected KeyLED mKeyLED;
    protected AutoPlay mAutoPlay;

    public ProjectManager(Project project){
        this.project = project;
    }

    public void addGrid(GridPadsReceptor.PadGrid grid){
        if(grids == null){ grids = new HashMap<>(); }
        if(!grids.containsKey(grid.getId())) grids.put(grid.getId(), new ArrayList<>());
        if(!grids.get(grid.getId()).contains(grid)) grids.get(grid.getId()).add(grid);
    }

    public void removeGrid(GridPadsReceptor.PadGrid grid){
        if(grids != null){
            if(grids.get(grid.getId()) != null) {
                grids.get(grid.getId()).remove(grid);
            }
        }
    }

    public List<GridPadsReceptor.PadGrid> getGridsFromId(int id){
        return grids.get(id);
    }

    public void loadProject(Context context, LoadProject.LoadingProject mLoadingProject){
        project_loaded_problems = new ArrayList<>();
        new LoadProject(context, mLoadingProject, this){
            @Override
            public void onFinish() {
                padPressCall = new PadPressCall();
                if(mKeyLED != null) {
                    XLog.e("onFinish(): KeyLED", "Instanced");
                    /*
                    mKeyLED.setToShowLed((row, colum, android_color, lp_index) -> {
                        List<GridPadsReceptor.PadGrid> padGrids = getGridsFromId(lp_index);
                        if(padGrids != null) for(GridPadsReceptor.PadGrid padGrid : padGrids){
                            padGrid.led(row, colum, android_color);
                        }
                    });

                     */
                    padPressCall.calls.add(mKeyLED);
                }
                if(mKeySounds != null){
                    XLog.e("onFinish(): KeySounds", "Instanced");
                    padPressCall.calls.add(mKeySounds);
                }
                if(mAutoPlay != null){
                    XLog.e("onFinish(): AutoPlay", "Instanced");
                    padPressCall.calls.add(mAutoPlay);
                }
            }
        };
    }

    public Project getProject(){ return project; }
    public KeySounds getKeySounds() {return mKeySounds;}
    public AutoPlay getAutoPlay() {return mAutoPlay;}
    public KeyLED getKeyLED() {return mKeyLED;}
    public PadPressCall getPadPressCall(){
        XLog.e("getPadPressCall: ProjectManager", String.valueOf(padPressCall.calls.size()));
        return padPressCall;
    }

    public void keyLEDInstance(Context context){ mKeyLED = new KeyLED(); }
    public void autoPlayInstance(Context context){ mAutoPlay = new AutoPlay((Activity) context); }
    public void keySoundsInstance(Context context){ mKeySounds = new KeySounds((Activity) context); }

    /**
     * Feche este projeto e libere memoria
     */
    public void release(){}

    @Override
    public void onShowLed(int row, int colum, int color, int grid_id) {
        XLog.v("ProjectManager", "On Show Led");
        List<GridPadsReceptor.PadGrid> grids = getGridsFromId(grid_id);
        if(grids != null){
            for(GridPadsReceptor.PadGrid grid : grids){
                grid.led(row, colum, color);
            }
        }
    }
}
