package com.xayup.multipad;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.annotation.IntegerRes;
import java.util.concurrent.atomic.AtomicBoolean;

public class autoPlayFunc {
    private Activity context;
	private AtomicBoolean running;
	private boolean touch;
	private AtomicBoolean paused;
	private int lineInt;
	private int autoplaySize;
	long time;
	protected int padWaiting;
	private String chain;
	private boolean seekChange;
	protected final int REQUEST_BTN = 0;
	protected final int REQUEST_CHAIN = 1;
	protected final int REQUEST_PRATICLE = 2;
    protected int touch_type = 0;
	private boolean request_returnto_chain; //(Pratica) True se a chain requerida ainda nao foi selecionada
	private boolean chainChanged;
	private int waitViewId;
	private int waitRequest;
    autoPlayFunc(Activity context){
        this.context = context;
		running = new AtomicBoolean(false);
		paused = new AtomicBoolean(false);
		touch = false;
		lineInt = 0;
		autoplaySize = playPads.autoPlay.size();
		time = 0;
		padWaiting = 0;
		chain = "19";
		seekChange= false;
		chainChanged = false;
		request_returnto_chain = false;
		waitViewId = 0;
		waitRequest = 0;
    }
	protected boolean isRunning(){
		return running.get();
	}
	protected boolean isPaused(){
		return paused.get();
	}
	protected void pause(){
		paused.set(true);
	}
	public void seekBarChanging(boolean stateChange){
		seekChange = stateChange;
	}
	protected void start(){
		time = SystemClock.uptimeMillis();
		paused.set(false);
		touch(0);
	}
	protected void stop(){
		running.set(false);
	}
	protected void touch(int chpadId){
		if(chpadId == padWaiting || chpadId == 0){
			touch = true;
		}
	}
	
	protected int getProgress(){
		return lineInt/*(int)(lineInt*100)/autoplaySize*/;
	}
	
	public void setProgress(int Percents){
	//	seekChange = true;
		lineInt = Percents;//(autoplaySize/100)*Percents;
	}
	
	protected void prev(){
		lineInt = lineInt - (autoplaySize/25);
		if(lineInt < 0){
			lineInt = 0;
		}
		if(paused.get()){
			touch(0);
		}
	}
    
	protected void chainChanged(){
		if(!((String)""+playPads.chainId).equals(chain))
			if(!request_returnto_chain){
				request_returnto_chain = true;
				chainChanged = true;
			}
	}
	
	protected void next(){
		lineInt = lineInt + (autoplaySize/25);
		if(lineInt >= autoplaySize){
			lineInt = autoplaySize-1;
		}
		if(paused.get()){
			touch(0);
		}
	}
	
	private void progressUpadate(){
		playPads.progressAutoplay.setProgress(getProgress());
	}
	
	private void pausedEvents(final int ViewId, int color, float alpha, int request){
		context.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				View touchInView = context.findViewById(ViewId);
				switch(request){
					case REQUEST_CHAIN:
						((ImageView)touchInView.findViewById(R.id.press)).setAlpha(playPads.watermark);
						((ImageView)touchInView.findViewById(R.id.press)).setImageDrawable(context.getDrawable(R.drawable.currentchain));
						break;
					case REQUEST_BTN:
						((ImageView)touchInView.findViewById(R.id.press)).setAlpha(alpha);
						((ImageView)touchInView.findViewById(R.id.press)).setImageDrawable(SkinTheme.btn_);
				        break;
					default:
						((ImageView)touchInView.findViewById(R.id.press)).setAlpha(1.0f);
						((ImageView)touchInView.findViewById(R.id.press)).setImageDrawable(new ColorDrawable(Color.GREEN));
						break;
				}
			}
        });
	}
	private void runPlay(final int ViewId, final int TOUCH){
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				View v = context.findViewById(ViewId);
                            v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
			            	v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
                }
		});
	}
	private void autoplaEvents(final int ViewId, final int request, int TOUCH){
		if(paused.get()){
			padWaiting = Integer.parseInt(chain+ViewId);
			pausedEvents(ViewId, Color.GREEN, 1.0f, REQUEST_PRATICLE);
			while((isRunning()) && paused.get() && !chainChanged && !touch && !seekChange){}
			pausedEvents(ViewId, 0, playPads.padPressAlpha, request);
			touch = false;
			seekChange = false;
			if(chainChanged){
				chainChanged = false;
				waitViewId = ViewId;
				waitRequest = request;
				autoplaEvents(Integer.parseInt(chain), REQUEST_CHAIN, TOUCH);
			}
			if((chainChanged == false) && (request_returnto_chain == true)){
				request_returnto_chain = false;
				autoplaEvents(waitViewId, waitRequest, TOUCH);
			} 
		} else {
            //runPlay(ViewId, TOUCH);
            XayUpFunctions.touchAndRelease(context, ViewId, TOUCH);
		}
	}
    public void play(){
    final Thread thread =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                time = SystemClock.uptimeMillis();
                int delay = 0;
                int touchViewId = 3;
                View touchInView;
                running.set(true);
                paused.set(false);
                String line = null;
                boolean inDelay = false;
                for (lineInt = 0; lineInt < autoplaySize; lineInt++) {
                  if (!playPads.progressAutoplay.getStatePressed()) {
                    progressUpadate();
                  }
                  line = playPads.autoPlay.get(lineInt);
                  if (running.get()) {
                    line = line.replace(" ", "").toLowerCase();
                    if (!line.isEmpty()) {
                      chain = line.substring(0, 2);
                      int request = REQUEST_BTN;
                                /*
                                padEvent e o evento que o autoplay repassa. Exemplo: "o" e "ON" (press), "f" e "OFF" (Release) and "t" e "TOCUH" (Touch and release))
                                */
                                String padEvent = line.substring(2, 3);
                      switch (padEvent) {
                        case "c":
                          touchViewId =
                              Integer.parseInt(
                                  VariaveisStaticas.chainsIDlist.get(
                                      Integer.parseInt(line.substring(3))));
                          request = REQUEST_CHAIN;
                          inDelay = false;
                          break;
                        case "d":
                          delay = Integer.parseInt(line.substring(line.indexOf("d") + 1));
                          inDelay = true;
                          break;
                        default:
                          inDelay = false;
                          touchViewId = Integer.parseInt(line.substring(line.length() - 2));
                          if (!(((String) "" + playPads.chainId).equals(chain) && paused.get()))
                            XayUpFunctions.touchAndRelease(context, Integer.parseInt(chain), XayUpFunctions.TOUCH_AND_RELEASE);
                          Log.e("Default", "default");
                                    switch(padEvent){
                                        case "f":
                              touch_type = XayUpFunctions.RELEASE;
                              Log.e("Touch", "Release");
                                        break;
                            case "t":
                              touch_type = XayUpFunctions.TOUCH_AND_RELEASE;
                              Log.e("Touch", "Touch and Release");
                                        break;
                            case "o":
                              touch_type = XayUpFunctions.TOUCH;
                              Log.e("Touch", "Touch");
                                        break;
                                    }
                          break;
                      }
                      Log.e("AAAAAAAAA", "AAAAAAAAA");
                      if (!inDelay) {
                        autoplaEvents(touchViewId, request, touch_type);
                      }
                      time = SystemClock.uptimeMillis();
                      time += delay;
                      delay = 0;
                      while (((running.get()) && !paused.get())
                          && SystemClock.uptimeMillis() < time
                          && !seekChange) {}
                      seekChange = false;
                    }
                  } else {
                    break;
                  }
                }
                context.runOnUiThread(
                    new Runnable() {
                      @Override
                      public void run() {
                        ((ImageView) context.findViewById(3).findViewById(R.id.press))
                            .setAlpha(0.0f);
                        ((RelativeLayout) context.findViewById(4))
                            .removeView(context.findViewById(3004));
                        ((RelativeLayout) context.findViewById(5))
                            .removeView(context.findViewById(3005));
                        ((RelativeLayout) context.findViewById(6))
                            .removeView(context.findViewById(3006));

                        playPads.autoPlayCheck = false;
                        padWaiting = -1;
                        ((SeekBar) playPads.progressAutoplay).setVisibility(View.GONE);
                        stop();
                      }
                    });
              }
            });
        thread.start();
    }
}