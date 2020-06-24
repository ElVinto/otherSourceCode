package initializer;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Vector;

import communication.GlobalMsgerRcv;
import communication.Msger;

import tools.Chrono;
import tools.Dprint;
import peers.LocalPeer;

/*
 * At the end of the count down this object 
 * kill all thread hosted by a location
 */
public class CountDownKiller extends TimerTask{
	
	Vector<LocalPeer> _hostedPeers ;
	Chrono _chrono ;
	Msger _lmsger;
	
	public Object lockKiller = new Object();
	
	public CountDownKiller(long countDown,Msger lmsgr) throws Exception{
		if(countDown<=0)
			throw new Exception("No time");
		
		_hostedPeers = new Vector<LocalPeer>();
		_chrono = new Chrono(countDown+1000);
		_lmsger =lmsgr;
		
		
	}
	
	public Vector<LocalPeer> getLocalPeers(){
		return _hostedPeers;
	}
	
	public Chrono getChrono(){
		return _chrono;
	}
	
	
	public void addLocalPeer(LocalPeer lp){
		_hostedPeers.add(lp );
	}
	
	public  synchronized void stopAll(){
		//_chrono.start();
//		if(_chrono.remainingTime()>0){
//			try {
////				Dprint.println("_chrono.remainingTime() "+_chrono.remainingTime());
//				
//				wait(_chrono.remainingTime());
//			} catch (InterruptedException e) {
//				//e.printStackTrace()
//			;}
			try{_lmsger.terminate();
			}catch(Exception e){e.printStackTrace(); }
			
//			
			for(LocalPeer lp:_hostedPeers){
				try{
					lp.setTimeOut(System.currentTimeMillis()-10);
					lp._forceEnd = true;
					lp.unPauses();
				}catch(Exception e){e.printStackTrace(); };
			}
			
			try{_lmsger.interrupt();
			}catch(Exception e){e.printStackTrace(); }
			
			try {wait(1000);
			} catch (InterruptedException e1) {	e1.printStackTrace();}
			
			for(LocalPeer lp:_hostedPeers){
				try{lp.interrupt();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			Dprint.closeScenarioWriter();
			
	//	}

	}
	
	public  synchronized void interruptAll(){
		
	for(LocalPeer lp:_hostedPeers){
		try{lp.interrupt();
		}catch(Exception e){
			// e.printStackTrace();
		}
	}
	}
	
	public void run(){
		stopAll();
		Dprint.println("ContDouwn "+ " HAS KILLED ");
	}
}
