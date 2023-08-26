package com.xayup.multipad;

import android.*;
import android.app.*;
import android.graphics.drawable.Drawable;
import android.hardware.usb.*;
import android.media.Image;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceService;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.content.*;
import com.xayup.multipad.MidiStaticVars;
import com.xayup.multipad.PlayPads;
import com.xayup.multipad.VariaveisStaticas;
import com.xayup.multipad.MidiMessage;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class UsbDeviceActivity extends Activity {
    public static final int CONNECTED_USB = 1;
    public static final int DEVICES_USB = 2;

    public static final String LP_X = "Launchpad X";
    public static final String LP_OPEN = "Launchpad Open";
    public static final String LP_PRO = "Launchpad Pro";
    public static final String LP_PRO_MK3 = "Launchpad Pro Mk3";
    public static final String LP_MK2 = "Launchpad Mk2";

    // Dados para envio
    public Activity padsActivity = new PlayPads();

    // Dados recebidos
    public List<int[]> received;

    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.connected_device);

        TextView c_device_name = findViewById(R.id.connecteddevice_launchpad_name);
        TextView c_device_vendor = findViewById(R.id.connecteddevice_vendor);
        ImageView c_device_image = findViewById(R.id.connecteddeviceImageView1);
        Button accept = findViewById(R.id.connecteddevice_accept);

        MidiStaticVars.device =
                (UsbDevice) this.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        MidiStaticVars.midiManager = (MidiManager) getSystemService(Context.MIDI_SERVICE);
        MidiStaticVars.midiDeviceInfo = MidiStaticVars.midiManager.getDevices();

        if (MidiStaticVars.device != null) {
            c_device_name.setText("" + MidiStaticVars.device.getProductName());
            c_device_vendor.setText("" + MidiStaticVars.device.getManufacturerName());
            c_device_image.setImageDrawable(
                    getMidiImagem(this, MidiStaticVars.device.getProductName()));
            for (MidiDeviceInfo midiInfo : MidiStaticVars.midiDeviceInfo) {
                if (midiInfo.getProperties()
                        .getString(MidiDeviceInfo.PROPERTY_PRODUCT)
                        .equals(MidiStaticVars.device.getProductName())) {
                    openMidiDevice(this, midiInfo);
                    c_device_name.setText("" + MidiStaticVars.device.getProductName());
                    c_device_vendor.setText("" + MidiStaticVars.device.getManufacturerName());
                    c_device_image.setImageDrawable(
                            getMidiImagem(this, MidiStaticVars.device.getProductName()));
                    break;
                }
            }
        }
        accept.setOnClickListener(
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View p1) {
                        finishAffinity();
                        startActivity(new Intent(getBaseContext(), MainActivity.class));
                    }
                });
    }

    public MidiDeviceInfo[] getMidiListDevices(Context context) {
        MidiStaticVars.midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        MidiStaticVars.midiDeviceInfo = MidiStaticVars.midiManager.getDevices();
        return MidiStaticVars.midiDeviceInfo;
    }

    public Drawable getMidiImagem(Context context, String product) {
        switch (product) {
            case LP_X:
                return context.getDrawable(R.drawable.lp_x);
            case LP_PRO:
                return context.getDrawable(R.drawable.lp_pro);
            case LP_OPEN:
                return context.getDrawable(R.drawable.lp_pro);
            case LP_PRO_MK3:
                return context.getDrawable(R.drawable.lp_pro_mk3);
            case LP_MK2:
                return context.getDrawable(R.drawable.lp_mk2);
            default:
                return context.getDrawable(R.drawable.desconhecido);
        }
    }

    public void openMidiDevice(Context context, final MidiDeviceInfo midi) {
        MidiStaticVars.midiDevice = midi;
        MidiStaticVars.midiManager.openDevice(
                midi,
                new MidiManager.OnDeviceOpenedListener() {
                    @Override
                    public void onDeviceOpened(MidiDevice midiDevice) {
                        if (midiDevice != null) {
                            MidiStaticVars.midiDevicePropertie =
                                    midiDevice
                                            .getInfo()
                                            .getProperties()
                                            .getString(MidiDeviceInfo.PROPERTY_PRODUCT);
                            try {
                                int offset = 0;
                                int numBytes = 0;
                                byte[] bytes = new byte[32];
                                int outPort = 0;
                                int inPort = 0;
                                switch (MidiStaticVars.midiDevicePropertie) {
                                    case LP_X:
                                        {
                                            // 240 0 32 41 2 12 0 127 247
                                            bytes[numBytes++] = (byte) 240;
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 32;
                                            bytes[numBytes++] = (byte) 41;
                                            bytes[numBytes++] = (byte) 2;
                                            bytes[numBytes++] = (byte) 12;

                                            bytes[numBytes++] = (byte) 14;
                                            bytes[numBytes++] = (byte) 1; // Programmer Mode

                                            bytes[numBytes++] = (byte) 247;
                                            inPort = 0;
                                            outPort = 0;
                                            break;
                                        }
                                    case LP_PRO:
                                        {
                                            bytes[numBytes++] = (byte) 240;
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 32;
                                            bytes[numBytes++] = (byte) 41;
                                            bytes[numBytes++] = (byte) 2;
                                            bytes[numBytes++] = (byte) 16;
                                            bytes[numBytes++] = (byte) 44;
                                            bytes[numBytes++] = (byte) 0x03; // Layout
                                            bytes[numBytes++] = (byte) 247;
                                            inPort = 0;
                                            outPort = 0;
                                            break;
                                        }
                                    case LP_OPEN:
                                        {
                                            bytes[numBytes++] = (byte) 240;
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 32;
                                            bytes[numBytes++] = (byte) 41;
                                            bytes[numBytes++] = (byte) 2;
                                            bytes[numBytes++] = (byte) 16;
                                            bytes[numBytes++] = (byte) 44;
                                            bytes[numBytes++] = (byte) 0x03; // Layout
                                            bytes[numBytes++] = (byte) 247;
                                            inPort = 0;
                                            outPort = 0;
                                            break;
                                        }
                                    case LP_PRO_MK3:
                                        {
                                            bytes[numBytes++] = (byte) 240;
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 32;
                                            bytes[numBytes++] = (byte) 41;
                                            bytes[numBytes++] = (byte) 2;
                                            bytes[numBytes++] = (byte) 14;
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 17; // Layout
                                            bytes[numBytes++] = (byte) 0; // Page
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 247;
                                            inPort = 0;
                                            outPort = 0;
                                            break;
                                        }
                                    case LP_MK2:
                                        {
                                            bytes[numBytes++] = (byte) 240;
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 32;
                                            bytes[numBytes++] = (byte) 41;
                                            bytes[numBytes++] = (byte) 2;
                                            bytes[numBytes++] = (byte) 24;
                                            bytes[numBytes++] = (byte) 34;
                                            bytes[numBytes++] = (byte) 0; // Layout
                                            bytes[numBytes++] = (byte) 247;
                                            inPort = 0;
                                            outPort = 0;
                                            break;
                                        }
                                    default:
                                        {
                                            inPort = midiDevice.getInfo().getInputPortCount() - 1;
                                            outPort = midiDevice.getInfo().getOutputPortCount() - 1;
                                            break;
                                        }
                                }

                                if (midi.getInputPortCount() > 0) {
                                    MidiStaticVars.midiInput = midiDevice.openInputPort(inPort);
                                    if (MidiStaticVars.midiInput != null) {
                                        MidiStaticVars.midiMessage = new MidiMessage();
                                        if (MidiStaticVars.device == null) {
                                            MidiStaticVars.midiOutputReceiver =
                                                    new MidiControlerMode(context);
                                        } else {
                                            MidiStaticVars.midiOutputReceiver =
                                                    new MidiOutputReceiver(context);
                                        }
                                    }
                                }
                                if (midi.getOutputPortCount() > 0) {
                                    MidiStaticVars.midiOutput = midiDevice.openOutputPort(outPort);
                                    if (MidiStaticVars.midiOutput != null) {

                                        MidiStaticVars.midiOutput.onConnect(
                                                MidiStaticVars.midiOutputReceiver);
                                    }
                                }
                                Toast.makeText(
                                                context,
                                                "Opened MIDI device: "
                                                        + MidiStaticVars.midiDevicePropertie,
                                                Toast.LENGTH_SHORT)
                                        .show();
                            } catch (NullPointerException n) {
                            }
                        }
                    }
                },
                new Handler(Looper.getMainLooper()));
    }

    public static int rowProgramMode(final int padid, boolean from_midi) {
        int x = Integer.parseInt((padid + "").substring(0, 1));
        int y;
        try {
            y = Integer.parseInt((padid + "").substring(1, 2));
        } catch (StringIndexOutOfBoundsException e) {
            y = x;
            x = 0;
        }
        switch (MidiStaticVars
                .midiDevicePropertie) { // Cada launchpad tem um layout diferente entao o tratamento
                // e
                // diferente
            case LP_PRO_MK3:
            case LP_OPEN:
            case LP_PRO:
            case LP_X:
                {
                    return ((8 + (x * (-1) + 1)) * 10) + y;
                }
            case LP_MK2:
                {
                    return (16 * (y - 1)) + x - 1;
                }
            default:
                {
                    return VariaveisStaticas.getDrumFromVelocity(padid, from_midi);
                }
        }
    }

    class MidiControlerMode extends MidiReceiver {
        Activity context;

        public MidiControlerMode(Context context) {
            this.context = (Activity) context;
            received = new ArrayList<>();
        }

        @Override
        public void onSend(byte[] data, int arg1, int arg2, long arg3) throws IOException {
            final int CHANNEL = data[1] & 0x0F;
            final int NOTE = data[2] & 0xFF;
            final int VELOCITY = data[3] & 0xFF;
            if (NOTE >= 0 && NOTE < 128 && VELOCITY >= 0 && VELOCITY < 128) {
                // received.add(
                //        new int[] {CHANNEL, NOTE, VELOCITY,
                // PlayPads.grids.get("grid_1").getId()});
                // if (receiver_thread.isStoped()) receiver_thread.start();
                runOnUiThread(
                        () -> {
                            try {
                                final ImageView led =
                                        PlayPads.grids
                                                .get("grid_1")
                                                .findViewById(rowProgramMode(NOTE, true))
                                                .findViewById(R.id.led);
                                final int color =
                                        VariaveisStaticas.colorInt(
                                                PlayPads.currentChainMC,
                                                VELOCITY,
                                                PlayPads.custom_color_table,
                                                PlayPads.oldColors);
                                led.setBackgroundColor(color);
                            } catch (NullPointerException n) {
                                Toast.makeText(context, "Controller mode: " + CHANNEL +" "+rowProgramMode(NOTE, true)+" "+VELOCITY, 0)
                                        .show();
                            }
                        });
            }
        }
    }

    class MidiOutputReceiver extends MidiReceiver {
        Activity context;

        public MidiOutputReceiver(Context context) {
            this.context = (Activity) context;
        }

        public void onSend(byte[] data, int offset, int count, long timestamp) throws IOException {
            final int channel = (data[0] & 0xFF);
            final int note = (data[1] & 0xFF);
            final int button = (data[2] & 0xFF);
            runOnUiThread(() -> Toast.makeText(context,
                    "Channel: " + channel +
                    ", Note: " + note +
                    ", Button: " + button, 0).show());
            if (note >= 0x80) {
                final int ACTION =
                        (note < 0x90 || (data[3] & 0xFF) == 0)
                                ? MotionEvent.ACTION_UP
                                : MotionEvent.ACTION_DOWN;
                runOnUiThread(
                        () -> {
                            try {
                                PlayPads.grids
                                        .get("grid_1")
                                        .findViewById(rowProgramMode(button, true))
                                        .dispatchTouchEvent(
                                                MotionEvent.obtain(0, 0, ACTION, 0, 0, 0));
                            } catch (NullPointerException n) {
                                Toast.makeText(context, n.toString(), 0).show();
                            }
                        });
                /*
                if (MidiStaticVars.device != null) {
                    if (((note > 128 && note <= 144) | (note >= 176 & note <= 178))
                            && (data[3] & 0xFF) > 0) {
                        runOnUiThread(
                                () -> {
                                    try {
                                        PlayPads.grids
                                                .get("grid_1")
                                                .findViewById(rowProgramMode(button, true))
                                                .dispatchTouchEvent(
                                                        MotionEvent.obtain(0, 0, ACTION, 0, 0, 0));
                                    } catch (NullPointerException n) {
                                        Toast.makeText(context, n.toString(), 0).show();
                                    }
                                });
                    }
                }

                 */
            }
        }
    }
}
