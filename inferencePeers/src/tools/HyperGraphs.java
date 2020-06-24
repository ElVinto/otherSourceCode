package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author VinTo
 *
 */
public class HyperGraphs {

	
	public static <E  extends Comparable<? super E>> boolean isConnectedIn(ArrayList<E> vertices, HyperGraph<E> hg){
		
		ArrayList<E> toVisit =  new ArrayList<E>();
		toVisit.add(vertices.get(0));
		ArrayList<E> visited = new ArrayList<E>();
		while(!toVisit.isEmpty()){
			E cur = toVisit.remove(0);
			visited.add(cur);
			ArrayList<E> neighsOfCur = hg.getNeighbors(cur);
			for(E neighOfCur :neighsOfCur){
				if(!visited.contains(neighOfCur) && vertices.contains(neighOfCur)){
					toVisit.add(neighOfCur);
				}
			}
		}
		
		if(!visited.containsAll(vertices)){
			toVisit.addAll(vertices);
			toVisit.removeAll(visited);
			System.out.println(" Unconnected buck "+ toVisit);
		}
		
		return visited.containsAll(vertices);
	}
	
	public static <E  extends Comparable<E>> boolean respectRunIntersect(ArrayList<E> arrayList, HyperGraph<AbstCompArrayList<E>> hjt){
		for(E v : arrayList){
			ArrayList<AbstCompArrayList<E>> vBuckets = new ArrayList<AbstCompArrayList<E>>();
			for(AbstCompArrayList<E> hjtBucket : hjt.getVertices()){
				if(hjtBucket.contains(v))
					vBuckets.add(hjtBucket);
			}
			if(!isConnectedIn(vBuckets, hjt)){
				
				System.out.println(" TAKE CARE "+v+" does not induced a connected graph");
				
				return false;
			}
		}
		return true;
	}
	
	// return true if dhjt is a jointree of hg [To FINISh]
	public static <E  extends Comparable<E>> boolean  isAjointreeOf( HyperGraph<AbstCompArrayList<E>> hjt, HyperGraph<E> hg){
		
		// check if hjt contains all vertices of hg
		HashSet<E> hjtElmts = new HashSet<E>();
		for(AbstCompArrayList<E> bucket : hjt.getVertices()){
			hjtElmts.addAll(bucket);
		}
		if(!hjtElmts.containsAll(hg.getVertices())){
			System.out.println("All elements of hg are not contained in htj");
			for(E v: hg.getVertices()){
				if(!hjtElmts.contains(v))
					System.out.println(v+" is not contain");
			}
			return false;
		}
		
		// check if hjt is a tree
		// htj has n-1 arcs 
		if(hjt.nbVertices()!= hjt.nbEdges()+1){
			System.out.println("The graphe does not have n-1 edges nb Vertices:"
					+hjt.getVertices().size()+ " nb edges: "+hjt.getHEdges().size());
			 return false;
		}
		// hjt is connected
		if(!isConnectedIn(new ArrayList<AbstCompArrayList<E>>(hjt.getVertices()), hjt)){
			return false;
		}
		// hjt respect with the running intersection
		return respectRunIntersect(new ArrayList<E> (hg.getVertices()),hjt);
		
	}
	
	public static <E extends Comparable<? super E>> WeightedElmt<E> updateWeightsAndSelectBestNode(
			String minMax, Set<WeightedElmt<E>> v2Update,
			HyperGraph<WeightedElmt<E>> whg) {

		// System.out.println("Le graphe à pondérer par Min fill "+whg);

		for (WeightedElmt<E> wv : v2Update) {

			HashSet<WeightedElmt<E>> seenWVertices = new HashSet<WeightedElmt<E>>();
			for (Collection<WeightedElmt<E>> whe : whg.getHEdges(wv)) {
				seenWVertices.addAll(whe);
			}

			int nbEdges = whg.nbNeighbors(wv);
			for (int i = 0; i < whg.nbNeighbors(wv) - 1; i++) {
				for (int j = i + 1; j < whg.nbNeighbors(wv); j++) {
					if (whg.getNeighbors(whg.getNeighbors(wv).get(i)).contains(
							whg.getNeighbors(wv).get(j))) {
						nbEdges++;
					}
				}
			}

			// System.out.println(wv+" is in "+ whg.getHEdges(wv));
			// Warning here wv is not in whg.nbNeighbors(wv)
			int wvCliqueWidth = (whg.nbNeighbors(wv) * (whg.nbNeighbors(wv) + 1)) / 2;
			wv.setWeight(wvCliqueWidth - nbEdges);
		}

		if (minMax.equals("min"))
			return Collections.min(whg.getVertices());
		if (minMax.equals("max"))
			return Collections.max(whg.getVertices());
		return null;
	}
	
	public static <E  extends Comparable<? super E>> WeightedElmt<E>  selectBestNodeAndUpdateWeights(
			Set<WeightedElmt<E>> v2Update, HyperGraph<WeightedElmt<E>>  whg){
		
		//	System.out.println("Le graphe ponderer par Min fill "+whg);
		
		for(WeightedElmt<E> wv :v2Update){
		
			HashSet<WeightedElmt<E>> seenWVertices = 
				new HashSet<WeightedElmt<E>>() ;
			for(Collection<WeightedElmt<E>> whe :whg.getHEdges(wv)){
				seenWVertices.addAll(whe);
			}
			
			int nbEdges=whg.nbNeighbors(wv) ;
			for(int i=0;i<whg.nbNeighbors(wv)-1;i++){
				for(int j=i+1;j<whg.nbNeighbors(wv);j++){
					if(	whg.getNeighbors(whg.getNeighbors(wv).get(i)).contains(whg.getNeighbors(wv).get(j)) ){
						nbEdges++;
					}
				}
			}
			
//			System.out.println(wv+" is in "+ whg.getHEdges(wv));
			// Warning here wv is not in whg.nbNeighbors(wv)
			int wvCliqueWidth =  (whg.nbNeighbors(wv)*(whg.nbNeighbors(wv)+1))/2;
			wv.setWeight(wvCliqueWidth -nbEdges);
		}
		
//		System.out.println("Le graphe pondéré par Min fill "+whg);
		
		
		
		return Collections.min(whg.getVertices());
	}
	
	public static <E  extends Comparable<? super E>> ArrayList<WeightedElmt<E>>  selectBestNodesAndUpdateWeights(
			Set<WeightedElmt<E>> v2Update, HyperGraph<WeightedElmt<E>>  whg){
		
		WeightedElmt<E> selectedVertex =
			selectBestNodeAndUpdateWeights(v2Update,whg);
		
		ArrayList<WeightedElmt<E>> selectedVertices  = new ArrayList<WeightedElmt<E>>();
		selectedVertices.add(selectedVertex);
		for(WeightedElmt<E> n: whg.getNeighbors(selectedVertex)){
			if(whg.getNeighbors(selectedVertex).size()==whg.getNeighbors(n).size()){
				int nbIncommon = 0;
				for(WeightedElmt<E> nOfn :whg.getNeighbors(n)){
					if(whg.getNeighbors(selectedVertex).contains(nOfn))
						nbIncommon ++;
				}
				if(nbIncommon==  whg.getNeighbors(selectedVertex).size()-1)
					selectedVertices.add(n);
				
			}
		}
		
		return selectedVertices;
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	public static <E  extends Comparable<? super E>> ArrayList<E>  minFillInvOrder(HyperGraph<E> hg){
		
		// transforms the input hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>>  whg = new HyperGraph<WeightedElmt<E>>();
		for(Collection<E> he:hg.getHEdges()){
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for(E v : he)
				whedge.add(new WeightedElmt<E>(v));
			whg.addHedge(whedge);
		}
		
//		System.out.println("Le graphe pondéréé: "+whg);
		
		// iteratively add the best node to the returned list, 
		// remove it from the graph, evaluate the remaining weighted hyper graph
		ArrayList<E> invOrder = new ArrayList<E>();
		
		HashSet<WeightedElmt<E>> v2Update = 
			new HashSet<WeightedElmt<E>>(whg.getVertices());
		
		while(!whg.getVertices().isEmpty()){
//			System.out.println("Le graphe pondéréé: "+whg);
			
			ArrayList<WeightedElmt<E>> selectedVertices =
				selectBestNodesAndUpdateWeights(v2Update,whg);
			v2Update.clear();
			for(WeightedElmt<E> selectedVertex: selectedVertices){
				
//				System.out.println("selectedVertex: "+selectedVertex+"\n");
				invOrder.add(0,selectedVertex.getElement());
				HashSet<ArrayList<WeightedElmt<E>>> associatedHEdges =
					whg.removeVertex(selectedVertex);
				
				for(ArrayList<WeightedElmt<E>> ahe: associatedHEdges){
					whg.removeHEdge(ahe);
					v2Update.addAll(ahe);
				}
					
			}
			v2Update.removeAll(selectedVertices);
//			System.out.println("new HE "+ v2Update+" induced by the removing of "+ selectedVertices);
			whg.addHedge(v2Update);		
			
			HashSet<WeightedElmt<E>> v2UpdateBis  = (HashSet<WeightedElmt<E>>) v2Update.clone();
			for(WeightedElmt<E> v : v2UpdateBis){
				for(ArrayList<WeightedElmt<E>> ahe: whg.getHEdges(v)){
					v2Update.addAll(ahe);}
			}
		}
		return invOrder;
	}
	
	
	
	public static <E  extends Comparable<? super E>> WeightedElmt<E>  selectBestMCSNodeAndUpdateWeights(
			Set<WeightedElmt<E>> v2Update, HyperGraph<WeightedElmt<E>>  whg, String maxOrMin){
		
		//	System.out.println("Le graphe ponderer par MCS  "+whg);
		
		for(WeightedElmt<E> wv :v2Update){
		
			HashSet<WeightedElmt<E>> seenWVertices = 
				new HashSet<WeightedElmt<E>>() ;
			for(Collection<WeightedElmt<E>> whe :whg.getHEdges(wv)){
				seenWVertices.addAll(whe);
			}
			
			wv.setWeight(whg.nbNeighbors(wv));
		}
		
		
		if(maxOrMin.equals("min"))
			return Collections.min(whg.getVertices());
		else
			return Collections.max(whg.getVertices());
	}
	
	public static <E  extends Comparable<? super E>> ArrayList<WeightedElmt<E>>  selectBestMCSNodesAndUpdateWeights(
			Set<WeightedElmt<E>> v2Update, HyperGraph<WeightedElmt<E>>  whg, String maxOrMin){
		
		WeightedElmt<E> selectedVertex =
			selectBestMCSNodeAndUpdateWeights(v2Update,whg, maxOrMin);
		
		ArrayList<WeightedElmt<E>> selectedVertices  = new ArrayList<WeightedElmt<E>>();
		selectedVertices.add(selectedVertex);
		for(WeightedElmt<E> n: whg.getNeighbors(selectedVertex)){
			if(whg.getNeighbors(selectedVertex).size()==whg.getNeighbors(n).size()){
				int nbIncommon = 0;
				for(WeightedElmt<E> nOfn :whg.getNeighbors(n)){
					if(whg.getNeighbors(selectedVertex).contains(nOfn))
						nbIncommon ++;
				}
				if(nbIncommon==  whg.getNeighbors(selectedVertex).size()-1)
					selectedVertices.add(n);
				
			}
		}
		
		return selectedVertices;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <E  extends Comparable<? super E>> ArrayList<E>  mCSOrder(HyperGraph<E> hg, String maxOrMin){
		
		// transforms the input hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>>  whg = new HyperGraph<WeightedElmt<E>>();
		for(Collection<E> he:hg.getHEdges()){
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for(E v : he)
				whedge.add(new WeightedElmt<E>(v));
			whg.addHedge(whedge);
		}
		
//		System.out.println("Le graphe pondéréé: "+whg);
		
		// iteratively add the best node to the returned list, 
		// remove it from the graph, evaluate the remaining weighted hyper graph
		ArrayList<E> order = new ArrayList<E>();
		
		HashSet<WeightedElmt<E>> v2Update = 
			new HashSet<WeightedElmt<E>>(whg.getVertices());
		
		while(!whg.getVertices().isEmpty()){
//			System.out.println("Le graphe pondéréé: "+whg);
			
			ArrayList<WeightedElmt<E>> selectedVertices =
				selectBestMCSNodesAndUpdateWeights(v2Update,whg, maxOrMin);
			v2Update.clear();
			for(WeightedElmt<E> selectedVertex: selectedVertices){
				
//				System.out.println("selectedVertex: "+selectedVertex+"\n");
				if(maxOrMin.equals("min"))
					order.add(0,selectedVertex.getElement());
				else
					order.add(selectedVertex.getElement());
				
				HashSet<ArrayList<WeightedElmt<E>>> associatedHEdges =
					whg.removeVertex(selectedVertex);
				
				for(ArrayList<WeightedElmt<E>> ahe: associatedHEdges){
					whg.removeHEdge(ahe);
					v2Update.addAll(ahe);
				}
					
			}
			v2Update.removeAll(selectedVertices);
//			System.out.println("new HE "+ v2Update+" induced by the removing of "+ selectedVertices);
			whg.addHedge(v2Update);		
			
			HashSet<WeightedElmt<E>> v2UpdateBis  = (HashSet<WeightedElmt<E>>) v2Update.clone();
			for(WeightedElmt<E> v : v2UpdateBis){
				for(ArrayList<WeightedElmt<E>> ahe: whg.getHEdges(v)){
					v2Update.addAll(ahe);}
			}
		}
		return order;
	}
	
	@SuppressWarnings("unchecked")
	public static <E  extends Comparable<? super E>>  HyperGraph<E> bucketElimination(
			HyperGraph<E> hg, 
			ArrayList<E> order, 
			ArrayList<ArrayList<E>> buckets, // out buckets.get(i) corresponds to the bucket of the ith vertex of order.get(i)
			E[] varFather){  // out fathers.[i] corresponds to the bucket of the ith vertex of order.get(i)
		// each variable is  associated to a bucket where it appears at first position
		/// the  buckets follow the variable order
		for(int i =0;i<order.size();i++){
			E vertex = order.get(i);
			ArrayList<E> nvHe = new ArrayList<E>();
			nvHe.add(vertex);
			buckets.add(nvHe);
		}
		
		
		// Given an "he", its vertices will be add to the bucket of it higher vertex.
		for(ArrayList<E> he:hg.getHEdges()){
			int maxIndexV = 0;
			for(E v : he)
				if(order.indexOf(v)>maxIndexV)
					maxIndexV = order.indexOf(v);
			for(E v : he){
				if(!buckets.get(maxIndexV).contains(v))
					buckets.get(maxIndexV).add(v);
			}
			
		}

//		System.out.println("buckets");
//		for(ArrayList<E> bucket: buckets){
//			System.out.println(bucket);
//		}
//		System.out.println();
//		System.out.println("order "+order);
//		System.out.println("buckets "+buckets);

		for(int i =(buckets.size()-1);i>=0;i--){
			int maxIndexV = 0;
			for(E v : buckets.get(i))
				if(order.indexOf(v)>maxIndexV &&  order.indexOf(v)<i){
//					System.out.println("v "+v+"  order.indexOf(v) "+order.indexOf(v)+
//							"  maxIndexV "+maxIndexV+"  i "+i);
					maxIndexV = order.indexOf(v);
				}
			
			varFather[i]=order.get(maxIndexV);
			
			ArrayList<E> projectout = new ArrayList<E> (buckets.get(i));
			projectout.remove(buckets.get(i).get(0));
			
			// test => Not a jointree
//			if(projectout.size()>0)
//			 projectout.remove(projectout.size()-1);
			
			for(E e : projectout)
				if(!buckets.get(maxIndexV).contains(e))
					buckets.get(maxIndexV).add(e);
	
		}	
//		System.out.println("buckets after project out ");
//		for(int i = 0;i < buckets.size(); i++){
//			System.out.println(order.get(i)+" "+varFather[i]+" "+buckets.get(i));
//		}
//		System.out.println();
		HyperGraph<E> jt = new HyperGraph<E>();
		for(ArrayList<E> b : buckets)
			jt.addHedge(b);
		
		return jt;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> ArrayList<E> dFSOrder(
			HyperGraph<E> g) {

		ArrayList<E> dFSOrder = new ArrayList<E>();

		// we chose a random peer as starter
		//@SuppressWarnings("unchecked")
		E vStarter = (E) g.getVertices().toArray()[(int) (Math.random()
				* g.nbVertices())];
		// debug
		vStarter = (E) g.getVertices().toArray()[0];
		
		visiteDFSNode(vStarter, dFSOrder, g);

		// we built a tree by a depth first search algorithm
		
		return dFSOrder;
	}
	
	private static <E extends Comparable<E>> void  visiteDFSNode(
			E vCurrent,
			ArrayList<E> dFSOrder,
			HyperGraph<E> g) {
		
		dFSOrder.add(vCurrent);
		
		ArrayList<E> unvisitedNeigh = new ArrayList<E>();
		for (E vNeigh : g.getNeighbors(vCurrent))
			if(! dFSOrder.contains(vNeigh))
				unvisitedNeigh.add(vNeigh);
		
		Collections.shuffle(unvisitedNeigh);
		for(E vNeigh : unvisitedNeigh)
			if(! dFSOrder.contains(vNeigh))
				visiteDFSNode(vNeigh, dFSOrder, g);
	}
	
	public static <E extends Comparable<E>> ArrayList<E> dFSOrderMaxFillIn(
			HyperGraph<E> g) {

		
		// transforms the hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
		for (Collection<E> he : g.getHEdges()) {
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for (E v : he)
				whedge.add(new WeightedElmt<E>(v));
			whg.addHedge(whedge);
		}
		HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(
				whg.getVertices());

		@SuppressWarnings("unchecked")
		WeightedElmt<E> wvStarter = updateWeightsAndSelectBestNode("max", v2Update, whg);
		
		ArrayList<WeightedElmt<E>> wdFSOrder = new ArrayList<WeightedElmt<E>>();
		visiteDFSNodeMaxFillIn(wvStarter, wdFSOrder, whg);

		ArrayList<E> dFSOrder = new ArrayList<E>();
		for(int i=0;i<wdFSOrder.size(); i++)
			dFSOrder.add(i,wdFSOrder.get(i)._elmt);
		return dFSOrder;
	}
	
	private static <E extends Comparable<E>> void  visiteDFSNodeMaxFillIn(
			WeightedElmt<E> wvCurrent,
			ArrayList<WeightedElmt<E>> wdFSOrder,
			HyperGraph<WeightedElmt<E>> whg
			) {
		
		wdFSOrder.add(wvCurrent);
//		nodeDepth.add(depth+1);
		
		while( !wdFSOrder.containsAll(whg.getNeighbors(wvCurrent))){
			WeightedElmt<E> bestWVNeigh = null;
			float bestScore =0;
			for (WeightedElmt<E> wvNeigh : whg.getNeighbors(wvCurrent)){
				if(! wdFSOrder.contains(wvNeigh)){
					if(bestWVNeigh == null)
						bestWVNeigh = wvNeigh;
					// compute the number of ancestors (child has as least a father)
					float score = wvNeigh._weight;
					if(score > bestScore){
						bestScore = score;
						bestWVNeigh = wvNeigh;
					}
						
				}
			}
			visiteDFSNodeMaxFillIn(bestWVNeigh, wdFSOrder, whg);
		}
	}
	
	public static <E extends Comparable<E>> ArrayList<E> dFSOrderMaxAncestors(
			HyperGraph<E> g) {

		ArrayList<E> dFSOrder = new ArrayList<E>();

		// we chose a random peer as starter
		@SuppressWarnings("unchecked")
		E vStarter = (E) g.getVertices().toArray()[(int) Math.random()
				* g.nbVertices()];
		
		visiteDFSNodeMaxNeigh(vStarter, dFSOrder, g);

		// we built a tree by a depth first search algorithm
		
		return dFSOrder;
	}
	
	private static <E extends Comparable<E>> void  visiteDFSNodeMaxAncestors(
			E vCurrent,
			ArrayList<E> dFSOrder,
//			ArrayList<Integer> nodeDepth,
//			int depth,
			HyperGraph<E> g) {
		
		dFSOrder.add(vCurrent);
//		nodeDepth.add(depth+1);
		
		while( !dFSOrder.containsAll(g.getNeighbors(vCurrent))){
			E bestVNeigh = null;
			Integer bestScore =0;
			for (E vNeigh : g.getNeighbors(vCurrent)){
				if(! dFSOrder.contains(vNeigh)){
					// compute the number of ancestors (child has as least a father)
					int score = 1;
					for(E nofN :g.getNeighbors(vNeigh))
						if(dFSOrder.contains(nofN))
							score ++;
					if(score > bestScore){
						bestScore = score;
						bestVNeigh = vNeigh;
					}
						
				}
			}
			visiteDFSNodeMaxAncestors(bestVNeigh, dFSOrder, //nodeDepth,depth,
					 g);
		}
	}
	
	
	
	public static <E extends Comparable<E>> ArrayList<E> dFSOrderMaxNeigh(
			HyperGraph<E> g) {

		ArrayList<E> dFSOrder = new ArrayList<E>();


		
		E bestV = null;
		Integer bestScore =0;
		for (E vNeigh : g.getVertices()){
			if(! dFSOrder.contains(vNeigh)){
				if(bestV == null)
					bestV = vNeigh;
				// compute the number of unvisited neighbors
				int score = g.getNeighbors(vNeigh).size()+1;

//				for(E nofN :g.getNeighbors(vNeigh))
//					if(dFSOrder.contains(nofN))
//						score --;
				if(score > bestScore){
					bestScore = score;
					bestV = vNeigh;
				}
					
			}
		}
		
		visiteDFSNodeMaxNeigh(bestV, dFSOrder, g);

		// we built a tree by a depth first search algorithm
		
		return dFSOrder;
	}
	
	private static <E extends Comparable<E>> void  visiteDFSNodeMaxNeigh(
			E vCurrent,
			ArrayList<E> dFSOrder,
			HyperGraph<E> g) {
		
		dFSOrder.add(vCurrent);
//		nodeDepth.add(depth+1);
		
		while( !dFSOrder.containsAll(g.getNeighbors(vCurrent))){
			E bestVNeigh = null;
			Integer bestScore =0;
			for (E vNeigh : g.getNeighbors(vCurrent)){
				if(! dFSOrder.contains(vNeigh)){
					if(bestVNeigh == null)
						bestVNeigh = vNeigh;
					// compute the number of unvisited neighbors
					int score = g.getNeighbors(vNeigh).size()+1;
	
//					for(E nofN :g.getNeighbors(vNeigh))
//						if(dFSOrder.contains(nofN))
//							score --;
					if(score > bestScore){
						bestScore = score;
						bestVNeigh = vNeigh;
					}
						
				}
			}
			visiteDFSNodeMaxNeigh(bestVNeigh, dFSOrder, g);
		}
	}
	
	
	public static <E extends Comparable<E>> ArrayList<E> bFSOrder(
			HyperGraph<E> g) {
		ArrayList<E> bFSOrder = new ArrayList<E>();

		//for (E vertex : g.getVertices())
		//	bFSOrder.add(vertex);
				
		// we chose a random peer as starter
		@SuppressWarnings("unchecked")
		E vStarter = (E) g.getVertices().toArray()[(int) (Math.random()
				* g.nbVertices())];
		// debug
//		vStarter = (E) g.getVertices().toArray()[0];
		
		
		ArrayList<E> file = new ArrayList<E>();
		file.add(vStarter);
		
		// we built a tree by a bread first search algorithm
		while (!file.isEmpty()) {
			E vCurrent = file.remove(0);
			if (!bFSOrder.contains(vCurrent)) {
				bFSOrder.add(vCurrent);
				for (E vNeigh : g.getNeighbors(vCurrent)) {
					if (!bFSOrder.contains(vNeigh)) {
						file.add(vNeigh);
					}
				}
			}
		}
		
		// System.out.println(" \n bFSOrder "+ bFSOrder);
		
		return bFSOrder;
	}
 

	/**
	 * @param <E>
	 * @param hg
	 * @param order
	 * @param buckets
	 * @param varFather
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E  extends Comparable<? super E>> HyperGraph<E> localRefinement(
			HyperGraph<E> hg,
			ArrayList<E> order,
			ArrayList<ArrayList<E>> buckets,
			E[] varFather,
			HashMap<Integer,ArrayList<Integer>> childrenOf // out
			){ 		

		// WARNING HERE POSITION in Order is used as IDENTIFIANT and will not represent the order . 
		for(int i=0; i<order.size();i++){
			
			childrenOf.put(i, new ArrayList<Integer>());
			E e= order.get(i);
			if(!varFather[i].equals(e)){
				childrenOf.get(order.indexOf(varFather[i])).add(i);
			}
		}
		
		// From leaves to roots
		for(int i=varFather.length-1;i>=0;i--){
			E e = order.get(i);
//			System.out.println("cleanning pipe from "+ e+" at "+i );
			
			cleanPipe(i, e, hg, order, buckets, childrenOf);
		}
		
		HyperGraph<E> jt = new HyperGraph<E>();
		for(ArrayList<E> b : buckets)
			jt.addHedge(b);
		
		return jt;
	}
	
	
	/**
	 * @param <E>
	 * @param i,  represents the index of the current bucket 
	 * @param e,  represents the current element of nothing
	 * @param hg  is the hypergraph of the theory
	 * @param order represents the updated order
	 * @param buckets corresponds to the buckets s.t. buckets.get(i) is the buckets of ith vertex of order.get(i)
	 * @param childrenOf represents the JoinTree s.t. childrenOf.get(i) is the children of order.get(i)
	 */
	private static <E  extends Comparable<? super E>>  void cleanPipe(
			int i, 
			E e, 
			HyperGraph<E> hg, 
			ArrayList<E> order,
			ArrayList<ArrayList<E>> buckets,
			HashMap<Integer,ArrayList<Integer>> childrenOf
			){
		
		// if i has children
		//if(!childrenOf.get(i).isEmpty()){	
			
			// if exactly one child of buckets i contains e
			int nbChildWith_e = 0;
			int iOnlyChildWith_e = -1;
			for(Integer j: childrenOf.get(i)){
				if(buckets.get(j).contains(e)){
					nbChildWith_e ++;
					iOnlyChildWith_e = j;
				}
			}
//			System.out.println(" nbChildWith "+e+" "+nbChildWith_e);
			// if all edges from hg in buckets i remain in bucket iOnlyChildWith_e
			boolean conserveEdges = ( buckets.get(i).size() >= 1) ;
			 if(nbChildWith_e == 1){
				
				for(E f : buckets.get(i)  ){
//					System.out.println("  "+ e+" -  "+ f);
//					System.out.println("  hg.getNeighbors("+e+")"+hg.getNeighbors(e)+ " contains "+f+" ?");
//					System.out.println("  buckets.get("+iOnlyChildWith_e+")"+buckets.get(iOnlyChildWith_e)+
//							 " contains "+f+" ?");
					if(!e.equals(f))
						if(hg.getNeighbors(e).contains(f))
							if(!buckets.get(iOnlyChildWith_e).contains(f)){
								conserveEdges =false;
								break;
							}
				}
//				System.out.println(" all edges from hg in bucket "+i+" remain in bucket "
//						+iOnlyChildWith_e+ " -> "+ conserveEdges );
			 }
				if(conserveEdges && nbChildWith_e == 1){
					buckets.get(i).remove(e);
					cleanPipe(iOnlyChildWith_e, e, hg, order, buckets, childrenOf);
					
				}else{
					// if we precede the elimination of e
					if(order.indexOf(e) != i){
						
						// eliminating e before i if  the neihbors of e in bucket(i) is a strict superset of the neighbors of e in hg
						
						buckets.get(i).remove(e);
						if(buckets.get(i).containsAll(hg.getNeighbors(e)) && 
								!hg.getNeighbors(e).containsAll(buckets.get(i))){
							
							// reorder 
							ArrayList<E> nvBuck = new ArrayList<E>();
							nvBuck.add(e);
							nvBuck.addAll(hg.getNeighbors(e));
							order.add(e);
							int iNvBuck = order.size()-1;
							buckets.add(iNvBuck,nvBuck);
							childrenOf.put(iNvBuck, new ArrayList<Integer>());
							
						
								ArrayList<Integer> toReconnect = new ArrayList<Integer>();
								for(Integer iChild: childrenOf.get(i)){
									if(buckets.get(iChild).contains(e)){
										toReconnect.add(iChild);
									}
								}
								for(Integer iChild: toReconnect){
									// deconnect  iChild bucket from iOnlyChildWith_e
									childrenOf.get(i).remove(iChild);
									// connect  iChild bucket to nvBuck
									childrenOf.get(iNvBuck).add(iChild);
									// project elements from ichild to nvBuck
									for(E f: buckets.get(iChild)){
										if(!buckets.get(iNvBuck).contains(f))
											buckets.get(iNvBuck).add(f);
									}
								}
							
							// connect nvBuck to i (projection has been made implicitly)
							childrenOf.get(i).add(iNvBuck);
						}else{
							buckets.get(i).add(e);
						}
					}
				}
	}
	
	
	
	
	//
	// Functions for handling network of P2P
	//
	


	public static HashMap<String,ArrayList<String>> dir2PeersVoc(File dir){
		ArrayList<String> litsString= new ArrayList<String>();
		ArrayList<ArrayList<String>> allTh= new ArrayList<ArrayList<String>>();
		ArrayList<String> target = new ArrayList<String>();
		sat4JAdapt.Sat4J.dir2OneDesc(dir, litsString, target, allTh);
		
		HashMap<String,ArrayList<String>> peers2Vocs = new HashMap<String,ArrayList<String>>();
		for(ArrayList<String> cl : allTh){
			ArrayList<String> relatedPeers = new ArrayList<String>() ;
			for(String lit : cl){
				String var = lit.replace("!", "");
				Collections.replaceAll(cl, lit,var);
				String pName = var.split(":")[0];
				if(!relatedPeers.contains(pName))
					relatedPeers.add(pName);
				if(!peers2Vocs.containsKey(pName)){
					peers2Vocs.put(pName, new ArrayList<String>());
					peers2Vocs.get(pName).add(var);
				}
			}
			//			System.out.println("peers appearing in the formula "+ cl+relatedPeers);
			for(String var: cl){
				for(String pName : relatedPeers)
					if(!peers2Vocs.get(pName).contains(var))
						peers2Vocs.get(pName).add(var);
			}	
		}
		return peers2Vocs;
	}
	
	
	public static HashMap<String,ArrayList<String>> neighborsMap(File fdot){
		HashMap<String, ArrayList<String>> neighborsMap = new HashMap<String, ArrayList<String>>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(fdot));
			String line = br.readLine();
			while(line!=null){
					String pName = "p"+line.split("->")[0].trim();
					ArrayList<String> neighbors = new ArrayList<String>();
					for(String p : (line.split("->")[1].trim()).split(" ")){
						neighbors.add("p"+p);
					}
					neighborsMap.put(pName, neighbors);
					
				line = br.readLine();
			}
			br.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return neighborsMap;
	}
	
	public static int depth(String p, HashMap<String,String> peers2Father){
		int depth = 0;
		String pred = p;
		while(! peers2Father.get(pred).equals(pred)){
			pred = peers2Father.get(pred);
			depth++;
		}
		return depth;
	}
	
	
	public static void createStatsFiles(File dirIn,File dirOut){
		HashMap<String,String> metricsMap = new HashMap<String,String>();
		Chrono chrono = new Chrono();
		chrono.start();
		
		HashMap<String,ArrayList<String>> peers2VocsInit = dir2PeersVoc(dirIn);
		
		HyperGraph<String> hg = new HyperGraph<String>();
		for(ArrayList<String> peerVoc :peers2VocsInit.values()){
			hg.addHedge(peerVoc);
		}
		
		metricsMap.put("parsingTime",""+chrono.time());
		chrono.reStart();
		
		System.out.println(" Le graphe "+hg);
		ArrayList<String> order = minFillInvOrder(hg);
		System.out.println(" les noeuds ordonnés "+order+"\n");
		
		long orderTime =chrono.time();
		metricsMap.put("orderTime",""+orderTime);
		chrono.reStart();
		
		ArrayList<ArrayList<String>> buckets = new ArrayList<ArrayList<String>>(order.size());
		String [] varFather = new String[order.size()];
		HyperGraph<String> hjtp = bucketElimination(hg,order,buckets,varFather);
		System.out.println(" jointree par BE "+hjtp);
		
		
		
		long beTime = chrono.time();
		metricsMap.put("beTime",""+beTime);
		chrono.reStart();
		
		long workingTime = beTime+orderTime;
		metricsMap.put("workingTime",""+workingTime);
		
		HashMap<String,ArrayList<String>> peers2VocsEnd = new HashMap<String, ArrayList<String>>();
		HashMap<String,String> peers2Father = new HashMap<String, String>();
		
		for(String p: peers2VocsInit.keySet()){
			int minIndVarFather = order.size();
			for(String initVar: peers2VocsInit.get(p)){
			int indFather= order.indexOf(varFather[order.indexOf(initVar)]);
			if(indFather<minIndVarFather)
				minIndVarFather=indFather;
			}
			peers2Father.put(p, order.get(minIndVarFather).split(":")[0]);
		}
		
		for(String p: peers2VocsInit.keySet()){
			
			
			peers2VocsEnd.put(p,new ArrayList<String>());
			for(String initVar: peers2VocsInit.get(p)){
				if(!peers2VocsEnd.get(p).contains(initVar))
					peers2VocsEnd.get(p).add(initVar);
				for( String nvVar :buckets.get(order.indexOf(initVar))){
					if(!peers2VocsEnd.get(p).contains(nvVar))
						peers2VocsEnd.get(p).add(nvVar);
				}
			}
			
			HashMap<String,ArrayList<String>> neighborsMap =
				neighborsMap(new File(dirIn.getAbsoluteFile()+File.separator+"graph.dot"));
			metricsMap.put("neighbors",""+neighborsMap.get(p));
			metricsMap.put("nbNeighbors",""+neighborsMap.get(p).size());
			metricsMap.put("depth", ""+depth(p,peers2Father));
			metricsMap.put("liveTime",""+workingTime+orderTime);
			metricsMap.put("pName",p);
			metricsMap.put("father", peers2Father.get(p));
			metricsMap.put("nbVarsAtStart",""+peers2VocsInit.get(p).size());
			metricsMap.put("nbVarsAtEnd", ""+peers2VocsEnd.get(p).size());
			metricsMap.put("nbSharedAtEnd", ""+(peers2VocsEnd.get(p).size()-peers2VocsInit.get(p).size()));
			metricsMap.put("nbSentMsg", ""+((neighborsMap.containsKey(peers2Father.get(p)))?100:0));
			
			createPeerFile(dirOut,metricsMap);
		}
		
		
		
		
		
//		for(String p:peers2VocsEnd.keySet()){
//			System.out.println("peer "+p+" initVoc:"+ peers2VocsInit.get(p));
//			System.out.println("peer "+p+"  endVoc:"+peers2VocsEnd.get(p));
//		}
		
				
	}
	
	
	public static void createPeerFile(File dirOut, HashMap<String,String> metricsMap){
		
		try {
		
		Dprint _dprint = new Dprint(dirOut.getAbsolutePath()+File.separatorChar+
				metricsMap.get("pName")+".stat");
		
		//Dprint.println("running DPLL for " + _name);
		_dprint.writeStat("father: "+metricsMap.get("father")+"\n");
			
		_dprint.writeStat("depth: "+metricsMap.get("depth")+"\n");
		_dprint.writeStat("neighbors: "+metricsMap.get("neighbors")+"\n");
		_dprint.writeStat("finishMode: "+0+"\n");
		_dprint.writeStat("finishAt: "+0+"\n");
		
		
		_dprint.writeStat("firstMsgTime: "+metricsMap.get("parsingTime")+"\n");
		_dprint.writeStat("treeTime: "+metricsMap.get("orderTime")+"\n");
		_dprint.writeStat("dpllTime: "+0 +"\n");
		_dprint.writeStat("liveTime: "+metricsMap.get("liveTime")+"\n");
		_dprint.writeStat("workingTime: "+metricsMap.get("workingTime")+"\n");
		_dprint.writeStat("waitingTime: "+0+"\n");
		_dprint.writeStat("sumProductTime: "+(0)+"\n");
		_dprint.writeStat("maxProductTime: "+(0)+"\n");
		
		_dprint.writeStat("sumAddResultTime: "+(0)+"\n");
		_dprint.writeStat("maxAddResultTime: "+(0)+"\n");
		
		_dprint.writeStat("sumAddRImpltTime: "+(0)+"\n");
		_dprint.writeStat("maxAddRImplTime: "+(0)+"\n");
	
		_dprint.writeStat("maxNbRImplicantsStored: "+0+"\n");
		
		_dprint.writeStat("nbClauses: "+0+"\n");
		
		_dprint.writeStat("nbVarsAtStart: "+metricsMap.get("nbVarsAtStart") +"\n");
		_dprint.writeStat("nbVarsAtEnd: "+metricsMap.get("nbVarsAtEnd")+"\n");
		_dprint.writeStat("nbSharedAtEnd: "+metricsMap.get("nbSharedAtEnd")+"\n");
		_dprint.writeStat("nbNeighbors: "+metricsMap.get("nbNeighbors")+"\n");
		
		int nbRcvdMsgs = 0;
		int nbSentMsgs =0;
		

		_dprint.writeStat("nbRcvdMsg: "+nbRcvdMsgs+"\n");
		_dprint.writeStat("nbSentMsg: "+nbSentMsgs+"\n");
		
		_dprint.closeStatWriter();
		
	} catch (IOException e) {
		e.printStackTrace();
	}
	}

	
	public static HyperGraph<String> peersVoc2HyperGraphOfPeers(File dirIn){
		HashMap<String,ArrayList<String>> peers2Vocs = dir2PeersVoc(dirIn);
		
		HyperGraph<String> hg = new HyperGraph<String>();
		for(ArrayList<String> peerVoc :peers2Vocs.values()){
			hg.addHedge(peerVoc);
		}
		
//		System.out.println(" Le graphe "+hg);
		ArrayList<String> order = minFillInvOrder(hg);
//		System.out.println(" les noeuds ordonnés "+order+"\n");
		
		ArrayList<ArrayList<String>> buckets = new ArrayList<ArrayList<String>>(order.size());
		String[] fathers = new String[order.size()];
		HyperGraph<String> jt = bucketElimination(hg,order,buckets,fathers);
		// System.out.println(" jointree par BE "+jt);
		return jt;
	}
	
	
	public static HyperGraph<String> peerDesciptions2HyperGraphOfVariables(File dir){
		ArrayList<String> litsString= new ArrayList<String>();
		ArrayList<ArrayList<String>> allTh= new ArrayList<ArrayList<String>>();
		ArrayList<String> target = new ArrayList<String>();
		sat4JAdapt.Sat4J.dir2OneDesc(dir, litsString, target, allTh);
		
		HyperGraph<String> hg = new HyperGraph<String>();
		
		for(ArrayList<String> cl : allTh){
			for(String lit : cl){
				Collections.replaceAll(cl, lit,lit.replace("!", ""));
			}
			hg.addHedge(cl);
		}
		
		ArrayList<String> order = minFillInvOrder(hg);
		HyperGraph<String> jt = bucketElimination(hg,order, 
				new ArrayList< ArrayList<String>>(), new String[order.size()]);
		System.out.println(" jointree par BE "+jt);
		return jt;
		
	}
	
	public static HyperGraph<String> exBA3(){
		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("a","b"));
		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("a","c"));
		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("a","d"));
		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("a","f"));
		ArrayList<String> row5 = new ArrayList<String>(Arrays.asList("a","g"));
		ArrayList<String> row6 = new ArrayList<String>(Arrays.asList("a","k"));
		
		ArrayList<String> row7 = new ArrayList<String>(Arrays.asList("b","k"));
		
		ArrayList<String> row9 = new ArrayList<String>(Arrays.asList("d","e"));
		ArrayList<String> row10 = new ArrayList<String>(Arrays.asList("d","i"));
		
		ArrayList<String> row11 = new ArrayList<String>(Arrays.asList("e","j"));
		
		ArrayList<String> row12 = new ArrayList<String>(Arrays.asList("g","h"));
		ArrayList<String> row13 = new ArrayList<String>(Arrays.asList("g","i"));
		
		ArrayList<String> row14 = new ArrayList<String>(Arrays.asList("i","l"));


		HyperGraph<String> dhg = new HyperGraph<String>();
		dhg.addHedge(row1);
		dhg.addHedge(row2);
		dhg.addHedge(row3);
		dhg.addHedge(row4);
		dhg.addHedge(row5);
		dhg.addHedge(row6);
		dhg.addHedge(row7);
		dhg.addHedge(row9);
		dhg.addHedge(row10);
		dhg.addHedge(row11);
		dhg.addHedge(row12);
		dhg.addHedge(row13);
		dhg.addHedge(row14);
		
		return dhg;
	}
	
	
	public static HyperGraph<String> exBA2(){
		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("a","b"));
		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("a","c"));
		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("a","e"));
		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("a","f"));
		ArrayList<String> row5 = new ArrayList<String>(Arrays.asList("a","i"));
		ArrayList<String> row6 = new ArrayList<String>(Arrays.asList("a","j"));
		
		ArrayList<String> row7 = new ArrayList<String>(Arrays.asList("b","d"));
		ArrayList<String> row8 = new ArrayList<String>(Arrays.asList("b","e"));
		ArrayList<String> row9 = new ArrayList<String>(Arrays.asList("b","h"));
		ArrayList<String> row10 = new ArrayList<String>(Arrays.asList("b","i"));
		ArrayList<String> row11 = new ArrayList<String>(Arrays.asList("b","j"));
		
		ArrayList<String> row12 = new ArrayList<String>(Arrays.asList("d","h"));
		ArrayList<String> row13 = new ArrayList<String>(Arrays.asList("d","g"));
		
		ArrayList<String> row14 = new ArrayList<String>(Arrays.asList("e","h"));


		HyperGraph<String> dhg = new HyperGraph<String>();
		dhg.addHedge(row1);
		dhg.addHedge(row2);
		dhg.addHedge(row3);
		dhg.addHedge(row4);
		dhg.addHedge(row5);
		dhg.addHedge(row6);
		dhg.addHedge(row7);
		dhg.addHedge(row8);
		dhg.addHedge(row9);
		dhg.addHedge(row10);
		dhg.addHedge(row11);
		dhg.addHedge(row12);
		dhg.addHedge(row13);
		dhg.addHedge(row14);
		
		return dhg;
	}
	
	
	public static HyperGraph<String> exBA1(){
		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("a","b"));
		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("a","c"));
		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("a","e"));
		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("a","j"));
		
		ArrayList<String> row5 = new ArrayList<String>(Arrays.asList("b","d"));
		ArrayList<String> row6 = new ArrayList<String>(Arrays.asList("b","i"));
		
		ArrayList<String> row7 = new ArrayList<String>(Arrays.asList("c","g"));
		
		ArrayList<String> row8 = new ArrayList<String>(Arrays.asList("d","f"));	

		ArrayList<String> row9 = new ArrayList<String>(Arrays.asList("e","f"));
		
		ArrayList<String> row10 = new ArrayList<String>(Arrays.asList("f","h"));
		ArrayList<String> row11 = new ArrayList<String>(Arrays.asList("f","i"));

		HyperGraph<String> dhg = new HyperGraph<String>();
		dhg.addHedge(row1);
		dhg.addHedge(row2);
		dhg.addHedge(row3);
		dhg.addHedge(row4);
		dhg.addHedge(row5);
		dhg.addHedge(row6);
		dhg.addHedge(row7);
		dhg.addHedge(row8);
		dhg.addHedge(row9);
		dhg.addHedge(row10);
		dhg.addHedge(row11);
		
		return dhg;
	}
	
	
	public static HyperGraph<String> exBA0(){
		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("a","b"));
		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("b","c"));
		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("c","d"));
		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("d","a"));
		
		ArrayList<String> row5 = new ArrayList<String>(Arrays.asList("a","e"));
		ArrayList<String> row6 = new ArrayList<String>(Arrays.asList("e","f"));
		ArrayList<String> row7 = new ArrayList<String>(Arrays.asList("f","g"));
		ArrayList<String> row8 = new ArrayList<String>(Arrays.asList("g","a"));	

		ArrayList<String> row9 = new ArrayList<String>(Arrays.asList("a","h"));
		ArrayList<String> row10 = new ArrayList<String>(Arrays.asList("h","i"));
		ArrayList<String> row11 = new ArrayList<String>(Arrays.asList("i","a"));

		
		ArrayList<String> row12 = new ArrayList<String>(Arrays.asList("a","j"));
		ArrayList<String> row13 = new ArrayList<String>(Arrays.asList("j","k"));
		ArrayList<String> row14 = new ArrayList<String>(Arrays.asList("k","a"));
		ArrayList<String> row15 = new ArrayList<String>(Arrays.asList("j","l"));
		ArrayList<String> row16 = new ArrayList<String>(Arrays.asList("l","k"));
		ArrayList<String> row17 = new ArrayList<String>(Arrays.asList("k","l"));

	
		HyperGraph<String> dhg = new HyperGraph<String>();
		dhg.addHedge(row1);
		dhg.addHedge(row2);
		dhg.addHedge(row3);
		dhg.addHedge(row4);
		dhg.addHedge(row5);
		dhg.addHedge(row6);
		dhg.addHedge(row7);
		dhg.addHedge(row8);
		dhg.addHedge(row9);
		dhg.addHedge(row10);
		dhg.addHedge(row11);
		dhg.addHedge(row12);
		dhg.addHedge(row13);
		dhg.addHedge(row14);
		dhg.addHedge(row15);
		dhg.addHedge(row16);
		
		return dhg;
	}
		
	
	public static void main(String[] args) {

//		HyperGraph<String> dhg = exBA3();
//		
//		 System.out.println(" Le graphe "+dhg);
//		ArrayList<String> order = dFSOrderMaxNeigh(dhg);
//		
////		ArrayList<ArrayList<String>> buckets = new ArrayList<ArrayList<String>>();
////		String[] varFather = new String[order.size()];
//			
////		System.out.println(" les noeuds ordonn�s par DFS "+order);
////		HyperGraph<String> jt = bucketElimination(dhg, order,
////				 buckets, 
////				 varFather); 
//		
//		HyperGraph<AbstCompArrayList<String>> hjt = new HyperGraph<AbstCompArrayList<String>> ();
//		HyperGraph<String> jt = DistribGraphDecomp.tokenEO(dhg, hjt, "MinProj",false);
//		System.out.println(" jt par TokenEO "+jt);
//		System.out.println( " check jointree By TokenEO "+isAjointreeOf(hjt, jt));
//		System.out.println(" jointree  By TokenEO "+hjt);
		
		
//		ArrayList<ArrayList<String>> buckets_1 = new ArrayList<ArrayList<String>>();
//		HashMap<Integer,ArrayList<Integer>> childrenOf_1 = new HashMap<Integer,ArrayList<Integer>>();
//		HyperGraph<String> jt1 = DistribGraphDecomp.AssynEO(dhg, buckets_1, childrenOf_1 );
//		System.out.println(" buckets par AssynEO "+jt1);
//		
//		System.out.println(" buckets_1 "+buckets_1);
//		System.out.println(" childrenOf_1 "+ childrenOf_1);
//		HyperGraph<AbstCompArrayList<String>> hjt1 = new HyperGraph<AbstCompArrayList<String>> ();
//		for(Integer iFather :childrenOf_1.keySet())
//			for(Integer iChild : childrenOf_1.get(iFather)){
//	//			System.out.println(buckets.get(iFather) +" <- "+buckets.get(iChild));
//				if(!iChild.equals(iFather) ){
//				ArrayListComp<String> vi = new ArrayListComp<String>(buckets_1.get(iFather));
//				//hjt2.addVertex(vi);
//				ArrayListComp<String> vj = new ArrayListComp<String>(buckets_1.get(iChild));
//				//hjt2.addVertex(vj);
//				hjt1.addHedge(vi,vj);
//			}
//		}
//	System.out.println(" jointree  par AssynEO "+hjt1);
//	System.out.println( " check jointree By AssynEO "+isAjointreeOf(hjt1, jt1));
		
		
		//System.out.println(" varFather "+ varFather);
//		System.out.println(" buckets par BE "+buckets);
		
//		
//		
//		HyperGraph<AbstCompArrayList<String>> hjt2 = new HyperGraph<AbstCompArrayList<String>> ();
//		
//		for(int i=buckets.size()-1; i>=0 ;i--){
//			if(varFather[i]!=order.get(i)){
//				
//				ArrayListComp<String> vi = new ArrayListComp<String>(buckets.get(i));
//				//hjt2.addVertex(vi);
//				ArrayListComp<String> vj = new ArrayListComp<String>(buckets.get(order.indexOf(varFather[i])));
//				//hjt2.addVertex(vj);
//				
//				hjt2.addHedge(vi,vj);
//			}
//			
//		}
//		
////		System.out.println(" jointree structure "+hjt2);
//		
//		System.out.println( " check the structure  "+isAjointreeOf(hjt2, jt));
//		
//		
//		HyperGraph<AbstCompArrayList<String>> hjtO = new HyperGraph<AbstCompArrayList<String>> ();
//		HashMap<Integer,ArrayList<Integer>> childrenOf = new HashMap<Integer,ArrayList<Integer>>();
//		HyperGraph<String> jtO =localRefinement(dhg, order, buckets, varFather, childrenOf);
//		
//		System.out.println("\n les noeuds ordonn�s par Raffinement "+order);
//		System.out.println("buckets apres raffinement "+buckets);
//		
////		 System.out.println("\n \n Refined jointree  "+jtO);
//		
//		for(Integer iFather :childrenOf.keySet())
//			for(Integer iChild : childrenOf.get(iFather)){
////				System.out.println(buckets.get(iFather) +" <- "+buckets.get(iChild));
//				if(!iChild.equals(iFather) ){
//				ArrayListComp<String> vi = new ArrayListComp<String>(buckets.get(iFather));
//				//hjt2.addVertex(vi);
//				ArrayListComp<String> vj = new ArrayListComp<String>(buckets.get(iChild));
//				//hjt2.addVertex(vj);
//				hjtO.addHedge(vi,vj);
//				}
//			}
//		// System.out.println(" jointree  Refined structure "+hjtO);
//		System.out.println( " check the refined jointree "+isAjointreeOf(hjtO, jtO));
		
//		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("a","b","e"));
//		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("a","e","f"));
//		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("a","e","c"));
//		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("c","f","g"));
//		ArrayList<String> row5 = new ArrayList<String>(Arrays.asList("b","d","g"));
//		ArrayList<String> row6 = new ArrayList<String>(Arrays.asList("c","d","g"));
//		
//				
//		HyperGraph<String> hg = new HyperGraph<String>();
//		hg.addHedge(row1);
//		hg.addHedge(row2);
//		hg.addHedge(row3);
//		hg.addHedge(row4);
//		hg.addHedge(row5);
//		hg.addHedge(row6);
		
		
		
		
		// random instance
		HyperGraph<Integer> dhgBA = new HyperGraph<Integer>();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("nbPeers","720");
		TreeMap<Integer,TreeSet<Integer>> g = benchMarkGenerator.graph.RandomGraphs.genBAGraph(params);
		
		for(Integer idP : g.keySet())
			for(Integer idN : g.get(idP)){
				if(idP<idN){
					ArrayList<Integer> he =new ArrayList<Integer>();
					he.add(idP);he.add(idN);
					dhgBA.addHedge(he);
				}
			}
		
		System.out.println(" Le graphe "+dhgBA);
		
		
		HyperGraph<AbstCompArrayList<Integer>> hjt = new HyperGraph<AbstCompArrayList<Integer>> ();
		HyperGraph<Integer> jt = DistribGraphDecomp.tokenEO(dhgBA, hjt,"MinAddProj",true);
		System.out.println(" jt par TokenEO "+jt);
//		System.out.println( " check jointree By TokenEO "+isAjointreeOf(hjt, jt));
//		System.out.println(" jointree  By TokenEO "+hjt);
		
//		
////		
////		ArrayList<ArrayList<Integer>> buckets_1 = new ArrayList<ArrayList<Integer>>();
////		HashMap<Integer,ArrayList<Integer>> childrenOf_1 = new HashMap<Integer,ArrayList<Integer>>();
////		HyperGraph<Integer> jt1 = DistribGraphDecomp.AssynEO(dhgBA, buckets_1, childrenOf_1 );
////		System.out.println(" jt by AssynEO "+jt1);
////		HyperGraph<AbstCompArrayList<Integer>> hjt1 = new HyperGraph<AbstCompArrayList<Integer>> ();
////		
////		for(Integer iFather :childrenOf_1.keySet())
////		for(Integer iChild : childrenOf_1.get(iFather)){
//////			System.out.println(buckets.get(iFather) +" <- "+buckets.get(iChild));
////			if(!iChild.equals(iFather) ){
////			ArrayListComp<Integer> vi = new ArrayListComp<Integer>(buckets_1.get(iFather));
////			//hjt2.addVertex(vi);
////			ArrayListComp<Integer> vj = new ArrayListComp<Integer>(buckets_1.get(iChild));
////			//hjt2.addVertex(vj);
////			hjt1.addHedge(vi,vj);
////			}
////		}
////	System.out.println(" jointree  Refined structure "+hjtO);
////	System.out.println( " check jointree By AssynEO "+isAjointreeOf(hjt1, jt1));
//		
//		ArrayList<Integer> order = dFSOrderMaxNeigh(dhgBA);
//		ArrayList<ArrayList<Integer>> buckets = new ArrayList<ArrayList<Integer>>();
//		Integer[] varFather = new Integer[order.size()];
////			
////		System.out.println(" les noeuds ordonn�s "+order+"\n");
//		HyperGraph<Integer> jt2 = bucketElimination(dhgBA,order,
//				 buckets, 
//				 varFather); 
//		System.out.println(" jt by (BE) DFS Max Neigh "+jt2);
//		
//		
//		// checking the jointree.
//		
//		HyperGraph<AbstCompArrayList<Integer>> hjt2 = new HyperGraph<AbstCompArrayList<Integer>> ();
//		
//		for(int i=buckets.size()-1; i>=0 ;i--){
//			if(varFather[i]!=order.get(i)){
//				
//				ArrayListComp<Integer> vi = new ArrayListComp<Integer>(buckets.get(i));
//				//hjt2.addVertex(vi);
//				ArrayListComp<Integer> vj = new ArrayListComp<Integer>(buckets.get(order.indexOf(varFather[i])));
//				//hjt2.addVertex(vj);
//				hjt2.addHedge(vi,vj);
//				
//			}	
//		}
////		System.out.println(" jointree Structure "+hjt2);
//		System.out.println( " check the jointree par DFS  Max Neigh BE "+isAjointreeOf(hjt2, jt2));
		
		
		
		
		
		
//		HyperGraph<AbstCompArrayList<Integer>> hjtO = new HyperGraph<AbstCompArrayList<Integer>> ();
//		HashMap<Integer,ArrayList<Integer>> childrenOf = new HashMap<Integer,ArrayList<Integer>>();
//		HyperGraph<Integer> jtO =localRefinement(dhgBA, order, buckets, varFather, childrenOf);
//		System.out.println("\n \n Refined jointree  "+jtO);
		
//		for(Integer iFather :childrenOf.keySet())
//			for(Integer iChild : childrenOf.get(iFather)){
////				System.out.println(buckets.get(iFather) +" <- "+buckets.get(iChild));
//				if(!iChild.equals(iFather) ){
//				ArrayListComp<Integer> vi = new ArrayListComp<Integer>(buckets.get(iFather));
//				//hjt2.addVertex(vi);
//				ArrayListComp<Integer> vj = new ArrayListComp<Integer>(buckets.get(iChild));
//				//hjt2.addVertex(vj);
//				hjtO.addHedge(vi,vj);
//				}
//			}
//		System.out.println(" jointree  Refined structure "+hjtO);
//		System.out.println( " check the refined jointree "+isAjointreeOf(hjtO, jtO));
		
		
		
		// peer description
//		File f = new File("baTest5_20/nbPeers=5/nbClsBynbVars=3.0/rLocBySh=2.0/instance=2");
//		HyperGraph<String> hjt1 =peerDesciptions2HyperGraphOfVariables(f);
//		HyperGraph<String> hjt2 =peersVoc2HyperGraphOfPeers(f);
		

	}

}
