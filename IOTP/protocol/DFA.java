/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: DFA.java
  
  Purpose:
  Defines the states of the protocol and abstract methods to be implemented by
  the client and server for the operations to apply at any given state of th
  protocol.
  Extended by ProtocolServerDFA and ProtocolClientDFA - each applying the procedures, state
  transitions and message generation for the respective state the DFA is at.
 */

package protocol;

import devices.*;

public abstract class DFA {
	
	// common server and client fields
	
	//The state of the protocol
	protected ProtocolState state = ProtocolState.IDLE;
	//The home
	protected Home home;
	
	//Initializes the DFA with the given home.
	public DFA(Home home) {
		this.home = home;
	}
	
	
	/*
	  Main DFA message processing procedure. Processes the given message with
	  respect to the current state of the DFA, changes the state of the DFA
	  accordingly and returns the message to be sent to the other side.
	 */
	public Message process(Message m) {
		// if the incoming message is a shutdown request, return immediately
		if (m == Message.TERMINATE) return m;
		// otherwise, process the message in the respective state of the protocol
		switch (state) {
		case IDLE:					return processIdle(m);
		case C_AWAITS_VERSION:		return processClientAwaitsVersion(m);
		case S_AWAITS_VERSION:		return processServerAwaitsVersion(m);
		case C_AWAITS_CHALLENGE:	return processClientAwaitsChallenge(m);
		case S_AWAITS_RESPONSE:		return processServerAwaitsResponse(m);
		case C_AWAITS_INIT:			return processClientAwaitsInit(m);
		case S_AWAITS_ACTION:		return processServerAwaitsAction(m);
		case C_AWAITS_CONFIRM:		return processClientAwaitsConfirm(m);
		default:					return Message.ERROR_GENERAL; // should not get here
		}
	}
	
	// Processes the given message when the protocol is in idle state.
	protected abstract Message processIdle(Message m);
	
	/*
	  Processes the given message when the protocol is in client awaits version
	  state.
	 */
	protected abstract Message processClientAwaitsVersion(Message m);
	
	/*
	  Processes the given message when the protocol is in server awaits version
	  selection state.
	 */
	protected abstract Message processServerAwaitsVersion(Message m);
	
	/*
	  Processes the given message when the protocol is in client awaits
	  authentication challenge state.
	 */
	protected abstract Message processClientAwaitsChallenge(Message m);
	
	/*
	  Processes the given message when the protocol is in server awaits client
	  authentication challenge response state.
	 */
	protected abstract Message processServerAwaitsResponse(Message m);
	
	/*
	  Processes the given message when the protocol is in client awaits init
	  state.
	 */
	protected abstract Message processClientAwaitsInit(Message m);
	
	/*
	  Processes the given message when the protocol is in server awaits action
	  state.
	 */
	protected abstract Message processServerAwaitsAction(Message m);
	
	/*
	  Processes the given message when the protocol is in client awaits
	  action confirmation / denial state.
	 */
	protected abstract Message processClientAwaitsConfirm(Message m);
	
	// getters
	
	//@return the current state of the protocol (the DFA)
	public ProtocolState state() {
		return state;
	}
	
	//@return the home attached to the DFA
	public Home home() {
		return this.home;
	}
		
	// setters
	
	/*
	  Sets the home attached to the DFA to the given one.
	  @param home the home to attach.
	 */
	public void setHouse(Home home) {
		this.home = home;
	}
}

//Enumerator for DFA states
enum ProtocolState {
	IDLE				("Idle"),
	C_AWAITS_VERSION	("Client awaits version"),
	S_AWAITS_VERSION	("Server awaits version selection"),
	C_AWAITS_CHALLENGE	("Client awaits challenge"),
	S_AWAITS_RESPONSE	("Server awaits response"),
	C_AWAITS_INIT		("Client awaits init"),
	S_AWAITS_ACTION		("Server awaits action"), 
	C_AWAITS_CONFIRM	("Client awaits confirmation");
	
	private String desc;
	
	private ProtocolState(String desc) {
		this.desc = desc;
	}
	
	@Override
	public String toString() {
		return desc;
	}
}
