package com.xayup.multipad;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiReceiver;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.IntRange;
import com.xayup.midi.controllers.LaunchpadMK2;
import com.xayup.midi.types.Devices;

import java.io.IOException;
import java.util.Arrays;

public class MidiDeviceController {
    protected Context context;
    protected MidiDevice device;
    protected Handler handler;
    protected Devices.MidiDevice midiDevice;
    protected MidiInputPort input;

    public MidiDeviceController(Context context, MidiDevice device, Devices.MidiDevice midiDevice){
        this.context = context;
        this.device = device;
        this.midiDevice = midiDevice;
        this.handler = new Handler(context.getMainLooper());
        this.input = device.openInputPort(midiDevice.input.getPortNumber());
        device.openOutputPort(midiDevice.output.getPortNumber()).onConnect(
            new MidiReceiver() {
                @Override
                public void onSend(byte[] data, int offset, int count, long time) throws IOException {
                    final int STATUS = data[offset] & 0xFF;
                    final int CHANNEL = STATUS & 0x0F;
                    final int MESSAGETYPE = (STATUS & 0xF0) >> 4;
                    final int NOTE = data[offset + 1] & 0xFF;
                    final int VELOCITY = data[offset + 2] & 0xFF;
                    if (NOTE < 128 && CHANNEL > 0) {
                        handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Log.v("MIDI Message", "Status: " + STATUS + " Channel: " + CHANNEL + " Message Type: " + MESSAGETYPE + " Note: " + NOTE + " " + Arrays.toString(LaunchpadMK2.configs.noteToXY.notToXY(NOTE)) + " Velocity: " + VELOCITY);
                                    try {
                                        int[] xy = midiDevice.config.noteToXY.notToXY(NOTE);
                                        if (xy[0] > -1 && xy[1] > -1) {
                                            final View pad =
                                                    PlayPads.grids
                                                            .get("grid_1").getPadView(xy[0], xy[1]);
                                            pad.dispatchTouchEvent(MotionEvent.obtain(0, 0, (MESSAGETYPE == 9 || VELOCITY > 0) ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP, 0, 0, 0));
                                        }
                                    } catch (NullPointerException n) {
                                        n.printStackTrace(System.out);
                                        //Toast.makeText(context, "Controller mode: " + CHANNEL +" "+rowProgramMode(NOTE, true)+" "+VELOCITY, 0).show();
                                    }
                                    handler.removeCallbacks(this);
                                }
                            }
                        );
                    }
                }
            }
        );
    }

    public void led(int row, int colum, @IntRange(from=0, to=127) int velocity) throws IOException {
        //Log.i("Input is null", String.valueOf(input == null));
        if(input != null && midiDevice.config.keymap != null && midiDevice.config.paletteChannel != null) {
            Object note = midiDevice.config.keymap[row][colum];
            if (note == null) return;
            boolean CC = false;
            if (note instanceof Devices.KeyID) {
                CC = ((Devices.KeyID) note).getType().equals("Control Change");
                note = ((Devices.KeyID) note).getId();
            }
            byte[] bytes = new byte[]{
                (byte) (((CC ? 0xB0 : (velocity == 0) ? 0x80 : 0x90) + (midiDevice.config.paletteChannel.get("classic") - 1)) & 0xFF),
                (byte) (((int) note) & 0xFF),
                (byte) (velocity & 0xFF)
            };
            Log.v("Data", "row: " + row + ", colum: " + colum + ", velocity: " + velocity);
            Log.v("Send MIDI", Arrays.toString(bytes));
            input.send(bytes, 0, bytes.length);
        }
    }
}
