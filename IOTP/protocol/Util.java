/* 
CS544 - Computer Networks
 Drexel University
 Protocol Implementation: IoT Home Control Protocol
 Abhilasha Jayaswal

 
 File name: Util.java
  
 Purpose:
 Utility methods unrelated to any particular part of the protocol. Include
 procedures for string to byte-stream conversion (and vice-versa), date and
 time etc.
 */

package protocol;

import java.text.SimpleDateFormat;
import java.util.*;

public class Util {

	/*
          Returns concats the given character to the given name until reached the
	  given string length.
	 */
	public static String bufferLeft(char c, int i, String name) {
		while (name.length() < i) {
			name = c + name;
		}
		return name;
	}

	/*
          @param stream a stream of bytes
	  @return the hexadecimal representation of the stream of bytes.
	 */
	public static String toHexString(byte[] stream) {
		String res = "";
		for (byte b: stream) {
			res += String.format("%02x ", b);
		}
		res = res.substring(0, res.length() - 1);
		return res;
	}
	
	/*
	  @param hexStr hexadecimal space-delimited representation of a byte stream.
	  @return the actual byte stream.
	 */
	public static byte[] toByteStream(String hexStr) {
		String[] bytes = hexStr.split(" ");
		byte[] res = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++)
			res[i] = (byte) Integer.parseInt(bytes[i], 16);
		return res;
	}
	
	/*
	  @return a list of bytes constructed from the given array of bytes.
	 */
	public static List<Byte> toByteList(byte[] bytes) {
		List<Byte> l = new ArrayList<Byte>();
		for (int i = 0; i < bytes.length; i++) {
			l.add(bytes[i]);
		}
		return l;
	}

	/*
	  @return the given stream of bytes with the ordinal and parameters
	  concatenated to it.
	 */
	public static byte[] cat(byte[] bytes, byte ordinal, byte[] params) {
		byte b[] = new byte[bytes.length + 1 + params.length];
		// name
		int i = 0;
		for (; i < bytes.length; i++) {
			b[i] = bytes[i];
		}
		// state
		b[i] = ordinal;
		i++;
		// parameters
		int j = 0;
		for (; i < b.length; i++) {
			b[i] = params[j];
			j++;
		}
		return b;
	}
	
	// date and time
	
	private static SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
	private static SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	private static Calendar cal;

	//@return The current date.
	public static String date() {
		cal = Calendar.getInstance();
		return df.format(cal.getTime());
	}
	
	//@return The current time.
        
	public static String time() {
		cal = Calendar.getInstance();
		return tf.format(cal.getTime());
	}
	
	//@return The current date and time.
	public static String dateTime() {
		return date() + " " + time();
	}
}
