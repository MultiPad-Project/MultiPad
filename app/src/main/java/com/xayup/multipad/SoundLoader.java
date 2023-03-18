package com.xayup.multipad;

import android.app.Activity;
import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import com.xayup.debug.XLog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundLoader {

  public final int SOUND = 0;
  public final int TYPE = 1;
  public final int TO_CHAIN = 2;
  public final int SOUNDPOOL = 3;
  public final int EXOPLAYER = 4;
  public final int SOUND_ID = 5;

  private Activity context;
  private Map<String, List<Map<Integer, Object>>> sounds;
  ExoPlayer mExoPlayer;
  SoundPool mSoundPool;
  Map<String, Integer> sound_rpt;

  // Temporario
  Map<Integer, Object> sound;

  public SoundLoader(Activity context) {
    this.context = context;
    this.sounds = new HashMap<String, List<Map<Integer, Object>>>();
    mExoPlayer = null;
    mSoundPool = null;
    sound_rpt = new HashMap<>();
  }
  /*
   * @Param sound_file é o arquivo de sample
   * @Param length é a duração da sample em segundos
   * @Param chain_and_pad é a junção de ChainSl(1 a 24)+padId(viewId())
   * @Param to_chain usado para identificar que deve-sse pular automaticamente para determinada chain
   */
  public void loadSound(String sound_file_path, int length, String chain_and_pad, String to_chain) {
    List<Map<Integer, Object>> to_map = new ArrayList<>();
    Map<Integer, Object> to_list = new HashMap<>();
    to_list.put(TO_CHAIN, to_chain);
    if (length > 5) { // ExoPlayer
      to_list.put(TYPE, EXOPLAYER);
      int list_size = 0;
      if (sounds.get(chain_and_pad) != null) {
        list_size = sounds.get(chain_and_pad).size();
      }
      MediaItem media =
          new MediaItem.Builder()
              .setMediaId(chain_and_pad + list_size)
              .setUri(sound_file_path)
              .build();
      to_list.put(SOUND, media);
      if (mExoPlayer == null) {
        mExoPlayer = new ExoPlayer.Builder(context).build();
      }
      context.runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              mExoPlayer.addMediaItem(media);
            }
          });
    } else { // SoundPool
      to_list.put(TYPE, SOUNDPOOL);
      if (mSoundPool == null) {
        mSoundPool = new SoundPool.Builder().setMaxStreams(10).build();
      }
      to_list.put(SOUND, mSoundPool.load(sound_file_path, 1));
    }
    /*
     * Condições: verifique se sounds contem uma lista para chain_and_pad.
     * Se contem, apenas adicione na lista. Caso contrário adicione uma lista.
     * para posteriormente adicionar itens nela.
     */
    if (sounds.get(chain_and_pad) == null) {
      to_map.add(to_list);
      sounds.put(chain_and_pad, to_map);
    } else {
      sounds.get(chain_and_pad).add(to_list);
    }
  }
  /*
   * Use sempre use isto pois pode haver ExoPlayer e será necessário prepará-lo no final
   */
  public void prepare() {
    if (mExoPlayer != null) {
      context.runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              mExoPlayer.prepare();
            }
          });
    }
  }

  public void resetRpt() {
    for (String key : sound_rpt.keySet()) {
      sound_rpt.put(key, 0);
    }
  }

  /*
   * @Param chain_and_pad é a junção de ChainSl(1 a 24)+padId(viewId())
   * @Param rpt quantos click já foi dada na mesma pad sequencialmente
   */
  public void playSound(String chain_and_pad) {
    try {
      if (sound_rpt.get(chain_and_pad) == null
          || sound_rpt.get(chain_and_pad) >= sounds.get(chain_and_pad).size()) {
        sound_rpt.put(chain_and_pad, 0);
      }
      int rpt = sound_rpt.get(chain_and_pad);
      sound = sounds.get(chain_and_pad).get(rpt);
      switch ((int) sound.get(TYPE)) {
        case EXOPLAYER:
          XLog.v("Play with", "ExoPlayer");
          mExoPlayer.pause();
          mExoPlayer.setMediaItem((MediaItem) sound.get(SOUND));
          mExoPlayer.play();
          break;
        case SOUNDPOOL:
          XLog.v("Play with", "SoundPool");
          if (sound.get(SOUND_ID) != null) mSoundPool.stop((int) sound.get(SOUND_ID));
          sound.put(SOUND_ID, mSoundPool.play((Integer) sound.get(SOUND), 1, 1, 1, 0, 1));
          break;
      }
      sound_rpt.put(chain_and_pad, sound_rpt.get(chain_and_pad) + 1);
      String to_chain = (String) sounds.get(chain_and_pad).get(rpt).get(TO_CHAIN);
      sound = null;
      if (to_chain != "") {
        XayUpFunctions.touchAndRelease(
            context,
            Integer.parseInt(VariaveisStaticas.chainsID[Integer.parseInt(to_chain)]),
            XayUpFunctions.TOUCH_AND_RELEASE);
      }
    } catch (NullPointerException e) {
      XLog.v("PlaySound() error", e.getMessage());
    }
  }

  public void release() {
    if (mSoundPool != null) {
      mSoundPool.release();
      mSoundPool = null;
    }
    if (mExoPlayer != null) {
      mExoPlayer.release();
      mExoPlayer = null;
    }
  }
}
