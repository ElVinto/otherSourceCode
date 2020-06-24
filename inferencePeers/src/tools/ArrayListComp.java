package tools;

import java.util.ArrayList;
import java.util.Collection;


public class  ArrayListComp<E extends Comparable<E>> extends AbstCompArrayList<E>{
	
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		ArrayListComp(){
			super();
		}
		
		ArrayListComp(Collection<E> c){
			super(c);
		}
		

		public boolean equals(Object o){
			if(o instanceof ArrayListComp ){
				@SuppressWarnings("unchecked")
				ArrayListComp<E> al=(ArrayListComp<E>) o;
				return this.hashCode()==al.hashCode();
				//return this.containsAll(al) && al.containsAll(this);
			}
			return false;
		}
		
		
		
		@Override
		public int compareTo(AbstCompArrayList<E> al) {
			if(this.containsAll(al) && al.containsAll(this))
				return 0;
			else
				return this.hashCode()-al.hashCode();
		}
		
		
		
		
		
}

