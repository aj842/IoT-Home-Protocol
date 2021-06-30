/*
 CS544 - Computer Networks
 Drexel University
 Protocol Implementation: IoT Home Control Protocol
 Abhilasha Jayaswal

 File Name: ClientIOThread.java
  
 Purpose:
 Process and handle action input from the user, to be posted as action to send
 the server for processing. Presents the user only valid options - selecting
 device type, number, action and parameters, or shutting down the connection.
 */

package client;

import java.io.*;
import java.util.*;

import protocol.Message;

import devices.*;

public class ClientIOThread extends Thread {
	
	//Client communication handler that uses this client input thread
	private ClientCommunication clientComm;
	/*
	  The home image on the client side on which all the device related 
          actions are to be performed
	 */
	private Home home;
	/*
	 A flag that indicates whether the user input read should be terminated,
	 to be used when a message is received from the server while reading user
	 input
	 */
	private volatile boolean killInput = false;
	
	/*
	  Constructs a new client input thread, attached to the client communication
	  handler and the home image at the client side.
	 */
	public ClientIOThread(ClientCommunication clientComm, Home home) {
		this.clientComm = clientComm;
		this.home = home;
	}
	
	/*
	 The main run method of the client input thread that handles user I/O.
	 It presents the user with valid options, and the thread collects the
	 user selections and in charge of posting the constructed action to the
	 communication handler.
	 */
	
	@Override
	public void run() {
		try {
			// initialize local variables for handling user input
			boolean legalInput = false;
			byte legalMin, legalMax;
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String input = null;
			String msg;
			
			// get device type and set legal configuration
			DeviceType[] types = DeviceType.values();
			legalMin = types[0].type();
			legalMax = types[types.length - 1].type();
			DeviceType selectedType = null;
                        
			// set message for user
			msg = "Select device type or press T to terminate:\n";
			for (DeviceType type: types)
				msg += "[" + type.type() + "] " + type + "  ";
			// read until legal
			while (!legalInput) {
				System.out.println(msg);
				input = br.readLine();
				// process terminate
				if (input.trim().equalsIgnoreCase("t")) {
					clientComm.postAction(Message.TERMINATE);
					return;
				}
				try {
					// check legal range
					byte code = Byte.parseByte(input);
					if (code < legalMin || code > legalMax)
						throw new Exception("selected device code not in range");
					selectedType = DeviceType.typeFromCode(code);
					// check legal type selected
					if (home.devices().get(selectedType.type()).isEmpty())
						throw new Exception("no devices of selected type");
				} catch (Exception e) {
					if (killInput) return;
					System.out.println("Illegal selection, try again: " +
							e.getMessage());
					continue;
				}
                                
				// mark input is legal
				legalInput = true;
				if (killInput) return;
			}
                        
			// reset
			input = null;
			legalInput = false;
			
			// selected devices
			List<IOTDevice> selectedDevices = home.devices().get(
					selectedType.type());
			
			// get device number
			byte selectedDeviceIndex = -1;
                        
			// set message for user
			msg = "Select device or press T to terminate:";
			for (IOTDevice d: selectedDevices)
				msg += "\n[" + d.deviceNumber()+ "] " + d.name().trim();
                        
			// read user input until it is legal
			while (!legalInput) {
				System.out.println(msg);
				try {
					input = br.readLine();
					// Termination processing
					if (input.trim().equalsIgnoreCase("t")) {
						clientComm.postAction(Message.TERMINATE);
						return;
					}
					// check selected device number is in range
					selectedDeviceIndex = Byte.parseByte(input);
					if (selectedDeviceIndex < 0
							|| selectedDeviceIndex >= selectedDevices.size()) {
						throw new Exception("selected device number not in range");
					}
				} catch (Exception e) {
					if (killInput) return;
					System.out.println("Illegal selection, try again: " +
							e.getMessage());
					continue;
				}
				// mark input is legal
				legalInput = true;
				if (killInput) return;
			}
			// reset
			input = null;
			legalInput = false;
			
			// selected device
			IOTDevice selectedDevice = selectedDevices.get(selectedDeviceIndex);
			System.out.println("selected device: " + selectedDevice.toCustomString());
			
			// operation
			byte selectedOpcode = -1;
                        
			// set message for user
			msg = "Select operation or press T to terminate:";
			Map<Byte,String> opCodesMap = selectedDevice.opKeysMap();
			for (byte key: opCodesMap.keySet())
				msg += "\n[" + key + "] " + opCodesMap.get(key);
			// read user input until legal
			while (!legalInput) {
				System.out.println(msg);
				try {
					input = br.readLine();
					// process terminate
					if (input.trim().equalsIgnoreCase("t")) {
						clientComm.postAction(Message.TERMINATE);
						return;
					}
					// check opcode in range
					selectedOpcode = Byte.parseByte(input);
					if (selectedOpcode < 0 || selectedOpcode >= opCodesMap.size()) {
						throw new Exception("selected opcode not in range");
					}
				} catch (Exception e) {
					if (killInput) return;
					System.out.println("Illegal selection, try again: " +
							e.getMessage());
					continue;
				}
				// mark input is legal
				legalInput = true;
				if (killInput) return;
			}
			// reset
			input = null;
			legalInput = false;
			
			// operation parameters
			String[] paramNames = selectedDevice.opKeysParamMap().get(
					selectedOpcode);
			byte[] params;
			
			// no parameters
			if (paramNames == null) {
				System.out.println("No parameters for operation: "
						+ selectedOpcode);
				params = new byte[]{};
			}
			// expected parameters - process
			else {
				params = new byte[paramNames.length];
				String[] inputArr;
				// set message for user
				msg = "Input " +
						Arrays.toString(paramNames).replace("[", "").replace("]", "") +
						(params.length > 1 ? " (separated by commas)" : "") +
						" or press T to terminate:";
				// read user input until legal
				while (!legalInput) {
					System.out.println(msg);
					try {
						inputArr = br.readLine().split(",");
						// process terminate
						if (inputArr[0].trim().equalsIgnoreCase("t  ")) {
							clientComm.postAction(Message.TERMINATE);
							return;
						}
						// check number of parameters
						if (inputArr.length != paramNames.length)
							throw new Exception(
									"unexpected number of parameters");
						// parse and check parameters
						for (int i = 0; i < inputArr.length; i++) {
							params[i] = Byte.parseByte(inputArr[i].trim());
						}
					} catch (Exception e) {
						if (killInput) return;
						System.out.println("Illegal selection, try again: "
								+ e.getMessage());
						continue;
					}
					// mark input is legal
					legalInput = true;
					if (killInput) return;
				}
			}
			
			// finally, post action to be processed by client communication
			// handler
			clientComm.postAction(home.createActionMessage(
					selectedType.type(),
					selectedDeviceIndex,
					selectedOpcode,
					params));
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	/*
	 Mark to kill the user input thread. Called by the communication handler
	 whenever a message is received by the server during user input collection. 
	 */
	public void killInput() {
		this.killInput = true;
	}
}
