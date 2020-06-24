package clustering.hierarchical;

import java.util.Collections;

import clustering.ClustersOfElevators;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import routing.BasicRoutingInterface;

/*
Hierarchical Clusterer with increasing distance

node2sortedNeigh order the list of neighbors of each node by unit (dist(n1,n2) = max(router.dist(n1,n2)))
node2sortedVals


*/


public class ClustroidRadiusClusterer {

	public static int getEdgeMeasFromTo(String unit, int src, int dest,  BasicRoutingInterface router  ){
		if(unit.equals("distance")){ // in meters
			int dist1 = (int) router.distance(src,dest);
			int dist2 = (int) router.distance(dest,src);

			return Math.max(dist1,dist2);
		}

		if(unit.equals("time")){ // in seconds
			int time1 = router.time(src,dest);
			int time2 = router.time(dest,src);
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

//		IntArrayList testList = new IntArrayList(new int[]{1006, 1029, 1010, 1015, 1099, 1113,  1222, 1187, 1214});// 1099, 1113  1222, 1187, 1214

		int nbNodes =  ce.elev2label.size();
		for(int e: ce.elev2label.keySet()){

//			// TODO REMOVE
//			if(! testList.contains(e))
//				continue;

			IntArrayList sortedNeighs = new  IntArrayList(nbNodes);
			IntArrayList sortedVals = new  IntArrayList(nbNodes);
			for(int f: ce.elev2label.keySet()){

//				// TODO REMOVE
//				if(! testList.contains(f))
//					continue;

				if(e!=f){
					int val = getEdgeMeasFromTo( unit,  e,  f,  ce.router );
					if(val>limit)
						continue;
					int idx =Collections.binarySearch(sortedVals, val);
					idx =idx<0?-(idx+1):idx;
					sortedVals.add(idx,val);
					sortedNeighs.add(idx,f);
				}
			}
			node2sortedNeighs.put(e,sortedNeighs);
			node2sortedNeighVals.put(e, sortedVals);
		}

//		for (int n: node2sortedNeighs.keySet()){
//			System.out.println("\n "+n+" "+ node2sortedNeighs.get(n));
//			System.out.println(" "+n+" "+ node2sortedNeighVals.get(n));
//		}

	}

	public static Int2IntOpenHashMap buildClusters(ClustersOfElevators ce, String unit, int limit) throws Exception {
		int nbNodes =  ce.elev2label.size();
		Int2ObjectOpenHashMap<IntArrayList> node2sortedNeighs = new Int2ObjectOpenHashMap<IntArrayList>(nbNodes);
		Int2ObjectOpenHashMap<IntArrayList> node2sortedNeighVals = new Int2ObjectOpenHashMap<IntArrayList>(nbNodes);

		sortNodes( ce,  unit,  limit, node2sortedNeighs, node2sortedNeighVals);

		return buildClusters( ce,  unit,  limit, node2sortedNeighs, node2sortedNeighVals);
	}

	public static Int2IntOpenHashMap buildClusters(ClustersOfElevators ce, String unit, int limit
			, Int2ObjectOpenHashMap<IntArrayList> node2sortedNeighs
			, Int2ObjectOpenHashMap<IntArrayList> node2sortedNeighVals
			) throws Exception {

		int nbNodes =  ce.elev2label.size();

		Int2ObjectOpenHashMap<IntOpenHashSet> clustroid2cluster = new Int2ObjectOpenHashMap<IntOpenHashSet>(nbNodes);
		Int2ObjectOpenHashMap<IntOpenHashSet> clustroid2incompatibles = new Int2ObjectOpenHashMap<IntOpenHashSet>(nbNodes);

		Int2IntOpenHashMap node2nextBestNeighIdx = new Int2IntOpenHashMap(nbNodes);
		for (int node:node2sortedNeighs.keySet()){

			if(node2sortedNeighs.get(node).isEmpty()){
				clustroid2cluster.put(node,new IntOpenHashSet(1));
				clustroid2cluster.get(node).add(node);
			}else{
				node2nextBestNeighIdx.put(node, 0);
			}
		}

//		System.out.println("\nnb of single clustroids: "+clustroid2cluster.size());


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

//		System.out.println("\nsortedClustroids: "+sortedClustroids);
//		System.out.println("sortedClustroidVals: "+sortedClustroidVals);


		// Build the clusters

		IntOpenHashSet curCluster = new IntOpenHashSet(nbNodes);
//		IntOpenHashSet curIncompatibles = new IntOpenHashSet(nbNodes);
		int [] candidates  = new int [2];

		while(! sortedClustroids.isEmpty()){

			// pick the 2 closest clustroids

			int curClustroid = sortedClustroids.getInt(0);

			int curClustroidNeighbour = node2sortedNeighs.get(curClustroid).getInt(node2nextBestNeighIdx.get(curClustroid));

//			System.out.println("\n\n\ncurClustroid "+curClustroid);
//			System.out.println("curClustroidNeighbour "+curClustroidNeighbour);

			if(!sortedClustroids.contains(curClustroidNeighbour)){
				// the neighbour belongs to an other cluster but sorted clustroid has not ben updated

				sortedClustroids.removeInt(0);
				sortedClustroidVals.removeInt(0);

				// look for a new neighbour for curClustroid
				boolean bestNeighFound = false;
				for(int curClustroidBestNeighIdx = 0 ;
						curClustroidBestNeighIdx < node2sortedNeighs.get(curClustroid).size() ;
						curClustroidBestNeighIdx++){
					int nvClustroidBestNeigh =  node2sortedNeighs.get(curClustroid).getInt(curClustroidBestNeighIdx);

					if( sortedClustroids.contains(nvClustroidBestNeigh)){

						if(clustroid2incompatibles.containsKey(curClustroid))
							if(clustroid2incompatibles.get(curClustroid).contains(nvClustroidBestNeigh))
								continue;

						if(clustroid2cluster.containsKey(curClustroid))
							if(clustroid2cluster.get(curClustroid).contains(nvClustroidBestNeigh))
								continue;

						int val = node2sortedNeighVals.get(curClustroid).getInt(curClustroidBestNeighIdx);

						int idx = Collections.binarySearch(sortedClustroidVals,val);
						idx =idx<0?-(idx+1):idx;
						sortedClustroids.add(idx,curClustroid);
						sortedClustroidVals.add(idx,val);

						node2nextBestNeighIdx.put(curClustroid, curClustroidBestNeighIdx);
						bestNeighFound =true;
						break;
					}
				}
				if(!bestNeighFound){
					if(!clustroid2cluster.containsKey(curClustroid)){
						clustroid2cluster.put(curClustroid,new IntOpenHashSet(1));
						clustroid2cluster.get(curClustroid).add(curClustroid);
					}
				}
				continue;
			}

			// Build the current cluster as the union of curClustroid and curClustroidNeighbour's clusters

			candidates [0] = curClustroid;
			candidates [1] = curClustroidNeighbour;
			for(int c : candidates){
				if( clustroid2cluster.containsKey(c)){
					curCluster.addAll( clustroid2cluster.get(c));
				}else{
					curCluster.add(c);
				}
			}


//			System.out.println("\n\t curCluster "+curCluster);


			// Compute the new clustroid: nvCLustroid and Check that each node in curCluster is valid
			// 	i.e., Check that nvClustroid is  within the diameter limit of all nodes in curCluster


			int nvClustroid= -1;
			int smallestMaxVal= Integer.MAX_VALUE;
			boolean curClusterIsValid = true;

			int [] aCurCluster = curCluster.toIntArray();
			for(int i=0;i<aCurCluster.length;i++){
				int ni =aCurCluster[i];
				int maxNi =Integer.MIN_VALUE;

				for(int j=0;  j < aCurCluster.length ; j++){
					if(j==i)
						continue;
					int nj = aCurCluster[j];
					int val = getEdgeMeasFromTo( unit,  ni,  nj,  ce.router );



					if(val>maxNi){
						maxNi =val;
					}
				}

				if(!curClusterIsValid)
					break;

				if(maxNi<smallestMaxVal){
					smallestMaxVal=maxNi;
					nvClustroid = ni;
				}
			}

			if(smallestMaxVal>limit){
				curClusterIsValid =false;
			}



//			System.out.println("\n\t curClusterIsValid "+curClusterIsValid);
//			System.out.println("\t nvClustroid "+nvClustroid);
//			System.out.println("\t smallestMaxVal "+smallestMaxVal);


			// Remove seen nodes from sortedClustroids and avoid the call of IdexOf
			int nbRemoved = 0;
			int nbRemainClustroids = sortedClustroids.size();
			for(int niIdx =0; niIdx< nbRemainClustroids; niIdx++){
				int ni =sortedClustroids.getInt(niIdx-nbRemoved);
				if(curCluster.contains(ni)){

					sortedClustroids.removeInt(niIdx-nbRemoved);
					sortedClustroidVals.removeInt(niIdx-nbRemoved);
					nbRemoved++;
					if(nbRemoved==curCluster.size())
						break;
				}
			}

//			System.out.println("\n\t cleaned sortedClustroids: "+sortedClustroids);
//			System.out.println("\t cleaned sortedClustroidVals: "+sortedClustroidVals);

			// Add nvClustroid or Re-add  curClustroid and  curClustroidNeighbour to sortedClustroids

			int nbNvClustroids=2;

			if(curClusterIsValid){
				candidates[0]= nvClustroid;
				nbNvClustroids=1;


				// nvCluster is the union of the clusters of curClustroid and curClustroidNeighbour
				clustroid2cluster.remove(curClustroid);
				clustroid2cluster.remove(curClustroidNeighbour);
				clustroid2cluster.put(nvClustroid, curCluster.clone());

				// nvCluster is not compatible with the incompatible of curClustroid and curClustroidNeighbour

				if(clustroid2incompatibles.containsKey(curClustroid) ||
						clustroid2incompatibles.containsKey(curClustroidNeighbour)){

					if (!clustroid2incompatibles.containsKey(nvClustroid))
						clustroid2incompatibles.put(nvClustroid, new IntOpenHashSet(nbRemainClustroids));

					if(clustroid2incompatibles.containsKey(curClustroid))
						clustroid2incompatibles.get(nvClustroid).addAll(clustroid2incompatibles.get(curClustroid));

					if(clustroid2incompatibles.containsKey(curClustroidNeighbour))
						clustroid2incompatibles.get(nvClustroid).addAll(clustroid2incompatibles.get(curClustroidNeighbour));
				}

			}else{

//				curIncompatibles.add(curClustroid);
//				curIncompatibles.add(curClustroidNeighbour);

				if (!clustroid2incompatibles.containsKey(curClustroid))
					clustroid2incompatibles.put(curClustroid, new IntOpenHashSet(nbRemainClustroids));

				if(clustroid2cluster.containsKey(curClustroidNeighbour)){
					clustroid2incompatibles.get(curClustroid).addAll(clustroid2cluster.get(curClustroidNeighbour));
				}else{
					clustroid2incompatibles.get(curClustroid).add(curClustroidNeighbour);
				}

				if (!clustroid2incompatibles.containsKey(curClustroidNeighbour))
					clustroid2incompatibles.put(curClustroidNeighbour, new IntOpenHashSet(nbRemainClustroids));

				if(clustroid2cluster.containsKey(curClustroid)){
					clustroid2incompatibles.get(curClustroidNeighbour).addAll(clustroid2cluster.get(curClustroid));
				}else{
					clustroid2incompatibles.get(curClustroidNeighbour).add(curClustroid);
				}

			}



//			System.out.println("\n\t updated clustroid2cluster: "+clustroid2cluster);
//			System.out.println("\n\t updated clustroid2incompatibles: "+clustroid2incompatibles);


			// update node2nextBestNeighIdx and sortedClustroids, sortedClustroidVals accordingly
			for(int candidateIdx=0;candidateIdx<nbNvClustroids;candidateIdx++){
				nvClustroid = candidates[candidateIdx];
				boolean bestNeighFound = false;
				for(int nvClustroidBestNeighIdx = 0 ;
						nvClustroidBestNeighIdx < node2sortedNeighs.get(nvClustroid).size() ;
						nvClustroidBestNeighIdx++){
					int nvClustroidBestNeigh =  node2sortedNeighs.get(nvClustroid).getInt(nvClustroidBestNeighIdx);
					if(!curCluster.contains(nvClustroidBestNeigh)
							&& sortedClustroids.contains(nvClustroidBestNeigh)){
						if(clustroid2incompatibles.containsKey(nvClustroid))
							if(clustroid2incompatibles.get(nvClustroid).contains(nvClustroidBestNeigh))
								continue;

						int val = node2sortedNeighVals.get(nvClustroid).getInt(nvClustroidBestNeighIdx);

						int idx = Collections.binarySearch(sortedClustroidVals,val);
						idx =idx<0?-(idx+1):idx;
						sortedClustroids.add(idx,nvClustroid);
						sortedClustroidVals.add(idx,val);

						node2nextBestNeighIdx.put(nvClustroid, nvClustroidBestNeighIdx);
						bestNeighFound =true;
						break;
					}
				}

				if(!bestNeighFound ){
					if(!clustroid2cluster.containsKey(nvClustroid)){
						clustroid2cluster.put(nvClustroid,new IntOpenHashSet(1));
						clustroid2cluster.get(nvClustroid).add(nvClustroid);
					}
				}

			}


//			System.out.println("\n\t updated sortedClustroids: "+sortedClustroids);
//			System.out.println("\t updated sortedClustroidVals: "+sortedClustroidVals);
//			System.out.println("\t  clustroid2incompatibles: "+clustroid2incompatibles);

			curCluster.clear();

		}

		//
//		System.out.println("\nFinal clustroid2cluster: "+clustroid2cluster);


		Int2IntOpenHashMap node2Cluster = new Int2IntOpenHashMap(nbNodes);
		int idClust= 0;
		for(int clustroid :clustroid2cluster.keySet()){
			for(int node: clustroid2cluster.get(clustroid)){
				node2Cluster.put( node,clustroid);
			}
			idClust++;
		}

		return node2Cluster;
	}

	public static void main(String args[]){


	}



}
