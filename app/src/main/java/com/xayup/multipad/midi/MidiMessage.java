package com.xayup.multipad.midi;

import android.util.Log;
import com.xayup.multipad.UsbDeviceActivity;

import java.io.IOException;

public class MidiMessage {
    int numBytes = 0;
    final byte[] bytes = new byte[3];
    int length = bytes.length;
    final int offset = 0;
    
    public MidiMessage(){}
    
    public void send(int type, int padid, int channel, byte note, int velocity) {
        numBytes = 0;
        bytes[numBytes++] = (byte) (note + (channel - 1));
        bytes[numBytes++] = (byte) UsbDeviceActivity.rowProgramMode(padid, false);
        bytes[numBytes++] = (byte) velocity;
        sendInput();
    }

    protected void sendInput() {
        try {
            MidiStaticVars.midiInput.send(bytes, offset, length);
        } catch (IOException i) {
            Log.d("sendInput()", i.getStackTrace()+"");
        }
    }

    protected void sendReceiver() {
        try{
            MidiStaticVars.midiOutputReceiver.send(bytes, offset, numBytes);
       } catch (IOException i){
          Log.d("sendReceiver()", i.getStackTrace()+"");
            }
    }
}
