package peers;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import tools.Voc2SeenPeers;

public class PeerDescription {

	String _pName;
	ArrayList<ArrayList<String>> _th;
	
	/*
	 * litNames[i] corresponds to the name of literal i
	 */
	
	ArrayList<ArrayList<String>> _thMappings;
	ArrayList<String> _neighbors;
	Voc2SeenPeers _voc2Peers;
	ArrayList<String> _diagLit;

	 public PeerDescription() {
		_pName = "";
		_th = new ArrayList<ArrayList<String>>();

		_thMappings = new ArrayList<ArrayList<String>>();
		_neighbors = new ArrayList<String>();
		_voc2Peers = new Voc2SeenPeers();
		_diagLit = new ArrayList<String>();
		
	}

	public PeerDescription(String pName, ArrayList<ArrayList<String>> th,
			ArrayList<ArrayList<String>> thMappings,
			ArrayList<String> neighbors,
			Voc2SeenPeers voc2Peers,
			ArrayList<String> diagLit) throws Exception {
		_pName = pName;
		_th = th;
		_thMappings = thMappings;
		_neighbors = neighbors;
		_voc2Peers = voc2Peers;
		_diagLit = diagLit;
	}

	public String get_pName() {
		return _pName;
	}

	public void set_pName(String name) {
		_pName = name;
	}

	public ArrayList<ArrayList<String>> get_th() {
		return _th;
	}


	public void setVoc2Peers(Voc2SeenPeers voc2Peers) {
		_voc2Peers = voc2Peers;
	}

	public void set_th(ArrayList<ArrayList<String>> _th) {
		this._th = _th;
	}

	public ArrayList<ArrayList<String>> get_thMappings() {
		return _thMappings;
	}

	public void set_thMappings(ArrayList<ArrayList<String>> mappings) {
		_thMappings = mappings;
	}

	public ArrayList<String> get_neighbors() {
		return _neighbors;
	}

	public void set_neighbors(ArrayList<String> _neighbors) {
		this._neighbors = _neighbors;
	}

	public Voc2SeenPeers getVoc2Peers() {
		return _voc2Peers;
	}

	public ArrayList<String> get_diagLit() {
		return _diagLit;
	}

	public void set_diagLit(ArrayList<String> lit) {
		_diagLit = lit;
	}

	

	public String toString() {
		String s = _pName;
		for (String neigh : _neighbors)
			s += neigh + " ";
		return s;
	}

}
