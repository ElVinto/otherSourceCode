package communication;

import initializer.CountDownKiller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import java.util.HashMap;

import java.util.Vector;

import javax.swing.text.html.HTMLDocument.Iterator;

import distributedAlgorithm.m2dt.M2DT;
import distributedAlgorithm.m2dt.MTypeM2DT;

import peers.LocalPeer;

import tools.Dprint;

/* A messenger is local to a process it
 * take msg from local box and lay it down to 
 * another localbox or global box */
public class Msger extends LocalPeerThread {

	int _nbDeliverByPeer = 1;

	HashMap<String, MyAddress> _adrBook;

	HashMap<MyAddress, Socket> _socketExt;

	HashMap<String,LocalPeer> _customers;

	HashMap<String, MsgBox> _customerRcvdBoxes ;

	HashMap<String, MsgBox> _customerSentBoxes ;

	TreeMap<MyAddress, ObjectOutputStream> _writerExt;

	GlobalMsgerRcv _gmsger;

	MyAddress _adServLocal;

	ArrayList<MyAddress> _adrExt;

	Vector<String> _localPeers;

	ArrayList<String> _toRemove;
	Object toRemoveLock ;

	Vector<String> _removedPeers;

	String location="";

	boolean _isReady = false;

	private int nbMsgLocal =0;

	private int nbMsgext= 0;

	private int _nbPeer2Rem;

	private CountDownKiller _killer ;

	private volatile boolean _terminate=false;

	private volatile boolean _forceEnd = false;

	public Object notifySmthLock = new Object(); 


	public Msger(int nbDelivByPeer, MyAddress adServLocal,
			HashMap<String, MyAddress> adrBook,String location,GlobalMsgerRcv gmsger) {
		super("Msger " + adServLocal.toString());

		this.location = location;
		Comparator<MyAddress> c = new Comparator<MyAddress>(){
			public int compare(MyAddress arg0, MyAddress arg1) {

				return arg0.compareTo(arg1);
			}

			//public boolean equals(Object obj){return (this.hashCode()-obj.hashCode()==0);}
		};

		_nbDeliverByPeer = nbDelivByPeer;
		_adrBook = adrBook;
		_adServLocal = adServLocal;
		_gmsger =gmsger;
		_writerExt = new TreeMap<MyAddress, ObjectOutputStream>(c);
		_socketExt = new HashMap<MyAddress, Socket>();
		_customers = new HashMap<String,LocalPeer>();
		_customerRcvdBoxes =  new HashMap<String, MsgBox>();
		_customerSentBoxes =  new HashMap<String, MsgBox>();
		initLocalPeers();
		initAdrServExt(adServLocal, adrBook);


	}

	public HashMap<String,LocalPeer> getCustomers(){
		return _customers;
	}

	public HashMap<String, MsgBox> getCustomersRcvdBox(){
		return _customerRcvdBoxes;
	}

	public HashMap<String, MsgBox> getCustomersSentBox(){
		return _customerSentBoxes;
	}

	public boolean isReady() {
		return _isReady;
	}

	public MyAddress getAddServLoc() {
		return _adServLocal;
	}

	private void initLocalPeers() {
		_localPeers = new Vector<String>();
		_toRemove = new ArrayList<String>();
		toRemoveLock = new Object();

		_removedPeers = new Vector<String>();
		for (String pName : _adrBook.keySet()) {
			if (_adrBook.get(pName).equals(_adServLocal)
					&& !pName.equals("servLoc"))
				_localPeers.add(pName);
		}
		_nbPeer2Rem = _localPeers.size();

	}

	private void initAdrServExt(MyAddress adServLocal,
			HashMap<String, MyAddress> adrBook) {
		_adrExt = new ArrayList<MyAddress>();
		for (MyAddress ad : adrBook.values()) {
			if (!_adrExt.contains(ad) && !ad.equals(adServLocal)) {
				_adrExt.add(ad);
			}
		}
	}

	private void initSocServExt() {
		// Contact the neighborhood
		ArrayList<MyAddress> toCall = new ArrayList<MyAddress>();

		toCall.addAll(_adrExt);
		while (!toCall.isEmpty()) {
			ArrayList<MyAddress> connectedAd = new ArrayList<MyAddress>();
			for (MyAddress ad : toCall) {
				Socket s;
				try {
					InetAddress inAddr = InetAddress.getByName(ad.host());
					s = new Socket(inAddr, ad.port());
					_socketExt.put(ad, s);
					_writerExt.put(ad, new ObjectOutputStream(s
							.getOutputStream()));
					connectedAd.add(ad);
					//					Dprint.println("Loc Msger " + _adServLocal
					//							+ " is  connected to external serv " + ad);
				} catch (Exception e) {
					//					Dprint.println("Loc Msger " + _adServLocal
					//					+ " fail to connect to external serv " + ad);

					_socketExt.remove(ad);
					_writerExt.remove(ad);

					// e.printStackTrace();
				}
			}
			toCall.removeAll(connectedAd);
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//		Dprint.println("list of avaible writer");
		//		for(MyAddress adr : _writerExt.keySet())
		//			Dprint.println(adr);

		_isReady = true;
	}

	public MyAddress getServLocal() {
		return this._adServLocal;
	}

	private boolean emptyMsgBox(String pLocal) {
		MsgBox box = _customerSentBoxes.get(pLocal);
		boolean emptyBox = false;
		int nbMsgsToDeliv = 0;
		if (_nbDeliverByPeer < box.size()) {
			nbMsgsToDeliv = _nbDeliverByPeer;
			emptyBox = false;
			//			setPause(false);
		} else {
			emptyBox = true;
			nbMsgsToDeliv = box.size();
		}

//				Dprint.println("Msger is delivering "+pLocal);
		for (int nb = 0; nb < nbMsgsToDeliv; nb++) {
			if(_forceEnd)
				return true;

			Msg<? extends Serializable> m = box.remove(0);
			if (m == null)
				break;

			//			if (m._type == MTypeNetwork.Close) {
			//				_toRemove.add(pLocal);	
			//				continue;
			//			}
			//			
			//			if (m._type == MTypeM2DT.EmptyDiag) {
			//				notifySmthTo("emptyDiag", null);
			//				break;
			//			}
			//			
			//			if(m._type== MTypeM2DT.EmptyRimpl)
			//				_customerRcvdBoxes.get(m.getRecipient()).add(0,m);
			//			
			String rcpient = m.getRecipient();

//			Dprint.println("Msger "+ _adServLocal +" is delivering a msg type "
//					+ m.getType() + " from " + m.getSender()
//					+ " to " + rcpient
//					+  " priority "+this.getPriority());

			if (_localPeers.contains(rcpient)) {
				nbMsgLocal++;


				_customerRcvdBoxes.get(rcpient).add(m);
				// awakening of the sleepy peer

				_customers.get(rcpient).unPauses();

			} else {

				if(_removedPeers.contains(rcpient))
					continue;

				MyAddress ad = _adrBook.get(rcpient);

				//				Dprint.println("Msger will deliver an extenal message " + m.getType()
				//				+ " for " + rcpient + " from " + m.getSender()
				//				+ " for foreign box  : " + ad);

				try {


					nbMsgext++;
					//					Dprint.println(m.getSender()+" is sending an external msg "
					//							+m.getType()+" to "+m.getRecipient());
					_writerExt.get(ad).writeObject(m); // EXCEPTION NUll pointer Exception


				} catch (Exception e) {
					// we make 3 tentative to re send the message
					if(! isTimeOut()){
						Dprint.println(" EXCEPTION when sending an external msg from "+m.getSender()+" to "+m.getRecipient()+
								"\n adr G Receiver "+ad);
						e.printStackTrace();
					}
					//					Dprint.println(e.getCause()+ " "+e.getMessage());
					//					for(StackTraceElement st :e.getStackTrace())
					//						Dprint.println(st.toString());
					//					int nbTry = 3;
					//					_writerExt.gwhile(!reSendTo(ad,m) && nbTry>0)
					//						nbTry--;
					//					if(nbTry <=0){
					////						Dprint.println(" MSGER get msg Type "
					////								+m.getType()+ " from _locSendBox of "+pLocal+
					////								" to external peer "+ m.getRecipient()+
					////								e.getStackTrace().toString()+" "+e.getMessage());
					//						e.printStackTrace();
					//					}
				}

			}
		}
		return emptyBox;
	}

	//	private boolean reSendTo(MyAddress ad, Msg<? extends Serializable> m ){
	//		try{
	//			Dprint.println("TRY TO RESEND to "+ad.host());
	//			InetAddress inAddr = InetAddress.getByName(ad.host());
	//			Socket s = new Socket(inAddr, ad.port());
	//			_socketExt.get(ad);
	//			_writerExt.put(ad, new ObjectOutputStream(s
	//					.getOutputStream()));
	//			_writerExt.get(ad).writeObject(m);
	//			
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//			return false;
	//		}
	//		return true ;
	//	}

	//
	//	private void terminateAll() {
	//		for(LocalPeer lp : _killer.getLocalPeers()){
	//			lp.notifySmthTo("emptyDiag", null);
	//		}
	//		terminate();
	//	}

	public  void  run() {

		if (!_isReady) {
			//			Dprint.println("LOC MSGER " + _adServLocal
			//					+ " is calling external servers ...");
			//			Dprint.println("Loc Msger:"+_adServLocal +"inits its server sockets");

			initSocServExt();
			_isReady = true;

			//Dprint.println("LOC MSGER " + _adServLocal + " IS READY");
		}

		startChrono();
		try{
			while (_nbPeer2Rem > 0 && ! isTimeOut() && !_forceEnd) {
				boolean allMsgBoxAreEmpty = true;
				//Dprint.println("LOC MSGER " + _adServLocal + " IS RUNNING");
				for (String pLocal : _localPeers) {
					// Dprint.println("Msger examines "+pLocal) ;
					allMsgBoxAreEmpty &= emptyMsgBox(pLocal);
				}

				removePeers();



				if( allMsgBoxAreEmpty){
					//				 Dprint.println("Msger reduce its priority ");
					
					setPriority(Math.min(MIN_PRIORITY+2, getPriority()+1));;
				}else{
					setPriority(Math.max(MAX_PRIORITY-2, getPriority()-1));;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		terminate();
//		Dprint.println("Msger" + _adServLocal.toString() + " HAS TERMINATED");
	}


	@SuppressWarnings("unchecked")
	private void removePeers() {
		//		Dprint.println("MSGER remove peer " + pLocal);

		ArrayList<String> toRemCopy;
		synchronized(toRemoveLock){
			if(_toRemove.isEmpty())
				return;
//			Dprint.println("Msger  gets toRemoveLock Copy");
			toRemCopy = (ArrayList<String>)_toRemove.clone();
//			Dprint.println("Msger relases toRemoveLock Copy ");
		}
		for(String pName : toRemCopy){
			if(_forceEnd)
				return;

			if (_localPeers.contains(pName)) {
				_localPeers.remove(pName);
				//				Dprint.println("_localPeers contains " + pName);
				while (!_customerSentBoxes.get(pName).isEmpty() && !_forceEnd) {
					//	Dprint.println("msger chechBox before remove ");
					emptyMsgBox(pName);
				}
				_nbPeer2Rem--;
				_customerSentBoxes.remove(pName);
				_customerRcvdBoxes.remove(pName);
				synchronized(notifySmthLock){
//					Dprint.println("Msger gets notifySmthLock Rem ");
					_customers.remove(pName);
//					Dprint.println("Msger  relase notifySmthLock Rem");
				}

				int iKiller = -1;
				for(LocalPeer lp : _killer.getLocalPeers())
					if(lp.getName().equals(pName)){
						iKiller =_killer.getLocalPeers().indexOf(lp);
						break;
					}
				if(iKiller!=-1)
					synchronized(_killer.lockKiller){
					_killer.getLocalPeers().remove(iKiller);
					}

//				Dprint.println("msger REMOVE " + pName + " from _local Peers ");
				_gmsger.removeCustomer(pName);
				for (MyAddress ad: _writerExt.keySet()){
					Msg<? extends Serializable> m = new Msg<Integer>(pName,_writerExt.get(ad).toString(),MTypeNetwork.PeerClose,null);
					try {
						_writerExt.get(ad).writeObject(m);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			if(!_removedPeers.contains(pName))
				_removedPeers.add(pName);


		}
		synchronized(toRemoveLock){
//			Dprint.println("Msger  gets toRemoveLock ");
			_toRemove.removeAll(toRemCopy);
//			Dprint.println("Msger  releases toRemoveLock ");
		}


	}

	public  void terminate() {
		if(!_terminate){
			_terminate = true;
			for (MyAddress ad : _socketExt.keySet()) {
				try {
					_writerExt.get(ad).close();
					_socketExt.get(ad).close();
					BufferedWriter w = new BufferedWriter(new FileWriter(location+File.separator+
							_adServLocal.toString()+".msger"));
					w.write("msger: "+ _adServLocal+"\n");
					w.write("nbOutMsg: "+nbMsgext+"\n");
					w.write("nbLocalMsg: "+nbMsgLocal+"\n");
					w.close();
				} catch (Exception ex) {
					Dprint.println(" Exception Socket ever closed ;) ");
				}
			}

			terminateGlobalMsger();
		}
	}


	private void terminateGlobalMsger(){
		_gmsger.terminate();
	}

	public void setKiller(CountDownKiller killer) {
		_killer = killer;

	}

	public  void notifySmthTo(String smth, Collection<String> names) {
		if(smth.equals("emptyRimpl")){
			synchronized(notifySmthLock){

//				Dprint.println("emtyRimpl  gets notifySmthLock ");

				for(String pName:names){
					if(_customers.containsKey(pName)){
					if(_customers.get(pName)._endLock.tryLock()){
//						Dprint.println(pName+" gets _endLock ");
						if(_localPeers.contains(pName)){
							_customers.get(pName).notifySmthTo("emptyRimpl", null);
						}
						_customers.get(pName)._endLock.unlock();
//						Dprint.println(pName+" releases _endLock ");
					}
					}
				}
				
//				Dprint.println("emtyRimpl  releases notifySmthLock ");
				
			}
		}

		if(smth.equals("emptyDiag")){


			synchronized(notifySmthLock){

//				Dprint.println("emptyDiag  gets notifySmthLock ");

				for(String pName: _customers.keySet()){

					if(_localPeers.contains(pName)){
//						Dprint.println("emptyDiag  forces end for "+pName);
						_customers.get(pName)._forceEnd =true;
						_customers.get(pName).unPauses();		
					}
				}

//				Dprint.println("emptyDiag  releases notifySmthLock ");
			}
			_forceEnd = true;
			
		}

		if(smth.equals("end")){
			java.util.Iterator<String> it = names.iterator(); 
			while(it.hasNext())
				synchronized(toRemoveLock){
					String name = it.next();
//					Dprint.println(name+" gets toRemoveLock ");
					_toRemove.add(name);
//					Dprint.println(name+" releases toRemoveLock ");
				}
		}
	}

}
