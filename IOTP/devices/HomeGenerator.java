/*  
  CS544 - Computer Networks
  Drexel University
  Protocol Implementation: IoT Home Control Protocol
  Abhilasha Jayaswal
 
  File name: HomeGenerator.java
  
  Purpose:
  Interface for home instance generator.
 */

package devices;

public interface HomeGenerator {
	
	//Returns a generated home object
	public Home createHome();
}
