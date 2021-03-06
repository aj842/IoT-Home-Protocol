/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: ServerCommunication.java
  
  Purpose:
  A class for a server communication handler object. A ServerCommunication object is
  created and assigned by ConnectListener to every incoming client
  connection; it then listens to the client commands and in charge of handling
  the communication, similar to ClientComm for the client.
  */

package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import protocol.Util;

import protocol.*;


public class ServerCommunication implements Runnable, Comparable<ServerCommunication> {
	
	/*
	  The unique numeric identifier of the client handled by this server
	  communication handler
	 */
	private int id;
	// The parent connection listener
	private ConnectListener connectionListener;
	//Flag for terminate
	private boolean terminate = false;
	//The connection socket
	private Socket socket;
	//DFA to be used to track protocol states and process messages
	private DFA dfa;
	
	/*
	  A queue to maintain update messages generated by other server communication
	  handlers in response to their client's actions
	 */
	private ConcurrentLinkedQueue<Message> sendQueue = new ConcurrentLinkedQueue<>();
	
	
	/*
	  Constructs a new server communication handler with the given client id,
	  parent connection listener, connection socket and server DFA.
	 */
	public ServerCommunication(int id, ConnectListener cl, Socket s, ProtocolServerDFA dfa) {
		this.id = id;
		this.connectionListener = cl;
		this.socket = s;
		this.dfa = dfa;
		// attach this server communication handler to the DFA
		dfa.setServerComm(this);
	}
	
	/*
	 main thread to handle server communication to the client, parse and
	 respond to the client messages etc.
	 */
	
	@Override
	public void run() {
		try {
			// initialize readers and writers
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			socket.setSoTimeout(Server.LISTEN_TIMEOUT_MS);
			
			System.out.println(Util.dateTime() + " -- Connection with C" + id +
					" initiated");
			
			// read messages
			while (true) {
				// read user input
				String inBuff = null;
				while (inBuff == null) {
					try {
						inBuff = br.readLine();
						if (inBuff == null) {
							//client closed connection.
							socket.close();
							return;
						}
					} catch (SocketTimeoutException e) {
						// on timeout, before attempting to read user input again,
						// send any pending update messages to the client
						while (!sendQueue.isEmpty()) {
							Message outMsg = sendQueue.remove();
							outMsg.customPrint("S ");
							outMsg.write(bw);
						}
						// handle terminate
						if (terminate) {
							Message.TERMINATE.write(bw);
							terminate();
							return;
						}
					}
				}
				// process client message and generate response
				Message inMsg = Message.fromHexString(inBuff);
				inMsg.customPrint("C" + id);
				Message outMsg = dfa.process(inMsg);
				
				// send response to client
				outMsg.customPrint("S ");
				outMsg.write(bw);
				
				// check for terminate / error
				if (outMsg.keycode() == Message.KEY_TERMINATE ||
						outMsg.keycode() == Message.KEY_ERROR) {
					terminate = true;
					terminate();
					return;
				}
			}
		} catch (Exception e) {
			connectionListener.remove(this);
			e.printStackTrace();
		}
	}
	
	/*
	  Terminates the current server communication handler and removes it from
	  the list of handlers maintained by the parent connection listener.
	 */
	public void terminate() throws Exception {
		connectionListener.remove(this);
		socket.close();
		System.out.println(Util.dateTime() + " Connection with C"
				+ id + " terminated");
	}
	
	//Marks the server communication handler to terminate.
	public void markterminate() {
		terminate = true;
	}

	/*
	  Adds the input update message to the queue of pending updates to be sent
	  to the client. Called by other server communication handlers that
	  confirmed an action, via broadcast to all other handlers.
	  @param msg the update message to add.
	 */
	public void appendToSendQueue(Message msg) {
		sendQueue.add(msg);
	}
	
	@Override
	public int compareTo(ServerCommunication o) {
		return id - o.id;
	}
}
