package com.xayup.multipad;

import android.content.Context;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.view.View;
import android.widget.*;
import android.app.*;
import com.xayup.multipad.pads.Render.MakePads;

import java.util.ArrayList;
import java.lang.*;

public class KeyLedColors {
    public KeyLedColors() { PlayPads.threadMap = new ArrayMap<String, ThreadLed>(); }

    public void readKeyLed(int rpt, int chain, int padid, Context context, MakePads.Pads mPads){
		String cpled = String.valueOf(chain) + padid;
		if((PlayPads.threadMap.get(cpled) != null) && !PlayPads.spamLeds && PlayPads.threadMap.get(cpled).isRunning()){
				PlayPads.threadMap.get(cpled).stop();
		}
		PlayPads.threadMap.put(cpled, new ThreadLed(context, chain, padid, rpt, mPads));
		PlayPads.threadMap.get(cpled).start();
    }
}
