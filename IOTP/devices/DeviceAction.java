/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: DeviceAction.java
  
  Purpose:
  Class for presenting an action that can be applied on a device. Provides
  functionality for converting the action to a message that can be passed to
  and from the server.
 */

package devices;

import protocol.Message;

public class DeviceAction {
	
	// fields
	
	public static final byte ILLEGAL_PARAM = -1;
	
	//The byte stream for both action (first byte) and parameters.
	private byte[] stream;
	
	/*
	  Constructs a new device action from the given sequence number, device type code,
	  device number, operation code and operation parameters.
	 */
	public DeviceAction(byte sequenceNumber, byte deviceType, byte deviceNumber, byte opcode, byte params[]) {
		this.stream = new byte[5 + params.length];
		stream[0] = Message.KEY_ACTION;
		stream[1] = sequenceNumber;
		stream[2] = deviceType;
		stream[3] = deviceNumber;
		stream[4] = opcode;
		for (int i = 0; i < params.length; i++) {
			stream[5+i] = params[i];
		}		
	}
	
	/*
	  Constructs an action from the given action message, without making an
	  input check.
	  @param inActionMsg the action message to construct an action from.
	 */
	public DeviceAction(Message inActionMsg) {
		this.stream = inActionMsg.bytes();
	}
	
	/*
	  Constructs an action from the given stream. Does not check validity.
	  @param stream
	 */
	public DeviceAction(byte[] stream) {
		this.stream = stream;
	}
		
	//Returns the device action sequence number
	public byte sequenceNumber() {
		return stream[1];
	}
	
	//Returns the device type
	public byte deviceType() {
		return stream[2];
	}
	
	//Returns the device number
	public byte deviceNumber() {
		return stream[3];
	}
	
	//Returns the opcode (byte) of this action
	public byte opcode() {
		return stream[4];
	}
	
	/*
          @param index index of the desired parameter
	  @return the parameter at the given index, or ILLEGAL_PARAM if no 
	  parameter exists at that index.
	 */
	public byte getParam(int index) {
		if (index < 0 || stream.length < index + 5)
			return ILLEGAL_PARAM;
		return stream[index + 5];
	}
	
	//Returns number of parameters in the device action
	public int numParams() {
		return stream.length - 5;
	}

	public Message toMessage() {
		return new Message(this.stream);
	}
}
