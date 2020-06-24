package clustering.connectedComponents;

import clustering.ClustersOfElevators;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import routing.BasicRoutingInterface;


/*
Clusters with increasing distance
unvisitedNodes <- AllNodes
while unvisitedNodes is not empty:
	toVisit <- pool(unvisitedNodes)
	nvCluster <- open a new cluster
	while toVisit is not empty
		node <- pool(toVisit)
	 	for each neighbor neigh of node, within the limit, such as neigh in unvisitedNodes ?
	 		unvisitedNodes.remove(neigh)
	 		toVisit.add(neigh)
	 		nvCluster.add(neigh)

*/

public class ConnectedComponents {

	public static double getMeasFromTo(String unit, int src, int dest,  BasicRoutingInterface router  ){
		if(unit.equals("distance")){ // in meters
			double dist = router.distance(src,dest);
//			System.out.println("dist from: "+src+ " to: "+dest+"  "+dist);
			return dist;
		}

		if(unit.equals("time")){ // in seconds
			return router.time(src,dest);
		}

		System.err.println("WARNING src: "+src+" dest "+dest+" NOT FOUND");
		return -1;
	}

	public static Int2IntOpenHashMap buildClusters(ClustersOfElevators ce, String unit, int limit ) throws Exception {

//		System.out.println("buildClusters CC "+unit+" =< "+limit);

		Int2IntOpenHashMap node2Cluster = new Int2IntOpenHashMap();
		int clusterId =-1;

		IntArrayList unVisitedNodes = new IntArrayList(ce.elev2label.keySet());
		IntArrayList visitedNodes = new IntArrayList(ce.elev2label.size());
		IntArrayList toVisit = new IntArrayList(ce.elev2label.size());

		while(! unVisitedNodes.isEmpty()){
			int firstNode = unVisitedNodes.getInt(0);
			unVisitedNodes.removeInt(0);
			toVisit.add(firstNode);
			clusterId++;

//			System.out.println("new cluster  "+clusterId);

			while(! toVisit.isEmpty()){
				int node = toVisit.getInt(0);
				toVisit.removeInt(0);

//				System.out.println("node: "+toVisit);
				for(int neigh: ce.elev2label.keySet()){
					if(node !=neigh
						&& !toVisit.contains(neigh)
						&& !visitedNodes.contains(neigh)){

						double measSrcDest = getMeasFromTo(unit,node,neigh,ce.router);
						double measDestSrc = getMeasFromTo(unit,neigh,node,ce.router);

						if( (0<=measSrcDest && measSrcDest <=limit)
								|| (0<=measDestSrc && measDestSrc<=limit)){
							unVisitedNodes.removeInt(unVisitedNodes.indexOf(neigh));

							toVisit.add(neigh);
						}

					}
				}

//				System.out.println("node: "+node+ " clusterId: "+ clusterId);
				node2Cluster.put(node, clusterId);
				visitedNodes.add(node);
			}

		}

		return node2Cluster;
	}

}
