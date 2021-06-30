/* 
 CS544 - Computer Networks
 Drexel University
 Protocol Implementation: IoT Home Control Protocol
 Abhilasha Jayaswal

 File name: Client.java
  
 Purpose:
 Main class for starting a client. Provides the main method to initialize a
 connection to the server, either with a client interface or in test mode (allows
 sending raw messages to the server).
 Behavior is controlled with command line arguments; please run with no
 arguments to receive description of expected arguments.
  
 */

package client;

public class Client {
	
    
	// client configuration
	//default timeout for client socket listener
	public static final int CLIENT_SOCKET_LISTENER_TIMEOUT = 1000;
	//default client protocol version
	public static final String PROTOCOL_VERSION = "IOTP 0001";
	//default host 
	private static final String MY_HOST = "127.0.0.1";
	//default port 
	private static final int MY_PORT = 9070;
	
	/*
	 Main method to startup a client connection to the server. Arguments:
	 specify the host to connect to.
	 specify the port to connect to.
	 run a client as default, which allows sending raw messages to the server. 
         If given, does not have to specify username and password.
	 @param args client command line arguments.
	 */
	public static void main(String args[]) {
		ClientCommunication clientCommunication = null;
		int i;
		
		// configuration, host
		String host = MY_HOST;
		for (i = 0; i < args.length - 1; i++) {
			if (args[i].equalsIgnoreCase("-host")) {
				host = args[i + 1];
				break;
			}
		}
		// port
		int port = MY_PORT;
		for (i = 0; i < args.length - 1; i++) {
			if (args[i].equalsIgnoreCase("-port")) {
				try {
					port = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException ex) {
					printUsageAndExit("Inaccessible port no. : " + args[i + 1]);
				}
				break;
			}
		}
		// user and password
		String userName, password;
                userName = null;
                password = null;
		for (i = 0; i < args.length - 1; i++) {
			if (args[i].equalsIgnoreCase("-login")) {
				String[] s = args[i + 1].split(":");
				if (s.length != 2) {
					printUsageAndExit("Incorrect username or password");
				}
				userName = s[0].trim();
				password = s[1].trim();
				break;
			}
		}
		// default client
		boolean test = false;
		for (i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-default")) {
				test = true;
				break;
			}
		}
		if (!test && (userName == null || password == null))
			printUsageAndExit("No username or password found");
		
		
		/*
		 STARTING CLIENT 
		 initializing client communication thread
		 */
		
		// starts in default mode
		if (test) {
			System.out.println("Testing Default-->");
			clientCommunication = new ClientCommunicationTester(host, port);
		}
		// start in standard mode
		else {
			clientCommunication = new ClientInterface(host, port, userName, password);
		}
		Thread thread = new Thread(clientCommunication);
		thread.start();
	}
	
	/*
	 Prints the given error message along with client run usage, and exits the
	 program.
	 @param message error message.
	 */
        
	private static void printUsageAndExit(String message) {
		System.out.println(message);
		System.out.println("Arguments as expected:");
		System.out.println("[-host <host>] [-port <port>] -login <userName>:<password>");
		System.out.println("E.g.: -host 122.0.1.2 -port 8080 -login myname:mypassword");
		System.out.println("*  Default host: 127.0.0.1");
		System.out.println("*  Default port: 9070");
		System.out.println("In order to run in default mode use the argument:");
		System.out.println("-default");
		System.exit(-1);
	}
}
