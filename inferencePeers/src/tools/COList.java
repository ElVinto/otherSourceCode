package tools;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author VinTo
 * @param <E>
 * 
 * Comparable ordered List
 */

public class COList <E extends Comparable<E>> extends ArrayList<E> implements Comparable<COList<E>>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public COList(){
		super();
	}
	
	public COList(Collection<E> c){
		super(c);
	}
	
	/**
	 * @param args
	 */

	public boolean equals(Object o){
		if(o instanceof ArrayListComp ){
			@SuppressWarnings("unchecked")
			COList<E> al=(COList<E>) o;
			return this.compareTo(al)==0;
		}
		return false;
	}

	@Override
	public int compareTo(COList<E> al) {
		
		if(this.size()== al.size())
			return compareE(0,al);
		else
			return this.hashCode()-al.hashCode();
	}
	
	public int compareE(int curInd, COList<E> al){
		if(curInd<this.size()){
			if(this.get(curInd).compareTo(al.get(curInd))!=0)
				return this.get(curInd).compareTo(al.get(curInd));
			else
				return compareE(curInd+1,al);
		}
		return hashCode() -al.hashCode();
	}

}
