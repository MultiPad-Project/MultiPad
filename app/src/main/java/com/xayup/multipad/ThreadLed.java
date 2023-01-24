package com.xayup.multipad;

import android.app.*;
import android.media.midi.MidiInputPort;
import android.os.SystemClock;
import android.view.View;
import android.widget.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.*;

public class ThreadLed implements Runnable {
	private AtomicBoolean running = new AtomicBoolean(false);
	private Activity context;
	private String cpled;
	private int rpt;
	private View root;
    private int loop = -1;

	public ThreadLed(final Activity context, final String cpled, final int rpt, View root) {
		this.context = context;
		this.cpled = cpled;
		this.rpt = rpt;
		this.root = root;
	}

	boolean isRunning() {
		return running.get();
	};

	protected void stop() {
        try {
		    running.set(false);
        } catch(NullPointerException n){}
	}

	public void runn() {
		run();
	}

	protected void start() {
		running.set(true);
        new Thread(this).start();
	}
    //Quando o tipo de led e 0: (2 5 1 0 a)
    //                                 ^ 
    public void stopZeroLooper(){
        if(loop == 0){
            stop();
        }
    }
    private void offCurrentLedLoop(List<String> led){
        boolean mc = false; /* chainLed */
        int padId; /* padLed */
        int corcode = 0; /* Color code. 0 = OFF */
        /*
        * haveOff sera usado para armazenar as pads
        * que ja foram desligados para evitar o mesmo
        * processo em uma pad ja processada
        */
        List<Integer> haveOff = new ArrayList<Integer>();
        read: for(String line : led){
            switch (line.substring(0, 1)) {
				case "o":
					if (line.contains("mc")) {
						mc = true;
						padId = VariaveisStaticas.chainCode[Integer.parseInt(line.substring(3, line.indexOf("a")))];
					} else if(line.toLowerCase().contains("l")){
						padId = 9;
					} else {
						padId = Integer.parseInt(line.substring(1, 3));
					}
                    if(haveOff.contains(padId)){
                        continue read;
                    } else {
                        haveOff.add(padId);
                        showLed(padId, corcode, mc);
                    }
					break;
				}
        }
        haveOff = null;
    }
    
    private void showLed(final int padid, final int corCode, final boolean MC){
        context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ImageView pad = context.findViewById(padid).findViewById(R.id.led);
							int color = VariaveisStaticas.colorInt(corCode, PlayPads.custom_color_table, PlayPads.oldColors);
							byte NOTE = MidiStaticVars.NOTE_ON;
							if(corCode == 0) NOTE = MidiStaticVars.NOTE_OFF;
							if(PlayPads.glowEf && padid != 9){
									ImageView glowEF = context.findViewById(Integer.parseInt("100"+padid));
									if(color == 0){
										glowEF.setAlpha(0.0f);
									} else {
										if(MC){
											glowEF.setAlpha(PlayPads.glowChainIntensity);
										} else {
											glowEF.setAlpha(PlayPads.glowIntensity);
										}
										
										glowEF.setColorFilter(color);
									}
							}
							pad.setBackgroundColor(color);
							if(MidiStaticVars.midiInput != null){
								try{
								int offset = 0;
								int channel = 1;
								int numBytes = 0;
								byte[] bytes = new byte[32];
								bytes[numBytes++] = (byte) (NOTE + (channel - 1));
								bytes[numBytes++] = (byte) (UsbDeviceActivity.rowProgramMode(padid));
								bytes[numBytes++] = (byte) corCode;
								MidiStaticVars.midiInput.send(bytes, offset, numBytes);
								} catch (IOException e){
									Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
								}
							}
						}
					});
    }
    
	@Override
	public void run() {
		if (PlayPads.ledFiles.get(cpled) != null) {
			long time = SystemClock.uptimeMillis();
			boolean nobreak = true;
			boolean delay = false;
            int indexLoop = 1;
            try {
                loop = Integer.parseInt(PlayPads.ledFiles.get(cpled).get(rpt).get(0));
                } catch (NumberFormatException n){
                    loop = 1;
                } catch (IndexOutOfBoundsException i){}
			String line;
            looper: while(true){
            for (int i = 1; i < PlayPads.ledFiles.get(cpled).get(rpt).size(); i++) {
                line = PlayPads.ledFiles.get(cpled).get(rpt).get(i);
				if (!isRunning() || PlayPads.stopAll) {
                    if(loop == 0)
                        offCurrentLedLoop(PlayPads.ledFiles.get(cpled).get(rpt));
                    else
				    	XayUpFunctions.clearLeds(context, root);
					break looper;
				}
				//	String line = PlayPads.ledFiles.get(cpled).get(rpt).get(i);
				delay = false;
				int padId = 0;
				int corcode = 0;
				boolean mc = false;
				switch (line.substring(0, 1)) {
				case "o":
					if (line.contains("mc")) {
						mc = true;
						padId = VariaveisStaticas.chainCode[Integer.parseInt(line.substring(3, line.indexOf("a")))];
					} else if(line.toLowerCase().contains("l")){
						System.out.println(line);
						padId = 9;
					} else {
						padId = Integer.parseInt(line.substring(1, 3));
					}
				//	PlayPads.ledOcuped.add(padId);
					corcode = Integer.parseInt(line.substring(line.indexOf("a") + 1));
					if (corcode > 127) {
						corcode = 0;
					}
					break;
				case "f":
					corcode = 0;
					if (line.contains("mc")) {
						mc = true;
						//	System.out.println(line);
						padId = VariaveisStaticas.chainCode[Integer.parseInt(line.substring(3))];
					} else if(line.contains("l")){
						padId = 9;
						} else {
						padId = Integer.parseInt(line.substring(1));
					}
					break;
				case "d":
					time = SystemClock.uptimeMillis();
					time += Integer.parseInt(line.substring(1));
					delay = true;
					break;
				}
				while ((SystemClock.uptimeMillis() < time) && (!PlayPads.stopAll) && isRunning()) {
				}

				if (!delay) {
					showLed(padId, corcode, mc);
				}
			}
                if(loop != 0){
                    if(indexLoop < loop){
                        indexLoop++;
                    } else {
                        break;
                    }
                }
                }
		}
	}
}