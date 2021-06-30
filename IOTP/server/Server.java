/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: Server.java
  
  Purpose:
  Main class for starting a server. The server is hardcoded to port 9070. This
  implementation is for demonstration purposes only, therefore it initializes a
  random home which it maintains throughout the entire run.
  The server initializes a connection listener which handles incoming
  connections.
 */

package server;

import devices.Home;
import devices.RandomHomeGenerator;

public class Server {
	
	//Server protocol version
	public static final String VERSION = "IOTP 0001";
	//Random home generation seed
	private static final long HOUSE_GEN_SEED = 4;
	//Maximum number of devices to generate a random home with
	private static final int MAX_DEVICES_PER_TYPE = 3;
	//Socket listen timeout
	public static final int LISTEN_TIMEOUT_MS = 1000;
	//Default server port
	public static final int DEFAULT_PORT = 9070;
	
	/*
	  Main method to initialize server.
	  @param args
	  @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// generate home
		RandomHomeGenerator rhg =
				new RandomHomeGenerator(HOUSE_GEN_SEED, MAX_DEVICES_PER_TYPE);
		Home home = rhg.createHome();
		home.customPrint();
		System.out.println();
		
		//initialize server
		Thread connectionListener = new Thread(new ConnectListener(home));
		connectionListener.start();
	}
}
