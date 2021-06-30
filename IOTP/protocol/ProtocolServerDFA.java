/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: ProtocolServerDFA.java
  
  Purpose:
  Extends the DFA class and provides functionality for server actions in the
  different states of the protocol (DFA).
 */

package protocol;

import server.*;
import devices.*;

public class ProtocolServerDFA extends DFA {
	
	//The parent connection listener
	private ConnectListener connectionListener;
	//The parent server communication handler
	private ServerCommunication serverComm;
	//Challenge for the authentication phase
	private byte[] auth_challenge;
	//Confirm message for client actions
	private Message confirm_client_action;
	
	//Constructs a ProtocolServerDFA with the given home and connection listener.
	public ProtocolServerDFA(Home home, ConnectListener cl) {
		super(home);
		this.connectionListener = cl;
	}
	
	// setters
	
	/*
	  Sets the server communication handler to the given one.
	  @param serverComm server communication handler to set.
	 */
	public void setServerComm(ServerCommunication serverComm) {
		this.serverComm = serverComm;
	}

	/*
	  Broadcast an update to all open connections for an action performed by
	  one of the connected clients.
	  Called whenever an action is confirmed by the server and applied on the
	  home, in order to update the home state at every one of the connected
	  clients.
	 */
	private void broadcastStateChange(Message actionMsg) {
		
                /* 
		  the broadcast handles concurrent clients by making sure all clients
		  are updated on each of the other clients confirmed actions
		 */
		
		Message updateMsg = Message.createUpdate(actionMsg);
		this.connectionListener.broadcast(updateMsg, serverComm);		
	}
	
	
	/*
	  Transitions the protocol state to "client awaits version" and immediately
	  calls the next process phase to prepare a version message to be sent to
	  the client.
	  If given an invalid message for the current state, returns an init
	  error message.
	 */
	protected Message processIdle(Message m) {
		if (m.length() == 1 && m.keycode() == Message.KEY_PING) {
			this.state = ProtocolState.C_AWAITS_VERSION;
			return process(Message.INTERNAL_MSG);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  Transitions the protocol state to "server awaits version" and returns
	  the server supported protocol version.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processClientAwaitsVersion(Message m) {
		if (m.keycode() == Message.KEY_INTERNAL_MSG) {
			this.state = ProtocolState.S_AWAITS_VERSION;
			return Message.VERSION_SERVER;
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  Transitions the protocol state to "client awaits auth_challenge" and immediately
	  calls the next process phase to prepare a auth_challenge message to be sent to
	  the client.
	  If given an invalid message for the current state or an unsupported
	  version, returns a version error message.
	 */
	protected Message processServerAwaitsVersion(Message m) {
		if (m.keycode() == Message.KEY_VERSION
				&& Server.VERSION.equals(m.content())) {
			this.state = ProtocolState.C_AWAITS_CHALLENGE;
			return process(Message.INTERNAL_MSG);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_VERSION;
	}

	/*
	  Transitions the protocol state to "server awaits response" and returns
	  the auth_challenge message to be sent to the client.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processClientAwaitsChallenge(Message m) {
		if (m.keycode() == Message.KEY_INTERNAL_MSG) {
			this.state = ProtocolState.S_AWAITS_RESPONSE;
			auth_challenge = Authentication.generateRandomChallenge();
			return new Message(auth_challenge, Message.KEY_CHALLENGE);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  Transitions the protocol state to "client awaits init" and immediately
	  calls the next process phase to prepare an init message to be sent to
	  the client.
	  If given an invalid message for the current state or the client failed
	  the auth_challenge, returns an authentication error message.
	 */
	protected Message processServerAwaitsResponse(Message m) {
		if (Authentication.checkUserResponse(auth_challenge, m.contentBytes())) {
			this.state = ProtocolState.C_AWAITS_INIT;
			return process(Message.INTERNAL_MSG);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_AUTH;
	}

	/*
	  Transitions the protocol state to "server awaits action" and returns
	  the init message to be sent to the client.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processClientAwaitsInit(Message m) {
		if (m.keycode() == Message.KEY_INTERNAL_MSG) {
			this.state = ProtocolState.S_AWAITS_ACTION;
			return Message.createInit(home);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  If given a terminate message, returns it immediately to signal terminate.
	  If given an action message from the client, applies the action on the
	  home, transitions the protocol state to "client awaits confirm_client_action" and
	  immediately calls the next process phase to prepare a confirm_client_action message
	  to be sent to the client.
	  If the action is confirmed and applied, also broadcasts the action to
	  all other active clients.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processServerAwaitsAction(Message m) {
		// terminate
		if (m.length() == 1 && m.keycode() == Message.KEY_TERMINATE) {
			return Message.TERMINATE;
		}
		// process action
		else if (m.length() > 0 && m.keycode() == Message.KEY_ACTION) {
			DeviceAction action = new DeviceAction(m);
			this.state = ProtocolState.C_AWAITS_CONFIRM;
			try {
				home.doAction(action);
			} catch (Exception e) {
				// action failed
				System.err.println("Action failed: " + e.getMessage());
				confirm_client_action = Message.createConfirm(action.sequenceNumber(),false);
				return process(Message.INTERNAL_MSG);
			}
			// action succeeded
			home.customPrint();
			/*
			 * CONCURRENT
			 * broadcast confirmed action to all other active clients
			 */
			broadcastStateChange(m);
			confirm_client_action = Message.createConfirm(action.sequenceNumber(), true);
			return process(Message.INTERNAL_MSG);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  Transitions the protocol state to "server awaits action" and returns
	  the confirm_client_action message to be sent to the client.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processClientAwaitsConfirm(Message m) {
		if (m.keycode() == Message.KEY_INTERNAL_MSG && confirm_client_action != null) {
			this.state = ProtocolState.S_AWAITS_ACTION;
			Message res = confirm_client_action;
			confirm_client_action = null;
			return res;
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}
}
