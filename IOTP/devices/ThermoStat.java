/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: ThermoStat.java
  
  Purpose:
  Class for representation of an Thermostat device, that can be part of
  a home controlled by the protocol.
  */

package devices;

import java.util.HashMap;
import java.util.Map;

import protocol.Util;

public class ThermoStat extends IOTDevice {
	
	// legal opcodes
	private static final byte TURN_ON = 0;
	private static final byte TURN_OFF = 1;
	private static final byte SET_TEMP = 2;
	protected static Map<Byte,String> opcodeMap;
	protected static Map<Byte,String[]> opcodeParamMap;
	static {
		// opcode map
		opcodeMap = new HashMap<>();
		opcodeMap.put(TURN_ON, "Turn ON");
		opcodeMap.put(TURN_OFF, "Turn OFF");
		opcodeMap.put(SET_TEMP, "Set temperature");
		
		// opcode parameters map
		opcodeParamMap = new HashMap<>();
		opcodeParamMap.put(TURN_ON, null);
		opcodeParamMap.put(TURN_OFF, null);
		opcodeParamMap.put(SET_TEMP, new String[]{"Temperature"});
	}
	
	// fields
	private ThermoStatState state;
	private byte temp;
	
	// constructors
	
	//Default constructor for ThermoStat
	ThermoStat() {}
	
	//Constructs ThermoStat with the given name and device number
	public ThermoStat(String name, byte deviceNumber) {
		super(name, deviceNumber);
	}
	
	//Constructs ThermoStat with the given name, device number and initial state
	public ThermoStat(String name, byte deviceNumber, ThermoStatState state) {
		super(name, deviceNumber);
		this.state = state;
	}
	
	/*
          Constructs ThermoStat with the given name, device number, initial state and
	  parameters (should contain only temperature).
	 */
	public ThermoStat(String name, byte deviceNumber, ThermoStatState state, byte[] params) {
		super(name, deviceNumber);
		this.state = state;
		this.temp = params[0];
	}
	
	// local setters
	
	/*
          Turns on the ThermoStat.
	  @throws Exception if the ThermoStat is already on.
	 */
	protected void turnOn() throws Exception {
		if (state == ThermoStatState.ON)
			throw new Exception("Cannot turn on ThermoStat " +
					deviceNumber + " (" + name + ") when already on");
		state = ThermoStatState.ON;
	}
	
	/*
          Turns off the ThermoStat.
	  @throws Exception if the ThermoStat is already off.
	 */
	protected void turnOff() throws Exception {
		if (state == ThermoStatState.OFF)
			throw new Exception("Cannot turn off ThermoStat " +
					deviceNumber + " (" + name + ") when already off");
		state = ThermoStatState.OFF;
	}
	
	/*
          Sets the temperature of the ThermoStat.
	  @param temp temperature to set.
	  @throws Exception if the ThermoStat is off.
	 */
	protected void setTemp(byte temp) throws Exception {
		if (state == ThermoStatState.OFF)
			throw new Exception("Cannot set temp for ThermoStat " +
					deviceNumber + " (" + name + ") when off");
		this.temp = temp;
	}
	
	// overriding methods
	
	@Override
	public byte deviceType() {
		return DeviceType.THERMO.type();
	}

	@Override
	public void doAction(DeviceAction action) throws Exception {
		byte opcode = action.opcode();
		// turn on
		if (opcode == TURN_ON) {
			if (action.numParams() != 0) throw new Exception("Turn on ThermoStat " +
					"expected 0 parameters, given: " + action.numParams());
			turnOn();
		}
		// turn off
		else if (opcode == TURN_OFF) {
			if (action.numParams() != 0) throw new Exception("Turn off ThermoStat " +
					"expected 0 parameters, given: " + action.numParams());
			turnOff();
		}
		// set temp
		else if (opcode == SET_TEMP) {
			if (action.numParams() != 1) throw new Exception("Set ThermoStat temp " +
					"expected 1 parameters, given: " + action.numParams());
			setTemp(action.getParam(0));
		}
		// error
		else {
			throw new Exception("Illegal opcode for ThermoStat: " + opcode);
		}
	}
	
	@Override
	public String toString() {
		return Util.bufferLeft(' ', 16, name) + state.ordinal();
	}
	
	@Override
	public byte[] getBytes() {
		return Util.cat(
				Util.bufferLeft(' ', 16, name).getBytes(),	// name
				(byte)state.ordinal(),				// state
				new byte[]{temp});				// params
	}
	
	@Override
	public String toCustomString() {
		return String.format("#%03d %-16s %-10s temp: %d",
				deviceNumber, name, state, temp);
	}
	
	@Override
	public Map<Byte,String> opKeysMap() {
		return opcodeMap;
	}
	
	@Override
	public Map<Byte,String[]> opKeysParamMap() {
		return opcodeParamMap;
	}
	
	// getters
	
	public byte temp() {
		return temp;
	}	
}

//Enumeration of ThermoStat states
enum ThermoStatState {
	OFF	((byte) 0),
	ON	((byte) 1);
	
	private ThermoStatState(byte type) {
		this.type = type;
	}
	
	private byte type;
	
	public byte type() {
		return type;
	}
	
	public static ThermoStatState typeFromCode(byte code) {
		switch (code) {
			case 0: return OFF;
			case 1: return ON;
			default: {
				throw new RuntimeException("Invalid ThermoStatState given: " + code);
			}
		}
	}	
}
