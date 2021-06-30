/* 
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: ClientCommunication.java
  
  Purpose:
  An interface for a client communication handler object in order to enforces client
  communication handlers.
 */

package client;

import protocol.Message;

public interface ClientCommunication extends Runnable {
	
	/*
	  Post an action to be sent to the server.
	  @param actionMessage action message to be sent to the server.
	 */
	public void postAction(Message actionMessage);
	
	/*
	  Get the posted action to be sent to the server.
	  @return the action message to be sent to the server.
	 */
	public Message getPostedActionAndReset();

	/*
	  Disrupt and terminate any current user input handling. Used to allow
	  incoming server messages update the state of the house at the client side.
	 */
	public void killInput();

}
