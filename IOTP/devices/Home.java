/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: Home.java
  
  Purpose:
  Class for representing a home controlled by the protocol. The home
  maintains all iot device instances. The server holds a home object for the 
  home it controls, and the clients receive encoding of the home, from which they
  generate a local image of the home they control remotely.
 */

package devices;

import java.util.ArrayList;
import java.util.List;

import protocol.Message;

import protocol.Util;

public class Home {
	
	//List of iot devices available in the home
	private List<List<IOTDevice>> devices = new ArrayList<List<IOTDevice>>();
	
        /*
          Sequence number field, used for tracking actions generated to be applied
	  on the home devices.
	 */
	private byte sequenceNumber = 0;
	
	//constructs a new home with no devices.
	public Home() {
		devices = new ArrayList<List<IOTDevice>>();
		devices.add(new ArrayList<IOTDevice>());
		devices.add(new ArrayList<IOTDevice>());
		devices.add(new ArrayList<IOTDevice>());
		devices.add(new ArrayList<IOTDevice>());
		devices.add(new ArrayList<IOTDevice>());
	}
	
	/*
          Adds the given device to the list of devices of the same type, with the
	  index of the last element as the device number.
	  @param device the device to add.
	  @return the added device.
	 */
	public IOTDevice addDevice(IOTDevice device) {
		List<IOTDevice> l = devices.get(device.deviceType());
		device.setDeviceNumber((byte)l.size());
		l.add(device);
		return device;
	}
	
	/*
          Applies the given action on the respective device.
	  @param action
	 */
	public synchronized void doAction(DeviceAction action) throws Exception {
		devices.get(action.deviceType()).get(action.deviceNumber())
				.doAction(action);
	}
	
	//@return the init message for this home
	public byte[] getInit() {
		// initialize byte stream and init opcode
		List<Byte> bytes = new ArrayList<Byte>();
		bytes.add((byte) Message.KEY_INITIAL);
		// iterate over devices and accumulate their encoding
		for (List<IOTDevice> deviceList : devices) {			
			bytes.add((byte) deviceList.size());
			for (IOTDevice device : deviceList) {
				bytes.addAll(Util.toByteList(device.getBytes()));
			}
		}
		bytes.addAll(Util.toByteList("\n".getBytes()));
		// convert to array before return
		byte bytesArr[] = new byte[bytes.size()];
		for (int i = 0; i < bytesArr.length; i++) {
			bytesArr[i] = bytes.get(i);
		}
		return bytesArr;
	}
	
	// used for custom printing the state of the home
	private static final String THIN_SEP =
			"---------------------------------------" +
			"------------------------------";
	private static final String THICK_SEP =
			"========================================" +
			"==============================";
	
	/*
          Prints the current state of the home - the list of all contained devices
	  and their states.
	 */
	public void customPrint() {
		String ind = "       ";
		String pre;
		int devTypes = devices.size();
		List<IOTDevice> devs;
		
		// print header
		System.out.println(THICK_SEP);
		System.out.println("Home current state:");
		System.out.println(String.format("%-7s%-4s %-16s %-10s %s",
				"Type", "Num", "Name", "State", "Params"));
		
		// iterate over device types and print all devices
		for (int devType = 0; devType < devTypes; devType++) {
			System.out.println(THIN_SEP);
			pre = String.format("%-7s",
					DeviceType.typeFromCodeSafe((byte) devType));
			devs = devices.get(devType);
			for (IOTDevice d: devs) {
				System.out.println(
						(pre == null ? ind : pre) +
						d.toCustomString());
				pre = null;
			}
		}	
		System.out.println(THICK_SEP);
	}
	
	/*
          @return the home object generated from the given INIT message, used by
	  the client to construct the image of the home locally from the encoding
	  sent from the server.
	 */
	public static Home createHomeFromInit(Message m) {
		// initialize an empty home
		Home home = new Home();
		byte[] b = m.bytes();
		int index = 1;
		// iterate over device types and construct devices
		for (byte deviceType = 0; deviceType < 5; deviceType++) {
			int deviceCount = b[index++];
			// construct device instances
			for (byte deviceNum = 0; deviceNum < deviceCount; deviceNum++) {
				int numParms = DeviceType.typeFromCodeSafe(deviceType)
						.numParams();
				byte[] d = new byte[17+numParms];
				for (int k = 0; k < d.length; k++) {
					d[k] = b[index++];
				}
				// create device and add to home
				IOTDevice device = IOTDevice.createDeviceFromBytes(
						DeviceType.typeFromCodeSafe(deviceType), deviceNum, d);
				home.addDevice(device);
			}
		}
		return home;
	}

	/*
          @return the action message generated from the given device type, number,
	  sequence number, opcode and parameters.
	 */
	public Message createActionMessage(byte deviceType, byte deviceNumber,
			byte opcode, byte[] params) {
		try {
			return devices.get(deviceType).get(deviceNumber)
					.getActionMessage(sequenceNumber++, opcode, params);
		} catch (Exception e) {
			e.printStackTrace();
			return Message.ERROR_GENERAL;
		}
	}

	//Apply the update from the given update message on the home
	public void doUpdate(Message updateMessage) throws Exception {
		// disguise the update as an action
		byte[] b = updateMessage.bytes();
		byte[] actionBytes = new byte[b.length + 1];
		actionBytes[0] = Message.KEY_ACTION;
		actionBytes[1] = 0x00;	// dummy sequence number
		for (int i = 1; i < b.length; i++)
			actionBytes[i + 1] = b[i];
		DeviceAction a = new DeviceAction(actionBytes);
		// apply the action
		doAction(a);
	}
	
	// getter method
	public List<List<IOTDevice>> devices() {
		return devices;
	}
}
