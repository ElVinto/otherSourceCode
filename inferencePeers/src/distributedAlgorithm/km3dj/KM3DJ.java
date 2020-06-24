package distributedAlgorithm.km3dj;

import distributedAlgorithm.km3dj.MsgContent.MStateAncestor;
import distributedAlgorithm.m3dj.M3DJMsgType;
import java.io.Serializable;
import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

import peers.LocalPeer;
import peers.PeerDescription;
import sun.security.util.PendingException;
import tools.Dprint;
import tools.Voc2SeenPeers;

import communication.Msg;
import distributedAlgorithm.m2dt.M2DT;
import distributedAlgorithm.m2dt.MTypeM2DT;


public class KM3DJ extends M2DT{


	private ArrayList<HashSet<String>> _vocFrom;

	private int [] _evalNeigh;

	private boolean [] _neighStateIsKnown ;

	private ArrayList<String> _msgAncestorRcvdFrom;

	private String _likelyParent = null;

	private ArrayList<String> _likelyChildren;
	
	private ArrayList<String> _children;

	private ArrayList<String> _vMin;

	private ArrayList<String> _myV;

	private ArrayList<ArrayList<ArrayList<String>>> _vRcdFrom;

	private int[] _nbInfRcvdFrom;

	private String _treeRef = null;

	private String _bestRef = null;

	private String _refCycle = null;

	private boolean _hasDetecCycle = false;

	private boolean _isInCycle = false;

	private int _radiusMax= 0;

	private int _radiusCur =0;

	private ArrayList<String> _sendersMsgUnif;

	private ArrayList<Object> _msgChildToOpen;
	
	private ArrayList<Object> _msgAncestorToOpen;
	
	
	private int _prevNbIncommon =0 ;

	public KM3DJ() {
		super();
	}



	public void initWith(String name, PeerDescription pD) {
		super.initWith(name, pD);
		_posIntree = Integer.MAX_VALUE;
		_vMin = new ArrayList<String>(2);_vMin.add("");_vMin.add("");
		_myV = new ArrayList<String>(2);_myV.add("");_myV.add("");
		_myV.set(1,""+_name);
		_vMin.set(1,""+_name);

		_vRcdFrom = new ArrayList<ArrayList<ArrayList<String>>>(_neighborsNames.size());
		_likelyChildren = new ArrayList<String>();
		_children = new ArrayList<String>();
		_msgChildToOpen = new ArrayList<Object>();
		_msgAncestorToOpen = new ArrayList<Object>();
		

		
		initNeihborsStructures();
	}

	private void initNeihborsStructures(){
		_evalNeigh = new int [_neighborsNames.size()];
		_nbInfRcvdFrom = new int[_neighborsNames.size()];
		_neighStateIsKnown = new boolean[_neighborsNames.size()];
		_vocFrom = new ArrayList<HashSet<String>>(_neighborsNames.size());
		_msgAncestorRcvdFrom = new ArrayList<String>();
		_sendersMsgUnif= new ArrayList<String>();
		for(int iPeer=0;iPeer< _neighborsNames.size();iPeer ++){
			_neighStateIsKnown[iPeer] = false;
			_nbInfRcvdFrom[iPeer] = 0;
			_vocFrom.add( new HashSet<String>());
			_vocFrom.get(iPeer).addAll(_voc2Peers.getVocFrom(_neighborsNames.get(iPeer)));
			
			//_evalNeigh[iPeer]=_vocFrom.get(iPeer).size() ;
			_evalNeigh[iPeer]+=Math.pow(_vocFrom.get(iPeer).size() ,(_radiusMax - _radiusCur));
		}
	}

	public void addParams(ArrayList<String> a){
		super.addParams(a);
		if(a!=null){
			if(a.contains("radius")){
				int iRad = a.indexOf("radius")+1;
				if(iRad<a.size()){
					_radiusMax = Integer.valueOf(a.get(iRad));
//					Dprint.println("radius Max: "+_radiusMax);
				}
			}
		}
	}

	public boolean wakeUp() {
		if (_awaked == false) {
			// Dprint.println(_name+ " wakeUp ");
			_awaked = true;
			if (_lp.isStarter()) {
				// Dprint.println(_name + " is the starter");
				_Tresult = new ArrayList<int[]>();;
				_ackOfVocOfInterest = true;
				_posIntree  = 0 ;
				_treeRef = _name ;
				_bestRef = _name ;
				_myV.set(0,""+_posIntree);
				_vMin.set(0,""+_posIntree);
				if (_neighborsNames.size() == 0) {
					// Dprint.println("running DPLL for " + _name);
					_expChild.remove(_name);
					//						_dpll.start();
					//middleResult(_dpll.implicants());
				}

				// simultaneously we built a distributed cover tree
				for(String neigh: _neighborsNames){
					sendMsg(KM3DJMsgType.DEPTH,_posIntree,neigh);
				}
				if(_radiusCur <= _radiusMax){
					sendSharedInfo();
				}
				if(_radiusCur == _radiusMax){
						_likelyParent = selectBestNeighbor();
						_vMin.set(0,""+0);
						sendChild();
					}
				_radiusCur++;

			}

			//_dpll.start();
			//_lp.printStat("neighbors: "+_neighborsNames+"\n");
			//_lp.printStat("nbClauses:"+_clauses.length+"\n");
			_firstMsgTime  = _lp.getChrono();
			//	Dprint.println(_name+" is terminated "+isTerminated());
			//_lp.printStat("firstRequest: "+_lp.getChrono()+"\n");



		}
		return false;
	}


	@SuppressWarnings("unchecked")
	public <MContent extends Serializable> boolean receiveMsg(Msg<MContent> m) {

		int iSender = _neighborsNames.indexOf(m.getSender());
		_nbMsgFromNeighbors[iSender]++;
		_nbMsgRcvAtEnd[iSender]++;


//		Dprint.println(_name+" receives msg "+KM3DJMsgType.typeToString(m.getType())+" ("+
//				(m.getContent()!=null?m.getContent():"")+") from "+m.getSender());

		switch (m.getType()) {

		case KM3DJMsgType.INFO: {
			receiveSharedInfoFrom(m.getSender(), m.getContent());
			break;
		}

		case KM3DJMsgType.DEPTH: {
			receiveDepth(m.getSender(),(Integer) m.getContent());
			if(_likelyParent!=null){
				while(!_msgChildToOpen.isEmpty()){
					Msg<MContent> mE = (Msg<MContent>) _msgChildToOpen.remove(0);
					receiveChild(mE.getSender(),(ArrayList<String>)  mE.getContent());
				}
				while(!_msgChildToOpen.isEmpty()){
					Msg<MContent> mE = (Msg<MContent>) _msgChildToOpen.remove(0);
					receiveAncestorState(mE.getSender(),(MStateAncestor) mE.getContent());
				}
			}
			break;
		}

		case KM3DJMsgType.CHILD: {
			if(_likelyParent==null){
				_msgChildToOpen.add(m);
			}else{
				while(!_msgChildToOpen.isEmpty()){
					Msg<MContent> mE = (Msg<MContent>) _msgChildToOpen.remove(0);
					receiveChild(mE.getSender(),(ArrayList<String>)  mE.getContent());
				}
				while(!_msgChildToOpen.isEmpty()){
					Msg<MContent> mE = (Msg<MContent>) _msgChildToOpen.remove(0);
					receiveAncestorState(mE.getSender(),(MStateAncestor) mE.getContent());
				}
				receiveChild(m.getSender(),(ArrayList<String>)  m.getContent());
				
			}
			break;
		}

		case KM3DJMsgType.ANCESTOR_STATE: {	
			
			if(_likelyParent==null){
				_msgAncestorToOpen.add(m);
			}else{
				while(!_msgChildToOpen.isEmpty()){
					Msg<MContent> mE = (Msg<MContent>) _msgChildToOpen.remove(0);
					receiveChild(mE.getSender(),(ArrayList<String>)  mE.getContent());
				}
				while(!_msgChildToOpen.isEmpty()){
					Msg<MContent> mE = (Msg<MContent>) _msgChildToOpen.remove(0);
					receiveAncestorState(mE.getSender(),(MStateAncestor) mE.getContent());
				}
				receiveAncestorState(m.getSender(),(MStateAncestor) m.getContent());
			}
			
			break;
		}

		case KM3DJMsgType.UPDATE: {
			receiveUpdate(m.getSender(),(String) m.getContent());
			break;
		}

		case KM3DJMsgType.SUBTREE_WIRED: {
			receiveSubTreeWired(m.getSender());
			break;
		}


		default: {
			_nbMsgFromNeighbors[iSender]--;
			_nbMsgRcvAtEnd[iSender]--;
			super.receiveMsg(m);
		}

		}
		return true;
	}


	private <INFO> void receiveSharedInfoFrom(String sender,INFO inf) {
		int iSender = _neighborsNames.indexOf(sender);
		
		updateSharedInfo( iSender, inf);
		_nbInfRcvdFrom[iSender]++;

//		String s = _name+ " _radiusCur "+_radiusCur;
//		for(int i =0; i<_nbInfRcvdFrom.length ;i++ ){
//			s+=" _nbInfRcvdFrom "+_neighborsNames.get(i)
//			+" _nbInfRcvdFrom["+i+"] "+_nbInfRcvdFrom[i];
//		}
//		Dprint.println(s);
		
		for(int nbInf : _nbInfRcvdFrom){
			if(nbInf<_radiusCur){
				return;
			}
		}

		
		if(_radiusCur <= _radiusMax){
			_fatherChoseAtR.add("fatherChoseAt"+_radiusCur+": "+selectBestNeighbor());
			sendSharedInfo();
		}
		if(_radiusCur == _radiusMax){
				_likelyParent = selectBestNeighbor();
				_bestRef = _likelyParent;
				if(_treeRef!=null){
					sendChild();
				}
		}
		_radiusCur++;

	}




	private void receiveDepth(String sender, int depth){
		if(_treeRef==null){
			_treeRef = sender;

			_posIntree= depth+1 ;

			_myV.set(0,""+_posIntree);
			_vMin.set(0,""+_posIntree);

			for(String neigh: _neighborsNames){
				if(!neigh.equals(sender))
					sendMsg(KM3DJMsgType.DEPTH,_posIntree,neigh);
			}

			if(_likelyParent !=null){
				sendChild();
			}

		}
	}


	private void receiveChild(String sender, ArrayList<String> vVal){

		int iSender = _neighborsNames.indexOf(sender);
		_neighStateIsKnown[iSender]= true;
		
		
		if( (vVal == null)){
			
			// If the peer is a leaf in the 1-cyclique composante it returns
			if(_refCycle==null){
				if(allAreTrueIn(_neighStateIsKnown) && _msgAncestorRcvdFrom.containsAll(_likelyChildren)){	
						_isInCycle = false;
						sendMsg(KM3DJMsgType.ANCESTOR_STATE, new MStateAncestor(_isInCycle,_vMin), _likelyParent);
				}
			}else{
				// It is the case where the peer which has detect the cycle ever know that is part of the cycle but
				// has not reveived msg child from all its neighborhood at this time
				if(_msgAncestorRcvdFrom.size()== _likelyChildren.size()-1 && !_msgAncestorRcvdFrom.contains(_refCycle)){
					// if the state of the neighborhood is known.
					if(allAreTrueIn(_neighStateIsKnown))
						sendMsg(KM3DJMsgType.ANCESTOR_STATE, new MStateAncestor(_isInCycle,_vMin), _likelyParent);

				}
			}
		}else{

			//a likely child is added once
			if(!_likelyChildren.contains(sender))
				_likelyChildren.add(sender);

			// a Min value is transmited to parent
			if(isLowerThan(vVal, _vMin, 0)){
				_bestRef = sender;
				_vMin = vVal;
				if(!_hasDetecCycle)
					sendMsg(KM3DJMsgType.CHILD, _vMin, _likelyParent);
				return;
			}

			// One peer detect the loop
			if(isEqualTo(vVal, _vMin, 0)){

				//Dprint.println(_name+" has detected cycle ; _msgAncestorRcvdFrom:"+ _msgAncestorRcvdFrom+" _likelyChildren: "+ _likelyChildren);
				_hasDetecCycle = true;
				_isInCycle = true;
				_refCycle = sender;

				// The peer that has discover the loop unblock the 
				// the wait of min_Antecedent
				if(_msgAncestorRcvdFrom.size()== _likelyChildren.size()-1 && !_msgAncestorRcvdFrom.contains(sender)){
					// if the state of the neighborhood is known.
					if(allAreTrueIn(_neighStateIsKnown))
						sendMsg(KM3DJMsgType.ANCESTOR_STATE, new MStateAncestor(_isInCycle,_vMin), _likelyParent);


				}
				return;
			}

		}
	}



	private void receiveAncestorState(String sender , MStateAncestor sAnces ) {
		
		if(!_msgAncestorRcvdFrom.contains(sender))
			_msgAncestorRcvdFrom.add(sender);

		if(sAnces.isInCycle){
			_isInCycle = true;
		}

		if(isLowerThan(sAnces.vVal, _vMin, 0)){
			_bestRef = sender;
			_vMin = sAnces.vVal;
		}

		if(!_hasDetecCycle){
			// this is in a branche or in the cycle
			if(allAreTrueIn(_neighStateIsKnown) && _msgAncestorRcvdFrom.containsAll(_likelyChildren)){
				sendMsg(KM3DJMsgType.ANCESTOR_STATE, new MStateAncestor(_isInCycle,_vMin), _likelyParent);
			}
		}else{
			// the peer which has detected the cycle
			if(_msgAncestorRcvdFrom.size()== _likelyChildren.size()-1 && !_msgAncestorRcvdFrom.contains(_refCycle)){
				// if the state of the neighborhood is known.
				if(allAreTrueIn(_neighStateIsKnown))
					sendMsg(KM3DJMsgType.ANCESTOR_STATE, new MStateAncestor(_isInCycle,_vMin), _likelyParent);

				return;
			}

			if(_msgAncestorRcvdFrom.containsAll (_likelyChildren)){
				sendMsg(KM3DJMsgType.UPDATE, "remove_Child", sender);
			}
		}



	}


	private void receiveUpdate(String sender, String instr){

		if(instr.equals("remove_Child")){
			_likelyChildren.remove(sender);
			sendMsg(KM3DJMsgType.UPDATE, "remove_parent", sender);
		}

		
		if(instr.equals("remove_parent")){
			if(isEqualTo(_myV, _vMin, 0)){
				
				_likelyParent = _treeRef;
				
				if(!_lp.isStarter()){
					sendMsg(KM3DJMsgType.UPDATE, "wire_subtree", _likelyParent);
					_likelyChildren.remove(_likelyParent);
				}else{
					_sendersMsgUnif.add(_likelyParent);
					for(String  neigh  : _neighborsNames){
						if(!neigh.equals(_likelyParent)){
							sendMsg(KM3DJMsgType.SUBTREE_WIRED,null, neigh);
							}
					}
					
				}
			}else{
				_likelyParent = _bestRef;
				_likelyChildren.remove(_likelyParent);
				sendMsg(KM3DJMsgType.UPDATE, "invert_dir", _likelyParent);
			}
		}

		
		if(instr.equals("invert_dir")){
			
			if(_likelyParent.equals(sender)){
				
				_likelyChildren.add(sender);
				if(isEqualTo(_vMin,_myV,0)){
					_likelyParent = _treeRef;
					if(!_lp.isStarter()){
						sendMsg(KM3DJMsgType.UPDATE, "wire_subtree", _likelyParent);
					}else{
						_sendersMsgUnif.add(_likelyParent);
						for(String  neigh  : _neighborsNames)
							if(!neigh.equals(_likelyParent))
								sendMsg(KM3DJMsgType.SUBTREE_WIRED,null, neigh);
					}
				}else{
					// we follow the path of invertion
					_likelyParent = _bestRef;
					_likelyChildren.remove(_bestRef);
					sendMsg(KM3DJMsgType.UPDATE, "invert_dir", _likelyParent);
				}
			}else{
				
			}
			
		}
		
		if(instr.equals("wire_subtree")){
			if(sender.equals(_likelyParent)){
				// we inform the remaining part of the network
				for(String  neigh  : _neighborsNames)
					if(!neigh.equals(_likelyParent))
						sendMsg(KM3DJMsgType.SUBTREE_WIRED,null, neigh);
			}else{
				_children.add(sender);
					sendMsg(KM3DJMsgType.UPDATE, "wire_subtree", sender);
					
					
					
			}
		}
	}

	private void receiveSubTreeWired(String sender){
		if(!_sendersMsgUnif.contains(sender))
			_sendersMsgUnif.add(sender);
		else
			return;

		if(sender.equals(_likelyParent)){
			for(String  neigh  : _neighborsNames)
				if(!neigh.equals(_likelyParent))
				 sendMsg(KM3DJMsgType.SUBTREE_WIRED,null, neigh);
		}

		if(_sendersMsgUnif.containsAll(_neighborsNames)){
			if(!_lp.isStarter()){
				_father = _likelyParent;
				sendMsg(KM3DJMsgType.SUBTREE_WIRED,null, _likelyParent);
			} else{
				_likelyParent = _name;
				_father = _name;
			}
			_children.addAll(_likelyChildren);
			_nbFirstMsgExpFromNeighbors = _children.size()- (_neighborsNames.size()- _nbFirstMsgExpFromNeighbors);
			
//			Dprint.println(" END KM3DJ  "+_name +"  father: "+_father+"  likelyChildren: "+_likelyChildren);
			
			prepareThebakwardsOfDiag();
		}
	}

	private void sendChild(){
//		Dprint.println(_name+" will be lChild of "+_likelyParent);
		sendMsg(KM3DJMsgType.CHILD, _vMin, _likelyParent);
		for(String neigh : super._neighborsNames)
			if(!neigh.equals(_likelyParent)){
				sendMsg(KM3DJMsgType.CHILD, null, neigh);
			}


	}
	
	private int nbInCommon(Collection< String> inf, int iPeer){
		int nbLitForIntersection = 0;
		for(String lit:_voc2Peers.getVoc()){
			if(_voc2Peers.getVocFrom(_neighborsNames.get(iPeer)).contains(lit)){
				nbLitForIntersection++;
				continue;
			}
		}
		
		return nbLitForIntersection;
	}

	@SuppressWarnings("unchecked")
	private <INFO> void updateSharedInfo(int iSender, INFO inf) {
		if(inf !=null){
			
			int prevSize = _vocFrom.get(iSender).size() ;
			_prevNbIncommon =  nbInCommon((Collection<String>) inf,  iSender);
			_vocFrom.get(iSender).addAll((Collection<String>) inf);
			_evalNeigh[iSender]+=  Math.pow(
					(double)Math.abs(_prevNbIncommon -nbInCommon((Collection<String>) inf,  iSender)),
					(double)(_radiusMax - _radiusCur));
			// _evalNeigh[iSender]+=Math.abs(_vocFrom.get(iSender).size() - prevSize) *(_radiusMax - _radiusCur);
		}
	}

	private void sendSharedInfo() {
		HashSet<String> result = new HashSet<String>();

		for(Collection<String> vocSh :_vocFrom)
			result.addAll(vocSh);

		for(String neigh: _neighborsNames)
			if(_radiusCur==0)
				sendMsg(KM3DJMsgType.INFO, null, neigh);
			else
				sendMsg(KM3DJMsgType.INFO, result, neigh);
	}


	private String selectBestNeighbor() {
		int bestISender = 0;
		for(int iSender = 0; iSender<_neighborsNames.size();iSender ++)
			if (bestISender <_evalNeigh[iSender]){
				bestISender = iSender;
			}
		return _neighborsNames.get(bestISender) ;
	}


	private boolean allAreTrueIn(boolean[] tab){
		boolean alltrue = true;
		for(boolean b: tab)
			alltrue &= b;
		return alltrue;
	}


	private boolean isLowerThan(ArrayList<String> v1, ArrayList<String> v2, int i){
		if(i>= v1.size())
			return false;
		return (v1.get(i).compareTo(v2.get(i)) < 0) ? true: 
			(v1.get(i).compareTo(v2.get(i)) == 0)? isLowerThan(v1,v2,i+1): false;
	}

	private boolean isEqualTo(ArrayList<String> v1, ArrayList<String> v2, int i){
		if(i>= v1.size())
			return true;
		return (v1.get(i).compareTo(v2.get(i)) == 0) ? isEqualTo(v1,v2,i+1): false;
	}



}
