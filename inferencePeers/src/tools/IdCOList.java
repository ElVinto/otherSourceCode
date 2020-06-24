package tools;

import java.util.Collection;

public class IdCOList <E extends Comparable<E>>  extends COList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public IdCOList(){
		super();
	}
	
	IdCOList(Collection<E> c){
		super(c);
	}
	
	public int compareTo(COList<E> al) {
		return  this.hashCode()-al.hashCode()+super.compareTo(al);
	}
}
