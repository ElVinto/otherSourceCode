package clustering.hierarchical;

import java.util.Collections;

import clustering.ClustersOfElevators;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import routing.BasicRoutingInterface;

/*
Hierarchical Clusterer with increasing distance

node2sortedNeigh order the list of neighbors of each node by unit (dist(n1,n2) = max(router.dist(n1,n2)))
node2sortedVals

sortedBestVal
sortedBestNode

node2clustroid
if n in node2clustroid.keys() and n not in  node2clustroid.values() -> n does not participate to the node selection



Initially: each node represents a cluster

select the 2 closest "clusters"
merge
	removes them
	add one of them


*/


public class CentroidRadiusClusterer_ToFinish {

	public static int getEdgeMeasFromTo(String unit, int src, int dest,  BasicRoutingInterface router  ){
		if(unit.equals("distance")){ // in meters
			int dist1 = (int) router.distance(src,dest);
			int dist2 = (int) router.distance(dest,src);

			return Math.max(dist1,dist2);
		}

		if(unit.equals("time")){ // in seconds
			int time1 = router.time(src,dest);
			int time2 = router.time(src,dest);
			return Math.max(time1, time2);
		}

		System.err.println("WARNING src: "+src+" dest "+dest+" NOT FOUND");
		return -1;
	}

	/**
	 * @param ce instance of cluster of elevators (input)
	 * @param unit in "distance" or "time" (input)
	 * @param limit upper bound of the distance or time between two node (input)
	 * @param node2sortedNeighs (output)
	 * @param node2sortedNeighVals (output)
	 */
	public static void sortNodes(ClustersOfElevators ce, String unit, int limit
			, Int2ObjectOpenHashMap<IntArrayList> node2sortedNeighs
			, Int2ObjectOpenHashMap<IntArrayList> node2sortedNeighVals
			){

		int nbNodes =  ce.elev2label.size();
		for(int e: ce.elev2label.keySet()){
			IntArrayList sortedNeighs = new  IntArrayList(nbNodes);
			IntArrayList sortedVals = new  IntArrayList(nbNodes);
			for(int f: ce.elev2label.keySet()){
				if(e!=f){
					int val = getEdgeMeasFromTo( unit,  e,  f,  ce.router );
					if(val>limit)
						continue;
					int idx =Collections.binarySearch(sortedVals, val);
					idx =idx<0?-(idx+1):idx;
					sortedVals.add(idx,val);
					sortedNeighs.add(idx,e);
				}
			}
			node2sortedNeighs.put(e,sortedNeighs);
			node2sortedNeighVals.put(e, sortedVals);
		}
	}

	public static Int2IntOpenHashMap buildClusters(ClustersOfElevators ce, String unit, int limit
			, Int2ObjectOpenHashMap<IntArrayList> node2sortedNeighs
			, Int2ObjectOpenHashMap<IntArrayList> node2sortedNeighVals
			) throws Exception {

		int nbNodes =  ce.elev2label.size();

		ObjectArrayList<IntArrayList> clusters = new ObjectArrayList<IntArrayList>(nbNodes);
		int nextAvailableClusterId = 0;

		Int2IntOpenHashMap node2nextBestNeighIdx = new Int2IntOpenHashMap(nbNodes);
		for (int node:node2sortedNeighs.keySet()){

			if(node2sortedNeighs.get(node).isEmpty()){
				IntArrayList singletonCluster = new IntArrayList(1);
				singletonCluster.add(node);
				clusters.add(singletonCluster);
				nextAvailableClusterId++;
			}else{
				node2nextBestNeighIdx.put(node, 0);
			}

		}


		// Initialize structures sortedClustroids and sortedClustroidVals

		IntArrayList sortedClustroids = new IntArrayList(nbNodes);
		IntArrayList sortedClustroidVals = new IntArrayList(nbNodes);
		for(int node: node2sortedNeighs.keySet()){

			if(!node2sortedNeighs.get(node).isEmpty()){
				int val = node2sortedNeighVals.get(node).getInt(node2nextBestNeighIdx.get(node));

				int idx = Collections.binarySearch( sortedClustroidVals,val);
				idx =idx<0?-(idx+1):idx;
				sortedClustroids.add(idx,node);
				sortedClustroidVals.add(idx,val);
			}
		}



		Int2IntOpenHashMap centroid2cluster = new Int2IntOpenHashMap(nbNodes);

		IntArrayList curCluster = new IntArrayList(nbNodes);

		while(! sortedClustroids.isEmpty()){

			// pick the 2 closest clustroids

			int curClustroid = sortedClustroids.getInt(0);
			sortedClustroids.removeInt(0);
			sortedClustroidVals.removeInt(0);


			int curClustroidNeighbour = node2sortedNeighs.get(curClustroid).getInt(node2nextBestNeighIdx.get(curClustroid));


			int idxNeighBestCentroid = sortedClustroids.indexOf(curClustroidNeighbour);
			sortedClustroids.removeInt(idxNeighBestCentroid);
			sortedClustroidVals.removeInt(idxNeighBestCentroid);


			// Build the current cluster as the union of curClustroid and curClustroidNeighbour's clusters

			if( centroid2cluster.containsKey(curClustroid)){
				for(int node: clusters.get(centroid2cluster.get(curClustroid))){
					assert !curCluster.contains(node); // TO COMMENT
					curCluster.add(node);
				}
			}else{
				curCluster.add(curClustroid);
			}

			if( centroid2cluster.containsKey(curClustroidNeighbour)){
				for(int node: clusters.get(centroid2cluster.get(curClustroidNeighbour))){
					assert !curCluster.contains(node); // TO COMMENT
					curCluster.add(node);
				}
			}else{
				curCluster.add(curClustroidNeighbour);
			}


			// Compute the new centroid: nvCLustroid

			//	Compute the centroid
			double centroidLat =0.0;
			double centroidLng =0.0;
			for(int node:curCluster){
				centroidLat+=ce.elev2lat.get(node);
				centroidLng+=ce.elev2lon.get(node);
			}
			centroidLat = centroidLat/(double)curCluster.size();
			centroidLat = centroidLng/(double)curCluster.size();

			//	nvClustroid is the closest node to centroid in curCluster
			for(int node : curCluster){
				double minDist = Double.MAX_VALUE;
				int nvClustroid = -1;
				double dist2centroid = Math.sqrt(
						Math.pow(centroidLat-ce.elev2lat.get(node),2)
						+Math.pow(centroidLng-ce.elev2lon.get(node),2));
				if(dist2centroid<minDist){
					nvClustroid=node;
					minDist=dist2centroid;
				}
			}

			// TODO FINISH
			curCluster.clear();


		}






		Int2IntOpenHashMap node2Cluster = new Int2IntOpenHashMap();

		return node2Cluster;
	}

	public static void main(String args[]){





	}



}
