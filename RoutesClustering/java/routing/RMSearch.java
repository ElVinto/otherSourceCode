package routing;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
/**
 * ©Insight Centre<br>
 * Created on: 2 Jan 2015<br>
 *
 * @author jhoran
 *
 */
public class RMSearch {

	private static double INV_TAU = 1/(Math.PI * 2);
	//40008 is the circumference of the earth through the poles.
	//It is important that the values here aren't rounded up, as exceeding the distance may result in A* finding a less than optimal route
	private static double KM_PER_RAD = 6371;//6367;// 40008/TAU;


	private static int[] predessors;
	private static double[] gScore;
	private static double[] fScore;
	private static double[] tDist;

	private static boolean[] openCheck;
	private static boolean[] closedCheck;
	private static int[] score;

	private static void initFieldsForAstar(int numNodes, boolean calcPredessors) {
		if( calcPredessors ) {
			if( predessors == null || predessors.length != numNodes ) {
				predessors  = new int[numNodes];
			} else {
				Arrays.fill(predessors, 0);
			}
		}
		if( gScore == null || gScore.length != numNodes ) {
			gScore  = new double[numNodes];
		} else {
			Arrays.fill(gScore, 0);
		}
		if( fScore == null || fScore.length != numNodes ) {
			fScore  = new double[numNodes];
		} else {
			Arrays.fill(fScore, 0);
		}
		if( tDist == null || tDist.length != numNodes ) {
			tDist  = new double[numNodes];
		} else {
			Arrays.fill(tDist, 0);
		}
		if( openCheck == null || openCheck.length != numNodes ) {
			openCheck  = new boolean[numNodes];
		} else {
			Arrays.fill(openCheck, false);
		}
		if( closedCheck == null || closedCheck.length != numNodes ) {
			closedCheck  = new boolean[numNodes];
		} else {
			Arrays.fill(closedCheck, false);
		}
	}

	private static void initFieldsForDijkstra(int numNodes) {
		if( predessors == null || predessors.length != numNodes ) {
			predessors  = new int[numNodes];
		}
		Arrays.fill(predessors, -1);

		if( closedCheck == null || closedCheck.length != numNodes ) {
			closedCheck  = new boolean[numNodes];
		}
		Arrays.fill(closedCheck, false);

		if( score == null || score.length != numNodes ) {
			score  = new int[numNodes];
		}
		Arrays.fill(score, Integer.MAX_VALUE);
	}

	/*
	 * TODO: Problems I'm running into here.  A* only finds the optimal path if the husteric doesn't overestimate the distance.  The
	 * distance currently is based off degrees mapped into ecludian space, the number of km in a degree depends on the latitiude.
	 * Possible one solution would be if the distance between road map nodes was expressed in angular units to begin with
	 */

	/**
	 * Implementation of A* using arrays.
	 * @param s the originating position
	 * @param t the terminating position
	 * @param rm the road map containing the nodes
	 * @param returnPath if this isn't null, then it will be populated with the path taken from s to t.
	 * @return the distance, in centimeters, from s to t
	 */
	public static int aStar(int s, int t, RoadMap rm, TIntList returnPath, TIntList returnScores){
		int u = 0;
		int numNodes = rm.roadMap2DistCostTime.size();


		if(numNodes == 0 || s < 0) {
			return -1;
		}
		if(s == t) {
			return 0;
		}
		boolean calcPredessors = returnPath != null;
		initFieldsForAstar(numNodes, calcPredessors);

		int[] predessors = calcPredessors ?  RMSearch.predessors : null;
		final double[] gScore = RMSearch.gScore;
		final double[] fScore = RMSearch.fScore;
		final double[] tDist = RMSearch.tDist;

		boolean[] openCheck = RMSearch.openCheck;
		boolean[] closedCheck = RMSearch.closedCheck;

		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(200, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				double s1 = fScore[o1];
				double s2 = fScore[o2];

				if(s1 > s2) return 1;
				if(s1 < s2) return -1;
				return o1 - o2;
			}
		});
		double t_lat = rm.getLat(t), t_lon = rm.getLon(t);

		fScore[s] = estimateDistance(
				rm.getLat(s), rm.getLon(s),
				t_lat, t_lon);
		gScore[s] = 0;
		openCheck[s] = true;
		queue.add(s);

		while(!queue.isEmpty()){
			int curId = queue.poll();

			if(curId == t){
				if(calcPredessors){
					int cur = t;
					while(cur != s){
						returnPath.add(cur);
						if(returnScores!=null)
							returnScores.add((int)gScore[cur]);
						cur = predessors[cur];
					}
					returnPath.add(s);
					returnPath.reverse();
					if(returnScores!=null){
						returnScores.add(0);
						returnScores.reverse();
					}
				}

				return (int) gScore[t];
			}
			double c_gScore = gScore[curId];
			openCheck[curId] = false;
			closedCheck[curId] = true;

			TIntObjectHashMap<TIntArrayList> neigh = rm.roadMap2DistCostTime.get(curId);
			TIntObjectIterator<TIntArrayList> itNeigh = neigh.iterator();

			for(int i = neigh.size(); i --> 0;){
				itNeigh.advance();
				int nId = itNeigh.key();

				if(closedCheck[nId])
					continue;
				//double n_lat = rm.getLat(nId), n_lon = rm.getLon(nId);
				double t_gScore = c_gScore + itNeigh.value().get(u);

				boolean inOpenSet = openCheck[nId];
				if(!inOpenSet || t_gScore < gScore[nId]){
					if(calcPredessors)
						predessors[nId] = curId;
					gScore[nId] = t_gScore;

					if(!inOpenSet){
						openCheck[nId] = true;
						//The estimated distance won't change, so calculate and cache it here
						tDist[nId] = estimateDistance(rm.getLat(nId), rm.getLon(nId), t_lat, t_lon);
					}
					else
						//Only checks the position on add, so need to remove to update its position in the queue
						queue.remove(nId);
					//Update the forecasted score
					fScore[nId] = t_gScore + tDist[nId];
					queue.add(nId);
				}
			}

		}

		return -1;
	}


	/**
	 * Calculate the distance between two GPS points using spherical geometry
	 * @param sLat latitude of the first coordinate
	 * @param sLon longitude of the first coordinate
	 * @param eLat latitude of the second coordinate
	 * @param eLon longitude of the second coordinate
	 * @return the distance between these two points in centimeters
	 */
	@SuppressWarnings("unused") //This method is primarily for testing purposes.
	private static double estimateDistanceR(double sLat, double sLon, double eLat, double eLon){
		double dist = RoadMapBlock.angularDistGPSCoordinates(sLat, sLon, eLat, eLon);
		dist *= 6371; //Radians to km

		dist *= 1e5; //km to cm

		return dist;
	}

	/**
	 * Calculate the Euclidean distance between two GPS points
	 * @param sLat latitude of the first coordinate
	 * @param sLon longitude of the first coordinate
	 * @param eLat latitude of the second coordinate
	 * @param eLon longitude of the second coordinate
	 * @return the distance between these two points in centimeters
	 */
	public static double estimateDistance(double sLat, double sLon, double eLat, double eLon){
		//Need to figure out the number of kilometers at a given latitude in order to convert radians to kilometers.
		//TODO: consider precalculating and caching this instead
		double avgLat = Math.max(Math.abs(sLat), Math.abs(eLat)); //Math.abs((sLat + eLat)/2);//M
		//40030 was used as the circumference of the earth when populating the data in RoadMap
		double circumferenceAtLat = 40030 * Math.cos(Math.toRadians(avgLat));

		//Just in case the route transverses
		double dLat = Math.abs(sLat - eLat);
		double dLon = Math.abs(sLon - eLon);
		if(dLat > 180) dLat = 360 - dLat;
		if(dLon > 180) dLon = 360 - dLon;

		dLat = Math.toRadians(dLat) * KM_PER_RAD;
		dLon = Math.toRadians(dLon) * (circumferenceAtLat * INV_TAU);

		double ans = (dLat * dLat) +
				(dLon * dLon);

		ans = Math.sqrt(ans) * 1e5; //km to cm

		return ans;
	}

	/**
	 * @param u the unit to use when calculating the cost from s to t from {@link RoadMap#roadMap2DistCostTime}
	 * @param s the starting point
	 * @param t the termination point
	 * @param rm the road map containing the two points
	 * @param returnPath can be null, otherwise it will be populated with the path taken from s to t
	 * @return the cost, in the chosen unit, of traveling from s to t.
	 */
	public static int dijkstra(int u, int s, int t, RoadMap rm, TIntList returnPath, TIntList returnScores){

//		System.err.println("debug dijkstra( u, s, t, rm, returnPath,returnScores)");

		int numNodes = rm.roadMap2DistCostTime.size();

		if(numNodes == 0 || s < 0) {
//			System.err.println("debug  s"+s+" numNodes"+numNodes);
			return -1;
		}
		if(s == t) {
			return 0;
		}

		initFieldsForDijkstra(numNodes);


		int[] predessors = RMSearch.predessors;
		final int[] score = RMSearch.score;
		boolean[] closedCheck = RMSearch.closedCheck;

		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(200, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				int s1 = score[o1];
				int s2 = score[o2];

				if(s1 > s2) return 1;
				if(s1 < s2) return -1;
				return o1 - o2;
			}
		});

		score[s] = 0;
		queue.add(s);

		while(!queue.isEmpty()){
			int curId = queue.poll();

			if(curId == t){
				if(returnPath != null){
					int cur = t;
					while(cur != s){
						returnPath.add(cur);
						if(returnScores!=null)
							returnScores.add(score[cur]);
						cur = predessors[cur];
					}
					returnPath.add(s);
					returnPath.reverse();
					if(returnScores!=null){
						returnScores.add(0);
						returnScores.reverse();
					}
				}
//				System.err.println("path " +returnPath);
//				System.err.println("scores " +returnScores);

				return score[t];
			}

			closedCheck[curId] = true;



			TIntObjectHashMap<TIntArrayList> neigh = rm.roadMap2DistCostTime.get(curId);
			TIntObjectIterator<TIntArrayList> itNeigh = neigh.iterator();
			for(int i = neigh.size(); i-- > 0;){
				itNeigh.advance();
				int nId = itNeigh.key();
				if(closedCheck[nId]) continue;

				int t_score = score[curId] + itNeigh.value().get(u);
				if(t_score < score[nId]){
					predessors[nId] = curId;
					if(score[nId] != Integer.MAX_VALUE)
						queue.remove(nId);
					score[nId] = t_score;

					queue.add(nId);
				}
			}
		}
//		System.err.println("debug  return"+-1);
		return -1;
	}

	/**
	 * @param u the unit to use when calculating the cost from s to t from {@link RoadMap#roadMap2DistCostTime}
	 * @param s the starting point
	 * @param t the termination point
	 * @param rm the road map containing the two points
	 * @param returnPath can be null, otherwise it will be populated with the path taken from s to t
	 * @return the cost, in the chosen unit, of traveling from s to t.
	 *//*
	public static int dijkstraGreedy(int u, int s, int t, RoadMap rm, TIntList returnPath, TIntList returnScores){

//		System.err.println("debug dijkstra( u, s, t, rm, returnPath");

		int numNodes = rm.roadMap2DistCostTime.size();

		if(numNodes == 0 || s < 0) {
//			System.err.println("debug  s"+s+" numNodes"+numNodes);
			return -1;
		}
		if(s == t) {
			return 0;
		}

		int[] predessors = new int[numNodes];
		final int[] score = new int[numNodes];
		boolean[] closedCheck = new boolean[numNodes];

		for(int i = 0; i < numNodes; i++) score[i] = Integer.MAX_VALUE;

		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(200, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				int s1 = score[o1];
				int s2 = score[o2];

				if(s1 > s2) return 1;
				if(s1 < s2) return -1;
				return o1 - o2;
			}
		});

		score[s] = 0;
		queue.add(s);

		while(!queue.isEmpty()){
			int curId = queue.poll();

			if(curId == t){
				if(returnPath != null){
					int cur = t;
					returnPath.add(cur);
					if(returnScores!=null)
						returnScores.add(score[cur]);
					while(cur != s){
						cur = predessors[cur];
						returnPath.add(cur);
						if(returnScores!=null)
							returnScores.add(score[cur]);
					}
					returnPath.add(s);
					returnPath.reverse();
					if(returnScores!=null){
						returnScores.add(0);
						returnScores.reverse();
					}
				}
//				System.err.println("debug  A score[t]"+score[t]);
				return score[t];
			}

			closedCheck[curId] = true;

			TIntObjectHashMap<TIntArrayList> neigh = rm.roadMap2DistCostTime.get(curId);
			TIntObjectIterator<TIntArrayList> itNeigh = neigh.iterator();
			for(int i = neigh.size(); i-- > 0;){
				itNeigh.advance();

				int nId = itNeigh.key();
				if(closedCheck[nId]) continue;

				int prev = curId;
				int next = nId;
				int cost = score[curId] + itNeigh.value().get(u);

				TIntObjectHashMap<TIntArrayList> nneigh = rm.roadMap2DistCostTime.get(next);
				int numNeigh;
				while((numNeigh = nneigh.size()) < 3){
					if(numNeigh == 1);  //cul de sac
					nn


				}




			}



			TIntObjectHashMap<TIntArrayList> neigh = rm.roadMap2DistCostTime.get(curId);
			TIntObjectIterator<TIntArrayList> itNeigh = neigh.iterator();
			for(int i = neigh.size(); i-- > 0;){
				itNeigh.advance();
				int nId = itNeigh.key();
				if(closedCheck[nId]) continue;

				int t_score = score[curId] + itNeigh.value().get(u);
				if(t_score < score[nId]){
					predessors[nId] = curId;
					boolean rem = score[nId] != Integer.MAX_VALUE;
					score[nId] = t_score;
					if(rem)
						queue.remove(nId);
					queue.add(nId);
				}
			}
		}
//		System.err.println("debug  return"+-1);
		return -1;
	}
	*/

	/**
	 * @param u the unit to use when calculating the cost from s to t from {@link RoadMap#roadMap2DistCostTime}
	 * @param s the starting point
	 * @param t the termination point
	 * @param rm the road map containing the two points
	 * @param returnPath can be null, otherwise it will be populated with the path taken from s to t
	 * @return the cost, in the chosen unit, of traveling from s to t, 0 if t contains s and -1 if no path has been found
	 */
	public static int dijkstra(int u, int s, TIntCollection t, RoadMap rm, TIntList returnPath, TIntList returnScores){
		int numNodes = rm.roadMap2DistCostTime.size();

		if(numNodes == 0 || s < 0) {
			return -1;
		}
		if( t.contains(s)) {
			return 0;
		}

		initFieldsForDijkstra(numNodes);

		int[] predessors = RMSearch.predessors;
		final int[] score = RMSearch.score;
		boolean[] closedCheck = RMSearch.closedCheck;

		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(200, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				int s1 = score[o1];
				int s2 = score[o2];

				if(s1 > s2) return 1;
				if(s1 < s2) return -1;
				return o1 - o2;
			}
		});

		score[s] = 0;
		queue.add(s);

		while(!queue.isEmpty()){
			int curId = queue.poll();

			if(t.contains(curId)){
				if(returnPath != null){
					int cur = curId;
					while(cur != s){
						returnPath.add(cur);
						if(returnScores!=null)
							returnScores.add(score[cur]);
						cur = predessors[cur];
					}
					returnPath.add(s);
					returnPath.reverse();
					if(returnScores!=null){
						returnScores.add(0);
						returnScores.reverse();
					}
				}
				return score[curId];
			}

			closedCheck[curId] = true;

			TIntObjectHashMap<TIntArrayList> neigh = rm.roadMap2DistCostTime.get(curId);
			TIntObjectIterator<TIntArrayList> itNeigh = neigh.iterator();
			for(int i = neigh.size(); i-- > 0;){
				itNeigh.advance();
				int nId = itNeigh.key();
				if(closedCheck[nId]) continue;

				int t_score = score[curId] + itNeigh.value().get(u);
				if(t_score < score[nId]){
					predessors[nId] = curId;
					boolean rem = score[nId] != Integer.MAX_VALUE;
					score[nId] = t_score;
					if(rem)
						queue.remove(nId);
					queue.add(nId);
				}
			}
		}

		return -1;
	}

	//=================================================================================================
	//Fibonacci Heap based A*


	/**
	 * A* algorithm using a Fibonacci Heap.  Doesn't use arrays, so it doesn't have the same constraints that
	 * the array implementation has.  However it is a little slower overall.
	 *
	 * @param s the start node
	 * @param t the terminating node
	 * @param returnPath the set of nodes that is traversed to get from s to t
	 * @param rm the road map containing these nodes
	 * @return the road distance between s and t
	 */
	public static int aStarFib(int s, int t, RoadMap rm, TIntList returnPath){
		//I don't think it makes any sense to use any other unit than distance here, as that is the only unit we
		//can reasonably estimate.  Possibly we could assume a fixed speed for the estimated distance, but that
		//would provide a very poor estimate to direct the search.
		int u = 0;

		int n = rm.roadMap2DistCostTime.size();

		if(0 >= n || s < 0){
			return -1;
		}
		if(s == t){
			return 0;
		}

		FibonacciHeap<AStarNode> queue = new FibonacciHeap<AStarNode>();
		//Need to keep track of the nodes already added
		TIntObjectHashMap<FibonacciHeapNode<AStarNode>> nodes = new TIntObjectHashMap<FibonacciHeapNode<AStarNode>>();

		//Will need the terminating coordinates a lot, so no point in having to keep looking them up
		double t_lat = rm.getLat(t), t_lon = rm.getLon(t);

		//Add the start node to the queue to kick off the loop.  No point in actually estimating the distance as the node
		//will be immediately popped
		FibonacciHeapNode<AStarNode> sNode = new FibonacciHeapNode<AStarNode>(
				new AStarNode(s, 0, Double.MAX_VALUE));
		nodes.put(s, sNode);
		queue.insert(
			sNode, sNode.getData().distToTarget
		);

		while(!queue.isEmpty()){
			AStarNode cur = queue.removeMin().getData();

			//Reconstruct Path
			if(cur.index == t) {
				double result = cur.gScore;
				if(returnPath != null){
					while(cur.index != s){
						returnPath.add(cur.index);
						cur = cur.predessor;
					}
					returnPath.add(s);
					returnPath.reverse();
				}
				return (int) result;
			}

			cur.closed = true;
			double c_gScore = cur.gScore;

			TIntObjectHashMap<TIntArrayList> neigh = rm.roadMap2DistCostTime.get(cur.index);
			TIntObjectIterator<TIntArrayList> itNeigh = neigh.iterator();

			for(int i = neigh.size(); i-- > 0;){
				itNeigh.advance();
				int nId = itNeigh.key();

				FibonacciHeapNode<AStarNode> nNode = nodes.get(nId);

				boolean inOpenSet = nNode != null;
				if(inOpenSet && nNode.getData().closed == true) continue;

				double t_gScore = c_gScore + itNeigh.value().get(u);

				//The distance will only have to be estimated once per node, as it doesn't change.  So might as well cache it.
				if(!inOpenSet){
					double n_lat = rm.getLat(nId), n_lon = rm.getLon(nId);
					nNode = new FibonacciHeapNode<AStarNode>(new AStarNode(nId, t_gScore,
							estimateDistance(t_lat, t_lon, n_lat, n_lon)));
					nNode.getData().predessor = cur;
					nodes.put(nId, nNode);
					queue.insert(nNode, nNode.getData().fScore());
				}
				else if(t_gScore < nNode.getData().gScore) {
					nNode.getData().predessor = cur;
					nNode.getData().gScore = t_gScore;
					queue.decreaseKey(nNode, nNode.getData().fScore());
				}
			}
		}

		return -1;
	}

	private static class AStarNode{
		public final int index;
		public final double distToTarget;
		public double gScore;
		public AStarNode predessor;
		public boolean closed;

		/**
		 * @param index unique node identifier, matching those used in the road map
		 * @param distFromStart current lowest cost to travel from the start to this node
		 * @param distToEnd estimated distance from this node to the terminating node
		 */
		public AStarNode(int index, double distFromStart, double distToEnd){
			this.index = index;
			this.gScore = distFromStart;
			this.distToTarget = distToEnd;
		}

		/**
		 * @return the estimated cost of traversing this node to reach the target.
		 */
		public double fScore(){
			return gScore + distToTarget;
		}

	}

	//=================================================================================================
	//Reachable area using Dijkstra
	/**
	 * And implementation of Dijkstra's algorithm that calculates the set of nodes that are reachable given
	 * a fixed limit.  The limit is based off the units from {@link RoadMap#roadMap2DistCostTime}, and must
	 * be expressed using the same units.
	 *
	 * @param u the unit that the limit is expressed in
	 * @param s the start node
	 * @param l	the limit to stop the search at
	 * @param rm the node map to search over
	 * @param outerShell if true the result will only contain the futhermost set of nodes that it
	 * 			could reach.  If false then all nodes that it can reach are returned.
	 * @return a list of nodes which were reached within the limit
	 */
	public static TIntHashSet reachableArea(int u, int s, int l, RoadMap rm, boolean outerShell) {
		int numNodes = rm.roadMap2DistCostTime.size();

		if(numNodes == 0 || s < 0)
			return null;


		if(l == 0) return new TIntHashSet();

		initFieldsForDijkstra(numNodes);

		int[] predessors = RMSearch.predessors;
		final int[] score = RMSearch.score;
		boolean[] closedCheck = RMSearch.closedCheck;

		//The queue requires that a score is assigned to a node prior to it being added to the queue.
		PriorityQueue<Integer> queue = buildStandardQueue(score);

		score[s] = 0;
		queue.add(s);
		int nCount = 0;

		while(!queue.isEmpty()){
			int curId = queue.poll();

			if(score[curId] > l){
				if(outerShell){
					/* Just the outer shell of nodes that are reachable.  This is done by taking all the nodes that are
					 * currently in the queue, and then taking one step back.  Consequently it means that it wouldn't work
					 * with cul-de-sacs that end right on the max distance (or roads that become cul-de-sacs because they
					 * traverse the edge of the map).  Not sure if it worth the processing power to include such roads.
					 */
					TIntHashSet result = new TIntHashSet();

					queue.add(curId);
					while(!queue.isEmpty()){
						int pId = predessors[queue.poll()];
						if(closedCheck[pId]){
							closedCheck[pId] = false; //Reusing this array just to track the nodes already added;
							while(rm.roadMap2DistCostTime.get(pId).size() < 3 && pId != s)
								pId = predessors[pId];
							if(pId != s){
								result.add(pId);
								closedCheck[pId] = false;
							}
							else if(closedCheck[s]
									// && rm.roadMap2DistCostTime.get(s).size() > 2
									){ //If the user starts on a junction, make sure it is added
								result.add(s);
								closedCheck[s] = false;
							}


						}

					}

					if(result.isEmpty()){
						try{
							System.err.println("RMSearch: Empty reachable area from node "+s);
							/*
							int distA = 0, distB = 0;
							int curA = -1, curB = -1;
							int prevA = s, prevB = s;

							int[] neighb = rm.roadMap2DistCostTime.get(s).keys();
							if(neighb.length == 0) return result;

							curA = neighb[0];
							if(neighb.length > 1)
								curB = neighb[1];

							distA += rm.roadMap2DistCostTime.get(s).get(curA).get(u);
							if(curB != -1)
								distB += rm.roadMap2DistCostTime.get(s).get(curB).get(u);

							while((curA == -1 || rm.roadMap2DistCostTime.get(curA).size() < 3) &&
									(curB == -1 || rm.roadMap2DistCostTime.get(curB).size() < 3)){
								if(curA == -1 && curB == -1){
									System.out.println("RMSearch: feaking out");
									return result;
								}
								boolean advanceA = curA != -1 && (curB == -1 || distA < distB);

								if(advanceA){
									neighb = rm.roadMap2DistCostTime.get(curA).keys();
									if(neighb.length > 2)
										break;
									if(neighb.length == 1) {
										curA = -1;

										System.out.println("RMSearch: culdesac");
										continue;
									}

									if(neighb[0] == prevA) curA = neighb[1];
									else curA = neighb[0];
									prevA = curA;

									distA += rm.roadMap2DistCostTime.get(prevA).get(curA).get(u);
								}
								else {
									neighb = rm.roadMap2DistCostTime.get(curB).keys();
									if(neighb.length > 2)
										break;
									if(neighb.length == 1) {
										curB = -1;
										System.out.println("RMSearch: culdesac");
										continue;
									}

									if(neighb[0] == prevA) curB = neighb[1];
									else curB = neighb[0];
									prevB = curB;

									distB += rm.roadMap2DistCostTime.get(prevB).get(curB).get(u);
								}
							}
							if(distB < distA){
								System.out.println("RMSearch: sanity check "+rm.roadMap2DistCostTime.get(curB).size());
								result.add(curB);
							}
							else{
								System.out.println("RMSearch: sanity check "+rm.roadMap2DistCostTime.get(curA).size());
								result.add(curA);
							}*/
						}catch (Exception e){
							System.err.println("RMSearch; terrible exception");
						}

					}
					/*
					result.add(predessors[curId]);
					closedCheck[predessors[curId]] = false; //Reusing this array just to track the nodes already added;

					while(!queue.isEmpty()){
						int pId = predessors[queue.poll()];
						if(closedCheck[pId]){
							result.add(pId);
							closedCheck[pId] = false;
						}
					}
					*/
					return result;
				} else {
					TIntHashSet result = new TIntHashSet();
					for(int i = 0; i < numNodes; i++)
						if(closedCheck[i]){
//							// TODO change for in degrees
//							if(rm.roadMap2DistCostTime.get(i).size()>2)
								result.add(i);
						}
					return result;
				}
			}
			nCount ++;
			closedCheck[curId] = true;

			TIntObjectHashMap<TIntArrayList> neigh = rm.roadMap2DistCostTime.get(curId);
			TIntObjectIterator<TIntArrayList> itNeigh = neigh.iterator();
			for(int i = neigh.size(); i-- > 0;){
				itNeigh.advance();
				int nId = itNeigh.key();
				if(closedCheck[nId]) continue;

				int t_score = score[curId] + itNeigh.value().get(u);
				if(t_score < score[nId]){
					predessors[nId] = curId;
					boolean rem = score[nId] != Integer.MAX_VALUE;
					score[nId] = t_score;
					//Priority queues only worry about the position on addition.  So remove and readd to account for the
					//new score.
					if(rem)
						queue.remove(nId);
					queue.add(nId);
				}
			}

		}

		return null;
	}

	//=================================================================================================
	//Inverse Dijkstra


	/**
	 * @param u the unit used to calculate the cost to travel from s to t
	 * @param s a set of possible starting points
	 * @param t the terminating point
	 * @param rm can be null, otherwise it will be populated with the path taken from the chosen node in s, to t
	 * @param returnPath the path from the closest node in s, to t
	 * @return the cost in traversing from s to t
	 */
	public static int invDijkstra(int u, TIntCollection s, int t, RoadMap rm, TIntList returnPath, TIntList returnScores){
		int numNodes = rm.getNbNodes();

		if(numNodes == 0 || s.size() < 0) {
			return -1;
		}
		if(s.contains(t)) {
			return 0;
		}

		initFieldsForDijkstra(numNodes);

		int[] predessors = RMSearch.predessors;
		final int[] score = RMSearch.score;
		boolean[] closedCheck = RMSearch.closedCheck;
		score[t] = 0;

		PriorityQueue<Integer> queue = buildStandardQueue(score);
		queue.add(t);

		while(!queue.isEmpty()){
			int curId = queue.poll();

			if(s.contains(curId)){
				int ans = score[curId];
				if(returnPath != null){
					while(curId != t){
						returnPath.add(curId);
						if(returnScores!=null)
							returnScores.add(score[curId]);
						curId = predessors[curId];
					}
					returnPath.add(t);
					if(returnScores!=null)
						returnScores.add(score[t]);
				}
				return ans;
			}

			closedCheck[curId] = true;
			TIntObjectHashMap<TIntArrayList> neighs = rm.roadMap2DistCostTime.get(curId);
			TIntObjectIterator<TIntArrayList> itNeighs = neighs.iterator();
			for(int i = neighs.size(); i-- > 0; ){
				itNeighs.advance();
				int nId = itNeighs.key();

				if(closedCheck[nId]) continue;

				int t_score = score[curId] + rm.roadMap2DistCostTime.get(nId).get(curId).get(u);
				if(t_score < score[nId]){
					if(predessors != null)
						predessors[nId] = curId;
					boolean rem = score[nId] == Integer.MAX_VALUE;
					score[nId] = t_score;
					if(rem)
						queue.remove(nId);
					queue.add(nId);
				}
			}
		}

		return -1;
	}

	public static int getPathLength(int u, TIntList path, RoadMap rm) {
		int size = path.size();
		if(size == 0) return -1;
		if(size == 1) return 0;

		TIntIterator itPath = path.iterator();
		int prev = itPath.next();
		int totalDist = 0;
		try{
			for(int i = size; i-- > 1; ){
				int cur = itPath.next();

				totalDist += rm.roadMap2DistCostTime.get(prev).get(cur).get(u);
				prev = cur;
			}
		}
		catch (NullPointerException e){
			return -1;
		}

		return totalDist;
	}

	/**
	 * @param score the elements used in deciding what order to sort the elements
	 * @return the queue
	 */
	private static PriorityQueue<Integer> buildStandardQueue(final int[] score){
		return new PriorityQueue<Integer>(500, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				int s1 = score[o1];
				int s2 = score[o2];

				if(s1 > s2) return 1;
				if(s1 < s2) return -1;
				return o1 - o2;
			}
		});
	}
}
