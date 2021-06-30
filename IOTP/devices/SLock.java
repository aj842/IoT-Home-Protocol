/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: SLock.java
  
  Purpose:
  Class for representation of an smart lock device, that can be part of a home
  controlled by the protocol.
 */

package devices;

import java.util.HashMap;
import java.util.Map;

import protocol.Util;

public class SLock extends IOTDevice {
	
	// legal opcodes
	private static final byte TURN_ON = 0;
	private static final byte TURN_OFF = 1;
	private static final byte ARM = 2;
	protected static Map<Byte,String> opcodeMap;
	protected static Map<Byte,String[]> opcodeParamMap;
	static {
		// opcode map
		opcodeMap = new HashMap<>();
		opcodeMap.put(TURN_ON, "Turn ON");
		opcodeMap.put(TURN_OFF, "Turn OFF");
		opcodeMap.put(ARM, "Arm");
		
		// opcode parameters map
		opcodeParamMap = new HashMap<>();
		opcodeParamMap.put(TURN_ON, null);
		opcodeParamMap.put(TURN_OFF, null);
		opcodeParamMap.put(ARM, null);
	}
	
	// fields
	private SLockState state;
	
	// constructors
	
	//Default constructor for SLock
	SLock() {}
	
	//Constructs SLock with the given name and device number
	public SLock(String name, byte deviceNumber) {
		super(name, deviceNumber);
	}
	
	//Constructs SLock with the given name, device number and initial state
	public SLock(String name, byte deviceNumber, SLockState state) {
		super(name, deviceNumber);
		this.state = state;
	}
	
	/*
	  Constructs AirCon with the given name, device number, initial state and
	  parameters (should be empty).
	 */
	public SLock(String name, byte deviceNumber, SLockState state, byte[] params) {
		this(name, deviceNumber, state);
	}

	// overriding methods
	
	@Override
	public byte deviceType() {
		return DeviceType.SLOCK.type();
	}

	@Override
	public void doAction(DeviceAction action) throws Exception {
		byte opcode = action.opcode();
		// turn on
		if (opcode == TURN_ON) {
			if (action.numParams() != 0) throw new Exception("Turn on SLock " +
					"expected 0 parameters, given: " + action.numParams());
			turnOn();
		}
		// turn off
		else if (opcode == TURN_OFF) {
			if (action.numParams() != 0) throw new Exception("Turn off SLock " +
					"expected 0 parameters, given: " + action.numParams());
			turnOff();
		}
		// dim
		else if (opcode == ARM) {
			if (action.numParams() != 0) throw new Exception("Arm SLock " +
					"expected 0 parameters, given: " + action.numParams());
			arm();
		}
		// error
		else {
			throw new Exception("Illegal opcode for SLock: " + opcode);
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
				new byte[]{});					// params
	}
	
	@Override
	public String toCustomString() {
		return String.format("#%03d %-16s %-10s",
				deviceNumber, name, state);
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
	  Turns on the SLock.
	  @throws Exception if the SLock is already on.
	 */
	protected void turnOn() throws Exception {
		if (state == SLockState.ON)
			throw new Exception("Cannot turn on SLock " +
					deviceNumber + " (" + name + ") when already on");
		state = SLockState.ON;
	}
	
	/*
	  Turns off the SLock.
	  @throws Exception if the SLock is already off.
	 */
	protected void turnOff() throws Exception {
		if (state == SLockState.OFF)
			throw new Exception("Cannot turn off SLock " +
					deviceNumber + " (" + name + ") when already off");
		state = SLockState.OFF;
	}
	
	/*
	  Arms the SLock.
	  @throws Exception if the SLock is already armed.
	 */
	protected void arm() throws Exception {
		if (state == SLockState.ARMED)
			throw new Exception("Cannot arm SLock " +
					deviceNumber + " (" + name + ") when already armed");
		state = SLockState.ARMED;
	}
}

//Enumeration of SLock states
enum SLockState {
	OFF((byte)0),
	ON((byte)1),
	ARMED((byte)2);
	
	private SLockState(byte type) {
		this.type = type;
	}
	
	private byte type;
	
	public byte type() {
		return type;
	}
	
	public static SLockState typeFromCode(byte code) {
		switch (code) {
			case 0: return OFF;
			case 1: return ON;
			case 2: return ARMED;
			default: {
				throw new RuntimeException("Invalid SLockState given: " + code);
			}
		}
	}		
}
