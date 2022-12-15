package com.xayup.multipad;

import android.app.*;
import android.media.midi.MidiInputPort;
import android.os.SystemClock;
import android.view.View;
import android.widget.*;
import java.io.IOException;
import java.util.concurrent.atomic.*;

public class ThreadLed implements Runnable {
	private AtomicBoolean running = new AtomicBoolean(false);
	private Activity context;
	private String cpled;
	private int rpt;
	private View root;

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
		running.set(false);

	}

	public void runn() {
		run();
	}

	protected void start() {
		running.set(true);
		new Thread() {
			public void e() {
			}

			@Override
			public void run() {
				runn();
			}
		}.start();
	}

	@Override
	public void run() {
		if (playPads.ledFiles.get(cpled) != null) {
			long time = SystemClock.uptimeMillis();
			boolean nobreak = true;
			boolean delay = false;
			//int size =
			for (String line : playPads.ledFiles.get(cpled).get(rpt)) {
				if (!isRunning() || playPads.stopAll) {
					XayUpFunctions.clearLeds(context, root);
					return;
				}
				//	String line = playPads.ledFiles.get(cpled).get(rpt).get(i);
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
				//	playPads.ledOcuped.add(padId);
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
				//time += System.currentTimeMillis();
				while ((SystemClock.uptimeMillis() < time) && (!playPads.stopAll) && isRunning()) {
				}

				if (!delay) {
					//	runUithread(context, padId, corcode);
					final int padid = padId;
					final int corCode = corcode;
					final boolean MC = mc;
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ImageView pad = context.findViewById(padid).findViewById(R.id.led);
							int color = VariaveisStaticas.colorInt(corCode, playPads.custom_color_table, playPads.oldColors);
							byte NOTE = MidiStaticVars.NOTE_ON;
							if(corCode == 0) NOTE = MidiStaticVars.NOTE_OFF;
							if(playPads.glowEf && padid != 9){
									ImageView glowEF = context.findViewById(Integer.parseInt("100"+padid));
									if(color == 0){
										glowEF.setAlpha(0.0f);
										
									} else {
										if(MC){
											glowEF.setAlpha(playPads.glowChainIntensity);
										} else {
											glowEF.setAlpha(playPads.glowIntensity);
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
			}
			 
		}
	}
}