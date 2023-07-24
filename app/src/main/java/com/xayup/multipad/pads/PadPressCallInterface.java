package com.xayup.multipad.pads;

import com.xayup.multipad.pads.Render.MakePads;

public interface PadPressCallInterface {
    boolean call(GridPadsReceptor.PadGrid padGrid, MakePads.PadInfo padInfo);
}
