package initializer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import main.DAsFullNetworkLauncher;


import communication.MsgBox;
import communication.MyAddress;
import communication.GlobalMsgerRcv;
import communication.LocalPeerThread;
import communication.Msg;
import communication.Msger;
import distributedAlgorithm.DistributedAlgorithm;



import peers.LocalPeer;
import peers.PeerDescription;
import specificException.InvalidArgumentException;
import tools.Dprint;

/* Cette classe :
 *  - cr�e les pairs d'inference sur une machine
 *  - initialise les connections r�seaux
 *  - initialise les boites d'envoies et de receptions de chaque pair */

public class LocalNetworkBuilder extends LocalPeerThread {

	String _peerStarter = "";

	MyAddress _locServ ;
	
	CountDownKiller _killer ;
	
	Msger _lmsger ;
	
	GlobalMsgerRcv _gmsger;

	HashMap<String,PeerDescription> _peerDescriptions ;

	public LocalNetworkBuilder(String peerStarter, ArrayList<String> locServ, 
			String dirIn, String dirOut,
			Class<? extends DistributedAlgorithm> algoClass,
			ArrayList<String> paramsOfAlgo,long delay) throws Exception {
		super(" network builder of "+dirIn);

		_peerStarter = peerStarter;
		
		//Dprint.println(" The out directory  "+dirOut);
		
		// looking for the file .res
		
		File dirNet = new File(dirOut);
		File net =null ;
		// Dprint.println(" The out directory  "+dirNet.getPath());
		for(File f :dirNet.listFiles() ){
			if(f.getName().contains(".res"))
				net = f;
		}
		if(net==null){
			Dprint.println(" The out directory does not contain the network file ");
		}
		
		// checking the peers description directory
		File dPeers = new File(dirIn);
		if (!dPeers.isDirectory()) {
			boolean dirContainsFNC = true;
			for( String fDesc : dPeers.list() ){
				if(fDesc.contains(".fnc")){
					break ;
				}
				dirContainsFNC = false;
			}
			if(!dirContainsFNC){
				Dprint.println(dPeers.getPath());
				Dprint.println(" you have to indicate the peers description directory ");
				return;
			}
		}
		
		
		
		Dprint.initScenarioWriter(dirOut+File.separator+"fic.scenario");

//		Dprint.println("Parsing fic.res ...");
		HashMap<String, MyAddress> adrBook =File2NetworkStructure.parseNetworkFic( locServ,net);
		
		
		
		
		_locServ =adrBook.get("servLoc");

//		Dprint.println("Parsing  peer description .fnc");
		Files2PeerDescriptions pDParser = new Files2PeerDescriptions(dPeers, adrBook);
		_peerDescriptions = new HashMap<String, PeerDescription>();
		for(PeerDescription pD : pDParser.getDescriptions()){
			_peerDescriptions.put(pD.get_pName(), pD);
			
//			for(String neigh :pD.get_neighbors())
//				System.out.print(pD.get_pName()+" "+neigh);
//			System.out.println();
			
		}

//		Dprint.println("Creating global receiver for "+_locServ );
		_gmsger = new GlobalMsgerRcv(_locServ,100 );
		_gmsger.setDelay(delay);

//		Dprint.println("Creating local messenger for "+_locServ );
		_lmsger  = new Msger(3,_locServ ,adrBook,dirOut,_gmsger);
		_lmsger.setDelay(delay);



		_killer = new CountDownKiller(delay,_lmsger);
		_lmsger.setKiller(_killer);
		
		 
//		Dprint.println("Adding "+algoClass.getCanonicalName()+" to all peers ");
		for(PeerDescription pD : _peerDescriptions.values()){
			DistributedAlgorithm dA= algoClass.newInstance();
			dA.initWith(algoClass.getCanonicalName(),pD);
			dA.addParams(paramsOfAlgo);
			LocalPeer lp = new LocalPeer(pD,dA,_lmsger,dirOut);
			lp.setDelay(delay);
			dA.setLocalPeer(lp);
			_killer.addLocalPeer(lp);

		}
		
		// sharing  peer references with other
		for(LocalPeer lp:_killer.getLocalPeers()){
			_lmsger.getCustomers().put(lp.getName(),lp);
			_gmsger.getCustomers().put(lp.getName(),lp);
		}
		
		
//		Dprint.println("Sharing MsgBox reference with messengers);
		for(LocalPeer lp:_killer.getLocalPeers()){
			String pName = lp.getName();
			_lmsger.getCustomersRcvdBox().put(pName, lp.getRcvdBox());
			_lmsger.getCustomersSentBox().put(pName, lp.getSentBox());
			_gmsger.getCustomersRcvdBox().put(pName, lp.getRcvdBox());
		}
		
		// creating the countDownKiller
		
//		Dprint.println(" The graph of peers : ");
//		for(PeerDescription pD :_peerDescriptions.values()){
//			Dprint.println(pD.get_pName()+" "+pD.get_neighbors());
//		}
	}
	
	

	public void run() {

		_gmsger.start();
		
		_lmsger.start();
		
		if (_peerDescriptions.containsKey(_peerStarter)){
			for(LocalPeer lp : _killer.getLocalPeers()){
				if(lp.getName().equals(_peerStarter))
					lp.setStarter(true);
			}
		}

		synchronized(_killer.lockKiller){
		for (LocalPeer lp : _killer.getLocalPeers()) {
			// Dprint.println(lp.getName()+" starts");
			lp.start();
		}
		}
		
		
		
		Timer chrono = new Timer();
		chrono.schedule(_killer, _killer.getChrono().remainingTime()+2000);
		
		try {
//			for (LocalPeer lp : _killer.getLocalPeers()) {
//				if(lp.isAlive()){
//					lp.join();
//					 Dprint.println(lp.getName()+" is died ");
//					}
//				}
//			if(_gmsger.isAlive())
//				_gmsger.join();
//			Dprint.println(" gmsger is died");
			if(_lmsger.isAlive()){
				_lmsger.join();
			_killer.stopAll();
			chrono.cancel();
			}
			// Dprint.println(" lmsger is died ");
		} catch (InterruptedException e) {	e.printStackTrace();}
		
		
		
		
		 // Dprint.println("Local Network builder HAS TERMINATED");
	}
}