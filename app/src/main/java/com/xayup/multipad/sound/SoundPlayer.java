package com.xayup.multipad.sound;

public interface SoundPlayer {
    void play();
    int getToChain();
    void pause();
    void stop();
    void prepare();
    void release();
}
