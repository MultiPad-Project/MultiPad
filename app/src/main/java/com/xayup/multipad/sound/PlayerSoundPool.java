package com.xayup.multipad.sound;

import android.content.Context;
import android.media.SoundPool;
import android.os.Handler;
import android.os.SystemClock;
import com.xayup.debug.XLog;

import java.util.List;

public abstract class PlayerSoundPool implements SoundPlayer {
    protected int STATE = 0; //0 - Stoped; 1 - Playing; 2 - Paused

    protected int to_chain;
    protected int sound_id;
    protected int streamID;
    protected boolean playing;
    protected SoundPool pool;
    protected Runnable runnable;
    protected int currentTime;
    protected long startedTime;
    protected int sampleDuration;
    protected Handler mHandler;
    protected List<Runnable> after_finish;

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
            currentTime = 0;
            STATE = 0;
            onFinished(this);
            while(!after_finish.isEmpty()){
                after_finish.remove(0).run();
            }
        };
    }

    public int getSampleDuration(){
        return sampleDuration;
    }

    public boolean isPlaying(){
        return STATE == 1;
    }
    public boolean isPaused(){
        return STATE == 2;
    }

    public boolean appendAfterFinish(Runnable run_this){
        if(run_this == null) return false;
        after_finish.add(run_this);
        return true;
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
        startedTime = SystemClock.uptimeMillis();
        STATE = 1;
    }

    @Override
    public void pause() {
        mHandler.removeCallbacks(runnable);
        pool.pause(streamID);
        currentTime = (int) (startedTime - SystemClock.uptimeMillis());
        STATE = 2;
    }

    @Override
    public void stop() {
        mHandler.removeCallbacks(runnable);
        pool.stop(streamID);
        currentTime = 0;
        STATE = 0;
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

    @Override
    public int currentTime(){
        return (currentTime = (int) (SystemClock.uptimeMillis() - startedTime)) > sampleDuration ? sampleDuration : currentTime;
    }

    /**
     * Quando a sample chega ao final
     */
    public abstract void onFinished(SoundPlayer player);

}
