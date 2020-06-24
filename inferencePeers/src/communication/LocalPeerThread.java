package communication;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import peers.*;
import tools.Dprint;
import java.util.concurrent.locks.*;


public class LocalPeerThread   extends Thread  {
	
	protected volatile boolean _authorizedPause =true;
	
	protected long _startTime ;
	
	protected long _totalTime ;
	
	protected long _lastbreakTime ;
	
	protected long _workingTime ;
	
	protected long _delay = 3000; // seconds 
	
	protected long _timeOut = Long.MAX_VALUE;
	
	protected Object workingLock = new Object();
	
	public void unPauses(){
		synchronized (workingLock) {
			if(_authorizedPause){
					_authorizedPause=false;
					
					workingLock.notifyAll();
//					Dprint.println(this.getName() + " STOP WAITING ");
				}
			}
			
		
		
//		if(_pause){
//			_pause=false;
//			_lastbreakTime = System.currentTimeMillis();
//			notifyAll();
//			
////			Dprint.println(this.getName() + " STOP WAITING ");
//		}
	}
	
	public void setAuthorizedPause(boolean p){
		synchronized(workingLock){
			_authorizedPause = p ;
		}
	}
	
	protected  void pauses(){
		try{
			synchronized (workingLock) {
				if(_authorizedPause){
					long curTime = System.currentTimeMillis();
					_workingTime += curTime -_lastbreakTime;
//					Dprint.println(this.getName() + " IS WAITING ");
					 Dprint.writeScenario(this.getName() + " is waiting \n");
					workingLock.wait();
					_lastbreakTime = System.currentTimeMillis();
//					Dprint.println(this.getName() + " IS WORKING ");
				}
			}
		
		
		}catch(Exception e){
			Dprint.print(e);
		}
//		try {
//			if(_pause){
////				 Dprint.println(this.getName() + " IS WAITING ");
//				long curTime = System.currentTimeMillis();
//				_workingTime += curTime -_lastbreakTime;
//				wait((_timeOut-curTime)+1);
////				Dprint.println(this.getName() + " IS WORKING ");
//				_lastbreakTime = System.currentTimeMillis();
//				setPause(false);
//			}else{
//				setPause(true);
//			}
//		} catch (InterruptedException e) {
//			Dprint.print(" Interruption of a waiting thread ");
//			setPause(false);
//			notifyAll();
//			 e.printStackTrace();
//		}
	}
	
	public LocalPeerThread(String name){
		super(name);
	}
	
	public void setDelay(long delay){
		 _delay = delay;
	}
	
	public void setTimeOut(long timeOut){
		 _timeOut = timeOut;
	}
	
	public long startChrono(){
		_startTime = System.currentTimeMillis();
		_timeOut = _startTime+_delay;
		_lastbreakTime = _startTime;
		 return _startTime;
	}
	
	public long stopChrono(){
		long stopTime = System.currentTimeMillis();
		_workingTime += stopTime - _lastbreakTime;
		_totalTime = stopTime -_startTime ;
		return stopTime;
	}
	
	public long getChrono(){
		return System.currentTimeMillis() -_startTime ;
	}
	
	public boolean isTimeOut(){
		return (System.currentTimeMillis()>_timeOut);
	}
	
	public long getWorkingTime(){
		return _workingTime;
	} 
	
	//public static Map<String,LocalPeer> _hostedPeers = Collections.synchronizedMap(new HashMap<String,LocalPeer>());
	
//	public  static Map<String, MsgBox> _locRcvdBo 
//	= Collections.synchronizedMap(new HashMap<String, MsgBox>());
//
//	public static Map<String, MsgBox> _locSendBox 
//	= Collections.synchronizedMap(new HashMap<String, MsgBox>());
	
//	public static Map<MyAddress,Msger> _locMsger = Collections.synchronizedMap(new HashMap<MyAddress,Msger>());

//	public static ConcurrentHashMap<MyAddress,GlobalMsgerRcv> _globRcver = new ConcurrentHashMap<MyAddress,GlobalMsgerRcv>();
}
