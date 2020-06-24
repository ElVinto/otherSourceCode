package distributedAlgorithm.m2dt.MsgContent;

import java.io.Serializable;
import java.util.TreeMap;

import tools.Voc2SeenPeers;

public class MVocOfInterest implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Voc2SeenPeers _vocOfInterest = null ;
	
	TreeMap<String,Integer> _name2Int =null;
	
	public MVocOfInterest(Voc2SeenPeers voc,
			TreeMap<String,Integer> name2Int){
		_vocOfInterest= voc;
		_name2Int = name2Int;
	}

	public Voc2SeenPeers getVocOfInterest() {
		return _vocOfInterest;
	}
	
	public TreeMap<String,Integer> getName2Int(){
		return _name2Int;
	}
	
	
}
