/* 
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
  
  File name: ClientCommunicationTester.java
  
  Purpose:
  Implementation for ClientComm that allows sending raw messages to the server.
  Designed for testing protocol robustness to random streams, illegal messages
  etc.
  
  Requirements Fullfilled:- CLIENT - UI
  */


package client;

import java.io.*;
import java.net.*;

import protocol.*;

import protocol.Util;

public class ClientCommunicationTester implements ClientCommunication {
	
	//Host to connect to
	private String host;
        
	//Port to connect to
	private int port;
	
        //Buffered reader for reading user input
	private BufferedReader userInputReader;
	
	/*
	  Constructor for a client communication tester with CLI to process user
	  input.
	 */
	public ClientCommunicationTester(String host, int port) {
		this.host = host;
		this.port = port;
		userInputReader = new BufferedReader(new InputStreamReader(System.in));
	}
	
	//main thread to handle client connection to the server and user input
	
	@Override
	public void run() {
		// flag to mark whether to continue connecting to the server for more
		// tests
		boolean connect = true;
		while (connect) {
			try {
				// initialize socket
				Socket socket = new Socket(host, port);
				socket.setSoTimeout(Client.CLIENT_SOCKET_LISTENER_TIMEOUT);

				System.out.println(Util.dateTime() + " DEFAUT CLIENT CONNECTED\n");

				BufferedReader br = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream()));

				Message m;

				// collect user input, send to server and process response
				while (true) {
					//read message from user and send to server
					m = getUserInputMessage();

					// send message to server
					System.out.println(" sending message to server:");
					write(m, bw);

					// terminate if necessary
					if (m.keycode() == Message.KEY_TERMINATE) {
						connect = false;
						break;
					}

					// read message from server
					System.out.println(" reading response from server:");
					String line = read(socket, br);
					if (line == null) return;
					m = Message.fromHexString(line);
					m.customPrint("Server");
					
					// break connection if received a shudown or error message
					// from the server
					if (m.keycode() == Message.KEY_TERMINATE ||
							m.keycode() == Message.KEY_ERROR) {
						break;
					}
					// generate valid challenge-response in case received a
					// challenge message from the server
					if (m.keycode() == Message.KEY_CHALLENGE) {
						System.out.println(">>> valid response message would be:");
						System.out.println(Util.toHexString(
								new Message(Authentication.generateUserResponse(
										"abhi",
										"abhi1234",
										m.contentBytes()),
										Message.KEY_RESPONSE).bytes()));
					}
				}

				// terminate
				socket.close();
				System.out.println(Util.dateTime() + " -- DEFAULT CLIENT DISCONNECTED");
				
				// if last terminate was initiated by the server, attempt to
				// reconnect
				// otherwise, when initiated by user, terminate
				if (connect) {
					System.out.println();
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" +
							"~~~~~~~~~~~~~~~~~~");
					System.out.println("terminate / error received from server");
					System.out.println("reconnecting");
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" +
							"~~~~~~~~~~~~~~~~~~");
					System.out.println();
				}

			}
			catch (ConnectException ce) {
				connect = false;
				System.out.println("Unable to connect to " + host + ":" + port);
				System.out.println("Make sure IOTP server is running and try again");
			}
			catch (Exception e) {
				connect = false;
				e.printStackTrace();
			}
		}
	}
	
	/*
	  Utility method to read user input byte stream and parse it into a message
	  to be sent to the server.
	 */
	private Message getUserInputMessage() throws Exception {
		
                //read message from user and send to server
		String input;
		byte[] b;
		Message m = null;
		while (m == null) {
			// read user input
			System.out.println("Enter message in hex bytes to send the server," +
					" e.g: '01 A6 8B'");
			input = userInputReader.readLine();
			input = input.replaceAll("\\s+"," ").trim();
			try {
				// parse input into message byte stream
				b = Util.toByteStream(input);
			} catch (NumberFormatException e) {
				System.out.println("invalid input, try again");
				continue;
			}
			m = new Message(b);
		}
		return m;
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
			} catch (SocketException e) {
				// connection exception, close connection
				System.err.println("socket exception thrown:");
				e.printStackTrace();
				return null;
			}
		}
		return line;
	}

	//Utility method to write to the output buffer
	private void write(Message m, BufferedWriter bw) throws IOException {
		m.customPrint("Client");
		m.write(bw);
	}
	
	// getters
	
	public String host() {
		return host;
	}
	
	public int port() {
		return port;
	}
		
	// overriding -- stub methods, never used
	
	@Override
	public void postAction(Message actionMessage) {}
	
	@Override
	public Message getPostedActionAndReset() { return null; }
	
	@Override
	public void killInput() {}
	
	// run Client in default mode
	public static void main(String[] args) {
		Client.main(new String[]{"-default"});
	}
}