package com.xayup.multipad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.*;
import com.xayup.multipad.midi.MidiStaticVars;
import com.xayup.multipad.pads.Render.MakePads;

import java.io.IOException;

public class XayUpFunctions {
    
    public static final int RELEASE = 0;
    public static final int TOUCH = 1;
    public static final int TOUCH_AND_RELEASE = 2;
    
	private static int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

	//Fullscreen AlertDialog
	protected static void showDiagInFullscreen(AlertDialog theDialog) {
		theDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		theDialog.getWindow().getDecorView().setSystemUiVisibility(flags);
		theDialog.show();
		theDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

	protected static void showDiagInFullscreen(ProgressDialog theDialog) {
		theDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		theDialog.getWindow().getDecorView().setSystemUiVisibility(flags);
		theDialog.show();
		theDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

	protected static void showDiagInFullscreen(Dialog theDialog) {
		theDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		theDialog.getWindow().getDecorView().setSystemUiVisibility(flags);
		theDialog.show();
		theDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

	//Fullscreen current window
	protected static void hideSystemBars(Window getWindow) {
		getWindow.getDecorView().setSystemUiVisibility(flags);
		final View decorView = getWindow.getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int arg0) {
				if (arg0 == 0) {
					decorView.setSystemUiVisibility(flags);
				}
			}
		});
	}

	//stop leds
	public static void clearLeds(Context context, MakePads.Pads mPads) {
		Handler handler = new Handler(context.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				for (int i = 1; (i <= 98); i++) {
					if (i != 90) {
						int row = i/10;
						int colum = i%10;
						mPads.getPadView(row, colum).findViewById(MakePads.PadInfo.PadLayerType.LED).setBackgroundColor(Color.TRANSPARENT);
						mPads.getGlows().getGlow(row, colum).setAlpha(0f);
						if(i != 9) {
							try {
								if (MidiStaticVars.midiInput != null) {
									int offset = 0;
									int numBytes = 0;
									byte[] bytes = new byte[32];
									bytes[numBytes++] = (byte) ((MidiStaticVars.NOTE_OFF & 0xFF)
											+ (MidiStaticVars.CHANNEL - 1));
									bytes[numBytes++] = (byte) (UsbDeviceActivity.rowProgramMode(i, false));
									bytes[numBytes++] = (byte) 0;
									MidiStaticVars.midiInput.send(bytes, numBytes, offset);
								}
							} catch (IOException ignored) {
							}
						}
					}
				}
				handler.removeCallbacks(this);
			}
		});
	}

	//stop sounds
    /*
	public static void stopSounds() {
		if (PlayPads.exoplayers != null) {
			for (String p : PlayPads.exoplayers.keySet()) {
				if (PlayPads.exoplayers.get(p).isPlaying()) {
					PlayPads.exoplayers.get(p).pause();
				}
				PlayPads.exoplayers.get(p).release();
			}
            PlayPads.keySound = null;
		} else if (PlayPads.keySoundPool != null) {
            for(Integer stream : PlayPads.streamsPool.values()){
                PlayPads.soundPool.stop(stream);
            }
            PlayPads.soundPool.release();
            PlayPads.keySoundPool = null;
            PlayPads.toChainPool = null;
        }
	}
    */
	//Toque na visualizacao
	public static void touchAndRelease(final Activity context, final int ViewId, final int type) {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
                    View v = context.findViewById(ViewId);
                    switch(type){
                        case TOUCH:
                            v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
                        break;
                        case RELEASE:
                            v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
                        break;
                        case TOUCH_AND_RELEASE:
                            v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
			            	v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
			
                        break;
                    }
				}
		});
	}

	//hide pad/chains overlay
	public static void changePadsPhantomLayerVisibility(ViewGroup grid, int visibility) {
		if(grid != null) {
			for(int i = 0; i < grid.getChildCount(); i++){
				if(grid.getChildAt(i) instanceof ViewGroup){
					ViewGroup pad = (ViewGroup) grid.getChildAt(i);
					for(int pi = 0; pi < grid.getChildCount(); pi++){
						switch (pad.getChildAt(pi).getId()){
							case MakePads.PadInfo.PadLayerType.PHANTOM:
							case MakePads.PadInfo.PadLayerType.PHANTOM_:
							case MakePads.PadInfo.PadLayerType.LOGO:
							case MakePads.PadInfo.PadLayerType.CHAIN_LED:
								pad.getChildAt(pi).setVisibility(visibility);
								pi = grid.getChildCount();
						}
					}
				}
			}
			grid.requestLayout();
		}
	}
}