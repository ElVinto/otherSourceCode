package tools;

/*
 * Comparable and weighted Id
 * an id is weighted by a Comparable
 * two cwids with same id but with differents weight are equal
 * two cwid with same id but with differents weight
 * 
 */

public class CWId<E,W extends Comparable<W>> 
	implements Comparable<CWId<E,W>> {
	
	W _weight ;
	E _elmt ;
	
	public CWId(W x, E elmt){
		_weight = x;
		_elmt = elmt;
	}
	
	public CWId( E elmt) {
		_weight = null;
		_elmt = elmt;
	}

	public E getElement(){
		return _elmt;
	}
	
	public Comparable<?> getWeight(){
		return _weight;
	}
	
	public void setElement(E elmt){
		_elmt = elmt;
	}
	
	public void setWeight(W  m){
		_weight =m;
	}
	

	@Override
	public int compareTo(CWId<E, W> cwe) {
		if(cwe._weight !=null &&  _weight !=null)
			return _weight.compareTo((W) cwe._weight);
		if(cwe._weight ==null &&  _weight ==null)
			return 	_elmt.hashCode() -cwe._elmt.hashCode() ;
		if(cwe._weight ==null)
			return 1;
		else
			return -1;
	}
	
	public boolean equals(Object o){
		if(o instanceof CWId){
			@SuppressWarnings("unchecked")
			CWId<E,W> we= (CWId<E,W>)o;
			return _elmt.equals(we._elmt) ;
		}else
			return false;
	}
	
	public String toString(){
		String result = "";
		result = // _weight+
			"."+_elmt;
		return result;
	}
	
	public int hashCode(){
		return _elmt.toString().hashCode();
	}
	
	public CWId<E,W>  clone(){
		return new CWId<E,W> (_weight,_elmt);
	}
	
	
	public static void main(String args []){
		WeightedElmt<String> s1 = new WeightedElmt<String>("coucou");
		WeightedElmt<String> s2 = new WeightedElmt<String>("coucou");
		System.out.println("s1.compareTo(s2): "+s1.compareTo(s2));
		System.out.println("s1.equals(s2): "+s1.equals(s2));
		
	}

	



}
