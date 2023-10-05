package com.xayup.midi;

import android.content.Context;
import android.content.Intent;
import android.media.midi.*;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import com.xayup.midi.controllers.Index;
import com.xayup.midi.tmppackage.KeyInteraction;
import com.xayup.midi.types.Color;
import com.xayup.midi.types.Color.ColorType;
import com.xayup.midi.types.Devices;
import com.xayup.midi.types.Devices.KeyID;
import com.xayup.midi.types.Devices.KeyType;
import com.xayup.midi.types.Devices.GridDeviceConfig;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class created based on <a href="https://github.com/Project-Amethyst/amethyst-player/tree/code/src/hardware">this</a> package
 * Thanks <a href="https://github.com/203Null">@203null</a>
 */
public class Hardware {
    int id;

    @Nullable String name;
    @Nullable MidiInputPort activeInput;
    @Nullable MidiOutputPort activeOutput;
    @Nullable
    GridDeviceConfig activeConfig;

    KeyInteraction keyPress;
    KeyInteraction keyRelease;

    public static MidiManager midiManager;

    public Hardware(int id, KeyInteraction keyPress, KeyInteraction keyRelease)
    {
        this.id = id;
        this.keyPress = keyPress;
        this.keyRelease = keyRelease;

        //// REPLACE THIS IN ANOTHER CLASS (IF NECCESSARY) ////
        start(/*Replace context*/null, (event, value) -> {
            Log.i("Midi Device Event", event);
            switch (event){
                case GridController.Events.EVENT_OPENED:{
                    break;
                }
                case GridController.Events.EVENT_CLOSED:{
                    break;
                }
                case GridController.Events.EVENT_CONNECTED:{
                    break;
                }
                case GridController.Events.EVENT_DISCONNECTED:{
                    break;
                }
            }
        });
    }

    public static boolean start(Context context, GridController.CallbackEvents connection_event) //: Promise<boolean>
    {
        try
        {
            if(midiManager == null)
            {
                context.startService(new Intent(Context.MIDI_SERVICE));
                midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
                midiManager.registerDeviceCallback(new MidiManager.DeviceCallback(){
                    @Override
                    public void onDeviceStatusChanged(MidiDeviceStatus status) {
                        super.onDeviceStatusChanged(status);
                        GridController.updateDeviceList(true, true);
                    }
                }, null);
                GridController.updateDeviceList(true, false);
            }
            GridController.connection_event = connection_event;
            return true;
        }
        catch(Exception e)
        {
            Log.e("Device Connection", e.toString());
            return false;
        }
    }

    public static class GridController {

        public static Map<String, Devices.MidiDevice> deviceList = new HashMap<>();

        @StringDef({Events.EVENT_CLOSED, Events.EVENT_OPENED, Events.EVENT_DISCONNECTED, Events.EVENT_CONNECTED})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Events {
            String EVENT_CLOSED = "Closed";
            String EVENT_OPENED = "Opened";
            String EVENT_DISCONNECTED = "Disconnected";
            String EVENT_CONNECTED = "Connected";
        }

        public interface CallbackEvents {
            void event(@Events String event, Object value);
        }

        private static CallbackEvents connection_event;

        /**
         * @param strict_mode default true
         * @param notify_event default false
         * @return Map with MidiDevice's
         */
        public static Map<String, Devices.MidiDevice> updateDeviceList(boolean strict_mode, boolean notify_event)
        {
            Map<String, Devices.MidiDevice> devices = new HashMap<>();

            for(MidiDeviceInfo device : midiManager.getDevices())
                if(device.getInputPortCount() >= 1 && device.getOutputPortCount() >= 1)
                    for(MidiDeviceInfo.PortInfo portInfoInput : device.getPorts())
                        if(portInfoInput.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT)
                            for(MidiDeviceInfo.PortInfo portInfoOutput : device.getPorts())
                                if(portInfoOutput.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT)
                                    if(portInfoInput.getName().equals(portInfoOutput.getName()))
                                        for(final String name : Index.launchpads.keySet())
                                            if(portInfoInput.getName().matches(Index.launchpads.get(name).midiNameRegex)) {
                                                Log.i("Output device config found", name);
                                                if(strict_mode) break;
                                                devices.put(
                                                        portInfoInput.getName(),
                                                        new Devices.MidiDevice(
                                                                device, portInfoInput.getName(), portInfoInput, portInfoOutput, Index.launchpads.get(name)
                                                        )
                                                );
                                            }

            if(notify_event) {
                for(String new_connection : devices.keySet())
                    if(!GridController.deviceList.containsKey(new_connection))
                        GridController.connection_event.event(Events.EVENT_CONNECTED, devices.get(new_connection));
                for(String removed_connection : GridController.deviceList.keySet())
                    if(!devices.containsKey(removed_connection))
                        GridController.connection_event.event(Events.EVENT_DISCONNECTED, GridController.deviceList.get(removed_connection));
            }

            GridController.deviceList = devices;
            return devices;
        }
    }

    public static void onMidiStateChange(String state) {Log.i("Midi State Changed", state);}




    /** @return the configuration of all the devices. */
    public static Map<String, GridDeviceConfig> configList() { return Index.launchpads; }

    public static void addConfig(GridDeviceConfig config) { Index.launchpads.put(config.name, config); }

    public static Map<String, Devices.MidiDevice> availableDevices() { return GridController.updateDeviceList(true, false); }

    /** Returns all the available MIDI inputs. */
    public static Map<String, MidiDeviceInfo.PortInfo> availableDeviceInputs()
    {
        Map<String, MidiDeviceInfo.PortInfo> inputs = new HashMap<>();
        for(Devices.MidiDevice midiDevice : GridController.deviceList.values())
            if(midiDevice.input != null)
                inputs.put(midiDevice.name, midiDevice.input);
        return inputs;
    }
    /** Returns all the available MIDI ouputs. */
    public static Map<String, MidiDeviceInfo.PortInfo> availableDeviceOutputs()
    {
        Map<String, MidiDeviceInfo.PortInfo> outputs = new HashMap<>();
        for(Devices.MidiDevice midiDevice : GridController.deviceList.values())
            if(midiDevice.output != null)
                outputs.put(midiDevice.name, midiDevice.output);
        return outputs;
    }

    public void deviceDisconnectedHandler(Devices.MidiDevice device)
    {
        /*if(device.input == this.activeInput. || device.output == this.activeOutput)
        {
            this.disconnect();
        }*/
    }

    public void connectDevice(Devices.MidiDevice device)
    {
        this.disconnect();
        if(device != null) this.connect(device);
    }

    /**
     * @param device MidiDevice.
     */
    private void connect(@Nullable Devices.MidiDevice device)
    {
        this.disconnect();

        midiManager.openDevice(device.deviceInfo, new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice midiDevice) {
                if(device.input != null) {
                    activeInput = midiDevice.openInputPort(device.input.getPortNumber());
                    name = device.input.getName();
                }
                if(device.output != null) {
                    activeOutput = midiDevice.openOutputPort(device.output.getPortNumber());
                    name = device.output.getName();

                    if(activeOutput != null) activeOutput.connect(new MidiReceiver() {
                        @Override
                        public void onSend(byte[] bytes, int offset, int count, long timestamp) throws IOException {
                            if(activeConfig != null) {
                                int note = bytes[offset + 1] & 0xFF;
                                int[] XY = activeConfig.noteToXY.notToXY(note);
                                if(XY[0] > -1 && XY[1] > -1){
                                    if((bytes[offset + 2] & 0xFF) > 0){
                                        keyPress.interact(id, XY);
                                        Log.i("Device note ON", Arrays.toString(XY));
                                    } else {
                                        keyRelease.interact(id, XY);
                                        Log.i("Device note OFF", Arrays.toString(XY));
                                    }
                                }
                                Log.i("XY from note device", Arrays.toString(XY));
                            }
                        }
                    });
                }

                if(activeInput == null && activeOutput == null) {
                    Log.e("Input and Output", "Both Input and output are undefined");
                    return;
                }

                if(device.config == null) //We need to try to auto match device config
                {
                    Log.i("DeviceConfig used", device.name);
                    activeConfig = Index.launchpads.get(device.name);
                    //Input
                    GridDeviceConfig input_config = null;
                    if(device.input != null)
                    {
                        Log.e("Input", "Attempting find input config for" + device.input.getName());
                        String input_name = device.input.getName();
                        for (String name : Index.launchpads.keySet())
                        {
                            if(input_name.matches(Index.launchpads.get(name).midiNameRegex))
                            {
                                Log.v("Input device config found", name);
                                input_config = Index.launchpads.get(name);
                                break;
                            }
                        }
                    }

                    //Output
                    GridDeviceConfig output_config = null;
                    if(device.output != null)
                    {
                        Log.e("Input", "Attempting find input config for" + device.output.getName());
                        String output_name = device.output.getName();
                        for (String name : Index.launchpads.keySet())
                        {
                            if(output_name.matches(Index.launchpads.get(name).midiNameRegex))
                            {
                                Log.v("Input device config found", name);
                                output_config = Index.launchpads.get(name);
                                break;
                            }
                        }
                    }

                    if(output_config == input_config)
                    {
                        activeConfig = output_config;
                    }
                    else //Not matched
                    {
                        if(device.input == null && device.output != null)
                        {
                            activeConfig = output_config;
                        }
                        else if(device.output == null)
                        {
                            activeConfig = input_config;
                        }
                        else
                        {
                            activeConfig = null;
                            Log.e("Input and Output", "Unable to auto match device config");
                        }
                    }
                }
                else // if (typeof config === "DeviceConfig") - Does not work, let's assume it is DeviceConfig
                {
                    Log.i("DeviceConfig", "from parameter used");
                    activeConfig = device.config;
                }

                if(activeConfig == null) Log.e("No active config", "");
                else {
                    Log.i("this.activeConfig.name", "config used");
                    if(activeInput != null && activeConfig.initializationSysex != null)
                        for(byte[] message : activeConfig.initializationSysex)
                            try { activeInput.send(message, 0, message.length);
                            } catch (IOException e) { Log.e("IOException", e.toString()); }
                }

                GridController.connection_event.event(GridController.Events.EVENT_OPENED, id);
            }
        }, null);

    }

    public void disconnect()
    {
        if(this.activeInput != null || this.activeOutput != null)
        {
            if(activeOutput != null) this.activeOutput.disconnect(null);

            this.activeInput = null;
            this.activeOutput = null;
            this.activeConfig = null;
            this.name = null;

            GridController.connection_event.event(GridController.Events.EVENT_CLOSED, this.id);
        }
    }

    @Nullable
    public GridDeviceConfig getDeviceInfo() { return this.activeConfig; }

    public void setConfig(GridDeviceConfig config) {}

    public boolean outputReady() { return this.activeOutput != null && this.activeConfig != null;}

    public void setColor(KeyID keyID, Color /*Color*/ color) throws IOException {
        if(this.activeConfig == null) return;
        if(keyID.isArray())
        {
            if(keyID.getXY()[0] == KeyType.SPECIAL_LED && keyID.getXY()[1] == 0 && this.activeConfig.specialLED != null)
                keyID = this.activeConfig.specialLED;
            else
                keyID = new KeyID(
                keyID.getXY()[0] + this.activeConfig.gridOffset[0],
                        keyID.getXY()[1] + this.activeConfig.gridOffset[1]);
            // console.log(`${keyID[0]} ${keyID[1]} ${note}`)
            Object deviceKeyID = this.activeConfig.keymap[keyID.getXY()[1]][keyID.getXY()[0]];
            if(deviceKeyID != null) setColorOnDevice(deviceKeyID, color);
        }
    }

    public void setColorOnDevice(Object keyID, /*Color*/ Color color) throws IOException {
        if(this.activeConfig == null) return;
        if(color.type == ColorType.PALETTE)
        {
            Integer channel = this.activeConfig.paletteChannel.get(color.palette());
            byte value = 0;//color.index();
            if(channel != null) {
                if (!(keyID instanceof KeyID)) {
                    this.activeInput.send(new byte[]{(byte) (0x90 + channel - 1), (byte) keyID, value}, 0, 3);
                } else {
                    int keyId[] = ((KeyID) keyID).getXY();
                    if (Devices.KeyType.Note == keyId[0]) {
                        this.activeInput.send(new byte[]{(byte) (0x90 + channel - 1), (byte) keyId[1], value}, 0, 3);
                    }
                    else if (((KeyID) keyID).getXY()[0] == KeyType.CC) {
                        this.activeInput.send(new byte[]{(byte) (0xb0 + channel - 1), (byte) keyId[1], value}, 0, 3);
                    }
                    else if (keyId[0] == KeyType.Sysex && this.activeConfig.rgbSysexGen != null) {
                        this.setColorOnDevice(keyId[1], new Color(ColorType.RGB, color.rgb()));
                    }
                }
            }
            else if(this.activeConfig.rgbSysexGen != null)
                this.setColorOnDevice(keyID, new Color(ColorType.RGB, color.rgb()));
        }
        /*else if(color.type === ColorType.RGB && this.activeConfig.rgbSysexGen != null)
        {
            //Assume All messages can be triggered via sysex. Like if a message was flaired with CC, but Sysex will override it
            Object message = (this.activeConfig.rgbSysexGen(
                    (keyID instanceof KeyID) ? ((KeyID) keyID)[1] : (int) keyID
                    , color.rgb()));
            this.activeOutput!.sendSysex([], message);
        }*/
    }

    public void clear(){}

    public void fill(int color){}

    public void fillPalette(int index, int channel){}
}