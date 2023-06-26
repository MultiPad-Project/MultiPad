package com.xayup.multipad.pads;

import java.util.ArrayList;
import java.util.List;

public class PadPressCall implements  PadPressCallInterface {
    public List<PadPressCallInterface> calls;
    protected int sucess = 0;

    public PadPressCall() {
        calls = new ArrayList<>();
    }

    public int getLastSucessCount() {
        return sucess;
    }

    @Override
    public boolean call(int chain, int x, int y) {
        sucess = 0;
        for (PadPressCallInterface mCall : calls) sucess += (mCall.call(chain, x, y)) ? 1 : 0;
        return true;
    }
}
