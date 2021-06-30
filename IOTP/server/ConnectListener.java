/*   
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: ConnectListener.java
  
  Purpose:
  Handles multiple connections to clients, by maintaining a list of ServerCommunication
  and listening to incoming connections from clients.
 */

package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.ConcurrentSkipListSet;

import protocol.Util;

import protocol.*;


import devices.Home;

public class ConnectListener implements Runnable {
	
	//Counter to assign unique numeric identifiers to incoming connections
	private static int ID_COUNTER = 0;
	//Set of active connections
	private ConcurrentSkipListSet<ServerCommunication> sList =
			new ConcurrentSkipListSet<ServerCommunication>();
	//Flag to mark terminate
	private volatile boolean terminate = false;
	//Home maintained by the server
	private Home home;
	
	//Constructs a new connection listener with the given attached home.
	public ConnectListener(Home home) {
		this.home = home;
	}
	
	@Override
	public void run() {
		try {
			// initialize listen socket
			ServerSocket servSocket = new ServerSocket(Server.DEFAULT_PORT);
			servSocket.setSoTimeout(Server.LISTEN_TIMEOUT_MS);
			System.out.println(Util.dateTime() + " -- Server started\n");
			
			//start terminate listener thread
			startterminateListener();
			
			// loop and listen to incoming connections
			
			/*
			  the following loop listens to incoming connections, and when it
			  receives one it launches a server communication handler for it
			  and continues listening, to handle multiple clients in parallel.
			 */
			
			while (true) {
				Socket commSocket = null;
				while (commSocket == null) {
					try {
						commSocket = servSocket.accept();
					} catch (SocketTimeoutException e) {
						// process terminate
						if (terminate) {
							servSocket.close();
							// close all open connections
							for (ServerCommunication sc: sList)
								sc.markterminate();
							while (!sList.isEmpty()) {}
							return;
						}
					}
				}
				// initialize server communication handler from accepted
				// connection and launch it
				ServerCommunication serverComm = new ServerCommunication(
						ID_COUNTER++,
						this,
						commSocket,
						new ProtocolServerDFA(home, this));
				sList.add(serverComm);
				Thread thread = new Thread(serverComm);
				thread.start();
			}
		}
		catch (BindException e) {
			System.out.println("Port " + Server.DEFAULT_PORT + " already bound");
			System.out.println("Cannot start RSHC server");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	  Raises the terminate flag, such that on the next terminate check the
	  connection will terminate.
	 */
	public void terminate() {
		terminate = true;
	}

	/*
	  Removes the given server communication from the list of communications.
	  @param serverComm
	 */
	public void remove(ServerCommunication serverComm) {
		this.sList.remove(serverComm);		
	}
	
	/*
	  Appends the given update message to the pending messages to send on each
	  server communication except the given one (which is connected to the
	  client that generated the action and caused the update; that client will
	  receive a confirm message).
	 */
	public void broadcast(Message updateMsg, ServerCommunication serverComm) {
		for (ServerCommunication s : sList) {
			if (s == serverComm) continue;
			s.appendToSendQueue(updateMsg);
		}
	}
	
	/*
	  Initiate server terminate command listener. Listens to standard input for
	  server user terminate command.
	 */
	private void startterminateListener() {
		/*
		  handles server terminate concurrently with listening to new client
		  connections
		  presents the server user an interface for terminating down the server
		 */
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// initialize reader
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				boolean terminate = false;
				String line;
				// read until received terminate command
				while (!terminate) {
					System.out.println(" Press T at anytime to terminate server");
					try {
						line = br.readLine();
						// received terminate
						if (line.trim().equalsIgnoreCase("t")) {
							terminate = true;
						}
						// retry
						else {
							System.out.println(" Unrecognized command: " +
									line);
						}
					} catch (IOException e) {
						System.out.println("unable to read input, retrying");
					}
				}
				// finally terminate
				terminate();
			}
		}).start();
	}
}
