package com.xayup.multipad.sound;

import android.app.Activity;
import android.content.Context;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;

public abstract class PlayerExoPlayer implements SoundPlayer{
    protected ExoPlayer exo;
    protected int to_chain;
    protected SoundPlayer this_class;

    /**
     * Cria uma instância do ExoPlayer.
     * @param context contexto atual
     * @param sound_path diretorio do arquivo de audio
     */
    public PlayerExoPlayer(Context context, String sound_path, String to_chain){
        this.this_class = this;
        this.exo = new ExoPlayer.Builder(context).build();
        ((Activity) context).runOnUiThread(()-> {
            this.exo.setMediaItem(new MediaItem.Builder().setUri(sound_path).build());
            exo.prepare();
        });
        this.exo.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if(playbackState == Player.STATE_ENDED){
                    onFinished(this_class);
                }
            }
        });
        this.to_chain = (to_chain != null) ? Integer.parseInt(to_chain) : -1;
    }

    @Override
    public boolean appendAfterFinish(Runnable after_finish){
        return false;
    }

    @Override
    public int currentTime(){
        return (int) exo.getCurrentPosition();
    }
    @Override
    public int restTime(){
        return  (int) exo.getDuration() - currentTime();
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
        exo.seekTo(0);
        exo.play();
    }

    @Override
    public void pause() {
        exo.pause();
    }

    @Override
    public void stop() {
        pause();
    }

    @Override
    public void prepare() {
        exo.prepare();
    }

    @Override
    public void release() {
        exo.release();
    }

    public abstract void onFinished(SoundPlayer player);
}
