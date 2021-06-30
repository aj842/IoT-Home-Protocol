/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: RandomHomeGenerator.java
  
  Purpose:
  Implements HomeGenerator and provides functionality for generating a random
  home with devices. Used for demonstration and testing purposes.
 */

package devices;

import java.util.Random;

public class RandomHomeGenerator implements HomeGenerator {
	
	//Random room names for device name generation
	private static final String[] ROOMS = new String[]{
		"bedroom",
		"kitchen",
		"bathroom",
		"dining",
		"living",
		"garage",
		"closet",
		"hallway",
		"basement",
		"laundry",
                "study"
	};
	
	//Maximum number of devices to allow per device type
	private int maxDevicesPerType = 5;
	//Random number generator
	private final Random rand;
	
	/*
	  Default constructor for random home generator, using the current time as
	  random seed.
	 */
	public RandomHomeGenerator() {
		this.rand = new Random(System.currentTimeMillis());
	}
	
	//Constructs a new random home generator from the given seed
	public RandomHomeGenerator(long seed) {
		this.rand = new Random(seed);
	}
	
	/*
	  Constructs a new random home generator from the given seed and number of
	  devices limit.
	 */
	public RandomHomeGenerator(long seed, int maxDevicesPerType) {
		this(seed);
		this.maxDevicesPerType = maxDevicesPerType;
	}
	
	//@return a randomly generated home
	public Home createHome() {
		// initialize an empty home
		Home home = new Home();
		
		// add lights
		int numLights = 1 + rand.nextInt(maxDevicesPerType);
		for (int i = 0; i < numLights; i++) {
			LightState s = (rand.nextInt(2) == 0 ? LightState.OFF : LightState.ON);
			Light d = new Light(room() + " light", (byte) i, s);
			try {
				d.dim((byte) rand.nextInt(256));
			} catch (Exception e) {}
			home.addDevice(d);
		}
		// add Blinds
		int numBlinds = 1 + rand.nextInt(maxDevicesPerType);
		for (int i = 0; i < numBlinds; i++) {
			BlindState s = (rand.nextInt(2) == 0 ? BlindState.UP
					: BlindState.DOWN);
			Blind d = new Blind(room() + " blind", (byte) i, s);
			try {
				d.dim((byte) rand.nextInt(256));
			} catch (Exception e) {}
			home.addDevice(d);
		}
		// add Thermostats
		int numThermos = 1 + rand.nextInt(maxDevicesPerType);
		for (int i = 0; i < numThermos; i++) {
			ThermoStatState s = (rand.nextInt(2) == 0 ? ThermoStatState.OFF
					: ThermoStatState.ON);
			ThermoStat d = new ThermoStat(room() + " thermo", (byte) i, s);
			try {
				d.setTemp((byte) rand.nextInt(256));
			} catch (Exception e) {}
			home.addDevice(d);
		}
		// add TVs
		int numTVs = 1 + rand.nextInt(maxDevicesPerType);
		for (int i = 0; i < numTVs; i++) {
			TVState s = (rand.nextInt(2) == 0 ? TVState.OFF
					: TVState.ON);
			TV d = new TV(room() + " tv", (byte) i, s);
			try {
				d.setChannel((byte) rand.nextInt(256));
				d.setVolume((byte) rand.nextInt(256));
			} catch (Exception e) {}
			home.addDevice(d);
		}
		// add Smart Locks
		int numSLocks = 1 + rand.nextInt(maxDevicesPerType);
		for (int i = 0; i < numSLocks; i++) {
			int stateInt = rand.nextInt(3);
			SLockState s = SLockState.OFF;
			switch (stateInt) {
			case 1:
				s = SLockState.ON;
				break;
			case 2:
				s = SLockState.ARMED;
				break;
			}
			SLock d = new SLock(room() + " slock", (byte) i, s);
			home.addDevice(d);
		}
		
		return home;
	}
	
	//@return a randomly chosen room, using the random generator
	private String room() {
		return ROOMS[rand.nextInt(ROOMS.length)];
	}
	
	//Main method for testing purposes
	public static void main(String[] args) {
		Home h = new RandomHomeGenerator().createHome();
		h.customPrint();
	}
}
