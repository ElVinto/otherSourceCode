package tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import propositionalLogic.Base;

public class Voc2SeenPeers implements Serializable{

	// WArning remove the public status of the attributes
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<String> _seen;
	public ArrayList<String> _vocString;
	public ArrayList<ArrayList<String>> _voc2SPeers;
	
	
	public void printVoc2Peers(){
		for (ArrayList<String> tabSp : _voc2SPeers)
			for (String sP : tabSp)
				Dprint.println(sP+" "+ _seen.contains(sP));
	}

	public Voc2SeenPeers(TreeMap<String, TreeSet<String>> voc2Peers) {
		initMembers();
		for (String lit : voc2Peers.keySet()) {
			addLit(lit, voc2Peers.get(lit));
		}
	}

	public Voc2SeenPeers() {
		initMembers();
	}

	private void initMembers() {
		_vocString = new ArrayList<String>();
		_voc2SPeers = new ArrayList<ArrayList<String>>();
		_seen = new ArrayList<String>();
	}

	public void addLit(String lit, TreeSet<String> peers) {
		int i = _vocString.size();
		_vocString.add(i, lit);
		_voc2SPeers.add(i, new ArrayList<String>());
		_voc2SPeers.get(i).addAll(peers);
	}

	public void addPeerTo(String lit, String p) {
		int iLit = _vocString.indexOf(lit);
		_voc2SPeers.get(iLit).add(p);
	}

	private void addLit(String lit, ArrayList<String> spLit) {
		int i = _vocString.size();
		_vocString.add(i, lit);
		_voc2SPeers.add(i, spLit);
	}

	public ArrayList<String> getVoc() {
		return _vocString;
	}
	
	public ArrayList<String> getVocFrom(String p){
		ArrayList<String> result = new ArrayList<String>();
		for(int iLit = 0; iLit< _vocString.size();iLit++ ){
			_voc2SPeers.get(iLit).contains(p);
			result.add(_vocString.get(iLit));
		}
		return result;
	}

	public int getIntOf(String lit) {
		return _vocString.indexOf(lit);
	}

	public String getStringOf(int lit) {
		return _vocString.get(lit);
	}
	
	public ArrayList<String> getSetOfString(int[] lits){
		ArrayList<String> result = new ArrayList<String>();
		for(int lit :lits){
			result.add(getStringOf(lit));
		}
		return result;
	}

	public boolean allPeersHaveBeenSeenForVoc(String lit) {
		int iLit = _vocString.indexOf(lit);
		return _seen.containsAll( _voc2SPeers.get(iLit))
				&& _seen.containsAll(_voc2SPeers.get(Base.opposedLit(iLit)));
	}
	
	public boolean allPeersHaveBeenSeenForLit(String lit) {
		int iLit = _vocString.indexOf(lit);
		return _seen.containsAll( _voc2SPeers.get(iLit));
	}


	public Voc2SeenPeers getNotYetSeenVoc() {
		Voc2SeenPeers result = new Voc2SeenPeers();
		for (String lit : _vocString) {
			if (!allPeersHaveBeenSeenForVoc(lit)) {
				int iLit = _vocString.indexOf(lit);
				result.addLit(lit,
						(ArrayList<String>) _voc2SPeers.get(iLit).clone());
			}
		}
		result._seen.addAll(_seen);
		return result;
	}
	
	
	public int[] getNotYetSeen() {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (String lit : _vocString) {
			if (!allPeersHaveBeenSeenForVoc(lit)) {
				int iLit = _vocString.indexOf(lit);
				res.add(iLit);
			}
		}
		int[] a = new int[res.size()];
		for (int i = 0; i < res.size(); i++) {
			a[i] = res.get(i);
		}
		return a;
	}

	public int[] getSeenIntLit() {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (String lit : _vocString) {
			if (allPeersHaveBeenSeenForLit(lit)) {
				int iLit = _vocString.indexOf(lit);
				res.add(iLit);
			}
		}
		int[] a = new int[res.size()];
		for (int i = 0; i < res.size(); i++) {
			a[i] = res.get(i);
		}
		return a;
	}

	public void union(Voc2SeenPeers vSp) {
		for(String pName :vSp._seen)
			seePeer(pName);
		
		for (String lit : vSp._vocString) {
			int iLitVSp = vSp._vocString.indexOf(lit);
			if (_vocString.contains(lit)) {
				int iLit = _vocString.indexOf(lit);
				for (String p : vSp._voc2SPeers.get(iLitVSp))
					if (!_voc2SPeers.get(iLit).contains(p))
						_voc2SPeers.get(iLit).add(p);
			} else {
				addLit(lit, vSp._voc2SPeers.get(iLitVSp));
			}
		}
	}

	public void seePeer(String p) {
		if (!_seen.contains(p)) {
			_seen.add(p);
		}
	}


}
