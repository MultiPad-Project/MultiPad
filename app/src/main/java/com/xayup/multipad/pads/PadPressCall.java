package com.xayup.multipad.pads;

import com.xayup.multipad.pads.Render.MakePads;

import java.util.ArrayList;
import java.util.List;

public class PadPressCall implements  PadPressCallInterface {
    public List<PadPressCallInterface> calls;
    protected int success = 0;

    public PadPressCall() {
        calls = new ArrayList<>();
    }

    public int getLastSuccessCount() {
        return success;
    }

    @Override
    public boolean call(MakePads.ChainInfo chain, MakePads.PadInfo pad) {
        success = 0;
        for (PadPressCallInterface mCall : calls) success += (mCall != null && mCall.call(chain, pad)) ? 1 : 0;
        return success > 0;
    }
}
