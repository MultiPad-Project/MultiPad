package com.xayup.multipad.pads;
import android.view.View;

public interface PadInteraction {
    public View.OnTouchListener onPadClick(View pad, GridPads.PadGrid padGrid);
}
