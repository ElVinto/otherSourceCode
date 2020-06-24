package tools;

public class WeightedElmt<E> implements Comparable<Object>{
	
	float _weight ;
	E _elmt ;
	
	public WeightedElmt(int x, E elmt){
		_weight = x;
		_elmt = elmt;
	}
	
	public WeightedElmt( E elmt) {
		_weight = 0;
		_elmt = elmt;
	}

	public E getElement(){
		return _elmt;
	}
	
	public float getWeight(){
		return _weight;
	}
	
	public void setElement(E elmt){
		_elmt = elmt;
	}
	
	public void setWeight(float m){
		_weight =m;
	}
	

	

	@SuppressWarnings("unchecked")
	public  int compareTo(WeightedElmt<E> we){
		
		if(_elmt.equals(we._elmt))
			return (int)Math.round(_weight-we._weight);
		
		return (_weight!=we._weight)? (int)(_weight-we._weight) :
			_elmt instanceof Comparable ? ((Comparable)_elmt).compareTo(we._elmt):
					_elmt.hashCode() -we._elmt.hashCode() ;
	}

	@SuppressWarnings("unchecked")
	public int compareTo(Object o) {
		return compareTo((WeightedElmt<E>) o);
	}
	
	public boolean equals(Object o){
		if(o instanceof WeightedElmt){
			WeightedElmt<E> we= (WeightedElmt<E>)o;
			return _elmt.equals(we._elmt) ;
		}else
			return false;
	}
	
	public String toString(){
		String result = "";
		result =//+_weight+
			"."+_elmt;
		return result;
	}
	
	public int hashCode(){
		return _elmt.toString().hashCode();
	}
	
	public WeightedElmt<E> clone(){
		return new WeightedElmt<E>(_elmt);
	}
	
	
	public static void main(String args []){
		WeightedElmt<String> s1 = new WeightedElmt<String>("coucou");
		WeightedElmt<String> s2 = new WeightedElmt<String>("coucou");
		System.out.println("s1.compareTo(s2): "+s1.compareTo(s2));
		System.out.println("s1.equals(s2): "+s1.equals(s2));
		
	}
	
}
