package tools;

import java.util.ArrayList;
import java.util.Collection;

public  class AbstCompArrayList<E extends Comparable<E>> extends ArrayList<E> implements Comparable<AbstCompArrayList<E>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Integer _id = null;
	
	public AbstCompArrayList(){
		super();
	}
	
	public AbstCompArrayList( Integer id){
		super();
		_id = id;
	}
	
	public AbstCompArrayList(Collection<E> c){
		super(c);
	}
	
	public AbstCompArrayList(Collection<E> c, Integer id){
		super(c);
		_id = id;
	}

	@Override
	public int compareTo(AbstCompArrayList<E> o) {
		if (_id ==null)
			return compareTo1(o);
		else
			return compareTo2(o);
	}
	
	@Override
	public boolean equals(Object o){
		if (_id ==null)
			return equals1(o);
		else
			return equals2(o);
	}

	
	public boolean equals1(Object o){
		if(o instanceof AbstCompArrayList ){
			@SuppressWarnings("unchecked")
			AbstCompArrayList<E> al=(AbstCompArrayList<E>) o;
			return this.hashCode()==al.hashCode();
			//return this.containsAll(al) && al.containsAll(this);
		}
		return false;
	}
	
	
	public int compareTo1(AbstCompArrayList<E> al) {
		if(this.containsAll(al) && al.containsAll(this))
			return 0;
		else
			return this.hashCode()-al.hashCode();
	}
	
	
	public boolean equals2(Object o){
		if(o instanceof AbstCompArrayList ){
			@SuppressWarnings("unchecked")
			AbstCompArrayList<E> al=(AbstCompArrayList<E>) o;
			if(al._id !=null)
				return _id.equals(al._id);
		}
		return false;
	}

	public int compareTo2(AbstCompArrayList<E> al) {
		
		if(this.size()== al.size())
			return compareE(0,al);
		else
			return this.hashCode()-al.hashCode();
	}
	
	public int compareE(int curInd, AbstCompArrayList<E> al){
		if(curInd<this.size()){
			if(this.get(curInd).compareTo(al.get(curInd))!=0)
				return this.get(curInd).compareTo(al.get(curInd));
			else
				return compareE(curInd+1,al);
		}
		return 0;
	}
	
	
}
