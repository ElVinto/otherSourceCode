package routing;

import java.io.IOException;

public interface BasicRoutingInterface {

	public void readMap(String fName) throws IOException;

	public void writeMap(String fName) throws IOException;

	/**
	 * @param s a source location node id
	 * @param d a destination location node id
	 * @return the distance in meters from x to y
	 */
	public double distance(int s, int d);

	/**
	 * @param label_s a source location
	 * @param label_d a destination location
	 * @return the distance in meters from x to y
	 */
	public double distance(String label_s, String label_d);

	/**
	 * @param x a source location node id
	 * @param y a destination location node id
	 * @return the time in seconds from x to y
	 */
	public int time(int s, int d);

	/**
	 * @param label_s a source location
	 * @param label_d a destination location
	 * @return the time in seconds from x to y
	 */
	public int time(String label_s, String label_d);


}
