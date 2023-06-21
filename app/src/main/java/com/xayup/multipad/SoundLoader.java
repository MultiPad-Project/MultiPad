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
import java.util.*;

public class SoundLoader {
  /*Map Object[] Index*/
  protected final int MAP_SOUNDS_INDEX = 0; //Return map with index to get sound from mSoundPlayer
  protected final int MAP_SOUND_SEQUENCE = 1; //Return int

  protected Activity context;
  protected SoundPool mSoundPool;

  protected List<SoundPlayer> mSoundPlayers;
  protected Map<String, Object[]> map;

  /*Current playing*/
  List<SoundPlayer> current_players;

  public SoundLoader(Activity context) {
    this.context = context;
    this.mSoundPlayers = new ArrayList<>();
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
    if(!map.containsKey(chain_and_pad)) map.put(chain_and_pad, new Object[]{new ArrayList<>(), 0});
    SoundPlayer player;
    ((ArrayList) map.get(chain_and_pad)[MAP_SOUNDS_INDEX]).add(mSoundPlayers.size());
    if(length > 5){
      /*Use ExoPlayer*/
      player = new PlayerExoPlayer(context, sound_file_path, to_chain){
        @Override
        public void onFinished(SoundPlayer player) {
          current_players.remove(player);
        }
      };
    } else {
      /*Use SoundPool*/
      player = new PlayerSoundPool(context, mSoundPool, sound_file_path, to_chain) {
        @Override
        public void onFinished(SoundPlayer player) {
          current_players.remove(player);
        }
      };
    }
    mSoundPlayers.add(player);
  }

  public void resetRpt() {
  }

  /**
   * @param chain_and_pad é a junção de ChainSl(1 a 24)+padId(viewId())
   */
  public void playSound(String chain_and_pad) {
    if(map.containsKey(chain_and_pad)) {
      Object[] tmp_map_sound_obj = map.get(chain_and_pad);
      SoundPlayer tmp_player = mSoundPlayers.get((int) ((ArrayList<?>) tmp_map_sound_obj[MAP_SOUNDS_INDEX]).get((int) tmp_map_sound_obj[MAP_SOUND_SEQUENCE]));
      context.runOnUiThread(tmp_player::play);
      current_players.add(tmp_player);
      if (tmp_player.getToChain() != -1){
        XayUpFunctions.touchAndRelease(context, Integer.parseInt(VariaveisStaticas.chainsIDlist.get(tmp_player.getToChain())), XayUpFunctions.TOUCH_AND_RELEASE);
      }
      if(((ArrayList<?>) tmp_map_sound_obj[MAP_SOUNDS_INDEX]).size()-1 == (int) tmp_map_sound_obj[MAP_SOUND_SEQUENCE]){
        tmp_map_sound_obj[MAP_SOUND_SEQUENCE] = 0;
      } else {
        tmp_map_sound_obj[MAP_SOUND_SEQUENCE] = 1 + (int) tmp_map_sound_obj[MAP_SOUND_SEQUENCE];
      }
    }
  }

  public void release() {
    mSoundPool.release();
    while(mSoundPlayers.isEmpty()){
      mSoundPlayers.remove(0).release();
    }
  }
}
