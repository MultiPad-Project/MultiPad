package com.xayup.multipad.pads;

import com.xayup.multipad.pads.Render.MakePads;

public interface PadPressCallInterface {
    boolean call(MakePads.ChainInfo chain, MakePads.PadInfo pad);
}
