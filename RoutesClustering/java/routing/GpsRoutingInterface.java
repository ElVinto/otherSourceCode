package routing;

public interface GpsRoutingInterface extends BasicRoutingInterface {

	/**
	 * @param lat_x latitude of x
	 * @param lon_x latitude of x
	 * @param lat_y longitude of y
	 * @param lon_y longitude of y
	 * @return the distance in meters from a node x to a node y
	 */
	public double distance(double lat_x, double lon_x, double lat_y, double lon_y);


	/**
	 * @param lat_x latitude of x
	 * @param lon_x latitude of x
	 * @param lat_y longitude of y
	 * @param lon_y longitude of y
	 * @return the time in seconds from a node x to a node y
	 */
	public int time(double lat_x, double lon_x, double lat_y, double lon_y);

}
