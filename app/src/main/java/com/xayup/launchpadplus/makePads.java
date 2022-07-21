package com.xayup.launchpadplus;



import android.view.*;
import android.view.View.*;
import android.widget.*;

import java.lang.reflect.Parameter;
import java.util.*;
import android.app.*;

import android.media.*;
import java.io.*;

public class makePads
{
	private Activity context;
	private ImageView phantom;
	private ImageView cantos;
	private String[] btnsIDs = {"0 0", "0 1", "0 2", "0 3", "0 4", "0 5", "0 6", "0 7", "0 8", "0 9",
		"1 0", "1 9", "2 0", "2 9", "3 0", "3 9", "4 0", "4 9", "5 0", "5 9", 
		"6 0", "6 9", "7 0", "7 9", "8 0", "8 9", "9 0", "9 1", "9 2", "9 3", 
		"9 4", "9 5", "9 6", "9 7", "9 8", "9 9"};
	private String[] chainIDs = {"19", "29", "39", "49", "59", "69", "79", "89"};
	private int[] mk2Chains = {100, 10, 20, 30, 40, 50, 60, 70, 80, 190, 91, 92, 93, 94, 95, 96, 97, 98, 99};

	Readers read = new Readers();
	private String currentProj;
	private playPads obt = new playPads();
	private keyLedColors ledLight;
	private int ViewID;
	private int largura;

	public makePads(String currentPath, int ViewID, final int largura, Activity activity){
	    this.context = activity;
        this.currentProj = currentPath;
        this.ViewID = ViewID;
        this.largura = largura;
    }

	public void makePadInLayout()
	{
		obt.pressLed = true;
		obt.chainSl = "1";

		int padWH = largura / 10;
		LinearLayout layoutpads = context.findViewById(ViewID);
		for (int l=0; l <= 9;l++)
		{
			LinearLayout line = new LinearLayout(context);
			View linev = line;
			layoutpads.addView(linev);
			for (int c=0; c <= 9; c++)
			{
				View layoutpad = context.getLayoutInflater().inflate(R.layout.pad, null);
				View cantov = null;
				float alpha = 0;
				final ImageView chainCurrent = layoutpad.findViewById(R.id.press);
				if (Arrays.asList(btnsIDs).contains(l + " " + c))
				{
					if ((l == 0 || l == 9) && (c == 0 || c == 9))
					{
						cantov = context.getLayoutInflater().inflate(R.layout.cantos, null);
						cantos = cantov.findViewById(R.id.canto);
						if (l == 0 && c == 9)
						{
							cantos.setImageDrawable(context.getDrawable(R.drawable.customlogo));
						}
						else
						{
							cantov.setVisibility(View.INVISIBLE);
							//cantos.setImageDrawable(context.getDrawable(R.drawable.cantos));
						}
					}
					else
					{
						phantom = layoutpad.findViewById(R.id.phantom);
						phantom.setImageDrawable(context.getDrawable(R.drawable.chainled));
						chainCurrent.setImageDrawable(context.getDrawable(R.drawable.currentchain));
						if (l == 1 && c == 9)
						{
							alpha = 1.0f;
						}
						else
						{
							alpha = 0.0f;
						}

						if ((l != 0 || l != 9) && (c == 0))
						{
							phantom.setRotationY(180);
						}
						else if ((c != 0 || c != 9) && (l == 0))
						{
							phantom.setRotation(-90);
						}
						else if ((c != 0 || c != 9) && (l == 9))
						{
							phantom.setRotation(90);
						}
					}
				}
				else
				{
					playPads.padPlayer.put(l + "" + c, new MediaPlayer());
                    playPads.ledrpt.put(l+""+c, 0);
                    playPads.soundrpt.put(l+""+c, 0);
					if (l == 4 && c == 4)
					{
						phantom = layoutpad.findViewById(R.id.phantom);
						phantom.setImageDrawable(context.getDrawable(R.drawable.phantom_));
						phantom.setRotation(0);
					}
					if (l == 4 && c == 5)
					{
						phantom = layoutpad.findViewById(R.id.phantom);
						phantom.setImageDrawable(context.getDrawable(R.drawable.phantom_));
						phantom.setRotation(90);
					}
					if (l == 5 && c == 4)
					{
						phantom = layoutpad.findViewById(R.id.phantom);
						phantom.setImageDrawable(context.getDrawable(R.drawable.phantom_));
						phantom.setRotation(-90);
					}
					if (l == 5 && c == 5)
					{
						phantom = layoutpad.findViewById(R.id.phantom);
						phantom.setImageDrawable(context.getDrawable(R.drawable.phantom_));
						phantom.setRotation(180);
					}
				}
				if ((l == 0 || l == 9) && (c == 0 || c == 9))
				{
					if((l == 0 || l == 9) && c == 0){
						cantov.setId(Integer.parseInt("1" + l + c));
					} else{
						cantov.setId(Integer.parseInt("" + l + c));
					}

					line.addView(cantov, padWH, padWH);
				}
				else
				{
					layoutpad.setId(Integer.parseInt(l + "" + c));
					chainCurrent.setAlpha(alpha);
					line.addView(layoutpad, padWH, padWH);
					layoutpad.setOnTouchListener(onTouch(Integer.parseInt(l + "" + c)));
				}
			}
		}
	}

    public OnTouchListener onTouch(int viewId){
        if(viewId == 3) {
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int viewId = view.getId();
                    ImageView press = view.findViewById(R.id.press);
                    if (playPads.autoPlayCheck) {
                        press.setAlpha(0.0f);
                        playPads.autoPlayCheck = false;
                        if(playPads.ledOn.isDaemon()){
                            playPads.ledOn.interrupt();
                        }
                    } else {
                        press.setImageDrawable(context.getDrawable(R.drawable.currentchain));
                        press.setAlpha(1.0f);
                        playPads.autoPlayCheck = true;
                        new autoPlayFunc(context).play();
                    }
                    return false;
                }
            };
        }
        else if(viewId == 7){
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int viewId = view.getId();
                    ImageView press = view.findViewById(R.id.press);
                    View currentView;
                    //make MK2 layout
                    if(!playPads.mk2){
                        for(int i = 1; i <= 99; i++){
                            if(i != 90 && !(Arrays.asList(mk2Chains).contains(i))){
                                currentView = context.findViewById(i);
                                ViewGroup.LayoutParams params = currentView.getLayoutParams();
                                params.height = largura/9;
                                params.width = largura/9;
                                currentView.setLayoutParams(params);
                            }
                        }
                        for(int i : mk2Chains){
                            currentView = context.findViewById(i);
                            ViewGroup.LayoutParams params = currentView.getLayoutParams();
                            params.height = 0;
                            params.width = 0;
                            currentView.setLayoutParams(params);
                        }
                        press.setImageDrawable(context.getDrawable(R.drawable.currentchain));
                        press.setAlpha(1.0f);
                        playPads.mk2 = true;
                    } else {
                        for(int i = 1; i <= 99; i++){
                            if(i != 90 && !(Arrays.asList(mk2Chains).contains(i))){
                                currentView = context.findViewById(i);
                                ViewGroup.LayoutParams params = currentView.getLayoutParams();
                                params.height = largura/10;
                                params.width = largura/10;
                                currentView.setLayoutParams(params);
                            }
                        }
                        for(int i : mk2Chains){
                            currentView = context.findViewById(i);
                            ViewGroup.LayoutParams params = currentView.getLayoutParams();
                            params.height = largura/10;
                            params.width = largura/10;
                            currentView.setLayoutParams(params);
                        }
                        press.setAlpha(0.0f);
                        playPads.mk2 = false;
                    }
                    return false;
                }
            };
        }
        else if (Arrays.asList(chainIDs).contains(viewId + ""))
        {
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int viewId = view.getId();
                    if(viewId != playPads.otherChain){
                        ImageView img = context.findViewById(playPads.otherChain).findViewById(R.id.press);
                        img.setAlpha(0.0f);
                        img = view.findViewById(R.id.press);
                        img.setImageDrawable(context.getDrawable(R.drawable.currentchain));
                        img.setAlpha(1.0f);
                        playPads.chainSl = ((viewId - 9) / 10) + "";
                        playPads.otherChain = view.getId();
                        for(String k : playPads.ledrpt.keySet()){
                            playPads.ledrpt.put(k, 0);
                            playPads.soundrpt.put(k, 0);
                        }

                    }
                    return false;
                }
            };
        }
        else if (!Arrays.asList(btnsIDs).contains(viewId))
        {
            return new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int viewId = view.getId();

                    ImageView pressed = context.findViewById(view.getId()).findViewById(R.id.press);
                                /*pressed.setImageDrawable(context.getDrawable(R.drawable.btn_));
                                pressed.setAlpha(1.0f);*/
                    boolean seek = false;
					if(obt.keySound.containsKey(obt.chainSl)){
						
                    if(obt.keySound.get(obt.chainSl).containsKey(viewId+"")){

                        if(playPads.soundrpt.get(""+viewId) == playPads.keySound.get(playPads.chainSl).get(view.getId() + "").size()){
                            playPads.soundrpt.put(""+viewId, 0);
                        }

                        try {
                                        /*if (obt.padPlayer.get(view.getId() + "").isPlaying() && seek) {
                                                obt.padPlayer.get(view.getId() + "").seekTo(0);
                                        }
                                        else {*/
                            if(playPads.padPlayer.get(view.getId() + "").isPlaying()) {
                                //obt.padPlayer.get(view.getId() + "").stop();
                                playPads.padPlayer.get(view.getId() + "").reset();
                            }
                            playPads.padPlayer.get(view.getId() + "").setDataSource(context, playPads.keySound.get(playPads.chainSl).get(view.getId() + "").get(playPads.soundrpt.get(viewId+"")));
                            playPads.padPlayer.get(view.getId() + "").prepare();
                            playPads.padPlayer.get(view.getId() + "").setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer p1) {
                                    p1.reset();
                                }
                            });
                            playPads.padPlayer.get(view.getId() + "" ).start();

                            //}
                        }
                        catch (IOException e)
                        {}
                        catch (IllegalStateException e)
                        {}
                        catch (SecurityException e)
                        {}
                        catch (IllegalArgumentException e)
                        {}
                        //obt.oldPad = viewId;

                        playPads.soundrpt.put(""+viewId, playPads.soundrpt.get(""+viewId)+1);
                    }
					}
                    if(playPads.ledFiles.get(playPads.chainSl+viewId) != null){
                        if(playPads.ledrpt.get(""+viewId) == playPads.ledFiles.get(playPads.chainSl+viewId).size()){
                            playPads.ledrpt.put(""+viewId, 0);
                        }
                        ledLight = new keyLedColors(playPads.ledrpt.get(""+viewId), obt.chainSl + view.getId(), currentProj, context);
                        ledLight.readKeyLed();
                        playPads.ledrpt.put(""+viewId, playPads.ledrpt.get(""+viewId)+1);
                    }
                    return false;
                }
            };
        }
        //Toast.makeText(context, ""+view.getId(), Toast.LENGTH_SHORT).show();
        return null;
    }
}
