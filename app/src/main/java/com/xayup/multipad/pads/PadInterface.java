package com.xayup.multipad.pads;

import android.view.View;

public abstract interface PadInterface {
    public View.OnTouchListener onPadTouch();

    public View.OnTouchListener onChainTouch();

    public View.OnTouchListener onPressWatermarkTouch();

    public View.OnTouchListener onLedSwitchTouch();

    public View.OnTouchListener onAutoplaySwitchTouch();

    public View.OnTouchListener onAutoplayPrevTouch();

    public View.OnTouchListener onAutoplayPauseTouch();

    public View.OnTouchListener onAutoplayNextTouch();

    public View.OnTouchListener onLayoutSwitchTouch();

    public View.OnTouchListener onWatermarkTouch();
}
