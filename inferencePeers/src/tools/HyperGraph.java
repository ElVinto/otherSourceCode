package tools;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;



/*
 * This class can both implements directed /undirected hyper graph.  
 * An hyper graph is an undirected graph where edges are made from set of vertices
 * A directed hyper graph implementation can be obtained by using lists as hyper edges.
 * The undirected hyper graph is built from set as implementation of hyper edges
 * 
 *  Examples:
 *  
 *  // directed hyper graph implementation 
 *  HyperGraph<String> dhg = new HyperGraph<String>();
 *  dhg.addHEedge(Arrays.asList("a","c","e"));
 *  dhg.addHEedge(Arrays.asList("a","e","c"));
 *  dhg.addHEedge(Arrays.asList("b","c","d")); 
 *  
 *  dhg is a directed hyper graph made of 3 hyper edges
 *  
 *  // undirected hyper graph implementation
 *  HyperGraph<String> hg = new HyperGraph<String>();
 *  hg.addHEedge(new HashSet(Arrays.asList("a","c","e")));
 *  hg.addHEedge(new HashSet(Arrays.asList("a","e","c")));
 *  hg.addHEedge(new HashSet(Arrays.asList("b","c","d")));
 *  
 *  hg is an undirected hyper graph made of 2 hyper edges 
 *  since [a, e, c] ans [a, c, e] are equivalent.
 * 
 */
public class HyperGraph <E extends Comparable<? super E>> {
	
	
	
	// variable to hyper edge
	private Hashtable< E , ArrayList<ArrayList<E>>> _v2h;
	// variable to neighbors
	private Hashtable< E , ArrayList<E>> _v2n;
	private ArrayList<ArrayList<E>> _hEdges;
	private ArrayList<E> _vertices;
	private boolean _ordered = false;
	
	
	 public HyperGraph (){
		 _v2h = new Hashtable< E , ArrayList<ArrayList<E>>>();
		 _v2n = new Hashtable < E , ArrayList<E>>();
		_hEdges = new ArrayList<ArrayList<E>>();
		_vertices = new ArrayList<E>();
	}
	 
	 public HashSet<ArrayList<E>> getHEdges(){
		 HashSet<ArrayList<E>> hR = new HashSet<ArrayList<E>>();
		 hR.addAll(_hEdges);
		 return hR;
		 
	 }
	 
	 public HashSet<ArrayList<E>> getHEdges(E vertex){
		 HashSet<ArrayList<E>> hR = new HashSet<ArrayList<E>>();
		 hR.addAll(_v2h.get(vertex));
		 return hR;
	 }
	 
	 public Set<E> getVertices(){
		 return _v2h.keySet();
	 }
	 
	 
	 public int nbNeighbors(E vertex){
		 return _v2n.get(vertex).size();
	 }
	 
	 public ArrayList<E> getNeighbors(E vertex){
		 return _v2n.get(vertex);
	 }
	  
	 public boolean addVertex( E  vertex){
		 for(E node :_v2h.keySet())
			 if(node.equals(vertex))
				 return false;
		 
		 _v2h.put(vertex, new ArrayList<ArrayList<E>>());
		 _v2n.put(vertex, new ArrayList<E>());
		 _vertices.add(vertex);
		 
		 return true;
	 }
	 
	 public boolean addHedge(E v1 , E v2){
		 ArrayList<E> he = new ArrayList<E>(); 
		 he.add(v1);
		 he.add(v2);
		 return addHedge(he);
	 }
	 
	 public boolean addHedge(Collection<E> he){
		 
		 ArrayList<E> nvHe = new ArrayList<E>(); 
		 for(E e: he){
			 if(!nvHe.contains(e)){
				 addVertex(e);
				 nvHe.add(_vertices.get(_vertices.indexOf(e)));
			 }
		 }
		Collections.sort(nvHe);
		
		 if(containsHedge(nvHe))
				return false;
		 
		_hEdges.add(nvHe);
		for(E e: nvHe){
			_v2h.get(e).add(nvHe);
			for(E e2: nvHe){
				if(!_v2n.get(e).contains(e2) && !e.equals(e2))
					_v2n.get(e).add(e2);
			}
		}
		return true;
	 }
	 
	 /*
	  * removes the  vertex v from hyper edges in which v belongs to.
	  * returns the set of modified hyper edges. 
	  * deletes empty hyper edges involved by the removing. 
	  * 
	  */
	 public HashSet<ArrayList<E>> removeVertex( E  v){
		 
		 _vertices.remove(v);
		 for(E n: _v2n.get(v))
			 _v2n.get(n).remove(v);
		 _v2n.remove(v);
		 
		 
		 HashSet<ArrayList<E>> vHedges =  new  HashSet<ArrayList<E>>();
		 
//		 System.out.println(" vToRem "+v);
		 
		 ArrayList<ArrayList<E>> tmp = _v2h.remove(v);
		 if(tmp != null){ 
			 vHedges.addAll(tmp);
			 for(ArrayList<E> he : vHedges){
				he.remove(v);
			 	if(he.isEmpty())
			 		removeHEdge(he);
			 }
	 	}
		
		return vHedges;	 
	 }
	 
	 
	 private boolean containsHedge(ArrayList<E> he){
		 for(ArrayList<E> _he: _hEdges ){
			 if(_he.containsAll(he) && he.containsAll(_he))
				 return true;
		 }
		 return false;
	 }
	 
	 
	 private ArrayList<E> remIn_hEdges(ArrayList<E> heParam){
		 
		 ArrayList<E> _he2Rem = null;
		 int indHe2Rem =-1;
		 for(ArrayList<E> _he: _hEdges ){
			 if(_he.containsAll(heParam) && heParam.containsAll(_he)){
				 indHe2Rem =_hEdges.indexOf(_he);
				 _he2Rem = _he;
				 break;
			 }
				 
		 }
		 if(_he2Rem == null)
			 return null;
		 else{
//			 System.out.println(" _hEdges contains " +_he2Rem+" "+_hEdges.contains(_he2Rem) );
			return _hEdges.remove(indHe2Rem);
		 }
		
	 }
	 
	 
	 /**
	  * removes the hyper edge "he" from the graph.
	  * removes and returns the set of unconstrained vertices involved by the removing.
	  */
	 public Collection< E > removeHEdge(ArrayList<E> he){
		 Collections.sort(he);
		 if(containsHedge(he)){
			 ArrayList<E> _he2Rem = remIn_hEdges(he);
			 if(_he2Rem == null)
				 return null;
			 HashSet< E > vToRem = new HashSet< E >();
			 for( E  v : _he2Rem){
				 if(_v2h.containsKey(v)){
					 _v2h.get(v).remove(he);
					 if(_v2h.get(v).size()==0){
						 _v2h.remove(v);
						 _vertices.remove(v);
						 vToRem.add(v);
					 }
				}
			 }
			 
			 for( E  v1 : _he2Rem){
				 for( E  v2 : _he2Rem){
					 if(!v1.equals(v2))
						 if(_v2n.get(v1).contains(v2)){
							 _v2n.get(v1).remove(v2);
						 }
				 }
			 }
			 
			 return vToRem;
		 }
		 return null;
	 }
	 
	 
	 /**
	  * removes the hyper edge "he" from the graph.
	  * removes and returns the set of unconstrained vertices involved by the removing.
	  */
	 public Collection< E > removeHEdge(E v1, E v2){
		 ArrayList<E> he = new ArrayList<E>();
		 he.add(v1);he.add(v2);
		 return removeHEdge(he);
	 }
	 
	 
	 /*
	  * return the width of the hyper graph. The width of a graph corresponds
	  * to the size of the larger hyper edge
	  */
	 public int width(){
//		 return sepWidth();
		 int max = 0;
		 for(ArrayList<E> he: _hEdges)
			 if(he.size()>max)
				 max = he.size();
		 return max-1;
	 }
	 
	 public int sepWidth(){
		 int max = 0;
		 for(ArrayList<E> he1: _hEdges)
			 if(max < he1.size())
			 for(ArrayList<E> he2: _hEdges ){
				 if(!he1.equals(he2) &&  max< he2.size()){
					 ArrayList<E> heTmp = new ArrayList<E>(he1);
					 heTmp.retainAll(he2);
					 if(max < heTmp.size())
						 max = heTmp.size();
				}
			}
		 return max-1;
	 }
	 
	 
	 public float avgWidth(){
		 float avg = 0;
		 for(ArrayList<E> he: _hEdges)
			avg += he.size();
		 return  avg/(float)_hEdges.size();
	 }
	 
	 
	 public int degree(){
		 int min = nbVertices();
		 for(E v: _v2n.keySet()){
			 if((_v2n.get(v).size())<min)
				 min = _v2n.get(v).size();
		 }
		 return min;
	 }
	 
	 public int nbVertices(){
		 return _vertices.size();
	 }
	 
	 public int nbEdges(){
		 
		 int nbEdges=0;
		 for(int i=0;i<_vertices.size()-1;i++){
			 for(int j=i+1;j<_vertices.size();j++){
				 if(_v2n.get(_vertices.get(i)).contains(_vertices.get(j)) ){
					 nbEdges++;
				 }
			 }
		 }
		 return nbEdges;
	 }
	 
	 
	 public String toString(){
			String s = "";
			s+=" HyperGraph : nbVertices: "+ _v2h.size()
			+" nbEdges: "+nbEdges()
			+" degree: "+degree()
			+" width: "+ width()
			+" sepWidth: "+ sepWidth()
			+" avgwidth: "+ avgWidth()+" \n";
//			for(E v : _v2h.keySet()){
//				s+= "vertex : "+v+" is in HEdeges : { ";
//				for(ArrayList<E> he: _v2h.get(v)){
//					s +=  "{" ;
//						for(E e: he)
//							s+= e+",";
//					s=s.substring(0, s.length()-1);
//					s+="} ";
//				}
//				s+="}\n";
//			}
			
//			for(ArrayList<E> he : _hEdges){
//				s+= he+"\n";
//			}
//			for(E v:_v2n.keySet()){
//				s+= v+" "+_v2n.get(v)+"\n";
//			}
//			
//			s+=" hyper Edges "+_hEdges+" width "+ width()+ "\n";
			
			return s;
		}
	 
	 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
				
		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("a","b","c"));
		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("b","c","d"));
		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("c","d","a"));
		// row4 and row 1 contains the same set of node
		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("b","a","e","c"));	
		
		
		HyperGraph<String> dhg = new HyperGraph<String>();
		dhg.addHedge(row1);
		dhg.addHedge(row2);
		dhg.addHedge(row3);
		dhg.addHedge(row4);
		System.out.println(dhg);
		dhg.removeHEdge(row4);
		System.out.println(dhg);
		

		
//		System.out.println(" ajout de "+new HashSet<String>(row4)+": "+ hg.addHedge(new HashSet<String>(row4))+hg);
//		
//		System.out.println(" retrait de c "+": "+hg.removeVertex("c")+hg);
//		
//		System.out.println(" retrait de e "+": "+hg.removeVertex("e")+hg);
//		
//		System.out.println(" retrait de "+new HashSet<String>(Arrays.asList("a","b"))+
//				": "+hg.removeHEdge(new HashSet<String>(Arrays.asList("a","b")))+hg);
//		
//		System.out.println(" retrait de b "+": "+hg.removeVertex("b")+hg);
//		
//		
//		System.out.println(" retrait de "+row4+": "+hg.removeHEdge(new HashSet<String>(row4))+hg);
//		
//		System.out.println(" retrait de a "+": "+hg.removeVertex("a")+hg);
//		
//		System.out.println(" retrait de d "+": "+hg.removeVertex("d")+hg);
//		

	}

}
