package tools;

import java.util.*;


public class Graph<E> {
	
	TreeSet<WeightedElmt<E>> _nodes ;
	TreeMap<E,WeightedElmt<E>> _elt2Id;
	TreeMap<E,TreeSet<E>> _neighbors;
	Comparator<E> _c ;
	
	
	private void initStructuresByDefault(){
		_nodes = new TreeSet<WeightedElmt<E>>();
		_c = new DefaultComparator<E>();
		_neighbors = new TreeMap<E, TreeSet<E>>(_c);
		_elt2Id = new TreeMap<E,WeightedElmt<E>>(_c);
	}
	
	public Graph() {	
		initStructuresByDefault();
	}
	
	
	public TreeMap<E, TreeSet<E>> get_neighbors() {
		return _neighbors;
	}
	
	public Set<E> get_nodes(){
		return _neighbors.keySet();
	}

	public void addVertex(E element){
		if(_neighbors.keySet().contains(element))
			;
		
		WeightedElmt<E> id =new WeightedElmt<E>(Integer.MAX_VALUE,element);
		
		_nodes.add(id);
		_elt2Id.put(element, id);
		_neighbors.put(element,new TreeSet<E>(_c));
	} 
	
	public void addEdge(E elmt1, E elmt2){
		
		if(_nodes.contains(elmt1)){
			
		}
		
		((TreeSet<E>)_neighbors.get(elmt1)).add(elmt2);
		((TreeSet<E>)_neighbors.get(elmt2)).add(elmt1);
	}
	
	/*
	 * This function is not completed I have 
	 * to remvove remaining relations
	 * that exist between nodes after a deletion. 
	 */
	void removeVertex(E elmt){
		WeightedElmt<E> id =_elt2Id.get(elmt);
		_nodes.remove(id);
		_neighbors.remove(id.getElement());
		_elt2Id.remove(elmt);
	}
	
	
	void connectNeighborsThenRemove(E elmt){
		ArrayList<E> neighOfIdElt = new ArrayList<E>(_neighbors.get(elmt));
		for(E neigh: neighOfIdElt){
			_neighbors.get(neigh).addAll(neighOfIdElt);
			_neighbors.get(neigh).remove(elmt);
			_neighbors.get(neigh).remove(neigh);
		}
		_nodes.remove(_elt2Id.get(elmt));
		_elt2Id.remove(elmt);
	}
	
	@SuppressWarnings("unchecked")
	public	void updateMinfill(E elmt){
		WeightedElmt<E> id =_elt2Id.get(elmt);
//		System.out.println("update Minfill Of : "+elmt);
		
		TreeSet<E> neighborsDup =(TreeSet<E>) _neighbors.get(elmt).clone();
		int minFill = 0;
		while(!neighborsDup.isEmpty()){
			E cur = (E) neighborsDup.first();
			neighborsDup.remove(cur);
			TreeSet<E> neighborsDup2 = (TreeSet<E>)neighborsDup.clone();
			neighborsDup2.removeAll(_neighbors.get(cur));
			minFill += neighborsDup2.size();
		}
	   _nodes.remove(id);
	   _elt2Id.remove(id.getElement());
	   WeightedElmt<E> nvId =new WeightedElmt<E>(minFill,elmt);
	   _nodes.add(nvId);
	   _elt2Id.put(nvId.getElement(),nvId);
	}
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<E> toEliminationOrder(){
		ArrayList <E> result = new ArrayList<E>();
		while(_nodes.isEmpty()){
			WeightedElmt<E> idFirst = _nodes.first();
			TreeSet<E> neigh = (TreeSet<E>)_neighbors.get(idFirst.getElement()).clone();
			connectNeighborsThenRemove(idFirst.getElement());
			TreeSet<E> updateNeigh = new TreeSet<E>();
			for(E n:neigh ){
				if(!updateNeigh.contains(n))
					updateMinfill(n);
				for(E nOfn : _neighbors.get(n)){
					if(!updateNeigh.contains(n))
						updateMinfill(nOfn);
				}
			}
			result.add(idFirst.getElement());
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Graph<TreeSet<E>> toJoinTree(){
		ArrayList<TreeSet<E>> setOfClusters = new ArrayList<TreeSet<E>>();
		
		// Computes the set of Clusters based on Elimination Order driven by Min fill heuristic
		while(!_nodes.isEmpty()){
			WeightedElmt<E> idFirst = _nodes.first();
			TreeSet<E> neigh = (TreeSet<E>)_neighbors.get(idFirst.getElement()).clone();
			
//			System.out.println("Removing "+ idFirst);
			connectNeighborsThenRemove(idFirst.getElement());
			TreeSet<E> updatedNeigh = new TreeSet<E>();
			for(E n:neigh ){
				if(!updatedNeigh.contains(n)){
					updateMinfill(n);
					updatedNeigh.add(n);
				}
				for(E nOfn : _neighbors.get(n)){
					if(!updatedNeigh.contains(nOfn))
						updateMinfill(nOfn);
						updatedNeigh.add(nOfn);
				}
			}
			neigh.add(idFirst.getElement());
			setOfClusters.add(neigh);
		}
			
//		for(TreeSet<E> cl :setOfClusters){
//			System.out.print("\n cl :");
//			for(E elmt:cl){
//				System.out.print(elmt+ " ");
//			}
//		}
//		System.out.println();
			
			
		
		// Removes some clusters with regard to the running intersection property
		TreeMap<Number, Number> toMoveAt = new TreeMap<Number, Number>();
		TreeSet<Number> toRemove = new TreeSet<Number>();
		for (int i = setOfClusters.size() - 1; i >0; i--) {
			int cur = i;
			if (!toRemove.contains(i)&& !toMoveAt.containsValue(i)){
//				System.out.println("Test cl "+cur);
				for (int j = cur - 1; j >= 0; j--) {
					if (setOfClusters.get(j)
							.containsAll(setOfClusters.get(cur))) {
//						System.out.println(" cl " + j + " contains all : cl "
//								+ cur);
						if (!toMoveAt.containsKey(j) && !toRemove.contains(j)) {
							if (toMoveAt.containsKey(cur)) {
								toMoveAt.put(j, toMoveAt.get(cur));
								toMoveAt.remove(cur);
							} else
								toMoveAt.put(j, cur);
							toRemove.add(j);
							cur = j;
						} else {
							toRemove.add(cur);
							break;
						}
					}
				}
			}
		}
		
		for(Number ind: toMoveAt.keySet())
			setOfClusters.set(toMoveAt.get(ind).intValue(),setOfClusters.get(ind.intValue()));
		

//		System.out.println();
		int adjust =0;
		for(Number ind : toRemove){
//			System.out.println("Remove cl:"+(ind.intValue()-adjust));
			setOfClusters.remove(ind.intValue()-adjust);
			adjust++;
		}
		
		
//		for(TreeSet<E> cl :setOfClusters){
//			System.out.print("\n cl :");
//			for(E elmt:cl){
//				System.out.print(elmt+ " ");
//			}
//		}
//		System.out.println();
		
		// Assembles the remaining clusters
		Graph<TreeSet<E>> g = new Graph<TreeSet<E>>();
		
		for(int i=setOfClusters.size()-1; i>=0;i--){
			g.addVertex(setOfClusters.get(i));
//			System.out.println(i+" "+setOfClusters.get(i));
			int max = 0;
			int indNeighMax =-1; 
			for(int j=i+1; j<setOfClusters.size();j++){
				TreeSet<E> interIJ  = (TreeSet<E>) setOfClusters.get(i).clone();
				interIJ.retainAll(setOfClusters.get(j));
				
				
				if(interIJ.size()> max){
					max = interIJ.size();
					indNeighMax =j;
				}	
			}
			if(indNeighMax>-1){
				g.addEdge(setOfClusters.get(i), setOfClusters.get(indNeighMax));
//				System.out.println("edge between "+ i+" "+indNeighMax);
			}
		}
		return g;
	}
	
	public String toString(){
		String s = "Graph : \n";
		for(E elmt :_neighbors.keySet()){
			s+= "node : "+elmt.toString()+" neighbors : [ ";
			for(E neigh: _neighbors.get(elmt))
				s +=  " "+neigh.toString()+" " ;
			s+=" ]\n";
		}
		return s;
	}
	
	
	public static void main (String args []){
		
		Graph<String> g = new Graph<String>();
		
		g.addVertex("v1");
		g.addVertex("v2");
		g.addEdge("v1","v2");
		
		g.addVertex("v1");
		g.addVertex("v2");
		g.addEdge("v1","v2");
		
		g.addVertex("v1");
		g.addVertex("v2");
		g.addEdge("v1","v2");
		
	}
	

}
