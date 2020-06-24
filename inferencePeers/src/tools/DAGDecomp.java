package tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import tools.DistribGraphDecomp.MsgLE;

public class DAGDecomp {

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
	
	public static <E extends Comparable<? super E>> void updateFillWeights(
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
			
			wv.setWeight(nbEdges);
			
		}
	}

	public static <E extends Comparable<E>> HashMap<E, HashSet<E>> fixMerger2Pred(
			HashMap<E, HashSet<E>> anc2preds, ArrayList<E> roots) {
		HashMap<E, HashSet<E>> fixRoot2Pred = new HashMap<E, HashSet<E>>();

		while (!anc2preds.isEmpty()) {
			E mostFamousRoot = null;
			int nbPredOfmostFamousRoot = -1;
			for (E anc : anc2preds.keySet()) {
				if (anc2preds.get(anc).size() > nbPredOfmostFamousRoot
						&& roots.contains(anc)) {
					mostFamousRoot = anc;
					nbPredOfmostFamousRoot = anc2preds.get(anc).size();
				}
			}

			fixRoot2Pred.put(mostFamousRoot, anc2preds.get(mostFamousRoot));
			anc2preds.remove(mostFamousRoot);
			ArrayList<E> toRem = new ArrayList<E>();
			for (E anc : anc2preds.keySet()) {
				anc2preds.get(anc).removeAll(fixRoot2Pred.get(mostFamousRoot));
				if (anc2preds.get(anc).isEmpty()) {
					toRem.add(anc);
				}
			}

			for (E e : toRem)
				anc2preds.remove(e);
		}
		return fixRoot2Pred;
	}

	/*
	 * builds a tree 
	 * hg input parmeter
	 * tree output 
	 * leaves output
	 * result root
	 */
	public static <E extends Comparable<E>> E rootedTree1MaxFillBFS(HyperGraph<E> hg,HashMap<E,E> tree,
			ArrayList<E> leaves) {
		HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
		
		for(E e: hg.getVertices()){
			whg.addVertex(new WeightedElmt<E>(e));
		}
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for (E v : he)
				whedge.add(new WeightedElmt<E>(v));
			whg.addHedge(whedge);
		}
		
		updateFillInWeights(whg.getVertices(), whg);
//		System.out.println(" updateFillInWeights : "+ whg);
		WeightedElmt<E> maxElmts =Collections.max(whg.getVertices());
		
		E root = maxElmts._elmt;
		
//		System.out.println(" maxElmt: "+ root);
		
		tree.put(root, root);
		
		ArrayList<E> toVisit = new ArrayList<E>();
		ArrayList<E> visited = new ArrayList<E>();
		toVisit.add(root);
		while(!toVisit.isEmpty()){
			E cur = toVisit.remove(0);
			visited.add(cur);
			for(E neigh :hg.getNeighbors(cur)){
				if(! toVisit.contains(neigh)&& ! visited.contains(neigh)){
					tree.put(neigh, cur);
					toVisit.add(neigh);	
				}
				
				
			}
			
		}
		
		leaves.addAll(tree.keySet());
		leaves.removeAll(tree.values());
		tree.put(root, root);
		
		return root;
	}
	
	public static <E extends Comparable<E>> void multiRootedDAG3(
			HyperGraph<E> hg, HashMap<E, ArrayList<E>> successorsOf,
			HashMap<E, ArrayList<E>> predecessorsOf) {
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
					if (whg.getNeighbors(wv).size() > 0) {
						if(!predecessorsOf.containsKey(wv.getElement()))
							predecessorsOf.put(wv.getElement(), new ArrayList<E>());
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
			for (WeightedElmt<E> maxE : maxElmts) {
				whg.removeVertex(maxE);
			}
			
			
			
			updateFillInWeights(v2Update, whg);
			v2Update.clear();
			ArrayList<WeightedElmt<E>> minElmts = new ArrayList<WeightedElmt<E>>();
			for (WeightedElmt<E> wv : whg.getVertices()) {
				boolean isMinInNeighborhood = true;
				for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
					if (wv.compareTo(wn) > 0) {
						isMinInNeighborhood = false;
						break;
					}
					for (WeightedElmt<E> wnn : whg.getNeighbors(wn)) {
						if (wv.compareTo(wnn) > 0) {
							isMinInNeighborhood = false;
							break;
						}
					}
					if (isMinInNeighborhood == false) {
						break;
					}
				}
				if (isMinInNeighborhood == true) {
					minElmts.add(wv);
					if (whg.getNeighbors(wv).size() > 0) {
						if(!successorsOf.containsKey(wv.getElement()))
							successorsOf.put(wv.getElement(), new ArrayList<E>());
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
			for (WeightedElmt<E> minE : minElmts) {
				whg.removeVertex(minE);
			}
			
		}

		// System.out.println("predecessors ");
		// for(E elmt : predecessorsOf.keySet()){
		// System.out.print(elmt+" preds");
		// for(E pred : predecessorsOf.get(elmt))
		// System.out.print(" "+pred);
		// System.out.println();
		// }

		// System.out.println("successors ");
		// for(E elmt : successorsOf.keySet()){
		// System.out.print(elmt+" succs");
		// for(E succ : successorsOf.get(elmt))
		// System.out.print(" "+succ);
		// System.out.println();
		// }

	}
	
	
	public static <E extends Comparable<E>> void multiRootedDAG21(
			HyperGraph<E> hg, HashMap<E, ArrayList<E>> successorsOf,
			HashMap<E, ArrayList<E>> predecessorsOf) {
		// transforms the hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for (E v : he)
				whedge.add(new WeightedElmt<E>(v));
			whg.addHedge(whedge);
		}

		// build the forest of trees where each tree has
		// as root a maxFill weighted node
		HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(
				whg.getVertices());
		
		// make 1 iteration to identify distrib node
		
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
//					for (WeightedElmt<E> wnn : whg.getNeighbors(wn)) {
//						if (wv.compareTo(wnn) < 0) {
//							isMaxInNeighborhood = false;
//							break;
//						}
//					}
//					if (isMaxInNeighborhood == false) {
//						break;
//					}
				}
				if (isMaxInNeighborhood == true) {
					maxElmts.add(wv);
					if (whg.getNeighbors(wv).size() > 0) {
						predecessorsOf.put(wv.getElement(), new ArrayList<E>());
						for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
							predecessorsOf.get(wv.getElement()).add(
									wn.getElement());
							if (!successorsOf.containsKey(wn.getElement()))
								successorsOf.put(wn.getElement(),
										new ArrayList<E>());
							successorsOf.get(wn.getElement()).add(
									wv.getElement());
						}

						
					}

				}
			}
			for (WeightedElmt<E> maxE : maxElmts) {
				whg.removeVertex(maxE);
			}
			
			
			
			
			updateFillInWeights(v2Update, whg);
			v2Update.clear();
			ArrayList<WeightedElmt<E>> minElmts = new ArrayList<WeightedElmt<E>>();
			for (WeightedElmt<E> wv : whg.getVertices()) {
				boolean isMinInNeighborhood = true;
				for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
					if (wv.compareTo(wn) > 0) {
						isMinInNeighborhood = false;
						break;
					}
					for (WeightedElmt<E> wnn : whg.getNeighbors(wn)) {
						if (wv.compareTo(wnn) > 0) {
							isMinInNeighborhood = false;
							break;
						}
					}
					if (isMinInNeighborhood == false) {
						break;
					}
				}
				if (isMinInNeighborhood == true) {
					minElmts.add(wv);
					
				}
			}
			
			
			// bfs continuation
			while (!whg.getVertices().isEmpty()) {
				WeightedElmt<E> wv =minElmts.remove(0);
				if (whg.getNeighbors(wv).size() > 0) {
					if (!successorsOf.containsKey(wv.getElement()))
						successorsOf.put(wv.getElement(), new ArrayList<E>());
					for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
						
							successorsOf.get(wv.getElement()).add(
								wn.getElement());
							if (!predecessorsOf.containsKey(wn.getElement()))
								predecessorsOf.put(wn.getElement(),
									new ArrayList<E>());
							predecessorsOf.get(wn.getElement()).add(
								wv.getElement());
							if(!minElmts.contains(wn)){
								minElmts.add(wn);
							}
					}
				}
				whg.removeVertex(wv);
			}
			
			
			
			
			for (WeightedElmt<E> minE : minElmts) {
				whg.removeVertex(minE);
			}
		
			
		

		// System.out.println("predecessors ");
		// for(E elmt : predecessorsOf.keySet()){
		// System.out.print(elmt+" preds");
		// for(E pred : predecessorsOf.get(elmt))
		// System.out.print(" "+pred);
		// System.out.println();
		// }

		// System.out.println("successors ");
		// for(E elmt : successorsOf.keySet()){
		// System.out.print(elmt+" succs");
		// for(E succ : successorsOf.get(elmt))
		// System.out.print(" "+succ);
		// System.out.println();
		// }

	}
	
	
	public static <E extends Comparable<E>> void multiRootedDAG2(
			HyperGraph<E> hg, HashMap<E, ArrayList<E>> successorsOf,
			HashMap<E, ArrayList<E>> predecessorsOf) {
		// transforms the hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for (E v : he)
				whedge.add(new WeightedElmt<E>(v));
			whg.addHedge(whedge);
		}

		// build the forest of trees where each tree has
		// as root a maxFill weighted node
		HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(
				whg.getVertices());
		
		// make 1 iteration to identify distrib node
		
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
					if (whg.getNeighbors(wv).size() > 0) {
						predecessorsOf.put(wv.getElement(), new ArrayList<E>());
						for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
							predecessorsOf.get(wv.getElement()).add(
									wn.getElement());
							if (!successorsOf.containsKey(wn.getElement()))
								successorsOf.put(wn.getElement(),
										new ArrayList<E>());
							successorsOf.get(wn.getElement()).add(
									wv.getElement());
						}

						
					}

				}
			}
			for (WeightedElmt<E> maxE : maxElmts) {
				whg.removeVertex(maxE);
			}
			
			
			v2Update.clear();
			v2Update.addAll(whg.getVertices());
			updateFillWeights(v2Update, whg);
			maxElmts = new ArrayList<WeightedElmt<E>>();
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
					
				}
			}
			
			
			// bfs continuation
			while (!whg.getVertices().isEmpty()) {
				WeightedElmt<E> wv =maxElmts.remove(0);
				if (whg.getNeighbors(wv).size() > 0) {
					if (!successorsOf.containsKey(wv.getElement()))
						successorsOf.put(wv.getElement(), new ArrayList<E>());
					for (WeightedElmt<E> wn : whg.getNeighbors(wv)) {
						
							successorsOf.get(wv.getElement()).add(
								wn.getElement());
							if (!predecessorsOf.containsKey(wn.getElement()))
								predecessorsOf.put(wn.getElement(),
									new ArrayList<E>());
							predecessorsOf.get(wn.getElement()).add(
								wv.getElement());
							if(!maxElmts.contains(wn)){
								maxElmts.add(wn);
							}
					}
				}
				whg.removeVertex(wv);
			}
			
			
			
			
			for (WeightedElmt<E> maxE : maxElmts) {
				whg.removeVertex(maxE);
			}
		
			
		

		// System.out.println("predecessors ");
		// for(E elmt : predecessorsOf.keySet()){
		// System.out.print(elmt+" preds");
		// for(E pred : predecessorsOf.get(elmt))
		// System.out.print(" "+pred);
		// System.out.println();
		// }

		// System.out.println("successors ");
		// for(E elmt : successorsOf.keySet()){
		// System.out.print(elmt+" succs");
		// for(E succ : successorsOf.get(elmt))
		// System.out.print(" "+succ);
		// System.out.println();
		// }

	}
	
	
	public static <E extends Comparable<E>> void multiRootedDAG(
			HyperGraph<E> hg, HashMap<E, ArrayList<E>> successorsOf,
			HashMap<E, ArrayList<E>> predecessorsOf) {
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
					if (whg.getNeighbors(wv).size() > 0) {
						predecessorsOf.put(wv.getElement(), new ArrayList<E>());
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
			for (WeightedElmt<E> maxE : maxElmts) {
				whg.removeVertex(maxE);
			}
		}

		// System.out.println("predecessors ");
		// for(E elmt : predecessorsOf.keySet()){
		// System.out.print(elmt+" preds");
		// for(E pred : predecessorsOf.get(elmt))
		// System.out.print(" "+pred);
		// System.out.println();
		// }

		// System.out.println("successors ");
		// for(E elmt : successorsOf.keySet()){
		// System.out.print(elmt+" succs");
		// for(E succ : successorsOf.get(elmt))
		// System.out.print(" "+succ);
		// System.out.println();
		// }

	}


	
	
	// anc2predsOf.get(e).get(anc) return for e, the list of Merger Ancestors
	// that can reach anc
	public static <E extends Comparable<E>> 
		HashMap<E, HashMap<E, HashSet<E>>> ancMerg2predsOf(
				Set<E> vertices,
				ArrayList<E> roots,
				HashMap<E, ArrayList<E>> successorsOf,
				HashMap<E, ArrayList<E>> predecessorsOf,
				ArrayList<E> mergers ){
		
		// A merger have 1 or 0 predecessor. s.t when a merger have a predecessor,
		// it predecessor is itself a merger
		
		ArrayList<E> mergersToVisit = new ArrayList<E>(roots);
		while(!mergersToVisit.isEmpty()){
			E curMerger = mergersToVisit.remove(0);
			mergers.add(curMerger);
			for( E succCurMerger : successorsOf.get(curMerger)){
				if(predecessorsOf.get(succCurMerger).size()<=1)
					mergersToVisit.add(succCurMerger);
			}
		}
		
		
		
		// ancestorsOf.get(e).get(pred) return the set of ancestors reachable
		// from e by pred
		HashMap<E, HashMap<E, HashSet<E>>> ancestorsOf = new HashMap<E, HashMap<E, HashSet<E>>>();
		for (E e : vertices) {
			ancestorsOf.put(e, new HashMap<E, HashSet<E>>());
			if (predecessorsOf.containsKey(e)) {
				for (E pred : predecessorsOf.get(e))
					ancestorsOf.get(e).put(pred, new HashSet<E>());
			}
		}

		ArrayList<E> toVisit = new ArrayList<E>();
		toVisit.addAll(roots);
		while (!toVisit.isEmpty()) {
			E curE = toVisit.remove(0);

			if (successorsOf.containsKey(curE))
				for (E succOfCur : successorsOf.get(curE)) {
					if (!toVisit.contains(succOfCur))
						toVisit.add(succOfCur);
				}
			if (predecessorsOf.containsKey(curE)) {
				for (E predOfCur : predecessorsOf.get(curE)) {
					ancestorsOf.get(curE).get(predOfCur).add(predOfCur);
					for (E predOfPred : ancestorsOf.get(predOfCur).keySet())
						ancestorsOf
								.get(curE)
								.get(predOfCur)
								.addAll(ancestorsOf.get(predOfCur).get(
										predOfPred));
				}
			}
		}
		
		
//		System.out
//				.println(" ancestorsOf.get(e).get(pred) return ancestors reachable from e by pred ");
//		for (E e : ancestorsOf.keySet()) {
//			System.out.print("  " + e);
//			for (E predE : ancestorsOf.get(e).keySet()) {
//				System.out.print(" pred -> " + predE + " : ");
//				for (E v : ancestorsOf.get(e).get(predE))
//					System.out.print(" " + v);
//			}
//			System.out.println();
//		}

		
		HashMap<E, HashMap<E, HashSet<E>>> ancMerg2predsOf = new HashMap<E, HashMap<E, HashSet<E>>>();
		for (E e :vertices) {
			ancMerg2predsOf.put(e, new HashMap<E, HashSet<E>>());
		}

		for (E e : ancestorsOf.keySet()) {
			for (E predOfE : ancestorsOf.get(e).keySet()) {
				for (E ancBypredOfE : ancestorsOf.get(e).get(predOfE)) {
					if(mergers.contains(ancBypredOfE)){
					if (!ancMerg2predsOf.get(e).containsKey(ancBypredOfE)) {
						ancMerg2predsOf.get(e).put(ancBypredOfE, new HashSet<E>());
					}
					ancMerg2predsOf.get(e).get(ancBypredOfE).add(predOfE);
					}
				}
			}
		}

//		System.out
//				.println("anc2predsOf.get(e).get(anc) return for e, the list of predecessors that can reach anc ");
//		for (E e : ancMerg2predsOf.keySet()) {
//			System.out.print("  " + e);
//			for (E predE : ancMerg2predsOf.get(e).keySet()) {
//				System.out.print(" pred -> " + predE + " : ");
//				for (E v : ancMerg2predsOf.get(e).get(predE))
//					System.out.print(" " + v);
//			}
//			System.out.println();
//		}

		
		return ancMerg2predsOf;
	}
	
	// propagate elements from leaves to roots
	public static  <E extends Comparable<E>> void bottomUpPropagation(
			HyperGraph<E> gShared,
			HashMap<E,E> treeOfShared,
			HashMap<E,HashMap<E,ArrayList<E>>> fatherAndBuckOf,
			ArrayList<E> leaves){
		
		
//		System.out.println(" Inside bottomUpPropagation ");
		
		ArrayList<E> toVisit = new ArrayList<E>(leaves);
		ArrayList<E> visited = new ArrayList<E>();
		while(!toVisit.isEmpty()){
//			System.out.println(" toVisit: "+toVisit);
			E cur = toVisit.remove(0);
			
			E predCur = treeOfShared.get(cur);
			if( !predCur.equals(cur) && !toVisit.contains(predCur)){
				toVisit.add(predCur);
			}
			
			if(!fatherAndBuckOf.containsKey(cur)){
				fatherAndBuckOf.put(cur, new HashMap<E, ArrayList<E>>());
				fatherAndBuckOf.get(cur).put(predCur, new ArrayList<E>());
				for(E neigh : (gShared.getNeighbors(cur)))
						fatherAndBuckOf.get(cur).get(predCur).add(neigh);
			}
			
			E predPredCur = treeOfShared.get(predCur);
			for(E e :fatherAndBuckOf.get(cur).get(predCur)){
				if(!fatherAndBuckOf.containsKey(predCur)){
					fatherAndBuckOf.put(predCur, new HashMap<E, ArrayList<E>>());
					fatherAndBuckOf.get(predCur).put(predPredCur, new ArrayList<E>());
					for(E neigh : (gShared.getNeighbors(predCur)))
						if(!visited.contains(neigh) && !toVisit.contains(neigh))
							fatherAndBuckOf.get(predCur).get(predPredCur).add(neigh);
					
				}
				if(!fatherAndBuckOf.get(predCur).get(predPredCur).contains(e)){	
					fatherAndBuckOf.get(predCur).get(predPredCur).add(e);
				}
			}
			
			visited.add(cur);
			
		}
		
		for(E cur : treeOfShared.keySet()){
			if(!fatherAndBuckOf.containsKey(cur))
				fatherAndBuckOf.put(cur, new HashMap<E, ArrayList<E>>());
			if(!fatherAndBuckOf.get(cur).containsKey(treeOfShared.get(cur)))
				fatherAndBuckOf.get(cur).put(treeOfShared.get(cur), new ArrayList<E>());
			if(!fatherAndBuckOf.get(cur).get(treeOfShared.get(cur)).contains(cur))
				fatherAndBuckOf.get(cur).get(treeOfShared.get(cur)).add(cur);
		}
		
	}
	
	// remove unecessary elements from root to leaves
	public static  <E extends Comparable<E>> void topDownProjection(
			HashMap<E,E> treeOfShared,
			HashMap<E,HashMap<E,ArrayList<E>>> fatherAndBuckOf,
			E root){
		
//		System.out.println("Inside topDownProjection");
		
		// from root to leaves if an element is in only one child 
		// it can be remove from the root
		
		// computing the children
		HashSet<E> children = new HashSet<E>();
		for( E e :treeOfShared.keySet()){
			if(treeOfShared.get(e).equals(root) && !e.equals(root))
				children.add(e);
		}
		
//		System.out.println(" children of: "+ root+ ": "+children);
		
		ArrayList<E> e2Rem = new ArrayList<E>();
		for(E e : fatherAndBuckOf.get(root).get(treeOfShared.get(root))){
			
			 E childWithE = null;
			 int nbChildWithE = 0;
			for(E c : children){
				if(fatherAndBuckOf.get(c).get(root).contains(e) && !root.equals(e)){
					nbChildWithE ++;
					childWithE = c;
				}
			}
			if(nbChildWithE ==1){
//				System.out.println(" elmt: "+ e+ " will be remove "+children);
				e2Rem.add(e);
				topDownProjection(
						treeOfShared,fatherAndBuckOf,childWithE);
			}
		}
		fatherAndBuckOf.get(root).get(treeOfShared.get(root)).removeAll(e2Rem);
		
	}


	public static <E extends Comparable<E>> HyperGraph<E> distribJT2(
			HyperGraph<E> hg, HyperGraph<AbstCompArrayList<E>> dualHjt) {


		HashMap<E, ArrayList<E>> successorsOf = new HashMap<E, ArrayList<E>>();
		HashMap<E, ArrayList<E>> predecessorsOf = new HashMap<E, ArrayList<E>>();

		// transform te input hg in a rooted HyperGraph
		multiRootedDAG21(hg, successorsOf, predecessorsOf);

//		System.out.println("successors ");
//		for(E elmt : successorsOf.keySet()){
//			System.out.print(elmt+" succs");
//			for(E succ : successorsOf.get(elmt))
//				System.out.print(" "+succ);
//			System.out.println();
//		}
//		
//		System.out.println("predecessors ");
//		for(E elmt : predecessorsOf.keySet()){
//			System.out.print(elmt+" preds");
//			for(E pred : predecessorsOf.get(elmt))
//				System.out.print(" "+pred);
//			System.out.println();
//		}
		
		
		HashMap<E, HashMap<E,ArrayList<E>>> fatherAndBuckOf =
			new HashMap<E, HashMap<E,ArrayList<E>>>();
		
		// save the roots Elements
		ArrayList<E> roots = new ArrayList<E>();
		for (E v : successorsOf.keySet()) {
			if (!predecessorsOf.containsKey(v))
				roots.add(v);
		}
		
//		 System.out.print("first roots ");
//		 for (E r : roots) {
//			 System.out.print(" " + r);
//		 }
//		 System.out.println();
		
		// To identify leaves easily we 
		for (E v : predecessorsOf.keySet()) {
			if (!successorsOf.containsKey(v))
				successorsOf.put(v, new ArrayList<E>());
		}
		
		
		
		ArrayList<E> mergers = new ArrayList<E>();
		HashMap<E, HashMap<E, HashSet<E>>> ancMerg2predsOf = ancMerg2predsOf(
					hg.getVertices(),roots,successorsOf,predecessorsOf,mergers);
		
//		System.out.println(" mergers "+ mergers );
//		
//		 System.out.println(" \n ancMerg2predsOf");
//		for (E e : ancMerg2predsOf.keySet()) {
//			System.out.print("vertex " + e);
//			for (E ancE : ancMerg2predsOf.get(e).keySet()) {
//				System.out.print("\n anc -> " + ancE + "  reachable by: ");
//				for (E v : ancMerg2predsOf.get(e).get(ancE))
//					System.out.print(" " + v);
//			}
//			System.out.println();
//		}
		
		
		
		// msgBox.get(recipient).get(fixPoint) contains element the have to
		// be propagated to fixpoint 
		HashMap<E,HashMap<E,ArrayList<E>>> msgBox =
			new HashMap<E,HashMap<E,ArrayList<E>>>();
		
		
		
		
		// from leaves to roots we propagate elements respecting the running
		// intersection. The bottom up propagation of a leaf starts by its
		// predecessors
		// then follows the last predecessor link.
		// The propagation stop either a root is reach or all the paths
		// from a leaf converge
//		int it =0;
		while ( !successorsOf.isEmpty()) {
		
			ArrayList<E> leaves = new ArrayList<E>();
			ArrayList<E> notLeaves = new ArrayList<E>();
			
			for (E v : successorsOf.keySet()) {
				if (successorsOf.get(v).isEmpty())
					leaves.add(v);
				else
					notLeaves.add(v);
				
			}

//			System.out.print(" leaves ");
//			for (E l : leaves) {
//				System.out.print(" " + l);
//			}
//			System.out.println();
//			System.out.print(" NOT  leaves ");
//			for (E l : notLeaves) {
//				System.out.print(" " + l);
//			}
//			System.out.println();
//			
//			System.out.println(" msgBox BEFORE THE TRANSFER " );
//			for (E e : msgBox.keySet()) {
//				System.out.print("  dest: " + e);
//				for (E fp : msgBox.get(e).keySet()) {
//					System.out.print("\n   stop at -> " + fp + "  msg : ");
//					for (E v : msgBox.get(e).get(fp))
//						System.out.print(" " + v);
//				}
//				System.out.println();
//			}
			
			// propagate each leaf
			for (E leaf : leaves) {
				// transfer received message that does not stop at leaf
				if(msgBox.containsKey(leaf)){
					ArrayList<E> fp2Rem = new ArrayList<E>();
					for(E fp : msgBox.get(leaf).keySet()){
						if(!fp.equals(leaf)){
							// randomly choose a pred that can foward the msg to the fixPoint
							ArrayList<E> preds = new ArrayList<E>(ancMerg2predsOf.get(leaf).get(fp));
							int i =(int)(preds.size()* Math.random());
							E pred = (preds.get(i));
							
							// add the rcvd msg to the list of msg of pred
							if(!msgBox.containsKey(pred))
								msgBox.put(pred, new HashMap<E, ArrayList<E>>());
							if(!msgBox.get(pred).containsKey(fp))
								msgBox.get(pred).put(fp, new ArrayList<E>());
							
							for( E rcvdE : msgBox.get(leaf).get(fp)){
								if(!msgBox.get(pred).get(fp).contains(rcvdE))
									msgBox.get(pred).get(fp).add(rcvdE);
								if(mergers.contains(leaf)){
									if(!fatherAndBuckOf.containsKey(leaf))
										fatherAndBuckOf.put(leaf, new HashMap<E, ArrayList<E>>());
									if(!fatherAndBuckOf.get(leaf).containsKey(pred))
										fatherAndBuckOf.get(leaf).put(pred,new ArrayList<E>());
									if(!fatherAndBuckOf.get(leaf).get(pred).contains(rcvdE))
										fatherAndBuckOf.get(leaf).get(pred).add(rcvdE);
								}
							}
							
							
							
							fp2Rem.add(fp);
						}
					}
					for(E fp : fp2Rem)
						msgBox.get(leaf).remove(fp);
				}
				
				
				if(!mergers.contains(leaf)){
					// CASE of a "repartitor send its elmt to Best Merger"
					HashMap<E, HashSet<E>> fixPoint2Pred = fixMerger2Pred(
							ancMerg2predsOf.get(leaf), roots);
					for(E fp : fixPoint2Pred.keySet()){
						for(E pred :fixPoint2Pred.get(fp)){
							if(!msgBox.containsKey(pred))
								msgBox.put(pred, new HashMap<E, ArrayList<E>>());
							if(!msgBox.get(pred).containsKey(fp))
								msgBox.get(pred).put(fp, new ArrayList<E>());
							if(!msgBox.get(pred).get(fp).contains(leaf))
								msgBox.get(pred).get(fp).add(leaf);
							
						}
					}
				}else{
					// Case of a "Merger" compose its buckets with the received voc
//					if(predecessorsOf.get(leaf).size()>1)
//						System.out.println(" ERROR predecessorsOf.get(leaf).size()>1 ");
					
					if(!fatherAndBuckOf.containsKey(leaf))
						fatherAndBuckOf.put(leaf, new HashMap<E, ArrayList<E>>());
					
					// roots are their own predecessor
					E pred = roots.contains(leaf)? leaf:predecessorsOf.get(leaf).get(0);
					if(!fatherAndBuckOf.get(leaf).containsKey(pred))
						fatherAndBuckOf.get(leaf).put(pred,new ArrayList<E>());
					if(!fatherAndBuckOf.get(leaf).get(pred).contains(leaf))
						fatherAndBuckOf.get(leaf).get(pred).add(leaf);
					if(msgBox.containsKey(leaf)){
					for(E fp : msgBox.get(leaf).keySet()){
						for( E eRcvd :msgBox.get(leaf).get(fp)){
							if(!fatherAndBuckOf.get(leaf).get(pred).contains(eRcvd)){
								fatherAndBuckOf.get(leaf).get(pred).add(eRcvd);
							}
						}
					}
					}
				}
			}
			
//			System.out.println(" fatherAndBuckOf " );
//			for (E e : fatherAndBuckOf.keySet()) {
//				System.out.print("  vertex: " + e);
//				for (E fatherE : fatherAndBuckOf.get(e).keySet()) {
//					System.out.print("\n   father -> " + fatherE + "  bucket : ");
//					for (E v : fatherAndBuckOf.get(e).get(fatherE))
//						System.out.print(" " + v);
//				}
//				System.out.println();
//			}
			
			for (E leaf : leaves) {
				successorsOf.remove(leaf);
			}

			
			for (E e : successorsOf.keySet()) {
				ArrayList<E> leavesToRem = new ArrayList<E>();
				for (E succE : successorsOf.get(e)) {
					if (leaves.contains(succE))
						leavesToRem.add(succE);

				}
				successorsOf.get(e).removeAll(leavesToRem);
			}
			
		} // end of the propagation
		
		
//	
//		System.out.println(" \n After DAG Propagation fatherAndBuckOf " );
//		for (E e : fatherAndBuckOf.keySet()) {
//			System.out.print("vertex: " + e);
//			for (E fatherE : fatherAndBuckOf.get(e).keySet()) {
//				System.out.print("\n father -> " + fatherE + "  bucket : ");
//				for (E v : fatherAndBuckOf.get(e).get(fatherE))
//					System.out.print(" " + v);
//			}
//			System.out.println();
//		}
		
		// Instead of considering a HyperGraph of elements we build a
		// the graph of shared elements where Hyper edges are replaced by
		// fully connected graph, the tree with does not changed.
		
		HashMap<E,HashSet<E>> formerRoot2Shared = new HashMap<E, HashSet<E>>();
		HyperGraph<E> gShared = new HyperGraph<E>();
		for(E r  :roots){
			ArrayList<E> buck =fatherAndBuckOf.get(r).get(r);
			for(int i=0;i<buck.size();i++){
				for(E r2 :roots){
					if(!r2.equals(r) &&
							fatherAndBuckOf.get(r2).get(r2).contains(buck.get(i))){
						gShared.addVertex(buck.get(i));
						break;
					}
				}
			}
		}
		
		if(!gShared.getVertices().isEmpty()){
		
		for(E s :gShared.getVertices() ){
			for(E r  :roots){
				ArrayList<E> buck =fatherAndBuckOf.get(r).get(r);
				if(buck.contains(s)){
					if(!formerRoot2Shared.containsKey(r))
						formerRoot2Shared.put(r, new HashSet<E>());
					if(!formerRoot2Shared.get(r).contains(s))
						formerRoot2Shared.get(r).add(s);
				}
			}
		}
		
		for( E fr :formerRoot2Shared.keySet()){
			ArrayList<E> alBuckFR = new ArrayList<E>(formerRoot2Shared.get(fr));
			for(int i1 = 0;i1< alBuckFR.size()-1;i1++){
				for(int i2 = i1;i2< alBuckFR.size();i2++){
					gShared.addHedge(alBuckFR.get(i1),alBuckFR.get(i2));
				}
			}
		}

		
//		System.out.println("\n formerRoot2Shared " );
//		for (E e : formerRoot2Shared.keySet()) {
//			System.out.print("  former root: " + e+ " shared:");
//			for (E sharedE : formerRoot2Shared.get(e)) {
//				System.out.print(" " + sharedE );
//			}
//			System.out.println();
//		}
//		
//		System.out.println("\n gShared "+gShared );
		
		
		
		HashMap<E,E> treeOfShared = new HashMap<E,E>();
		ArrayList<E> leaves = new ArrayList<E>();
		E nvRoot =rootedTree1MaxFillBFS(gShared,treeOfShared, leaves);
		
//		System.out.println();
//		System.out.println("\n treeOfShared: root "+nvRoot+" leaves: "+leaves );
//		for (E e : treeOfShared.keySet()) {
//			System.out.print("  vertex: " + e+ " father:");
//			System.out.print(" " +  treeOfShared.get(e) );
//			System.out.println();
//		}
		
		
		
		
		bottomUpPropagation(gShared,treeOfShared,fatherAndBuckOf, leaves);
		
		
//		System.out.println(" \n After bottomUpPropagation fatherAndBuckOf " );
//		for (E e : fatherAndBuckOf.keySet()) {
//			System.out.print("vertex: " + e);
//			for (E fatherE : fatherAndBuckOf.get(e).keySet()) {
//				System.out.print("\n father -> " + fatherE + "  bucket : ");
//				for (E v : fatherAndBuckOf.get(e).get(fatherE))
//					System.out.print(" " + v);
//			}
//			System.out.println();
//		}
		
//		boolean stop = true;
//		if(stop)
//			return null;
//		
		topDownProjection(treeOfShared,fatherAndBuckOf, nvRoot);
		
//		System.out.println(" \n After topDownProjection fatherAndBuckOf " );
//		for (E e : fatherAndBuckOf.keySet()) {
//			System.out.print("vertex: " + e);
//			for (E fatherE : fatherAndBuckOf.get(e).keySet()) {
//				System.out.print("\n father -> " + fatherE + "  bucket : ");
//				for (E v : fatherAndBuckOf.get(e).get(fatherE))
//					System.out.print(" " + v);
//			}
//			System.out.println();
//		}
		
		
		// we link former roots with new One by their shared elmts
		for(E fr : formerRoot2Shared.keySet()){
			for(E sh : formerRoot2Shared.get(fr)){
				if(fatherAndBuckOf.get(sh).get(treeOfShared.get(sh)).
						containsAll(formerRoot2Shared.get(fr))){
					fatherAndBuckOf.get(fr).put(sh, 
							fatherAndBuckOf.get(fr).get(fr));
					fatherAndBuckOf.get(fr).remove(fr);
					break;
				}
			}
		}
		
		
//		System.out.println(" \n After  link with former rootsfatherAndBuckOf " );
//		for (E e : fatherAndBuckOf.keySet()) {
//			System.out.print("vertex: " + e);
//			for (E fatherE : fatherAndBuckOf.get(e).keySet()) {
//				System.out.print("\n father -> " + fatherE + "  bucket : ");
//				for (E v : fatherAndBuckOf.get(e).get(fatherE))
//					System.out.print(" " + v);
//			}
//			System.out.println();
//		}
		
		}
		HyperGraph<E> hjt1 = new HyperGraph<E>();
		for(E v : fatherAndBuckOf.keySet()){
			E predV = (new ArrayList<E>(fatherAndBuckOf.get(v).keySet())).get(0);
				hjt1.addHedge(fatherAndBuckOf.get(v).get(predV));
				// if its not the root  only for dualHJT
				if(!v.equals(predV)){
					E  predPredV = (new ArrayList<E>(fatherAndBuckOf.get(predV).keySet())).get(0);
					
					ArrayListComp<E> vBuck = 
						new ArrayListComp<E>(fatherAndBuckOf.get(v).get(predV));
					if(vBuck.isEmpty() || vBuck.contains(null))
						continue;
					
					ArrayListComp<E> vPredBuck =
						new ArrayListComp<E>(fatherAndBuckOf.get(predV).get(predPredV));
					if(vPredBuck.isEmpty() || vPredBuck.contains(null))
						continue;
					
					if(!vBuck.equals(vPredBuck))
						dualHjt.addHedge(vBuck, vPredBuck);
				}
			
		}
		

		return hjt1;
	}
	
	
	
	
	
	
	
	
	
	
	
	private static <E> void min_predsCur_Bucks_Ancs(int ipred,
			ArrayList<ArrayList<ArrayList<Integer>>> predsCur_Bucks_iBuckAncs,
			int[] cur_predsCur_Bucks_Ancs,
			int[] min_predsCur_Bucks_Ancs,
			int[] val_Min,
			ArrayList<ArrayList<E>> bucks) {
		
		if(ipred >= predsCur_Bucks_iBuckAncs.size()){
			// Now I have to select the min
			
			int nbDiffTop=0;
			HashSet<Integer> hsItop = new HashSet<Integer>(); 
			for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
				int iLast = predsCur_Bucks_iBuckAncs.get(ip).size()-1;
				hsItop.add(predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]).get(iLast));
			}
			nbDiffTop = hsItop.size();
			
			
			int maxTw = 0;
			for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
				for(Integer ib : predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip])){
					if( bucks.get(ib).size()>maxTw )
						maxTw = bucks.get(ib).size();
				}
			}
			
			
			int nbBucksProp = 0;
			HashSet<Integer> hsDiffBucks = new HashSet<Integer>();
			for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
				if(hsDiffBucks.isEmpty()){
					hsDiffBucks.addAll(predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]));
				}else{
					ArrayList<Integer> aTmp = new ArrayList<Integer>(predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]));
					aTmp.removeAll(hsDiffBucks);
					hsDiffBucks.removeAll(predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]));
					hsDiffBucks.addAll(aTmp);
				}
			}
			nbBucksProp = hsDiffBucks.size();
			
			if(nbDiffTop>val_Min[0])
				return;
			if(nbDiffTop<val_Min[0]){
				val_Min[0] = nbDiffTop;
				val_Min[1] = maxTw;
				val_Min[2] = nbBucksProp;
				for(int k=0;k < cur_predsCur_Bucks_Ancs.length; k++)
					cur_predsCur_Bucks_Ancs[k] = 
						min_predsCur_Bucks_Ancs[k];
				return;
			}
			
			if(maxTw>val_Min[1])
				return;
			if(maxTw<val_Min[1]){
				val_Min[0] = nbDiffTop;
				val_Min[1] = maxTw;
				val_Min[2] = nbBucksProp;
				for(int k=0;k < cur_predsCur_Bucks_Ancs.length; k++)
					cur_predsCur_Bucks_Ancs[k] = 
						min_predsCur_Bucks_Ancs[k];
				return;
			}
			
			if(nbBucksProp>val_Min[2])
				return;
			if(nbBucksProp<val_Min[2]){
				val_Min[0] = nbDiffTop;
				val_Min[1] = maxTw;
				val_Min[2] = nbBucksProp;
				for(int k=0;k < cur_predsCur_Bucks_Ancs.length; k++)
					cur_predsCur_Bucks_Ancs[k] = 
						min_predsCur_Bucks_Ancs[k];
			}
			
			return;
		}
		for(int k=0;k< predsCur_Bucks_iBuckAncs.get(ipred).size();k++){
			cur_predsCur_Bucks_Ancs[ipred]=k;
			min_predsCur_Bucks_Ancs(ipred+1,predsCur_Bucks_iBuckAncs
					, cur_predsCur_Bucks_Ancs, min_predsCur_Bucks_Ancs,val_Min, bucks);
		}
	}

	public static <E extends Comparable<E>> void distribJT3(
			HyperGraph<E> hg, HyperGraph<ArrayListComp<E>> dualHjt) {

		HashMap<E, ArrayList<E>> successorsOf = new HashMap<E, ArrayList<E>>();
		HashMap<E, ArrayList<E>> predecessorsOf = new HashMap<E, ArrayList<E>>();

		// transform te input hg in a rooted HyperGraph
		multiRootedDAG21(hg, successorsOf, predecessorsOf);
		
		

//		System.out.println("successors ");
//		for(E elmt : successorsOf.keySet()){
//			System.out.print(elmt+" succs");
//			for(E succ : successorsOf.get(elmt))
//				System.out.print(" "+succ);
//			System.out.println();
//		}
//		
//		System.out.println("predecessors ");
//		for(E elmt : predecessorsOf.keySet()){
//			System.out.print(elmt+" preds");
//			for(E pred : predecessorsOf.get(elmt))
//				System.out.print(" "+pred);
//			System.out.println();
//		}
		
		// save the roots Elements
		ArrayList<E> roots = new ArrayList<E>();
		for (E v : successorsOf.keySet()) {
			if (!predecessorsOf.containsKey(v))
				roots.add(v);
		}
		
//		 System.out.print("first roots ");
//		 for (E r : roots) {
//			 System.out.print(" " + r);
//		 }
//		 System.out.println();
		
		
		// To identify leaves easily we 
//		for (E v : predecessorsOf.keySet()) {
//			if (!successorsOf.containsKey(v))
//				successorsOf.put(v, new ArrayList<E>());
//		}
		
		
		ArrayList<ArrayList<E>> bucks = new ArrayList<ArrayList<E>>();
		
		HashMap<E,ArrayList<Integer>> node2iBucks = new HashMap<E, ArrayList<Integer>>();
		for(E e :hg.getVertices()){
			node2iBucks.put(e,new ArrayList<Integer>());
		}
		
		
		HashMap<Integer,Integer> iBuck2iFather = new HashMap<Integer, Integer>();
		
		HashMap<Integer,ArrayList<Integer>> iBuck2iBuckAncestors = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<E> toVisit = new ArrayList<E>(roots);
		while(! toVisit.isEmpty()){
			E cur = toVisit.remove(0);
			if(roots.contains(cur)){
				 ArrayList<E> b =new ArrayList<E>();
				 b.add(cur);
				 bucks.add(b);
				 int iBuck = bucks.indexOf(b);
				 node2iBucks.get(cur).add(iBuck);
				 iBuck2iFather.put(iBuck, iBuck);
				 iBuck2iBuckAncestors.put(iBuck, new ArrayList<Integer>());
				 iBuck2iBuckAncestors.get(iBuck).add(iBuck);
			}else{
				ArrayList<ArrayList<ArrayList<Integer>>> predsCur_Bucks_iBuckAncs = new ArrayList<ArrayList<ArrayList<Integer>>>();
				for(E predCur :predecessorsOf.get(cur)){
					predsCur_Bucks_iBuckAncs.add(new ArrayList<ArrayList<Integer>>());
					for(int iB :node2iBucks.get(predCur))
						predsCur_Bucks_iBuckAncs.get(predsCur_Bucks_iBuckAncs.size()-1).add(iBuck2iBuckAncestors.get(iB));
				}
				
				// try to chose bucket a pred that minimize the tw
				int[] cur_predsCur_Bucks_Ancs = new int[predsCur_Bucks_iBuckAncs.size()];
				int[] min_predsCur_Bucks_Ancs = new int[predsCur_Bucks_iBuckAncs.size()];
				int[] val_Min = new int[3];
				for(int i=0;i<val_Min.length;i++)
					val_Min[i]= Integer.MAX_VALUE;
				min_predsCur_Bucks_Ancs(0,
							predsCur_Bucks_iBuckAncs,
							cur_predsCur_Bucks_Ancs,
							min_predsCur_Bucks_Ancs,
							val_Min,
							bucks);
				
				// propagate 
				HashSet<Integer> hsDiffBucks = new HashSet<Integer>();
				for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
					if(hsDiffBucks.isEmpty()){
						hsDiffBucks.addAll(predsCur_Bucks_iBuckAncs.get(ip).get(min_predsCur_Bucks_Ancs[ip]));
					}else{
						ArrayList<Integer> aTmp = new ArrayList<Integer>(predsCur_Bucks_iBuckAncs.get(ip).get(min_predsCur_Bucks_Ancs[ip]));
						aTmp.removeAll(hsDiffBucks);
						hsDiffBucks.removeAll(predsCur_Bucks_iBuckAncs.get(ip).get(min_predsCur_Bucks_Ancs[ip]));
						hsDiffBucks.addAll(aTmp);
					}
				}
				HashSet<Integer> hsIntersBucks = new HashSet<Integer>();
				for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
					if(hsDiffBucks.isEmpty()){
						hsIntersBucks.addAll(predsCur_Bucks_iBuckAncs.get(ip).get(min_predsCur_Bucks_Ancs[ip]));
					}else{
						hsIntersBucks.retainAll(predsCur_Bucks_iBuckAncs.get(ip).get(min_predsCur_Bucks_Ancs[ip]));
					}
				}
				hsIntersBucks.addAll(hsDiffBucks);
				for(Integer iB: hsIntersBucks){
					bucks.get(iB).add(cur);
				}
				
				// add as many as bucket than predecessors
				for(int iPredBuck : min_predsCur_Bucks_Ancs){
					ArrayList<E> b =new ArrayList<E>();
					 b.add(cur);
					 bucks.add(b);
					 int iBuck = bucks.indexOf(b);
					 node2iBucks.get(cur).add(iBuck);
					 iBuck2iFather.put(iBuck, iPredBuck);
					 iBuck2iBuckAncestors.put(iBuck, new ArrayList<Integer>());
					 iBuck2iBuckAncestors.get(iBuck).addAll( iBuck2iBuckAncestors.get(iPredBuck));
				}
			}
		}
		
		
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
	
	
	public static void exemple2(){
		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("1", "2"));
		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("2", "3"));
		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("3", "4"));
		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("4", "1"));
		ArrayList<String> row5 = new ArrayList<String>(Arrays.asList("2", "4"));
		
		ArrayList<String> row6 = new ArrayList<String>(Arrays.asList("5", "6"));
		ArrayList<String> row7 = new ArrayList<String>(Arrays.asList("6", "7"));
		ArrayList<String> row8 = new ArrayList<String>(Arrays.asList("7", "8"));
		ArrayList<String> row9 = new ArrayList<String>(Arrays.asList("8", "5"));
		ArrayList<String> row10 = new ArrayList<String>(Arrays.asList("6", "8"));
		
		ArrayList<String> row11 = new ArrayList<String>(Arrays.asList("9", "10"));
		ArrayList<String> row12 = new ArrayList<String>(Arrays.asList("10", "11"));
		ArrayList<String> row13 = new ArrayList<String>(Arrays.asList("11", "12"));
		ArrayList<String> row14 = new ArrayList<String>(Arrays.asList("12", "9"));
		ArrayList<String> row15 = new ArrayList<String>(Arrays.asList("10", "12"));
		
		ArrayList<String> row16 = new ArrayList<String>(Arrays.asList("13", "14"));
		ArrayList<String> row17 = new ArrayList<String>(Arrays.asList("14", "15"));
		ArrayList<String> row18 = new ArrayList<String>(Arrays.asList("15", "16"));
		ArrayList<String> row19 = new ArrayList<String>(Arrays.asList("16", "13"));
		ArrayList<String> row20 = new ArrayList<String>(Arrays.asList("14", "16"));

		ArrayList<String> row21 = new ArrayList<String>(Arrays.asList("1", "5"));
		ArrayList<String> row22 = new ArrayList<String>(Arrays.asList("5", "9"));
		ArrayList<String> row23 = new ArrayList<String>(Arrays.asList("9", "13"));
		ArrayList<String> row24 = new ArrayList<String>(Arrays.asList("13", "1"));
		
		HyperGraph<String> hg = new HyperGraph<String>();
		hg.addHedge(row1);
		hg.addHedge(row2);
		hg.addHedge(row3);
		hg.addHedge(row4);
		hg.addHedge(row5);
		hg.addHedge(row6);
		hg.addHedge(row7);
		hg.addHedge(row8);
		hg.addHedge(row9);
		hg.addHedge(row10);
		hg.addHedge(row11);
		hg.addHedge(row12);
		hg.addHedge(row13);
		hg.addHedge(row14);
		hg.addHedge(row15);
		hg.addHedge(row16);
		hg.addHedge(row17);
		hg.addHedge(row18);
		hg.addHedge(row19);
		hg.addHedge(row20);
		hg.addHedge(row21);
		hg.addHedge(row22);
		hg.addHedge(row23);
		hg.addHedge(row24);
		
		System.out.println(" Le graphe " + hg);
//		 ArrayList<String> order = HyperGraphs.minFillInvOrder(hg);
//		
//		 ArrayList<ArrayList<String>> buckets = new
//		 ArrayList<ArrayList<String>>();
//		 String[] varFather = new String[order.size()];
//		
//		 System.out.println(" les noeuds ordonn�s "+order+"\n");
//		 HyperGraph<String> jt = HyperGraphs.bucketElimination(hg,order,
//		 buckets,
//		 varFather);
//		 System.out.println(" jointree par BE Min Fill"+jt);
		
		 
		HyperGraph<AbstCompArrayList<String>> dualHjt = new HyperGraph<AbstCompArrayList<String>>();
		HyperGraph<String> jt2 = distribJT2(hg, dualHjt);
		
		
		System.out.println(" jointree par DistBotUp 2" + jt2);
		System.out.println( " \n check the jointree par BE DistBotUp "+HyperGraphs.isAjointreeOf(dualHjt, hg));
		System.out.println(" \n Dual jointree par DistBotUp 2 " + dualHjt);
	}
	
	public static void exemple1(){
		
		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("a", "b"));
		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("b", "c"));
		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("c", "d"));
		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("d", "a"));

		ArrayList<String> row5 = new ArrayList<String>(Arrays.asList("a", "e"));
		ArrayList<String> row6 = new ArrayList<String>(Arrays.asList("e", "f"));
		ArrayList<String> row7 = new ArrayList<String>(Arrays.asList("f", "g"));
		ArrayList<String> row8 = new ArrayList<String>(Arrays.asList("g", "a"));

		ArrayList<String> row9 = new ArrayList<String>(Arrays.asList("a", "h"));
		ArrayList<String> row10 = new ArrayList<String>(Arrays.asList("h", "i"));
		ArrayList<String> row11 = new ArrayList<String>(Arrays.asList("i", "a"));

		ArrayList<String> row12 = new ArrayList<String>(Arrays.asList("a", "j"));
		ArrayList<String> row13 = new ArrayList<String>(Arrays.asList("j", "k"));
		ArrayList<String> row14 = new ArrayList<String>(Arrays.asList("k", "a"));
		ArrayList<String> row15 = new ArrayList<String>(Arrays.asList("j", "l"));
		ArrayList<String> row16 = new ArrayList<String>(Arrays.asList("l", "k"));
		// ArrayList<String> row17 = new
		// ArrayList<String>(Arrays.asList("k","l"));

		HyperGraph<String> hg = new HyperGraph<String>();
		hg.addHedge(row1);
		hg.addHedge(row2);
		hg.addHedge(row3);
		hg.addHedge(row4);
		hg.addHedge(row5);
		hg.addHedge(row6);
		hg.addHedge(row7);
		hg.addHedge(row8);
		hg.addHedge(row9);
		hg.addHedge(row10);
		hg.addHedge(row11);
		hg.addHedge(row12);
		hg.addHedge(row13);
		hg.addHedge(row14);
		hg.addHedge(row15);
		hg.addHedge(row16);

		System.out.println(" Le graphe " + hg);
//		 ArrayList<String> order = HyperGraphs.minFillInvOrder(hg);
//		
//		 ArrayList<ArrayList<String>> buckets = new
//		 ArrayList<ArrayList<String>>();
//		 String[] varFather = new String[order.size()];
//		
//		 System.out.println(" les noeuds ordonn�s "+order+"\n");
//		 HyperGraph<String> jt = HyperGraphs.bucketElimination(hg,order,
//		 buckets,
//		 varFather);
//		 System.out.println(" jointree par BE Min Fill"+jt);
		
		 
		HyperGraph<AbstCompArrayList<String>> dualHjt = new HyperGraph<AbstCompArrayList<String>>();
		HyperGraph<String> jt2 = distribJT2(hg, dualHjt);
		
		
		System.out.println(" jointree par DistBotUp 2" + jt2);
		System.out.println( " \n check the jointree par BE DistBotUp "+HyperGraphs.isAjointreeOf(dualHjt, hg));
		System.out.println(" \n Dual jointree par DistBotUp 2 " + dualHjt);
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
	
	public static void testGrids(){
		HyperGraph<Integer> grid = grid(7,8);
		System.out.println(" grid" + grid);
		
		HyperGraph<AbstCompArrayList<Integer>> dualHjt = new HyperGraph<AbstCompArrayList<Integer>>();
		HyperGraph<Integer> jt2 = distribJT2(grid, dualHjt);
		
		
		System.out.println(" jointree par DistBotUp 2" + jt2);
		System.out.println( " \n check the jointree par DistBotUp "+HyperGraphs.isAjointreeOf(dualHjt, grid));
		// System.out.println(" \n Dual jointree par DistBotUp 2 " + dualHjt);
		
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 exemple2();
		
//		testGrids();
	}

}
