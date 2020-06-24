package peers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import tools.Dprint;
import communication.LocalPeerThread;
import communication.MTypeNetwork;
import communication.Msg;
import communication.MsgBox;
import communication.Msger;
import distributedAlgorithm.DistributedAlgorithm;

public class LocalPeer extends LocalPeerThread {

	String _pName = "";
	PeerDescription _pD = null;
	DistributedAlgorithm _dA = null;
	Msger _msger = null;
	public MsgBox _rcvdBox ;
	public MsgBox _sentBox ;
	boolean _isStarter = false;
	public volatile boolean _isTerminating = false;
//	public Dprint _dprint = null;
	public boolean firstMsg = true;
	public Lock _endLock = null;
	
	public volatile boolean _forceEnd = false;
	public volatile boolean _otherEnd = false;
	public String _dirOut="";
	

	public LocalPeer(PeerDescription pD, DistributedAlgorithm dA, Msger msger, String dirOut) {
		super(pD.get_pName());
		_pD = pD;
		_pName = pD.get_pName();
		_dirOut = dirOut;
		_msger = msger;
		_dA = dA ;
		_rcvdBox = new MsgBox();
		_sentBox = new MsgBox();
		_endLock = new ReentrantLock();
		
		
	}
	
	public MsgBox getRcvdBox(){
		return _rcvdBox ;
	}
	
	public MsgBox getSentBox(){
		return _sentBox ;
	}
	
//	public void printStat(String s){
//		_dprint.writeStat(s);
//	}
	
//	public void closePrinter(){
//		_dprint.closeWriters();
//	}

	public DistributedAlgorithm getAlgo() {
		return _dA;
	}

	public void setAlgo(DistributedAlgorithm _da) {
		_dA = _da;
	}

	public PeerDescription getDescription() {
		return _pD;
	}

	public void setDescription(PeerDescription _pd) {
		_pD = _pd;
	}

	public void setStarter(boolean isStarter) {
		_isStarter = true;
	}

	public  boolean isStarter() {
		return _isStarter;
	}

	

	public <MContent  extends   Serializable> void sendMsg(String sender, String recipient, int type,
			MContent content) {
		Msg<? extends Serializable> m = new Msg<MContent>(sender, recipient, type, content);
		_sentBox.add( m);
		 Dprint.writeScenario(_pName+" is sending msg type "+ type+" from "+_pName+" to "+recipient+" \n");
		// Dprint.println(_pName+" is sending msg type "+ type+" to "+recipient);
//		_msger.unPauses();
	}
	
	public void receiveMsgs(){
		Msg<? extends Serializable>  m = _rcvdBox.remove(0);
		Dprint.writeScenario(_pName+" is receiving msg type "+ m.getType()+
				" from "+m.getSender()+" to "+m.getRecipient()+" \n");
		_dA.receiveMsg(m);
	}

//	public void middleResult() {
//		if (!_rcvdBox.isEmpty()) {
//			Msg<? extends Serializable> m = _rcvdBox.remove(0);
//			_dA.receiveMsg(m);
//		}
//
//	}
	
	public  Object  notifySmthTo(String smth, Collection<String> pNames){
		if(smth.equals("emptyRimpl")){
			if(pNames !=null)
				if(!pNames.isEmpty())
					_msger.notifySmthTo(smth, pNames);
					
			else{
				if(!_isTerminating)
					if(_endLock.tryLock()){
//						Dprint.println(_pName+" gets _endLock By Msger");
						_dA.notifyEndFor("emptyRimpl");
						_endLock.unlock();
//						Dprint.println(_pName+" releases _endLock ");
					}
					
				}
					
		}
		
		if(smth.equals("emptyDiag")){
			_msger.notifySmthTo(smth, pNames);
				
		}
		
		
		
		
		return null;
	}

	

	public  void run() {
		startChrono();
		//printStat(_pName+"\n");
		//printStat("start at: "+curTime()+"\n");
		
		if (isStarter() && !_dA.isAwaked()) {
			_dA.wakeUp();
		}
		
//		Dprint.println(_pName+ " before loop "+!_dA.isTerminated()+"  "+ !isTimeOut() +"  "+ !_forceEnd+"  "+ !_otherEnd);
		while (!_dA.isTerminated() && !isTimeOut() && !_forceEnd&& !_otherEnd) {	
//			Dprint.println(_pName + " IS RUNNING ");
			
			while(!_forceEnd && !_otherEnd &&_authorizedPause && !_dA.remainingTask())
				pauses();
			_authorizedPause =true;
			
			if (!_rcvdBox.isEmpty()) {
				receiveMsgs();
				
			}
			// mettre impliquants locaux ici
			if(!(!_forceEnd && !_otherEnd &&_authorizedPause && !_dA.remainingTask()))
				_dA.otherTask();
			
			
		}
		
//		Dprint.println(_pName+ " after loop "+!_dA.isTerminated()+"  "+ !isTimeOut() +"  "+ !_forceEnd+"  "+ !_otherEnd);
		terminate();
//				Dprint.println(_pName+" HAS TERMINATED ");
	}
	
	public  synchronized void  terminate(){
		// Since this method is synchronized when isTerminating is true,
		// the entire bas ever been executed
//		Dprint.println(_pName+ " is waiting _endLock Terminate");
		if(_endLock.tryLock())
			_endLock.lock();
//		Dprint.println(_pName+ " gets _endLock Terminate");
		try{
		if(_isTerminating) return; 
			_isTerminating = true;
			stopChrono();
			
			
			_dA.terminate();
//			sendMsg(_pName,"Msger",MTypeNetwork.Close,null);
			
//			printStat("workingTime: "+_workingTime+"\n");
//			printStat("totalTime: "+_totalTime+"\n");
//			_dprint.closeWriters();
			_pD = null;
			_dA = null;
			ArrayList<String> a = new ArrayList<String>();
			a.add(_pName);
			if(!_forceEnd)
				_msger.notifySmthTo("end", a);
			
			_rcvdBox = null ;
			_sentBox = null ;
			_msger = null;
		}finally{
			_endLock.unlock();
//			Dprint.println(_pName+ "relaeses _endLock Terminate");
		}
	//		Dprint.println(_pName+" excute terminate ");
	}
	
	// other tools
	public String curTime(){
		return (new Date(System.currentTimeMillis())).toString();
	}
}
