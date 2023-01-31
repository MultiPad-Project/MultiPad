package com.xayup.multipad;

import android.app.Activity;
import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundLoader {

    public final String SOUND = "sound";
    public final String TYPE = "type";
    public final String TO_CHAIN = "to_chain";
    public final String SOUNDPOOL = "SoundPool";
    public final String EXOPLAYER = "ExoPlayer";

    private Activity context;
    private Map<String, List<Map<String, Object>>> sounds;
    ExoPlayer mExoPlayer;
    SoundPool mSoundPool;
    Map<String, Integer> sound_rpt;
    public SoundLoader(Activity context){
        this.context = context;
        this.sounds = new HashMap<String, List<Map<String, Object>>>();
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
    public void loadSound(String sound_file_path, int length, String chain_and_pad, String to_chain){
        List<Map<String, Object>> to_map = new ArrayList<>();
        Map<String, Object> to_list = new HashMap<>();
        to_list.put(TO_CHAIN, to_chain);
        if(length > 5){ //ExoPlayer
            to_list.put(TYPE, EXOPLAYER);
            int list_size = 0;
            if(sounds.get(chain_and_pad) != null){
                list_size = sounds.get(chain_and_pad).size();
            }
            MediaItem media = new MediaItem.Builder().setMediaId(chain_and_pad+list_size).setUri(sound_file_path).build();
            to_list.put(SOUND, media);
            if(mExoPlayer == null){
                mExoPlayer = new ExoPlayer.Builder(context).build();
            }
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mExoPlayer.addMediaItem(media);
                }
            });
        } else { //SoundPool
            to_list.put(TYPE, SOUNDPOOL);
            if(mSoundPool == null){
                mSoundPool = new SoundPool.Builder().setMaxStreams(7).build();
            }
            to_list.put(SOUND, mSoundPool.load(sound_file_path, 1));
        }
        /*
        * Condições: verifique se sounds contem uma lista para chain_and_pad.
        * Se contem, apenas adicione na lista. Caso contrário adicione uma lista.
        * para posteriormente adicionar itens nela.
         */
        if(sounds.get(chain_and_pad) == null){
            to_map.add(to_list);
            sounds.put(chain_and_pad, to_map);
        } else {
            sounds.get(chain_and_pad).add(to_list);
        }
    }
    /*
    * Use sempre use isto pois pode haver ExoPlayer e será necessário prepará-lo no final
     */
    public void prepare(){
        if(mExoPlayer != null){
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mExoPlayer.prepare();
                }
            });
        }
    }
    /*
    * @Param chain_and_pad é a junção de ChainSl(1 a 24)+padId(viewId())
    * @Param rpt quantos click já foi dada na mesma pad sequencialmente
     */
    public void playSound(String chain_and_pad){
        try {
            int rpt;
            if(sound_rpt.get(chain_and_pad) == null){
                sound_rpt.put(chain_and_pad, 0);
                rpt = 0;
            } else {
                rpt = sound_rpt.get(chain_and_pad)+1;
                if(rpt >= sounds.get(chain_and_pad).size()){
                    rpt = 0;
                }
                sound_rpt.put(chain_and_pad, rpt);
            }
            switch((String) sounds.get(chain_and_pad).get(rpt).get(TYPE)){
                case EXOPLAYER:
                    Log.e("Play with", "ExoPlayer");
                    mExoPlayer.setMediaItem((MediaItem) sounds.get(chain_and_pad).get(rpt).get(SOUND));
                    mExoPlayer.play();
                    break;
                case SOUNDPOOL:
                    Log.e("Play with", "SoundPool");
                    mSoundPool.play((Integer) sounds.get(chain_and_pad).get(rpt).get(SOUND), 1, 1, 1, 0, 1);
                    break;
            }
            String to_chain = (String) sounds.get(chain_and_pad).get(rpt).get(TO_CHAIN);
            if(to_chain != ""){
                XayUpFunctions.touchAndRelease(context, Integer.parseInt(VariaveisStaticas.chainsID[Integer.parseInt(to_chain)]) ,XayUpFunctions.TOUCH_AND_RELEASE);
            }
        } catch (NullPointerException e){
            Log.e("PlaySound() error", e.getMessage());
        }
    }
}
