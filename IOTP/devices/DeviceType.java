/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: DeviceType.java
  
  Purpose:
  Enumerator for supported device types.
 */

package devices;

//Enumerator for known device types
public enum DeviceType {
	LIGHT	((byte) 0, 1),
	BLIND	((byte) 1, 1),
	THERMO	((byte) 2, 1),
	TV		((byte) 3, 2),
	SLOCK	((byte) 4, 0);
	//NO_SUCH_DEVICE((byte) -1, null);//NO_SUCH_DEVICE((byte) -1, null);
	
	private DeviceType(byte type, int numParams) {
		this.type = type;
		this.numParams = numParams;
	}
	
	// fields
	private byte type;
	private int numParams;
	
	// getters
	
	//@return the type byte code
	public byte type() {
		return type;
	}
	
	//@return the number of parameters related to the device
	public int numParams() {
		return numParams;
	}
	
	public static DeviceType[] legalValues() {
		return new DeviceType[]{
			LIGHT,BLIND,THERMO,TV,SLOCK	
		};
	}
	
	/*
	  @param code byte code for the device type.
	  @return the device type with the given code.
	 */
	public static DeviceType typeFromCode(byte code) throws Exception {
		switch (code) {
		case 0: return LIGHT;
		case 1: return BLIND;
		case 2: return THERMO;
		case 3: return TV;
		case 4: return SLOCK;
		default: throw new Exception("Illegal IoT device type code: " + code);
		}
	}
	
	/*
	  Used internally, identical to typeFromCode but does not throw exceptions.
	  If an illegal code is given, returns LIGHT.
	  @param code byte code for the device type.
	  @return the device type with the given code.
	 */
	protected static DeviceType typeFromCodeSafe(byte code) {
		try {
			return typeFromCode(code);
		} catch (Exception e) {
			return LIGHT;
		}
	}
}