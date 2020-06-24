package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

import com.sun.org.apache.bcel.internal.generic.NEWARRAY;

import main.ArgsHandler;

//import sun.java2d.SunGraphicsEnvironment.TTFilter;


public class DistribGraphDecomp {

	public static class MsgLE<E> {
		// expeditor
		E _exp;
		// recipient
		E _rec;
		// fixpoint
		E _fixP;
		LinkedElmts<E, E> _msg;

		public MsgLE(E exp, E rec, E fixP,LinkedElmts<E, E> msg) {
			_exp = exp;
			_rec = rec;
			_fixP = fixP;
			_msg = msg;
		}
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

	public static <E extends Comparable<? super E>> void updateFillInWeights(
			Set<WeightedElmt<E>> v2Update, HyperGraph<WeightedElmt<E>> whg) {

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
	}
	
	public static <E extends Comparable<? super E>> void updateExtraNeighWeights(
			Set<WeightedElmt<E>> v2Update, HyperGraph<WeightedElmt<E>> whg) {

		// System.out.println("Le graphe à pondérer par Min fill "+whg);

		for (WeightedElmt<E> wv : v2Update) {

			HashSet<WeightedElmt<E>> seenWVertices = new HashSet<WeightedElmt<E>>();
			for (Collection<WeightedElmt<E>> whe : whg.getHEdges(wv)) {
				seenWVertices.addAll(whe);
			}

			int nbEdgesInNeigh = whg.nbNeighbors(wv);
			for (int i = 0; i < whg.nbNeighbors(wv) - 1; i++) {
				for (int j = i + 1; j < whg.nbNeighbors(wv); j++) {
					if (whg.getNeighbors(whg.getNeighbors(wv).get(i)).contains(
							whg.getNeighbors(wv).get(j))) {
						nbEdgesInNeigh++;
					}
				}
			}
			
			int extraNeigh =  nbEdgesInNeigh - whg.nbNeighbors(wv) ; 
			wv.setWeight(extraNeigh);
		}
	}
	
	
	

	public static <E extends Comparable<? super E>> WeightedElmt<E> selectByDistToOptAndUpdateWeights(
			Set<WeightedElmt<E>> v2Update, HyperGraph<WeightedElmt<E>> whg) {

		// System.out.println("Le graphe à pondérer par Min fill "+whg);

		for (WeightedElmt<E> wv : v2Update) {

			HashSet<WeightedElmt<E>> seenWVertices = new HashSet<WeightedElmt<E>>();
			for (Collection<WeightedElmt<E>> whe : whg.getHEdges(wv)) {
				seenWVertices.addAll(whe);
			}

			int nbEdgesInNeigh = whg.nbNeighbors(wv);
			for (int i = 0; i < whg.nbNeighbors(wv) - 1; i++) {
				for (int j = i + 1; j < whg.nbNeighbors(wv); j++) {
					if (whg.getNeighbors(whg.getNeighbors(wv).get(i)).contains(
							whg.getNeighbors(wv).get(j))) {
						nbEdgesInNeigh++;
					}
				}
			}

			// System.out.println(wv+" is in "+ whg.getHEdges(wv));
			// Warning here wv is not in whg.nbNeighbors(wv) it is the fill in
			int wvCliqueWidth = (whg.nbNeighbors(wv) * (whg.nbNeighbors(wv) + 1)) / 2;
			int fillIn = wvCliqueWidth - nbEdgesInNeigh;// wvCliqueWidth;
			int extraNeigh = /* nbEdgesInNeigh - whg.nbNeighbors(wv) ; */wvCliqueWidth;
			//
			// if(whg.nbNeighbors(wv)==0 || (wvCliqueWidth ==
			// whg.nbNeighbors(wv))){
			// wv.setWeight(0);
			// continue;
			// }

			float distToBestSep = 1;
			if (nbEdgesInNeigh > 1) {
				distToBestSep -= ((float) whg.nbNeighbors(wv))
						/ (float) (nbEdgesInNeigh);
			} else
				distToBestSep = 0;

			float distToBestMerge = 1;
			if (nbEdgesInNeigh > 0)
				distToBestMerge -= ((float) (nbEdgesInNeigh) / (float) wvCliqueWidth);
			else
				distToBestMerge = 0;

			if ((distToBestMerge) <= (distToBestSep))
				wv.setWeight(-(distToBestMerge));
			else
				wv.setWeight(distToBestSep);

			if ((fillIn + distToBestMerge) <= (extraNeigh + distToBestSep)) {
				// System.out.print(" fill In "+ (-(fillIn+1)));
				wv.setWeight(-(fillIn + distToBestMerge + 1));
			} else {
				// System.out.print(" extraNeigh "+(extraNeigh+1));
				wv.setWeight(extraNeigh + distToBestSep + 1);
			}
		}

		WeightedElmt<E> bestMerger = null;
		for (WeightedElmt<E> wvTmp : whg.getVertices()) {
			if (bestMerger == null) {
				if (wvTmp.getWeight() <= 0)
					bestMerger = wvTmp;
			} else {
				if (wvTmp.getWeight() <= 0)
					if (bestMerger.getWeight() < wvTmp.getWeight())
						bestMerger = wvTmp;
			}
		}

		WeightedElmt<E> bestSep = null;
		for (WeightedElmt<E> wvTmp : whg.getVertices()) {
			if (bestSep == null) {
				if (wvTmp.getWeight() > 0)
					bestSep = wvTmp;
			} else {
				if (wvTmp.getWeight() > 0)
					if (bestSep.getWeight() > wvTmp.getWeight())
						bestSep = wvTmp;
			}
		}

		if (bestMerger == null)
			return bestSep;
		if (bestSep == null)
			return bestMerger;
		// System.out.println("  bestMerger "+bestMerger.getWeight()+"  bestSep "+
		// bestSep.getWeight()+"");

		if ((bestMerger.getWeight() * -1) <= bestSep.getWeight())
			return bestMerger;
		else
			return bestSep;
	}

	/*
	 * Given an hypergraph hg this method return a HyperTree built by max fill
	 * heuristic The node with the max min-fill
	 */
	public static <E extends Comparable<E>> ArrayList<E> minMaxFillDistOrder(
			String metric, HyperGraph<E> hg) {

		// transforms the input hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for (E v : he)
				whedge.add(new WeightedElmt<E>(v));
			whg.addHedge(whedge);
		}

		// iteratively add the best node to the returned list,
		// remove it from the graph, evaluate the remaining weighted hyper graph

		ArrayList<E> order = new ArrayList<E>();
		ArrayList<E> maxOrder = new ArrayList<E>();
		ArrayList<E> minOrder = new ArrayList<E>();
		boolean maxMin = false;
		if (metric.equals("maxmin")) {
			maxMin = true;
		}
		HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(
				whg.getVertices());
		while (!whg.getVertices().isEmpty()) {
			// System.out.println("Le graphe pondéréé: "+whg);

			// Choose the best vertex of the graph
			WeightedElmt<E> selectedVertex = null;
			if (metric.equals("max") || metric.equals("min"))
				selectedVertex = updateWeightsAndSelectBestNode(metric,
						v2Update, whg);

			if (metric.equals("maxmin")) {
				if (maxMin)
					selectedVertex = updateWeightsAndSelectBestNode("max",
							v2Update, whg);
				else
					selectedVertex = updateWeightsAndSelectBestNode("min",
							v2Update, whg);
			}

			if (metric.equals("maxmin1")) {
				maxMin = false;
				selectedVertex = updateWeightsAndSelectBestNode("min",
						v2Update, whg);
				v2Update.clear();
				if (selectedVertex.getWeight() > 0) {
					selectedVertex = updateWeightsAndSelectBestNode("max",
							v2Update, whg);
					maxMin = true;
				}
			}

			if (metric.equals("maxmin2")) {

				maxMin = false;
				WeightedElmt<E> selectedVertexMin = updateWeightsAndSelectBestNode(
						"min", v2Update, whg);
				v2Update.clear();
				WeightedElmt<E> selectedVertexMax = updateWeightsAndSelectBestNode(
						"min", v2Update, whg);

				if (selectedVertexMin.getWeight() == 0) {
					selectedVertex = selectedVertexMin;
					maxMin = false;
				} else {
					int wvCliqueWidth = (whg.nbNeighbors(selectedVertexMax) * (whg
							.nbNeighbors(selectedVertexMax) + 1)) / 2;
					int extraNeigh = (int) (wvCliqueWidth
							- selectedVertexMax.getWeight() - whg
							.nbNeighbors(selectedVertexMax));

					if (extraNeigh == 0) {
						selectedVertex = selectedVertexMax;
						maxMin = true;
					} else {
						if (selectedVertexMin.getWeight() <= extraNeigh) {
							selectedVertex = selectedVertexMin;
							maxMin = false;
						} else {
							selectedVertex = selectedVertexMax;
							maxMin = true;
						}
					}

				}

				// selectedVertex = selectByDistToOptAndUpdateWeights(v2Update,
				// whg);
				// //
				// maxMin = selectedVertex.getWeight() > 0;
//				System.out.println("maxmin2 select "
//						+ selectedVertex.getWeight());
			}

			v2Update.clear();

			// place the best vertex in the ordered list

			// System.out.println("selectedVertex: "+selectedVertex+"\n");
			if (metric.equals("max") || metric.equals("min"))
				order.add(selectedVertex.getElement());

			if (metric.equals("maxmin") || metric.equals("maxmin1")
					|| metric.equals("maxmin2")) {
				if (maxMin)
					maxOrder.add(selectedVertex.getElement());
				else
					minOrder.add(selectedVertex.getElement());

				if (!metric.equals("maxmin"))
					maxMin = !maxMin;
			}

			// remove the best vertex from the graph and
			// save the neighbors for update

			HashSet<ArrayList<WeightedElmt<E>>> associatedHEdges = whg
					.removeVertex(selectedVertex);

			for (ArrayList<WeightedElmt<E>> ahe : associatedHEdges) {
				whg.removeHEdge(ahe);
				v2Update.addAll(ahe);
			}

			v2Update.remove(selectedVertex);
			// System.out.println("new HE "+
			// v2Update+" induced by the removing of "+ selectedVertices);
			// whg.addHedge(v2Update);

		}

		if (metric.equals("max"))
			return order;
		if (metric.equals("min")) {
			Collections.reverse(order);
			return order;
		}

		if (metric.equals("maxmin") || metric.equals("maxmin1")
				|| metric.equals("maxmin2")) {
			for (int i = 0; i < maxOrder.size(); i++)
				order.add(maxOrder.get(i));
			for (int i = minOrder.size() - 1; i >= 0; i--)
				order.add(minOrder.get(i));

			// if(metric.equals("maxmin2"))
			// Collections.reverse(order);
			return order;
		}

		return null;
	}

	public static <E extends Comparable<E>> ArrayList<E> bFSOrder(
			HyperGraph<E> g) {
		ArrayList<E> bFSOrder = new ArrayList<E>();

		//for (E vertex : g.getVertices())
		//	bFSOrder.add(vertex);
		
		
		// we chose a random peer as starter
		@SuppressWarnings("unchecked")
		E vStarter = (E) g.getVertices().toArray()[(int) Math.random()
				* g.nbVertices()];
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
	
	public static <E extends Comparable<E>> ArrayList<E> dFSOrder(
			HyperGraph<E> g) {

		ArrayList<E> dFSOrder = new ArrayList<E>();

		// we chose a random peer as starter
		@SuppressWarnings("unchecked")
		E vStarter = (E) g.getVertices().toArray()[(int) (Math.random()
				* g.nbVertices())];
		
		visiteDFSNode(vStarter, dFSOrder, g);

		// we built a tree by a depth first search algorithm
		
		return dFSOrder;
	}
	
	private static <E extends Comparable<E>> void  visiteDFSNode(
			E vCurrent,
			ArrayList<E> dFSOrder,
			HyperGraph<E> g) {
		
		dFSOrder.add(vCurrent);
		for (E vNeigh : g.getNeighbors(vCurrent)){
			if(! dFSOrder.contains(vNeigh)){
				visiteDFSNode(vNeigh, dFSOrder, g);
			}
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
		updateFillInWeights(v2Update, whg);

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
	
	
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> ArrayList<E> AssynEOrder(HyperGraph<E> hg) {
		
		// transforms the input hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for (E v : he){
				whedge.add(new WeightedElmt<E>(v));
			}
			whg.addHedge(whedge);
		}
		
		
		// DFS order is used has a complement of the weight affected to each node
		ArrayList<E> dfsOrder = HyperGraphs.dFSOrderMaxNeigh(hg);
		// Reversing DFS order place node that have to be eliminate first at the first position 
		Collections.reverse(dfsOrder);
//		System.out.println(" dfsOrder "+dfsOrder);
		// For convenience, we consider an order of weighted elements
		ArrayList<WeightedElmt<E>> wOrder = new ArrayList<WeightedElmt<E>>();
		wOrder.addAll(whg.getVertices());
		for(WeightedElmt<E> we :whg.getVertices()){
			int i = wOrder.indexOf(we); 
			int nvPos = dfsOrder.indexOf(we._elmt);
			if(nvPos<i){
				wOrder.add(nvPos,we);
				wOrder.remove(i+1);
			}else{
				wOrder.add(nvPos+1,we);
				wOrder.remove(i);
			}
		}
		
		
		// initializes the local rank of each node from 0 to max
		HashMap<E,Integer> node2LocElimOrder = new HashMap<E,Integer>();
		for(E e :hg.getVertices())
			node2LocElimOrder.put(e, Integer.MAX_VALUE);

		// Extend the partial order induced by local elimination to a total order
		ArrayList<E> locElimOrder = new ArrayList<E>();
		
		// iteratively remove elected bests local and re-evaluate the remaining weighted hyper graph
			

		HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(whg.getVertices());
		int currentRank = 0;
		
		/*
		 *		RANK NODES BY SUCCESSIVE LOCAL ELECTIONS  
		 */
		while (!whg.getVertices().isEmpty()) {
			 
			// ELECT NODES WITH MINIMAL WEIGHT IN THE NEIGHBORHOOD
			currentRank ++;
			 
			 // update the weight of each vertex
			 updateFillInWeights(v2Update, whg);
			 
			 
			 // contains best local nodes with  THE minimal weight and a order among its neighborhood			
			ArrayList<WeightedElmt<E>> bestLocWNodes = new ArrayList<WeightedElmt<E>>();
			 // contains the vote for each vertex
			HashMap<WeightedElmt<E>,WeightedElmt<E>> node2Vote = new HashMap<WeightedElmt<E>,WeightedElmt<E>>();
			
			// Each node elects its best neighbor and 
			for(WeightedElmt<E> we : whg.getVertices()){
				 WeightedElmt<E> bestLocalNode = we;
				 for(WeightedElmt<E> nei :whg.getNeighbors(we)){
					 if((int)nei._weight <= (int)bestLocalNode._weight){
						 if((int)nei._weight == (int)bestLocalNode._weight &&
								 wOrder.indexOf(nei)> wOrder.indexOf(bestLocalNode)){
							 continue;
						 }
						 bestLocalNode = nei;
					}
				 }
				 if(bestLocalNode.equals(we))
					 bestLocWNodes.add(bestLocalNode);
				 node2Vote.put(we,bestLocalNode);
			}
			
//			System.out.println(" votes "+ bestLocWNodes);
//			System.out.println(" best Nodes "+ bestLocWNodes);
			
			 // contains elected local nodes with  THE minimal weight and  DFSorder among its neighborhood			
			ArrayList<WeightedElmt<E>> electWNodes = new ArrayList<WeightedElmt<E>>();
			
			// Choose the elected vertices
			for(WeightedElmt<E> bln : bestLocWNodes){
				boolean elected = true;
				for(WeightedElmt<E> nei : whg.getNeighbors(bln)){
					if(!node2Vote.get(nei).equals(bln))
						elected =false;
				}
				if(elected){
					electWNodes.add(bln);
				}
			}
			
//			System.out.println(" elected Nodes "+ electWNodes);
			
			// rank the node in a local elimination order
			for(WeightedElmt<E> we :electWNodes){
				node2LocElimOrder.put(we._elmt,currentRank);
				locElimOrder.add(we._elmt);
			}
			
			// remove the elected vertices from the graph and save the neighbors for update
			v2Update.clear();
			for(WeightedElmt<E> we :electWNodes){
				HashSet<ArrayList<WeightedElmt<E>>> associatedHEdges = whg
					.removeVertex(we);
				for (ArrayList<WeightedElmt<E>> ahe : associatedHEdges) {
					whg.removeHEdge(ahe);
					v2Update.addAll(ahe);
				}
			}
			v2Update.removeAll(electWNodes);
			ArrayList<WeightedElmt<E>> remFromV2Update = new ArrayList<WeightedElmt<E>>();
			for(WeightedElmt<E> we:v2Update){
				if(!whg.getVertices().contains(we)){
					remFromV2Update.add(we);
				}
			}
			v2Update.removeAll(remFromV2Update);
			// System.out.println("new HE "+
			// v2Update+" induced by the removing of "+ selectedVertices);
			// whg.addHedge(v2Update);

		}
		
		// rank the remainig graph by Max Neigh
		
		ArrayList<E> remainVertices = new ArrayList<E>();
		for(E e : hg.getVertices()){
			if(!locElimOrder.contains(e))
				remainVertices.add(e);
		}
		
		ArrayList<E> sortedRemainVertices = new ArrayList<E>();
		int nbSort = 0;
		while(remainVertices.size()>0){
			E e = remainVertices.remove(0);
			int pos = 0;
			for(int i=0; i<sortedRemainVertices.size();i++){
				if(hg.getNeighbors(sortedRemainVertices.get(i)).size()
						< hg.getNeighbors(e).size())
					pos++;
				else
					break;
			}
			if(pos<sortedRemainVertices.size())
				sortedRemainVertices.add(pos,e);
			else
				sortedRemainVertices.add(e);
		}
			
		
		
//		System.out.println(" local elimination order node rank  "+ node2LocElimOrder);
//		System.out.println(" local elimination order "+ locElimOrder);
		
		return locElimOrder;
		
		}
	
	public static <E extends Comparable<E>> ArrayList<E> dFSOrderAssynEO(
			HyperGraph<E> g) {

		ArrayList<E> dFSOrder = new ArrayList<E>();
		ArrayList<E> assynEO = AssynEOrder(g);
		E bestV = assynEO.get(assynEO.size()-1);
		
		
		visiteDFSOrderAssynEO(bestV, assynEO, dFSOrder, g);

		// we built a tree by a depth first search algorithm
		
		return dFSOrder;
	}
	
	private static <E extends Comparable<E>> void  visiteDFSOrderAssynEO(
			E vCurrent,
			ArrayList<E> assynEO,
			ArrayList<E> dFSOrder,
			HyperGraph<E> g) {
		
		dFSOrder.add(vCurrent);
//		nodeDepth.add(depth+1);
		
		while( !dFSOrder.containsAll(g.getNeighbors(vCurrent))){
			E bestVNeigh = null;
			Integer bestScore = 0;
			for (E vNeigh : g.getNeighbors(vCurrent)){
				if(! dFSOrder.contains(vNeigh)){
					if(bestVNeigh == null)
						bestVNeigh = vNeigh;
					// compute the order in AssynEO
					int score = assynEO.indexOf(vNeigh);
	
					if(score > bestScore){
						bestScore = score;
						bestVNeigh = vNeigh;
					}
						
				}
			}
			visiteDFSOrderAssynEO(bestVNeigh,assynEO, dFSOrder, g);
		}
	}
	
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> HyperGraph<E> AssynEO(
			HyperGraph<E> hg) {
		
		// transforms the input hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for (E v : he){
				whedge.add(new WeightedElmt<E>(v));
			}
			whg.addHedge(whedge);
		}
		
		
		// DFS order is used has a complement of the weight affected to each node
		ArrayList<E> dfsOrder = HyperGraphs.dFSOrderMaxNeigh(hg);
		// Reversing DFS order place node that have to be eliminate first at the first position 
		Collections.reverse(dfsOrder);
//		System.out.println(" dfsOrder "+dfsOrder);
		// For convenience, we consider an order of weighted elements
		ArrayList<WeightedElmt<E>> wOrder = new ArrayList<WeightedElmt<E>>();
		wOrder.addAll(whg.getVertices());
		for(WeightedElmt<E> we :whg.getVertices()){
			int i = wOrder.indexOf(we); 
			int nvPos = dfsOrder.indexOf(we._elmt);
			if(nvPos<i){
				wOrder.add(nvPos,we);
				wOrder.remove(i+1);
			}else{
				wOrder.add(nvPos+1,we);
				wOrder.remove(i);
			}
		}
		
		
		// initializes the local rank of each node from 0 to max
		HashMap<E,Integer> node2LocElimOrder = new HashMap<E,Integer>();
		for(E e :hg.getVertices())
			node2LocElimOrder.put(e, Integer.MAX_VALUE-1);

		// Extend the partial order induced by local elimination to a total order
		ArrayList<E> locElimOrder = new ArrayList<E>();
		
		// iteratively remove elected bests local and re-evaluate the remaining weighted hyper graph
			

		HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(whg.getVertices());
		int currentRank = 0;
		
		/*
		 *		RANK NODES BY SUCCESSIVE LOCAL ELECTIONS  
		 */
		while (!whg.getVertices().isEmpty()) {
			 
			// ELECT NODES WITH MINIMAL WEIGHT IN THE NEIGHBORHOOD
			currentRank ++;
			 
			 // update the weight of each vertex
			 updateFillInWeights(v2Update, whg);
			 
			 
			 // contains best local nodes with  THE minimal weight and a order among its neighborhood			
			ArrayList<WeightedElmt<E>> bestLocWNodes = new ArrayList<WeightedElmt<E>>();
			 // contains the vote for each vertex
			HashMap<WeightedElmt<E>,WeightedElmt<E>> node2Vote = new HashMap<WeightedElmt<E>,WeightedElmt<E>>();
			
			// Each node elects its best neighbor and 
			for(WeightedElmt<E> we : whg.getVertices()){
				 WeightedElmt<E> bestLocalNode = we;
				 for(WeightedElmt<E> nei :whg.getNeighbors(we)){
					 if((int)nei._weight <= (int)bestLocalNode._weight){
						 if((int)nei._weight == (int)bestLocalNode._weight &&
								 wOrder.indexOf(nei)> wOrder.indexOf(bestLocalNode)){
							 continue;
						 }
						 bestLocalNode = nei;
					}
				 }
				 if(bestLocalNode.equals(we))
					 bestLocWNodes.add(bestLocalNode);
				 node2Vote.put(we,bestLocalNode);
			}
			
//			System.out.println(" votes "+ bestLocWNodes);
//			System.out.println(" best Nodes "+ bestLocWNodes);
			
			 // contains elected local nodes with  THE minimal weight and  DFSorder among its neighborhood			
			ArrayList<WeightedElmt<E>> electWNodes = new ArrayList<WeightedElmt<E>>();
			
			// Choose the elected vertices
			for(WeightedElmt<E> bln : bestLocWNodes){
				boolean elected = true;
				for(WeightedElmt<E> nei : whg.getNeighbors(bln)){
					if(!node2Vote.get(nei).equals(bln))
						elected =false;
				}
				if(elected){
					electWNodes.add(bln);
				}
			}
			
//			System.out.println(" elected Nodes "+ electWNodes);
			
			// rank the node in a local elimination order
			for(WeightedElmt<E> we :electWNodes){
				node2LocElimOrder.put(we._elmt,currentRank);
				locElimOrder.add(we._elmt);
			}
			
			// remove the elected vertices from the graph and save the neighbors for update
			v2Update.clear();
			for(WeightedElmt<E> we :electWNodes){
				HashSet<ArrayList<WeightedElmt<E>>> associatedHEdges = whg
					.removeVertex(we);
				for (ArrayList<WeightedElmt<E>> ahe : associatedHEdges) {
					whg.removeHEdge(ahe);
					v2Update.addAll(ahe);
				}
			}
			v2Update.removeAll(electWNodes);
			ArrayList<WeightedElmt<E>> remFromV2Update = new ArrayList<WeightedElmt<E>>();
			for(WeightedElmt<E> we:v2Update){
				if(!whg.getVertices().contains(we)){
					remFromV2Update.add(we);
				}
			}
			v2Update.removeAll(remFromV2Update);
			// System.out.println("new HE "+
			// v2Update+" induced by the removing of "+ selectedVertices);
			// whg.addHedge(v2Update);

		}
		
//		System.out.println(" local elimination order node rank  "+ node2LocElimOrder);
//		System.out.println(" local elimination order "+ locElimOrder);
		
		// When it is possible each node selects as parent a neighbor that get the lowest greater rank after him
		HashMap<E,E> parentOf = new HashMap<E,E>();
		HashMap<E,ArrayList<E>> nearbyPredecessorsOf = new HashMap<E,ArrayList<E>>();
		HashMap<E,ArrayList<E>> childrenOf = new HashMap<E,ArrayList<E>>();
		
		// there are two kind of roots, the one that have children and the others that don't
		ArrayList<E> pseudoRoots = new ArrayList<E> ();
		for(E e :node2LocElimOrder.keySet()){
			
			E higherNeigh = null;
			int lowestGreaterRank = Integer.MAX_VALUE;
			for(E n: hg.getNeighbors(e)){
				if(lowestGreaterRank > node2LocElimOrder.get(n) && 
						 node2LocElimOrder.get(e)< node2LocElimOrder.get(n)){
					lowestGreaterRank = node2LocElimOrder.get(n);
					higherNeigh = n;
				}
				if( node2LocElimOrder.get(e)> node2LocElimOrder.get(n)){
					if(!nearbyPredecessorsOf.containsKey(e))
						nearbyPredecessorsOf.put(e, new ArrayList<E>());
					nearbyPredecessorsOf.get(e).add(n);
				}
			}
			if(higherNeigh != null){
				parentOf.put(e,higherNeigh);
				if(!childrenOf.containsKey(higherNeigh))
					childrenOf.put(higherNeigh, new ArrayList<E>());
				childrenOf.get(higherNeigh).add(e);
			}else{
				pseudoRoots.add(e);
			}
		}
		// we remove from pseudo root nodes that don't have children
		ArrayList<E> psr2Rem = new ArrayList<E>();
		for(E psr : pseudoRoots){
			if(!parentOf.values().contains(psr))
				psr2Rem.add(psr);
		}
		pseudoRoots.removeAll(psr2Rem);
		
//		System.out.println(" pseudoRoots "+ pseudoRoots);
//		
//		System.out.println(" parentOf "+ parentOf);
//		
//		System.out.println(" nearbyPredecessorsOf "+ nearbyPredecessorsOf);
		

		// remove descendants from little cousins 
//		for( E e : nearbyPredecessorsOf.keySet()){
//			if(!nearbyPredecessorsOf.get(e).isEmpty()){
//				ArrayList<E> desd2Rem  = new ArrayList<E>();
//				for(E c:nearbyPredecessorsOf.get(e)){
//					E ch = c;
//					while( locElimOrder.indexOf(e)> locElimOrder.indexOf(ch)){
//						if(!parentOf.containsKey(ch))
//							break;
//						
//						if(e.equals(parentOf.get(ch)))
//							desd2Rem.add(c);
//						ch = parentOf.get(ch);
//					}
//				}
//				nearbyPredecessorsOf.get(e).removeAll(desd2Rem);
//			}
//		}
		
		// compute the ancestors path for each node in the forest of pseudo roots
		HashMap<E, ArrayList<E>> ancestorPathOf = new HashMap<E,ArrayList<E>>();
		for(E e : parentOf.keySet()){
			ancestorPathOf.put(e,new ArrayList<E>());
			E a = e;
			while(!pseudoRoots.contains(a)){
				a= parentOf.get(a);
				ancestorPathOf.get(e).add(0,a);
			}
		}
		
		HashMap<E,ArrayList<E>> node2Bucks = new HashMap<E,ArrayList<E>>(); 
		for(E e : hg.getVertices()){	
			node2Bucks.put(e,new ArrayList<E>());
			// Leaves in JT add themselves to their bucket
			if(!nearbyPredecessorsOf.keySet().contains(e))
				if(!psr2Rem.contains(e))
					node2Bucks.get(e).add(e);
		}
		
		
		/*
		 * for each variable
		 *  bottom up projection from the node to common ancestors with nearby predecessors
		 */
		// Not leaves project to to common ancestors with nearby predecessors
		for(E e : nearbyPredecessorsOf.keySet()){
			
				// eBis replace e by a predecessor at the call ancestorPathOf some node don't have parent
				E eBis = (ancestorPathOf.containsKey(e))? e :nearbyPredecessorsOf.get(e).get(0) ;
				
				int i=0;
				boolean sameHigherAncestor = true;
				while(sameHigherAncestor){
	
					for(E c :nearbyPredecessorsOf.get(e)){
						if(i>= ancestorPathOf.get(c).size() || i>= ancestorPathOf.get(eBis).size()){
							sameHigherAncestor = false;
							i++;
							break;
						}
						if(!ancestorPathOf.get(c).get(i).equals(ancestorPathOf.get(eBis).get(i))){
							sameHigherAncestor = false;
							break;
						}
					}
					if(sameHigherAncestor)
						i++;
					else
						i--;
				}
				
				if(i<0){ // ancestor paths are different we have to project e in all of them
					// add e to the bucket of its ancestor
					for(E a : ancestorPathOf.get(eBis)){
						node2Bucks.get(a).add(e);
					}
					// add e to the bucket of its nearby predecessors and their ancestors
					for(E c :nearbyPredecessorsOf.get(e)){
						if(!node2Bucks.get(c).contains(e))
							node2Bucks.get(c).add(e);
						for(E a : ancestorPathOf.get(c)){
							if(!node2Bucks.get(a).contains(e))
								node2Bucks.get(a).add(e);
						}
					}
				}else{// ancestor paths get common prefixes
					 // we have to project e in all of them till common ancestor
					for(int j = i;j < ancestorPathOf.get(eBis).size();j++){
						node2Bucks.get( ancestorPathOf.get(eBis).get(j)).add(e);
					}
					for(E c :nearbyPredecessorsOf.get(e)){
						if(!node2Bucks.get(c).contains(e))
							node2Bucks.get(c).add(e);
						for(int j = i+1;j < ancestorPathOf.get(c).size();j++){
							if(!node2Bucks.get(ancestorPathOf.get(c).get(j)).contains(e))
							node2Bucks.get(ancestorPathOf.get(c).get(j)).add(e);
						}
					}
				}
		}
		
//		System.out.println(" Buckets from local elimination order "+ node2Bucks);
		
		/*
		 * FIRST BOTTOM UP LOCAL ELIMINATION-PROJECTION IN THE FOREST ROOTED BY PSEUDOROOTS
		 */
		
		// prepare a bottom up traversal
//		ArrayList<E> leaves = new ArrayList<E>();
//		for(E e :node2LocElimOrder.keySet()){
//			if(parentOf.keySet().contains(e)){
//				boolean isLeaf = true;
//				for(E n: hg.getNeighbors(e)){
//					if(node2LocElimOrder.get(e)> node2LocElimOrder.get(n)){
//						isLeaf = false;
//						break;
//					}
//				}
//				if(isLeaf)
//					leaves.add(e);
//			}
//		}
//		System.out.println(" leaves "+ leaves);
		
//		HyperGraph<E> hgCopy = new HyperGraph<E>();
//		for (Collection<E> he : hg.getHEdges()) {
//			HashSet<E> hedge = new HashSet<E>();
//			hedge.addAll(he);
//			hgCopy.addHedge(hedge);
//		}
		
		

		

//		for(E l: leaves ){
//			int pos = 0;
//			for(E e :toVisit){
//				if(locElimOrder.indexOf(e)<locElimOrder.indexOf(l))
//					pos++;
//				else
//					break;
//			}
//			if(pos<toVisit.size())
//				toVisit.add(pos,l);
//			else
//				toVisit.add(l);
//		}
		
//		System.out.println(" order of leaves to visit "+ toVisit);
		
		// Bottom Up  Projection from leaves to pseudo roots
//		while(!toVisit.isEmpty()){
//			E e = toVisit.remove(0);
//			if(hgCopy.getVertices().contains(e)){
//				// Fill the bucket with the remaining neighbor of e
//				for(E n :hgCopy.getNeighbors(e)){
//					if(!node2Bucks.get(e).contains(n)){
//						node2Bucks.get(e).add(n);
//					}
//				}
////				System.out.println("add neighbours to node2Bucks.get("+e+") "+ node2Bucks.get(e));
//				// project shared node to parent  buck
//				if(parentOf.containsKey(e)){
//					E p = parentOf.get(e);
//					if(!pseudoRoots.contains(e))
//						for(E v: node2Bucks.get(e))
//							if(!node2Bucks.get(p).contains(v) && !v.equals(e))
//								node2Bucks.get(p).add(v);
//					// project
//					
////					System.out.println("project to parent node2Bucks.get("+p+") "+ node2Bucks.get(p));
//					// add e to its bucket if not
//					if(!node2Bucks.get(e).contains(e))
//						node2Bucks.get(e).add(0,e);
//					// plan a visit to the parent regarding its rank
//					if(!pseudoRoots.contains(p)){
//						int pos = 0;
//						for(E v :toVisit){
//							if(locElimOrder.indexOf(v)<locElimOrder.indexOf(p))
//								pos++;
//							else
//								break;
//						}
//						if(pos<toVisit.size())
//							toVisit.add(pos,p);
//						else
//							toVisit.add(p);
//					}
//				}
////				System.out.println(" remove "+e+" ");
//				hgCopy.removeVertex(e);
////				System.out.println("hgCopy "+ hgCopy);
//			}			
//		}
		
		
		
		
		
//		System.out.println(" Buckets from local elimination order "+ node2Bucks);
		
		/*
		 * COMPLETE LOCAL ELECTION ELIMINATION ORDER FROM PSEUDO ROOTS WITH DFSORDER
		 */
		
		// prepare the bottom up traversal of from pseudo roots following the dfsorder
		HashMap<E,E> dfsParentOf = new HashMap<E,E>();

		for(int i=0;i<dfsOrder.size()-1;i++){
			for(int j=i+1;j<dfsOrder.size();j++){
				E p = dfsOrder.get(j);
				E c= dfsOrder.get(i);
				
				if(hg.getNeighbors(c).contains(p)){
					dfsParentOf.put(c,p);
					break;
				}
			}
		}
		dfsParentOf.put(dfsOrder.get(dfsOrder.size()-1),dfsOrder.get(dfsOrder.size()-1));
//		System.out.println(" dfsParent from pseudo roots"+ dfsParentOf);

		ArrayList<E> toVisit = new ArrayList<E>();
		HashMap<E,ArrayList<E>> highNodes2Buck = new HashMap<E,ArrayList<E>>();
		for(E r: pseudoRoots ){
			int pos = 0;
			for(E e :toVisit){
				if(dfsOrder.indexOf(e)<dfsOrder.indexOf(r))
					pos++;
				else
					break;
			}
			toVisit.add(pos,r);
			highNodes2Buck.put(r, node2Bucks.get(r));
		}
		
		// Bottom Up Projection without elimination from pseudo roots, stop at the lower common ancestor
		while(toVisit.size()>1){
			E e = toVisit.remove(0);
			E p = dfsParentOf.get(e);
			if(!p.equals(e)){
				if(!toVisit.contains(p)){
					// project shared node from e
					highNodes2Buck.put(p, (ArrayList<E>)highNodes2Buck.get(e).clone());
					int pos = 0;
					// place p at a good position
					for(E v :toVisit){
						if(dfsOrder.indexOf(v)<dfsOrder.indexOf(p))
							pos++;
						else
							break;
					}
					if(pos<toVisit.size())
						toVisit.add(pos,p);
					else
						toVisit.add(p);
				}else{
					// project shared not contain in p from e
					for(E v : highNodes2Buck.get(e))
						if(!highNodes2Buck.get(p).contains(v))
							highNodes2Buck.get(p).add(v);
				}
				
			}
		}
//		System.out.println(" Bucks Project without Elimination from"+pseudoRoots+" \n  "+ highNodes2Buck);
// The lower common ancestor remains in toVisit
//		System.out.println(" Common Ancestor "+ toVisit);
		
		// Top Down late Elimination
		HashMap<E,ArrayList<E>> dfsChildrenOf = new HashMap<E,ArrayList<E>>();
		for(E c : dfsParentOf.keySet()){
			E p = dfsParentOf.get(c);
			if(!dfsChildrenOf.containsKey(p)){
				dfsChildrenOf.put(p, new ArrayList<E>());
			}
			if(!p.equals(c))
				dfsChildrenOf.get(p).add(c);
		}
//		System.out.println(" dfsChildrenOf "+ dfsChildrenOf);
		
		while(!toVisit.isEmpty()){
			E e = toVisit.remove(0);
			ArrayList<E> toRem = new ArrayList<E>();
			for(E v : highNodes2Buck.get(e)){
				if(dfsChildrenOf.containsKey(e)){
					int nbChildWithV = 0;
					
					for(E c: dfsChildrenOf.get(e)){
						if(highNodes2Buck.containsKey(c))
							if(highNodes2Buck.get(c).contains(v)){
								nbChildWithV++;
								if(nbChildWithV>1)
									break;
							}
						
					}
					if(nbChildWithV==1)
						toRem.add(v);
					}
			}
			highNodes2Buck.get(e).removeAll(toRem);
			for(E c: dfsChildrenOf.get(e)){
				if(highNodes2Buck.containsKey(c))
					if(! (highNodes2Buck.get(c).isEmpty() || pseudoRoots.contains(c)))
						toVisit.add(c);
			}
		}
//		System.out.println(" Late Elimination "+pseudoRoots+" \n  "+ highNodes2Buck);
		
		
		// Return the hyper GRAPH made from early local election elimination and late DFS order
		HyperGraph<E> returnHg = new HyperGraph<E>();
		for(ArrayList<E> buck :node2Bucks.values()){
			if(buck.size()>1)
				returnHg.addHedge(buck);
		}
		for(ArrayList<E> buck :highNodes2Buck.values()){
			if(buck.size()>1)
				returnHg.addHedge(buck);
		}
		
		
		return returnHg;
		
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> HyperGraph<E> AssynEO(
			HyperGraph<E> hg,
			ArrayList<ArrayList<E>> buckets_out,
			HashMap<Integer,ArrayList<Integer>> childrenOf_out
			) {
		
		// transforms the input hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for (E v : he){
				whedge.add(new WeightedElmt<E>(v));
			}
			whg.addHedge(whedge);
		}
		
		
		// DFS order is used has a complement of the weight affected to each node
		ArrayList<E> dfsOrder = HyperGraphs.dFSOrderMaxNeigh(hg);
		// Reversing DFS order place node that have to be eliminate first at the first position 
		Collections.reverse(dfsOrder);
//		System.out.println(" dfsOrder "+dfsOrder);
		// For convenience, we consider an order of weighted elements
		ArrayList<WeightedElmt<E>> wOrder = new ArrayList<WeightedElmt<E>>();
		wOrder.addAll(whg.getVertices());
		for(WeightedElmt<E> we :whg.getVertices()){
			int i = wOrder.indexOf(we); 
			int nvPos = dfsOrder.indexOf(we._elmt);
			if(nvPos<i){
				wOrder.add(nvPos,we);
				wOrder.remove(i+1);
			}else{
				wOrder.add(nvPos+1,we);
				wOrder.remove(i);
			}
		}
		
		
		// initializes the local rank of each node from 0 to max
		HashMap<E,Integer> node2LocElimOrder = new HashMap<E,Integer>();
		for(E e :hg.getVertices())
			node2LocElimOrder.put(e, Integer.MAX_VALUE-1);

		// Extend the partial order induced by local elimination to a total order
		ArrayList<E> locElimOrder = new ArrayList<E>();
		
		// iteratively remove elected bests local and re-evaluate the remaining weighted hyper graph
			

		HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(whg.getVertices());
		int currentRank = 0;
		
		/*
		 *		RANK NODES BY SUCCESSIVE LOCAL ELECTIONS  
		 */
		while (!whg.getVertices().isEmpty()) {
			 
			// ELECT NODES WITH MINIMAL WEIGHT IN THE NEIGHBORHOOD
			currentRank ++;
			 
			 // update the weight of each vertex
			 updateFillInWeights(v2Update, whg);
			 
			 
			 // contains best local nodes with  THE minimal weight and a order among its neighborhood			
			ArrayList<WeightedElmt<E>> bestLocWNodes = new ArrayList<WeightedElmt<E>>();
			 // contains the vote for each vertex
			HashMap<WeightedElmt<E>,WeightedElmt<E>> node2Vote = new HashMap<WeightedElmt<E>,WeightedElmt<E>>();
			
			// Each node elects its best neighbor and 
			for(WeightedElmt<E> we : whg.getVertices()){
				 WeightedElmt<E> bestLocalNode = we;
				 for(WeightedElmt<E> nei :whg.getNeighbors(we)){
					 if((int)nei._weight <= (int)bestLocalNode._weight){
						 if((int)nei._weight == (int)bestLocalNode._weight &&
								 wOrder.indexOf(nei)> wOrder.indexOf(bestLocalNode)){
							 continue;
						 }
						 bestLocalNode = nei;
					}
				 }
				 if(bestLocalNode.equals(we))
					 bestLocWNodes.add(bestLocalNode);
				 node2Vote.put(we,bestLocalNode);
			}
			
//			System.out.println(" votes "+ bestLocWNodes);
//			System.out.println(" best Nodes "+ bestLocWNodes);
			
			 // contains elected local nodes with  THE minimal weight and  DFSorder among its neighborhood			
			ArrayList<WeightedElmt<E>> electWNodes = new ArrayList<WeightedElmt<E>>();
			
			// Choose the elected vertices
			for(WeightedElmt<E> bln : bestLocWNodes){
				boolean elected = true;
				for(WeightedElmt<E> nei : whg.getNeighbors(bln)){
					if(!node2Vote.get(nei).equals(bln))
						elected =false;
				}
				if(elected){
					electWNodes.add(bln);
				}
			}
			
//			System.out.println(" elected Nodes "+ electWNodes);
			
			// rank the node in a local elimination order
			for(WeightedElmt<E> we :electWNodes){
				node2LocElimOrder.put(we._elmt,currentRank);
				locElimOrder.add(we._elmt);
			}
			
			// remove the elected vertices from the graph and save the neighbors for update
			v2Update.clear();
			for(WeightedElmt<E> we :electWNodes){
				HashSet<ArrayList<WeightedElmt<E>>> associatedHEdges = whg
					.removeVertex(we);
				for (ArrayList<WeightedElmt<E>> ahe : associatedHEdges) {
					whg.removeHEdge(ahe);
					v2Update.addAll(ahe);
				}
			}
			v2Update.removeAll(electWNodes);
			ArrayList<WeightedElmt<E>> remFromV2Update = new ArrayList<WeightedElmt<E>>();
			for(WeightedElmt<E> we:v2Update){
				if(!whg.getVertices().contains(we)){
					remFromV2Update.add(we);
				}
			}
			v2Update.removeAll(remFromV2Update);
			// System.out.println("new HE "+
			// v2Update+" induced by the removing of "+ selectedVertices);
			// whg.addHedge(v2Update);

		}
		
//		System.out.println(" local elimination order node rank  "+ node2LocElimOrder);
//		System.out.println(" local elimination order "+ locElimOrder);
		
		// When it is possible each node selects as parent a neighbor that get the lowest greater rank after him
		HashMap<E,E> parentOf = new HashMap<E,E>();
		HashMap<E,ArrayList<E>> nearbyPredecessorsOf = new HashMap<E,ArrayList<E>>();
		HashMap<E,ArrayList<E>> childrenOf = new HashMap<E,ArrayList<E>>();
		
		// there are two kind of roots, the one that have children and the others that don't
		ArrayList<E> pseudoRoots = new ArrayList<E> ();
		for(E e :node2LocElimOrder.keySet()){
			
			E higherNeigh = null;
			int lowestGreaterRank = Integer.MAX_VALUE;
			for(E n: hg.getNeighbors(e)){
				if(lowestGreaterRank > node2LocElimOrder.get(n) && 
						 node2LocElimOrder.get(e)< node2LocElimOrder.get(n)){
					lowestGreaterRank = node2LocElimOrder.get(n);
					higherNeigh = n;
				}
				if( node2LocElimOrder.get(e)> node2LocElimOrder.get(n)){
					if(!nearbyPredecessorsOf.containsKey(e))
						nearbyPredecessorsOf.put(e, new ArrayList<E>());
					if(!nearbyPredecessorsOf.get(e).contains(n))
						nearbyPredecessorsOf.get(e).add(n);
				}
			}
			if(higherNeigh != null){
				parentOf.put(e,higherNeigh);
				if(!childrenOf.containsKey(higherNeigh))
					childrenOf.put(higherNeigh, new ArrayList<E>());
				childrenOf.get(higherNeigh).add(e);
			}else{
				pseudoRoots.add(e);
			}
		}
		// we remove from pseudo root nodes that don't have children
		ArrayList<E> psr2Rem = new ArrayList<E>();
		for(E psr : pseudoRoots){
			if(!parentOf.values().contains(psr))
				psr2Rem.add(psr);
		}
		pseudoRoots.removeAll(psr2Rem);
		
//		System.out.println(" pseudoRoots "+ pseudoRoots);
//		
//		System.out.println(" parentOf "+ parentOf);
//		
//		System.out.println(" nearbyPredecessorsOf "+ nearbyPredecessorsOf);
		
		
		// compute the ancestors path for each node in the forest of pseudo roots
		HashMap<E, ArrayList<E>> ancestorPathOf = new HashMap<E,ArrayList<E>>();
		for(E e : parentOf.keySet()){
			ancestorPathOf.put(e,new ArrayList<E>());
			E a = e;
			while(!pseudoRoots.contains(a)){
				a= parentOf.get(a);
				ancestorPathOf.get(e).add(0,a);
			}
		}
		
		HashMap<E,ArrayList<E>> node2Bucks = new HashMap<E,ArrayList<E>>(); 
		for(E e : hg.getVertices()){	
			node2Bucks.put(e,new ArrayList<E>());
			// Leaves in JT add themselves to their bucket
//			if(!nearbyPredecessorsOf.keySet().contains(e)){
//				if(!psr2Rem.contains(e))
//					node2Bucks.get(e).add(e);
//			}
			if(parentOf.containsKey(e))
				 node2Bucks.get(e).add(e);
				
		}
		
		
		/*
		 * FIRST BOTTOM UP LOCAL ELIMINATION-PROJECTION IN THE FOREST ROOTED BY PSEUDOROOTS
		 */

		/*
		 * for each variable
		 *  bottom up projection from the node to common ancestors with nearby predecessors
		 */
		// Not leaves project to to common ancestors with nearby predecessors
		for(E e : nearbyPredecessorsOf.keySet()){
			
				// eBis replace e by a predecessor at the call ancestorPathOf some node don't have parent
				E eBis = (ancestorPathOf.containsKey(e))? e :nearbyPredecessorsOf.get(e).get(0) ;
				
				ArrayList<E> ancestorsEbis = new ArrayList<E>();
				ancestorsEbis.addAll(ancestorPathOf.get(eBis));
				if(eBis.equals(e))
					ancestorsEbis.add(e);
				
//				System.out.println("	e nearbyPredecessorsOf.get("+e+") ->" +nearbyPredecessorsOf.get(e));
//				System.out.println("	eBis ancestorPathOf.get("+eBis+") ->" +ancestorsEbis);
				
				int i=0;
				boolean sameHigherAncestor = true;
				while(sameHigherAncestor){
					for(E c :nearbyPredecessorsOf.get(e)){
//						System.out.println(" 		c ancestorPathOf.get("+c+") ->" +ancestorPathOf.get(c));
						if(i>= ancestorPathOf.get(c).size() || i>= ancestorsEbis.size()){
							sameHigherAncestor = false;
							break;
						}
						if(!ancestorPathOf.get(c).get(i).equals(ancestorsEbis.get(i))){
							sameHigherAncestor = false;
							break;
						}
					}
					
					if(sameHigherAncestor)
						i++;
					else
						i--;
				}
//				System.out.println(" 	i ->" +i);
				if(i<0){ // ancestor paths are different we have to project e in all of them
					// add e to the bucket of its ancestor
					for(E a : ancestorPathOf.get(eBis)){
						if(!node2Bucks.get(a).contains(e))
							node2Bucks.get(a).add(e);
					}
					// add e to the bucket of its nearby predecessors and their ancestors
					for(E c :nearbyPredecessorsOf.get(e)){
						if(!node2Bucks.get(c).contains(e))
							if(!node2Bucks.get(c).contains(e))
								node2Bucks.get(c).add(e);
						for(E a : ancestorPathOf.get(c)){
							if(!node2Bucks.get(a).contains(e))
								node2Bucks.get(a).add(e);
						}
					}
				}else{// ancestor paths get common prefixes
					 // we have to project e in all of them till common ancestor
					for(int j = i;j < ancestorPathOf.get(eBis).size();j++){
						if(!node2Bucks.get( ancestorPathOf.get(eBis).get(j)).contains(e))
							node2Bucks.get( ancestorPathOf.get(eBis).get(j)).add(e);
					}
					for(E c :nearbyPredecessorsOf.get(e)){
						if(!node2Bucks.get(c).contains(e))
							node2Bucks.get(c).add(e);
						for(int j = i+1;j < ancestorPathOf.get(c).size();j++){
							if(!node2Bucks.get(ancestorPathOf.get(c).get(j)).contains(e))
								node2Bucks.get(ancestorPathOf.get(c).get(j)).add(e);
						}
					}
				}
		}
		
//		System.out.println(" Buckets from local elimination order "+ node2Bucks);
		// Save buckets and parent links in out structure
		
//		System.out.println(" Buckets from local elimination order "+ node2Bucks);
//		System.out.println(" parentOf "+ parentOf);
//		
		int iB=0;
		for(E c : parentOf.keySet()){

//			System.out.println(" next iB "+ iB );
			
			int iBChild = (!buckets_out.contains(node2Bucks.get(c))) ?
					iB : buckets_out.indexOf(node2Bucks.get(c));
//			System.out.println(" iBChild "+ iBChild + "  node2Bucks.get("+c+")  " +node2Bucks.get(c));
			
			int iBParent = (!buckets_out.contains(node2Bucks.get(parentOf.get(c)))) ?
					((iBChild ==iB )? iB+1 : iB) : buckets_out.indexOf(node2Bucks.get(parentOf.get(c)));		
//			System.out.println(" iBParent "+ iBParent + "  node2Bucks.get("+parentOf.get(c)+")  " + node2Bucks.get(parentOf.get(c)));
			
			if(iBChild==iB)		
				buckets_out.add(iBChild,node2Bucks.get(c));
			
			if(iBParent>=iB)
				buckets_out.add(iBParent,node2Bucks.get(parentOf.get(c)));
			
//			System.out.println(" buckets_out "+ buckets_out );
			
			if(!childrenOf_out.containsKey(iBParent))
				childrenOf_out.put(iBParent, new ArrayList<Integer>());
			
			childrenOf_out.get(iBParent).add(iBChild);
			// System.out.println(" childrenOf_out "+ childrenOf_out );

			iB = (iB>iBParent)? ((iB>iBChild)? iB:iBChild+1): (iBParent>iBChild)?iBParent+1:iBChild+1;
			
		}
		
		
		// Return the hyper GRAPH made from early local election elimination and late DFS order
		HyperGraph<E> returnHg = new HyperGraph<E>();
		for(ArrayList<E> buck :node2Bucks.values()){
			// if(buck.size()>1)
				returnHg.addHedge(buck);
		}
	
		if(pseudoRoots.size()<=1){
			return returnHg;	
		}
		
		
		System.out.println(" Pseudo roots" + pseudoRoots);
	
		/*
		 * COMPLETE LOCAL ELECTION ELIMINATION ORDER FROM PSEUDO ROOTS WITH DFSORDER
		 */
		
		// prepare the bottom up traversal of from pseudo roots following the dfsorder
		HashMap<E,E> dfsParentOf = new HashMap<E,E>();

		for(int i=0;i<dfsOrder.size()-1;i++){
			for(int j=i+1;j<dfsOrder.size();j++){
				E p = dfsOrder.get(j);
				E c= dfsOrder.get(i);
				
				if(hg.getNeighbors(c).contains(p)){
					dfsParentOf.put(c,p);
					break;
				}
			}
		}
		dfsParentOf.put(dfsOrder.get(dfsOrder.size()-1),dfsOrder.get(dfsOrder.size()-1));
		System.out.println(" dfsParent from pseudo roots"+ dfsParentOf);

		ArrayList<E> toVisit = new ArrayList<E>();
		HashMap<E,ArrayList<E>> highNodes2Buck = new HashMap<E,ArrayList<E>>();
		for(E r: pseudoRoots ){
			int pos = 0;
			for(E e :toVisit){
				if(dfsOrder.indexOf(e)<dfsOrder.indexOf(r))
					pos++;
				else
					break;
			}
			toVisit.add(pos,r);
			highNodes2Buck.put(r, node2Bucks.get(r));
		}
		
		// Bottom Up Projection without elimination from pseudo roots, stop at the lower common ancestor
		while(toVisit.size()>1){
			E e = toVisit.remove(0);
			E p = dfsParentOf.get(e);
			if(!p.equals(e)){
				if(!toVisit.contains(p)){
					// project shared node from e
					highNodes2Buck.put(p, (ArrayList<E>)highNodes2Buck.get(e).clone());
					int pos = 0;
					// place p at a good position
					for(E v :toVisit){
						if(dfsOrder.indexOf(v)<dfsOrder.indexOf(p))
							pos++;
						else
							break;
					}
					if(pos<toVisit.size())
						toVisit.add(pos,p);
					else
						toVisit.add(p);
				}else{
					// project shared not contain in p from e
					for(E v : highNodes2Buck.get(e))
						if(!highNodes2Buck.get(p).contains(v))
							highNodes2Buck.get(p).add(v);
				}
				
			}
		}
		System.out.println(" Bucks Project without Elimination from"+pseudoRoots+" \n  "+ highNodes2Buck);
		
// The lower common ancestor remains in toVisit
		System.out.println(" Common Ancestor "+ toVisit);
		
		// Top Down late Elimination
		HashMap<E,ArrayList<E>> dfsChildrenOf = new HashMap<E,ArrayList<E>>();
		for(E c : dfsParentOf.keySet()){
			E p = dfsParentOf.get(c);
			if(!dfsChildrenOf.containsKey(p)){
				dfsChildrenOf.put(p, new ArrayList<E>());
			}
			if(!p.equals(c))
				dfsChildrenOf.get(p).add(c);
		}
		System.out.println(" dfsChildrenOf "+ dfsChildrenOf);
		
		while(!toVisit.isEmpty()){
			E e = toVisit.remove(0);
			ArrayList<E> toRem = new ArrayList<E>();
			for(E v : highNodes2Buck.get(e)){
				if(dfsChildrenOf.containsKey(e)){
					int nbChildWithV = 0;
					
					for(E c: dfsChildrenOf.get(e)){
						if(highNodes2Buck.containsKey(c))
							if(highNodes2Buck.get(c).contains(v)){
								nbChildWithV++;
								if(nbChildWithV>1)
									break;
							}
						
					}
					if(nbChildWithV==1)
						toRem.add(v);
					}
			}
			highNodes2Buck.get(e).removeAll(toRem);
			for(E c: dfsChildrenOf.get(e)){
				if(highNodes2Buck.containsKey(c))
					if(! (highNodes2Buck.get(c).isEmpty() || pseudoRoots.contains(c)))
						toVisit.add(c);
			}
		}
		System.out.println(" Late Elimination pseudoRoots "+ pseudoRoots+" \n  ");
		System.out.println(" Late Elimination highNodes2Buck "+ highNodes2Buck+" \n  ");
		
		for(E c : highNodes2Buck.keySet()){
			
			if(dfsParentOf.containsKey(c))
				if(!dfsParentOf.get(c).equals(c)){
					
					int iBChild = (!buckets_out.contains(highNodes2Buck.get(c))) ?
							iB : buckets_out.indexOf(highNodes2Buck.get(c));
					
					System.out.println(" iBChild "+ iBChild + "  highNodes2Buck("+c+")  " +highNodes2Buck.get(c));
					
					int iBParent = (!buckets_out.contains(highNodes2Buck.get(dfsParentOf.get(c)))) ?
							((iBChild ==iB )? iB+1 : iB) : buckets_out.indexOf(highNodes2Buck.get(dfsParentOf.get(c)));
					System.out.println(" iBParent "+ iBParent + "  highNodes2Buck("+dfsParentOf.get(c)+")  " + 
							highNodes2Buck.get(dfsParentOf.get(c)));
					
					if(iBChild==iB)		
						buckets_out.add(iBChild,highNodes2Buck.get(c));
					
					if(iBParent>=iB)
						buckets_out.add(iBParent,highNodes2Buck.get(dfsParentOf.get(c)));
					
					System.out.println(" buckets_out "+ buckets_out );
					
					if (!childrenOf_out.containsKey(iBParent))
						childrenOf_out.put(iBParent, new ArrayList<Integer>());
					
					childrenOf_out.get(iBParent).add(iBChild);
					
			iB = (iB>iBParent)? ((iB>iBChild)? iB:iBChild+1): (iBParent>iBChild)?iBParent+1:iBChild+1;
			
				}
			
		}
		
		for(ArrayList<E> buck :highNodes2Buck.values()){
			// if(buck.size()>1)
				returnHg.addHedge(buck);
		}
		
		
		

		
		return returnHg;
		
	}
	
	
	
	
	

	public static <E extends Comparable<E>> ArrayList<E> circuitsDFSOrderMaxNeigh(
			HyperGraph<E> g,
			ArrayList<E>  dFSOrder) {

		ArrayList<E>  circuitsOrder = new ArrayList<E>();
		
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
		
		visiteDFSNodeMaxNeigh(bestV, dFSOrder, circuitsOrder,g);		
		return circuitsOrder;
	}
	
	private static <E extends Comparable<E>> void  visiteDFSNodeMaxNeigh(
			E vCurrent,
			ArrayList<E> dFSOrder,
			ArrayList<E>  circuitsOrder,
			HyperGraph<E> g) {
		
		dFSOrder.add(vCurrent);
		circuitsOrder.add(vCurrent);
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
			visiteDFSNodeMaxNeigh(bestVNeigh, dFSOrder,circuitsOrder, g);
			circuitsOrder.add(vCurrent);
		}
	}
	
	private static  <E extends Comparable<E>> void updateNeighScore(String heuristique, 
			HyperGraph<E> hg, 
			E tokenNode, 
			ArrayList<E> tokElimOrder, 
			ArrayList<HashSet<E>> curProjOut,
			HashMap<E,ArrayList<Integer>> scoreOf,
			HashSet<E> tokenUpdatedNodes
			){
		// update scores for each neighbors considering out projection ONLY uneliminated node can update their score
//		System.out.println("		curProjOut "+curProjOut);

		
		
		if(heuristique.equals("MinAddProj")){
			/*
			 * Min Add Heuristique
			 */
			for(E n: hg.getNeighbors(tokenNode)){
				if(!tokElimOrder.contains(n) && !tokenUpdatedNodes.contains(n)){
					tokenUpdatedNodes.add(n);
					// compute the remaining neighbor of n
					HashSet<E> neighCurBuck = new HashSet<E>(hg.getNeighbors(n));
					neighCurBuck.removeAll(tokElimOrder);
					// decrease the score if v is contain in a projection
					for(HashSet<E> h :curProjOut){
						if(h.contains(n) ){
							neighCurBuck.removeAll(h);
						}
					}
					int neighCurScore = neighCurBuck.size();
					if( !scoreOf.get(n).get(0).equals(neighCurScore)){
						scoreOf.get(n).set(0,neighCurScore);
					}
				}
			}
		}
		
		if(heuristique.equals("MinProj")){
			/*
			 * Update Min Neighbors Heuristiques
			 */
			for(E n: hg.getNeighbors(tokenNode)){
				if(!tokElimOrder.contains(n)&& !tokenUpdatedNodes.contains(n)){
					tokenUpdatedNodes.add(n);
					// compute the remaining neighbor of n
					HashSet<E> neighCurBuck = new HashSet<E>(hg.getNeighbors(n));
					neighCurBuck.removeAll(tokElimOrder);
					boolean nIsInclude = false;
					// compute the neighbors from the projection 
						for(HashSet<E> h :curProjOut){
							if(h.contains(n)){
								nIsInclude = true;
								neighCurBuck.addAll(h);
							}
						}
						
//					System.out.println("		MinProj "+n+" : neighCurBuck"+neighCurBuck);
					int neighCurScore = nIsInclude?neighCurBuck.size()-1:neighCurBuck.size();
					
					if( !scoreOf.get(n).get(0).equals(neighCurScore)){
						scoreOf.get(n).set(0,neighCurScore);
					}
				}
			}
		}
		
		if(heuristique.equals("MinRemNeigh")){
		/*
		 * Remaining  Min Neighbors Heuristiques
		 */
		for(E n: hg.getNeighbors(tokenNode)){
			if(!tokElimOrder.contains(n)&& !tokenUpdatedNodes.contains(n)){
				tokenUpdatedNodes.add(n);
				// compute the remaining neighbor of n
				HashSet<E> neighCurBuck = new HashSet<E>(hg.getNeighbors(n));
				neighCurBuck.removeAll(tokElimOrder);
				int neighCurScore = neighCurBuck.size();
				if( !scoreOf.get(n).get(0).equals(neighCurScore)){
					scoreOf.get(n).set(0,neighCurScore);
				}
			}
		}
		}
		
		if(heuristique.equals("MinNeigh")){
		/*
		 * static  Neighbors Heuristiques
		 */
		// DO nothing since the number of neighbor has already been used
		}
		
		
	}	
	
	
	
	@SuppressWarnings("unchecked")
	private static <E extends Comparable<E>> void updateVote(HyperGraph<E> hg, 
			E tokenNode, 
			HashMap<E, ArrayList<Integer>> scoreOf, 
			ArrayList<E> tokElimOrder,
			HashMap<E, E> voteOf,
			HashSet<E> tokenUpdatedNodes){
			
		// update the vote of the neighbors (eliminated node can also vote for un eliminated node)
		ArrayList<E> extend_Neighbors = new ArrayList<E>( hg.getNeighbors(tokenNode));
		extend_Neighbors.add(tokenNode);
		
//		System.out.println("		extend_Neighbors of "+tokenNode+ " "+extend_Neighbors	);
		
		for(E n: extend_Neighbors){
			
			 if(voteOf.get(n)==null)
				 continue;
			
			HashSet<E> updatedNeighbors = new HashSet<E> ();

			 E nMin = null;
			// by default a pair consider its previous vote has min
			if(!tokElimOrder.contains(voteOf.get(n))){
				updatedNeighbors.addAll((HashSet<E>)tokenUpdatedNodes.clone()); 
				updatedNeighbors.retainAll( hg.getNeighbors(n));
				 nMin = voteOf.get(n);	
			}else{
				updatedNeighbors.addAll(hg.getNeighbors(n));
				
			}
			updatedNeighbors.add(n);
			updatedNeighbors.removeAll(tokElimOrder);
			
			 
			// compare the min with the updated neighborhood
			for(E nOfn: updatedNeighbors){
//				if(!tokElimOrder.contains(nOfn)){
					if(nMin == null )
						nMin = nOfn;
					else{
						if(scoreOf.get(nOfn).get(0)< scoreOf.get(nMin).get(0)){
							nMin = nOfn;
						}
						if((scoreOf.get(nOfn).get(0).equals(scoreOf.get(nMin).get(0))) &&
								scoreOf.get(nOfn).get(1)<scoreOf.get(nMin).get(1)){
							nMin = nOfn;
						}
					}
//				}
			}
			voteOf.put(n,nMin);
		}
	}
	
	private static  <E extends Comparable<E>> int  nextInCircuits(
			HashMap<E,ArrayList<Integer>> circleIndOf,
			ArrayList<E>  circleOrder ,
			E tokenNode, int curPosInCircle){
		
		
		
		int occurence = 0;
		while(circleIndOf.get(tokenNode).get(occurence) < (curPosInCircle%circleOrder.size())){
			occurence++;
			if(occurence >= circleIndOf.get(tokenNode).size()){
				occurence --;
				break;
			}
				
		}
		if( circleIndOf.get(tokenNode).get(occurence) < (curPosInCircle%circleOrder.size())){
			occurence = 0;
		}
		
		
//		System.out.print("	circleOrder ");
//		for(int i=0;i< circleOrder.size();i++)
//			System.out.print(i+":"+circleOrder.get(i)+", ");
//		System.out.println();
//		System.out.print("	circleIndOf.get("+tokenNode+") "+circleIndOf.get(tokenNode));
//		System.out.println("	tokenNode "+tokenNode+ "occ "+ occurence +"	curPosInCircle "+ curPosInCircle%circleOrder.size() );
		
		int iToken =circleIndOf.get(tokenNode).get(occurence);
		curPosInCircle=iToken+1;
		
//		System.out.println("	nTokenNode "+circleOrder.get(curPosInCircle%circleOrder.size())+" iNToken "+curPosInCircle%circleOrder.size());
		
		return  curPosInCircle ;
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> HyperGraph<E> tokenEO(
			HyperGraph<E> hg,
			HyperGraph<AbstCompArrayList<E>> hjt_out,
			String heuristique,
			boolean invOrder){
		
		// DFS order is used in order to make a circuit in the network
		ArrayList<E>  dFSOrder = new ArrayList<E>();
		ArrayList<E>  circleOrder = circuitsDFSOrderMaxNeigh(hg,dFSOrder);
		circleOrder.remove(circleOrder.size()-1);
		if(invOrder){
			Collections.reverse(dFSOrder);
			Collections.reverse(circleOrder);
		}
		HashMap<E,ArrayList<Integer>> circleIndOf = new HashMap<E,ArrayList<Integer>>();
		for(int i=0;i< circleOrder.size();i++){
			E e = circleOrder.get(i);
			if(!circleIndOf.containsKey(e)){
				circleIndOf.put(e,new ArrayList<Integer>());
			}
			circleIndOf.get(e).add(i);
		}
		
		int curPosInCircle = 0;
		
//		System.out.println(" circuitsOrder "+ circleOrder);
//		System.out.println(" circleIndOf "+ circleIndOf);

		// map each node to a score initially the number of neighbors. Afterwars vars added to currents projections
		HashMap<E,ArrayList<Integer>> scoreOf = new HashMap<E,ArrayList<Integer>>();
		for(E e: hg.getVertices()){
			ArrayList<Integer> score = new ArrayList<Integer>();
			score.add(hg.getNeighbors(e).size());
			score.add(dFSOrder.indexOf(e));
			scoreOf.put(e,score);
		}
//		System.out.println(" scoreOf "+ scoreOf);
		
		// For each node saves its vote. 
		HashMap<E,E> voteOf = new HashMap<E, E>();
		
		// initially each node vote for the node in the neighborhood that have the lowest number of neighbors
		for(E e: hg.getVertices()){
			// by default a pair consider itself has a min even if it's  eliminated 
			 E eMin = e;
			// compare the min with the neighborhood
			for(E n: hg.getNeighbors(e)){
				if(scoreOf.get(n).get(0)< scoreOf.get(eMin).get(0)){
					eMin = n;
				}
				if((scoreOf.get(n).get(0).equals(scoreOf.get(eMin).get(0))) &&
						scoreOf.get(n).get(1)<scoreOf.get(eMin).get(1)){
					eMin = n;
				}
			}
			voteOf.put(e,eMin);
		}
		//		System.out.println(" voteOf "+ voteOf);
		
		// save the current node holding the token
		E tokenNode = circleOrder.get(curPosInCircle);
		// record the vote of the tokenNode 
		// record the token updated Nodes,  initially all nodes have been updated
		HashSet<E> tokenUpdatedNodes = new HashSet<E>(hg.getVertices());
		
		// save the bucket of each node
		HashMap<E,HashSet<E>> node2buckOf = new HashMap<E, HashSet<E>>();
		// save the current  projections in the network.
		ArrayList<HashSet<E>> curProjOut = new ArrayList<HashSet<E>>();
		// save the projection to it original bucket
		HashMap<HashSet<E>,ArrayList<HashSet<E>>> proj2buckOf = new HashMap<HashSet<E>, ArrayList<HashSet<E>>>();
		// record for each visit of the token the number of eliminated node 
		HashMap<E,Integer> e2NbElimLastVisit = new HashMap<E,Integer>();
		for(E e : hg.getVertices()){
			e2NbElimLastVisit.put(e, -1);
		}
		
		// save the node elimination order to the token
		ArrayList<E> tokElimOrder = new ArrayList<E>();
		
		int debug_Count =0;
		
		while(!tokElimOrder.containsAll(hg.getVertices())){
			debug_Count++;
//			if(debug_Count>500)
//				break;
//			System.out.println(debug_Count+" tokenNode "+ tokenNode);
//			
//			System.out.println("		tokElimOrder "+tokElimOrder);
//			ArrayList<E> tmp = new ArrayList<E>( hg.getVertices());
//			tmp.removeAll(tokElimOrder);
//			System.out.println("		ToElim "+(tmp));
			
			
			if(e2NbElimLastVisit.get(tokenNode)< tokElimOrder.size()){
				e2NbElimLastVisit.put(tokenNode, tokElimOrder.size());
				/*
				 * LOCAL ELECTION
				 */
				//			System.out.println("	LOCAL ELECTION ");	
				
				 updateNeighScore(heuristique,hg,tokenNode,tokElimOrder,curProjOut,scoreOf, tokenUpdatedNodes);
				
//				System.out.println("		scoreOf "+scoreOf);
//				System.out.println("		tokenUpdatedNodes "+tokenUpdatedNodes);
//				System.out.println("		curModifScoreNodes "+curModifScoreNodes);
	
				if(!tokenUpdatedNodes.isEmpty() ){
					updateVote(hg, tokenNode, scoreOf, tokElimOrder,voteOf, tokenUpdatedNodes);
				
//					System.out.println("		voteOf "+voteOf);
				
				}
			}else{
				
//				System.out.println("		voteOf Token "+voteOf.get(tokenNode));
			}
			
			// check if the token holder receive the vote of all the neighborhood 
			boolean elected = !tokElimOrder.contains(tokenNode);

			
			if(elected){
				for(E n: hg.getNeighbors(tokenNode)){
					if(voteOf.get(n)!=null)
						if(!voteOf.get(n).equals(tokenNode)){
							elected = false;
							break;
						}
				}
			}
			
			
			// ATTENTION MODIFICATION
			if(elected ){ 
//			if(elected || (eVote!=null? eVote.equals(tokenNode):false)){
//				System.out.println("		ELECTED ");
				
				// eliminate t constraints that have already be taken into consideration
				tokElimOrder.add(tokenNode);
				
				// Take into account all
				HashSet<E> curBuck = new HashSet<E>(hg.getNeighbors(tokenNode));
				curBuck.removeAll(tokElimOrder);
				
				// update the current projections 
				// Merge  bucket that contains t in a new bucket
				
				HashSet<E> nvProj = new HashSet<E>(curBuck);
				ArrayList<HashSet<E>> proj2Rem = new ArrayList<HashSet<E>>();
				for(HashSet<E> proj :curProjOut){
					if(proj.contains(tokenNode)){
						nvProj.addAll(proj);
						proj2Rem.add(proj);
					}
				}
				
				// Remove input proj from the current projection
				curProjOut.removeAll(proj2Rem);
				
//				System.out.println("		input projs "+proj2Rem);
				
				nvProj.remove(tokenNode);
				curProjOut.add(nvProj);
				
//				System.out.println("		output proj "+nvProj);
				
				// The current bucket contains the node and the projection from beside
				curBuck.add(tokenNode);
				curBuck.addAll(nvProj);
				
				node2buckOf.put(tokenNode, curBuck);
//				System.out.println("		curBuck "+curBuck);
				
				if(!proj2buckOf.containsKey(nvProj))
					proj2buckOf.put(nvProj,new ArrayList<HashSet<E>>());
				proj2buckOf.get(nvProj).add(curBuck);
				
				for(HashSet<E> proj: proj2Rem){
					for(HashSet<E> buck : proj2buckOf.get(proj)){
						ArrayListComp<E> vi = new ArrayListComp<E>(buck);
						ArrayListComp<E> vj = new ArrayListComp<E>(curBuck);
						hjt_out.addHedge(vi, vj);
					}
				}
				
				
				
				// remove the token holder from participant
				scoreOf.get(tokenNode).set(0,Integer.MAX_VALUE);
				
//				System.out.println("		eliminate "+ tokenNode);
				
				
				tokenUpdatedNodes.clear();
				
				updateNeighScore(heuristique,hg,tokenNode,tokElimOrder,curProjOut,scoreOf,tokenUpdatedNodes);
				
//				System.out.println("		scoreOf "+scoreOf);
//				System.out.println("		tokenUpdatedNodes "+tokenUpdatedNodes);

				
				updateVote(hg, tokenNode, scoreOf, tokElimOrder,voteOf, tokenUpdatedNodes);
				
//				System.out.println("		voteOf "+voteOf);
				

			}
			
			/*
			 * FOLLOWING THE MIN NEIGHBOR LINK 
			 */
			//	System.out.println("	FOLLOWING THE MIN NEIGHBOR LINK ");
			
			if(voteOf.get(tokenNode) == null){
				curPosInCircle = nextInCircuits( circleIndOf,circleOrder ,tokenNode, curPosInCircle);
				tokenNode = circleOrder.get(curPosInCircle%circleOrder.size());
			}else{
				if(voteOf.get(tokenNode).equals(tokenNode)){
					E nextMinNeigh = null;
					for(E n:  hg.getNeighbors(tokenNode)){
						if(voteOf.get(n)== null)
							continue;
						if(!tokElimOrder.contains(voteOf.get(n))&& !voteOf.get(n).equals(tokenNode)){
							if(nextMinNeigh == null){
								nextMinNeigh = n;
								continue ;
							}
							if(scoreOf.get(voteOf.get(n)).get(0)< scoreOf.get(voteOf.get(nextMinNeigh)).get(0)){
								nextMinNeigh = n;
							}
							if((scoreOf.get(voteOf.get(n)).get(0).equals(scoreOf.get(voteOf.get(nextMinNeigh)).get(0))) &&
									scoreOf.get(voteOf.get(n)).get(1)<scoreOf.get(voteOf.get(nextMinNeigh)).get(1)){
								nextMinNeigh = n;
							}
							
						}
					}
					if(nextMinNeigh !=null){
//						System.out.println("	"+tokenNode+ "	avoid loop following "
//								+nextMinNeigh+  ": scoreVote "+ scoreOf.get(voteOf.get(nextMinNeigh))+" vote "+ voteOf.get(nextMinNeigh));
						tokenNode = nextMinNeigh;
					}else{
						curPosInCircle = nextInCircuits( circleIndOf,circleOrder ,tokenNode, curPosInCircle);
						tokenNode = circleOrder.get(curPosInCircle%circleOrder.size());
						}
				}else{
//					 System.out.println("	tokenNode "+tokenNode+ " eVote "+
//							 eVote+ ": scrore "+ scoreOf.get(eVote)+ " vote "+ voteOf.get(eVote));
					tokenNode = voteOf.get(tokenNode);
				}
			}
			
		}
		
		// return the join-tree
		
		// return the hyper graph containing the tree
		HyperGraph<E> returnHg = new HyperGraph<E>();
		for(HashSet<E> buck :node2buckOf.values()){
			// if(buck.size()>1)
				returnHg.addHedge(buck);
		}
		
		//	System.out.println("node2buckOf "+node2buckOf);
		
		return returnHg;
	}
	
	

	

	public static <E extends Comparable<E>> HyperGraph<E> distribJT(
			HyperGraph<E> hg) {

		HyperGraph<E> hjt = new HyperGraph<E>();
		// maps abstract node to its real representation
		HashMap<E, ArrayList<E>> abst2RealsE = new HashMap<E, ArrayList<E>>();
		for (E e : hg.getVertices()) {
			abst2RealsE.put(e, new ArrayList<E>());
			abst2RealsE.get(e).add(e);
		}

		while (hg.getVertices().size() > 1) {
			// transforms the hyper graph hg in a weighted hyper graph whg
			HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
			for (Collection<E> he : hg.getHEdges()) {
				HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
				for (E v : he)
					whedge.add(new WeightedElmt<E>(v));
				whg.addHedge(whedge);
			}

			// build the forest of trees where each tree has
			// as root a maxFillDistrb weighted node
			HashMap<E, ArrayList<E>> successorsOf = new HashMap<E, ArrayList<E>>();
			HashMap<E, ArrayList<E>> predecessorsOf = new HashMap<E, ArrayList<E>>();
			HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(
					whg.getVertices());
			while (!whg.getVertices().isEmpty()) {
				updateFillInWeights(v2Update, whg);
				v2Update.clear();
				ArrayList<WeightedElmt<E>> maxElmts = new ArrayList<WeightedElmt<E>>();
				for (WeightedElmt<E> wv : whg.getVertices()) {
					boolean isMaxInNeighborhood = true;
					for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
						if (wv.compareTo(wn) < 0) {
							isMaxInNeighborhood = false;
							break;
						}
						for (WeightedElmt<E> wnn : whg.getNeighbors(wn)) {
							if (wv.compareTo(wnn) < 0) {
								isMaxInNeighborhood = false;
								break;
							}
						}
						if (isMaxInNeighborhood == false) {
							break;
						}
					}
					if (isMaxInNeighborhood == true) {
						maxElmts.add(wv);
						if( whg.getNeighbors(wv).size()>0){
							successorsOf.put(wv.getElement(),new ArrayList<E>());
						for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
							successorsOf.get(wv.getElement()).add(
									wn.getElement());
							if (!predecessorsOf.containsKey(wn.getElement()))
								predecessorsOf.put(wn.getElement(),
										new ArrayList<E>());
							predecessorsOf.get(wn.getElement()).add(
									wv.getElement());
						}

						v2Update.addAll(whg.getNeighbors(wv));
						}
						
					}
				}
				for (WeightedElmt<E> maxE : maxElmts)
					whg.removeVertex(maxE);

			}
			
			System.out.println("predecessors ");
			for(E elmt : predecessorsOf.keySet()){
				System.out.print(elmt+" preds");
				for(E pred : predecessorsOf.get(elmt))
					System.out.print(" "+pred);
				System.out.println();
			}
			
			System.out.println("successors ");
			for(E elmt : successorsOf.keySet()){
				System.out.print(elmt+" succs");
				for(E succ : successorsOf.get(elmt))
					System.out.print(" "+succ);
				System.out.println();
			}
			
			HashMap<E, LinkedElmts<E, E>> elmts2NotSeen = new HashMap<E, LinkedElmts<E, E>>();
			for (E elmt : predecessorsOf.keySet()) {
				LinkedElmts<E, E> e2NS = new LinkedElmts<E, E>(elmt);
				e2NS._links.addAll(predecessorsOf.get(elmt));
				elmts2NotSeen.put(elmt, e2NS);

			}

			System.out.println(" Linked Elmts ");

			// create a bucket for each successor of an element, and add the
			// element to the bucket
			HashMap<E, HashMap<E, ArrayList<LinkedElmts<E, E>>>> bucketsOfSuccs = new HashMap<E, HashMap<E, ArrayList<LinkedElmts<E, E>>>>();
			for (E elmt : successorsOf.keySet()) {
				bucketsOfSuccs.put(elmt,
						new HashMap<E, ArrayList<LinkedElmts<E, E>>>());
				for (E succOfElmt : successorsOf.get(elmt)) {
					bucketsOfSuccs.get(elmt).put(succOfElmt,
							new ArrayList<LinkedElmts<E, E>>());
					// add the element to the bucket : no ned TO ADD HERE
//					LinkedElmts<E, E> e2NS = new LinkedElmts<E, E>(elmt);
//					if (predecessorsOf.containsKey(elmt))
//						e2NS._links.addAll(predecessorsOf.get(elmt));
//					bucketsOfSuccs.get(elmt).get(succOfElmt).add(e2NS);
				}

			}

			System.out.println("buckets of successors ");
			for(E e :bucketsOfSuccs.keySet()){
				System.out.print("  "+e);
				for(E eSucc :bucketsOfSuccs.get(e).keySet()){
					System.out.print(" buck -> "+eSucc+" : ");
					for(LinkedElmts<E, E> v : bucketsOfSuccs.get(e).get(eSucc))
						System.out.print(" "+v._elmt+" "+v._links);
				}
				System.out.println();
			}
			
			

			// save the roots Elements
			HashMap<E, ArrayList<E>> roots = new HashMap<E, ArrayList<E>>();
			for (E v : successorsOf.keySet()) {
				if (!predecessorsOf.containsKey(v))
					roots.put(v, new ArrayList<E>());
			}

			System.out.print(" save the roots ");
			for (E r : roots.keySet()) {
				System.out.print(" " + r);
			}
			System.out.println();

			// from leaves to roots we propagate elements respecting the running
			// intersection. The bottom up propagation of a leaf starts by its
			// predecessors
			// then follows the last predecessor link.
			// The propagation stop either a root is reach or all the paths
			// from a leaf converge
			int iTest = 0;
			
			while (!predecessorsOf.isEmpty()) {
				ArrayList<E> leaves = new ArrayList<E>();
				for (E v : predecessorsOf.keySet()) {
					if (!successorsOf.containsKey(v))
						leaves.add(v);
				}
				
				

				System.out.print(" leaves ");
				for (E l : leaves) {
					System.out.print(" " + l);
				}
				System.out.println();
			
				boolean stop = true ;
				if(stop && iTest==2)
					return null;
				iTest++;
				
				// propagate each leaf
				for (E leaf : leaves) {
					
					// msgBox contains the expediter, the recipient and 
					// the element to propagate 
					ArrayList<MsgLE<E>> msgBox = new ArrayList<MsgLE<E>>();
					// initialize the list with the predecessor of the leaf
					for (E neighOfLeaf : elmts2NotSeen.get(leaf)._links) {
//						msgBox.add(new MsgLE<E>(leaf, neighOfLeaf,
//								elmts2NotSeen.get(leaf)));
					}
					
					while (!msgBox.isEmpty()) {
						System.out.println();
						MsgLE<E> mCur = msgBox.remove(0);
						
						System.out.println("msg exp:"+mCur._exp+" rec:"+mCur._rec+
								" le:"+mCur._msg._elmt+ " "+mCur._msg._links);
						// add or intersect the received LE elmt to the
						// appropriate bucket of the recipient
						int iMsgLe = bucketsOfSuccs.get(mCur._rec)
								.get(mCur._exp).indexOf(mCur._msg);
						if (iMsgLe >= 0) {
//							System.out.println(" intersect msg");
							bucketsOfSuccs.get(mCur._rec).get(mCur._exp)
									.get(iMsgLe).intersect(mCur._msg);
						} else {
//							System.out.println(" add msg to "+ mCur._rec);
							LinkedElmts<E, E> nvLe = mCur._msg.clone();
							nvLe._links.remove(mCur._rec);
							bucketsOfSuccs.get(mCur._rec).get(mCur._exp)
									.add(nvLe);
						}

						// Test if the element need to be propagate
						iMsgLe = bucketsOfSuccs.get(mCur._rec).get(mCur._exp)
								.indexOf(mCur._msg);
						LinkedElmts<E, E> nvLe = bucketsOfSuccs.get(mCur._rec)
								.get(mCur._exp).get(iMsgLe);
						
						System.out.println("nvLe elm:"+nvLe._elmt+" links:"+nvLe._links);
						
						if (nvLe._links.isEmpty()
								|| !predecessorsOf.containsKey(mCur._rec)) {
							continue;
						}

						// propagate the received element to the last
						// predecessor
						
						E lastPred = predecessorsOf.get(mCur._rec).get(
								predecessorsOf.get(mCur._rec).size() - 1);
//						System.out.println("Last predecessor Of "+mCur._rec+": "+lastPred);
//						msgBox.add(new MsgLE<E>(mCur._rec, lastPred,nvLe));

					}
					
					
				}
				
				for (E leaf : leaves) {
					predecessorsOf.remove(leaf);
				}
				
				ArrayList<E> succsToRem = new ArrayList<E>();
				for(E e : successorsOf.keySet()){
					ArrayList<E> leavesToRem = new ArrayList<E>();
					for(E succE :successorsOf.get(e)){
						if(leaves.contains(succE))
							leavesToRem.add(succE);
								
					}
					successorsOf.get(e).removeAll(leavesToRem);
					if(successorsOf.get(e).isEmpty())
						succsToRem.add(e);
				}
				for(E e:succsToRem)
				 successorsOf.remove(e);
			}
			
			

			// At this step buckets are stable we save thhem in
			// the returning jointree
			for (E e : bucketsOfSuccs.keySet())
				for (E eSucc : bucketsOfSuccs.get(e).keySet()) {
					ArrayList<E> he = new ArrayList<E>();
					he.addAll(abst2RealsE.get(e));
					for (LinkedElmts<E, E> leSucc : bucketsOfSuccs.get(e).get(
							eSucc))
						he.addAll(abst2RealsE.get(leSucc._elmt));
					hjt.addHedge(he);
				}

			abst2RealsE.clear();
			// associate the root element to the list of its shared Element
			for (E root : roots.keySet()) {
				for (E succOfRoot : bucketsOfSuccs.get(root).keySet()) {
					for (LinkedElmts<E, E> le : bucketsOfSuccs.get(root).get(
							succOfRoot)) {
						if (!le._links.isEmpty()) {
							if (!abst2RealsE.containsKey(root))
								abst2RealsE.put(root, new ArrayList<E>());
							abst2RealsE.get(root).add(le._elmt);
						}
					}
				}
			}

			hg = new HyperGraph<E>();
			
			
			@SuppressWarnings("unchecked")
			ArrayList<E> aRoots = new ArrayList<E>( abst2RealsE.keySet());
			for (int i = 0; i < aRoots.size() - 1; i++)
				for (int j = i + 1; j < aRoots.size(); j++) {
					E r1 = aRoots.get(i);
					E r2 = aRoots.get(j);
					boolean linked = false;
					for (E e1 : abst2RealsE.get(r1))
						if (abst2RealsE.get(r2).contains(e1)) {
							ArrayList<E> he = new ArrayList<E>();
							he.add(r1);
							he.add(r2);
							hg.addHedge(he);
							linked = true;
							break;
						}
					if (linked)
						break;
				}

		}
		// create a new graphe based on the shared elements of the roots buckets

		return hjt;
		// while (!predecessorsOf.isEmpty()) {
		// // search for the leaves
		// HashMap<WeightedElmt<E>, ArrayList<ArrayList<WeightedElmt<E>>>>
		// leavesAncestors = new HashMap<WeightedElmt<E>,
		// ArrayList<ArrayList<WeightedElmt<E>>>>();
		// for (WeightedElmt<E> wv : predecessorsOf.keySet()) {
		// if (!successorsOf.containsKey(wv))
		// leavesAncestors.put(wv,
		// new ArrayList<ArrayList<WeightedElmt<E>>>(
		// predecessorsOf.get(wv).size()));
		// }
		//
		// // ce n'est pas une bonne id�e trop couteux et ne propage pas
		// // l'�l�ment
		// // plut�t propager l'�l�ment dns l'arbre et regarder s'il se
		// // supprime
		//
		// // for each direct predecessor save the set of ancestors
		// for (WeightedElmt<E> leaf : leavesAncestors.keySet()) {
		// for (int iDirectPred = 0; iDirectPred < predecessorsOf
		// .get(leaf).size(); iDirectPred++) {
		// WeightedElmt<E> directPred = predecessorsOf.get(leaf).get(
		// iDirectPred);
		// ArrayList<WeightedElmt<E>> alToVisit = new
		// ArrayList<WeightedElmt<E>>();
		// ArrayList<WeightedElmt<E>> alVisited = new
		// ArrayList<WeightedElmt<E>>();
		// alToVisit.add(directPred);
		// while (!alToVisit.isEmpty()) {
		// WeightedElmt<E> wvCur = alToVisit.remove(0);
		// for (WeightedElmt<E> wvPred : predecessorsOf.get(wvCur)) {
		// if (!alToVisit.contains(wvPred)
		// && !alVisited.contains(wvPred))
		// alToVisit.add(wvPred);
		// }
		// alVisited.add(wvCur);
		// }
		// leavesAncestors.get(leaf).add(iDirectPred, alVisited);
		// }
		// }
		// for (WeightedElmt<E> leaf : leavesAncestors.keySet()) {
		//
		// }
		//
		// }

	}
	
	public static <E extends Comparable<E>> HashMap<E,HashSet<E>> 
		fixPoint2Pred( HashMap<E,HashSet<E>> anc2preds){
		 HashMap<E,HashSet<E>> fixPoint2Pred = new HashMap<E,HashSet<E>>();
		 
		 while(!anc2preds.isEmpty()){
		 E mostFamous=null;
		 int nbPredOfmostFamous =-1;
		 for(E anc : anc2preds.keySet()){
			 if(anc2preds.get(anc).size()>nbPredOfmostFamous){
				 mostFamous =anc;
				 nbPredOfmostFamous = anc2preds.get(anc).size();
			 }
		 }
		 fixPoint2Pred.put(mostFamous, anc2preds.get(mostFamous));
		 anc2preds.remove(mostFamous);
		 ArrayList<E> toRem = new ArrayList<E>();
		 for(E anc : anc2preds.keySet()){
			 anc2preds.get(anc).removeAll( fixPoint2Pred.get(mostFamous));
			 if(anc2preds.get(anc).isEmpty()){
				 toRem.add(anc);
			 }
		 }
		 
		 for(E e :toRem)
			 anc2preds.remove(e);
		 }
		 return fixPoint2Pred ;
	}
	
	
	public static <E extends Comparable<E>> HyperGraph<E> distribJT2(
			HyperGraph<E> hg, 
			ArrayList<ArrayListComp<E>> buckets,
			ArrayList<ArrayListComp<E>> bucketFather
			) {

		HyperGraph<E> hjt = new HyperGraph<E>();
		
		// maps abstract node to its real representation
		HashMap<E, ArrayList<E>> abst2RealsE = new HashMap<E, ArrayList<E>>();
		for (E e : hg.getVertices()) {
			abst2RealsE.put(e, new ArrayList<E>());
			abst2RealsE.get(e).add(e);
		}

		while (hg.getVertices().size() > 1) {
			// transforms the hyper graph hg in a weighted hyper graph whg
			HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
			for (Collection<E> he : hg.getHEdges()) {
				HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
				for (E v : he)
					whedge.add(new WeightedElmt<E>(v));
				whg.addHedge(whedge);
			}

			// build the forest of trees where each tree has
			// as root a maxFillDistrb weighted node
			HashMap<E, ArrayList<E>> successorsOf = new HashMap<E, ArrayList<E>>();
			HashMap<E, ArrayList<E>> predecessorsOf = new HashMap<E, ArrayList<E>>();
			HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(
					whg.getVertices());
			while (!whg.getVertices().isEmpty()) {
				updateFillInWeights(v2Update, whg);
				v2Update.clear();
				ArrayList<WeightedElmt<E>> maxElmts = new ArrayList<WeightedElmt<E>>();
				for (WeightedElmt<E> wv : whg.getVertices()) {
					boolean isMaxInNeighborhood = true;
					for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
						if (wv.compareTo(wn) < 0) {
							isMaxInNeighborhood = false;
							break;
						}
						for (WeightedElmt<E> wnn : whg.getNeighbors(wn)) {
							if (wv.compareTo(wnn) < 0) {
								isMaxInNeighborhood = false;
								break;
							}
						}
						if (isMaxInNeighborhood == false) {
							break;
						}
					}
					if (isMaxInNeighborhood == true) {
						maxElmts.add(wv);
						if( whg.getNeighbors(wv).size()>0){
							predecessorsOf.put(wv.getElement(),new ArrayList<E>());
						for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
							predecessorsOf.get(wv.getElement()).add(
									wn.getElement());
							if (!successorsOf.containsKey(wn.getElement()))
								successorsOf.put(wn.getElement(),
										new ArrayList<E>());
							successorsOf.get(wn.getElement()).add(
									wv.getElement());
						}

						v2Update.addAll(whg.getNeighbors(wv));
						}
						
					}
				}
				for (WeightedElmt<E> maxE : maxElmts){
					whg.removeVertex(maxE);
				}
			}
			
			System.out.println("predecessors ");
			for(E elmt : predecessorsOf.keySet()){
				System.out.print(elmt+" preds");
				for(E pred : predecessorsOf.get(elmt))
					System.out.print(" "+pred);
				System.out.println();
			}
			
			System.out.println("successors ");
			for(E elmt : successorsOf.keySet()){
				System.out.print(elmt+" succs");
				for(E succ : successorsOf.get(elmt))
					System.out.print(" "+succ);
				System.out.println();
			}
			
			HashMap<E, LinkedElmts<E, E>> elmts2NotSeen = new HashMap<E, LinkedElmts<E, E>>();
			for (E elmt : predecessorsOf.keySet()) {
				LinkedElmts<E, E> e2NS = new LinkedElmts<E, E>(elmt);
				e2NS._links.addAll(predecessorsOf.get(elmt));
				elmts2NotSeen.put(elmt, e2NS);

			}

//			System.out.println(" Linked Elmts ");

			// create a bucket for each successor of an element, and add the
			// element to the bucket
			HashMap<E, HashMap<E, ArrayList<LinkedElmts<E, E>>>> bucketsOfSuccs = new HashMap<E, HashMap<E, ArrayList<LinkedElmts<E, E>>>>();
			for (E elmt : successorsOf.keySet()) {
				bucketsOfSuccs.put(elmt,
						new HashMap<E, ArrayList<LinkedElmts<E, E>>>());
				for (E succOfElmt : successorsOf.get(elmt)) {
					bucketsOfSuccs.get(elmt).put(succOfElmt,
							new ArrayList<LinkedElmts<E, E>>());
					// add the element to the bucket : no ned TO ADD HERE
//					LinkedElmts<E, E> e2NS = new LinkedElmts<E, E>(elmt);
//					if (predecessorsOf.containsKey(elmt))
//						e2NS._links.addAll(predecessorsOf.get(elmt));
//					bucketsOfSuccs.get(elmt).get(succOfElmt).add(e2NS);
				}

			}

//			System.out.println("buckets of successors ");
//			for(E e :bucketsOfSuccs.keySet()){
//				System.out.print("  "+e);
//				for(E eSucc :bucketsOfSuccs.get(e).keySet()){
//					System.out.print(" buck -> "+eSucc+" : ");
//					for(LinkedElmts<E, E> v : bucketsOfSuccs.get(e).get(eSucc))
//						System.out.print(" "+v._elmt+" "+v._links);
//				}
//				System.out.println();
//			}
			
			

			// save the roots Elements
			HashMap<E, ArrayList<E>> roots = new HashMap<E, ArrayList<E>>();
			for (E v : successorsOf.keySet()) {
				if (!predecessorsOf.containsKey(v))
					roots.put(v, new ArrayList<E>());
			}

			System.out.print(" save the roots ");
			for (E r : roots.keySet()) {
				System.out.print(" " + r);
			}
			System.out.println();

			
			
			// ancestorsOf.get(e).get(pred) return the set of ancestors reachable from e by pred
			HashMap<E,HashMap<E,HashSet<E>>> ancestorsOf = new HashMap<E, HashMap<E,HashSet<E>>>();
			for(E e : abst2RealsE.keySet()){
				ancestorsOf.put(e, new HashMap<E,HashSet<E>>());
				if(predecessorsOf.containsKey(e)){
					for(E pred : predecessorsOf.get(e))
					ancestorsOf.get(e).put(pred, new HashSet<E>());
				}
			}
			
			ArrayList<E> toVisit = new ArrayList<E>();
			toVisit.addAll(roots.keySet());
			while(!toVisit.isEmpty()){
				E curE = toVisit.remove(0);
				
				if(successorsOf.containsKey(curE))
					for(E succOfCur :successorsOf.get(curE)){
						if(!toVisit.contains(succOfCur))
							toVisit.add(succOfCur);
					}
				if(predecessorsOf.containsKey(curE)){
					for(E predOfCur :predecessorsOf.get(curE)){
						ancestorsOf.get(curE).get(predOfCur).add(predOfCur);
						for(E predOfPred : ancestorsOf.get(predOfCur).keySet())
							ancestorsOf.get(curE).get(predOfCur).addAll(
									ancestorsOf.get(predOfCur).get(predOfPred));
					}
				}
			}
			
			
			System.out.println(" ancestorsOf.get(e).get(pred) return ancestors reachable from e by pred ");
			for(E e :ancestorsOf.keySet()){
				System.out.print("  "+e);
				for(E predE :ancestorsOf.get(e).keySet()){
					System.out.print(" pred -> "+predE+" : ");
					for( E v : ancestorsOf.get(e).get(predE))
						System.out.print(" "+v);
				}
				System.out.println();
			}
			
			
			
			
			
			
			// anc2predsOf.get(e).get(anc) return for e, the list of predecessors that can reach anc
			HashMap<E,HashMap<E,HashSet<E>>> anc2predsOf = new HashMap<E, HashMap<E,HashSet<E>>>();
			for(E e : abst2RealsE.keySet()){
				anc2predsOf.put(e, new HashMap<E,HashSet<E>>());
			}
			
			for(E e : ancestorsOf.keySet()){
				for(E predOfE : ancestorsOf.get(e).keySet()){
					for(E ancBypredOfE :ancestorsOf.get(e).get(predOfE)){
						if(!anc2predsOf.get(e).containsKey(ancBypredOfE)){
							anc2predsOf.get(e).put(ancBypredOfE, new HashSet<E>());
						}
						anc2predsOf.get(e).get(ancBypredOfE).add(predOfE);
					}
				}
			}
			
			System.out.println("anc2predsOf.get(e).get(anc) return for e, the list of predecessors that can reach anc ");
			for(E e :anc2predsOf.keySet()){
				System.out.print("  "+e);
				for(E predE :anc2predsOf.get(e).keySet()){
					System.out.print(" pred -> "+predE+" : ");
					for( E v : anc2predsOf.get(e).get(predE))
						System.out.print(" "+v);
				}
				System.out.println();
			}
	
			// from leaves to roots we propagate elements respecting the running
			// intersection. The bottom up propagation of a leaf starts by its
			// predecessors
			// then follows the last predecessor link.
			// The propagation stop either a root is reach or all the paths
			// from a leaf converge
			int iTest = 0;
			
			while (!predecessorsOf.isEmpty()) {
				ArrayList<E> leaves = new ArrayList<E>();
				for (E v : predecessorsOf.keySet()) {
					if (!successorsOf.containsKey(v))
						leaves.add(v);
				}
				
				
				System.out.print(" leaves ");
				for (E l : leaves) {
					System.out.print(" " + l);
				}
				System.out.println();
			
				boolean stop = true ;
				if(stop && iTest==2)
					return null;
				iTest++;
				
				// propagate each leaf
				for (E leaf : leaves) {
					
					HashMap<E,HashSet<E>> fixPoint2Pred = fixPoint2Pred(anc2predsOf.get(leaf));
					
					// msgBox contains the expediter, the recipient and 
					// the element to propagate 
					ArrayList<MsgLE<E>> msgBox = new ArrayList<MsgLE<E>>();
					// initialize the list with the predecessor of the leaf
					for(E fP : fixPoint2Pred.keySet())
						for(E pred: fixPoint2Pred.get(fP)){						
						msgBox.add(new MsgLE<E>(leaf, pred,fP,
								elmts2NotSeen.get(leaf)));
					}
					
					while (!msgBox.isEmpty()) {
//						System.out.println();
						MsgLE<E> mCur = msgBox.remove(0);
						
//						System.out.println("msg exp:"+mCur._exp+" rec:"+mCur._rec+
//								" le:"+mCur._msg._elmt+ " "+mCur._msg._links);
						// add or intersect the received LE elmt to the
						// appropriate bucket of the recipient
						int iMsgLe = bucketsOfSuccs.get(mCur._rec)
								.get(mCur._exp).indexOf(mCur._msg);
						if (iMsgLe >= 0) {
//							System.out.println(" intersect msg");
							bucketsOfSuccs.get(mCur._rec).get(mCur._exp)
									.get(iMsgLe).intersect(mCur._msg);
						} else {
//							System.out.println(" add msg to "+ mCur._rec);
							LinkedElmts<E, E> nvLe = mCur._msg.clone();
							nvLe._links.remove(mCur._rec);
							bucketsOfSuccs.get(mCur._rec).get(mCur._exp)
									.add(nvLe);
						}

						// Test if the element need to be propagate
						iMsgLe = bucketsOfSuccs.get(mCur._rec).get(mCur._exp)
								.indexOf(mCur._msg);
						LinkedElmts<E, E> nvLe = bucketsOfSuccs.get(mCur._rec)
								.get(mCur._exp).get(iMsgLe);
						
//						System.out.println("nvLe elm:"+nvLe._elmt+" links:"+nvLe._links);
						
						if (nvLe._links.isEmpty()
								|| !predecessorsOf.containsKey(mCur._rec)) {
							continue;
						}

						// propagate the received element to the fix Point
						// predecessor
						if(!mCur._rec.equals(mCur._fixP)){
							int randPred = (int) (anc2predsOf.get(mCur._rec).get(mCur._fixP).size()*Math.random());
							@SuppressWarnings("unchecked")
							E fpPred = (E)anc2predsOf.get(mCur._rec).get(mCur._fixP).toArray()[randPred];
//							System.out.println(" fpPred Of "+mCur._rec+": "+fpPred);
							msgBox.add(new MsgLE<E>(mCur._rec, fpPred,mCur._fixP,nvLe));
						}
					}
					
					
				}
				
				for (E leaf : leaves) {
					predecessorsOf.remove(leaf);
				}
				
				ArrayList<E> succsToRem = new ArrayList<E>();
				for(E e : successorsOf.keySet()){
					ArrayList<E> leavesToRem = new ArrayList<E>();
					for(E succE :successorsOf.get(e)){
						if(leaves.contains(succE))
							leavesToRem.add(succE);
								
					}
					successorsOf.get(e).removeAll(leavesToRem);
					if(successorsOf.get(e).isEmpty())
						succsToRem.add(e);
				}
				for(E e:succsToRem)
				 successorsOf.remove(e);
			}
			
			

			// At this step buckets are stable we save them in
			// the returning jointrees avoidiing the root
			for (E e : bucketsOfSuccs.keySet())
				if (!roots.keySet().contains(e)) {
					for (E eSucc : bucketsOfSuccs.get(e).keySet()) {
						ArrayList<E> he = new ArrayList<E>();
						he.addAll(abst2RealsE.get(e));
						for (LinkedElmts<E, E> leSucc : bucketsOfSuccs.get(e)
								.get(eSucc))
							he.addAll(abst2RealsE.get(leSucc._elmt));
						hjt.addHedge(he);
						buckets.add(new ArrayListComp<E>(he));
						bucketFather.add(new ArrayListComp<E>(
								abst2RealsE.get(e)));

					}
				}

			abst2RealsE.clear();
			// associate the root element to the list of its shared Element
			for (E root : roots.keySet()) {
				for (E succOfRoot : bucketsOfSuccs.get(root).keySet()) {
					for (LinkedElmts<E, E> le : bucketsOfSuccs.get(root).get(
							succOfRoot)) {
						if (!le._links.isEmpty()) {
							if (!abst2RealsE.containsKey(root))
								abst2RealsE.put(root, new ArrayList<E>());
							abst2RealsE.get(root).add(le._elmt);
						}
					}
				}
			}
			
			// Since roots have been changed we can now add them
			for (E e : bucketsOfSuccs.keySet())
				if (roots.keySet().contains(e)) {
					for (E eSucc : bucketsOfSuccs.get(e).keySet()) {
						ArrayList<E> he = new ArrayList<E>();
						he.addAll(abst2RealsE.get(e));
						for (LinkedElmts<E, E> leSucc : bucketsOfSuccs.get(e)
								.get(eSucc))
							he.addAll(abst2RealsE.get(leSucc._elmt));
						hjt.addHedge(he);
						buckets.add(new ArrayListComp<E>(he));
						bucketFather.add(new ArrayListComp<E>(
								abst2RealsE.get(e)));

					}
				}
			

			hg = new HyperGraph<E>();
			
			
			@SuppressWarnings("unchecked")
			ArrayList<E> aRoots = new ArrayList<E>( abst2RealsE.keySet());
			for (int i = 0; i < aRoots.size() - 1; i++)
				for (int j = i + 1; j < aRoots.size(); j++) {
					E r1 = aRoots.get(i);
					E r2 = aRoots.get(j);
					boolean linked = false;
					for (E e1 : abst2RealsE.get(r1))
						if (abst2RealsE.get(r2).contains(e1)) {
							ArrayList<E> he = new ArrayList<E>();
							he.add(r1);
							he.add(r2);
							hg.addHedge(he);
							linked = true;
							break;
						}
					if (linked)
						break;
				}

		}
		// create a new graphe based on the shared elements of the roots buckets

		return hjt;
	}
	
	
	


	public static HyperGraph<Integer> genRandomGraph(
			HashMap<String, Object> params) {

		HyperGraph<Integer> dhgBA = new HyperGraph<Integer>();
		TreeMap<Integer, TreeSet<Integer>> g = null;
		if (params.get("randomGraph").equals("BA")) {
			g = benchMarkGenerator.graph.RandomGraphs.genBAGraph(params);
		}
		if (params.get("randomGraph").equals("WS")) {
			g = benchMarkGenerator.graph.RandomGraphs.genWSGraph(params);
		}
		if (params.get("randomGraph").equals("UD")) {
			g = benchMarkGenerator.graph.RandomGraphs.genUDGraph(params);
		}

		for (Integer idP : g.keySet())
			for (Integer idN : g.get(idP)) {
				if (idP < idN) {
					ArrayList<Integer> he = new ArrayList<Integer>();
					he.add(idP);
					he.add(idN);
					dhgBA.addHedge(he);
				}
			}

		return dhgBA;
	}

	public static HashMap<String, File> saveMetricsInFiles(
			HashMap<String, HashMap<Integer, Number>> metrics, File outFolder)
			throws IOException {

		HashMap<String, File> curveFiles = new HashMap<String, File>();
		for (String metric : metrics.keySet()) {
			File fDat = new File(outFolder.getPath() + File.separator + metric
					+ ".dat");
			BufferedWriter bw = new BufferedWriter(new FileWriter(fDat));
			// bw.write("Vertices\t"+metric+"\n");
			System.out.println(" metric " + metric);
			TreeSet<Integer> nbVerticesTS = new TreeSet<Integer>(metrics.get(
					metric).keySet());
			for (Integer nbVertices : nbVerticesTS) {
				bw.write(nbVertices + " " + metrics.get(metric).get(nbVertices)
						+ "\n");
			}
			bw.close();
			curveFiles.put(metric, fDat);
		}

		return curveFiles;
	}

	public static void writeDefault2DLinesPoints(String x, String y,
			HashMap<String, File> curveFiles, File outFolder, String extraTitle)
			throws Exception {

		String title = /* ""+y+" by "+ x+ */"(" + extraTitle + ")";
		String drawType = y.isEmpty() ? "points" : "linespoints";

		String cmd = "reset \n" + "set terminal postscript enhanced color \n" 
				+ "set output \'"
				+ outFolder.getPath() + File.separator + "draw.eps\' \n"
				+ "set title \'" + title + "\' \n" 
				+ "set autoscale \n"
				+ "set style data " + drawType +" \n" 
				+ "set key inside left top vertical \n"
				+ "set xlabel \'" + x + "\' \n" + "set ylabel \'" + y + "\' \n"
				+ "set grid \n"
//				+ "set style line 1 lt 1 lw 3.0 \n"
//				+ "set style line 3 lt 3 lw 3.0 \n"
//				+ "set pointsize 0.5 \n" 
				+ "plot ";
		
		int iLine = 1;
		for (String curveName : curveFiles.keySet()) {
			cmd = cmd + "\'" + curveFiles.get(curveName).getPath() + "\' "
					+ " using 1:2 "
					+ " lt "+iLine
//					+ " pi 3"
					// pt gives a particular point type: 1=+, 2=X, 3=*, 4=square, 5=filled square, 6=circle,   7=filled circle, 8=triangle, 9=filled triangle, etc.
					+ " pt "+ iLine
					// postscipt: 1=diamond 2=+ 3=square 4=X 5=triangle 6=*
//					+ " ps "+ (iLine+3)
					+ " title \'" + curveName + "\'" + ",  ";
			iLine++;
		}
		
		cmd = cmd.substring(0, cmd.lastIndexOf(","));

		// System.out.println(cmd);

		File fPlot = new File(outFolder.getPath() + File.separator
				+ "linePoints.plot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(fPlot));
		bw.write(cmd);
		bw.write("\n");
		bw.close();
		
		System.out.println("write file: " +fPlot.getPath());
		if(System.getProperty("file.separator").equals("/"))
			Runtime.getRuntime().exec("gnuplot "+ fPlot.getPath());
		else
			Runtime.getRuntime().exec("C:/Program Files/" +
					"gnuplot/binary/gnuplot.exe "
						+ fPlot.getPath());

	}
	
	public static void tmp2DLinesPlot()
		throws Exception {
	
		String x= "nbVertices";
		String y= "avgTime";
		String outFolderName = "C:/Users/VinTo/workspace/CP2012/draws/expe/WSGraph/avgTime";
		File outFolder = new File(outFolderName);
		HashMap<Integer, File> curveFiles = new HashMap<Integer, File>();
		curveFiles.put(0, new File(outFolderName+ File.separator +"BE-DFS.dat"));
		curveFiles.put(1, new File(outFolderName+ File.separator +"BE-DFS-MCS.dat"));
		curveFiles.put(2, new File(outFolderName+ File.separator +"BE-MinFill.dat"));
		curveFiles.put(3, new File(outFolderName+ File.separator +"TE-MinCluster.dat"));
//		curveFiles.put("TE-MinCluster-Bis", new File(outFolderName+ File.separator +"TE-MinCluster-Bis.dat"));
		curveFiles.put(4, new File(outFolderName+ File.separator +"TE-MinProj.dat"));
//		curveFiles.put("TE-MinProj-Bis", new File(outFolderName+ File.separator +"TE-MinProj-Bis.dat"));
		
		String[] curNames = {"BE-DFS","BE-DFS-MCS","BE-MinFill","TE-MinCluster","TE-MinProj"};
		
		String extraTitle = "WSGraph step: 100 nbInstances 10";
		
	String title = /* ""+y+" by "+ x+ */"(" + extraTitle + ")";
	String drawType = y.isEmpty() ? "points" : "linespoints";

	String cmd = "reset \n" + "set terminal postscript \n" 
			+ "set output \'"
			+ outFolder.getPath() + File.separator + "draw.eps\' \n"
			+ "set title \'" + title + "\' \n" 
//			+ "set size square \n"
			+ "set autoscale \n"
			+ "set style data " + drawType +" \n" 
			+ "set key left top \n"
			+ "set xlabel \'" + x + "\' \n" + "set ylabel \'" + y + "\' \n"
			+ "set grid \n"
//			+ "set style line 1 lt 1 lw 3.0 \n"
//			+ "set style line 3 lt 3 lw 3.0 \n"
//			+ "set pointsize 0.5 \n" 
			+ "plot ";
	
	int iLine = 1;
	for (Integer curveNum : curveFiles.keySet()) {
		cmd = cmd + "\'" + curveFiles.get(curveNum).getPath() + "\' "
				+ " using 1:2 "
				+ " lt "+iLine
//				+ " pi 3"
				// pt gives a particular point type: 1=+, 2=X, 3=*, 4=square, 5=filled square, 6=circle,  7=filled circle, 8=triangle, 9=filled triangle, etc.
				+ " pt "+ iLine
				// postscipt: 1=diamond 2=+ 3=square 4=X 5=triangle 6=*
//				+ " ps "+ (iLine+3)
				+ " title \'" + curNames[curveNum] + "\'" + ",  ";
		iLine++;
	}
	
	cmd = cmd.substring(0, cmd.lastIndexOf(","));

	// System.out.println(cmd);

	File fPlot = new File(outFolder.getPath() + File.separator
			+ "linePoints.plot");
	BufferedWriter bw = new BufferedWriter(new FileWriter(fPlot));
	bw.write(cmd);
	bw.write("\n");
	bw.close();
	
	System.out.println("write file: " +fPlot.getPath());
	if(System.getProperty("file.separator").equals("/"))
		Runtime.getRuntime().exec("gnuplot "+ fPlot.getPath());
	else
		Runtime.getRuntime().exec("C:/Program Files/" +
				"gnuplot/binary/gnuplot.exe "
					+ fPlot.getPath());

	}
	
	
	

	public static void testRandom() throws Exception{
		// random instance
		HashMap<String, HashMap<Integer, Number>> metrics = new HashMap<String, HashMap<Integer, Number>>();
		metrics.put("avgMinFillTimeOrder", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTimeOrder",
				new HashMap<Integer, Number>());
		 metrics.put("avgBFSTimeOrder", new HashMap<Integer,Number>());

		// metrics.put("avgMinFillTimeBEJT", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		 metrics.put("avgBFSTimeBEJT", new HashMap<Integer,Number>());

		// metrics.put("avgMinFillTime", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTime", new HashMap<Integer, Number>());
		 metrics.put("avgBFSTime", new HashMap<Integer,Number>());

		// metrics.put("avgMinFillJTwidth", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistJTwidth", new HashMap<Integer, Number>());
		 metrics.put("avgBFSJTwidth", new HashMap<Integer,Number>());

		int nbInstances = 5;
		int stepNbVertices = 100;
		int nbVerticesMax = 520;
		for (int nbVertices = 20; nbVertices <= nbVerticesMax; nbVertices += stepNbVertices) {

			System.out.println();
			System.out.println("nbVertices " + nbVertices);
			System.out.println();
			// metrics.get("avgMinFillTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxFillDistTimeOrder").put(nbVertices, (long) 0.0);
			metrics.get("avgMinFillDistTimeOrder").put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMinFillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin1FillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeOrder").put(nbVertices,(long) 0.0);

			// metrics.get("avgMinFillTimeBEJT").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxFillDistTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgMinFillDistTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMinFillDistTimeBEJT")
					.put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMin1FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin3FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeBEJT").put(nbVertices,(long) 0.0);

			// metrics.get("avgMinFillJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMinFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMinFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMin1FillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMin2FillDistJTwidth").put(nbVertices, 0);
			 metrics.get("avgBFSJTwidth").put(nbVertices,0);

			for (int numInstance = 0; numInstance < nbInstances; numInstance++) {

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("nbPeers", "" + nbVertices);
				params.put("randomGraph", "BA");
				HyperGraph<Integer> dhg = genRandomGraph(params);
				// System.out.println(" Le graphe " + dhg);

				// long timeMinFillorder = System.currentTimeMillis();
				// ArrayList<Integer> minFillorder = HyperGraphs
				// .minFillInvOrder(dhg);
				// timeMinFillorder = System.currentTimeMillis() -
				// timeMinFillorder;
				// metrics.get("avgMinFillTimeOrder").put(nbVertices,
				// ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))+
				// timeMinFillorder);
				// // System.out.println(" minFillorder  " + minFillorder);
				// System.out.println("timeMinFillorder "+ timeMinFillorder);

				long timeMaxFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxFillDistorder = minMaxFillDistOrder(
						"max", dhg);
				timeMaxFillDistorder = System.currentTimeMillis()
						- timeMaxFillDistorder;
				metrics.get("avgMaxFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxFillDistTimeOrder").get(
								nbVertices))
								+ timeMaxFillDistorder);
				// System.out.println(" MaxFillDistorder  " + MaxFillDistorder);
				System.out.println("timeMaxFillDistorder "
						+ timeMaxFillDistorder);

				long timeMinFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> minFillDistorder = minMaxFillDistOrder(
						"min", dhg);
				timeMinFillDistorder = System.currentTimeMillis()
						- timeMinFillDistorder;
				metrics.get("avgMinFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMinFillDistTimeOrder").get(
								nbVertices))
								+ timeMinFillDistorder);
				System.out.println("timeMinFillDistorder "
						+ timeMinFillDistorder);

				long timeMaxMinFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMinFillDistorder = minMaxFillDistOrder(
						"maxmin", dhg);
				timeMaxMinFillDistorder = System.currentTimeMillis()
						- timeMaxMinFillDistorder;
				metrics.get("avgMaxMinFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMinFillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMinFillDistorder);
				System.out.println("timeMaxMinFillDistorder "
						+ timeMaxMinFillDistorder);

				long timeMaxMin1FillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMin1FillDistorder = minMaxFillDistOrder(
						"maxmin1", dhg);
				timeMaxMin1FillDistorder = System.currentTimeMillis()
						- timeMaxMin1FillDistorder;
				metrics.get("avgMaxMin1FillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin1FillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMin1FillDistorder);
				System.out.println("timeMaxMin1FillDistorder "
						+ timeMaxMin1FillDistorder);

				long timeMaxMin2FillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMin2FillDistorder = minMaxFillDistOrder(
						"maxmin2", dhg);
				timeMaxMin2FillDistorder = System.currentTimeMillis()
						- timeMaxMin2FillDistorder;
				metrics.get("avgMaxMin2FillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMin2FillDistorder);
				System.out.println("timeMaxMin2FillDistorder "
						+ timeMaxMin2FillDistorder);

				 long timeBFSorder = System.currentTimeMillis();
				 ArrayList<Integer> BFSorder = bFSOrder(dhg);
				 timeBFSorder = System.currentTimeMillis() - timeBFSorder;
				 metrics.get("avgBFSTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))+
				 timeBFSorder);
				 System.out.println("timeBFSorder "+ timeBFSorder);

				// Built Jointree by BE with the Order and

				// ArrayList<ArrayList<Integer>> bucketsMinF = new
				// ArrayList<ArrayList<Integer>>();
				// Integer[] varFatherMinF = new Integer[minFillorder.size()];
				// long timeMinFillBEJT = System.currentTimeMillis();
				// HyperGraph<Integer> jtMinF =
				// HyperGraphs.bucketElimination(dhg,
				// minFillorder, bucketsMinF, varFatherMinF);
				// timeMinFillBEJT = System.currentTimeMillis() -
				// timeMinFillBEJT;
				// metrics.get("avgMinFillTimeBEJT").put(nbVertices,
				// ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))+
				// timeMinFillBEJT);
				// int twMinFillBE = jtMinF.width();
				// metrics.get("avgMinFillJTwidth").put(nbVertices,
				// ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))+
				// twMinFillBE);
				// System.out.println(" jointree par BE Min-Fill witdh " +
				// jtMinF.width()+" time "+timeMinFillBEJT);

				ArrayList<ArrayList<Integer>> bucketsMaxF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxF = new Integer[maxFillDistorder.size()];
				long timeMaxFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxF = HyperGraphs.bucketElimination(dhg,
						maxFillDistorder, bucketsMaxF, varFatherMaxF);
				timeMaxFillDistBEJT = System.currentTimeMillis()
						- timeMaxFillDistBEJT;
				metrics.get("avgMaxFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxFillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxFillDistBEJT);
				int twMaxFillDistBE = jtMaxF.width();
				metrics.get("avgMaxFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxFillDistJTwidth").get(
								nbVertices))
								+ twMaxFillDistBE);
				System.out.println(" jointree par BE Max-Fill Dist width "
						+ jtMaxF.width() + " time " + timeMaxFillDistBEJT);

				ArrayList<ArrayList<Integer>> bucketsMinDF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMinDF = new Integer[minFillDistorder.size()];
				long timeMinFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMinDF = HyperGraphs.bucketElimination(
						dhg, minFillDistorder, bucketsMinDF, varFatherMinDF);
				timeMinFillDistBEJT = System.currentTimeMillis()
						- timeMinFillDistBEJT;
				metrics.get("avgMinFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMinFillDistTimeBEJT").get(
								nbVertices))
								+ timeMinFillDistBEJT);
				int twMinFillDistBE = jtMinDF.width();
				metrics.get("avgMinFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMinFillDistJTwidth").get(
								nbVertices))
								+ twMinFillDistBE);
				System.out.println(" jointree par BE Min-Fill Dist width "
						+ jtMinDF.width() + " time " + timeMinFillDistBEJT);

				ArrayList<ArrayList<Integer>> bucketsMaxMinDF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMinDF = new Integer[maxMinFillDistorder
						.size()];
				long timeMaxMinFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxMinDF = HyperGraphs.bucketElimination(
						dhg, maxMinFillDistorder, bucketsMaxMinDF,
						varFatherMaxMinDF);
				timeMaxMinFillDistBEJT = System.currentTimeMillis()
						- timeMaxMinFillDistBEJT;
				metrics.get("avgMaxMinFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMinFillDistBEJT);
				int twMaxMinFillDistBE = jtMaxMinDF.width();
				metrics.get("avgMaxMinFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMinFillDistJTwidth").get(
								nbVertices))
								+ twMaxMinFillDistBE);
				System.out.println(" jointree par BE Max-Min-Fill Dist width "
						+ jtMaxMinDF.width() + " time "
						+ timeMaxMinFillDistBEJT);

				ArrayList<ArrayList<Integer>> bucketsMaxMin1DF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMin1DF = new Integer[maxMin1FillDistorder
						.size()];
				long timeMaxMin1FillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxMin1DF = HyperGraphs
						.bucketElimination(dhg, maxMin1FillDistorder,
								bucketsMaxMin1DF, varFatherMaxMin1DF);
				timeMaxMin1FillDistBEJT = System.currentTimeMillis()
						- timeMaxMin1FillDistBEJT;
				metrics.get("avgMaxMin1FillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMin1FillDistBEJT);
				int twMaxMin1FillDistBE = jtMaxMin1DF.width();
				metrics.get("avgMaxMin1FillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMin1FillDistJTwidth")
								.get(nbVertices)) + twMaxMin1FillDistBE);
				System.out.println(" jointree par BE Max-Min-Fill1 Dist width "
						+ jtMaxMin1DF.width() + " time "
						+ timeMaxMin1FillDistBEJT);

//				ArrayList<ArrayList<Integer>> bucketsMaxMin2DF = new ArrayList<ArrayList<Integer>>();
//				Integer[] varFatherMaxMin2DF = new Integer[maxMin2FillDistorder
//						.size()];
////				long timeMaxMin2FillDistBEJT = System.currentTimeMillis();
//				HyperGraph<Integer> jtMaxMin2DF = HyperGraphs
//						.bucketElimination(dhg, maxMin2FillDistorder,
//								bucketsMaxMin2DF, varFatherMaxMin2DF);
				
				long timeMaxMin2FillDistBEJT = System.currentTimeMillis();
				
				HyperGraph<AbstCompArrayList<Integer>> dualHjt = new HyperGraph<AbstCompArrayList<Integer>>();
				HyperGraph<Integer> jtMaxMin2DF = DAGDecomp.distribJT2(dhg, dualHjt);
				
				
				
				timeMaxMin2FillDistBEJT = System.currentTimeMillis()
						- timeMaxMin2FillDistBEJT;
				metrics.get("avgMaxMin2FillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMin2FillDistBEJT);
				int twMaxMin2FillDistBE = jtMaxMin2DF.width();
				metrics.get("avgMaxMin2FillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMin2FillDistJTwidth")
								.get(nbVertices)) + twMaxMin2FillDistBE);
				System.out.println(" jointree par BE Max-Min-Fill2 Dist width "
						+ jtMaxMin2DF.width() + " time "
						+ timeMaxMin2FillDistBEJT);

				 ArrayList<ArrayList<Integer>> bucketsBFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherBFS = new Integer[BFSorder.size()];
				 long timeBFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtBFS =
				 HyperGraphs.bucketElimination(dhg,
				 BFSorder, bucketsBFS, varFatherBFS);
				 timeBFSBEJT = System.currentTimeMillis() - timeBFSBEJT;
				 metrics.get("avgBFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))+
				 timeBFSBEJT);
				 int twBFSBE = jtBFS.width();
				 metrics.get("avgBFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))+
				 twBFSBE);
				 System.out.println(" jointree par BE BFS width " +
				 jtBFS.width()+" time "+timeBFSBEJT);
			}

			// metrics.get("avgMinFillTimeOrder").put(nbVertices,
			// ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMinFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMinFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin1FillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))/nbInstances);

			// metrics.get("avgMinFillTimeBEJT").put(nbVertices,
			// ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMinFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))/nbInstances);

			// metrics.get("avgMinFillTime").put(nbVertices,
			// (Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices)+
			// (Long)metrics.get("avgMinFillTimeOrder").get(nbVertices));
			metrics.get("avgMaxFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxFillDistTimeBEJT")
							.get(nbVertices)
							+ (Long) metrics.get("avgMaxFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMinFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMinFillDistTimeBEJT")
							.get(nbVertices)
							+ (Long) metrics.get("avgMinFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMinFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMinFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMin1FillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMin1FillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMin2FillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMin2FillDistTimeOrder")
									.get(nbVertices));
			 metrics.get("avgBFSTime").put(nbVertices,
			 (Long)metrics.get("avgBFSTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgBFSTimeOrder").get(nbVertices));

			// metrics.get("avgMinFillJTwidth").put(nbVertices,
			// ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMinFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMinFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMin1FillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMin2FillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))/nbInstances);
		}

		File dirOut = new File("C:/Users/VinTo/workspace/expe120411");
		if (!dirOut.exists())
			dirOut.mkdirs();

		HashMap<String, HashMap<String, HashMap<Integer, Number>>> dataPlots = new HashMap<String, HashMap<String, HashMap<Integer, Number>>>();

		dataPlots.put("avgTimeOrder",
				new HashMap<String, HashMap<Integer, Number>>());
		// dataPlots.get("avgTimeOrder").put("avgMinFillTimeOrder",
		// metrics.get("avgMinFillTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMaxFillDistTimeOrder",
				metrics.get("avgMaxFillDistTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMinFillDistTimeOrder",
				metrics.get("avgMinFillDistTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMaxMinFillDistTimeOrder",
				metrics.get("avgMaxMinFillDistTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMaxMin1FillDistTimeOrder",
				metrics.get("avgMaxMin1FillDistTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMaxMin2FillDistTimeOrder",
				metrics.get("avgMaxMin2FillDistTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgBFSTimeOrder",
		 metrics.get("avgBFSTimeOrder"));

		dataPlots.put("avgTimeBEJT",
				new HashMap<String, HashMap<Integer, Number>>());
		// dataPlots.get("avgTimeBEJT").put("avgMinFillTimeBEJT",
		// metrics.get("avgMinFillTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMaxFillDistTimeBEJT",
				metrics.get("avgMaxFillDistTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMinFillDistTimeBEJT",
				metrics.get("avgMinFillDistTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMaxMinFillDistTimeBEJT",
				metrics.get("avgMaxMinFillDistTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMaxMin1FillDistTimeBEJT",
				metrics.get("avgMaxMin1FillDistTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMaxMin2FillDistTimeBEJT",
				metrics.get("avgMaxMin2FillDistTimeBEJT"));
		 dataPlots.get("avgTimeBEJT").put("avgBFSTimeBEJT",
		 metrics.get("avgBFSTimeBEJT"));

		dataPlots.put("avgTime",
				new HashMap<String, HashMap<Integer, Number>>());
		// dataPlots.get("avgTime").put("avgMinFillTime",
		// metrics.get("avgMinFillTime"));
		dataPlots.get("avgTime").put("avgMaxFillDistTime",
				metrics.get("avgMaxFillDistTime"));
		dataPlots.get("avgTime").put("avgMinFillDistTime",
				metrics.get("avgMinFillDistTime"));
		dataPlots.get("avgTime").put("avgMaxMinFillDistTime",
				metrics.get("avgMaxMinFillDistTime"));
		dataPlots.get("avgTime").put("avgMaxMin1FillDistTime",
				metrics.get("avgMaxMin1FillDistTime"));
		dataPlots.get("avgTime").put("avgMaxMin2FillDistTime",
				metrics.get("avgMaxMin2FillDistTime"));
		 dataPlots.get("avgTime").put("avgBFSTime",
		 metrics.get("avgBFSTime"));

		dataPlots.put("avgJTWidth",
				new HashMap<String, HashMap<Integer, Number>>());
		// dataPlots.get("avgJTWidth").put("avgMinFillJTwidth",
		// metrics.get("avgMinFillJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMaxFillDistJTwidth",
				metrics.get("avgMaxFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMinFillDistJTwidth",
				metrics.get("avgMinFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMaxMinFillDistJTwidth",
				metrics.get("avgMaxMinFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMaxMin1FillDistJTwidth",
				metrics.get("avgMaxMin1FillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMaxMin2FillDistJTwidth",
				metrics.get("avgMaxMin2FillDistJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgBFSJTwidth",
		 metrics.get("avgBFSJTwidth"));

		String extraTitle = " BAGraph " + "step: " + stepNbVertices
				+ "  nbInstances " + nbInstances;
		for (String plotName : dataPlots.keySet()) {
			File dirPlot = new File(dirOut.getPath() + File.separator
					+ plotName);
			if (dirPlot.exists())
				FileTools.recursiveDelete(dirPlot);
			dirPlot.mkdirs();
			HashMap<String, File> curveFiles = saveMetricsInFiles(
					dataPlots.get(plotName), dirPlot);
			writeDefault2DLinesPoints("nbVertices", plotName, curveFiles,
					dirPlot, extraTitle);
		}

	}
	
	public static void testRandomTok() throws Exception{
		// random instance
		HashMap<String, HashMap<Integer, Number>> metrics = new HashMap<String, HashMap<Integer, Number>>();
		 metrics.put("avgMinFillTimeOrder", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTimeOrder",
				new HashMap<Integer, Number>());
		 metrics.put("avgBFSTimeOrder", new HashMap<Integer,Number>());

		// metrics.put("avgMinFillTimeBEJT", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		 metrics.put("avgBFSTimeBEJT", new HashMap<Integer,Number>());

		// metrics.put("avgMinFillTime", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTime", new HashMap<Integer, Number>());
		 metrics.put("avgBFSTime", new HashMap<Integer,Number>());

		// metrics.put("avgMinFillJTwidth", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistJTwidth", new HashMap<Integer, Number>());
		 metrics.put("avgBFSJTwidth", new HashMap<Integer,Number>());

		int nbInstances = 5;
		int stepNbVertices = 100;
		int nbVerticesMax = 520;
		for (int nbVertices = 20; nbVertices <= nbVerticesMax; nbVertices += stepNbVertices) {

			System.out.println();
			System.out.println("nbVertices " + nbVertices);
			System.out.println();
			// metrics.get("avgMinFillTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxFillDistTimeOrder").put(nbVertices, (long) 0.0);
			metrics.get("avgMinFillDistTimeOrder").put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMinFillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin1FillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeOrder").put(nbVertices,(long) 0.0);

			// metrics.get("avgMinFillTimeBEJT").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxFillDistTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgMinFillDistTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMinFillDistTimeBEJT")
					.put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMin1FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin3FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeBEJT").put(nbVertices,(long) 0.0);

			// metrics.get("avgMinFillJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMinFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMinFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMin1FillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMin2FillDistJTwidth").put(nbVertices, 0);
			 metrics.get("avgBFSJTwidth").put(nbVertices,0);

			for (int numInstance = 0; numInstance < nbInstances; numInstance++) {

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("nbPeers", "" + nbVertices);
				params.put("randomGraph", "BA");
				HyperGraph<Integer> dhg = genRandomGraph(params);
				// System.out.println(" Le graphe " + dhg);

				// long timeMinFillorder = System.currentTimeMillis();
				// ArrayList<Integer> minFillorder = HyperGraphs
				// .minFillInvOrder(dhg);
				// timeMinFillorder = System.currentTimeMillis() -
				// timeMinFillorder;
				// metrics.get("avgMinFillTimeOrder").put(nbVertices,
				// ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))+
				// timeMinFillorder);
				// // System.out.println(" minFillorder  " + minFillorder);
				// System.out.println("timeMinFillorder "+ timeMinFillorder);

				long timeMaxFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxFillDistorder = minMaxFillDistOrder(
						"max", dhg);
				timeMaxFillDistorder = System.currentTimeMillis()
						- timeMaxFillDistorder;
				metrics.get("avgMaxFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxFillDistTimeOrder").get(
								nbVertices))
								+ timeMaxFillDistorder);
				// System.out.println(" MaxFillDistorder  " + MaxFillDistorder);
				System.out.println("timeMaxFillDistorder "
						+ timeMaxFillDistorder);

				long timeMinFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> minFillDistorder = minMaxFillDistOrder(
						"min", dhg);
				timeMinFillDistorder = System.currentTimeMillis()
						- timeMinFillDistorder;
				metrics.get("avgMinFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMinFillDistTimeOrder").get(
								nbVertices))
								+ timeMinFillDistorder);
				System.out.println("timeMinFillDistorder "
						+ timeMinFillDistorder);

				long timeMaxMinFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMinFillDistorder = minMaxFillDistOrder(
						"maxmin", dhg);
				timeMaxMinFillDistorder = System.currentTimeMillis()
						- timeMaxMinFillDistorder;
				metrics.get("avgMaxMinFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMinFillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMinFillDistorder);
				System.out.println("timeMaxMinFillDistorder "
						+ timeMaxMinFillDistorder);

				long timeMaxMin1FillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMin1FillDistorder = minMaxFillDistOrder(
						"maxmin1", dhg);
				timeMaxMin1FillDistorder = System.currentTimeMillis()
						- timeMaxMin1FillDistorder;
				metrics.get("avgMaxMin1FillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin1FillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMin1FillDistorder);
				System.out.println("timeMaxMin1FillDistorder "
						+ timeMaxMin1FillDistorder);

				long timeMaxMin2FillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMin2FillDistorder = minMaxFillDistOrder(
						"maxmin2", dhg);
				timeMaxMin2FillDistorder = System.currentTimeMillis()
						- timeMaxMin2FillDistorder;
				metrics.get("avgMaxMin2FillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMin2FillDistorder);
				System.out.println("timeMaxMin2FillDistorder "
						+ timeMaxMin2FillDistorder);

				 long timeBFSorder = System.currentTimeMillis();
				 ArrayList<Integer> BFSorder = bFSOrder(dhg);
				 timeBFSorder = System.currentTimeMillis() - timeBFSorder;
				 metrics.get("avgBFSTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))+
				 timeBFSorder);
				 System.out.println("timeBFSorder "+ timeBFSorder);

				// Built Jointree by BE with the Order and

				// ArrayList<ArrayList<Integer>> bucketsMinF = new
				// ArrayList<ArrayList<Integer>>();
				// Integer[] varFatherMinF = new Integer[minFillorder.size()];
				// long timeMinFillBEJT = System.currentTimeMillis();
				// HyperGraph<Integer> jtMinF =
				// HyperGraphs.bucketElimination(dhg,
				// minFillorder, bucketsMinF, varFatherMinF);
				// timeMinFillBEJT = System.currentTimeMillis() -
				// timeMinFillBEJT;
				// metrics.get("avgMinFillTimeBEJT").put(nbVertices,
				// ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))+
				// timeMinFillBEJT);
				// int twMinFillBE = jtMinF.width();
				// metrics.get("avgMinFillJTwidth").put(nbVertices,
				// ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))+
				// twMinFillBE);
				// System.out.println(" jointree par BE Min-Fill witdh " +
				// jtMinF.width()+" time "+timeMinFillBEJT);

				ArrayList<ArrayList<Integer>> bucketsMaxF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxF = new Integer[maxFillDistorder.size()];
				long timeMaxFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxF = HyperGraphs.bucketElimination(dhg,
						maxFillDistorder, bucketsMaxF, varFatherMaxF);
				timeMaxFillDistBEJT = System.currentTimeMillis()
						- timeMaxFillDistBEJT;
				metrics.get("avgMaxFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxFillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxFillDistBEJT);
				int twMaxFillDistBE = jtMaxF.width();
				metrics.get("avgMaxFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxFillDistJTwidth").get(
								nbVertices))
								+ twMaxFillDistBE);
				System.out.println(" jointree par BE Max-Fill Dist width "
						+ jtMaxF.width() + " time " + timeMaxFillDistBEJT);

				ArrayList<ArrayList<Integer>> bucketsMinDF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMinDF = new Integer[minFillDistorder.size()];
				long timeMinFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMinDF = HyperGraphs.bucketElimination(
						dhg, minFillDistorder, bucketsMinDF, varFatherMinDF);
				timeMinFillDistBEJT = System.currentTimeMillis()
						- timeMinFillDistBEJT;
				metrics.get("avgMinFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMinFillDistTimeBEJT").get(
								nbVertices))
								+ timeMinFillDistBEJT);
				int twMinFillDistBE = jtMinDF.width();
				metrics.get("avgMinFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMinFillDistJTwidth").get(
								nbVertices))
								+ twMinFillDistBE);
				System.out.println(" jointree par BE Min-Fill Dist width "
						+ jtMinDF.width() + " time " + timeMinFillDistBEJT);

				ArrayList<ArrayList<Integer>> bucketsMaxMinDF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMinDF = new Integer[maxMinFillDistorder
						.size()];
				long timeMaxMinFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxMinDF = HyperGraphs.bucketElimination(
						dhg, maxMinFillDistorder, bucketsMaxMinDF,
						varFatherMaxMinDF);
				timeMaxMinFillDistBEJT = System.currentTimeMillis()
						- timeMaxMinFillDistBEJT;
				metrics.get("avgMaxMinFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMinFillDistBEJT);
				int twMaxMinFillDistBE = jtMaxMinDF.width();
				metrics.get("avgMaxMinFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMinFillDistJTwidth").get(
								nbVertices))
								+ twMaxMinFillDistBE);
				System.out.println(" jointree par BE Max-Min-Fill Dist width "
						+ jtMaxMinDF.width() + " time "
						+ timeMaxMinFillDistBEJT);

				ArrayList<ArrayList<Integer>> bucketsMaxMin1DF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMin1DF = new Integer[maxMin1FillDistorder
						.size()];
				long timeMaxMin1FillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxMin1DF = HyperGraphs
						.bucketElimination(dhg, maxMin1FillDistorder,
								bucketsMaxMin1DF, varFatherMaxMin1DF);
				timeMaxMin1FillDistBEJT = System.currentTimeMillis()
						- timeMaxMin1FillDistBEJT;
				metrics.get("avgMaxMin1FillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMin1FillDistBEJT);
				int twMaxMin1FillDistBE = jtMaxMin1DF.width();
				metrics.get("avgMaxMin1FillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMin1FillDistJTwidth")
								.get(nbVertices)) + twMaxMin1FillDistBE);
				System.out.println(" jointree par BE Max-Min-Fill1 Dist width "
						+ jtMaxMin1DF.width() + " time "
						+ timeMaxMin1FillDistBEJT);

//				ArrayList<ArrayList<Integer>> bucketsMaxMin2DF = new ArrayList<ArrayList<Integer>>();
//				Integer[] varFatherMaxMin2DF = new Integer[maxMin2FillDistorder
//						.size()];
////				long timeMaxMin2FillDistBEJT = System.currentTimeMillis();
//				HyperGraph<Integer> jtMaxMin2DF = HyperGraphs
//						.bucketElimination(dhg, maxMin2FillDistorder,
//								bucketsMaxMin2DF, varFatherMaxMin2DF);
				
				long timeMaxMin2FillDistBEJT = System.currentTimeMillis();
				
				HyperGraph<AbstCompArrayList<Integer>> dualHjt = new HyperGraph<AbstCompArrayList<Integer>>();
				HyperGraph<Integer> jtMaxMin2DF = DAGDecomp.distribJT2(dhg, dualHjt);
				
				
				
				timeMaxMin2FillDistBEJT = System.currentTimeMillis()
						- timeMaxMin2FillDistBEJT;
				metrics.get("avgMaxMin2FillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMin2FillDistBEJT);
				int twMaxMin2FillDistBE = jtMaxMin2DF.width();
				metrics.get("avgMaxMin2FillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMin2FillDistJTwidth")
								.get(nbVertices)) + twMaxMin2FillDistBE);
				System.out.println(" jointree par BE Max-Min-Fill2 Dist width "
						+ jtMaxMin2DF.width() + " time "
						+ timeMaxMin2FillDistBEJT);

				 ArrayList<ArrayList<Integer>> bucketsBFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherBFS = new Integer[BFSorder.size()];
				 long timeBFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtBFS =
				 HyperGraphs.bucketElimination(dhg,
				 BFSorder, bucketsBFS, varFatherBFS);
				 timeBFSBEJT = System.currentTimeMillis() - timeBFSBEJT;
				 metrics.get("avgBFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))+
				 timeBFSBEJT);
				 int twBFSBE = jtBFS.width();
				 metrics.get("avgBFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))+
				 twBFSBE);
				 System.out.println(" jointree par BE BFS width " +
				 jtBFS.width()+" time "+timeBFSBEJT);
			}

			// metrics.get("avgMinFillTimeOrder").put(nbVertices,
			// ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMinFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMinFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin1FillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))/nbInstances);

			// metrics.get("avgMinFillTimeBEJT").put(nbVertices,
			// ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMinFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))/nbInstances);

			// metrics.get("avgMinFillTime").put(nbVertices,
			// (Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices)+
			// (Long)metrics.get("avgMinFillTimeOrder").get(nbVertices));
			metrics.get("avgMaxFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxFillDistTimeBEJT")
							.get(nbVertices)
							+ (Long) metrics.get("avgMaxFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMinFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMinFillDistTimeBEJT")
							.get(nbVertices)
							+ (Long) metrics.get("avgMinFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMinFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMinFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMin1FillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMin1FillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMin2FillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMin2FillDistTimeOrder")
									.get(nbVertices));
			 metrics.get("avgBFSTime").put(nbVertices,
			 (Long)metrics.get("avgBFSTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgBFSTimeOrder").get(nbVertices));

			// metrics.get("avgMinFillJTwidth").put(nbVertices,
			// ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMinFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMinFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMin1FillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMin2FillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))/nbInstances);
		}

		File dirOut = new File("C:/Users/VinTo/workspace/expe120411");
		if (!dirOut.exists())
			dirOut.mkdirs();

		HashMap<String, HashMap<String, HashMap<Integer, Number>>> dataPlots = new HashMap<String, HashMap<String, HashMap<Integer, Number>>>();

		dataPlots.put("avgTimeOrder",
				new HashMap<String, HashMap<Integer, Number>>());
		// dataPlots.get("avgTimeOrder").put("avgMinFillTimeOrder",
		// metrics.get("avgMinFillTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMaxFillDistTimeOrder",
				metrics.get("avgMaxFillDistTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMinFillDistTimeOrder",
				metrics.get("avgMinFillDistTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMaxMinFillDistTimeOrder",
				metrics.get("avgMaxMinFillDistTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMaxMin1FillDistTimeOrder",
				metrics.get("avgMaxMin1FillDistTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMaxMin2FillDistTimeOrder",
				metrics.get("avgMaxMin2FillDistTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgBFSTimeOrder",
		 metrics.get("avgBFSTimeOrder"));

		dataPlots.put("avgTimeBEJT",
				new HashMap<String, HashMap<Integer, Number>>());
		// dataPlots.get("avgTimeBEJT").put("avgMinFillTimeBEJT",
		// metrics.get("avgMinFillTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMaxFillDistTimeBEJT",
				metrics.get("avgMaxFillDistTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMinFillDistTimeBEJT",
				metrics.get("avgMinFillDistTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMaxMinFillDistTimeBEJT",
				metrics.get("avgMaxMinFillDistTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMaxMin1FillDistTimeBEJT",
				metrics.get("avgMaxMin1FillDistTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMaxMin2FillDistTimeBEJT",
				metrics.get("avgMaxMin2FillDistTimeBEJT"));
		 dataPlots.get("avgTimeBEJT").put("avgBFSTimeBEJT",
		 metrics.get("avgBFSTimeBEJT"));

		dataPlots.put("avgTime",
				new HashMap<String, HashMap<Integer, Number>>());
		// dataPlots.get("avgTime").put("avgMinFillTime",
		// metrics.get("avgMinFillTime"));
		dataPlots.get("avgTime").put("avgMaxFillDistTime",
				metrics.get("avgMaxFillDistTime"));
		dataPlots.get("avgTime").put("avgMinFillDistTime",
				metrics.get("avgMinFillDistTime"));
		dataPlots.get("avgTime").put("avgMaxMinFillDistTime",
				metrics.get("avgMaxMinFillDistTime"));
		dataPlots.get("avgTime").put("avgMaxMin1FillDistTime",
				metrics.get("avgMaxMin1FillDistTime"));
		dataPlots.get("avgTime").put("avgMaxMin2FillDistTime",
				metrics.get("avgMaxMin2FillDistTime"));
		 dataPlots.get("avgTime").put("avgBFSTime",
		 metrics.get("avgBFSTime"));

		dataPlots.put("avgJTWidth",
				new HashMap<String, HashMap<Integer, Number>>());
		// dataPlots.get("avgJTWidth").put("avgMinFillJTwidth",
		// metrics.get("avgMinFillJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMaxFillDistJTwidth",
				metrics.get("avgMaxFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMinFillDistJTwidth",
				metrics.get("avgMinFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMaxMinFillDistJTwidth",
				metrics.get("avgMaxMinFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMaxMin1FillDistJTwidth",
				metrics.get("avgMaxMin1FillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMaxMin2FillDistJTwidth",
				metrics.get("avgMaxMin2FillDistJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgBFSJTwidth",
		 metrics.get("avgBFSJTwidth"));

		String extraTitle = " BAGraph " + "step: " + stepNbVertices
				+ "  nbInstances " + nbInstances;
		for (String plotName : dataPlots.keySet()) {
			File dirPlot = new File(dirOut.getPath() + File.separator
					+ plotName);
			if (dirPlot.exists())
				FileTools.recursiveDelete(dirPlot);
			dirPlot.mkdirs();
			HashMap<String, File> curveFiles = saveMetricsInFiles(
					dataPlots.get(plotName), dirPlot);
			writeDefault2DLinesPoints("nbVertices", plotName, curveFiles,
					dirPlot, extraTitle);
		}

	}
	
	
	
	
	
	public static void testRandom2(HashMap<String,Object> params) throws Exception{
		// random instance
		
		String dirOutName = (String)params.get("dirOutName");
		String graphModel = (String) params.get("graphModel");
		int nbInstances = (Integer) params.get("nbInstances");
		int nbVerticesMax = (Integer) params.get("nbVerticesMax");
		
		
		
		
		HashMap<String,HashMap<String,Object>> rGraphParams = 
			new HashMap<String, HashMap<String,Object>>();
		
		if(graphModel.equals("BA")){
			rGraphParams.put("BAGraph", new HashMap<String,Object>());
			rGraphParams.get("BAGraph").put("randomGraph", "BA");
		}
		
		if(graphModel.equals("WS")){
			rGraphParams.put("WSGraph", new HashMap<String,Object>());
			rGraphParams.get("WSGraph").put("randomGraph", "WS");
		}
		
		if(graphModel.equals("UD")){
			rGraphParams.put("UDGraph", new HashMap<String,Object>());
			rGraphParams.get("UDGraph").put("randomGraph", "UD");
		}
		
		for(String k : rGraphParams.keySet()){
		
		HashMap<String, HashMap<Integer, Number>> metrics = new HashMap<String, HashMap<Integer, Number>>();
		
		metrics.put("avgMinFillTimeOrder", new HashMap<Integer,Number>());
		metrics.put("avgMaxMin2FillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgBFSTimeOrder", new HashMap<Integer,Number>());

		metrics.put("avgMinFillTimeBEJT", new HashMap<Integer,Number>());
		metrics.put("avgMaxMin2FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		metrics.put("avgBFSTimeBEJT", new HashMap<Integer,Number>());

		metrics.put("avgMinFillTime", new HashMap<Integer,Number>());
		metrics.put("avgMaxMin2FillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgBFSTime", new HashMap<Integer,Number>());

		metrics.put("avgMinFillJTwidth", new HashMap<Integer,Number>());
		metrics.put("avgMaxMin2FillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgBFSJTwidth", new HashMap<Integer,Number>());
		


		
		int stepNbVertices = 20;
		
		for (int nbVertices = 20; nbVertices <= nbVerticesMax; nbVertices += stepNbVertices) {

			System.out.println();
			System.out.println("nbVertices " + nbVertices);
			System.out.println();
			
			metrics.get("avgMinFillTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeOrder").put(nbVertices,(long) 0.0);

			metrics.get("avgMinFillTimeBEJT").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeBEJT").put(nbVertices,(long) 0.0);

			metrics.get("avgMinFillJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMin2FillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgBFSJTwidth").put(nbVertices,0);

			for (int numInstance = 0; numInstance < nbInstances; numInstance++) {

				rGraphParams.get(k).put("nbPeers", "" + nbVertices);
				HyperGraph<Integer> dhg = genRandomGraph(rGraphParams.get(k));
				// System.out.println(" Le graphe " + dhg);

				 long timeMinFillorder = System.currentTimeMillis();
				 ArrayList<Integer> minFillorder = HyperGraphs
				 .minFillInvOrder(dhg);
				 timeMinFillorder = System.currentTimeMillis() -
				 timeMinFillorder;
				 metrics.get("avgMinFillTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))+
				 timeMinFillorder);
				// // System.out.println(" minFillorder  " + minFillorder);
				 System.out.println("timeMinFillorder "+ timeMinFillorder);

				
				long timeMaxMin2FillDistorder = System.currentTimeMillis();
//				ArrayList<Integer> maxMin2FillDistorder = minMaxFillDistOrder(
//						"maxmin2", dhg);
				timeMaxMin2FillDistorder = System.currentTimeMillis()
						- timeMaxMin2FillDistorder;
				metrics.get("avgMaxMin2FillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMin2FillDistorder);
				System.out.println("timeMaxMin2FillDistorder "
						+ timeMaxMin2FillDistorder);

				 long timeBFSorder = System.currentTimeMillis();
				 // take care to remove dFS -> BFS
				 ArrayList<Integer> BFSorder = dFSOrder(dhg);
				 timeBFSorder = System.currentTimeMillis() - timeBFSorder;
				 metrics.get("avgBFSTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))+
				 timeBFSorder);
				 System.out.println("timeBFSorder "+ timeBFSorder);

				// Built Jointree by BE with the Order and

				 ArrayList<ArrayList<Integer>> bucketsMinF = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherMinF = new Integer[minFillorder.size()];
				 long timeMinFillBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtMinF =
				 HyperGraphs.bucketElimination(dhg,
				 minFillorder, bucketsMinF, varFatherMinF);
				 timeMinFillBEJT = System.currentTimeMillis() -
				 timeMinFillBEJT;
				 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))+
				 timeMinFillBEJT);
				 int twMinFillBE = jtMinF.width();
				 metrics.get("avgMinFillJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))+
				 twMinFillBE);
				 System.out.println(" jointree par BE Min-Fill witdh " +
				 jtMinF.width()+" time "+timeMinFillBEJT);

				
				
				long timeMaxMin2FillDistBEJT = System.currentTimeMillis();
				
				HyperGraph<AbstCompArrayList<Integer>> dualHjt = new HyperGraph<AbstCompArrayList<Integer>>();
				HyperGraph<Integer> jtMaxMin2DF = DistribTreeDecomp.distribJT5(dhg, dualHjt, 0);
				
				timeMaxMin2FillDistBEJT = System.currentTimeMillis()
						- timeMaxMin2FillDistBEJT;
				metrics.get("avgMaxMin2FillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMin2FillDistBEJT);
				int twMaxMin2FillDistBE = jtMaxMin2DF.width();
				metrics.get("avgMaxMin2FillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMin2FillDistJTwidth")
								.get(nbVertices)) + twMaxMin2FillDistBE);
				System.out.println(" jointree par BE Max-Min-Fill2 Dist width "
						+ jtMaxMin2DF.width() + " time "
						+ timeMaxMin2FillDistBEJT);

				 ArrayList<ArrayList<Integer>> bucketsBFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherBFS = new Integer[BFSorder.size()];
				 long timeBFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtBFS =
				 HyperGraphs.bucketElimination(dhg,
				 BFSorder, bucketsBFS, varFatherBFS);
				 timeBFSBEJT = System.currentTimeMillis() - timeBFSBEJT;
				 metrics.get("avgBFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))+
				 timeBFSBEJT);
				 int twBFSBE = jtBFS.width();
				 metrics.get("avgBFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))+
				 twBFSBE);
				 System.out.println(" jointree par BE BFS width " +
				 jtBFS.width()+" time "+timeBFSBEJT);
			}

			 metrics.get("avgMinFillTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))/nbInstances);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))/nbInstances);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTime").put(nbVertices,
			 (Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgMinFillTimeOrder").get(nbVertices));
			metrics.get("avgMaxMin2FillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMin2FillDistTimeOrder")
									.get(nbVertices));
			 metrics.get("avgBFSTime").put(nbVertices,
			 (Long)metrics.get("avgBFSTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgBFSTimeOrder").get(nbVertices));

			 metrics.get("avgMinFillJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))/nbInstances);
			metrics.get("avgMaxMin2FillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMin2FillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))/nbInstances);
		}

		File dirOut = new File(dirOutName);
		if (!dirOut.exists())
			dirOut.mkdirs();

		HashMap<String, HashMap<String, HashMap<Integer, Number>>> dataPlots = new HashMap<String, HashMap<String, HashMap<Integer, Number>>>();

		dataPlots.put("avgTimeOrder",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTimeOrder").put("avgMinFillTimeOrder",
		 metrics.get("avgMinFillTimeOrder"));
		dataPlots.get("avgTimeOrder").put("avgMaxMin2FillDistTimeOrder",
				metrics.get("avgMaxMin2FillDistTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgBFSTimeOrder",
		 metrics.get("avgBFSTimeOrder"));

		dataPlots.put("avgTimeBEJT",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTimeBEJT").put("avgMinFillTimeBEJT",
		 metrics.get("avgMinFillTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgMaxMin2FillDistTimeBEJT",
				metrics.get("avgMaxMin2FillDistTimeBEJT"));
		 dataPlots.get("avgTimeBEJT").put("avgBFSTimeBEJT",
		 metrics.get("avgBFSTimeBEJT"));

		dataPlots.put("avgTime",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTime").put("avgMinFillTime",
		 metrics.get("avgMinFillTime"));
		dataPlots.get("avgTime").put("avgMaxMin2FillDistTime",
				metrics.get("avgMaxMin2FillDistTime"));
		 dataPlots.get("avgTime").put("avgBFSTime",
		 metrics.get("avgBFSTime"));

		dataPlots.put("avgJTWidth",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgJTWidth").put("avgMinFillJTwidth",
		 metrics.get("avgMinFillJTwidth"));
		dataPlots.get("avgJTWidth").put("avgMaxMin2FillDistJTwidth",
				metrics.get("avgMaxMin2FillDistJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgBFSJTwidth",
		 metrics.get("avgBFSJTwidth"));

		String extraTitle = k + " step: " + stepNbVertices
				+ "  nbInstances " + nbInstances;
		for (String plotName : dataPlots.keySet()) {
			File dirPlot = new File(dirOut.getPath() + File.separator
					+k+File.separator+ plotName);
			if (dirPlot.exists())
				FileTools.recursiveDelete(dirPlot);
			dirPlot.mkdirs();
			HashMap<String, File> curveFiles = saveMetricsInFiles(
					dataPlots.get(plotName), dirPlot);
			writeDefault2DLinesPoints("nbVertices", plotName, curveFiles,
					dirPlot, extraTitle);
		}
		}

	}
	

	public static void testRandom3(HashMap<String,Object> params) throws Exception{
		// random instance
		
		String dirOutName = (String)params.get("dirOutName");
		String graphModel = (String) params.get("graphModel");
		int nbInstances = (Integer) params.get("nbInstances");
		int nbVerticesMax = (Integer) params.get("nbVerticesMax");
		
		
		
		
		HashMap<String,HashMap<String,Object>> rGraphParams = 
			new HashMap<String, HashMap<String,Object>>();
		
		if(graphModel.equals("BA")){
			rGraphParams.put("BAGraph", new HashMap<String,Object>());
			rGraphParams.get("BAGraph").put("randomGraph", "BA");
		}
		
		if(graphModel.equals("WS")){
			rGraphParams.put("WSGraph", new HashMap<String,Object>());
			rGraphParams.get("WSGraph").put("randomGraph", "WS");
		}
		
		if(graphModel.equals("UD")){
			rGraphParams.put("UDGraph", new HashMap<String,Object>());
			rGraphParams.get("UDGraph").put("randomGraph", "UD");
		}
		
		for(String k : rGraphParams.keySet()){
		
		HashMap<String, HashMap<Integer, Number>> metrics = new HashMap<String, HashMap<Integer, Number>>();
		
		metrics.put("avgMinFillTimeOrder", new HashMap<Integer,Number>());
		metrics.put("avgDFSTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgBFSTimeOrder", new HashMap<Integer,Number>());

		metrics.put("avgMinFillTimeBEJT", new HashMap<Integer,Number>());
		metrics.put("avgDFSTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgBFSTimeBEJT", new HashMap<Integer,Number>());

		metrics.put("avgMinFillTime", new HashMap<Integer,Number>());
		metrics.put("avgDFSTime", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncTime", new HashMap<Integer, Number>());
		metrics.put("avgBFSTime", new HashMap<Integer,Number>());

		metrics.put("avgMinFillJTwidth", new HashMap<Integer,Number>());
		metrics.put("avgDFSJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgBFSJTwidth", new HashMap<Integer,Number>());
		


		
		int stepNbVertices = 100;
		
		for (int nbVertices = 20; nbVertices <= nbVerticesMax; nbVertices += stepNbVertices) {

			System.out.println();
			System.out.println("nbVertices " + nbVertices);
			System.out.println();
			
			metrics.get("avgMinFillTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgDFSTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgDFSMaxAncTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgBFSTimeOrder").put(nbVertices,(long) 0.0);

			metrics.get("avgMinFillTimeBEJT").put(nbVertices,(long) 0.0);
			metrics.get("avgDFSTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgDFSMaxAncTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgBFSTimeBEJT").put(nbVertices,(long) 0.0);

			metrics.get("avgMinFillJTwidth").put(nbVertices, 0);
			metrics.get("avgDFSJTwidth").put(nbVertices, 0);
			metrics.get("avgDFSMaxAncJTwidth").put(nbVertices, 0);
			metrics.get("avgBFSJTwidth").put(nbVertices,0);

			for (int numInstance = 0; numInstance < nbInstances; numInstance++) {

				rGraphParams.get(k).put("nbPeers", "" + nbVertices);
				HyperGraph<Integer> dhg = genRandomGraph(rGraphParams.get(k));
				// System.out.println(" Le graphe " + dhg);

				 long timeMinFillorder = System.currentTimeMillis();
				 ArrayList<Integer> minFillorder = HyperGraphs
				 .minFillInvOrder(dhg);
				 timeMinFillorder = System.currentTimeMillis() -
				 timeMinFillorder;
				 metrics.get("avgMinFillTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))+
				 timeMinFillorder);
				// // System.out.println(" minFillorder  " + minFillorder);
				 System.out.println("timeMinFillorder "+ timeMinFillorder);

				
				long timeDFSMaxAncOrder = System.currentTimeMillis();
					ArrayList<Integer> DFSMaxAncorder = dFSOrderAssynEO(dhg);
					timeDFSMaxAncOrder = System.currentTimeMillis()
							- timeDFSMaxAncOrder;
					metrics.get("avgDFSMaxAncTimeOrder").put(nbVertices,
					((Long) metrics.get("avgDFSMaxAncTimeOrder").get(nbVertices)) 
					+ timeDFSMaxAncOrder);
					System.out.println("timeDFSMaxAncorder "	+ timeDFSMaxAncOrder);

				 
				 
				long timeDFSorder = System.currentTimeMillis();
				ArrayList<Integer> DFSorder = dFSOrder(dhg);
				timeDFSorder = System.currentTimeMillis()
						- timeDFSorder;
				metrics.get("avgDFSTimeOrder").put(nbVertices,
				((Long) metrics.get("avgDFSTimeOrder").get(nbVertices)) 
				+ timeDFSorder);
				System.out.println("timeDFSorder "	+ timeDFSorder);

				
				
				
				 long timeBFSorder = System.currentTimeMillis();
				 ArrayList<Integer> BFSorder = bFSOrder(dhg);
				 timeBFSorder = System.currentTimeMillis() - timeBFSorder;
				 metrics.get("avgBFSTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))+
				 timeBFSorder);
				 System.out.println("timeBFSorder "+ timeBFSorder);

				
				 
				 // Built Jointree by BE with the Order and

				 ArrayList<ArrayList<Integer>> bucketsMinF = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherMinF = new Integer[minFillorder.size()];
				 
				 long timeMinFillBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtMinF =
				 HyperGraphs.bucketElimination(dhg,
				 minFillorder, bucketsMinF, varFatherMinF);
				 timeMinFillBEJT = System.currentTimeMillis() -
				 timeMinFillBEJT;
				 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))+
				 timeMinFillBEJT);
				 int twMinFillBE = jtMinF.width();
				 metrics.get("avgMinFillJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))+
				 twMinFillBE);
				 System.out.println(" jointree par BE Min-Fill witdh " +
				 jtMinF.width()+" time "+timeMinFillBEJT);

				 
				 long timeDFSMaxAncBEJT = System.currentTimeMillis();
				 // Token EO
				 HyperGraph<Integer> jtDFSMaxAnc =
					tokenEO(dhg,new HyperGraph<AbstCompArrayList<Integer>> (),"MinProj",true);
				 // Assyn EO
//				 ArrayList<ArrayList<Integer>> bucketsDFSMaxAnc = new
//				 ArrayList<ArrayList<Integer>>();
//				 Integer[] varFatherDFSMaxAnc = new Integer[DFSMaxAncorder.size()];
//				 HashMap<Integer,ArrayList<Integer>> childrenOf = new HashMap<Integer,ArrayList<Integer>>();

//				 HyperGraph<Integer> jtDFSMaxAnc =
//					 AssynEO(dhg, bucketsDFSMaxAnc, childrenOf);
//				 HyperGraph<Integer> jtDFSMaxAnc =
//					 AssynEO(dhg, bucketsDFSMaxAnc, childrenOf);

				// DSfAssynEO
//					 dFSOrderAssynEO(g)(dhg);
//				 HyperGraphs.bucketElimination(dhg,
//				 DFSMaxAncorder, bucketsDFSMaxAnc, varFatherDFSMaxAnc);			 
//				 HyperGraphs.localRefinement(dhg, DFSMaxAncorder, bucketsDFSMaxAnc,varFatherDFSMaxAnc, childrenOf);
				 
				 
				 timeDFSMaxAncBEJT = System.currentTimeMillis() - timeDFSMaxAncBEJT;
				 metrics.get("avgDFSMaxAncTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices))+
				 timeDFSMaxAncBEJT);
				 int twDFSMaxAncBE = jtDFSMaxAnc.width();
				 metrics.get("avgDFSMaxAncJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgDFSMaxAncJTwidth").get(nbVertices))+
				 twDFSMaxAncBE);
				 System.out.println(" jointree par BE DFSMaxAnc width " +
				 jtDFSMaxAnc.width()+" time "+timeDFSMaxAncBEJT);
				
				ArrayList<ArrayList<Integer>> bucketsDFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherDFS = new Integer[DFSorder.size()];
				 long timeDFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtDFS =
				 HyperGraphs.bucketElimination(dhg,
				 DFSorder, bucketsDFS, varFatherDFS);
				 timeDFSBEJT = System.currentTimeMillis() - timeDFSBEJT;
				 metrics.get("avgDFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgDFSTimeBEJT").get(nbVertices))+
				 timeDFSBEJT);
				 int twDFSBE = jtDFS.width();
				 metrics.get("avgDFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgDFSJTwidth").get(nbVertices))+
				 twDFSBE);
				 System.out.println(" jointree par BE DFS width " +
				 jtDFS.width()+" time "+timeDFSBEJT);
				 
				 
//				 ArrayList<ArrayList<Integer>> bucketsDFSMaxAnc = new
//				 ArrayList<ArrayList<Integer>>();
//				 Integer[] varFatherDFSMaxAnc = new Integer[DFSMaxAncorder.size()];
//				 long timeDFSMaxAncBEJT = System.currentTimeMillis();
//				 HashMap<Integer,ArrayList<Integer>> childrenOf = new HashMap<Integer,ArrayList<Integer>>();
//				 HyperGraph<Integer> jtDFSMaxAnc =
//				 HyperGraphs.localRefinement(dhg,  DFSorder, bucketsDFS, varFatherDFS, childrenOf);
//				 timeDFSMaxAncBEJT = System.currentTimeMillis() - timeDFSMaxAncBEJT;
//				 metrics.get("avgDFSMaxAncTimeBEJT").put(nbVertices,
//				 ((Long)metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices))+
//				 timeDFSMaxAncBEJT);
//				 int twDFSMaxAncBE = jtDFSMaxAnc.width();
//				 metrics.get("avgDFSMaxAncJTwidth").put(nbVertices,
//				 ((Integer)metrics.get("avgDFSMaxAncJTwidth").get(nbVertices))+
//				 twDFSMaxAncBE);
//				 System.out.println(" jointree par BE DFSMaxAnc width " +
//				 jtDFSMaxAnc.width()+" time "+timeDFSMaxAncBEJT);
				 
				 

				 ArrayList<ArrayList<Integer>> bucketsBFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherBFS = new Integer[BFSorder.size()];
				 long timeBFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtBFS =
				 HyperGraphs.bucketElimination(dhg,
				 BFSorder, bucketsBFS, varFatherBFS);
				 timeBFSBEJT = System.currentTimeMillis() - timeBFSBEJT;
				 metrics.get("avgBFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))+
				 timeBFSBEJT);
				 int twBFSBE = jtBFS.width();
				 metrics.get("avgBFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))+
				 twBFSBE);
				 System.out.println(" jointree par BE BFS width " +
				 jtBFS.width()+" time "+timeBFSBEJT);
			}

			 metrics.get("avgMinFillTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))/nbInstances);
			 metrics.get("avgDFSTimeOrder").put(	nbVertices,
						((Long) metrics.get("avgDFSMaxAncTimeOrder").get(	nbVertices))/ nbInstances);
			metrics.get("avgDFSMaxAncTimeOrder").put(	nbVertices,
			((Long) metrics.get("avgDFSTimeOrder").get(	nbVertices))/ nbInstances);
			 metrics.get("avgBFSTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))/nbInstances);
			 metrics.get("avgDFSTimeBEJT").put(nbVertices,
						((Long) metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices))/ nbInstances);
			 metrics.get("avgDFSTimeBEJT").put(nbVertices,
			((Long) metrics.get("avgDFSTimeBEJT").get(nbVertices))/ nbInstances);
			 metrics.get("avgBFSTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTime").put(nbVertices,
			 (Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgMinFillTimeOrder").get(nbVertices));
			 metrics.get("avgDFSMaxAncTime").put(nbVertices,
			(Long) metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices)+
			(Long) metrics.get("avgDFSMaxAncTimeOrder").get(nbVertices));
			 metrics.get("avgDFSMaxAncTime").put(nbVertices,
			(Long) metrics.get("avgDFSTimeBEJT").get(nbVertices)+
			(Long) metrics.get("avgDFSTimeOrder").get(nbVertices));
			 metrics.get("avgBFSTime").put(nbVertices,
			 (Long)metrics.get("avgBFSTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgBFSTimeOrder").get(nbVertices));

			 metrics.get("avgMinFillJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))/nbInstances);
			 metrics.get("avgDFSMaxAncJTwidth").put(nbVertices,
						((Integer) metrics.get("avgDFSMaxAncJTwidth").get(nbVertices))/ nbInstances);
			 metrics.get("avgDFSJTwidth").put(nbVertices,
			((Integer) metrics.get("avgDFSJTwidth").get(nbVertices))/ nbInstances);
			 metrics.get("avgBFSJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))/nbInstances);
		}

		File dirOut = new File(dirOutName);
		if (!dirOut.exists())
			dirOut.mkdirs();

		HashMap<String, HashMap<String, HashMap<Integer, Number>>> dataPlots = new HashMap<String, HashMap<String, HashMap<Integer, Number>>>();

		dataPlots.put("avgTimeOrder",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTimeOrder").put("avgMinFillTimeOrder",
		 metrics.get("avgMinFillTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgDFSMaxAncTimeOrder",
				metrics.get("avgDFSMaxAncTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgDFSTimeOrder",
				metrics.get("avgDFSTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgBFSTimeOrder",
		 metrics.get("avgBFSTimeOrder"));

		dataPlots.put("avgTimeBEJT",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTimeBEJT").put("avgMinFillTimeBEJT",
		 metrics.get("avgMinFillTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgDFSMaxAncTimeBEJT",
					metrics.get("avgDFSMaxAncTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgDFSTimeBEJT",
				metrics.get("avgDFSTimeBEJT"));
		 dataPlots.get("avgTimeBEJT").put("avgBFSTimeBEJT",
		 metrics.get("avgBFSTimeBEJT"));

		dataPlots.put("avgTime",
				new HashMap<String, HashMap<Integer, Number>>());
		dataPlots.get("avgTime").put("avgMinFillTime",
		 metrics.get("avgMinFillTime"));
		dataPlots.get("avgTime").put("avgDFSMaxAncTime",
				metrics.get("avgDFSMaxAncTime"));
		dataPlots.get("avgTime").put("avgDFSTime",
				metrics.get("avgDFSTime"));
		 dataPlots.get("avgTime").put("avgBFSTime",
		 metrics.get("avgBFSTime"));

		dataPlots.put("avgJTWidth",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgJTWidth").put("avgMinFillJTwidth",
		 metrics.get("avgMinFillJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgTokenEO",
					metrics.get("avgDFSMaxAncJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgDFSJTwidth",
				metrics.get("avgDFSJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgBFSJTwidth",
		 metrics.get("avgBFSJTwidth"));

		String extraTitle = k + " step: " + stepNbVertices
				+ "  nbInstances " + nbInstances;
		for (String plotName : dataPlots.keySet()) {
			File dirPlot = new File(dirOut.getPath() + File.separator
					+k+File.separator+ plotName);
			if (dirPlot.exists())
				FileTools.recursiveDelete(dirPlot);
			dirPlot.mkdirs();
			HashMap<String, File> curveFiles = saveMetricsInFiles(
					dataPlots.get(plotName), dirPlot);
			writeDefault2DLinesPoints("nbVertices", plotName, curveFiles,
					dirPlot, extraTitle);
		}
		}

	}
	
	
	
	public static void testRandomToKenEOMinNeigh(HashMap<String,Object> params) throws Exception{
		// random instance
		
		String dirOutName = (String)params.get("dirOutName");
		String graphModel = (String) params.get("graphModel");
		int nbInstances = (Integer) params.get("nbInstances");
		int nbVerticesMax = (Integer) params.get("nbVerticesMax");
		
		
		
		HashMap<String,HashMap<String,Object>> rGraphParams = 
			new HashMap<String, HashMap<String,Object>>();
		
		if(graphModel.equals("BA")){
			rGraphParams.put("BAGraph", new HashMap<String,Object>());
			rGraphParams.get("BAGraph").put("randomGraph", "BA");
		}
		
		if(graphModel.equals("WS")){
			rGraphParams.put("WSGraph", new HashMap<String,Object>());
			rGraphParams.get("WSGraph").put("randomGraph", "WS");
		}
		
		if(graphModel.equals("UD")){
			rGraphParams.put("UDGraph", new HashMap<String,Object>());
			rGraphParams.get("UDGraph").put("randomGraph", "UD");
		}
		
		for(String k : rGraphParams.keySet()){
		
		HashMap<String, HashMap<Integer, Number>> metrics = new HashMap<String, HashMap<Integer, Number>>();
		
		metrics.put("avgMinFillTimeOrder", new HashMap<Integer,Number>());
		metrics.put("avgDFSTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgBFSTimeOrder", new HashMap<Integer,Number>());

		metrics.put("avgMinFillTimeBEJT", new HashMap<Integer,Number>());
		metrics.put("avgDFSTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgBFSTimeBEJT", new HashMap<Integer,Number>());

		metrics.put("avgMinFillTime", new HashMap<Integer,Number>());
		metrics.put("avgDFSTime", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncTime", new HashMap<Integer, Number>());
		metrics.put("avgBFSTime", new HashMap<Integer,Number>());

		metrics.put("avgMinFillJTwidth", new HashMap<Integer,Number>());
		metrics.put("avgDFSJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgBFSJTwidth", new HashMap<Integer,Number>());
		


		
		int stepNbVertices = 100;
		
		for (int nbVertices = 20; nbVertices <= nbVerticesMax; nbVertices += stepNbVertices) {

			System.out.println();
			System.out.println("nbVertices " + nbVertices);
			System.out.println();
			
			metrics.get("avgMinFillTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgDFSTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgDFSMaxAncTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgBFSTimeOrder").put(nbVertices,(long) 0.0);

			metrics.get("avgMinFillTimeBEJT").put(nbVertices,(long) 0.0);
			metrics.get("avgDFSTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgDFSMaxAncTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgBFSTimeBEJT").put(nbVertices,(long) 0.0);

			metrics.get("avgMinFillJTwidth").put(nbVertices, 0);
			metrics.get("avgDFSJTwidth").put(nbVertices, 0);
			metrics.get("avgDFSMaxAncJTwidth").put(nbVertices, 0);
			metrics.get("avgBFSJTwidth").put(nbVertices,0);

			for (int numInstance = 0; numInstance < nbInstances; numInstance++) {

				rGraphParams.get(k).put("nbPeers", "" + nbVertices);
				HyperGraph<Integer> dhg = genRandomGraph(rGraphParams.get(k));
				// System.out.println(" Le graphe " + dhg);

				 long timeMinFillorder = System.currentTimeMillis();
				 ArrayList<Integer> minFillorder = HyperGraphs
				 .minFillInvOrder(dhg);
				 timeMinFillorder = System.currentTimeMillis() -
				 timeMinFillorder;
				 metrics.get("avgMinFillTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))+
				 timeMinFillorder);
				// // System.out.println(" minFillorder  " + minFillorder);
				 System.out.println("timeMinFillorder "+ timeMinFillorder);

				
				long timeDFSMaxAncOrder = System.currentTimeMillis();
					ArrayList<Integer> DFSMaxAncorder = dFSOrderAssynEO(dhg);
					timeDFSMaxAncOrder = System.currentTimeMillis()
							- timeDFSMaxAncOrder;
					metrics.get("avgDFSMaxAncTimeOrder").put(nbVertices,
					((Long) metrics.get("avgDFSMaxAncTimeOrder").get(nbVertices)) 
					+ timeDFSMaxAncOrder);
					System.out.println("timeDFSMaxAncorder "	+ timeDFSMaxAncOrder);

				 
				 
				long timeDFSorder = System.currentTimeMillis();
				ArrayList<Integer> DFSorder = dFSOrder(dhg);
				timeDFSorder = System.currentTimeMillis()
						- timeDFSorder;
				metrics.get("avgDFSTimeOrder").put(nbVertices,
				((Long) metrics.get("avgDFSTimeOrder").get(nbVertices)) 
				+ timeDFSorder);
				System.out.println("timeDFSorder "	+ timeDFSorder);

				
				
				
				 long timeBFSorder = System.currentTimeMillis();
				 ArrayList<Integer> BFSorder = bFSOrder(dhg);
				 timeBFSorder = System.currentTimeMillis() - timeBFSorder;
				 metrics.get("avgBFSTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))+
				 timeBFSorder);
				 System.out.println("timeBFSorder "+ timeBFSorder);

				
				 
				 // Built Jointree by BE with the Order and

				 ArrayList<ArrayList<Integer>> bucketsMinF = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherMinF = new Integer[minFillorder.size()];
				 
				 long timeMinFillBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtMinF =
				 HyperGraphs.bucketElimination(dhg,
				 minFillorder, bucketsMinF, varFatherMinF);
				 timeMinFillBEJT = System.currentTimeMillis() -
				 timeMinFillBEJT;
				 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))+
				 timeMinFillBEJT);
				 int twMinFillBE = jtMinF.width();
				 metrics.get("avgMinFillJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))+
				 twMinFillBE);
				 System.out.println(" jointree par BE Min-Fill witdh " +
				 jtMinF.width()+" time "+timeMinFillBEJT);

				 
				 long timeDFSMaxAncBEJT = System.currentTimeMillis();
				 // Token EO
				 HyperGraph<Integer> jtDFSMaxAnc =
					tokenEO(dhg,new HyperGraph<AbstCompArrayList<Integer>> (), "MinProj", true);
				 
				 
				 timeDFSMaxAncBEJT = System.currentTimeMillis() - timeDFSMaxAncBEJT;
				 metrics.get("avgDFSMaxAncTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices))+
				 timeDFSMaxAncBEJT);
				 int twDFSMaxAncBE = jtDFSMaxAnc.width();
				 metrics.get("avgDFSMaxAncJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgDFSMaxAncJTwidth").get(nbVertices))+
				 twDFSMaxAncBE);
				 System.out.println(" jointree par BE DFSMaxAnc width " +
				 jtDFSMaxAnc.width()+" time "+timeDFSMaxAncBEJT);
				
				ArrayList<ArrayList<Integer>> bucketsDFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherDFS = new Integer[DFSorder.size()];
				 long timeDFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtDFS =
				 HyperGraphs.bucketElimination(dhg,
				 DFSorder, bucketsDFS, varFatherDFS);
				 timeDFSBEJT = System.currentTimeMillis() - timeDFSBEJT;
				 metrics.get("avgDFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgDFSTimeBEJT").get(nbVertices))+
				 timeDFSBEJT);
				 int twDFSBE = jtDFS.width();
				 metrics.get("avgDFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgDFSJTwidth").get(nbVertices))+
				 twDFSBE);
				 System.out.println(" jointree par BE DFS width " +
				 jtDFS.width()+" time "+timeDFSBEJT);
				 
				 
//				 ArrayList<ArrayList<Integer>> bucketsDFSMaxAnc = new
//				 ArrayList<ArrayList<Integer>>();
//				 Integer[] varFatherDFSMaxAnc = new Integer[DFSMaxAncorder.size()];
//				 long timeDFSMaxAncBEJT = System.currentTimeMillis();
//				 HashMap<Integer,ArrayList<Integer>> childrenOf = new HashMap<Integer,ArrayList<Integer>>();
//				 HyperGraph<Integer> jtDFSMaxAnc =
//				 HyperGraphs.localRefinement(dhg,  DFSorder, bucketsDFS, varFatherDFS, childrenOf);
//				 timeDFSMaxAncBEJT = System.currentTimeMillis() - timeDFSMaxAncBEJT;
//				 metrics.get("avgDFSMaxAncTimeBEJT").put(nbVertices,
//				 ((Long)metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices))+
//				 timeDFSMaxAncBEJT);
//				 int twDFSMaxAncBE = jtDFSMaxAnc.width();
//				 metrics.get("avgDFSMaxAncJTwidth").put(nbVertices,
//				 ((Integer)metrics.get("avgDFSMaxAncJTwidth").get(nbVertices))+
//				 twDFSMaxAncBE);
//				 System.out.println(" jointree par BE DFSMaxAnc width " +
//				 jtDFSMaxAnc.width()+" time "+timeDFSMaxAncBEJT);
				 
				 

				 ArrayList<ArrayList<Integer>> bucketsBFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherBFS = new Integer[BFSorder.size()];
				 long timeBFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtBFS =
				 HyperGraphs.bucketElimination(dhg,
				 BFSorder, bucketsBFS, varFatherBFS);
				 timeBFSBEJT = System.currentTimeMillis() - timeBFSBEJT;
				 metrics.get("avgBFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))+
				 timeBFSBEJT);
				 int twBFSBE = jtBFS.width();
				 metrics.get("avgBFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))+
				 twBFSBE);
				 System.out.println(" jointree par BE BFS width " +
				 jtBFS.width()+" time "+timeBFSBEJT);
			}

			 metrics.get("avgMinFillTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))/nbInstances);
			 metrics.get("avgDFSTimeOrder").put(	nbVertices,
						((Long) metrics.get("avgDFSMaxAncTimeOrder").get(	nbVertices))/ nbInstances);
			metrics.get("avgDFSMaxAncTimeOrder").put(	nbVertices,
			((Long) metrics.get("avgDFSTimeOrder").get(	nbVertices))/ nbInstances);
			 metrics.get("avgBFSTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))/nbInstances);
			 metrics.get("avgDFSTimeBEJT").put(nbVertices,
						((Long) metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices))/ nbInstances);
			 metrics.get("avgDFSTimeBEJT").put(nbVertices,
			((Long) metrics.get("avgDFSTimeBEJT").get(nbVertices))/ nbInstances);
			 metrics.get("avgBFSTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTime").put(nbVertices,
			 (Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgMinFillTimeOrder").get(nbVertices));
			 metrics.get("avgDFSMaxAncTime").put(nbVertices,
			(Long) metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices)+
			(Long) metrics.get("avgDFSMaxAncTimeOrder").get(nbVertices));
			 metrics.get("avgDFSMaxAncTime").put(nbVertices,
			(Long) metrics.get("avgDFSTimeBEJT").get(nbVertices)+
			(Long) metrics.get("avgDFSTimeOrder").get(nbVertices));
			 metrics.get("avgBFSTime").put(nbVertices,
			 (Long)metrics.get("avgBFSTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgBFSTimeOrder").get(nbVertices));

			 metrics.get("avgMinFillJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))/nbInstances);
			 metrics.get("avgDFSMaxAncJTwidth").put(nbVertices,
						((Integer) metrics.get("avgDFSMaxAncJTwidth").get(nbVertices))/ nbInstances);
			 metrics.get("avgDFSJTwidth").put(nbVertices,
			((Integer) metrics.get("avgDFSJTwidth").get(nbVertices))/ nbInstances);
			 metrics.get("avgBFSJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))/nbInstances);
		}

		File dirOut = new File(dirOutName);
		if (!dirOut.exists())
			dirOut.mkdirs();

		HashMap<String, HashMap<String, HashMap<Integer, Number>>> dataPlots = new HashMap<String, HashMap<String, HashMap<Integer, Number>>>();

		dataPlots.put("avgTimeOrder",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTimeOrder").put("avgMinFillTimeOrder",
		 metrics.get("avgMinFillTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgDFSMaxAncTimeOrder",
				metrics.get("avgDFSMaxAncTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgDFSTimeOrder",
				metrics.get("avgDFSTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgBFSTimeOrder",
		 metrics.get("avgBFSTimeOrder"));

		dataPlots.put("avgTimeBEJT",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTimeBEJT").put("avgMinFillTimeBEJT",
		 metrics.get("avgMinFillTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgDFSMaxAncTimeBEJT",
					metrics.get("avgDFSMaxAncTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgDFSTimeBEJT",
				metrics.get("avgDFSTimeBEJT"));
		 dataPlots.get("avgTimeBEJT").put("avgBFSTimeBEJT",
		 metrics.get("avgBFSTimeBEJT"));

		dataPlots.put("avgTime",
				new HashMap<String, HashMap<Integer, Number>>());
		dataPlots.get("avgTime").put("avgMinFillTime",
		 metrics.get("avgMinFillTime"));
		dataPlots.get("avgTime").put("avgDFSMaxAncTime",
				metrics.get("avgDFSMaxAncTime"));
		dataPlots.get("avgTime").put("avgDFSTime",
				metrics.get("avgDFSTime"));
		 dataPlots.get("avgTime").put("avgBFSTime",
		 metrics.get("avgBFSTime"));

		dataPlots.put("avgJTWidth",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgJTWidth").put("avgMinFillJTwidth",
		 metrics.get("avgMinFillJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgTokenEO",
					metrics.get("avgDFSMaxAncJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgDFSJTwidth",
				metrics.get("avgDFSJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgBFSJTwidth",
		 metrics.get("avgBFSJTwidth"));

		String extraTitle = k + " step: " + stepNbVertices
				+ "  nbInstances " + nbInstances;
		for (String plotName : dataPlots.keySet()) {
			File dirPlot = new File(dirOut.getPath() + File.separator
					+k+File.separator+ plotName);
			if (dirPlot.exists())
				FileTools.recursiveDelete(dirPlot);
			dirPlot.mkdirs();
			HashMap<String, File> curveFiles = saveMetricsInFiles(
					dataPlots.get(plotName), dirPlot);
			writeDefault2DLinesPoints("nbVertices", plotName, curveFiles,
					dirPlot, extraTitle);
		}
		}

	}
	

	public static void testRandomT(HashMap<String,Object> params) throws Exception{
		// random instance
		
		String dirOutName = (String)params.get("dirOutName");
		String graphModel = (String) params.get("graphModel");
		int nbInstances = (Integer) params.get("nbInstances");
		int nbVerticesMax = (Integer) params.get("nbVerticesMax");
		int nbVerticesMin = (Integer) params.get("nbVerticesMin");
		int stepNbVertices  = (Integer) params.get("step");
		
		
		
		
		HashMap<String,HashMap<String,Object>> rGraphParams = 
			new HashMap<String, HashMap<String,Object>>();
		
		if(graphModel.equals("BA")){
			rGraphParams.put("BAGraph", new HashMap<String,Object>());
			rGraphParams.get("BAGraph").put("randomGraph", "BA");
		}
		
		if(graphModel.equals("WS")){
			rGraphParams.put("WSGraph", new HashMap<String,Object>());
			rGraphParams.get("WSGraph").put("randomGraph", "WS");
		}
		
		if(graphModel.equals("UD")){
			rGraphParams.put("UDGraph", new HashMap<String,Object>());
			rGraphParams.get("UDGraph").put("randomGraph", "UD");
		}
		
		for(String k : rGraphParams.keySet()){
		// random instance
		HashMap<String, HashMap<Integer, Number>> metrics = new HashMap<String, HashMap<Integer, Number>>();
		 metrics.put("avgMinFillTimeOrder", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTimeOrder",
				new HashMap<Integer, Number>());
		 metrics.put("avgBFSTimeOrder", new HashMap<Integer,Number>());

		 metrics.put("avgMinFillTimeBEJT", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		 metrics.put("avgBFSTimeBEJT", new HashMap<Integer,Number>());

		 metrics.put("avgMinFillTime", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTime", new HashMap<Integer, Number>());
		 metrics.put("avgBFSTime", new HashMap<Integer,Number>());

		 metrics.put("avgMinFillJTwidth", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistJTwidth", new HashMap<Integer, Number>());
		 metrics.put("avgBFSJTwidth", new HashMap<Integer,Number>());
		 
			
			for (int nbVertices = nbVerticesMin; nbVertices <= nbVerticesMax; nbVertices += stepNbVertices) {

				System.out.println();
				System.out.println("nbVertices " + nbVertices);
				System.out.println();

			 metrics.get("avgMinFillTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxFillDistTimeOrder").put(nbVertices, (long) 0.0);
			metrics.get("avgMinFillDistTimeOrder").put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMinFillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin1FillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeOrder").put(nbVertices,(long) 0.0);

			 metrics.get("avgMinFillTimeBEJT").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxFillDistTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgMinFillDistTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMinFillDistTimeBEJT")
					.put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMin1FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin3FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeBEJT").put(nbVertices,(long) 0.0);

			 metrics.get("avgMinFillJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMinFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMinFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMin1FillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMin2FillDistJTwidth").put(nbVertices, 0);
			 metrics.get("avgBFSJTwidth").put(nbVertices,0);

			for (int numInstance = 0; numInstance < nbInstances; numInstance++) {
			 System.out.println("numInstance " + numInstance);
				
			 rGraphParams.get(k).put("nbPeers", "" + nbVertices);
				HyperGraph<Integer> dhg = genRandomGraph(rGraphParams.get(k));
				// System.out.println(" Le graphe " + dhg);

				 long timeMinFillorder = System.currentTimeMillis();
				 ArrayList<Integer> minFillorder = HyperGraphs
				 .minFillInvOrder(dhg);
				 timeMinFillorder = System.currentTimeMillis() -
				 timeMinFillorder;
				 metrics.get("avgMinFillTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))+
				 timeMinFillorder);
				 // System.out.println(" minFillorder  " + minFillorder);
//				 System.out.println("timeMinFillorder "+ timeMinFillorder);

				long timeMaxFillDistorder = System.currentTimeMillis();
//				ArrayList<Integer> maxFillDistorder = minMaxFillDistOrder(
//						"max", dhg);
				ArrayList<Integer> maxFillDistorder = new ArrayList<Integer>();
				
				timeMaxFillDistorder = System.currentTimeMillis()
						- timeMaxFillDistorder;
				metrics.get("avgMaxFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxFillDistTimeOrder").get(
								nbVertices))
								+ timeMaxFillDistorder);
				// System.out.println(" MaxFillDistorder  " + MaxFillDistorder);
//				System.out.println("timeMaxFillDistorder "
//						+ timeMaxFillDistorder);

				long timeMinFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> minFillDistorder =  new ArrayList<Integer>(); 
//					minMaxFillDistOrder(	"min", dhg);
				timeMinFillDistorder = System.currentTimeMillis()
						- timeMinFillDistorder;
				metrics.get("avgMinFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMinFillDistTimeOrder").get(
								nbVertices))
								+ timeMinFillDistorder);
//				System.out.println("timeMinFillDistorder "
//						+ timeMinFillDistorder);

				long timeMaxMinFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMinFillDistorder =  new ArrayList<Integer>();
//					minMaxFillDistOrder(	"maxmin", dhg);
				timeMaxMinFillDistorder = System.currentTimeMillis()
						- timeMaxMinFillDistorder;
				metrics.get("avgMaxMinFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMinFillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMinFillDistorder);
//				System.out.println("timeMaxMinFillDistorder "
//						+ timeMaxMinFillDistorder);

				long timeMaxMin1FillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMin1FillDistorder  = new ArrayList<Integer>();
//					minMaxFillDistOrder("maxmin1", dhg);
				timeMaxMin1FillDistorder = System.currentTimeMillis()
						- timeMaxMin1FillDistorder;
				metrics.get("avgMaxMin1FillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin1FillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMin1FillDistorder);
//				System.out.println("timeMaxMin1FillDistorder "
//						+ timeMaxMin1FillDistorder);

				long timeMaxMin2FillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMin2FillDistorder =dFSOrderMaxNeigh( dhg);
				timeMaxMin2FillDistorder = System.currentTimeMillis()
						- timeMaxMin2FillDistorder;
				metrics.get("avgMaxMin2FillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMin2FillDistorder);
//				System.out.println("timeMaxMin2FillDistorder "
//						+ timeMaxMin2FillDistorder);

				 long timeBFSorder = System.currentTimeMillis();
				 ArrayList<Integer> BFSorder = dFSOrder(dhg);
				 timeBFSorder = System.currentTimeMillis() - timeBFSorder;
				 metrics.get("avgBFSTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))+
				 timeBFSorder);
//				 System.out.println("timeBFSorder "+ timeBFSorder);

				// Built Jointree by BE with the Order and

				 ArrayList<ArrayList<Integer>> bucketsMinF = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherMinF = new Integer[minFillorder.size()];
				 long timeMinFillBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtMinF =
				 HyperGraphs.bucketElimination(dhg,
				 minFillorder, bucketsMinF, varFatherMinF);
				 timeMinFillBEJT = System.currentTimeMillis() -
				 timeMinFillBEJT;
				 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))+
				 timeMinFillBEJT);
				 int twMinFillBE = jtMinF.width();
				 metrics.get("avgMinFillJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))+
				 twMinFillBE);
				 System.out.println(" jointree par BE Min-Fill witdh " +
				 jtMinF.width()+" time "+(timeMinFillBEJT+timeMinFillorder));

				ArrayList<ArrayList<Integer>> bucketsMaxF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxF = new Integer[maxFillDistorder.size()];
				long timeMaxFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxF = 
					tokenEO(dhg,new HyperGraph<AbstCompArrayList<Integer>> (), "MinAddProj",true);
				timeMaxFillDistBEJT = System.currentTimeMillis()
						- timeMaxFillDistBEJT;
				metrics.get("avgMaxFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxFillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxFillDistBEJT);
				int twMaxFillDistBE = jtMaxF.width();
				metrics.get("avgMaxFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxFillDistJTwidth").get(
								nbVertices))
								+ twMaxFillDistBE);
				System.out.println(" jointree par TE-MinProj width "
						+ jtMaxF.width() + " time " + timeMaxFillDistBEJT);
				
					
				

				ArrayList<ArrayList<Integer>> bucketsMinDF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMinDF = new Integer[minFillDistorder.size()];
				long timeMinFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMinDF = tokenEO(dhg,new HyperGraph<AbstCompArrayList<Integer>> (), "MinProj",true);
				timeMinFillDistBEJT = System.currentTimeMillis()
						- timeMinFillDistBEJT;
				metrics.get("avgMinFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMinFillDistTimeBEJT").get(
								nbVertices))
								+ timeMinFillDistBEJT);
				int twMinFillDistBE = jtMinDF.width();
				metrics.get("avgMinFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMinFillDistJTwidth").get(
								nbVertices))
								+ twMinFillDistBE);
				System.out.println(" jointree par TE-MinCluster width "
						+ jtMinDF.width() + " time " + timeMinFillDistBEJT);

				
				ArrayList<ArrayList<Integer>> bucketsMaxMinDF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMinDF = new Integer[maxMinFillDistorder
						.size()];
				long timeMaxMinFillDistBEJT = System.currentTimeMillis(); 
				HyperGraph<Integer> jtMaxMinDF = 
				tokenEO(dhg,new HyperGraph<AbstCompArrayList<Integer>> (), "MinAddProj",false);

				timeMaxMinFillDistBEJT = System.currentTimeMillis()
						- timeMaxMinFillDistBEJT;
				metrics.get("avgMaxMinFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMinFillDistBEJT);
				int twMaxMinFillDistBE = jtMaxMinDF.width();
				metrics.get("avgMaxMinFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMinFillDistJTwidth").get(
								nbVertices))
								+ twMaxMinFillDistBE);
				System.out.println(" jointree par TE-MinProj 2 width "
						+ jtMaxMinDF.width() + " time "
						+ timeMaxMinFillDistBEJT);

				
				
				ArrayList<ArrayList<Integer>> bucketsMaxMin1DF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMin1DF = new Integer[maxMin1FillDistorder
						.size()];
				long timeMaxMin1FillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxMin1DF = 
				tokenEO(dhg,new HyperGraph<AbstCompArrayList<Integer>> (), "MinProj",false);
				timeMaxMin1FillDistBEJT = System.currentTimeMillis()
						- timeMaxMin1FillDistBEJT;
				metrics.get("avgMaxMin1FillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMin1FillDistBEJT);
				int twMaxMin1FillDistBE = jtMaxMin1DF.width();
				metrics.get("avgMaxMin1FillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMin1FillDistJTwidth")
								.get(nbVertices)) + twMaxMin1FillDistBE);
				System.out.println(" jointree par TE-MinCluster 2 width "
						+ jtMaxMin1DF.width() + " time "
						+ timeMaxMin1FillDistBEJT);

				
				
				ArrayList<ArrayList<Integer>> bucketsMaxMin2DF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMin2DF = new Integer[maxMin2FillDistorder
						.size()];
				long timeMaxMin2FillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxMin2DF = HyperGraphs
						.bucketElimination(dhg, maxMin2FillDistorder,
								bucketsMaxMin2DF, varFatherMaxMin2DF);
				
//	
//				HyperGraph<AbstCompArrayList<Integer>> dualHjt = new HyperGraph<AbstCompArrayList<Integer>>();
//				HyperGraph<Integer> jtMaxMin2DF = DAGDecomp.distribJT2(dhg, dualHjt);
				
				timeMaxMin2FillDistBEJT = System.currentTimeMillis()
						- timeMaxMin2FillDistBEJT;
				metrics.get("avgMaxMin2FillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMin2FillDistBEJT);
				int twMaxMin2FillDistBE = jtMaxMin2DF.width();
				metrics.get("avgMaxMin2FillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMin2FillDistJTwidth")
								.get(nbVertices)) + twMaxMin2FillDistBE);
				System.out.println(" jointree par BE DFS-MCS width "
						+ jtMaxMin2DF.width() + " time "
						+ (timeMaxMin2FillDistBEJT+timeMaxMin2FillDistorder));
				
				
				

				 ArrayList<ArrayList<Integer>> bucketsBFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherBFS = new Integer[BFSorder.size()];
				 long timeBFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtBFS =
				 HyperGraphs.bucketElimination(dhg,
				 BFSorder, bucketsBFS, varFatherBFS);
				 timeBFSBEJT = System.currentTimeMillis() - timeBFSBEJT;
				 metrics.get("avgBFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))+
				 timeBFSBEJT);
				 int twBFSBE = jtBFS.width();
				 metrics.get("avgBFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))+
				 twBFSBE);
				 System.out.println(" jointree par BE DFS width " +
				 jtBFS.width()+" time "+(timeBFSBEJT+ timeBFSorder));
			}

			 metrics.get("avgMinFillTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMinFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMinFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin1FillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMinFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTime").put(nbVertices,
			 (Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgMinFillTimeOrder").get(nbVertices));
			metrics.get("avgMaxFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxFillDistTimeBEJT")
							.get(nbVertices)
							+ (Long) metrics.get("avgMaxFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMinFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMinFillDistTimeBEJT")
							.get(nbVertices)
							+ (Long) metrics.get("avgMinFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMinFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMinFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMin1FillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMin1FillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMin2FillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMin2FillDistTimeOrder")
									.get(nbVertices));
			 metrics.get("avgBFSTime").put(nbVertices,
			 (Long)metrics.get("avgBFSTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgBFSTimeOrder").get(nbVertices));

			 metrics.get("avgMinFillJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMinFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMinFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMin1FillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMin2FillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))/nbInstances);
		}


		File dirOut = new File(dirOutName);
		if (!dirOut.exists())
			dirOut.mkdirs();
//		else{
//			int iVersion =dirOutName.lastIndexOf("_");
//			if(iVersion> -1){
//				int nvVersion = Integer.valueOf(dirOutName.substring(iVersion))+1;
//				dirOut = new File(dirOutName+"_"+nvVersion);
//			}else{
//				dirOut = new File(dirOutName+"_1");
//			}
//			dirOut.mkdirs();
//		}

		HashMap<String, HashMap<String, HashMap<Integer, Number>>> dataPlots = new HashMap<String, HashMap<String, HashMap<Integer, Number>>>();
		
//		dataPlots.put("avgTimeOrder",
//				new HashMap<String, HashMap<Integer, Number>>());
//		 dataPlots.get("avgTimeOrder").put("avgMinFillTimeOrder",
//		 metrics.get("avgMinFillTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMaxFillDistTimeOrder",
//				metrics.get("avgMaxFillDistTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMinFillDistTimeOrder",
//				metrics.get("avgMinFillDistTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMaxMinFillDistTimeOrder",
//				metrics.get("avgMaxMinFillDistTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMaxMin1FillDistTimeOrder",
//				metrics.get("avgMaxMin1FillDistTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMaxMin2FillDistTimeOrder",
//				metrics.get("avgMaxMin2FillDistTimeOrder"));
//		 dataPlots.get("avgTimeOrder").put("avgBFSTimeOrder",
//		 metrics.get("avgBFSTimeOrder"));
//
//		dataPlots.put("avgTimeBEJT",
//				new HashMap<String, HashMap<Integer, Number>>());
//		 dataPlots.get("avgTimeBEJT").put("avgMinFillTimeBEJT",
//		 metrics.get("avgMinFillTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMaxFillDistTimeBEJT",
//				metrics.get("avgMaxFillDistTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMinFillDistTimeBEJT",
//				metrics.get("avgMinFillDistTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMaxMinFillDistTimeBEJT",
//				metrics.get("avgMaxMinFillDistTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMaxMin1FillDistTimeBEJT",
//				metrics.get("avgMaxMin1FillDistTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMaxMin2FillDistTimeBEJT",
//				metrics.get("avgMaxMin2FillDistTimeBEJT"));
//		 dataPlots.get("avgTimeBEJT").put("avgBFSTimeBEJT",
//		 metrics.get("avgBFSTimeBEJT"));

		dataPlots.put("avgTime",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTime").put("BE-MinFill",
		 metrics.get("avgMinFillTime"));
		dataPlots.get("avgTime").put("TE-MinProj",
				metrics.get("avgMaxFillDistTime"));
		dataPlots.get("avgTime").put("TE-MinCluster",
				metrics.get("avgMinFillDistTime"));
		dataPlots.get("avgTime").put("TE-MinProj-Bis",
				metrics.get("avgMaxMinFillDistTime"));
		dataPlots.get("avgTime").put("TE-MinCluster-Bis",
				metrics.get("avgMaxMin1FillDistTime"));
		dataPlots.get("avgTime").put("BE-DFS-MCS",
				metrics.get("avgMaxMin2FillDistTime"));
		 dataPlots.get("avgTime").put("BE-DFS",
		 metrics.get("avgBFSTime"));

		dataPlots.put("avgJTWidth",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgJTWidth").put("BE-MinFill",
		 metrics.get("avgMinFillJTwidth"));
		dataPlots.get("avgJTWidth").put("TE-MinProj",
				metrics.get("avgMaxFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("TE-MinCluster",
				metrics.get("avgMinFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("TE-MinProj-Bis",
				metrics.get("avgMaxMinFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("TE-MinCluster-Bis",
				metrics.get("avgMaxMin1FillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("BE-DFS-MCS",
				metrics.get("avgMaxMin2FillDistJTwidth"));
		 dataPlots.get("avgJTWidth").put("BE-DFS",
		 metrics.get("avgBFSJTwidth"));

			String extraTitle = k + " step: " + stepNbVertices
			+ "  nbInstances " + nbInstances;
	for (String plotName : dataPlots.keySet()) {
		File dirPlot = new File(dirOut.getPath() + File.separator
				+k+File.separator+ plotName);
		if (dirPlot.exists())
			FileTools.recursiveDelete(dirPlot);
		dirPlot.mkdirs();
		HashMap<String, File> curveFiles = saveMetricsInFiles(
				dataPlots.get(plotName), dirPlot);
		try{
		writeDefault2DLinesPoints("nbVertices", plotName, curveFiles,
				dirPlot, extraTitle);
		}
		catch(Exception e){
			System.out.println( "La courbe n'est pas dessin�e \n"+e.getMessage());
		}
	}
		}

	}
	

	public static void testRandomTE(HashMap<String,Object> params) throws Exception{
		// random instance
		
		String dirOutName = (String)params.get("dirOutName");
		String graphModel = (String) params.get("graphModel");
		int nbInstances = (Integer) params.get("nbInstances");
		int nbVerticesMax = (Integer) params.get("nbVerticesMax");
		int nbVerticesMin = (Integer) params.get("nbVerticesMin");
		int stepNbVertices  = (Integer) params.get("step");
		
		
		
		
		HashMap<String,HashMap<String,Object>> rGraphParams = 
			new HashMap<String, HashMap<String,Object>>();
		
		if(graphModel.equals("BA")){
			rGraphParams.put("BAGraph", new HashMap<String,Object>());
			rGraphParams.get("BAGraph").put("randomGraph", "BA");
		}
		
		if(graphModel.equals("WS")){
			rGraphParams.put("WSGraph", new HashMap<String,Object>());
			rGraphParams.get("WSGraph").put("randomGraph", "WS");
		}
		
		if(graphModel.equals("UD")){
			rGraphParams.put("UDGraph", new HashMap<String,Object>());
			rGraphParams.get("UDGraph").put("randomGraph", "UD");
		}
		
		for(String k : rGraphParams.keySet()){
		// random instance
		HashMap<String, HashMap<Integer, Number>> metrics = new HashMap<String, HashMap<Integer, Number>>();
		 metrics.put("avgMinFillTimeOrder", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTimeOrder",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTimeOrder",
				new HashMap<Integer, Number>());
		 metrics.put("avgBFSTimeOrder", new HashMap<Integer,Number>());

		 metrics.put("avgMinFillTimeBEJT", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTimeBEJT",
				new HashMap<Integer, Number>());
		 metrics.put("avgBFSTimeBEJT", new HashMap<Integer,Number>());

		 metrics.put("avgMinFillTime", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistTime", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistTime", new HashMap<Integer, Number>());
		 metrics.put("avgBFSTime", new HashMap<Integer,Number>());

		 metrics.put("avgMinFillJTwidth", new HashMap<Integer,Number>());
		metrics.put("avgMaxFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMinFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMinFillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin1FillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin2FillDistJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgMaxMin3FillDistJTwidth", new HashMap<Integer, Number>());
		 metrics.put("avgBFSJTwidth", new HashMap<Integer,Number>());
		 
			
			for (int nbVertices = nbVerticesMin; nbVertices <= nbVerticesMax; nbVertices += stepNbVertices) {

				System.out.println();
				System.out.println("nbVertices " + nbVertices);
				System.out.println();

			 metrics.get("avgMinFillTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxFillDistTimeOrder").put(nbVertices, (long) 0.0);
			metrics.get("avgMinFillDistTimeOrder").put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMinFillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin1FillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeOrder").put(nbVertices,(long) 0.0);

			 metrics.get("avgMinFillTimeBEJT").put(nbVertices,(long) 0.0);
			metrics.get("avgMaxFillDistTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgMinFillDistTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMinFillDistTimeBEJT")
					.put(nbVertices, (long) 0.0);
			metrics.get("avgMaxMin1FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgMaxMin3FillDistTimeBEJT").put(nbVertices,
					(long) 0.0);
			metrics.get("avgBFSTimeBEJT").put(nbVertices,(long) 0.0);

			 metrics.get("avgMinFillJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMinFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMinFillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMin1FillDistJTwidth").put(nbVertices, 0);
			metrics.get("avgMaxMin2FillDistJTwidth").put(nbVertices, 0);
			 metrics.get("avgBFSJTwidth").put(nbVertices,0);

			for (int numInstance = 0; numInstance < nbInstances; numInstance++) {
			 System.out.println("numInstance " + numInstance);
				
			 rGraphParams.get(k).put("nbPeers", "" + nbVertices);
				HyperGraph<Integer> dhg = genRandomGraph(rGraphParams.get(k));
				// System.out.println(" Le graphe " + dhg);

				 long timeMinFillorder = System.currentTimeMillis();
				 ArrayList<Integer> minFillorder = HyperGraphs
				 .minFillInvOrder(dhg);
				 timeMinFillorder = System.currentTimeMillis() -
				 timeMinFillorder;
				 metrics.get("avgMinFillTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))+
				 timeMinFillorder);
//				  System.out.println(" minFillorder  " + minFillorder);
				 System.out.println("timeMinFillorder "+ timeMinFillorder);

				long timeMaxFillDistorder = System.currentTimeMillis();
//				ArrayList<Integer> maxFillDistorder = minMaxFillDistOrder(
//						"max", dhg);
				ArrayList<Integer> maxFillDistorder = new ArrayList<Integer>();
				
				timeMaxFillDistorder = System.currentTimeMillis()
						- timeMaxFillDistorder;
				metrics.get("avgMaxFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxFillDistTimeOrder").get(
								nbVertices))
								+ timeMaxFillDistorder);
				// System.out.println(" MaxFillDistorder  " + MaxFillDistorder);
//				System.out.println("timeMaxFillDistorder "
//						+ timeMaxFillDistorder);

				long timeMinFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> minFillDistorder =  new ArrayList<Integer>(); 
//					minMaxFillDistOrder(	"min", dhg);
				timeMinFillDistorder = System.currentTimeMillis()
						- timeMinFillDistorder;
				metrics.get("avgMinFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMinFillDistTimeOrder").get(
								nbVertices))
								+ timeMinFillDistorder);
//				System.out.println("timeMinFillDistorder "
//						+ timeMinFillDistorder);

				long timeMaxMinFillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMinFillDistorder = 
					HyperGraphs.mCSOrder( dhg,"min");
				timeMaxMinFillDistorder = System.currentTimeMillis()
						- timeMaxMinFillDistorder;
				metrics.get("avgMaxMinFillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMinFillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMinFillDistorder);
				System.out.println("timeMaxMinFillDistorder "
						+ timeMaxMinFillDistorder);

				long timeMaxMin1FillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMin1FillDistorder  = 
					HyperGraphs.mCSOrder( dhg,"max");
				timeMaxMin1FillDistorder = System.currentTimeMillis()
						- timeMaxMin1FillDistorder;
				metrics.get("avgMaxMin1FillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin1FillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMin1FillDistorder);
				System.out.println("timeMaxMin1FillDistorder "
						+ timeMaxMin1FillDistorder);

				long timeMaxMin2FillDistorder = System.currentTimeMillis();
				ArrayList<Integer> maxMin2FillDistorder =dFSOrderMaxNeigh( dhg);
				timeMaxMin2FillDistorder = System.currentTimeMillis()
						- timeMaxMin2FillDistorder;
				metrics.get("avgMaxMin2FillDistTimeOrder").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
								nbVertices))
								+ timeMaxMin2FillDistorder);
//				System.out.println("timeMaxMin2FillDistorder "
//						+ timeMaxMin2FillDistorder);

				 long timeBFSorder = System.currentTimeMillis();
				 ArrayList<Integer> BFSorder = dFSOrder(dhg);
				 timeBFSorder = System.currentTimeMillis() - timeBFSorder;
				 metrics.get("avgBFSTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))+
				 timeBFSorder);
//				 System.out.println("timeBFSorder "+ timeBFSorder);

				 System.out.println();
				 
				// Built Jointree by BE with the Order and

				 ArrayList<ArrayList<Integer>> bucketsMinF = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherMinF = new Integer[minFillorder.size()];
				 long timeMinFillBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtMinF =
				 HyperGraphs.bucketElimination(dhg,
				 minFillorder, bucketsMinF, varFatherMinF);
				 timeMinFillBEJT = System.currentTimeMillis() -
				 timeMinFillBEJT;
				 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))+
				 timeMinFillBEJT);
				 int twMinFillBE = jtMinF.width();
				 metrics.get("avgMinFillJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))+
				 twMinFillBE);
				 System.out.println(" jointree par BE Min-Fill witdh " +
				 jtMinF.width()+" time "+(timeMinFillBEJT+timeMinFillorder));

				ArrayList<ArrayList<Integer>> bucketsMaxF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxF = new Integer[maxFillDistorder.size()];
				long timeMaxFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxF = 
					tokenEO(dhg,new HyperGraph<AbstCompArrayList<Integer>> (), "MinAddProj",true);
				timeMaxFillDistBEJT = System.currentTimeMillis()
						- timeMaxFillDistBEJT;
				metrics.get("avgMaxFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxFillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxFillDistBEJT);
				int twMaxFillDistBE = jtMaxF.width();
				metrics.get("avgMaxFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxFillDistJTwidth").get(
								nbVertices))
								+ twMaxFillDistBE);
				System.out.println(" jointree par TE-MinProj width "
						+ jtMaxF.width() + " time " + timeMaxFillDistBEJT);
				
					
				

				ArrayList<ArrayList<Integer>> bucketsMinDF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMinDF = new Integer[minFillDistorder.size()];
				long timeMinFillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMinDF = tokenEO(dhg,new HyperGraph<AbstCompArrayList<Integer>> (), "MinProj",true);
				timeMinFillDistBEJT = System.currentTimeMillis()
						- timeMinFillDistBEJT;
				metrics.get("avgMinFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMinFillDistTimeBEJT").get(
								nbVertices))
								+ timeMinFillDistBEJT);
				int twMinFillDistBE = jtMinDF.width();
				metrics.get("avgMinFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMinFillDistJTwidth").get(
								nbVertices))
								+ twMinFillDistBE);
				System.out.println(" jointree par TE-MinCluster width "
						+ jtMinDF.width() + " time " + timeMinFillDistBEJT);

				
				
				
				
				/*
				 * EN TRAVAUX
				 */
				
				ArrayList<ArrayList<Integer>> bucketsMaxMinDF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMinDF = new Integer[maxMinFillDistorder
						.size()];
				long timeMaxMinFillDistBEJT = System.currentTimeMillis(); 
				HyperGraph<Integer> jtMaxMinDF = 
					 HyperGraphs.bucketElimination(dhg,
							 maxMinFillDistorder, bucketsMaxMinDF,varFatherMaxMinDF);

				timeMaxMinFillDistBEJT = System.currentTimeMillis()
						- timeMaxMinFillDistBEJT;
				metrics.get("avgMaxMinFillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMinFillDistBEJT);
				int twMaxMinFillDistBE = jtMaxMinDF.width();
				metrics.get("avgMaxMinFillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMinFillDistJTwidth").get(
								nbVertices))
								+ twMaxMinFillDistBE);
				System.out.println(" jointree par BE-MinCS width "
						+ jtMaxMinDF.width() + " time "
						+ timeMaxMinFillDistBEJT);

				
				/*
				 *  FIN DES TRAVAUX
				 */
				
				
				
				
				ArrayList<ArrayList<Integer>> bucketsMaxMin1DF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMin1DF = new Integer[maxMin1FillDistorder
						.size()];
				long timeMaxMin1FillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxMin1DF = 
					HyperGraphs.bucketElimination(dhg,
							 maxMin1FillDistorder, bucketsMaxMin1DF,varFatherMaxMin1DF);
				timeMaxMin1FillDistBEJT = System.currentTimeMillis()
						- timeMaxMin1FillDistBEJT;
				metrics.get("avgMaxMin1FillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMin1FillDistBEJT);
				int twMaxMin1FillDistBE = jtMaxMin1DF.width();
				metrics.get("avgMaxMin1FillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMin1FillDistJTwidth")
								.get(nbVertices)) + twMaxMin1FillDistBE);
				System.out.println(" jointree par BE-MaxCS width "
						+ jtMaxMin1DF.width() + " time "
						+ timeMaxMin1FillDistBEJT);

				
				
				ArrayList<ArrayList<Integer>> bucketsMaxMin2DF = new ArrayList<ArrayList<Integer>>();
				Integer[] varFatherMaxMin2DF = new Integer[maxMin2FillDistorder
						.size()];
				long timeMaxMin2FillDistBEJT = System.currentTimeMillis();
				HyperGraph<Integer> jtMaxMin2DF = HyperGraphs
						.bucketElimination(dhg, maxMin2FillDistorder,
								bucketsMaxMin2DF, varFatherMaxMin2DF);
				
//	
//				HyperGraph<AbstCompArrayList<Integer>> dualHjt = new HyperGraph<AbstCompArrayList<Integer>>();
//				HyperGraph<Integer> jtMaxMin2DF = DAGDecomp.distribJT2(dhg, dualHjt);
				
				timeMaxMin2FillDistBEJT = System.currentTimeMillis()
						- timeMaxMin2FillDistBEJT;
				metrics.get("avgMaxMin2FillDistTimeBEJT").put(
						nbVertices,
						((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
								nbVertices))
								+ timeMaxMin2FillDistBEJT);
				int twMaxMin2FillDistBE = jtMaxMin2DF.width();
				metrics.get("avgMaxMin2FillDistJTwidth").put(
						nbVertices,
						((Integer) metrics.get("avgMaxMin2FillDistJTwidth")
								.get(nbVertices)) + twMaxMin2FillDistBE);
				System.out.println(" jointree par BE DFS-MCS width "
						+ jtMaxMin2DF.width() + " time "
						+ (timeMaxMin2FillDistBEJT+timeMaxMin2FillDistorder));
				
				
				

				 ArrayList<ArrayList<Integer>> bucketsBFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherBFS = new Integer[BFSorder.size()];
				 long timeBFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtBFS =
				 HyperGraphs.bucketElimination(dhg,
				 BFSorder, bucketsBFS, varFatherBFS);
				 timeBFSBEJT = System.currentTimeMillis() - timeBFSBEJT;
				 metrics.get("avgBFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))+
				 timeBFSBEJT);
				 int twBFSBE = jtBFS.width();
				 metrics.get("avgBFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))+
				 twBFSBE);
				 System.out.println(" jointree par BE DFS width " +
				 jtBFS.width()+" time "+(timeBFSBEJT+ timeBFSorder));
			}

			 metrics.get("avgMinFillTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMinFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMinFillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin1FillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistTimeOrder").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeOrder").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMinFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistTimeBEJT").put(
					nbVertices,
					((Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTime").put(nbVertices,
			 (Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgMinFillTimeOrder").get(nbVertices));
			metrics.get("avgMaxFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxFillDistTimeBEJT")
							.get(nbVertices)
							+ (Long) metrics.get("avgMaxFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMinFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMinFillDistTimeBEJT")
							.get(nbVertices)
							+ (Long) metrics.get("avgMinFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMinFillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMinFillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMinFillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMin1FillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMin1FillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMin1FillDistTimeOrder")
									.get(nbVertices));
			metrics.get("avgMaxMin2FillDistTime").put(
					nbVertices,
					(Long) metrics.get("avgMaxMin2FillDistTimeBEJT").get(
							nbVertices)
							+ (Long) metrics.get("avgMaxMin2FillDistTimeOrder")
									.get(nbVertices));
			 metrics.get("avgBFSTime").put(nbVertices,
			 (Long)metrics.get("avgBFSTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgBFSTimeOrder").get(nbVertices));

			 metrics.get("avgMinFillJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))/nbInstances);
			metrics.get("avgMaxFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMinFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMinFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMinFillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMinFillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin1FillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMin1FillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			metrics.get("avgMaxMin2FillDistJTwidth").put(
					nbVertices,
					((Integer) metrics.get("avgMaxMin2FillDistJTwidth").get(
							nbVertices))
							/ nbInstances);
			 metrics.get("avgBFSJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))/nbInstances);
		}


		File dirOut = new File(dirOutName);
		if (!dirOut.exists())
			dirOut.mkdirs();
//		else{
//			int iVersion =dirOutName.lastIndexOf("_");
//			if(iVersion> -1){
//				int nvVersion = Integer.valueOf(dirOutName.substring(iVersion))+1;
//				dirOut = new File(dirOutName+"_"+nvVersion);
//			}else{
//				dirOut = new File(dirOutName+"_1");
//			}
//			dirOut.mkdirs();
//		}

		HashMap<String, HashMap<String, HashMap<Integer, Number>>> dataPlots = new HashMap<String, HashMap<String, HashMap<Integer, Number>>>();
		
//		dataPlots.put("avgTimeOrder",
//				new HashMap<String, HashMap<Integer, Number>>());
//		 dataPlots.get("avgTimeOrder").put("avgMinFillTimeOrder",
//		 metrics.get("avgMinFillTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMaxFillDistTimeOrder",
//				metrics.get("avgMaxFillDistTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMinFillDistTimeOrder",
//				metrics.get("avgMinFillDistTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMaxMinFillDistTimeOrder",
//				metrics.get("avgMaxMinFillDistTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMaxMin1FillDistTimeOrder",
//				metrics.get("avgMaxMin1FillDistTimeOrder"));
//		dataPlots.get("avgTimeOrder").put("avgMaxMin2FillDistTimeOrder",
//				metrics.get("avgMaxMin2FillDistTimeOrder"));
//		 dataPlots.get("avgTimeOrder").put("avgBFSTimeOrder",
//		 metrics.get("avgBFSTimeOrder"));
//
//		dataPlots.put("avgTimeBEJT",
//				new HashMap<String, HashMap<Integer, Number>>());
//		 dataPlots.get("avgTimeBEJT").put("avgMinFillTimeBEJT",
//		 metrics.get("avgMinFillTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMaxFillDistTimeBEJT",
//				metrics.get("avgMaxFillDistTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMinFillDistTimeBEJT",
//				metrics.get("avgMinFillDistTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMaxMinFillDistTimeBEJT",
//				metrics.get("avgMaxMinFillDistTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMaxMin1FillDistTimeBEJT",
//				metrics.get("avgMaxMin1FillDistTimeBEJT"));
//		dataPlots.get("avgTimeBEJT").put("avgMaxMin2FillDistTimeBEJT",
//				metrics.get("avgMaxMin2FillDistTimeBEJT"));
//		 dataPlots.get("avgTimeBEJT").put("avgBFSTimeBEJT",
//		 metrics.get("avgBFSTimeBEJT"));

		dataPlots.put("avgTime",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTime").put("BE-MinFill",
		 metrics.get("avgMinFillTime"));
		dataPlots.get("avgTime").put("TE-MinProj",
				metrics.get("avgMaxFillDistTime"));
		dataPlots.get("avgTime").put("TE-MinCluster",
				metrics.get("avgMinFillDistTime"));
		dataPlots.get("avgTime").put("BE-MinCS",
				metrics.get("avgMaxMinFillDistTime"));
		dataPlots.get("avgTime").put("BE-MaxCS",
				metrics.get("avgMaxMin1FillDistTime"));
		dataPlots.get("avgTime").put("BE-DFS-MCS",
				metrics.get("avgMaxMin2FillDistTime"));
		 dataPlots.get("avgTime").put("BE-DFS",
		 metrics.get("avgBFSTime"));

		dataPlots.put("avgJTWidth",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgJTWidth").put("BE-MinFill",
		 metrics.get("avgMinFillJTwidth"));
		dataPlots.get("avgJTWidth").put("TE-MinProj",
				metrics.get("avgMaxFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("TE-MinCluster",
				metrics.get("avgMinFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("BE-MinCS",
				metrics.get("avgMaxMinFillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("BE-MaxCS",
				metrics.get("avgMaxMin1FillDistJTwidth"));
		dataPlots.get("avgJTWidth").put("BE-DFS-MCS",
				metrics.get("avgMaxMin2FillDistJTwidth"));
		 dataPlots.get("avgJTWidth").put("BE-DFS",
		 metrics.get("avgBFSJTwidth"));

			String extraTitle = k + " step: " + stepNbVertices
			+ "  nbInstances " + nbInstances;
	for (String plotName : dataPlots.keySet()) {
		File dirPlot = new File(dirOut.getPath() + File.separator
				+k+File.separator+ plotName);
		if (dirPlot.exists())
			FileTools.recursiveDelete(dirPlot);
		dirPlot.mkdirs();
		HashMap<String, File> curveFiles = saveMetricsInFiles(
				dataPlots.get(plotName), dirPlot);
		try{
		writeDefault2DLinesPoints("nbVertices", plotName, curveFiles,
				dirPlot, extraTitle);
		}
		catch(Exception e){
			System.out.println( "La courbe n'est pas dessin�e \n"+e.getMessage());
		}
	}
		}

	}
	

	public static void testRandomDFS(HashMap<String,Object> params) throws Exception{
		// random instance
		String dirOutName = (String)params.get("dirOutName");
		String graphModel = (String) params.get("graphModel");
		int nbInstances = (Integer) params.get("nbInstances");
		int nbVerticesMax = (Integer) params.get("nbVerticesMax");
		
		
		HashMap<String,HashMap<String,Object>> rGraphParams = 
			new HashMap<String, HashMap<String,Object>>();
		
		if(graphModel.equals("BA")){
			rGraphParams.put("BAGraph", new HashMap<String,Object>());
			rGraphParams.get("BAGraph").put("randomGraph", "BA");
		}
		
		if(graphModel.equals("WS")){
			rGraphParams.put("WSGraph", new HashMap<String,Object>());
			rGraphParams.get("WSGraph").put("randomGraph", "WS");
		}
		
		if(graphModel.equals("UD")){
			rGraphParams.put("UDGraph", new HashMap<String,Object>());
			rGraphParams.get("UDGraph").put("randomGraph", "UD");
		}
		
		for(String k : rGraphParams.keySet()){
		
		HashMap<String, HashMap<Integer, Number>> metrics = new HashMap<String, HashMap<Integer, Number>>();
		
		metrics.put("avgDFSTimeOrder", new HashMap<Integer,Number>());
		metrics.put("avgDFSRootTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncTimeOrder", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxNeighTimeOrder", new HashMap<Integer,Number>());
		metrics.put("avgDFSMaxFillInTimeOrder", new HashMap<Integer,Number>());

		metrics.put("avDFSTimeBEJT", new HashMap<Integer,Number>());
		metrics.put("avgDFSRootTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncTimeBEJT", new HashMap<Integer, Number>());
		metrics.put("avgBFSTimeBEJT", new HashMap<Integer,Number>());

		metrics.put("avgMinFillTime", new HashMap<Integer,Number>());
		metrics.put("avgDFSTime", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncTime", new HashMap<Integer, Number>());
		metrics.put("avgBFSTime", new HashMap<Integer,Number>());

		metrics.put("avgMinFillJTwidth", new HashMap<Integer,Number>());
		metrics.put("avgDFSJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgDFSMaxAncJTwidth", new HashMap<Integer, Number>());
		metrics.put("avgBFSJTwidth", new HashMap<Integer,Number>());
		
		
		int stepNbVertices = 100;
		
		for (int nbVertices = 20; nbVertices <= nbVerticesMax; nbVertices += stepNbVertices) {

			System.out.println();
			System.out.println("nbVertices " + nbVertices);
			System.out.println();
			
			metrics.get("avgMinFillTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgDFSTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgDFSMaxAncTimeOrder").put(nbVertices,(long) 0.0);
			metrics.get("avgBFSTimeOrder").put(nbVertices,(long) 0.0);

			metrics.get("avgMinFillTimeBEJT").put(nbVertices,(long) 0.0);
			metrics.get("avgDFSTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgDFSMaxAncTimeBEJT").put(nbVertices, (long) 0.0);
			metrics.get("avgBFSTimeBEJT").put(nbVertices,(long) 0.0);

			metrics.get("avgMinFillJTwidth").put(nbVertices, 0);
			metrics.get("avgDFSJTwidth").put(nbVertices, 0);
			metrics.get("avgDFSMaxAncJTwidth").put(nbVertices, 0);
			metrics.get("avgBFSJTwidth").put(nbVertices,0);

			for (int numInstance = 0; numInstance < nbInstances; numInstance++) {

				rGraphParams.get(k).put("nbPeers", "" + nbVertices);
				HyperGraph<Integer> dhg = genRandomGraph(rGraphParams.get(k));
				// System.out.println(" Le graphe " + dhg);

				 long timeMinFillorder = System.currentTimeMillis();
				 ArrayList<Integer> minFillorder = HyperGraphs
				 .minFillInvOrder(dhg);
				 timeMinFillorder = System.currentTimeMillis() -
				 timeMinFillorder;
				 metrics.get("avgMinFillTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))+
				 timeMinFillorder);
				// // System.out.println(" minFillorder  " + minFillorder);
//				 System.out.println("timeMinFillorder "+ timeMinFillorder);

				
				long timeDFSMaxAncOrder = System.currentTimeMillis();
					ArrayList<Integer> DFSMaxAncorder = dFSOrderMaxAncestors(dhg);
					timeDFSMaxAncOrder = System.currentTimeMillis()
							- timeDFSMaxAncOrder;
					metrics.get("avgDFSMaxAncTimeOrder").put(nbVertices,
					((Long) metrics.get("avgDFSMaxAncTimeOrder").get(nbVertices)) 
					+ timeDFSMaxAncOrder);
//					System.out.println("timeDFSMaxAncorder "	+ timeDFSMaxAncOrder);

				 
				 
				long timeDFSorder = System.currentTimeMillis();
				ArrayList<Integer> DFSorder = dFSOrder(dhg);
				timeDFSorder = System.currentTimeMillis()
						- timeDFSorder;
				metrics.get("avgDFSTimeOrder").put(nbVertices,
				((Long) metrics.get("avgDFSTimeOrder").get(nbVertices)) 
				+ timeDFSorder);
//				System.out.println("timeDFSorder "	+ timeDFSorder);

				
				
				
				 long timeBFSorder = System.currentTimeMillis();
				 ArrayList<Integer> BFSorder = bFSOrder(dhg);
				 timeBFSorder = System.currentTimeMillis() - timeBFSorder;
				 metrics.get("avgBFSTimeOrder").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))+
				 timeBFSorder);
//				 System.out.println("timeBFSorder "+ timeBFSorder);

				
				 
				 // Built Jointree by BE with the Order and

				 ArrayList<ArrayList<Integer>> bucketsMinF = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherMinF = new Integer[minFillorder.size()];
				 
				 long timeMinFillBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtMinF =
				 HyperGraphs.bucketElimination(dhg,
				 minFillorder, bucketsMinF, varFatherMinF);
				 timeMinFillBEJT = System.currentTimeMillis() -
				 timeMinFillBEJT;
				 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))+
				 timeMinFillBEJT);
				 int twMinFillBE = jtMinF.width();
				 metrics.get("avgMinFillJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))+
				 twMinFillBE);
				 System.out.println(" jointree par BE Min-Fill witdh " +
				 jtMinF.width()+" time "+timeMinFillBEJT);

//				 ArrayList<ArrayList<Integer>> bucketsDFSMaxAnc = new
//				 ArrayList<ArrayList<Integer>>();
//				 Integer[] varFatherDFSMaxAnc = new Integer[DFSMaxAncorder.size()];
//				 long timeDFSMaxAncBEJT = System.currentTimeMillis();
//				 HyperGraph<Integer> jtDFSMaxAnc =
//				 HyperGraphs.bucketElimination(dhg,
//				 DFSMaxAncorder, bucketsDFSMaxAnc, varFatherDFSMaxAnc);
//				 HashMap<Integer,ArrayList<Integer>> childrenOf = new HashMap<Integer,ArrayList<Integer>>();
//				 HyperGraphs.localRefinement(dhg, DFSMaxAncorder, bucketsDFSMaxAnc,varFatherDFSMaxAnc, childrenOf);
//				 timeDFSMaxAncBEJT = System.currentTimeMillis() - timeDFSMaxAncBEJT;
//				 metrics.get("avgDFSMaxAncTimeBEJT").put(nbVertices,
//				 ((Long)metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices))+
//				 timeDFSMaxAncBEJT);
//				 int twDFSMaxAncBE = jtDFSMaxAnc.width();
//				 metrics.get("avgDFSMaxAncJTwidth").put(nbVertices,
//				 ((Integer)metrics.get("avgDFSMaxAncJTwidth").get(nbVertices))+
//				 twDFSMaxAncBE);
//				 System.out.println(" jointree par BE DFSMaxAnc width " +
//				 jtDFSMaxAnc.width()+" time "+timeDFSMaxAncBEJT);
				
				ArrayList<ArrayList<Integer>> bucketsDFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherDFS = new Integer[DFSorder.size()];
				 long timeDFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtDFS =
				 HyperGraphs.bucketElimination(dhg,
				 DFSorder, bucketsDFS, varFatherDFS);
				 timeDFSBEJT = System.currentTimeMillis() - timeDFSBEJT;
				 metrics.get("avgDFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgDFSTimeBEJT").get(nbVertices))+
				 timeDFSBEJT);
				 int twDFSBE = jtDFS.width();
				 metrics.get("avgDFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgDFSJTwidth").get(nbVertices))+
				 twDFSBE);
				 System.out.println(" jointree par BE DFS width " +
				 jtDFS.width()+" time "+timeDFSBEJT);
				 
				 
				 ArrayList<ArrayList<Integer>> bucketsDFSMaxAnc = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherDFSMaxAnc = new Integer[DFSMaxAncorder.size()];
				 long timeDFSMaxAncBEJT = System.currentTimeMillis();
				 HashMap<Integer,ArrayList<Integer>> childrenOf = new HashMap<Integer,ArrayList<Integer>>();
				 HyperGraph<Integer> jtDFSMaxAnc =
				 HyperGraphs.localRefinement(dhg,  DFSorder, bucketsDFS, varFatherDFS, childrenOf);
				 timeDFSMaxAncBEJT = System.currentTimeMillis() - timeDFSMaxAncBEJT;
				 metrics.get("avgDFSMaxAncTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices))+
				 timeDFSMaxAncBEJT);
				 int twDFSMaxAncBE = jtDFSMaxAnc.width();
				 metrics.get("avgDFSMaxAncJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgDFSMaxAncJTwidth").get(nbVertices))+
				 twDFSMaxAncBE);
				 System.out.println(" jointree par BE DFSMaxAnc width " +
				 jtDFSMaxAnc.width()+" time "+timeDFSMaxAncBEJT);
				 
				 

				 ArrayList<ArrayList<Integer>> bucketsBFS = new
				 ArrayList<ArrayList<Integer>>();
				 Integer[] varFatherBFS = new Integer[BFSorder.size()];
				 long timeBFSBEJT = System.currentTimeMillis();
				 HyperGraph<Integer> jtBFS =
				 HyperGraphs.bucketElimination(dhg,
				 BFSorder, bucketsBFS, varFatherBFS);
				 timeBFSBEJT = System.currentTimeMillis() - timeBFSBEJT;
				 metrics.get("avgBFSTimeBEJT").put(nbVertices,
				 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))+
				 timeBFSBEJT);
				 int twBFSBE = jtBFS.width();
				 metrics.get("avgBFSJTwidth").put(nbVertices,
				 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))+
				 twBFSBE);
				 System.out.println(" jointree par BE BFS width " +
				 jtBFS.width()+" time "+timeBFSBEJT);
			}

			 metrics.get("avgMinFillTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeOrder").get(nbVertices))/nbInstances);
			 metrics.get("avgDFSTimeOrder").put(	nbVertices,
						((Long) metrics.get("avgDFSMaxAncTimeOrder").get(	nbVertices))/ nbInstances);
			metrics.get("avgDFSMaxAncTimeOrder").put(	nbVertices,
			((Long) metrics.get("avgDFSTimeOrder").get(	nbVertices))/ nbInstances);
			 metrics.get("avgBFSTimeOrder").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeOrder").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices))/nbInstances);
			 metrics.get("avgDFSTimeBEJT").put(nbVertices,
						((Long) metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices))/ nbInstances);
			 metrics.get("avgDFSTimeBEJT").put(nbVertices,
			((Long) metrics.get("avgDFSTimeBEJT").get(nbVertices))/ nbInstances);
			 metrics.get("avgBFSTimeBEJT").put(nbVertices,
			 ((Long)metrics.get("avgBFSTimeBEJT").get(nbVertices))/nbInstances);

			 metrics.get("avgMinFillTime").put(nbVertices,
			 (Long)metrics.get("avgMinFillTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgMinFillTimeOrder").get(nbVertices));
			 metrics.get("avgDFSMaxAncTime").put(nbVertices,
			(Long) metrics.get("avgDFSMaxAncTimeBEJT").get(nbVertices)+
			(Long) metrics.get("avgDFSMaxAncTimeOrder").get(nbVertices));
			 metrics.get("avgDFSMaxAncTime").put(nbVertices,
			(Long) metrics.get("avgDFSTimeBEJT").get(nbVertices)+
			(Long) metrics.get("avgDFSTimeOrder").get(nbVertices));
			 metrics.get("avgBFSTime").put(nbVertices,
			 (Long)metrics.get("avgBFSTimeBEJT").get(nbVertices)+
			 (Long)metrics.get("avgBFSTimeOrder").get(nbVertices));

			 metrics.get("avgMinFillJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgMinFillJTwidth").get(nbVertices))/nbInstances);
			 metrics.get("avgDFSMaxAncJTwidth").put(nbVertices,
						((Integer) metrics.get("avgDFSMaxAncJTwidth").get(nbVertices))/ nbInstances);
			 metrics.get("avgDFSJTwidth").put(nbVertices,
			((Integer) metrics.get("avgDFSJTwidth").get(nbVertices))/ nbInstances);
			 metrics.get("avgBFSJTwidth").put(nbVertices,
			 ((Integer)metrics.get("avgBFSJTwidth").get(nbVertices))/nbInstances);
		}

		File dirOut = new File(dirOutName);
		if (!dirOut.exists())
			dirOut.mkdirs();

		HashMap<String, HashMap<String, HashMap<Integer, Number>>> dataPlots = new HashMap<String, HashMap<String, HashMap<Integer, Number>>>();

		dataPlots.put("avgTimeOrder",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTimeOrder").put("avgMinFillTimeOrder",
		 metrics.get("avgMinFillTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgDFSMaxAncTimeOrder",
				metrics.get("avgDFSMaxAncTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgDFSTimeOrder",
				metrics.get("avgDFSTimeOrder"));
		 dataPlots.get("avgTimeOrder").put("avgBFSTimeOrder",
		 metrics.get("avgBFSTimeOrder"));

		dataPlots.put("avgTimeBEJT",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgTimeBEJT").put("avgMinFillTimeBEJT",
		 metrics.get("avgMinFillTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgDFSMaxAncTimeBEJT",
					metrics.get("avgDFSMaxAncTimeBEJT"));
		dataPlots.get("avgTimeBEJT").put("avgDFSTimeBEJT",
				metrics.get("avgDFSTimeBEJT"));
		 dataPlots.get("avgTimeBEJT").put("avgBFSTimeBEJT",
		 metrics.get("avgBFSTimeBEJT"));

		dataPlots.put("avgTime",
				new HashMap<String, HashMap<Integer, Number>>());
		dataPlots.get("avgTime").put("avgMinFillTime",
		 metrics.get("avgMinFillTime"));
		dataPlots.get("avgTime").put("avgDFSMaxAncTime",
				metrics.get("avgDFSMaxAncTime"));
		dataPlots.get("avgTime").put("avgDFSTime",
				metrics.get("avgDFSTime"));
		 dataPlots.get("avgTime").put("avgBFSTime",
		 metrics.get("avgBFSTime"));

		dataPlots.put("avgJTWidth",
				new HashMap<String, HashMap<Integer, Number>>());
		 dataPlots.get("avgJTWidth").put("avgMinFillJTwidth",
		 metrics.get("avgMinFillJTwidth"));
		 
		 dataPlots.get("avgJTWidth").put("avgDFSMaxAncJTwidth",
					metrics.get("avgDFSMaxAncJTwidth"));
		 
		 dataPlots.get("avgJTWidth").put("avgDFSJTwidth",
				metrics.get("avgDFSJTwidth"));
		 dataPlots.get("avgJTWidth").put("avgBFSJTwidth",
		 metrics.get("avgBFSJTwidth"));

		String extraTitle = k + " step: " + stepNbVertices
				+ "  nbInstances " + nbInstances;
		for (String plotName : dataPlots.keySet()) {
			File dirPlot = new File(dirOut.getPath() + File.separator
					+k+File.separator+ plotName);
			if (dirPlot.exists())
				FileTools.recursiveDelete(dirPlot);
			dirPlot.mkdirs();
			HashMap<String, File> curveFiles = saveMetricsInFiles(
					dataPlots.get(plotName), dirPlot);
			writeDefault2DLinesPoints("nbVertices", plotName, curveFiles,
					dirPlot, extraTitle);
		}
		}

	}
	
	
	public static HyperGraph<Integer> grid(int nbL, int nbC){
		HyperGraph<Integer> hg = new HyperGraph<Integer>();
		for(int l=0;l<nbL;l++){
			for(int c=0;c<nbC;c++){
				//int ligne =nbC*l;
				if(c<nbC-1)
					hg.addHedge(l*nbC+c,l*nbC+c+1);
				if(l<nbL-1)
					hg.addHedge(l*nbC+c,(l*nbC)+nbC+c);
			}
		}
		
		return hg;
	}
	
	
	
	/**
	 * @param args
	 * @throws Exception
	 * 
	 * nohup java -Xmx2048m -Xms1024m -jar distribGraphDecomp -graphModel BA -nbInstances 10  -nbVerticesMin 20 -step 100 -nbVerticesMax 1020 -dirOutName C:/expe060312
	 */
	public static void main(String[] args) throws Exception {
		
//		tmp2DLinesPlot();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		ArgsHandler.setDefaultIntTo("nbInstances", 10, args, params);
		ArgsHandler.setDefaultIntTo("nbVerticesMin", 20,args, params);
		ArgsHandler.setDefaultIntTo("step",100 ,args, params);
		ArgsHandler.setDefaultIntTo("nbVerticesMax", 420,args, params);
		ArgsHandler.setDefaultParamTo("graphModel", "BA",args, params);
		
		ArgsHandler.setDefaultFileNameTo("dirOutName", "C:/Users/VinTo/workspace/CP2012/draws/expe270512-sep",
				args, params);
		
		System.out.println("params");
		for(String param : params.keySet()){
			System.out.println("  "+param+": "+params.get(param));
		}
		System.out.println();
		
		testRandomTE(params);
		
	}

}
