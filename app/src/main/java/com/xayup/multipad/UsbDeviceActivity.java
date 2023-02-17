package com.xayup.multipad;

import android.*;
import android.app.*;
import android.graphics.drawable.Drawable;
import android.hardware.usb.*;
import android.media.Image;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UsbDeviceActivity extends Activity {
	public final static int CONNECTED_USB = 1;
	public final static int DEVICES_USB = 2;

	public final String LP_X = "Launchpad X";
	public final String LP_OPEN = "Launchpad Open";
    public final String LP_PRO = "Launchpad Pro";
	public final String LP_PRO_MK3 = "Launchpad Pro Mk3";
	public final String LP_MK2 = "Launchpad Mk2";

	int activity = 0;

	//Dados para envio
	public Activity padsActivity = new PlayPads();

	@Override
	public void onCreate(Bundle instance) {
		super.onCreate(instance);
			setContentView(R.layout.connected_device);

			TextView c_device_name = findViewById(R.id.connecteddevice_launchpad_name);
			TextView c_device_vendor = findViewById(R.id.connecteddevice_vendor);
			ImageView c_device_image = findViewById(R.id.connecteddeviceImageView1);
			Button accept = findViewById(R.id.connecteddevice_accept);

            MidiStaticVars.device = (UsbDevice) this.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
			MidiStaticVars.midiManager = (MidiManager) getSystemService(Context.MIDI_SERVICE);
			MidiStaticVars.midiDeviceInfo = MidiStaticVars.midiManager.getDevices();

			if (MidiStaticVars.device != null) {
				c_device_name.setText("" + MidiStaticVars.device.getProductName());
				c_device_vendor.setText("" + MidiStaticVars.device.getManufacturerName());
				c_device_image.setImageDrawable(getMidiImagem(this, MidiStaticVars.device.getProductName()));
				for (MidiDeviceInfo midiInfo : MidiStaticVars.midiDeviceInfo) {
					if (midiInfo.getProperties().getString(MidiDeviceInfo.PROPERTY_PRODUCT)
							.equals(MidiStaticVars.device.getProductName())) {
						openMidiDevice(this, midiInfo);
						c_device_name.setText("" + MidiStaticVars.device.getProductName());
						c_device_vendor.setText("" + MidiStaticVars.device.getManufacturerName());
						c_device_image.setImageDrawable(getMidiImagem(this, MidiStaticVars.device.getProductName()));
						break;
					}
				}
			}
			accept.setOnClickListener(new Button.OnClickListener() {

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

	public void openMidiDevice(Context context, MidiDeviceInfo midi) {
		if (MidiStaticVars.midiDevice == midi) {
			Toast.makeText(context, midi.getProperties().getString(MidiDeviceInfo.PROPERTY_PRODUCT) + ": "
					+ context.getString(R.string.midi_aready_connected), Toast.LENGTH_SHORT).show();
		} else {
			MidiStaticVars.midiDevice = midi;
            MidiStaticVars.midiManager.openDevice(
                    midi,
                    new MidiManager.OnDeviceOpenedListener() {
                        @Override
                        public void onDeviceOpened(MidiDevice midiDevice) {
                            if (midiDevice != null) {

                                String propertie =
                                        midiDevice
                                                .getInfo()
                                                .getProperties()
                                                .getString(MidiDeviceInfo.PROPERTY_PRODUCT);
                                Toast.makeText(
                                                context,
                                                "Opened MIDI device: " + propertie,
                                                Toast.LENGTH_SHORT)
                                        .show();
                                try {
                                    int offset = 0;
                                    int numBytes = 0;
                                    byte[] bytes = new byte[32];
                                    int outPort = 0;
                                    switch (propertie) {
                                        case LP_X:
                                            // 240 0 32 41 2 12 0 127 247
                                            bytes[numBytes++] = (byte) 240;
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 32;
                                            bytes[numBytes++] = (byte) 41;
                                            bytes[numBytes++] = (byte) 2;
                                            bytes[numBytes++] = (byte) 12;
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 127;
                                            bytes[numBytes++] = (byte) 247;
                                            MidiStaticVars.midiInput = midiDevice.openInputPort(1);
                                            outPort = 1;
                                            MidiStaticVars.midiInput.onSend(
                                                    bytes, offset, numBytes, 0);
                                            break;
                                        case LP_PRO: /*
                                                     bytes[numBytes++] = (byte) 240;
                                                     bytes[numBytes++] = (byte) 0;
                                                     bytes[numBytes++] = (byte) 32;
                                                     bytes[numBytes++] = (byte) 41;
                                                     bytes[numBytes++] = (byte) 2;
                                                     bytes[numBytes++] = (byte) 16;
                                                     bytes[numBytes++] = (byte) 44;
                                                     bytes[numBytes++] = (byte) 0x03;
                                                     bytes[numBytes++] = (byte) 247; */
                                            Toast.makeText(
                                                            context,
                                                            context.getString(
                                                                    R.string.midi_lp_pro_live),
                                                            Toast.LENGTH_LONG)
                                                    .show();
                                            MidiStaticVars.midiInput = midiDevice.openInputPort(1);
                                            outPort = 1;
                                            MidiStaticVars.midiInput.onSend(
                                                    bytes, offset, numBytes, 0);
                                            break;
                                        case LP_OPEN:
                                            bytes[numBytes++] = (byte) 240;
                                            bytes[numBytes++] = (byte) 0;
                                            bytes[numBytes++] = (byte) 32;
                                            bytes[numBytes++] = (byte) 41;
                                            bytes[numBytes++] = (byte) 2;
                                            bytes[numBytes++] = (byte) 16;
                                            bytes[numBytes++] = (byte) 44;
                                            bytes[numBytes++] = (byte) 0x03;
                                            bytes[numBytes++] = (byte) 247;
                                            MidiStaticVars.midiInput = midiDevice.openInputPort(1);
                                            outPort = 1;
                                            MidiStaticVars.midiInput.onSend(
                                                    bytes, offset, numBytes, 0);
                                            break;/*
                                        case LP_PRO_MK3:
                                            break;
                                        case LP_MK2:
                                            break;*/
                                        default:
                                            if (midiDevice.getInfo().getInputPortCount() > 0)
                                                MidiStaticVars.midiInput =
                                                        midiDevice.openInputPort(
                                                                midiDevice
                                                                                .getInfo()
                                                                                .getInputPortCount()
                                                                        - 1);
                                            outPort = midiDevice.getInfo().getOutputPortCount() - 1;
                                            break;
                                    }

                                    if (midiDevice.getInfo().getOutputPortCount() > 0) {
                                        MidiStaticVars.midiOutput =
                                                midiDevice.openOutputPort(outPort);
                                        MidiStaticVars.midiOutput.connect(
                                                new MidiOutputReceiver(context));
                                    }
                                } catch (IOException e) {
                                }
                            }
                        }
                    },
                    new Handler(Looper.getMainLooper()));
		}
	}

	public static int rowProgramMode(int padid) {
		int d = Integer.parseInt((padid + "").substring(0, 1));
		int u;
		try {
			u = Integer.parseInt((padid + "").substring(1, 2));
		} catch (StringIndexOutOfBoundsException e) {
			u = d;
			d = 0;
		}
		return ((8 + (d * (-1) + 1)) * 10) + u;
	}

	class MidiOutputReceiver extends MidiReceiver {
		Activity context;

		public MidiOutputReceiver(Context context) {
			this.context = (Activity) context;
		}

		public void onSend(byte[] data, int offset, int count, long timestamp) throws IOException {
			final int channel = (data[0] & 0xFF);
			final int Note = (data[1] & 0xFF);
			final int buttom = (data[2] & 0xFF);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (((Note > 128 && Note <= 144) | (Note >= 176 & Note <= 178)) && (data[3] & 0xFF) > 0) {
							padsActivity.findViewById(rowProgramMode(buttom))
									.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
							//Toast.makeText(padsActivity, "Note On: " + rowProgramMode(buttom), Toast.LENGTH_SHORT).show();
						} else {
							padsActivity.findViewById(rowProgramMode(buttom))
									.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
							//	Toast.makeText(padsActivity, "Note Off: " + rowProgramMode(buttom), Toast.LENGTH_SHORT).show();
						}
					} catch (NullPointerException n) {
					}
				}
			});

		}
	}
}
