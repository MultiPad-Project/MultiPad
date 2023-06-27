package com.xayup.multipad.pads;

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
    public boolean call(int chain, int x, int y) {
        success = 0;
        for (PadPressCallInterface mCall : calls) success += (mCall != null && mCall.call(chain, x, y)) ? 1 : 0;
        return success > 0;
    }
}
