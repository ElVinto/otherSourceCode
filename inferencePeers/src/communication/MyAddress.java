package communication;

public class MyAddress implements Comparable<Object>{
	int _port;
	String _host;
	public MyAddress(int port, String host){
		_port = port ;
		_host = host ;
	}
	public int port(){ return _port;}
	public String host(){return _host;}
	
	public boolean equals(Object ad){
		return (((MyAddress)ad)._port == _port && 
				_host.equals(((MyAddress)ad)._host)) ;
	}
	
	public String toString(){
		return  _host+"-"+_port;
	}
	
	public int compareTo(Object o) {
		MyAddress ad = (MyAddress)o;
		return _host.equals(ad._host)? _port -ad._port :_host.compareTo(ad._host);
	}
	
	
}


