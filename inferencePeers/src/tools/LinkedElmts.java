package tools;

import java.util.ArrayList;

public class LinkedElmts<E,L> {

	E _elmt ;
	ArrayList<L> _links = new ArrayList<L>();
	
	
	
	public LinkedElmts(E elmt){
		_elmt=elmt;
	}
	
	public boolean equals(Object o){
		if(o instanceof LinkedElmts){
		@SuppressWarnings("unchecked")
		LinkedElmts<E,L> le = (LinkedElmts<E,L>) o;
		return _elmt.equals(le._elmt) ;
		}
		return false;
	}
	
	public void intersect( LinkedElmts<E,L> le){
		_links.retainAll(le._links);
	}
	
	public LinkedElmts<E,L> clone(){
		LinkedElmts<E,L> le = new LinkedElmts<E,L>(_elmt);
		le._links.addAll(_links);
		return le;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
