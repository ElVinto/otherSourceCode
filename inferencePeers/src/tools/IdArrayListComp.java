package tools;

import java.util.Collection;

public class IdArrayListComp<E extends Comparable<E>> extends ArrayListComp<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public IdArrayListComp(){
		super();
	}
	
	public IdArrayListComp(Collection<E> c){
		super(c);
		
	}
	
	public int compareTo(IdArrayListComp<E> ial){
		return super.compareTo(ial)+ hashCode()-ial.hashCode();
	}
}
