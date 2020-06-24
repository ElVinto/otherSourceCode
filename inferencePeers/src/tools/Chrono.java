package tools;

public class Chrono {

	public static long millis2Sec(long millis){
		return millis%1000;
	}
	
	public static long millis2Min(long millis){
		return millis%60000;
	}
	
	public static long millis2H(long millis){
		return millis%3600000;
	}
	
	public static long hours2Millis(long h){
		return h*3600000;
	}
	
	public static long min2Millis(long m){
		return m*60000;
	}
	
	public static long s2Millis(long s){
		return s*1000;
	}
	
	long _start ;
	long _contDown ;
	
	public Chrono(long contDown){
		_start = 0;
		_contDown = contDown;
	}
	
	public Chrono(){
		_start = 0;
		_contDown = 0;
	}
	
	public void start(){
		_start = System.currentTimeMillis();
	}

	public void reStart(){
		_contDown = 0;
		start();
	}
	
	public long time(){
		return System.currentTimeMillis() - _start;
	}
	
	public long getTimeMillis(){
		return time();
	}
	
	public long getTimeSec(){
		return getTimeMillis()/ 1000;
	}

	public long getTimeMin(){
		return getTimeMillis()/ (60*1000);
	}
	
	public long getTimeHours(){
		return getTimeMillis()/ (3600*1000);
	}
	
	public void setCountDown( long delay){
		_contDown = delay;
	}
	
	
	public long remainingTime(){
		if(_start !=0)
			return  _contDown -time();
		else
			return _contDown;
	}
}
