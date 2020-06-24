package tools;


public class IntInt  {

	int _first;
	int _second;
	
	public IntInt(int first,int second){
		_first = first;
		_second = second;
	}
	
	public int getFirst(){
		return _first;
	}
	
	public int getSecond(){
		return _second;
	}
	
	public boolean equals(Object o){
		IntInt i =(IntInt) o;
		return i._first == _first;
	}
	
	public int compare(IntInt i){
		return _first-i._first;
	}
}
