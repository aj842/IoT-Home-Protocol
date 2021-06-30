/*  
CS544 - Computer Networks
 Drexel University
 Protocol Implementation: IoT Home Control Protocol
 Abhilasha Jayaswal

 File name: Blind.java
  
 Purpose:
 Class for representation of a Blind(shades or curtain) device, that can be part of a home
 controlled by the protocol.
 */

package devices;

import java.util.HashMap;
import java.util.Map;

import protocol.Util;

public class Blind extends IOTDevice {
	
	// legal opcodes
	private static final byte PUT_DOWN = 0;
	private static final byte PULL_UP = 1;
	private static final byte DIM = 2;
	protected static Map<Byte,String> opcodeMap;
	protected static Map<Byte,String[]> opcodeParamMap;
	static {
		// opcode map
		opcodeMap = new HashMap<>();
		opcodeMap.put(PUT_DOWN, "Put down");
		opcodeMap.put(PULL_UP, "Pull up");
		opcodeMap.put(DIM, "Dim");
		
		// opcode parameters map
		opcodeParamMap = new HashMap<>();
		opcodeParamMap.put(PUT_DOWN, null);
		opcodeParamMap.put(PULL_UP, null);
		opcodeParamMap.put(DIM, new String[]{"Dim level"});
	}
	
	// fields
	private BlindState state;
	private byte dimLevel;
	
	//Default constructor for Shade.
	Blind() {}
	
	//Constructs Shade with the given name and device number
	public Blind(String name, byte deviceNumber) {
		super(name, deviceNumber);
	}

	//Constructs Shade with the given name, device number and initial state
	public Blind(String name, byte deviceNumber, BlindState state) {
		super(name, deviceNumber);
		this.state = state;
	}
	
	/*
         Constructs Shade with the given name, device number, initial state and
	 parameters (should contain only dim level).
	 */
	public Blind(String desc, byte deviceNum, BlindState state, byte[] parms) {
		this(desc, deviceNum, state);
		this.dimLevel = parms[0];
	}

	// overriding methods
	
	@Override
	public byte deviceType() {
		return DeviceType.BLIND.type();
	}

	@Override
	public void doAction(DeviceAction action) throws Exception {
		byte opcode = action.opcode();
		// turn on
		if (opcode == PUT_DOWN) {
			if (action.numParams() != 0) throw new Exception("Put down Blinds " +
					"expected 0 parameters, given: " + action.numParams());
			putDown();
		}
		// turn off
		else if (opcode == PULL_UP) {
			if (action.numParams() != 0) throw new Exception("Pull up Blinds " +
					"expected 0 parameters, given: " + action.numParams());
			pullUp();
		}
		// dim
		else if (opcode == DIM) {
			if (action.numParams() != 1) throw new Exception("Dim Blinds " +
					"expected 1 parameters, given: " + action.numParams());
			dim(action.getParam(0));
		}
		// error
		else {
			throw new Exception("Illegal opkey for Blinds: " + opcode);
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
        Puts down the Blinds
	 */
	protected void putDown() throws Exception {
		if (state == BlindState.DOWN)
			throw new Exception("Cannot put down Shade " +
					deviceNumber + " (" + name + ") when already down");
		state = BlindState.DOWN;
	}
	
	//Pulls the blinds up
	protected void pullUp() throws Exception {
		if (state == BlindState.UP)
			throw new Exception("Cannot pull up Shade " +
					deviceNumber + " (" + name + ") when already up");
		state = BlindState.UP;
	}
	
	//Sets the dim level of the blinds
	protected void dim(byte dimLevel) throws Exception {
		if (state == BlindState.UP)
			throw new Exception("Cannot dim Shade " +
					deviceNumber + " (" + name + ") when up");
		this.dimLevel = dimLevel;
	}
	
	// getters
	
	public byte dimLevel() {
		return dimLevel;
	}	
}

//Enumeration of Blind states.
enum BlindState {
	UP((byte) 0),
	DOWN((byte) 1);
	
	private BlindState(byte type) {
		this.type = type;
	}
	
	private byte type;
	
	public byte type() {
		return type;
	}
	
	public static BlindState typeFromCode(byte code) {
		switch (code) {
			case 0: return UP;
			case 1: return DOWN;
			default: {
				throw new RuntimeException("Invalid BlindState given: " + code);
			}
		}
	}		
}
