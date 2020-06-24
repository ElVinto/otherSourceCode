package distributedAlgorithm.m2dt;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

import propositionalLogic.Base;

import initializer.*;



public class MContentM2DT implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArrayList<ArrayList<String>> _f = null;

	public ArrayList<String> _diagLit= null;
	
	public TreeMap<String, TreeSet<String>> _sh2PToSend = null ;

	public MContentM2DT(int[][] fm, ArrayList<String> litNames, 
			ArrayList<String> diagLit, 
			TreeMap<String, TreeSet<String>> sh2PNotSent,
			Collection<String> children) {

		_diagLit = diagLit;
		_f = new ArrayList<ArrayList<String>>();
		 for(int[] impl :fm){
			  ArrayList<String> fString = 
				  PeerDescription2BaseInt.litsToString(litNames,impl);
			 _f.add(fString);
			 for( String lit : fString ){
				 if(!_sh2PToSend.keySet().contains(lit) &&
						 sh2PNotSent.keySet().contains(lit)){
					 _sh2PToSend.put(lit,sh2PNotSent.remove(lit));
					 String oppLit =  Base.oppLit(lit);
					 _sh2PToSend.put(oppLit, sh2PNotSent.remove(oppLit));
					 
					 _sh2PToSend.get(lit).removeAll(children);
					 _sh2PToSend.get(oppLit).removeAll(children);
					 
				 }
			 }
		}
	
	}
	
	

	public ArrayList<ArrayList<String>> getFormula(){
		return _f ;
	}
	
	public ArrayList<String> getDiagLit(){
		return _diagLit ;
	}

	public TreeMap<String, TreeSet<String>> get_sh2PToSend() {
		return _sh2PToSend;
	}
	
	public String toString() {
		return (this ==null||_f ==null)? " ":_f.toString();
	}


}
