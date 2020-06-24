package tools;
import java.util.*;


public class Heap<E> {
	
	private  ArrayList<E> tab  ;
	private int indMin =1 ;
	
	public Heap(){
		tab = new ArrayList<E>(10);
		tab.add(null);
	}
	
	private void swap(int i, int j){
		E temp = tab.get(i);
		tab.set(i, tab.get(j));
		tab.set(j,temp);
	}
	
	
	@SuppressWarnings("unchecked")
	private void shift_up(int ind){
		while(ind>indMin){
			int indFather = (ind/2);
			if (((Comparable<? super E>)tab.get(ind)).compareTo(tab.get(indFather))<0 )
				swap(ind,indFather);
			else
				break;
			ind = indFather ;
		}
		shift_down( ind);
	}
	
	@SuppressWarnings("unchecked")
	private void shift_down(int ind){
		
		while(ind<tab.size()-1){
			if(!(tab.size()>2*ind))break ;
			int indChild = 2*ind;
			if(((Comparable<? super E>) tab.get(ind)).compareTo(tab.get(indChild))>0){
				swap(ind,indChild);
			}else{
				if(!(tab.size()>2*ind+1))break ;
				indChild = 2*ind +1;
				if(((Comparable<? super E>) tab.get(ind)).compareTo(tab.get(indChild))>0)
					swap(ind,indChild);
				else 
					break ;
			}
			ind = indChild ;
		}
	}
	
	public void update(E elmt, int i){
		boolean found = false ;
		while(!found){
			
		}
		
	}
	
	public void add(E element ){
		tab.add( element);
		shift_up(tab.size()-1);
	}
	
	public E peekMin(){
		if((tab.size()-1)<=indMin){
			return ((tab.size()-1)==indMin)? tab.remove(indMin): null;
		}
		E result = tab.get(indMin);
		tab.set(indMin, tab.remove(tab.size()-1));
		shift_down(indMin);
		return result;
	}
	
	public void display(){
		for(E element : tab)
			System.out.print(element+" ");
		System.out.println();
		
	}
	
}
