package tools;

import java.util.Comparator;

public class DefaultComparator<T> implements Comparator<T> {

	@SuppressWarnings("unchecked")
	@Override
	public int compare(T o1, T o2) {
		if(o1 instanceof Comparable && o2 instanceof Comparable 
				&& o1.getClass().equals(o2.getClass()) )
			return ((Comparable)o1).compareTo(o2);
		
		return o1.hashCode()-o2.hashCode();
		

	}



}
