package com.xayup.multipad.midi;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.IntRange;
import com.xayup.midi.manager.thread.DataReceiverThread;
import com.xayup.midi.types.Devices;
import com.xayup.multipad.PlayPads;

import java.io.IOException;
import java.util.Arrays;

public class MidiDeviceController {
    //protected Context context;
    protected UsbDeviceConnection device;
    protected Handler handler;
    public final Devices.MidiDevice midiDevice;
    public final DataReceiverThread dataReceiverThread;

    public MidiDeviceController(Context context, UsbDeviceConnection device, Devices.MidiDevice midiDevice){
        //this.context = context;
        this.device = device;
        this.midiDevice = midiDevice;
        this.handler = new Handler(context.getMainLooper());
        //this.input = device.openInputPort(midiDevice.input.getPortNumber());
        Log.v("Claim", String.valueOf(device.claimInterface(midiDevice.usbInterface, true)));
        this.dataReceiverThread = new DataReceiverThread(context, device, midiDevice.input) {
            @Override
            public void dataReceived(byte[] data) {
                int offset = 0;
                final int STATUS = data[offset] & 0xFF;
                final int CHANNEL = STATUS & 0x0F;
                final int MESSAGETYPE = (STATUS & 0xF0) >> 4;
                final int NOTE = data[offset + 1] & 0xFF;
                final int VELOCITY = data[offset + 2] & 0xFF;
                Log.v("MIDI Message", "Status: " + STATUS + " Channel: " + CHANNEL + " Message Type: " + MESSAGETYPE + " Note: " + NOTE + " Velocity: " + VELOCITY + " " + Arrays.toString(midiDevice.config.noteToXY.notToXY(VELOCITY)));
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "MIDI Message:" + "Status: " + STATUS + " Channel: " + CHANNEL + " Message Type: " + MESSAGETYPE + " Note: " + NOTE + " Velocity: " + VELOCITY + " " + Arrays.toString(midiDevice.config.noteToXY.notToXY(VELOCITY)), Toast.LENGTH_SHORT).show();
                        handler.removeCallbacks(this);
                    }
                });
                if (VELOCITY < 128) {
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Log.v("MIDI Message: Handler: ", "Status: " + STATUS + " Channel: " + CHANNEL + " Message Type: " + MESSAGETYPE + " Note: " + NOTE + " Velocity: " + VELOCITY + " " + Arrays.toString(midiDevice.config.noteToXY.notToXY(VELOCITY)));
                                    try {
                                        int[] xy = midiDevice.config.noteToXY.notToXY(VELOCITY);
                                        if (xy[0] > -1 && xy[1] > -1) {
                                            final View pad = PlayPads.grids.getPadView(xy[0], xy[1]);
                                            pad.dispatchTouchEvent(MotionEvent.obtain(0, 0, (STATUS != 8 && VELOCITY > 0) ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP, 0, 0, 0));
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
        };
    }

    public void led(int row, int colum, @IntRange(from=0, to=127) int velocity) throws IOException {
        Log.v("app to device message", "Output: " + midiDevice.output + ", KeyMap: " + midiDevice.config.keymap + ", Palette Channel " + midiDevice.config.paletteChannel);
        if(midiDevice.output != null && midiDevice.config.keymap != null && midiDevice.config.paletteChannel != null) {
            Object note = midiDevice.config.keymap[row][colum];
            if (note == null) return;
            boolean CC = false;
            if (note instanceof Devices.KeyID) {
                CC = ((Devices.KeyID) note).getType().equals("Control Change");
                note = ((Devices.KeyID) note).getId();
            }
            int status = (CC ? 0xB0 : (velocity == 0) ? 0x80 : 0x90);
            byte[] bytes = new byte[]{
                (byte) ((status >> 4) & 0xFF),
                (byte) ((status + (midiDevice.config.paletteChannel.get("classic") - 1)) & 0xFF),
                (byte) (((int) note) & 0xFF),
                (byte) (velocity & 0xFF)
            };
            Log.v("Data", "row: " + row + ", colum: " + colum + ", velocity: " + velocity);
            Log.v("Send MIDI", Arrays.toString(bytes));
            Log.v("Status", String.valueOf(device.bulkTransfer(midiDevice.output, bytes, 0, bytes.length, 0)));
        }
    }
}
