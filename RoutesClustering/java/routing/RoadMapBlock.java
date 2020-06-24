package routing;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.Serializable;

//import org.insight_centre.ride_matching.stats.StatOnFindMatches;
import tools.ObjectSerializer;
import tools.TimeManager;

/**
 * ©Insight Centre<br>
 * Created on: 8 December 2014
 *
 * @author jhoran
 *
 *
 * http://en.wikipedia.org/wiki/Solution_of_triangles#Solving_spherical_triangles
 *
 * TODO: litle bug on california map
 *  ride start road node == ride destination road node
 *  	ignoring sched 016582c427d74e21d45c64d9ef45acf3c91952bc98992d0791e79170 start:35.99596,-115.10312 dest:36.10977,-115.15445
 *	ride start road node == ride destination road node
 *  		ignoring sched c2529b025008ca42cebe9a2cd7446b151d4cae84964ab0a855215729 start:39.18767,-119.76875 dest:39.2453,-119.94631
 *
 * TODO: Consider using https://developers.google.com/maps/documentation/utilities/polylinealgorithm to store the coordinates.  Will save space, but not
 *             sure if it is worth the processing time. Probably not
 *
 * TODO: Convert block coords to ints
 */

public class RoadMapBlock extends RoadMap {

	//0.15 seems to give a better value though.
	//===================================================================================
	//Tuning variables
	/**
	 * The number of degrees that make up a block
	 */
	private static final double BLOCK_SIZE = 0.1;

	/**
	 * Value to control how close you need to be to a border between two blocks, before it is just assumed to be inside.
	 * This is needed to compensate for floating point errors when a query point lands right on a border.
	 */
	private static final double DIST_EPISLON = 1e-10;
	//===================================================================================


	//These variables will be computed automatically based of the value of BLOCK_SIZE
	private static final double BLOCK_POWER, MAX_BLOCK_RADIUS;

	final TIntDoubleHashMap blkId2Lat, blkId2Lon;
	final TIntObjectHashMap<TIntArrayList> blkId2Nodes;

	static {
		int power = 1;
		while(1 > (BLOCK_SIZE * power)) power *= 10;
		BLOCK_POWER = power;

		MAX_BLOCK_RADIUS = angularDistGPSCoordinates(0, 0,
				BLOCK_SIZE/2, BLOCK_SIZE/2);
	}

	private static final double RIGHT_ANGLE = Math.PI / 2;
	private static final double RAD_PER_DEG = Math.PI / 360;

	/*
	 * Internal helper class to serialize the blocks without re-serializing the data held in RoadMap.
	 */
	private static class BlockHolder implements Serializable {
		private static final long serialVersionUID = (long) (-8174876591630909L * BLOCK_SIZE + RoadMapBlock.serialVersionUID);

		TIntDoubleHashMap blkId2Lat, blkId2Lon;
		TIntObjectHashMap<TIntArrayList> blkId2Nodes;
	}

	/**
	 * @param osmFolderName the location of the osm data
	 * @param mapName the name of the map
	 * @param outFolderName folder to save serialized data.  Will be created if it doesn't exist
	 * @throws ClassNotFoundException if there is an error deserializing data
	 * @throws IOException if there is an error deserializing data
	 */
	public RoadMapBlock(String osmFolderName, String mapName) throws ClassNotFoundException, IOException {
		super(osmFolderName, mapName);

		BlockHolder hold = null;

		hold = new BlockHolder();
		hold.blkId2Lat = new TIntDoubleHashMap();
		hold.blkId2Lon = new TIntDoubleHashMap();
		hold.blkId2Nodes = new TIntObjectHashMap<TIntArrayList>();

		initBlkMap(nodeId2Lat, nodeId2Lon, hold.blkId2Lat, hold.blkId2Lon, hold.blkId2Nodes);


		blkId2Lat = hold.blkId2Lat;
		blkId2Lon = hold.blkId2Lon;
		blkId2Nodes = hold.blkId2Nodes;
	}

	public RoadMapBlock(String serializedRoadMapFileName) throws FileNotFoundException, ClassNotFoundException, IOException{
		super();
		RoadMapBlock rm = (RoadMapBlock) ObjectSerializer.readSerializable( serializedRoadMapFileName);
		roadMap2DistCostTime = rm.roadMap2DistCostTime;
		nodeId2Lat = rm.nodeId2Lat ;
		nodeId2Lon = rm.nodeId2Lon;
		minLat = rm.getMinLat();
		maxLat=rm.getMaxLat();
		minLon=rm.getMinLon();
		maxLon=rm.getMaxLon();

		initMeters();


		roadMap2DistCostTime = rm.roadMap2DistCostTime;
		nodeId2Lat = rm.nodeId2Lat ;
		nodeId2Lon = rm.nodeId2Lon;

		blkId2Lat = rm.blkId2Lat;
		blkId2Lon = rm.blkId2Lon;
		blkId2Nodes = rm.blkId2Nodes;
	}

	/**
	 * @param nodeId2Lat the populated map translating the nodeId to the latitude
	 * @param nodeId2Lon the populated map translating the nodeId to the longitude
	 * @param blkId2Lat an empty map where the latitude of the blocks will be placed
	 * @param blkId2Lon an empty map where the longitude of the blocks will be placed
	 * @param blkId2Nodes an empty map that will contain all the nodes IDs in each block
	 */
	public static void initBlkMap(TIntDoubleHashMap nodeId2Lat,
			TIntDoubleHashMap nodeId2Lon, TIntDoubleHashMap blkId2Lat,
			TIntDoubleHashMap blkId2Lon, TIntObjectHashMap<TIntArrayList> blkId2Nodes) {
		int blkIdCounter = 0;

		TDoubleObjectHashMap<TIntArrayList> blkLat2NodeId = new TDoubleObjectHashMap<TIntArrayList>();

		TIntDoubleIterator itNodeId2Lat = nodeId2Lat.iterator();
		for(int i = nodeId2Lat.size(); i-- > 0; ) {
			itNodeId2Lat.advance();

			int nodeID = itNodeId2Lat.key();
			double nLat = itNodeId2Lat.value();
			double nLon = nodeId2Lon.get(nodeID);

			double blkLat = getBlockValue(nLat);
			double blkLon = getBlockValue(nLon);

			int curBlkId = -1;

			TIntArrayList blkOnLat = blkLat2NodeId.get(blkLat);
			if(blkOnLat == null) {
				blkOnLat = new TIntArrayList();
				blkLat2NodeId.put(blkLat, blkOnLat);
			} else { //No point in calling the iterator if the list was just created after all
				TIntIterator itBlkOnLat = blkOnLat.iterator();
				for(int j = blkOnLat.size(); j-- > 0;) {
					int id = itBlkOnLat.next();
					if(blkId2Lon.get(id) == blkLon){
						curBlkId = id;
						break;
					}
				}
			}

			TIntArrayList curNodes;

			if(curBlkId == -1) { //Time to create a new block
				curBlkId = blkIdCounter ++;

				blkId2Lat.put(curBlkId, blkLat);
				blkId2Lon.put(curBlkId, blkLon);
				blkId2Nodes.put(curBlkId, curNodes = new TIntArrayList());

				blkOnLat.add(curBlkId);
			} else {
				curNodes = blkId2Nodes.get(curBlkId);
			}

			curNodes.add(nodeID);
		}
	}

	/**
	 * @param val a double value representing one part of a GPS coordinate
	 * @return the coordinate that represents the lower left hand corner of the block of GPS nodes
	 */
	private static double getBlockValue(double val) {
		return (Math.floor(val / BLOCK_SIZE) * (BLOCK_POWER*BLOCK_SIZE)) / BLOCK_POWER;
	}

	/**
	 * @param lat the latitude of the search point
	 * @param lon the longitude of the search point
	 * @return the nodeId of the closest node to this point
	 *
	 * @deprecated for testing purposes only, directly calls {@link RoadMap#closestRoadMapNode(double, double)}
	 * 			use {@link #closestRoadMapNode(double, double)} instead.
	 */
	@Deprecated
	public int closestRoadMapNodeBase(double lat, double lon){
		return super.closestRoadMapNode(lat, lon);
	}

	/* (non-Javadoc)
	 * @see rideSchedules.RoadMap#closestRoadMapNode(double, double)
	 */
	@Override
	public int closestRoadMapNode(double lat, double lon) {
		long time_algo = TimeManager.getCpuTimeInNano();
		nbCalls_closestRoadMapNode++;
		//Find closest block;
		double minBlkDist = Double.MAX_VALUE;
		int closestBlk = -1;

		//Just a time saving thing seeing we will need this value a lot.
		double cLat = Math.cos(lat * RAD_PER_DEG * 2);

		TIntDoubleHashMap block2DistFromTarget = new TIntDoubleHashMap(); //to skip having to recompute the angular distance

		TIntDoubleIterator itBlkId2Lat = blkId2Lat.iterator();
		for(int i = blkId2Lat.size(); i-- > 0;){
			itBlkId2Lat.advance();
			int blkId = itBlkId2Lat.key();
			//Getting the distance from the midpoint provides a better upperbound
			double dist = angularDistGPSCoordinates(lat, lon,
					itBlkId2Lat.value()+BLOCK_SIZE*0.5, blkId2Lon.get(blkId)+BLOCK_SIZE*0.5, cLat);
			block2DistFromTarget.put(blkId, dist);
			if(minBlkDist > dist){
				minBlkDist = dist;
				closestBlk = blkId;
			}
		}
		//Find closest node in block
		double minDist = Double.MAX_VALUE;
		int closestNode = -1;
		TIntArrayList blk = blkId2Nodes.get(closestBlk);
		TIntIterator itBlk = blk.iterator();
		for(int i = blk.size(); i-- > 0;){
			int nodeId = itBlk.next();
			double dist = angularDistGPSCoordinates(lat, lon,
					nodeId2Lat.get(nodeId), nodeId2Lon.get(nodeId), cLat);
			if(minDist > dist){
				minDist = dist;
				closestNode = nodeId;
			}
		}

		//Find other nodes within range
		itBlkId2Lat = blkId2Lat.iterator();
		for(int i = blkId2Lat.size(); i-- > 0;) {
			itBlkId2Lat.advance();

			int blkId = itBlkId2Lat.key();
			if(blkId == closestBlk) continue; //Skip starting block
			if(block2DistFromTarget.get(blkId) >  MAX_BLOCK_RADIUS + minDist)
				continue;//If the block is outside the maximum distance, then skip

			double blkLat = itBlkId2Lat.value();
			double blkLon = blkId2Lon.get(blkId);

			if(intersection(lat, lon, minDist, blkLat, blkLon, cLat)) {

				blk = blkId2Nodes.get(blkId);
				itBlk = blk.iterator();
				for(int j = blk.size(); j-- > 0;){
					int nodeId = itBlk.next();
					double dist = angularDistGPSCoordinates(lat, lon,
							nodeId2Lat.get(nodeId), nodeId2Lon.get(nodeId), cLat);
					if(minDist > dist){
						minDist = dist;
						closestNode = nodeId;
					}
				}
			}
		}
		time_closestRoadMapNode_ns += TimeManager.getCpuTimeInNano() - time_algo;
		long timeTaken = TimeManager.getCpuTimeInNano() - time_algo;
//		StatOnFindMatches.totalRMNodeGetTime += timeTaken;
		return closestNode;
	}

	/**
	 * Checks to see if a block with a index point in the lower left button intersects with a cirle
	 * with a given radius and starting point.
	 *
	 * @param cx latitude of circle starting point
	 * @param cy longitude of circle stopping point
	 * @param r distance to closest node (in angular units)
	 * @param sx latitude of lower left corner of block
	 * @param sy longitude of lower left corner of block
	 * @param skip skip distance check to radius
	 * @param ccX cosine of the cx coordinate.  To skip having to recompute it.
	 * @return if the circle will intersect the block at any point
	 */
	private static boolean intersection(double cx, double cy, double r, double sx, double sy, double ccX) {
		//Check the midpoint
		//Done outside this method so cached check can be used instead
		/*
		double distToCenter = angularDistGPSCoordonates(cx, cy, sx + BLOCK_SIZE/2, sy + BLOCK_SIZE/2);
		if(distToCenter > MAX_BLOCK_RADIUS + r)
			return false;
		*/

		//Get the distance to each of the corners; checking each corner after calculation because if any corner
		//is within circle, then no point continuing
		double[] cornerDists = new double[4];
		cornerDists[0] = angularDistGPSCoordinates(cx, cy, sx, sy, ccX);
		if(cornerDists[0] < r) return true;
		cornerDists[1] = angularDistGPSCoordinates(cx, cy, sx, sy + BLOCK_SIZE, ccX);
		if(cornerDists[1] < r) return true;
		cornerDists[2]= angularDistGPSCoordinates(cx, cy, sx + BLOCK_SIZE, sy, ccX);
		if(cornerDists[2] < r) return true;
		cornerDists[3] = angularDistGPSCoordinates(cx, cy, sx + BLOCK_SIZE, sy + BLOCK_SIZE, ccX);
		if(cornerDists[3] < r) return true;

		//Get the closest corner; the side of the block facing the point must at least encompass this corner
		int firstCorner = 0, secondCorner;
		for(int i = 1; i < 4; i++)
			if(cornerDists[i] < cornerDists[firstCorner])
				firstCorner = i;

		double c1_lat = sx + (firstCorner == 2 || firstCorner == 3 ? BLOCK_SIZE : 0);
		double c1_lon = sy + (firstCorner == 1 || firstCorner == 3 ? BLOCK_SIZE : 0);

		double op_lat = sx + (firstCorner < 2 ? BLOCK_SIZE : 0);
		double op_lon = sy + (firstCorner % 2 == 0 ? BLOCK_SIZE : 0);

		//Because we dealing with GPS coordinates mapped onto a sphere, finding the side of the box facing
		//the point isn't always represented by the two closest corners, as it would be in eclidian space.
		//So instead we check the two corners next to the closest corner to see if either of them make
		//a side facing the block.  If neither of them do, and it hasn't already swept the corner, then it must be false.

		boolean betweenLat = between(cx, c1_lat, op_lat);
		boolean betweenLon = between(cy, c1_lon, op_lon);
		/* Both of these conditions being true means that the point lies within the area claimed by the box.  Normally
		 * this shouldn't be the case, however as the boxes aren't equally sized it is possible for the center of the
		 * box above to be closer than the point the box actually falls in.  So when the initial box isn't the one
		 * claimed by this point, when we are presented by this box, it will always have to be checked.
		 */
		if(betweenLat && betweenLon)
			return true;
		if(betweenLat)
			secondCorner = (firstCorner + 2) %4;
		else if(betweenLon)
			secondCorner = firstCorner +(firstCorner % 2 == 0 ? 1 : -1);
		else return false;

		//Get the latitude and longitude of the two corners
		double c1_dist = cornerDists[firstCorner];
		double c2_dist = cornerDists[secondCorner];
		double c2_lat = sx + (secondCorner == 2 || secondCorner == 3 ? BLOCK_SIZE : 0);
		double c2_lon = sy + (secondCorner == 1 || secondCorner == 3 ? BLOCK_SIZE : 0);

		double c1_dist_cos = Math.cos(c1_dist);
		//
		double c2_dist_cos = Math.cos(c2_dist);
		double c2_dist_sin = Math.sin(c2_dist);

		//Can't pre-compute distance between corners, as distance changes in different latitudes
		double distBetwCorners = angularDistGPSCoordinates(c1_lat, c1_lon, c2_lat, c2_lon);
		double distBetwCorners_sin = Math.sin(distBetwCorners),
			   distBetwCorners_cos = Math.cos(distBetwCorners);

		//Check to see if it is possible to form a right angled triangle.  If the radius didn't encompass any of the corners,
		//and it doesn't form a right angle, then no point continuing.  This only really arises because we aren't dealing with
		//actual squares, so the initial radius check encompasses area outside the corners of the "square".  Also fail if it
		//forms a perfect right angle, as that case is already covered when it is checked to see if the corner lies inside the
		//radius.

		//No need to actually check this anymore, because all cases are covered by either checking if the radius encompasses
		//corner, if the point is facing a side.

		double c1_angle = Math.acos( (c1_dist_cos - (distBetwCorners_cos * c2_dist_cos))/
				(distBetwCorners_sin * c2_dist_sin) );
//<<<<<<< HEAD

		//angle can become NAN if it is really close to the edge of the line.  Try and calculate the opposite angle and
		//if it still returns NAN, then just assume true.  Better to have false positives than false neg here.
		if(Double.isNaN(c1_angle)){
			double c1_dist_sin = Math.sin(c1_dist);
			double c2_angle = Math.acos( (c2_dist_cos - (distBetwCorners_cos * c1_dist_cos))/
					(distBetwCorners_sin * c1_dist_sin) );
			if(Double.isNaN(c2_angle)) return true;

			if(c2_angle >= RIGHT_ANGLE) return false;
			double distIntersection = Math.asin(c2_dist_sin * Math.sin(c1_angle));
			if(Double.isNaN(distIntersection)) return true;
			return distIntersection < r;
		}

		if(c1_angle >= RIGHT_ANGLE) return false;

		double distIntersection = Math.asin(c2_dist_sin * Math.sin(c1_angle));
		if(Double.isNaN(distIntersection)) return true;
		return distIntersection < r;

	}

	/**
	 * @param latX latitude of the first coordinate
	 * @param lonX longitude of the first coordinate
	 * @param latY latitude of the second coordinate
	 * @param lonY longitude of the second coordinate
	 * @return angular distance between the two points in radians
	 */
	public static double angularDistGPSCoordinates(double latX, double lonX, double latY, double lonY){
		double cLatX = latX * RAD_PER_DEG * 2;
		cLatX = Math.cos(cLatX);

		return angularDistGPSCoordinates(latX, lonX, latY, lonY, cLatX);
	}

	/**
	 * @param latX latitude of the first coordinate
	 * @param lonX longitude of the first coordinate
	 * @param latY latitude of the second coordinate
	 * @param lonY longitude of the second coordinate
	 * @param cLatX cosine of the latX value in radians.  Just to skip having to recompute it every time
	 * @return angular distance between the two points in radians
	 */
	private static double angularDistGPSCoordinates(double latX, double lonX, double latY, double lonY, double cLatX) {
		double dLat = (latX-latY) * RAD_PER_DEG;
		double dLon = (lonX-lonY) * RAD_PER_DEG;
		double cLatY = Math.cos(latY * RAD_PER_DEG * 2);

		double sHfLat = Math.sin(dLat);
		double sHfLon = Math.sin(dLon);

		double a = sHfLat * sHfLat + sHfLon * sHfLon * cLatX * cLatY;
		return 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	}


	/**
	 * Simple method to see if one value lies between two others.  Doesn't require any ordering on a or b
	 * @param t the testing value
	 * @param a one of the values that t will be tested against
	 * @param b one of the values that t will be tested against
	 * @return true if t is between a and b
	 */
	private static boolean between(double t, double a, double b){
		if(t < a && t > b) return true;
		if(t > a && t < b) return true;

		//Floating point errors are a bit of an issue at times, so if it is right on the line (or close enough to be within a margin of error)
		//then assume true;
		if(Math.abs(t - a) < DIST_EPISLON || Math.abs(t - b) < DIST_EPISLON)
			return true;

		return false;
	}

	public static double angleBetweenLines(double latA, double lonA, double latB, double lonB, double latC, double lonC, double latD, double lonD){
		double lenAB = angularDistGPSCoordinates(latA, lonA, latB, lonB);
		double lenAC = angularDistGPSCoordinates(latA, lonA, latC, lonC);
		double lenBC = angularDistGPSCoordinates(latB, lonB, latC, lonC);
		double lenBD = angularDistGPSCoordinates(latB, lonB, latD, lonD);
		double lenCD = angularDistGPSCoordinates(latC, lonC, latD, lonD);
		double lenAD = angularDistGPSCoordinates(latA, lonA, latD, lonD);

		double  cosAB = Math.cos(lenAB), sinAB = Math.sin(lenAB),
				cosBD = Math.cos(lenBD),
				cosAD = Math.cos(lenAD), sinAD = Math.sin(lenAD),
				cosAC = Math.cos(lenAC), sinAC = Math.sin(lenAC),
				cosBC = Math.cos(lenBC),
				cosCD = Math.cos(lenCD), sinCD = Math.sin(lenCD);

		double angleBAD = Math.acos(
				(cosBD - (cosAB * cosAD))/
				(sinAB * sinAD)
		);
		double angleBAC = Math.acos(
				(cosBC - (cosAB * cosAC))/
				(sinAB * sinAC)
		);

		if(lenAC == 0) return lenBD == 0 ? 0 : angleBAD;
		if(lenAD == 0) return lenBC == 0 ? 0 : angleBAC;

		double angle1, angle2, cosLen;
		if(angleBAD > angleBAC){ //Chosen D as one corner of the triangle
			angle1 = angleBAD;
			double angleBDC = Math.acos(
					(cosBC - (cosBD * cosCD))/
					(Math.sin(lenBD) * sinCD)
			);

			double angleADC = Math.acos(
					(cosAC - (cosAD * cosCD))/
					(sinAD * sinCD)
			);

			if(angleBDC > angleADC){
				angle2 = angleBDC;
				cosLen = cosBD;
			} else {
				angle2 = angleADC;
				cosLen = cosAD;
			}
		} else { //Choose C as one corner
			angle1 = angleBAC;

			double angleACD = Math.acos(
					(cosAD - (cosAC * cosCD))/
					(sinAC * sinCD)
			);

			double angleBCD = Math.acos(
					(cosBD - (cosBC * cosCD))/
					(Math.sin(lenBC) * sinCD)
			);

			if(angleBCD > angleACD){//choose b as other corner
				angle2 = angleBCD;
				cosLen = cosBC;
			} else {//choose a as other corner
				angle2 = angleACD;
				cosLen = cosAC;
			}
		}

		if(angle1 == Double.NaN && angle2 == Double.NaN) return 0;
		if(angle1 == Double.NaN) return angle2;
		if(angle2 == Double.NaN) return angle1;


		if(angle1 > RIGHT_ANGLE || angle2 > RIGHT_ANGLE){
			angle1 = Math.PI - angle1;
			angle2 = Math.PI - angle2;
		}

		double angle3 = Math.acos(
				(Math.sin(angle1) * Math.sin(angle2) * cosLen) -
				(Math.cos(angle1) * Math.cos(angle2))
		);

		double latT = latA + (latD - latC), lonT = lonA + (lonD - lonC);
		double lenTA = RoadMapBlock.angularDistGPSCoordinates(latA, lonA, latT, lonT);
		double lenTB = RoadMapBlock.angularDistGPSCoordinates(latB, lonB, latT, lonT);
		double angleT = Math.acos(
				(Math.cos(lenTB) - (cosAB * Math.cos(lenTA)))/
				(sinAB * Math.sin(lenTA))
		);

		//angleBAD > angleBAC then diverging
		return angleT > RIGHT_ANGLE ? Math.PI - angle3 : angle3;
	}

	/*
	public static double angleBetweenLines(double latA, double lonA, double latB, double lonB, double latC, double lonC, double latD, double lonD){


		double t;
		//Between the two lines, get the two points with the lowest latitude;
		if(latA > latB){
			t = latB;
			latB = latA;
			latA = t;
			t = lonB;
			lonB = lonA;
			lonA = t;
		}
		if(latC > latD){
			t = latD;
			latD = latC;
			latC = t;
			t = lonD;
			lonD = lonC;
			lonC = t;
		}

		double lenAB = angularDistGPSCoordinates(latA, lonA, latB, lonB);
		double lenAC = angularDistGPSCoordinates(latA, lonA, latC, lonC);
		double lenBC = angularDistGPSCoordinates(latB, lonB, latC, lonC);
		double lenCD = angularDistGPSCoordinates(latC, lonC, latD, lonD);
		double lenAD = angularDistGPSCoordinates(latA, lonA, latD, lonD);

		if(lenAC == 0){
			double lenBD = angularDistGPSCoordinates(latB, lonB, latD, lonD);
			if(lenBD == 0) return 0; //superimposed
			double angleC = Math.acos(
					(Math.cos(lenBD) - (Math.cos(lenAB) * Math.cos(lenCD)))/
					(Math.sin(lenAB) * Math.sin(lenCD))
			);
			return angleC;
		}

		double cosAC = Math.cos(lenAC);
		double sinAC = Math.sin(lenAC);

		double angleA = Math.acos(
				(Math.cos(lenBC) - (cosAC * Math.cos(lenAB)))/
				(sinAC * Math.sin(lenAB))
		);

		double angleB = Math.acos(
				(Math.cos(lenAD) - (cosAC * Math.cos(lenCD)))/
				(sinAC * Math.sin(lenCD))
		);

		//if one line is pointing directly at the other;
		if(angleA == Double.NaN && angleB == Double.NaN) return 0;
		if(angleA == Double.NaN) return angleB;
		if(angleB == Double.NaN) return angleA;

		if(angleA > RIGHT_ANGLE) angleA = RIGHT_ANGLE - angleA;
		if(angleB > RIGHT_ANGLE) angleB = RIGHT_ANGLE - angleB;

		double angleC = Math.acos(
				(Math.sin(angleA) * Math.sin(angleB) * cosAC) -
				(Math.cos(angleA) * Math.cos(angleB))
		);

		return angleC;
	} */

	private static final long serialVersionUID = -570778L + ObjectStreamClass.lookup(RoadMap.class).getSerialVersionUID();

}
