package routing;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

public abstract class ARoadMap {
	// Performance attributes
	public long time_closestRoadMapNode_ns;
	public int nbCalls_closestRoadMapNode;

	public long time_dikjstra_ns;
	public int nbCalls_dikjstra;

	public abstract double getMinLat();
	public abstract double getMaxLat();
	public abstract double getMinLon();
	public abstract double getMaxLon();
	public abstract String getMapName();
	public abstract int getNbNodes();

/*	public int getNodeNum();
	public int[] getNeighbourNodes(int node);
	public int getCost(int i, int j, int n);
*/
	public abstract double getLat(int nid);
	public abstract double getLon(int nid);

	public abstract int closestRoadMapNode(double lat, double lon);
	/**
	 * u = 0 -> cm, u = 1 -> milli euro, u = 2 -> secondes
	 * u -> 1 -> milli euro
	 * u -> 2 -> secondes
	 */
	public abstract int shortestPath(int u, int transpMode, int s, TIntArrayList t,
			TIntArrayList returnPath, TIntArrayList returnScores);
	/**
	 * u = 0 -> cm, u = 1 -> milli euro, u = 2 -> secondes
	 *
	 */
	public abstract int shortestPath(int u, int transpMode , int s, int t, TIntArrayList returnPath, TIntArrayList returnScores);
	public abstract int shortestPath(int u, int s, int t, TIntList returnPath, TIntList returnScores);
	public abstract int dijkstra(int u, int s, TIntCollection t, TIntList returnPath, TIntList returnScores);
	public abstract int invDijkstra(int u, TIntCollection s, int t, TIntList returnPath, TIntList returnScores);

	public abstract TIntHashSet reachableArea(int u, int s, int l, boolean outerShell);
	public abstract TIntHashSet dirversReachableArea(int u, int s, int t, int timeLimit, boolean outerShell);

}
