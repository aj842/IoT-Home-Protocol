/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: ProtocolClientDFA.java
  
  Purpose:
  Extends the DFA class and provides functionality for client actions in the
  different states of the protocol (DFA).
 */

package protocol;

import client.*;
import devices.*;

public class ProtocolClientDFA extends DFA {

	//The parent client communication handler
	private ClientCommunication clientComm;
	
        //The client username
	private String userName = null;
	//The client password
	private String password = null;
	//Response for the authentication phase
	private Message response;
	
	/*
	  Constructs a ClientDFA with the given client communication handler,
	  username and password.
	 */
	public ProtocolClientDFA(ClientCommunication clientComm, String userName, String password) {
		super(null);
		this.clientComm = clientComm;
		this.userName = userName;
		this.password = password;
	}

	/*
	 Transitions the protocol state to "client awaits version" and returns a
	 poke message to initiate communication with the server.
	 If given an invalid message for the current state, returns a general
	 error message.
	*/
	protected Message processIdle(Message m) {
		if (m.keycode() == Message.KEY_INTERNAL_MSG) {
			state = ProtocolState.C_AWAITS_VERSION;
			return Message.PING;
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  Transitions the protocol state to "server awaits version" and immediately
	  calls the next process phase to prepare a version message to be sent to
	  the server.
	  If given an invalid message for the current state, returns a version
	  error message.
	 */
	protected Message processClientAwaitsVersion(Message m) {
		if (m.keycode() == Message.KEY_VERSION
				&& Client.PROTOCOL_VERSION.equals(m.content())) {
			state = ProtocolState.S_AWAITS_VERSION;
			return process(Message.INTERNAL_MSG);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_VERSION;
	}

	/**
	 * Transitions the protocol state to "client awaits challenge" and returns
	 * the client selected protocol version.
	 * If given an invalid message for the current state, returns a general
	 * error message.
	 */
	protected Message processServerAwaitsVersion(Message m) {
		if (m.keycode() == Message.KEY_INTERNAL_MSG) {
			this.state = ProtocolState.C_AWAITS_CHALLENGE;
			return Message.VERSION_CLIENT;
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  Transitions the protocol state to "server awaits response" and immediately
	  calls the next process phase to send the response to the server.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processClientAwaitsChallenge(Message m) {
		if (m.keycode() == Message.KEY_CHALLENGE) {
			response = new Message(Authentication.generateUserResponse(
					userName,
					password,
					m.contentBytes()),
					Message.KEY_RESPONSE);
			state = ProtocolState.S_AWAITS_RESPONSE;
			return process(Message.INTERNAL_MSG);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  Transitions the protocol state to "client awaits init" and returns the
	  challenge response message to be sent to the server.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processServerAwaitsResponse(Message m) {
		if (m.keycode() == Message.KEY_INTERNAL_MSG && response != null) {
			this.state = ProtocolState.C_AWAITS_INIT;
			Message res = response;
			response = null;
			return res;
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  Transitions the protocol state to "server awaits action" and immediately
	  calls the next process phase to send the action to the server.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processClientAwaitsInit(Message m) {
		if (m.keycode() == Message.KEY_INITIAL) {
			this.home = Home.createHomeFromInit(m);
			System.out.println("::: Server home image at client side :::");
			this.home.customPrint();
			this.state = ProtocolState.S_AWAITS_ACTION;
			return process(Message.INTERNAL_MSG);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  If given an action message, transitions the protocol state to "client
	  awaits confirm" and returns action message to be sent to the server.
	  If given an update message (sent from the server), applies the update on
	  the local home image.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processServerAwaitsAction(Message m) {
		// immediately after init message, signal client that no message
		// is to be sent to server until given userName input
		if (m.keycode() == Message.KEY_INTERNAL_MSG) {
			return Message.WAIT_USER_INPUT;
		}
		// process userName action
		else if (m.keycode() == Message.KEY_ACTION) {
			state = ProtocolState.C_AWAITS_CONFIRM;
			return m;
		}
		// process server update
		else if (m.keycode() == Message.KEY_UPDATE) {
			return processUpdate(m);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}

	/*
	  If given ac confirm message, transitions the protocol state to "server
	  awaits action" and signals to await userName input action.
	  If given an update message (sent from the server), applies the update on
	  the local home image.
	  If given an invalid message for the current state, returns a general
	  error message.
	 */
	protected Message processClientAwaitsConfirm(Message m) {
		if (m.keycode() == Message.KEY_CONFIRM) {
			byte[] b = m.bytes();
			boolean confirmed = b[2] == 1;
			DeviceAction action = new DeviceAction(clientComm.getPostedActionAndReset());
			if (confirmed) {
				// apply confirmed message internally
				try {
					home.doAction(action);
				} catch (Exception e) {
					System.out.println("Internal error applying posted message on home");
					state = ProtocolState.IDLE;
					return Message.ERROR_GENERAL;
				}
				System.out.println("::: Action " + b[1]
						+ " confirmed, new state of the home :::");
				home.customPrint();
			}
			else {
				System.out.println("::: Action " + b[1] + " denied :::");
			}
			state = ProtocolState.S_AWAITS_ACTION;
			return Message.WAIT_USER_INPUT;
		}
		// process server update
		else if (m.keycode() == Message.KEY_UPDATE) {
			return processUpdate(m);
		}
		// error: go back to idle and return error message
		this.state = ProtocolState.IDLE;
		return Message.ERROR_GENERAL;
	}
	
	/*
	  Should be called to process a server update (response to actions
	  performed by some other client).
	  @param m update message.
	 */
	private Message processUpdate(Message m) {
		try {
			home.doUpdate(m);
		} catch (Exception e) {
			System.out.println("Internal error applying update on home");
			state = ProtocolState.IDLE;
			return Message.ERROR_GENERAL;
		}
		clientComm.killInput();
		System.out.println("::: Update received from server :::");
		home.customPrint();
		return Message.WAIT_USER_INPUT;
	}
}
