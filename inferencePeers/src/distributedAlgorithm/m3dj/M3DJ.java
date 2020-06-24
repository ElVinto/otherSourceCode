package distributedAlgorithm.m3dj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import com.sun.org.apache.bcel.internal.generic.NEWARRAY;

import peers.LocalPeer;
import peers.PeerDescription;
import sun.security.util.PendingException;
import tools.Dprint;
import tools.Voc2SeenPeers;

import communication.Msg;
import distributedAlgorithm.m2dt.M2DT;
import distributedAlgorithm.m2dt.MTypeM2DT;

public class M3DJ extends M2DT {

	final int NOVALUE = -1;

	int[] _newEdgesFrom;

	int[] _nbNeighKnownBy;

	int[] _neighMinfill;

	int _minfill = Integer.MAX_VALUE;

	ArrayList<String> _awaitedMinFillFrom;

	ArrayList<String> _likelyChild;
	
	ArrayList<String> _earlyReq;
	
	ArrayList<String> _earlyMerge;
	
	TreeMap<String,Boolean> _earlyVote;

	int _nbNeighMsgRcvd = 0;
	

	int _nbMinfillMsgRcvd = 0;

	int _nbVoteMsgRcvd = 0;
	
	String _diagRef = "";

	boolean _hasRecvReq = false;
	boolean _hasRecvMerge = false;
	boolean rootAtStart = false;
	
	// some metrics
	

	

	public M3DJ() {
		super();
	}

	public void initWith(String name, PeerDescription pD) {
		super.initWith(name, pD);
		_awaitedMinFillFrom = new ArrayList<String>();
		_likelyChild = new ArrayList<String>();
		_earlyMerge = new ArrayList<String>();
		_earlyReq = new ArrayList<String>();
		_earlyVote = new TreeMap<String, Boolean>();
		_newEdgesFrom = new int[pD.get_neighbors().size()];
		_nbNeighKnownBy = new int[pD.get_neighbors().size()];
		
		_neighMinfill = new int[pD.get_neighbors().size()];
		
		for (int i = 0; i < _newEdgesFrom.length; i++) {
			_newEdgesFrom[i] = NOVALUE;
			_nbNeighKnownBy[i] = 0;
			_neighMinfill[i] = NOVALUE;
		}
		// _father = _name;
	}
	
	public boolean wakeUp() {
		if (_awaked == false) {
			// Dprint.println(_name+ " wakeUp ");
			_awaked = true;
			if (_lp.isStarter()) {
				// Dprint.println(_name + " is the starter");
				_Tresult = new ArrayList<int[]>();;
				_ackOfVocOfInterest = true;
				if (_neighborsNames.size() == 0) {
					// Dprint.println("running DPLL for " + _name);
					_expChild.remove(_name);
//					_dpll.start();
					//middleResult(_dpll.implicants());
				}
			}
			
//			_dpll.start();
			//_lp.printStat("neighbors: "+_neighborsNames+"\n");
			//_lp.printStat("nbClauses:"+_clauses.length+"\n");
			_firstMsgTime  = _lp.getChrono();
//			Dprint.println(_name+" is terminated "+isTerminated());
			
			//_lp.printStat("firstRequest: "+_lp.getChrono()+"\n");
			
			
			for (String neigh : _neighborsNames)
				sendMsg(M3DJMsgType.NEIGHBORS, _neighborsNames, neigh);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public <MContent extends Serializable> boolean receiveMsg(Msg<MContent> m) {

		int iSender = _neighborsNames.indexOf(m.getSender());
		_nbMsgFromNeighbors[iSender]++;
		_nbMsgRcvAtEnd[iSender]++;
		switch (m.getType()) {

		case M3DJMsgType.NEIGHBORS: {
			ArrayList<String> neighs = (ArrayList<String>) m.getContent();
			receiveNeighborsOf(m.getSender(), neighs);

			break;
		}

		case M3DJMsgType.MINFILL: {
			receiveMinfill(m.getSender(), (Integer) m.getContent());
			break;
		}

		case M3DJMsgType.VOTE: {
			receiveVote(m.getSender(), (Boolean) m.getContent());
			break;
		}

		case M3DJMsgType.MERGE: {
			receiveMerge(m.getSender());
			break;
		}

		case M3DJMsgType.CHANGE_FATHER: {
			receiveInvertFather(m.getSender());
			break;
		}

		case MTypeM2DT.Request: {
			this.receiveRequest(m.getSender(),(Integer) m.getContent());
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

	private void receiveNeighborsOf(String sender, ArrayList<String> neighs) {
		_nbNeighMsgRcvd++;
		if (!_awaked) {
			wakeUp();
		}

		updateAcquaintance(sender, neighs);

		if (_nbNeighMsgRcvd == _neighborsNames.size()) {
			// Dprint.println(_name+
			// " has received all neighbors of it neighbors");
			sendMinFill();
			prepareVote();
		}
	}
	
	
	
	private void updateAcquaintance(String sender, ArrayList<String> neighs) {
		int iSender = _neighborsNames.indexOf(sender);
		int nbEdge2Add = 0;
		for (String nOfN : neighs) {
			if (_neighborsNames.contains(nOfN)) {
				_nbNeighKnownBy[iSender]++;
				int iNeigh = _neighborsNames.indexOf(nOfN);
				if (_newEdgesFrom[iNeigh] == NOVALUE) {
					nbEdge2Add++;
				}
			}
		}
		_newEdgesFrom[iSender] = nbEdge2Add;
	}
	
	
		

	private void prepareVote() {
		ArrayList<String> selectedNeigh = new ArrayList<String>();
		int max = _nbNeighKnownBy[0];
		selectedNeigh.add(_neighborsNames.get(0));
		for (int iNeigh = 1; iNeigh < _neighborsNames.size(); iNeigh++) {
			if (_nbNeighKnownBy[iNeigh] >= max) {
				if (_nbNeighKnownBy[iNeigh] > max)
					selectedNeigh.clear();
				selectedNeigh.add(_neighborsNames.get(iNeigh));
				max = _nbNeighKnownBy[iNeigh];
			}
		}

		if (selectedNeigh.size() == 1) {
			_father = selectedNeigh.get(0);
		} else {
			_awaitedMinFillFrom.addAll(selectedNeigh);
		}
	}

	private void sendMinFill() {
		int cardOfNeighGraph = _neighborsNames.size();
		for (int nbEdges : _newEdgesFrom)
			cardOfNeighGraph += nbEdges;
		_minfill = ((_neighborsNames.size() * _neighborsNames.size() - 1) / 2)
				- cardOfNeighGraph;

		for (String neigh : _neighborsNames)
			sendMsg(M3DJMsgType.MINFILL, _minfill, neigh);
	}

	private void receiveMinfill(String sender, int minF) {
		// Dprint.println(_name+ " receive minfill from "+sender);
		_nbMinfillMsgRcvd++;
		int iSender = _neighborsNames.indexOf(sender);
		_neighMinfill[iSender] = minF;
		if (_nbMinfillMsgRcvd == _neighborsNames.size()) {
			// Dprint.println(_name+ " has received all minfill ");
			sendVote();
			for(String neigh : _earlyVote.keySet())
				receiveVote(neigh, _earlyVote.get(neigh));
		}
	}

	private void sendVote() {
		if (!_awaitedMinFillFrom.isEmpty()) {
			_father = selectOneNeighbor();
		}
		if (_likelyChild.contains(_father)) {
			_likelyChild.remove(_father);
		}
		for (String neigh : _neighborsNames)
			if (neigh.equals(_father))
				sendMsg(M3DJMsgType.VOTE, true, _father);
			else
				sendMsg(M3DJMsgType.VOTE, false, neigh);
		
		
	}

	private String selectOneNeighbor() {
		String selectedNeigh = "";
		for (String neigh : _awaitedMinFillFrom) {
			if (selectedNeigh.equals(""))
				selectedNeigh = neigh;
			else {
				int iSelect = _neighborsNames.indexOf(selectedNeigh);
				int iNeigh = _neighborsNames.indexOf(neigh);
				if (_neighMinfill[iSelect] > _neighMinfill[iNeigh]) {
					selectedNeigh = neigh;
				} else if (_neighMinfill[iSelect] == _neighMinfill[iNeigh]) {
					if (selectedNeigh.compareTo(neigh) > 0)
						selectedNeigh = neigh;
				}
			}
		}
		return selectedNeigh;
	}

	private void receiveVote(String sender, boolean vote) {
		
		if(_father.equals("")){
			_earlyVote.put(sender,vote);
			return;
		}
		_nbVoteMsgRcvd++;
		
		if (vote) {
			//Dprint.println(_name+" received the vote of "+ sender+ " his father is "+_father);
			if (sender.equals(_father)) {
				if (_name.compareTo(sender) < 0) {
					_father = _name;
					_likelyChild.add(sender);
				}
			} else
				_likelyChild.add(sender);
		}
		
		//assert !(_nbVoteMsgRcvd>_neighborsNames.size());
		if (_nbVoteMsgRcvd == _neighborsNames.size()) {
			// Dprint.println(_name+ " has received the vote of all its
			// neighborhood");
//			Dprint.println(_name + " choose as father: " + _father
//					+ " likelyChildren: " + _likelyChild);

			if (_lp.isStarter()) {
				_hasRecvReq =true;
				_hasRecvMerge=true;
				_diagRef =_name;
				if (_father.equals(_name)){
					rootAtStart =true;
					for (String child : _likelyChild)
						sendMsg(MTypeM2DT.Request, _posIntree, child);
					for (String neigh : _neighborsNames)
						if (!_likelyChild.contains(neigh))
							sendMsg(M3DJMsgType.MERGE, null, neigh);
				}else {
					sendMsg(M3DJMsgType.MERGE, null, _father);	
					_father = _diagRef;
//					Dprint.println(_name+" choses as father "+_father);
				}
			}else{
				for(String p: _earlyReq){
					receiveRequest(p,_posIntree);
				}
				for(String p:_earlyMerge){
					receiveMerge(p);
				}
			}
		}

	}

	private void receiveMerge(String sender) {
		
//		boolean toPrint = true ; 
		
		if(_nbVoteMsgRcvd!=_neighborsNames.size()){
			_earlyMerge.add(sender);
			return;
		}

		if(_diagRef.equals(""))
			if(!_likelyChild.contains(sender)|| _name.equals(_father))
				_nbFirstMsgExpFromNeighbors--;
			else{
				;// If sender stay a child _nbFirstMsgExpFromNeighbors-- with VocOfInterest
				;// else sender became a father _nbFirstMsgExpFromNeighbors-- in Invert Father
				;// If sender is a root 
			}
		else
			if(!_likelyChild.contains(sender)){
				_nbFirstMsgExpFromNeighbors--;
				; // The sender and pi cannot change father
			}else{
				;// _nbFirstMsgExpFromNeighbors-- with VocOfInterest
			}
		
		if (!_hasRecvMerge&&_nbVoteMsgRcvd==_neighborsNames.size()) {
			_hasRecvMerge = true;
			_diagRef = sender;
			
			if (_father.equals(_name)) {
				receiveInvertFather(sender);
//				toPrint= false;
			} else {
				sendMsg(M3DJMsgType.MERGE, null, _father);
			}
			return;
		}
		
//		if (toPrint)
//			Dprint.println(_name + " recv Merge from "+sender+ " nb Msg Exp "+_nbFirstMsgExpFromNeighbors);		
		prepareThebakwardsOfDiag();
	}

	protected void receiveRequest(String sender, int posInTreeSender) {
	
		if(_nbVoteMsgRcvd!=_neighborsNames.size()){
			_earlyReq.add(sender);
			_posIntree = posInTreeSender;
			return;
		}
		
		_posIntree = posInTreeSender+1;
		_nbFirstMsgExpFromNeighbors -- ;
	
//		Dprint.println(_name + " recv recv Req from "+sender 
//				+ " nb Msg Exp "+_nbFirstMsgExpFromNeighbors);
		
		
		if (!_hasRecvReq&&_nbVoteMsgRcvd==_neighborsNames.size()) {
			_hasRecvReq = true;
			_hasRecvMerge = true;
			_diagRef = sender;
			for (String child : _likelyChild)
				sendMsg(MTypeM2DT.Request, _posIntree, child);

			for (String neigh : _neighborsNames)
				if (!_likelyChild.contains(neigh) && !neigh.equals(_father))
					sendMsg(M3DJMsgType.MERGE, null, neigh);
			
		}else{
			
		}
		prepareThebakwardsOfDiag();
	}

	protected void receiveVocOfInterest(String sender,
			Voc2SeenPeers voc2Peers,
			TreeMap<String,Integer> name2Int){
		
//		Dprint.println(_name + " recv Voc from "+sender);

		super.receiveVocOfInterest(sender, voc2Peers, name2Int);
	}
	
	private void receiveInvertFather(String sender) {
		
		if (!_father.equals(_diagRef)) {
			
//			 Here we count the merge rcvd that we have'nt take into account
			if(_likelyChild.contains(_diagRef)&&!_father.equals(_name))
				_nbFirstMsgExpFromNeighbors -- ;
			
			if(!_father.equals(_name))
				_likelyChild.add(sender);
			
			changeDirection();
			_likelyChild.remove(_diagRef);
			for (String child : _likelyChild)
				if(!child.equals(sender))
					sendMsg(MTypeM2DT.Request, _posIntree, child);

			for (String neigh : _neighborsNames)
				if (!_likelyChild.contains(neigh) && !neigh.equals(_father))
					sendMsg(M3DJMsgType.MERGE, null, neigh);
			
		}else{
//			 Here we wait that VocOfInterest decreases _nbFirstMsgExpFromNeighbors --
			_likelyChild.add(sender);
			if(_lp.isStarter()&& !rootAtStart){
				rootAtStart =true;
				for (String child : _likelyChild)
					if(!child.equals(sender))
						sendMsg(MTypeM2DT.Request, _posIntree, child);

				for (String neigh : _neighborsNames)
					if (!_likelyChild.contains(neigh) && !neigh.equals(_father))
						sendMsg(M3DJMsgType.MERGE, null, neigh);
			}
			
		}
//		Dprint.println(_name + " recv Invert Father from "+sender 
//				+ " nb Msg Exp "+_nbFirstMsgExpFromNeighbors);
		
		prepareThebakwardsOfDiag();
	}

	private void changeDirection() {
		_father = _diagRef;
		sendMsg(M3DJMsgType.CHANGE_FATHER, null, _father);
//		Dprint.println(_name + " changes its father to " + _father);
	}

}
