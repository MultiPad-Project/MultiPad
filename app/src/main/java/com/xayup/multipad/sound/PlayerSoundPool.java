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
     * Load sample with SoundPool (with "onFinish()" support).
     * @param context application context.
     * @param pool an instance of SoundPool as it already supports simultaneous audios, so don't use 'new SoundPool..' directly.
     * @param sound_path the location of the sound files to be uploaded.
     * @param duration Sample duration.
     */
    public PlayerSoundPool(Context context, SoundPool pool, String sound_path, int duration, String to_chain){
        this.pool = pool;
        this.sound_id = pool.load(sound_path, 1);
        this.to_chain = (to_chain != null) ? Integer.parseInt(to_chain) : -1;
        this.mHandler = new Handler(context.getMainLooper());
        this.sampleDuration = duration;
        XLog.v("Sound Durations Media Player", String.valueOf(sampleDuration));
        /*For Handler*/
        this.runnable = ()->{
            XLog.v("Try call", "onFinish()");
            playing = false;
            onFinished(this);
        };
    }

    /**
     * @return returns the chain (MC) of this sample, otherwise -1 if there is no
     */
    @Override
    public int getToChain(){
        return to_chain;
    }

    @Override
    public void play() {
        mHandler.postDelayed(runnable, sampleDuration);
        this.streamID = pool.play(sound_id, 1f, 1f, 1, 0, 1f);
    }

    @Override
    public void pause() {
        mHandler.removeCallbacks(runnable);
        pool.pause(streamID);
    }

    @Override
    public void stop() {
        mHandler.removeCallbacks(runnable);
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
        mHandler.removeCallbacks(runnable);
    }

    /**
     * Quando a sample chega ao final
     */
    public abstract void onFinished(SoundPlayer player);

}
