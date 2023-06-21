package com.xayup.multipad.sound;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import com.xayup.debug.XLog;

public abstract class PlayerSoundPool implements SoundPlayer {
    protected int to_chain;
    protected int sound_id;
    protected int streamID;
    protected boolean playing;
    protected SoundPool pool;
    protected Runnable runnable;
    protected int currentTime;
    protected int sampleDuration;
    protected Handler mHandler;

    /**
     * Cria um no ID SoundPool
     * @param pool uma instância do SoundPool pois já suporta audios simultaneos, então não use 'new SoundPool..' diretamente
     * @param sound_path o local do arquivos de som para ser carregado
     */
    public PlayerSoundPool(Context context, SoundPool pool, String sound_path, String to_chain){
        this.pool = pool;
        this.sound_id = pool.load(sound_path, 1);
        this.to_chain = (to_chain != null) ? Integer.parseInt(to_chain) : -1;
        this.mHandler = new Handler(context.getMainLooper());

        /*Get Sample Duration*/
        MediaPlayer tmp = MediaPlayer.create(context, Uri.parse(sound_path));
        this.sampleDuration = tmp.getDuration();
        tmp.release();

        /*For Handler*/
        this.runnable = ()->{
            if(playing){
                XLog.v("Try call", "onFinish()");
                playing = false;
                onFinished(this);
            }
        };

    }


    /**
     * @return retorna a chain (MC) desta sample, do contrário -1 se não houver
     */
    @Override
    public int getToChain(){
        return to_chain;
    }

    @Override
    public void play() {
        mHandler.postDelayed(runnable, SystemClock.uptimeMillis()+sampleDuration);
        this.streamID = pool.play(sound_id, 1f, 1f, 1, 0, 1f);
    }

    @Override
    public void pause() {
        mHandler.removeCallbacks(runnable);
        pool.pause(streamID);
    }

    @Override
    public void stop() {
        pool.stop(streamID);
    }

    @Override
    public void prepare() {
    }

    /**
     * Please Release Original SoundPool instance
     */
    @Override
    public void release() {
    }

    /**
     * Quando a sample chega ao final
     */
    public abstract void onFinished(SoundPlayer player);

}
