package com.xayup.multipad;

import android.app.Activity;
import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import com.xayup.debug.XLog;
import com.xayup.multipad.sound.PlayerExoPlayer;
import com.xayup.multipad.sound.PlayerSoundPool;
import com.xayup.multipad.sound.SoundPlayer;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

public class SoundLoader {
  protected Activity context;
  protected SoundPool mSoundPool;

  protected boolean stop_all;
  /**/
  protected Map<String, List<SoundPlayer>> map;
  /*Current playing*/
  protected final List<SoundPlayer> current_players;

  protected int[] sequencer;

  public SoundLoader(Activity context) {
    this.context = context;
    this.stop_all = false;
    this.sequencer = new int[100];
    this.map = new HashMap<>();
    this.mSoundPool = new SoundPool.Builder().setMaxStreams(10).build();
    this.current_players = new ArrayList<>();
  }
  /**
   * @param sound_file_path é o arquivo de sample
   * @param length é a duração da sample em segundos
   * @param chain_and_pad é a junção de ChainSl(1 a 24)+padId(viewId())
   * @param to_chain usado para identificar que deve-se pular automaticamente para determinada chain
   */
  public void loadSound(String sound_file_path, int length, String chain_and_pad, String to_chain) {
    if(!map.containsKey(chain_and_pad)) map.put(chain_and_pad, new ArrayList<>());
    SoundPlayer player;
    if(length > 5){
      /*Use ExoPlayer*/
      player = new PlayerExoPlayer(context, sound_file_path, to_chain){
        @Override
        public void onFinished(SoundPlayer player) {
          XLog.v("onFinished()", "PlayerExoPlayer");
          synchronized (this) {
            if(!stop_all) current_players.remove(player);
          }
          XLog.v("Current list size", String.valueOf(current_players.size()));
        }
      };
    } else {
      /*Use SoundPool*/
      player = new PlayerSoundPool(context, mSoundPool, sound_file_path, to_chain) {
        @Override
        public void onFinished(SoundPlayer player) {
          XLog.v("onFinished()", "PlayerSoundPool");
          synchronized (this) {
            if(!stop_all) current_players.remove(player);
          }
          XLog.v("Current list size", String.valueOf(current_players.size()));
        }
      };
    }
    map.get(chain_and_pad).add(player);
  }

  public void resetSequencer() {
    Arrays.fill(sequencer, 0);
  }

  protected int[] getXY(String chain_and_pad){
    char[] chars = chain_and_pad.toCharArray();
    int row = Character.getNumericValue(chars[chars.length-2]);
    int colum = Character.getNumericValue(chars[chars.length-1]);
    return new int[]{row, colum};
  }

  public int getSequence(int row, int colum){
    return sequencer[(row*10)+colum];
  }

  protected void changeSequence(int row, int colum, int value){
    sequencer[(row*10)+colum] = value;
  }

  /**
   * @param chain_and_pad é a junção de ChainSl(1 a 24)+padId(viewId())
   */
  public void playSound(String chain_and_pad) {
    if(map.containsKey(chain_and_pad)) {
      List<SoundPlayer> tmp_map_sound = map.get(chain_and_pad);
      if(tmp_map_sound == null || tmp_map_sound.size() < 1) return;
      int[] xy = getXY(chain_and_pad);
      int sequence = getSequence(xy[0], xy[1]);
      if(sequence >= tmp_map_sound.size()) {
        sequence = 0;
      }
      SoundPlayer tmp_player = tmp_map_sound.get(sequence++);
      context.runOnUiThread(tmp_player::play);
      current_players.add(tmp_player);
      if (tmp_player.getToChain() != -1){
        XayUpFunctions.touchAndRelease(context, Integer.parseInt(VariaveisStaticas.chainsIDlist.get(tmp_player.getToChain())), XayUpFunctions.TOUCH_AND_RELEASE);
      }
      changeSequence(xy[0], xy[1], sequence);
    }
  }

  public void stopAll(){
    stop_all = true;
    while(!current_players.isEmpty()) current_players.remove(0).stop();
    stop_all = false;
  }

  public void release() {
    mSoundPool.release();
    List<String> keys = new ArrayList<>(map.keySet());
    while(!keys.isEmpty()){
      List<SoundPlayer> players = map.get(keys.remove(0));
      if(players == null) continue;
      while(!players.isEmpty()) players.remove(0).release();
    }
    map.clear();
    sequencer = null;
  }
}
