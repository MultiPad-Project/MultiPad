package com.xayup.multipad.midi;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.midi.*;
import com.xayup.midi.manager.DevicesManager;
import com.xayup.multipad.midi.controller.ControllerManager;

public class MidiStaticVars {

    public static DevicesManager devicesManager = null;
    
    public static UsbManager manager = null;
    public static UsbDevice device = null;
    public static MidiManager midiManager = null;
    public static MidiDeviceInfo[] midiDeviceInfo = null;
	public static MidiDeviceInfo midiDevice = null;
    public static MidiInputPort midiInput = null;
    public static MidiOutputPort midiOutput = null;
    public static MidiReceiver midiOutputReceiver = null;
    public static MidiDeviceController midiDeviceController = null;
    public static int input_type = 1;
    public static String midiDevicePropertie = null;

    //Controller Mode
    public static ControllerManager controllerManager = null;
    
    //Finais
	public static final byte NOTE_ON = (byte) 0x90;
	public static final byte NOTE_OFF = (byte) 0x80;
    public static final int MIDI_INPUT = 0;
    public static final int MIDI_RECEIVER = 1;
    
    public static int CHANNEL = 1;
}
