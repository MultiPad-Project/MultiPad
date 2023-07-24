package com.xayup.multipad.projects.project.keysounds;

import android.app.Activity;
import android.content.Context;
import android.media.SoundPool;

import com.xayup.debug.XLog;
import com.xayup.multipad.VariaveisStaticas;
import com.xayup.multipad.XayUpFunctions;
import com.xayup.multipad.pads.Render.MakePads;
import com.xayup.multipad.sound.PlayerExoPlayer;
import com.xayup.multipad.sound.PlayerSoundPool;
import com.xayup.multipad.sound.SoundPlayer;

import java.util.*;

public class SoundLoader {
  protected final int SYNCHRONIZE_WITH_MILLIS = 500;

  protected Activity context;
  protected SoundPool mSoundPool;

  protected boolean stop_all;
  /**/
  protected Map<String, List<SoundPlayer>> map;
  /*Current playing*/
  protected List<SoundPlayer> current_players;

  int[] sequencer;

  public SoundLoader(Context context) {
    this.context = (Activity) context;
    this.stop_all = false;
    this.sequencer = new int[100/*row 10*10 columns*/];
    this.map = new HashMap<>();
    this.mSoundPool = new SoundPool.Builder().setMaxStreams(10).build();
    this.current_players = new ArrayList<>();
  }
  /**
   * @param sound_file_path é o arquivo de sample
   * @param necessary_info required information obtained by "KeySoundsReader > getNecessaryMetadata()".
   * @param chain_and_pad é a junção de ChainSl(1 a 24)+padId(viewId())
   * @param to_chain usado para identificar que deve-se pular automaticamente para determinada chain
   */
  public void loadSound(String sound_file_path, int[] necessary_info, String chain_and_pad, String to_chain) {
    //XLog.v("Sound Durations Metadata", sound_file_path.substring(sound_file_path.lastIndexOf(File.separator)) + ": " + Arrays.toString(necessary_info));
    if(!map.containsKey(chain_and_pad)) map.put(chain_and_pad, new ArrayList<>());
    SoundPlayer player;
    if(necessary_info[1] > 950 || necessary_info[0] > 5400 /*5 seconds*/){
      /*Use ExoPlayer*/
      //XLog.v("Use Sample Player", "PlayerExoPlayer");
      player = new PlayerExoPlayer(context, sound_file_path, to_chain){
        @Override
        public void onFinished(SoundPlayer player) {
          //XLog.v("onFinished()", "PlayerExoPlayer");
          if(!stop_all) current_players.remove(player);
          //XLog.v("Current list size", String.valueOf(current_players.size()));
        }
      };
    } else {
      /*Use SoundPool*/
      //XLog.v("Use Sample Player", "PlayerSoundPool");
      player = new PlayerSoundPool(context, mSoundPool, sound_file_path, necessary_info[0], to_chain) {
        @Override
        public void onFinished(SoundPlayer player) {
          //XLog.v("onFinished()", "PlayerSoundPool");
          if(!stop_all) current_players.remove(player);
          //XLog.v("Current list size", String.valueOf(current_players.size()));
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
    return sequencer[MakePads.PadID.getGridIndexFromXY(10, row, colum)];
  }

  protected void changeSequence(int row, int colum, int value){
    sequencer[MakePads.PadID.getGridIndexFromXY(10, row, colum)] = value;
  }

  /**
   * @param chain_and_pad é a junção de ChainSl(1 a 24)+padId(viewId())
   */
  public boolean playSound(String chain_and_pad) {
    if(map.containsKey(chain_and_pad)) {
      List<SoundPlayer> tmp_map_sound = map.get(chain_and_pad);
      if(tmp_map_sound == null || tmp_map_sound.size() < 1) return false;
      int[] xy = getXY(chain_and_pad);
      //XLog.v("XY", Arrays.toString(xy));
      int sequence = getSequence(xy[0], xy[1]);
      if(sequence >= tmp_map_sound.size()) {
        sequence = 0;
      }
      //XLog.v("sequence", Arrays.toString(sequencer));
      SoundPlayer tmp_player = tmp_map_sound.get(sequence++);
      if(!useSynchronizeSample(tmp_player)) context.runOnUiThread(tmp_player::play);
      if(!current_players.contains(tmp_player)) current_players.add(tmp_player);
      if (tmp_player.getToChain() != -1){
        XayUpFunctions.touchAndRelease(context, Integer.parseInt(VariaveisStaticas.chainsIDlist.get(tmp_player.getToChain())), XayUpFunctions.TOUCH_AND_RELEASE);
      }
      changeSequence(xy[0], xy[1], sequence);
      return true;
    }
    return false;
  }

  public boolean useSynchronizeSample(SoundPlayer sample){
    boolean use_synchronize_samples = false;
    if(!use_synchronize_samples) return false;
    SoundPlayer last_sound;
    if(current_players.size() > 0
            && (last_sound = current_players.get(current_players.size()-1)).restTime() <= SYNCHRONIZE_WITH_MILLIS
            ) {
      XLog.v("SOUND CURRENT TIME", String.valueOf(last_sound.currentTime()));
      last_sound.appendAfterFinish(() -> context.runOnUiThread(sample::play));
      return true;
    }
    return false;
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
  }
}
