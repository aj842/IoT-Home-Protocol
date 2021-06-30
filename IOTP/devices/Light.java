/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: Light.java
  
  Purpose:
  Class for representation of a light device, that can be part of a home
  controlled by the protocol.
 */

package devices;

import java.util.*;

import protocol.Util;

public class Light extends IOTDevice {
	
	// legal opcodes
	private static final byte TURN_ON = 0;
	private static final byte TURN_OFF = 1;
	private static final byte DIM = 2;
	protected static Map<Byte,String> opcodeMap;
	protected static Map<Byte,String[]> opcodeParamMap;
	static {
		// opcode map
		opcodeMap = new HashMap<>();
		opcodeMap.put(TURN_ON, "Turn ON");
		opcodeMap.put(TURN_OFF, "Turn OFF");
		opcodeMap.put(DIM, "Dim");
		
		// opcode parameters map
		opcodeParamMap = new HashMap<>();
		opcodeParamMap.put(TURN_ON, null);
		opcodeParamMap.put(TURN_OFF, null);
		opcodeParamMap.put(DIM, new String[]{"Dim level"});
	}
	
	// fields
	private LightState state;
	private byte dimLevel;
	//Default constructor for Light
	Light() {}
	
	//Constructs Light with the given name and device number
	public Light(String name, byte deviceNumber) {
		super(name, deviceNumber);
	}
	
	//Constructs Light with the given name, device number and initial state
	public Light(String name, byte deviceNumber, LightState state) {
		super(name, deviceNumber);
		this.state = state;
	}

	/*
	  Constructs Light with the given name, device number, initial state and
	  parameters (should contain only dim level).
	 */
	public Light(String name, byte deviceNumber, LightState state, byte[] params) {
		super(name, deviceNumber);
		this.state = state;
		this.dimLevel = params[0];
	}

	// overriding methods
	
	@Override
	public byte deviceType() {
		return DeviceType.LIGHT.type();
	}

	@Override
	public void doAction(DeviceAction action) throws Exception {
		byte opcode = action.opcode();
		// turn on
		if (opcode == TURN_ON) {
			if (action.numParams() != 0) throw new Exception("Turn on Light " +
					"expected 0 parameters, given: " + action.numParams());
			turnOn();
		}
		// turn off
		else if (opcode == TURN_OFF) {
			if (action.numParams() != 0) throw new Exception("Turn off Light " +
					"expected 0 parameters, given: " + action.numParams());
			turnOff();
		}
		// dim
		else if (opcode == DIM) {
			if (action.numParams() != 1) throw new Exception("Dim Light " +
					"expected 1 parameters, given: " + action.numParams());
			dim(action.getParam(0));
		}
		// error
		else {
			throw new Exception("Illegal opcode for Light: " + opcode);
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
				new byte[]{dimLevel});				// params
	}
	
	@Override
	public String toCustomString() {
		return String.format("#%03d %-16s %-10s dim-level: %d",
				deviceNumber, name, state, dimLevel);
	}
	
	@Override
	public Map<Byte,String> opKeysMap() {
		return opcodeMap;
	}
	
	@Override
	public Map<Byte,String[]> opKeysParamMap() {
		return opcodeParamMap;
	}
	
	// local setters
	
	/*
	  Turns on the light.
	  @throws Exception if the light is already on.
	 */
	protected void turnOn() throws Exception {
		if (state == LightState.ON)
			throw new Exception("Cannot turn on Light " +
					deviceNumber + " (" + name + ") when already on");
		state = LightState.ON;
	}
	
	/*
	  Turns off the light.
	  @throws Exception if the light is already off.
	 */
	protected void turnOff() throws Exception {
		if (state == LightState.OFF)
			throw new Exception("Cannot turn off Light " +
					deviceNumber + " (" + name + ") when already off");
		state = LightState.OFF;
	}
	
	/*
	  Sets the dim level of the light.
	  @param dimLevel the dim level to set.
	  @throws Exception if the light is off.
	 */
	protected void dim(byte dimLevel) throws Exception {
		if (state == LightState.OFF)
			throw new Exception("Cannot dim Light " +
					deviceNumber + " (" + name + ") when off");
		this.dimLevel = dimLevel;
	}
	
	// getters
	
	public LightState state() {
		return state;
	}
	
	public byte dimLevel() {
		return dimLevel;
	}
}

//Enumeration of Light states
enum LightState {
	OFF((byte)0),
	ON((byte)1);
	
	private LightState(byte type) {
		this.type = type;
	}
	
	private byte type;
	
	public byte type() {
		return type;
	}
	
	public static LightState typeFromCode(byte code) {
		switch (code) {
			case 0: return OFF;
			case 1: return ON;
			default: {
				throw new RuntimeException("Invalid LightState given: " + code);
			}
		}
	}	
}
