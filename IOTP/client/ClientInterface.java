/*  CS544 - Computer Networks
 Drexel University
 Protocol Implementation: IoT Home Control Protocol
 Abhilasha Jayaswal

 File name: ClientInterface.java
 
 Purpose:
 Provides an implementation of ClientCommunication interface, which handles
 client communication to the server. This class collects userName input, handles
 message parsing and generation.
*/

package client;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import protocol.*;

import protocol.Util;

public class ClientInterface implements ClientCommunication {

	// indicator to flag the client to process userName input
	private static final String POSTED_MESSAGE = "POSTED_MESSAGE";
	
	//Host to connect to
	private String host;
        
	//Port to connect to
	private int port;
	
        //Username
	private String userName;
        
	//Password
	private String password;
        
	//DFA to be used to track protocol states and process messages
	private DFA dfa;
        
	//Thread that handles userName I/O
	private ClientIOThread clientIOThread;
	/*
	  Holds messages posted by the userName I/O thread, whenever an input is
	  received from the userName
	 */
	private volatile Message postedAction;
	
	/*
	 Constructor for a client communication handler with a CLI for processing
	 client input.
	 */
	public ClientInterface(String host, int port, String userName, String password) {
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.dfa = new ProtocolClientDFA(this, userName, password);
	}
	
	//main thread to handle client connection to the server and userName input
	
	@Override
	public void run() {
		boolean userShutDown = false;
		try {
			// initialize socket
			Socket socket = new Socket(host, port);
			socket.setSoTimeout(Client.CLIENT_SOCKET_LISTENER_TIMEOUT);

			System.out.println(Util.dateTime() + " -- Client " + userName
					+ " connected\n");
			
			BufferedReader br = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			
			// poke
			write(dfa.process(Message.INTERNAL_MSG), bw);
			
			while (true) {
				// read next message from input
				String line = read(socket, br);
				if (line == null) return;
				
				// process input message
				Message inMsg;
				// message generated by userName input
				if (line == POSTED_MESSAGE) {
					inMsg = postedAction;
					//postedAction = null;
				}
				// message received from server
				else {
					inMsg = Message.fromHexString(line);
					inMsg.customPrint("S");
					
					// handle shutdown
					if (inMsg.keycode() == Message.KEY_TERMINATE ||
							inMsg.keycode() == Message.KEY_ERROR) {
						clientIOThread.killInput();
						break;
					}
				}
				
				// process output message
				Message outMsg = dfa.process(inMsg);
				// error
				if (outMsg == null) {
					throw new RuntimeException("Invalid Out Message.");
				}
				// collect input from userName
				else if (outMsg == Message.WAIT_USER_INPUT) {
					//start a client input thread
					createClientInputThread();
				}
				// send message to server
				else {
					write(outMsg, bw);
					// terminate if userName selected to terminate
					if (outMsg == Message.TERMINATE) {
						userShutDown = true;
						break;
					}
				}					
			}
			
			// terminate
			socket.close();
			System.out.println(Util.dateTime() + " Client " + userName + " disconnected");
			if (!userShutDown) System.out.println("Press any key to exit");
			
		}
		catch (ConnectException ce) {
			System.out.println("Unable to connect to " + host + ":" + port);
			System.out.println("Make sure RSHC server is running and try again");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
         Initializes the client input thread, that runs in parallel to the client
	 listening on server updates.
	 */
	private void createClientInputThread() {
		//initialize client input thread
		clientIOThread = new ClientIOThread(this, this.dfa.home());
		clientIOThread.start();
	}
	
	//Utility method to read from the input buffer
	private String read(Socket socket, BufferedReader br) throws IOException {
		String line = null;
		while (line == null) {
			try {
				// read message from server
				line = br.readLine();
				if (line == null) {
					// server closed connection
					socket.close();
					return null;
				}
			} catch (SocketTimeoutException ste) {
				// if userName generated a posted message, flag to parse that message
				if (this.postedAction != null) {
					return POSTED_MESSAGE;
				}
			}
		}
		return line;
	}

	//Utility method to write to the output buffer
	private void write(Message m, BufferedWriter bw) throws IOException {
		m.customPrint("C");
		m.write(bw);
	}
	
	// getter methods
	public String host() {
		return host;
	}
	
	public int port() {
		return port;
	}
	
	public String userName() {
		return userName;
	}
	
	public String password() {
		return password;
	}
	
	// overriding
	
	@Override
	public void postAction(Message actionMessage) {
		this.postedAction = actionMessage;
	}
	
	@Override
	public Message getPostedActionAndReset() {
		Message res = postedAction;
		postedAction = null;
		return res;
	}
	
	@Override
	public void killInput() {
		this.clientIOThread.killInput();
	}
}