package com.xayup.multipad.sound;

public interface SoundPlayer {
    void play();
    int getToChain();
    void pause();
    void stop();
    void prepare();
    void release();
    int currentTime();
    int restTime();
    int getDuration();
    boolean appendAfterFinish(Runnable after_finish);
}
