/**
 * @author Vincent Armant, Post-Doctoral Researcher
 * Copyright (c) 2013,2014. The Insight Centre for Data Analytics, University College Cork, Ireland.
 *  All rights reserved.
 */
package routing;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.PriorityQueue;
import java.util.Random;


import tools.FileManager;
import tools.ObjectSerializer;
import tools.TimeManager;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

/*
 * Move  reachableArea(int u, int s, int l, RoadMap rm, boolean outerShell) in RMSearch here
 *
 * Put the intersectio of 2 areas or perimeters here.
 * 	intersectinPerim(List perim1, list perim2)
 * 	intersectingArea(List area1, list list2)
 * 	// intersectinPerim = intersectingArea iif the diameters are equals
 *
 */

public class RoadMap extends ARoadMap implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * roadMap2DistCostTime.get(node_i).get(node_j).get(0) -> cm
	 * roadMap2DistCostTime.get(node_i).get(node_j).get(1) -> milli euro
	 * roadMap2DistCostTimeget(node_i).get(node_j).get(2) -> secondes
	 *
	 * TODO write 3 accessors
	 * 	getDist(node_i,node_j) -> dist in cm EQUIV TO  roadMap2DistCostTime.get(node_i).get(node_j).get(0)
	 *  getDist(node_i,node_j) -> cost in milli euro EQUIV TO  roadMap2DistCostTime.get(node_i).get(node_j).get(1)
	 *  getDist(node_i,node_j) -> time in sec EQUIV TO  roadMap2DistCostTime.get(node_i).get(node_j).get(2)
	 *
	 * Refactoring steps:
	 *  1) make roadMap2DistCostTime private
	 *  2) Use the accessors;
	 *
	 */
	public TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> roadMap2DistCostTime;



	protected TIntDoubleHashMap nodeId2Lat;
	protected TIntDoubleHashMap nodeId2Lon;

	protected double minLat;

	protected double maxLat;

	protected double minLon;

	protected double maxLon;
	protected String mapName;

	public static enum ShortestDistSearch{DIKJSTRA,ASTAR};
	public ShortestDistSearch shortestDistSearch = ShortestDistSearch.DIKJSTRA;

	protected RoadMap(){
		initMeters();
	}

	public RoadMap(String osmFolderName, String mapName) throws IOException {

		roadMap2DistCostTime = new TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>>();
		nodeId2Lat = new TIntDoubleHashMap() ;
		nodeId2Lon = new TIntDoubleHashMap();
		String osmFileName = osmFolderName+mapName+".osm";
		OSMRoadParser.populateEnrichedRoadMapStructures(osmFileName
				,roadMap2DistCostTime
				,nodeId2Lat
				,nodeId2Lon
				);
		initMapExtremeCoordinates();

		initMeters();
	}



	public RoadMap(String serializedRoadMapFileName) throws FileNotFoundException, ClassNotFoundException, IOException{
		RoadMap rm = (RoadMap) ObjectSerializer.readSerializable( serializedRoadMapFileName);
		roadMap2DistCostTime = rm.roadMap2DistCostTime;
		nodeId2Lat = rm.nodeId2Lat ;
		nodeId2Lon = rm.nodeId2Lon;
		minLat = rm.getMinLat();
		maxLat=rm.getMaxLat();
		minLon=rm.getMinLon();
		maxLon=rm.getMaxLon();

		initMeters();
	}

	protected void initMeters(){
		time_closestRoadMapNode_ns = 0;
		 nbCalls_closestRoadMapNode = 0;

		 time_dikjstra_ns =0;
		 nbCalls_dikjstra =0;
	}


	private void initMapExtremeCoordinates(){
		minLat =Double.MAX_VALUE;
		maxLat=-Double.MAX_VALUE;
		TIntDoubleIterator itLat = nodeId2Lat.iterator();
		for(int i= nodeId2Lat.size();i-->0;){
			itLat.advance();
			double lat = nodeId2Lat.get(itLat.key());
			if(lat>getMaxLat())
				maxLat=lat;
			if(lat<getMinLat())
				minLat =lat ;
		}

		minLon = Double.MAX_VALUE;
		maxLon =-Double.MAX_VALUE;
		TIntDoubleIterator itLon = nodeId2Lon.iterator();
		for(int i= nodeId2Lon.size();i-->0;){
			itLon.advance();
			double lon = nodeId2Lon.get(itLon.key());
			if(lon>getMaxLon())
				maxLon = lon;
			if(lon<getMinLon())
				minLon = lon ;
		}

	}

	public int getDist(int node_i, int node_j) {
		if( !roadMap2DistCostTime.contains(node_i)  )
			return -1;
		if( !roadMap2DistCostTime.get(node_i).contains(node_j)  )
			return -1;
		return roadMap2DistCostTime.get(node_i).get(node_j).get(0);
	}

	public int getCost(int node_i, int node_j) {
		if( !roadMap2DistCostTime.contains(node_i)  )
			return -1;
		if( !roadMap2DistCostTime.get(node_i).contains(node_j)  )
			return -1;
		return roadMap2DistCostTime.get(node_i).get(node_j).get(1);
	}

	public int getTime(int node_i, int node_j) {
		if( !roadMap2DistCostTime.contains(node_i)  )
			return -1;
		if( !roadMap2DistCostTime.get(node_i).contains(node_j)  )
			return -1;
		return roadMap2DistCostTime.get(node_i).get(node_j).get(2);
	}

	public double getLat(int roadNode){
		long timeLastSchedsAddedInNano = TimeManager.getCpuTimeInNano();
		double res = nodeId2Lat.get(roadNode);
		long timeTaken = TimeManager.getCpuTimeInNano() - timeLastSchedsAddedInNano;
//		StatOnFindMatches.totalLatLonGetTime += timeTaken;
		return res;
	}

	public double getLon(int roadNode){
		long timeLastSchedsAddedInNano = TimeManager.getCpuTimeInNano();
		double res = nodeId2Lon.get(roadNode);
		long timeTaken = TimeManager.getCpuTimeInNano() - timeLastSchedsAddedInNano;
//		StatOnFindMatches.totalLatLonGetTime += timeTaken;
		return res;
	}

	/**
	 * @return the maxLat
	 */
	public double getMaxLat() {
		return maxLat;
	}

	/**
	 * @return the minLat
	 */
	public double getMinLat() {
		return minLat;
	}


	/**
	 * @return the minLon
	 */
	public double getMinLon() {
		return minLon;
	}

	/**
	 * @return the maxLon
	 */
	public double getMaxLon() {
		return maxLon;
	}

	/**
	 * @return the mapName
	 */
	public String getMapName() {
		return mapName;
	}

	/**
	 * return number of nodes
	 */
	public int getNbNodes(){
		return nodeId2Lat.size();
	}

	/**
	 * @return number of Arcs in the road map
	 */
	public int getNbArcs(){
		int nbArcs =0;
		TIntObjectIterator<TIntObjectHashMap<TIntArrayList>> itNodes = roadMap2DistCostTime.iterator();
		for(int i=roadMap2DistCostTime.size();i-->0;){
			itNodes.advance();
			int nodeId = itNodes.key();
			nbArcs += roadMap2DistCostTime.get(nodeId).size();
		}
		return nbArcs;
	}

	/**
	 * @return number of Arcs in the rod map adjacent to a node with a degree greater than 2
	 */
	public int getNbArcsAdjNodeDegGT2(){
		int nbArcs =0;
		TIntObjectIterator<TIntObjectHashMap<TIntArrayList>> itNodes = roadMap2DistCostTime.iterator();
		for(int i=roadMap2DistCostTime.size();i-->0;){
			itNodes.advance();
			int nodeId = itNodes.key();
			if(roadMap2DistCostTime.get(nodeId).size()>2)
				nbArcs += roadMap2DistCostTime.get(nodeId).size();
		}
		return nbArcs;
	}


	/**
	 * @param lat: represents the location latitude
	 * @param lon: represents the location longitude
	 * @return  the closest road map node id
	 */
	public int closestRoadMapNode(double lat, double lon){
		nbCalls_closestRoadMapNode++;
		long algoTime = TimeManager.getCpuTimeInNano();

		double minDist = Double.MAX_VALUE;
		int closestNode = -1;
		TIntDoubleIterator itRoadNodeId = nodeId2Lat.iterator();
		for(int i=nodeId2Lat.size(); i-->0; ){
			itRoadNodeId.advance();
			int roadNodeId = itRoadNodeId.key();
			double dist = OSMRoadParser.distGPSCoordonates(
					nodeId2Lat.get(roadNodeId), nodeId2Lon.get(roadNodeId),
					lat, lon);
			if(minDist>dist){
				minDist = dist;
				closestNode = roadNodeId ;
			}
		}
		algoTime = TimeManager.getCpuTimeInNano()-algoTime;
		time_closestRoadMapNode_ns += algoTime;
//		StatOnFindMatches.totalRMNodeGetTime += algoTime;
		return closestNode;
	}



	/**
	 * @param unit is the unit used to measure the shorter path. When unit =0 (resp 1, 2) the value returned represents a distance in cm ( resp. millieuro, seconde).
	 * @param transpMode represents the type of transport used to traverse the path. When transpMode= 1 (resp. 2 3) the path is traversed by pedestrian (resp. a car, a bus)
	 * @param userStartRMNode represents the node with the coordinate of the the start location.
	 * @param userDestRMNode represents the node with the coordinate of the destination location.
	 * @param path represents the returned path
	 * @param returnScores represents the returned path measured units
	 * @return the measured unit of the shortest path
	 */
	//TODO:rename function
	public int shortestPath(int u, int transpMode , int userStartRMNode, int userDestRMNode, TIntArrayList path, TIntArrayList returnScores){
			int val = -1;
			if(transpMode ==2) // driving
				val = shortestPath(u, userStartRMNode, userDestRMNode, path, returnScores);
			else{ // taking public tranportation or walking
				if(u==1) // the cost of walking is free
					val = 0;
				if(u==0){
					val = shortestPath(u, userStartRMNode, userDestRMNode, path, returnScores);
				}
				if(u==2){
					// transform  distance to  time in for walking or public transit
					int distCm = shortestPath(0, userStartRMNode, userDestRMNode, path, returnScores);
					if(transpMode ==1)
						val= distInCm2WalkingInSec(distCm); // sec
					if(transpMode ==3)
						val= distInCm2PublicTransportTravellingInSec(distCm); // sec
				}
			}

			// It is not correct to remove node with arrity > 2 a passenger can be taken in the subpath
//			if(path != null){
//				TIntIterator itPath = path.iterator();
//				itPath.next(); // Don't remove the first
//				TIntIterator itReturnScores = null;
//				if( returnScores != null) {
//					itReturnScores = returnScores.iterator();
//					itReturnScores.next(); // Don't remove the last in the path but remove the one with arrity >2
//				}
//				for(int i = path.size()-1; i -- > 1; ){// Don't remove the last node in the path
//					int nodeId = itPath.next();
//					if( itReturnScores != null ) {
//						itReturnScores.next();
//					}
//					TIntObjectHashMap<TIntArrayList> neigh = roadMap2DistCostTime.get(nodeId);
//					if(neigh.size() <= 2) {
//						itPath.remove();
//						if( itReturnScores != null)
//							itReturnScores.remove();
//					}
//				}
//
//			}

			return val;
	}

	/**
	 * @param unit is the unit used to measure the shorter path. When unit =0 (resp 1, 2) the value returned represent a distance in cm ( resp. millieuro, seconde).
	 * @param transpMode represents the type of transport used to traverse the path. When transpMode= 1 (resp. 2 3) the path is traverse by pedestrian (resp. a car, a bus)
	 * @param userStartRMNode represents the node with the coordinate the start location.
	 * @param userDestRMNodes represents the nodes with the coordinate the destination location.
	 * @param path represents the returned path
	 * @return the measured unit of the shortest path
	 */
	public int shortestPath(int u, int transpMode , int userStartRMNode, TIntArrayList userDestRMNodes, TIntArrayList path, TIntArrayList returnScores){
			int val = -1;
			if(transpMode ==2) // driving
				val = dijkstra(u, userStartRMNode, userDestRMNodes, path, returnScores);
			else{ // taking public tranportation or walking
				if(u==1) // the cost of walking is free
					val = 0;
				if(u==0){
					val = dijkstra(u, userStartRMNode, userDestRMNodes, path, returnScores);
				}
				if(u==2){
					int distCm = dijkstra(0, userStartRMNode, userDestRMNodes, path, returnScores);
					if(transpMode ==1)
						val= distInCm2WalkingInSec(distCm); // sec
					if(transpMode ==3)
						val= distInCm2PublicTransportTravellingInSec(distCm); // sec
				}
			}
			return val;
	}




	/**
	 * @param distInCm is the traveled distance in centimeters.
	 * @return the walking time in seconds corresponding to distInCm.
	 * The average walking speed considered is 4 km/h.
	 */
	int distInCm2WalkingInSec(int distInCm){
		return (int) Math.round((distInCm/100000.0)*(3600.0/4.0)); // sec
	}

	/**
	 * @param distInCm is the traveled distance in centimeters.
	 * @return the public transport traveling time in seconds corresponding to distInCm.
	 * 	The average public transport speed considered is 30 km/h.
	 */
	int distInCm2PublicTransportTravellingInSec(int distInCm){
		return (int) Math.round((distInCm/100000.0)*(3600.0/30.0)); // sec
	}

	/**
	 * Computes the smallest path in a specified unit using Dikjstra algorithm.
	 * @param unit when equals 0 (resp. 1, 2) represents a distance in cm (resp. cost in milli euros, time in seconds)
	 * @param s represents the source road map node
	 * @param t represents the target road map node
	 * @param returnPath: represents the returned path between s and t if it is not null.
	 * @return the smallest path in the specified unit
	 */
	public  int shortestPath(int u,
			int s,
			int t,
			TIntList returnPath,
			TIntList returnScores
			){

		nbCalls_dikjstra++;
		long algoTime = TimeManager.getCpuTimeInNano();

//		System.err.println("debug shortestPath( u, s, t, returnPath,returnScores)");
		int val =-1;
		if(u==0)
			val = RMSearch.aStar(s, t, this, returnPath, returnScores);
		else
			val = RMSearch.dijkstra(u, s, t, this, returnPath, returnScores);

		algoTime = TimeManager.getCpuTimeInNano()-algoTime;
		time_dikjstra_ns+=algoTime;
		return val;
	}


	public int getPathUnit(int u, TIntList path){
		int pathU = 0;
		for(int iPathNode =0;iPathNode<path.size()-1;iPathNode++){
			int node = path.get(iPathNode);
			int succ =  path.get(iPathNode+1);
			pathU += roadMap2DistCostTime.get(node).get(succ).get(u);
		}
		return pathU;
	}


	/**
	 * Computes the smallest path in a specified unit using Dikjstra algorithm.
	 * @param u: represents the unit measured between the linked road map nodes.
	 * 	  If u equals 0 (resp. 1, 2) the method considers distance in cm (resp. cost in milli euros, time in seconds)
	 * @param s: represents the source road map node
	 * @param t: represents the target road map nodes
	 * @param returnPath: represents the returned path between s and t if it is not null.
	 * @return the smallest path in the specified unit or -1 if no path has been found
	 */
	public  int dijkstra(int u,
			int s,
			TIntCollection t,
			TIntList returnPath,
			TIntList returnScores
			){

		nbCalls_dikjstra++;
		long algoTime = TimeManager.getCpuTimeInNano();

		int val = RMSearch.dijkstra(u, s, t, this, returnPath, returnScores);

		algoTime = TimeManager.getCpuTimeInNano()-algoTime;
		time_dikjstra_ns+=algoTime;
//		StatOnFindMatches.totalDijkstraTime += algoTime;
		return val;
	}

	/**
	 * Computes the smallest path in a specified unit using Dikjstra algorithm.
	 * @param u: represents the unit measured between the linked road map nodes.
	 * 	  If u equals 0 (resp. 1, 2) the method considers distance in cm (resp. cost in milli euros, time in seconds)
	 * @param s: represents the source road map node
	 * @param t: represents the target road map nodes
	 * @param returnPath: represents the returned path between s and t if it is not null.
	 * @return the smallest path in the specified unit
	 */
	public  int invDijkstra(int u,
			TIntCollection s,
			int t,
			TIntList returnPath,
			TIntList returnScores
			){

		nbCalls_dikjstra++;

		long algoTime = TimeManager.getCpuTimeInNano();

		int val = RMSearch.invDijkstra(u, s, t, this, returnPath, returnScores);

		algoTime = TimeManager.getCpuTimeInNano()-algoTime;
		time_dikjstra_ns+=algoTime;
//		StatOnFindMatches.totalInvDijkstraTime += algoTime;
		return val;
	}


	/**
	 * @deprecated use dijkstra(int u, int s, TIntCollection t, TIntList returnPath);
	 *
	 * Computes the smallest path in a specified unit using Dikjstra algorithm.
	 * @param u: represents the unit measured between the linked road map nodes.
	 * 	  If u equals 0 (resp. 1, 2) the method considers distance in cm (resp. cost in milli euros, time in seconds)
	 * @param s: represents the source road map node
	 * @param t: represents the target road map nodes
	 * @param returnPath: represents the returned path between s and t if it is not null.
	 * @return the smallest path in the specified unit
	 */
	public  int dijkstra_old(int u,
			int s,
			int t,
			TIntList returnPath
			){

		nbCalls_dikjstra++;
		long algoTime = TimeManager.getCpuTimeInNano();

		int n = roadMap2DistCostTime.size();

		if(0>=n || s<0){
			algoTime = TimeManager.getCpuTimeInNano()-algoTime;
			time_dikjstra_ns+=algoTime;
			return -1;
		}
		if(t==s){
			algoTime = TimeManager.getCpuTimeInNano()-algoTime;
			time_dikjstra_ns+=algoTime;
			return 0;
		}
//		System.out.println(" "+ roadMap);

		int [] shorter_dist = new int[n];
		boolean [] marked = new boolean[n];

		int [] arrayOfPredecessors = (returnPath==null)?null:new int[n];

		for(int i=0;i<n;i++){
			if(arrayOfPredecessors != null)
				arrayOfPredecessors[i]= -1;
			shorter_dist[i] = Integer.MAX_VALUE;
			marked[i] = false;
		}

		shorter_dist[s]=0 ;

		PriorityQueue<Integer> shorter_dist_prio_queue = new PriorityQueue<Integer>();
		shorter_dist_prio_queue.add(0);

		TIntObjectHashMap<TIntArrayList> shorter_dist2nodes = new TIntObjectHashMap<TIntArrayList>();
		shorter_dist2nodes.put(0, new TIntArrayList());
		shorter_dist2nodes.get(0).add(s);


		while (!shorter_dist_prio_queue.isEmpty()){

			int cur_shorter_dist = shorter_dist_prio_queue.poll();

			TIntIterator it = shorter_dist2nodes.get(cur_shorter_dist).iterator();
			while(it.hasNext() ){
				int curNode = it.next();
				if(t==(curNode)){
					if(arrayOfPredecessors != null){
						int iNode = curNode;
						while(iNode != -1){
							returnPath.add(iNode);
							iNode = arrayOfPredecessors[iNode];
						}
						returnPath.reverse();
					}
					algoTime = TimeManager.getCpuTimeInNano()-algoTime;
					time_dikjstra_ns+=algoTime;

					System.out.println(" debug return cur_shorter_dist " +cur_shorter_dist);

					return cur_shorter_dist;
				}
				if(marked[curNode] || !roadMap2DistCostTime.containsKey(curNode)){
					continue;
				}

				int []  neighs = roadMap2DistCostTime.get(curNode).keys();
				for(int idx_neigh=0;idx_neigh< neighs.length;idx_neigh++){
					int neigh = neighs[idx_neigh];
					if(marked[neigh]){
						continue;
					}
					int neigh_dist = cur_shorter_dist +roadMap2DistCostTime.get(curNode).get(neigh).get(u);

					if(cur_shorter_dist<0)
						System.out.println(" debug cur_shorter_dist <0 "+cur_shorter_dist);

					if(shorter_dist[neigh]> neigh_dist){
						shorter_dist[neigh] = neigh_dist;

						shorter_dist_prio_queue.add(shorter_dist[neigh]);

						if(!shorter_dist2nodes.containsKey(shorter_dist[neigh])){
							shorter_dist2nodes.put(shorter_dist[neigh], new TIntArrayList());
						}
						shorter_dist2nodes.get(shorter_dist[neigh]).add(neigh);
						if(arrayOfPredecessors != null)
							arrayOfPredecessors[neigh]=curNode;
					}
				}
				marked[curNode] = true;

			}
		}

		algoTime = TimeManager.getCpuTimeInNano()-algoTime;
		time_dikjstra_ns+=algoTime;
		return -1;
	}

	public  void writePathsInCSV(
			String fName) throws IOException {

		   BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
	       writer.write("nodeId1,nodeId2,link\n");

	        TIntHashSet treated = new TIntHashSet(roadMap2DistCostTime.size());
	        TIntObjectIterator<TIntObjectHashMap<TIntArrayList>> itNodes = roadMap2DistCostTime.iterator();
			for(int i=roadMap2DistCostTime.size();i-->0;){
				itNodes.advance();
				int node = itNodes.key();
				double nodeLat = nodeId2Lat.get(node);
				double nodeLon = nodeId2Lon.get(node);

//				boolean pointWriten = false;
				TIntObjectIterator<TIntArrayList> itNeigh = roadMap2DistCostTime.get(node).iterator();
				for(int j=roadMap2DistCostTime.get(node).size();j-->0;){
					itNeigh.advance();
					int neigh = itNeigh.key();
					if(!treated.contains(neigh)){
						double neighLat = nodeId2Lat.get(neigh);
						double neighLon = nodeId2Lon.get(neigh);
						String link="<LineString><coordinates>"
								+nodeLon+","+nodeLat+" "
								+neighLon+","+neighLat+
								"</coordinates></LineString>";
						writer.write(
									node
									+","+neigh
									+ ",\"<MultiGeometry>"
//									+(pointWriten?"":point)
									+ link
									+"</MultiGeometry>\""
									+"\n");
					}
				}

				treated.add(node);
			}

	        writer.flush();
	        writer.close();

	        System.out.println("write new "+fName);
	}



	public void writePathInFile(TIntArrayList path, String fName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
        writer.write("nodeId,latitude,longitude\n");

        for(int i=0;i<path.size();i++){
        	int nodeId = path.get(i);
        	writer.write(nodeId
        			+","+nodeId2Lat.get(nodeId)+
					","+nodeId2Lon.get(nodeId)+"\n");
        }


        writer.flush();
        writer.close();
        System.out.println("write file: " + fName);

    }

	@Override
	public TIntHashSet reachableArea(int s, int t, int l, boolean outerShell) {
		return RMSearch.reachableArea(s, t, l, this, outerShell);
	}

	@Override
	public TIntHashSet dirversReachableArea(int u, int s, int t, int timeLimit,
			boolean outerShell) {
		return null;
	}






}
