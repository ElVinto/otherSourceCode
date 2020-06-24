package tools;

public class StringInt  {

	String _first;
	int _second;
	
	public StringInt(String first,int second){
		_first = first;
		_second = second;
	}
	
	public String getFirst(){
		return _first;
	}
	
	public int getSecond(){
		return _second;
	}
	public boolean equals(Object o){
		StringInt s =(StringInt) o;
		return s._first.equals(_first);
	}
	
	public int compare(StringInt s){
		return _first.compareTo(s._first);
	}
}
