package com.xayup.multipad;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;

public class MidiStaticVars {
    
    public static UsbManager manager = null;
    public static UsbDevice device = null;
    public static MidiManager midiManager = null;
    public static MidiDeviceInfo[] midiDeviceInfo = null;
	public static MidiDeviceInfo midiDevice = null;
    public static MidiInputPort midiInput = null;
    public static MidiOutputPort midiOutput = null;
	public static byte NOTE_ON = (byte) 0x90;
	public static byte NOTE_OFF = (byte) 0x80;

	//Dados para envio
	public static int CHANNEL = 1;
    
}
