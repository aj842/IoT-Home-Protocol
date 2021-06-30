/*  
 * CS544 - Computer Networks
 * Drexel University
 * Protocol Implementation: IoT Home Control Protocol
 * Abhilasha Jayaswal
 */

package protocol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

import server.Server;
import client.Client;


import devices.Home;

public class Message {
	
	public static final byte KEY_INTERNAL_MSG =	-2;
	public static final byte KEY_WAIT_USER_INPUT = -1;
	public static final byte KEY_PING =	 0;
	public static final byte KEY_VERSION =	 1;
	public static final byte KEY_ERROR =	 2;
	public static final byte KEY_CHALLENGE = 3;
	public static final byte KEY_RESPONSE =	 4;
	public static final byte KEY_INITIAL =   5;
	public static final byte KEY_ACTION =	 6;
	public static final byte KEY_CONFIRM =	 7;
	public static final byte KEY_UPDATE =	 8;
	public static final byte KEY_TERMINATE = 9;
	
	public static final Message INTERNAL_MSG = new Message(KEY_INTERNAL_MSG);
	public static final Message PING = new Message(KEY_PING);
	public static final Message VERSION_SERVER =
			new Message((Server.VERSION).getBytes(), KEY_VERSION);
	public static final Message VERSION_CLIENT =
			new Message((Client.PROTOCOL_VERSION).getBytes(), KEY_VERSION);
	public static final Message TERMINATE = new Message(KEY_TERMINATE);
	public static final Message WAIT_USER_INPUT = new Message(
			KEY_WAIT_USER_INPUT);	
		
	public static final Message ERROR_GENERAL =
			createError("General error");
	public static final Message ERROR_PING =
			createError("Ping error");
	public static final Message ERROR_VERSION =
			createError("Unsupported version");
	public static final Message ERROR_AUTH =
			createError("Failed authentication");
	
	
	private static final int WRAP_SIZE = 60;
	
	
	private byte keycode = -1; // to catch erroneous initialization
	private final byte[] bytes;
	
	
	public Message(byte[] allBytes) {
		keycode = allBytes[0];
		bytes = allBytes;
	}
	public Message(byte[] givenMessage, byte keycode) {
		this.keycode= keycode;  
		bytes = new byte[givenMessage.length + 1];
		bytes[0] = keycode;
		for (int i = 0; i < givenMessage.length; i++)
			bytes[i + 1] = givenMessage[i];
	}
	
	public Message(byte keycode) {
		this.keycode = keycode;
		bytes = new byte[]{keycode};
	}
	
	
	public static Message createConfirm(byte seqNum, boolean accept) {
		return new Message(new byte[] {KEY_CONFIRM, seqNum,
				(accept? (byte) 1 : (byte) 0)});
	}
	
	private static Message createError(String msg) {
		return new Message(msg.getBytes(), KEY_ERROR);
	}
	
	public static Message createInit(Home home) {
		return new Message(home.getInit());
	}
	
	public static Message fromHexString(String hexStr) {
		return new Message(Util.toByteStream(hexStr));
	}
	
	public static Message createUpdate(Message actionMsg) {
		byte[] actionStream = actionMsg.bytes;
		byte[] updateStream = new byte[actionStream.length - 1];
		updateStream[0] = KEY_UPDATE;
		for (int i = 2; i < actionStream.length; i++)
			updateStream[i - 1] = actionStream[i];
		return new Message(updateStream);
	}
	
	public int length() {
		return bytes.length;
	}
	
	public byte keycode() {
		return keycode;
	}
	
	/**
	 * @return the raw stream of bytes of the message.
	 */
	public byte[] bytes() {
		return this.bytes;
	}
	
	/**
	 * @return the string representation of the message content (without the
	 * keycode).
	 */
	public String content() {
		return new String(contentBytes());
	}
	
	/**
	 * @return the raw stream of bytes of the content part of the message (i.e.
	 * without the first byte - the keycode).
	 */
	public byte[] contentBytes() {
		return Arrays.copyOfRange(bytes, 1, bytes.length);
	}
	
	/**
	 * To be used when writing to output stream.
	 * @return a hexadecimal string representation of the bytes of the message,
	 * terminated with a newline.
	 */
	public String toHexString() {
		return Util.toHexString(bytes) + "\n";
	}
	
	@Override
	public String toString() {
		return new String(bytes);
	}
	
	/**
	 * @return a pretty string representation of the message, with separation
	 * between the opcode and the message content.
	 */
	public String toCustomString() {
		return "OP: " + keycode + " | MESSAGE: " + content();
	}
	
	// ACTIONS
	
	/**
	 * Writes the message (its hexadecimal + '\n' representation) to the given
	 * buffered writer and flushes.
	 * @param bw
	 * @throws IOException
	 */
	public void write(BufferedWriter bw) throws IOException {
		bw.write(toHexString());
		bw.flush();
	}
	
	/**
	 * Pretty prints this message with prefix set to the given sender.
	 * @param sender
	 */
	public void customPrint(String sender) {
		System.out.println(Util.time() + " " + sender + " >");
		String raw = toCustomString().replaceAll("\n", "\\n").replaceAll("\r", "\\r");
		String bytecode = toHexString();
		System.out.println(indentedWrapped(
				"raw:  ", raw, WRAP_SIZE));
		System.out.println(indentedWrapped(
				"byte: ", bytecode, WRAP_SIZE));
	}
	
	// utility methods
	
	/**
	 * @return a string constructed of spaces in the length of the given string.
	 */
	private static String indent(String s) {
		String res = "";
		int size = s.length();
		for (int i = 0; i < size; i++)
			res += " ";
		return res;
	}
	
	/**
	 * @return an indented wrapped version of the input string, with the given
	 * prefix at the beginning of the first line of the string.
	 */
	private static String indentedWrapped(String prefix, String s, int wrap) {
		String res = prefix;
		String ind = indent(prefix);
		int lim = s.length() / wrap;
		for (int i = 0; i < lim; i++) {
			res += s.substring(0, wrap) + "\n" + ind;
			s = s.substring(wrap, s.length());
		}
		res += s;
		return res;
	}
}
