package tools;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sun.jmx.remote.util.OrderClassLoaders;

public class ArrayListComparator<E> implements Comparator< List<E> > {
	
	boolean _directed = false;
	Comparator<E> _comparator;
	
	public ArrayListComparator(boolean directed){
		_directed = directed;
		_comparator = new DefaultComparator<E>();
	}


	@Override
	public int compare(List<E> o1, List<E> o2) {
		return 0;
	}

	public static void main(String[] args) {
	

	}

	
}
