package com.xayup.multipad.projects.project.keyled;

import android.app.Activity;
import android.os.SystemClock;

import com.xayup.debug.XLog;
import com.xayup.multipad.pads.GridPadsReceptor;
import com.xayup.multipad.projects.thread.KeyLedThread;
import com.xayup.multipad.projects.thread.LoadProject;
import com.xayup.multipad.pads.PadPressCallInterface;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.projects.project.MapData;
import com.xayup.multipad.projects.Project;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.*;

public class KeyLED implements MapData, Project.KeyLEDInterface, PadPressCallInterface {
    protected LedMap mLedMap;
    protected KeyLedThread keyLedThread;

    public KeyLED() {
        this.mLedMap = new LedMap();
    }

    public void parse(File[] keyled_file, LoadProject.LoadingProject mLoadingProject) {
        new KeyLEDReader().read(keyled_file, mLedMap, mLoadingProject);
    }

    @Override
    public boolean showLed(int chain, int x, int y, int id) {
        XLog.v("Try show led", "");
        KeyLEDData ledData = mLedMap.getLedData(chain, x, y);
        if (ledData != null && ledData.length() > 0) {
            if(keyLedThread != null) keyLedThread.addLed(new ArrayList<>(List.of(ledData.getFrames())), id);
            XLog.v("Success show led", "");
            return true;
        }
        XLog.v("Error show led", "");

        return false;
    }

    public void setKeyLedThread(KeyLedThread keyLedThread) {
        this.keyLedThread = keyLedThread;
    }
    public KeyLedThread getKeyLedThread(){
        return this.keyLedThread;
    }

    public void resetSequence(){
        mLedMap.resetSequencesIndex();
    }
    
    public void clear(){
        mLedMap.clear();
    }

    @Override
    public boolean breakLed(int chain, int x, int y, int id) {
        return false;
    }

    @Override
    public boolean breakAll() {
        return false;
    }

    @Override
    public boolean call(GridPadsReceptor.PadGrid padGrid, MakePads.PadInfo padInfo) {
        return showLed(padGrid.getCurrentChain().getMc(), padInfo.getRow(), padInfo.getColum(), padGrid.getProject().getId());
    }
}
