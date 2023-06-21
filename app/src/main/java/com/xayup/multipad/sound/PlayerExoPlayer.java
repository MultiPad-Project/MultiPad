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

    /**
     * Cria uma instância do ExoPlayer.
     * @param context contexto atual
     * @param sound_path diretorio do arquivo de audio
     */
    public PlayerExoPlayer(Context context, String sound_path, String to_chain){
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
                }
            }
        });
        this.to_chain = (to_chain != null) ? Integer.parseInt(to_chain) : -1;
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
        pause();
        exo.seekTo(0);
        exo.play();
    }

    @Override
    public void pause() {
        exo.pause();
    }

    @Override
    public void stop() {
        exo.stop();
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
