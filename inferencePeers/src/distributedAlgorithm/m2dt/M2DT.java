package distributedAlgorithm.m2dt;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;


import peers.LocalPeer;

import peers.PeerDescription;
import propositionalLogic.Base_AL;
import propositionalLogic.Base;
import propositionalLogic.DPLLIter;

//import tools.Dprint;
import tools.Dprint;
import tools.Voc2SeenPeers;
import communication.*;
import distributedAlgorithm.DistributedAlgorithm;
import distributedAlgorithm.m2dt.MsgContent.MImpl;
import distributedAlgorithm.m2dt.MsgContent.MVocOfInterest;
import distributedAlgorithm.m2dt.MsgContent.MendM2DT;

public class M2DT implements DistributedAlgorithm {
	
	protected String _daName;
	
	protected String _name;
	
	protected String dirOut;

	protected LocalPeer _lp;

	/*
	 * Structure related to the communication with the neighbors
	 */
	public ArrayList<String> _neighborsNames;
	
	/* nb  msg  sent to neighbor */
	protected int[] _nbMsgToNeighbors;

	/* nb msg received from neighbors */
	protected int[] _nbMsgFromNeighbors;

	/* nb end msg  received from child */
	protected int[] _nbMsgRcvAtEnd;

	/* collect peers that have accepted this peer as father  */
	protected int _nbFirstMsgExpFromNeighbors;
	protected ArrayList<String> _expChild;
	
	protected Vector<String> _childNames ;
	/* convert the int representing a literal from a neighbor
	 * to a new int */
	protected ArrayList<TreeMap<Integer,Integer>> _convertFromChild; 

	protected String _father = "";
	
	protected int _posIntree =-1;

	protected ArrayList<int[]> _Tresult;

	/*
	 * Structure for managing the distributed
	 * algorithm
	 */
	protected boolean _awaked = false;
	protected volatile boolean _emptyDiag = false;
	protected volatile boolean _emptyRImpl = false;
	protected volatile boolean _terminate = false ;
	
	/*
	 * Structure for managing base the formula
	 */
	
	protected int[][] _clauses;
	
	protected volatile Vector<int[]> _locRImpl;
	
	protected TreeMap<Integer,TreeSet<Integer>> _mappingLitEq;

	protected volatile ArrayList<ArrayList<int[]>> _TChild;

	protected Voc2SeenPeers _voc2Peers;

	protected DPLLIter _dpll;
	
	protected volatile boolean _dpllStart = false;
	
	protected volatile boolean _dpllEnd = false;
	
	protected volatile long _dpllTime = -1;
	
	protected boolean _ackOfVocOfInterest =false;

	protected ArrayList<String> _ackToSend;

	protected ArrayList<String> _fatherChoseAtR;
	
	/* some metrics */

	protected volatile long _firstMsgTime = -1;
	
	private volatile int _nbResultSent =0;
	
	private volatile int _maxNbResultStored = 0;
	
	private volatile int _maxNbRImplicantsStored =0;
	private volatile int _curNbImplicantsStored = 0;
	
	private volatile long _timeLastSent = -1 ;
	private volatile long _maxDiff2Sent = -1;
	private volatile long _sum2Sent = -1;
	

	private volatile long _maxProductTime = -1;
	private volatile long _sumProductTime = 0;
	
	private volatile long _maxAddResultTime = -1;
	private volatile long _sumAddResultTime = 0;
	
	private volatile long _maxAddRImplTime = -1;
	private volatile long _sumAddRImpltTime = 0;

	private volatile long _treeTime=-1;

	private int _nbRImplReceived=0 ;

	private boolean _joinTree = false;

	private int _nbVarsAtStart = 0;

	
	protected ArrayList<String> _diagnosesMsgs;
	/* Constructor */
	public M2DT() {}
	
	
	public void initWith(String name,PeerDescription pD) {
		_daName = name;
		_name = pD.get_pName();
		

		initBaseElements(pD);
		initNeighborStructures(pD.get_neighbors());
		
		_nbVarsAtStart = _voc2Peers.getVoc().size()/2;
		_dpll = new DPLLIter(_clauses, _voc2Peers.getVoc().size(), this);
		_dpll.setTarget(_voc2Peers.getNotYetSeen());
		int [] litOrder = new int[pD.get_diagLit().size()];
		for(int i=0; i< pD.get_diagLit().size();i++)
			litOrder[i]= _dpll.oppositeOf(_voc2Peers.getIntOf(pD.get_diagLit().get(i)));
		_dpll.setLitOrder(litOrder);
		_dpll. setByNext(true);

		
		int nbNeighbors = pD.get_neighbors().size();
		_father = "";
		
		_nbFirstMsgExpFromNeighbors = nbNeighbors;
		_expChild = new ArrayList<String>();
		_expChild.add(_name);
		
		_diagnosesMsgs = new ArrayList<String>();
		
	}

	private void initBaseElements(PeerDescription pD) {

		
		_Tresult = new ArrayList<int[]> ();
		_locRImpl = new Vector<int[]>();
		_voc2Peers = pD.getVoc2Peers();
		_voc2Peers.seePeer(_name);
		
		initEquivLit(pD.get_thMappings());

		_clauses = PeerDescription2BaseInt.clausesOfInt(_voc2Peers.getVoc(), pD.get_th());
		
		_fatherChoseAtR = new ArrayList<String> ();
	}
	
	private void initEquivLit(ArrayList<ArrayList<String>> thMapping){
		_mappingLitEq = new TreeMap<Integer,TreeSet<Integer>>();
		
		for(ArrayList<String> cl :thMapping){

			for(String lit: cl){
				if(!_mappingLitEq.containsKey(_voc2Peers.getIntOf(lit)))
					_mappingLitEq.put(_voc2Peers.getIntOf(lit), new TreeSet<Integer>());
				for(String lit2: cl)
					if(!lit2.equals(lit)){
						int iLit = _voc2Peers.getIntOf(lit);
						int iLit2 = _voc2Peers.getIntOf(lit2);
						_mappingLitEq.get(iLit).add(Base.opposedLit(iLit2));
					}
			}
		}
	}

	

	private void initNeighborStructures(ArrayList<String> neighbors) {
		_neighborsNames = neighbors;
		_childNames = new Vector<String>();
		_childNames.add(_name);
		_ackToSend = new ArrayList<String>();
		_TChild =null;
		_convertFromChild = new ArrayList<TreeMap<Integer,Integer>>();
		_convertFromChild.add(new TreeMap<Integer,Integer>());
		_nbMsgFromNeighbors = new int[_neighborsNames.size()];
		_nbMsgToNeighbors = new int[_neighborsNames.size()];
		_nbMsgRcvAtEnd = new int [_neighborsNames.size()];
		for (int i=0; i<_neighborsNames.size(); i++) {
			_nbMsgFromNeighbors[i]=0;
			_nbMsgToNeighbors[i]=0;
		}
	}
	
	public void setLocalPeer(LocalPeer lp){
		_lp = lp ;
		dirOut=lp._dirOut ;
	}

	public boolean isAwaked() {
		return _awaked;
	}
	
	
	
	
	public  boolean wakeUp() {
		if (_awaked == false) {
			_awaked = true;
			
			if (_lp.isStarter()) {
				_father = _name;
				_posIntree = 0;
				
//				Dprint.println(_name + " is the starter");
				
				_ackOfVocOfInterest =true;
				if(_neighborsNames.size()==0){
					
//					Dprint.println("WAKING UP and  RUNNING Dpll for the starter  "+_name);
					
					_TChild = new ArrayList<ArrayList<int[]>> (_childNames.size());
					for(int i=0;i< _childNames.size();i++){
						_TChild.add( new ArrayList<int[]>());
					}
					_dpllStart =true;
					_expChild.remove(_name);
//					_dpll.start();
				}
			}
			
//			Dprint.println(_name + " is the starter");
			
//			 _dpll.start();
			_firstMsgTime  = _lp.getChrono();
			//_lp.printStat("neighbors: "+_neighborsNames+"\n");

			//_lp.printStat("posInTree: "+_posIntree+"\n");
			
			
			for (String neighbor : _neighborsNames) {
				
				if (!neighbor.equals(_name)&&!neighbor.equals(_father)){
//					Dprint.println(_name+" is sending msg to "+neighbor);
					this.sendMsg(MTypeM2DT.Request, _posIntree, neighbor);
				}
			}
			
			
			return true;
		}
		return false;
	}
	
	public void otherTask(){
		
		if(_dpll !=null){
			if(_dpll.isByNext() && _dpllEnd ==false && _dpllStart){
				_dpll.implicants();
			}
		}
		
		
		if(_TChild != null){
			if(! _locRImpl.isEmpty()){
//				Vector<int[]> copy = (Vector<int[]>) _locRImpl.clone();
//				receiveRImpl(_name,copy.toArray(new int[0][0]));
//				_locRImpl.removeAll(copy);
				
//				Dprint.println(_name+" Other task remove one Rimpl");
				int[][] t = {_locRImpl.remove(0)};
				receiveRImpl(_name,t);
				
			}
		}
		
		
		
	}
	
	private void addLocRimpl2Tchild(){
		if(_TChild != null){
//			Dprint.println(_name+" Adding _locRimpl "+_locRImpl.isEmpty());
			if(! _locRImpl.isEmpty()){
				receiveRImpl(_name, _locRImpl.toArray(new int[_locRImpl.size()][1]));
				_locRImpl.clear();
				
			}
		}
	}

	public  boolean remainingTask(){
		if(_dpll !=null)
			if(!_dpllEnd )
				if(_dpllStart)
					return (!_locRImpl.isEmpty() || !_lp._rcvdBox.isEmpty() || (_dpll.isByNext()));
				
			
		if(_TChild != null)
			return (!_locRImpl.isEmpty() || !_lp._rcvdBox.isEmpty());
		
		return  !_lp._rcvdBox.isEmpty();
	}
	
	public  <MContent extends Serializable> boolean receiveMsg(Msg<MContent> m) {
		String sender = m.getSender();

//		Dprint.println(_name + " receives " +m.getType() + " from  " + sender);
		
		_nbMsgFromNeighbors[_neighborsNames.indexOf(sender)]++;

		switch (m.getType()) {
		
		case MTypeM2DT.EmptyRimpl:{
			_emptyRImpl =true;
			for(String child : _childNames){
				if(!child.equals(_name))
					sendMsg(MTypeM2DT.EmptyRimpl, null, child);
			}
			_lp.unPauses();
			break;
		}
		
		case MTypeM2DT.Request: {
//			Dprint.println(_name + " receive " +" a REQUEST " 
//					+ " from  " + sender);
			
			receiveRequest(sender,(Integer) m.getContent());
			break;
		}
		
		case MTypeM2DT.VocOfinterest:{
//			Dprint.println(_name + " receive " +" the Target VOC "
//					+ " from  " + sender);
			Voc2SeenPeers var2Peers = 
				((MVocOfInterest) m.getContent()).getVocOfInterest();
			TreeMap<String,Integer> name2Int =
				((MVocOfInterest) m.getContent()).getName2Int();
			receiveVocOfInterest(sender,var2Peers,name2Int);
			break;
		}
		
		case MTypeM2DT.AckVocOfInterest:{
//			Dprint.println(_name + " receive " +" the ACK VOC "
//					+ " from  " + sender);
			receiveAckVoc(sender);
			break;
		}
		
		case MTypeM2DT.Implicant: {
			
			int[][] formula =((MImpl) m.getContent()).getFormula();
			// translate lit(int) of received formula into local lit (int)
			
//			 Dprint.println(_name +" received from :"+sender+"\n"+Base.base2String(formula));
//			 Dprint.println(_name+" "+_childNames);
//			 Dprint.println("iChild "+" "+_childNames.indexOf(sender));
//			 Dprint.println(_name+" "+_convertFromChild.size());
			for(int[] impls : formula)
				for(int i = 0; i<impls.length; i++){
					int iChild = _childNames.indexOf(sender);
					impls[i] = _convertFromChild.get(iChild).get(impls[i]);
					
				}
			// Dprint.println(" rewritted formula ");
			// Base.printBase(formula);
//			Dprint.println(_name + " receive " +" IMPL "+ Base.base2String(formula)
//					+ " from  " + sender);
			
			receiveRImpl(sender,formula);	
			break;
		}
		
		case MTypeM2DT.End: {
//			Dprint.println(_name + " receive END from "+sender);
			receiveEnd(sender, (MendM2DT) m.getContent());
			break;
		}
		}
		return true;
	}
	
	protected void receiveRequest(String sender, int posInTreeSender) {
		_nbFirstMsgExpFromNeighbors--;
		_nbMsgRcvAtEnd[_neighborsNames.indexOf(sender)]++;
		if (!_lp.isStarter()) {
			if (_father.equals("")) {
				_father = sender;
				_posIntree = posInTreeSender+1;
				wakeUp();
				
				//_lp.closePrinter();
//				Dprint.println(_name+" gets father "+_father);
			}
		}
		prepareThebakwardsOfDiag();
	}
	
	protected void receiveVocOfInterest(String sender,
			Voc2SeenPeers voc2Peers,
			TreeMap<String,Integer> name2Int){
		_nbFirstMsgExpFromNeighbors--;
		_voc2Peers.union(voc2Peers);
		int iChild = _childNames.size();
		_childNames.add(iChild,sender);
		_expChild.add(sender);
		_convertFromChild.add(iChild,new TreeMap<Integer,Integer>());
		for(String lit: name2Int.keySet()){
			_convertFromChild.get(iChild).put(name2Int.get(lit),_voc2Peers.getIntOf(lit));
		}
		
//		String s = _name + " convert From child "+sender+"\n";
//		for (String lit : name2Int.keySet()) {
//			int iLit = _convertFromChild.get(iChild).get(name2Int.get(lit));
//			s = s + lit + "   i" + sender + ":" + name2Int.get(lit) + "   i"
//					+ _name + ":" + iLit + "\n";
//		}
//		Dprint.println(s);

		_ackToSend.add(sender);
		prepareThebakwardsOfDiag();
//			Dprint.println("running DPLL for "+_name);
//			_dpll.implicants();
//			checkEnd();
		
	}
	
	protected  void prepareThebakwardsOfDiag(){
//		 Dprint.println( _name + " prepareThebakwardsOfDiag "+ _nbFirstMsgExpFromNeighbors);
		if (_nbFirstMsgExpFromNeighbors == 0 && _TChild==null) {
			
//			 Dprint.println( _name +" prepareThebakwardsOfDiag "+ "  _locRImpl.size()"+_locRImpl.size()+ "_expChild "+_expChild);
			
			
			_treeTime = _lp.getChrono();
			
			_TChild = new ArrayList<ArrayList<int[]>>();
			for(int i=0;i< _childNames.size();i++){
				_TChild.add( new ArrayList<int[]>());
			}
			
			sendVocOfInterest();
			
			
			
			for (String p : _ackToSend) {
				sendMsg(MTypeM2DT.AckVocOfInterest, null, p);
			}
			
			_timeLastSent = System.currentTimeMillis();
			
			if ( _expChild.contains(_name)) {
				addLocRimpl2Tchild();
					
			}
		}
	}
	
//	protected void addlocalDNF(){
//		
//		_dpll.start();
//		
//			//_expChild.remove(_name);
//
////			Dprint.println("adding _locRimpl for " + _name);
//			if(_locRImpl != null){
//				receiveRImpl(_name,_locRImpl);
////				Base.printBase(_locRImpl);
////				int iName = _childNames.indexOf(_name);
////				Base.printBase(_TChild[iName]);
//			}
//			
//
//			
////			_dpllTime= System.currentTimeMillis();
////			int[][] impls =_dpll.implicants();
////			if(impls!=null)
////				middleResult(impls);
////			_dpllTime = System.currentTimeMillis()-_dpllTime;
//		
//			
//			checkEnd();
//		
//	}

	
	
	
	protected void receiveAckVoc(String sender){
//		Dprint.println(_name+" receives Ac Voc from"+sender + " _nbFirstMsgExpFromNeighbors "+ _nbFirstMsgExpFromNeighbors);
//		Dprint.println(_name+" receives Ac Voc _expChild"+_expChild);
		_ackOfVocOfInterest = true;
		_nbMsgRcvAtEnd[_neighborsNames.indexOf(sender)]++;
		
//		prepareThebakwardsOfDiag();
		
		if(_joinTree){
//			  Dprint.println("END JOINTREE "+ _name );
			_lp._otherEnd = true;
			return;
		}
		
		if(_expChild.contains(_name)){
			_dpllStart =true;
		}else if(_nbFirstMsgExpFromNeighbors<=0
				&& 	_expChild.isEmpty()){
			long  start= System.currentTimeMillis();
			distributeRImplWithTChild( new int[0] ,-1);
			long time = System.currentTimeMillis()-start;
			_sumProductTime += time;
			_maxProductTime = Math.max(_maxProductTime, time);
		}
	}	
	
	protected  void  receiveRImpl(String sender, int[][] thReceive) {
		_expChild.remove(sender);
		int iSender = _childNames.indexOf(sender);
		
		long aTime =0;
		
			
		
			aTime = System.currentTimeMillis();
			
		// _TChild[iSender] =Base.union(_TChild[iSender], thReceive);
			for (int[] imp:thReceive ){
//				Dprint.println(_name + " receives  IMPL "+ _voc2Peers.getSetOfString(imp)+ " from "+ sender);
				aTime = System.currentTimeMillis() -aTime ;
				_TChild.get(iSender).add(imp);
				_maxAddRImplTime = Math.max(aTime, _maxAddRImplTime);
				_sumAddRImpltTime += aTime;
				aTime = System.currentTimeMillis();
			}
			_nbRImplReceived++;
				
			if( _nbFirstMsgExpFromNeighbors <= 0
						&& _expChild.isEmpty()
						&& _ackOfVocOfInterest) {
							for (int[] f_rcvInt : thReceive) {
							
					long  start= System.currentTimeMillis();
					distributeRImplWithTChild(f_rcvInt, iSender);
					long time = System.currentTimeMillis()-start;
					_sumProductTime += time;
					_maxProductTime = Math.max(_maxProductTime, time);
					
					_curNbImplicantsStored =0;
					for(int i=0; i< _TChild.size();i++){
						_curNbImplicantsStored+= _TChild.get(i).size();
					}
					_curNbImplicantsStored += _Tresult.size() ;
					_maxNbRImplicantsStored = Math.max(_maxNbRImplicantsStored, _curNbImplicantsStored);	
				}
			
//			if(_lp.isTimeOut()){
//				terminate();
//				return; 
//			}
		}
		
		
	}
	
	
	protected void sendVocOfInterest(){
		if(_lp.isStarter()){
			_dpllStart = true;
			if(_joinTree){
//				  Dprint.println("END JOINTREE "+ _name );
				_lp._otherEnd = true;
			}
			return;
		}
		
		
		Voc2SeenPeers voc2Send = _voc2Peers.getNotYetSeenVoc();
		TreeMap<String,Integer> name2Int = new TreeMap<String,Integer>(); 
		for (String lit : voc2Send.getVoc()){
			name2Int.put(lit,_voc2Peers.getIntOf(lit));
		}
		MVocOfInterest content = new MVocOfInterest(voc2Send,name2Int);
		
		
		
		sendMsg(MTypeM2DT.VocOfinterest, content, _father);
	}
	
	/*
	 * Do not forget to convert the received ArrayOf String 2 int[]
	 */
	protected void distributeRImplWithTChild(int[] rImpl,int child2Avoid ) {
		
		int[] memImp= new int[_TChild.size()];
		boolean backward = false;
		//int[][][] bRImpl =new int[1][0][0];
		
		if(_TChild.size()==0)
			return;
		
		int iChild = 0;
		while(iChild>=0){
//			Dprint.println(" Examine iChild "+iChild);
			int iImpl =0;	
			if (iChild != child2Avoid) {
				if(backward){
					backward =false;
					iImpl = memImp[iChild]+1;
				}
//				Dprint.println(" iChild is not to avoid ");
				while (iImpl < _TChild.get(iChild).size()) {
					memImp[iChild] = iImpl;
					if (allconsistent(rImpl, memImp, iChild, child2Avoid)) {
						if (iChild == memImp.length - 1) {
							int[] impl = composeImpl(rImpl, memImp, child2Avoid);
							
//							Dprint.println(_name + " rImpl "+ _voc2Peers.getSetOfString(impl));
							
							int[] nvRimpl =Base.remove(impl , _voc2Peers.getSeenIntLit());
							int [][] tab ={nvRimpl};
							sendRimpl(tab);
							if(trivialEnd())
								break;
							
						}else {
							break;
						}
					}
					iImpl++;
					
//					if(_lp.isTimeOut()){
//						terminate();
//						return; 
//					}
				}
			} else {
//					Dprint.print(" Its maybe a leaf ");
				if (iChild == (memImp.length - 1)) {
//					Dprint.print(" The last column is to avoid");
					int[] impl = composeImpl(rImpl, memImp, child2Avoid);
					
//					Dprint.println(_name + " rImpl "+ _voc2Peers.getSetOfString(impl));
					
					int[] nvRimpl =Base.remove(impl , _voc2Peers.getSeenIntLit());
					int [][] tab ={nvRimpl};
					sendRimpl(tab);
					
				}
				
			}
			
			if(trivialEnd())
				break;
			
			if( iChild >=(memImp.length-1)
					|| iImpl>=_TChild.get(iChild).size()
					|| backward){
				backward =true;
				iChild--;
			}else
				iChild++;
		}
		
		
			
		
	}
	
	protected int[] composeImpl(int[] impl, int[] memImp, int col2Avoid){
		
		int rImplSize = impl.length;
		for(int p=0 ; p < memImp.length ;p++){
			if(p!= col2Avoid){
				rImplSize += _TChild.get(p).get(memImp[p]).length;
				}
		}
		if(rImplSize==0)
			return new int[0];
		int[] rImpl = new int[rImplSize];
		System.arraycopy(impl, 0,rImpl,0, impl.length);
		rImplSize = impl.length;
		for(int p=0 ; p < memImp.length ;p++){
			if(p!= col2Avoid){
				int [] implCh = _TChild.get(p).get(memImp[p]);
				System.arraycopy(implCh, 0,rImpl,rImplSize, implCh.length);
				rImplSize += implCh.length;
			}
		}
		return Base.removeDuplicateLit(rImpl);
	}


	
	
	
	protected void sendRimpl(int[][] bRImpl) {
		int[][][] nvbRImpl =new int[1][0][0];
		
		long aTime = System.currentTimeMillis();
		
		for (int[] imp : bRImpl) {
			aTime = System.currentTimeMillis();
			//if (Base_AL.addIfnotSubSubme(_Tresult, imp)) {
				
				aTime = System.currentTimeMillis()- aTime ;
				_maxAddResultTime = Math.max(aTime, _maxAddResultTime);
				_sumAddResultTime += aTime; 
				
				if(imp.length==0 && !_lp.isStarter() ){
					@SuppressWarnings("unchecked") Vector<String> children = ( Vector<String>)_childNames.clone();
					 children.remove(_name);
					 _emptyRImpl =true;
//					 Dprint.println(_name+ " has an EMPTY R-Impl");
					 _lp.notifySmthTo("emptyRimpl",children );
					
					
//					for(String child : _childNames)
//						if(!child.equals(_name))
//							sendMsg(MTypeM2DT.EmptyRimpl, null, child);
//					_lp.unPauses();
					
					
					
					
				}

				if(_lp.isStarter()){
					if(!_emptyDiag ){
						if(imp.length==0  ){
							_emptyDiag = true;
//							Dprint.println(_name + " DIAGNOSIS "+ _voc2Peers.getSetOfString(imp)+" after: "+_lp.getChrono());
							_diagnosesMsgs.add("DIAGNOSIS: "+_voc2Peers.getSetOfString(imp)+" after: "+_lp.getChrono()+"\n");
							_lp.notifySmthTo("emptyDiag", null);
							// sendMsg(MTypeM2DT.EmptyDiag, null, _neighborsNames.get(0));
							
						}else{
//							Dprint.println(_name + " DIAGNOSIS "+ _voc2Peers.getSetOfString(imp)+" after: "+_lp.getChrono());
							if(!_lp.isTimeOut())
								_diagnosesMsgs.add("DIAGNOSIS: "+_voc2Peers.getSetOfString(imp)+" after: "+_lp.getChrono()+"\n");
								
						}
					}

				}else{
					nvbRImpl[0]= Base.union(imp, nvbRImpl[0]);
				}
			}
		//}
		
		_maxNbResultStored =  _Tresult.size()> _maxNbResultStored ?
				_Tresult.size():_maxNbResultStored;
		
		if(nvbRImpl[0].length>0){
			if(!_lp.isStarter()){
				MImpl mImplicants = new MImpl(nvbRImpl[0]);
					
//				Dprint.println(_name+ " _voc2Peers.getVoc "+_voc2Peers.getVoc());
//				Dprint.println(_name+ " _voc2Peers.getNotYetSeenVoc() "+_voc2Peers.getNotYetSeenVoc().getVoc());
				
//				for(int[] imp: nvbRImpl[0])
//					Dprint.println(_name+"  sends "+ _voc2Peers.getSetOfString(imp));
				
				
				long diff2Sent =  System.currentTimeMillis() -_timeLastSent;
				_sum2Sent +=  diff2Sent;
				_maxDiff2Sent = Math.max(_maxDiff2Sent, diff2Sent);
				_timeLastSent = System.currentTimeMillis();
				
				sendMsg(MTypeM2DT.Implicant, mImplicants, _father);
				
			}
			
			_nbResultSent+= nvbRImpl[0].length;
		}
	}

	protected boolean allconsistent(int[] impl, int[] memImp, int col,
			int col2Avoid) {
		for(int i=0;i<col;i++)
			if(i!=col2Avoid && !Base.isConsistent(_TChild.get(col).get(memImp[col]), _TChild.get(i).get(memImp[i])))
				return false;
		return Base.isConsistent(_TChild.get(col).get(memImp[col]),impl);
	}

	// A concaténer avec reception d'un implicant ...
	public  <Impl>  boolean middleResult(Impl imp) {
		// ADD SH LIT
		if(_lp ==null)
			return false;
		if(_lp._endLock.tryLock()){
			if(_lp._isTerminating==true){
				_lp._endLock.unlock();
				return false;
			}
//			Dprint.println(_name +"gets _endLock for middle Result");
			
			_locRImpl.add(addEqLits((int[]) imp ));
			//		Dprint.println("  implicant without mapLit of "+_name+" : "+Base.base2String(impls));

			_dpllTime = _dpll.getDPLLTime();

			
				//			Dprint.println(_name + " local impl "+ _voc2Peers.getSetOfString(impls[i]));
//							Dprint.println(_name + " loc-Rimpl "+ _voc2Peers.getSetOfString(addEqLits(impls[i] )));
		
			

			//		prepareThebakwardsOfDiag();
			//		_lp.notifyAll();
			//_lp.unPauses();
			
			_lp._endLock.unlock();
//			Dprint.println(_name +" releas _endLock for middle Result");
		}
		
		
//		Dprint.println("  implicant with mapLit of "+_name+" : "+Base.base2String(impls));
//		
//		
//		if(_TChild != null){
////			Dprint.println(_name +" is adding rImpl  to _Tchild ");
////			if(_dpll.dpllIsEnd() && _TChild[iName].length==0){
////				impls=Base.union(_locRImpl, impls);
////			}
//			receiveRImpl(_name,impls);
//		}else{
////			Dprint.println(_name +" is adding  rImpl to _locRimpl ");
//			_locRImpl =Base.union(_locRImpl, impls);
//			
//		}
////		_lp.middleResult();
		
		return true;
	}


	/*
	 *   
	 */
	protected void receiveEnd(String pName, MendM2DT content) {
		_nbMsgRcvAtEnd[_neighborsNames.indexOf(pName)]= content.getNbRcvEndMsgChild();
	}
	
	protected boolean trivialEnd(){
		return(_emptyDiag || _emptyRImpl || _lp._forceEnd || _lp._otherEnd); 
	}

	protected  boolean checkEnd() {
	
//		if(_lp.isTimeOut()){
//			return; 
//		}
		
//		Dprint.println(_name+" checkEnd "+
//				" _expChild.isEmpty() "+_expChild.isEmpty()+
//				" _dpll.dpllIsEnd() "+dpllEnded()+
//				" _ackOfVocOfInterest "+_ackOfVocOfInterest+
//				" _locRImpl.isEmpty "+_locRImpl.isEmpty()+
//				" _lp._rcvdBox.isEmpty "+_lp._rcvdBox.isEmpty()
//				);
		
		if(trivialEnd())
			return true;
			
		if ( !(_expChild.isEmpty() && 
				dpllEnded() &&
				_ackOfVocOfInterest &&
				_locRImpl.isEmpty() &&
				_lp._rcvdBox.isEmpty()
		) ){
//			Dprint.println(_name+" is not finished because of "+
//					" _expChild.isEmpty() "+_expChild.isEmpty()+
//					" _dpll.dpllIsEnd() "+_dpll.dpllIsEnd()+
//					" _ackOfVocOfInterest "+_ackOfVocOfInterest
//					);
			return false;
		}
		
		
		for (int i=0;i< _nbMsgRcvAtEnd.length;i++) {
//			Dprint.println(_name+" receive from "+_neighborsNames.get(i)
//					+" at end: "+_nbMsgRcvAtEnd[i]
//					+" but counts "+ _nbMsgFromNeighbors[i]);
			if (_nbMsgRcvAtEnd[i] != _nbMsgFromNeighbors[i]){
//				Dprint.println(_name+" is not ended because "+ _neighborsNames.get(i));
				return false;
			}
		}
		
		
		
		
		setTerminated(true);
		
		return true;
	}

	protected void sendEndMsg() {
		if (_lp.isStarter())
			return;
		else{
//			Dprint.println(" father of "+_name+ " "+_father);
			if(_neighborsNames.contains(_father)){
				MendM2DT cEnd = new MendM2DT(_nbMsgToNeighbors[_neighborsNames.indexOf(_father)] + 1);
				sendMsg(MTypeM2DT.End, cEnd, _father);
			}else{
				Dprint.println(" father unknown at end "+_name);
			}
		}
	}

	protected <MContent extends Serializable> void sendMsg(int type,MContent content, String recipient) {
		
		
//		for (int i=0;i< _nbMsgRcvAtEnd.length;i++) {
//			Dprint.println(_name+" receive from "+_neighborsNames.get(i) +" "+ _nbMsgFromNeighbors[i]
//			                    +" send to it "+ _nbMsgToNeighbors[i]);
//		}
//		Dprint.println(_name+" is sending msg "+type+ " to "+recipient);
		
		_nbMsgToNeighbors[_neighborsNames.indexOf(recipient)]++;
		_lp.sendMsg(_name, recipient, type, content);
	}

	
	
	/*
	 * adds shared variables to local implicants
	 */
	protected int[] addEqLits(int[] rImpl) {
		ArrayList<Integer> lits = new ArrayList<Integer>();
		for(int l :rImpl)
			if(_mappingLitEq.containsKey(l))
				lits.addAll(_mappingLitEq.get(l));
		int[] litToAdd = new int[lits.size()];
		for(int i=0;i<lits.size();i++ )
			litToAdd[i] =lits.get(i).intValue();
		return Base.union(rImpl, litToAdd);
	}
	
	
	
	public  boolean isTerminated() {
		return checkEnd();
	}
	
	public  void setTerminated(boolean b) {
		 _terminate=b;
	}
	
	public  boolean dpllEnded(){
		return _dpllEnd;
	}

	public  void setDpllEnd(boolean b){
		_dpllEnd =b;
	} 
	
	public void writeStats(){
		
		try {
			long liveTime = _lp.getChrono();
			Dprint _dprint = new Dprint(dirOut+File.separatorChar+_name+".stat");

			String finishMode = (_lp.isTimeOut())? "TIMEOUT":"END";
			//Dprint.println("running DPLL for " + _name);
			_dprint.writeStat("father: "+_father+"\n");


			for(String choiceOfFather:_fatherChoseAtR){
				_dprint.writeStat(choiceOfFather+"\n");
			}

			_dprint.writeStat("depth: "+_posIntree+"\n");
			_dprint.writeStat("neighbors: "+_neighborsNames+"\n");
			_dprint.writeStat("finishMode: "+finishMode+"\n");
			_dprint.writeStat("finishAt: "+_lp.getChrono()+"\n");


			_dprint.writeStat("firstMsgTime: "+_firstMsgTime+"\n");
			_dprint.writeStat("treeTime: "+_treeTime+"\n");
			_dprint.writeStat("dpllTime: "+(_dpllTime) +"\n");
			_dprint.writeStat("liveTime: "+liveTime+"\n");
			_dprint.writeStat("workingTime: "+(_lp.getWorkingTime())+"\n");
			_dprint.writeStat("waitingTime: "+(liveTime-_lp.getWorkingTime())+"\n");
			_dprint.writeStat("sumProductTime: "+(_sumProductTime)+"\n");
			_dprint.writeStat("maxProductTime: "+(_maxProductTime)+"\n");

			_dprint.writeStat("sumAddResultTime: "+(_sumAddResultTime)+"\n");
			_dprint.writeStat("maxAddResultTime: "+(_maxAddResultTime)+"\n");

			_dprint.writeStat("sumAddRImpltTime: "+(_sumAddRImpltTime)+"\n");
			_dprint.writeStat("maxAddRImplTime: "+(_maxAddRImplTime)+"\n");


			_dprint.writeStat("workByLiveTime: "+(liveTime<=0?0:(long)_lp.getWorkingTime()/(long)liveTime)+"\n");
			_dprint.writeStat("waitByLiveTime: "+(liveTime<=0?0:(long)(liveTime-_lp.getWorkingTime())/(long)liveTime)+"\n");

			_dprint.writeStat("moyTime2SSent: "+ (_nbResultSent==0?0:_sum2Sent/(_nbResultSent))+"\n");
			_dprint.writeStat("maxTime2SSent: "+ _maxDiff2Sent+"\n");

			_dprint.writeStat("nbResultSent: "+(_nbResultSent)+"\n");
			_dprint.writeStat("productivity: "+((_nbResultSent>0)?((long)_nbRImplReceived/(long)_nbResultSent):0)+"\n");
			_dprint.writeStat("avProductTime: "+((_sumProductTime>0)?((long)_nbRImplReceived/(long)_sumProductTime):0)+"\n");

			int nbLocalRimpls = ((_TChild!=null)?_TChild.get(0).size():0);
			_dprint.writeStat("nbLocalImplicants: "+nbLocalRimpls+"\n");
			_dprint.writeStat("nbLocalRImplBynbCl: "+((long)nbLocalRimpls/(long)_clauses.length+"\n"));

			_dprint.writeStat("nbResultsStoredAtEnd: "+_Tresult.size()+"\n");
			_dprint.writeStat("maxNbResultStored: "+_maxNbResultStored+"\n");

			_dprint.writeStat("nbNbRImplicantsStoredAtEnd: "+_curNbImplicantsStored+"\n");
			_dprint.writeStat("maxNbRImplicantsStored: "+_maxNbRImplicantsStored +"\n");

			_dprint.writeStat("nbClauses: "+_clauses.length+"\n");

			_dprint.writeStat("nbVarsAtStart: "+_nbVarsAtStart +"\n");
			_dprint.writeStat("nbVarsAtEnd: "+(_voc2Peers.getVoc().size()/2)+"\n");
			_dprint.writeStat("nbSharedAtEnd: "+((_voc2Peers.getVoc().size()/2 )-_nbVarsAtStart)+"\n");
			_dprint.writeStat("nbNeighbors: "+_neighborsNames.size()+"\n");

			int nbRcvdMsgs = 0;
			int nbSentMsgs =0;
			for (int i=0;i< _nbMsgRcvAtEnd.length;i++) {
				nbRcvdMsgs += _nbMsgRcvAtEnd[i];
				nbSentMsgs += _nbMsgToNeighbors[i];
			}

			_dprint.writeStat("nbRcvdMsg: "+nbRcvdMsgs+"\n");
			_dprint.writeStat("nbSentMsg: "+nbSentMsgs+"\n");

			for(String msgDiag:_diagnosesMsgs){
				_dprint.writeStat(msgDiag);
			}


			if (_lp.isStarter()&& finishMode.equals("END")){
				//Dprint.println("MINIMAL DIAGNOSES: ");
				for(int[] impl:_Tresult){
//					_dprint.writeStat("MINIMAL DIAGNOSIS: "+_voc2Peers.getSetOfString(impl)+"\n");
					//Dprint.println(_voc2Peers.getSetOfString(impl));
				}
			}

			_dprint.closeStatWriter();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void  notifyEndFor(String obName){
		
		
		if(obName.equals("dpll")){
			
			
			
			if(_lp._endLock.tryLock()){
			
			setDpllEnd(true);
			_dpllTime = _dpll.getDPLLTime();
//			Dprint.println("dpll ends for "+_name+" it take "+_dpllTime);
			
			
			
			prepareThebakwardsOfDiag();
//			if(_lp !=null)
//				_lp.unPauses();
			
//			Dprint.println(_name+" notifyEndFor "+
//					" _expChild.isEmpty() "+_expChild.isEmpty()+
//					" _dpll.dpllIsEnd() "+dpllEnded()+
//					" _ackOfVocOfInterest "+_ackOfVocOfInterest+
//					" _locRImpl.isEmpty "+_locRImpl.isEmpty()+
//					" _lp._rcvdBox.isEmpty "+_lp._rcvdBox.isEmpty()
//					);
			_lp._endLock.unlock();
			}
		}
		
		
		if(obName.equals("emptyRimpl")){
			if(_lp._isTerminating || _emptyRImpl)
				return;
			_emptyRImpl =true;
			_lp._forceEnd = true;
//			Dprint.println("force by Empty RImpl"+_name);
			
			@SuppressWarnings("unchecked") Vector<String> children = ( Vector<String>) _childNames.clone();
			 children.remove(_name); 
			 _lp.unPauses();
			 _lp.notifySmthTo("emptyRimpl",children );
			
			
			
		}
		
		
		
	}
	
	public synchronized void terminate() {
		
		if(!_lp._endLock.tryLock())
			return;
 
		
		if(_dpll!=null){
			_dpll._forceEnd=true;
		}
		_dpll=null;
		
		sendEndMsg();
		writeStats();
		_lp =null;
		 _neighborsNames =null;		
		 _nbMsgToNeighbors=null;
		 _nbMsgFromNeighbors=null;
		 _nbMsgRcvAtEnd=null;
		 _expChild=null;		
		 _childNames=null ;
		_convertFromChild=null; 
		 _Tresult=null;
		 _clauses=null;
		 _locRImpl=null;
		 _mappingLitEq=null;
		 _TChild=null;
		 _voc2Peers=null;
		
	}

	public String getDAName() {
		return _daName;
	}

	
	public void ends(String name) {
		;
	}

	public String getName() {
		return _name;
	}


	@Override
	public void addParams(ArrayList<String> listParams) {
		if(listParams !=null){
			if(!listParams.isEmpty()){
				if(listParams.contains("joinTree")){
					// Dprint.println(_name+ " will stop at the joinTree");
					_joinTree  = true ;
				}
			}
		}
		
	}


	@Override
	public LocalPeer getLocalPeer() {
		return _lp;
	}
	
	


	

	

}
